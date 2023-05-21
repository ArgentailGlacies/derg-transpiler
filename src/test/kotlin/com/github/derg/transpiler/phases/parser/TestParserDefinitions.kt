package com.github.derg.transpiler.phases.parser

import com.github.derg.transpiler.source.Assignability
import com.github.derg.transpiler.source.Mutability
import com.github.derg.transpiler.source.Passability
import com.github.derg.transpiler.source.Visibility
import com.github.derg.transpiler.source.lexeme.EndOfFile
import org.junit.jupiter.api.Test

/**
 * Determines whether the current token stream is parsed correctly. The expectation is that there will be [wipCount]
 * number of tokens resulting in [ParseOk.Incomplete], followed by [postOkCount] [ParseOk.Complete], before finally
 * resulting in [ParseOk.Finished].
 */
private fun <Type> Tester<Type>.isChain(wipCount: Int = 0, postOkCount: Int = 0): Tester<Type> =
    isWip(wipCount).isOk(postOkCount).isDone()

class TestParserFunction
{
    private val tester = Tester { functionParserOf() }
    
    @Test
    fun `Given valid token, when parsing, then correctly parsed`()
    {
        // Basic structure must be correctly parsed
        tester.parse("fun foo() {}").isChain(5, 1).isValue(funOf("foo"))
        tester.parse("fun foo() -> Foo {}").isChain(7, 1).isValue(funOf("foo", valType = "Foo"))
        tester.parse("fun foo(): Foo {}").isChain(7, 1).isValue(funOf("foo", errType = "Foo"))
        tester.parse("fun foo(): Foo -> Bar {}").isChain(9, 1).isValue(funOf("foo", valType = "Bar", errType = "Foo"))
        
        // Parameters must be correctly parsed
        tester.parse("fun foo(mut a: Foo) {}").isChain(9, 1)
            .isValue(funOf("foo", params = listOf(parOf("a", type = "Foo", ass = Assignability.ASSIGNABLE))))
        tester.parse("fun foo(ref a: Foo) {}").isChain(9, 1)
            .isValue(funOf("foo", params = listOf(parOf("a", type = "Foo", ass = Assignability.REFERENCE))))
        tester.parse("fun foo(    a: Foo) {}").isChain(8, 1)
            .isValue(funOf("foo", params = listOf(parOf("a", type = "Foo", ass = Assignability.CONSTANT))))
        
        tester.parse("fun foo(in    a: Foo) {}").isChain(9, 1)
            .isValue(funOf("foo", params = listOf(parOf("a", type = "Foo", pas = Passability.IN))))
        tester.parse("fun foo(inout a: Foo) {}").isChain(9, 1)
            .isValue(funOf("foo", params = listOf(parOf("a", type = "Foo", pas = Passability.INOUT))))
        tester.parse("fun foo(out   a: Foo) {}").isChain(9, 1)
            .isValue(funOf("foo", params = listOf(parOf("a", type = "Foo", pas = Passability.OUT))))
        tester.parse("fun foo(move  a: Foo) {}").isChain(9, 1)
            .isValue(funOf("foo", params = listOf(parOf("a", type = "Foo", pas = Passability.MOVE))))
        tester.parse("fun foo(      a: Foo) {}").isChain(8, 1)
            .isValue(funOf("foo", params = listOf(parOf("a", type = "Foo", pas = Passability.IN))))
        
        tester.parse("fun foo(a: Foo, b: Bar) {}").isChain(12, 1)
            .isValue(funOf("foo", params = listOf(parOf("a", type = "Foo"), parOf("b", type = "Bar"))))
        
        // Default values for parameters must be supported
        tester.parse("fun foo(a = 1) {}").isChain(8, 1)
            .isValue(funOf("foo", params = listOf(parOf("a", value = 1))))
        tester.parse("fun foo(a: Foo = 1) {}").isChain(10, 1)
            .isValue(funOf("foo", params = listOf(parOf("a", type = "Foo", value = 1))))
        
        // Visibility must be correctly parsed
        tester.parse("exported  fun foo() {}").isChain(6, 1).isValue(funOf("foo", vis = Visibility.EXPORTED))
        tester.parse("public    fun foo() {}").isChain(6, 1).isValue(funOf("foo", vis = Visibility.PUBLIC))
        tester.parse("protected fun foo() {}").isChain(6, 1).isValue(funOf("foo", vis = Visibility.PROTECTED))
        tester.parse("private   fun foo() {}").isChain(6, 1).isValue(funOf("foo", vis = Visibility.PRIVATE))
        tester.parse("          fun foo() {}").isChain(5, 1).isValue(funOf("foo", vis = Visibility.PRIVATE))
    }
    
    @Test
    fun `Given return statement, when parsing, then correctly parsed`()
    {
        tester.parse("fun f() { return _ }").isChain(7, 1)
            .isValue(funOf("f", valType = null, statements = listOf(returnOf())))
        tester.parse("fun f() -> Int { return 0 }").isChain(9, 1)
            .isValue(funOf("f", valType = "Int", statements = listOf(returnOf(0))))
    }
    
    @Test
    fun `Given invalid token, when parsing, then correct error`()
    {
        tester.parse("").isBad { ParseError.UnexpectedToken(EndOfFile) }
        tester.parse("fun").isWip(1).isBad { ParseError.UnexpectedToken(EndOfFile) }
        tester.parse("fun foo(").isWip(3).isBad { ParseError.UnexpectedToken(EndOfFile) }
    }
    
    @Test
    fun `Given function with variable, when parsing, then correctly parsed`()
    {
        val expected = funOf("foo", statements = listOf(varOf("bar", 0)))
        
        tester.parse("fun foo() { val bar = 0 }").isChain(9, 1).isValue(expected)
    }
}

class TestParserType
{
    private val tester = Tester { typeParserOf() }
    
    @Test
    fun `Given valid segment, when parsing, then correctly parsed`()
    {
        // Basic structure must be correctly parsed
        tester.parse("type Foo {}").isChain(3, 1).isValue(typeOf("Foo"))
        
        // Properties must be correctly parsed
        tester.parse("type Foo { mut val a: Bar }").isChain(8, 1)
            .isValue(typeOf("Foo", props = listOf(propOf("a", type = "Bar", ass = Assignability.ASSIGNABLE))))
        tester.parse("type Foo { ref val a: Bar }").isChain(8, 1)
            .isValue(typeOf("Foo", props = listOf(propOf("a", type = "Bar", ass = Assignability.REFERENCE))))
        tester.parse("type Foo {     val a: Bar }").isChain(7, 1)
            .isValue(typeOf("Foo", props = listOf(propOf("a", type = "Bar", ass = Assignability.CONSTANT))))
        
        tester.parse("type Foo { val a: Bar }").isChain(7, 1)
            .isValue(typeOf("Foo", props = listOf(propOf("a", type = "Bar", mut = Mutability.IMMUTABLE))))
        tester.parse("type Foo { var a: Bar }").isChain(7, 1)
            .isValue(typeOf("Foo", props = listOf(propOf("a", type = "Bar", mut = Mutability.MUTABLE))))
        
        tester.parse("type Foo { exported  val a: Bar }").isChain(8, 1)
            .isValue(typeOf("Foo", props = listOf(propOf("a", type = "Bar", vis = Visibility.EXPORTED))))
        tester.parse("type Foo { public    val a: Bar }").isChain(8, 1)
            .isValue(typeOf("Foo", props = listOf(propOf("a", type = "Bar", vis = Visibility.PUBLIC))))
        tester.parse("type Foo { protected val a: Bar }").isChain(8, 1)
            .isValue(typeOf("Foo", props = listOf(propOf("a", type = "Bar", vis = Visibility.PROTECTED))))
        tester.parse("type Foo { private   val a: Bar }").isChain(8, 1)
            .isValue(typeOf("Foo", props = listOf(propOf("a", type = "Bar", vis = Visibility.PRIVATE))))
        tester.parse("type Foo {           val a: Bar }").isChain(7, 1)
            .isValue(typeOf("Foo", props = listOf(propOf("a", type = "Bar", vis = Visibility.PRIVATE))))
        
        tester.parse("type Foo { val a: Foo val b: Bar }").isChain(11, 1)
            .isValue(typeOf("Foo", props = listOf(propOf("a", type = "Foo"), propOf("b", type = "Bar"))))
        
        // Default values for properties must be supported
        tester.parse("type Foo { val a = 1 }").isChain(7, 1)
            .isValue(typeOf("Foo", props = listOf(propOf("a", value = 1))))
        
        // Visibility must be correctly parsed
        tester.parse("exported  type Foo {}").isChain(4, 1).isValue(typeOf("Foo", vis = Visibility.EXPORTED))
        tester.parse("public    type Foo {}").isChain(4, 1).isValue(typeOf("Foo", vis = Visibility.PUBLIC))
        tester.parse("protected type Foo {}").isChain(4, 1).isValue(typeOf("Foo", vis = Visibility.PROTECTED))
        tester.parse("private   type Foo {}").isChain(4, 1).isValue(typeOf("Foo", vis = Visibility.PRIVATE))
        tester.parse("          type Foo {}").isChain(3, 1).isValue(typeOf("Foo", vis = Visibility.PRIVATE))
    }
    
    @Test
    fun `Given invalid token, when parsing, then correct error`()
    {
        tester.parse("").isBad { ParseError.UnexpectedToken(EndOfFile) }
        tester.parse("type").isWip(1).isBad { ParseError.UnexpectedToken(EndOfFile) }
        tester.parse("type Foo {").isWip(3).isBad { ParseError.UnexpectedToken(EndOfFile) }
    }
}

class TestParserVariable
{
    private val tester = Tester { variableParserOf() }
    
    @Test
    fun `Given valid token, when parsing, then correctly parsed`()
    {
        // Assignability must be correctly parsed
        tester.parse("mut val foo = 0").isChain(4, 1).isValue(varOf("foo", 0, ass = Assignability.ASSIGNABLE))
        tester.parse("ref val foo = 0").isChain(4, 1).isValue(varOf("foo", 0, ass = Assignability.REFERENCE))
        tester.parse("    val foo = 0").isChain(3, 1).isValue(varOf("foo", 0, ass = Assignability.CONSTANT))
        
        // Mutability must be correctly parsed
        tester.parse("val foo = 0").isChain(3, 1).isValue(varOf("foo", 0, mut = Mutability.IMMUTABLE))
        tester.parse("var foo = 0").isChain(3, 1).isValue(varOf("foo", 0, mut = Mutability.MUTABLE))
        
        // Visibility must be correctly parsed
        tester.parse("exported  val foo = 0").isChain(4, 1).isValue(varOf("foo", 0, vis = Visibility.EXPORTED))
        tester.parse("public    val foo = 0").isChain(4, 1).isValue(varOf("foo", 0, vis = Visibility.PUBLIC))
        tester.parse("protected val foo = 0").isChain(4, 1).isValue(varOf("foo", 0, vis = Visibility.PROTECTED))
        tester.parse("private   val foo = 0").isChain(4, 1).isValue(varOf("foo", 0, vis = Visibility.PRIVATE))
        tester.parse("          val foo = 0").isChain(3, 1).isValue(varOf("foo", 0, vis = Visibility.PRIVATE))
    }
    
    @Test
    fun `Given invalid token, when parsing, then correct error`()
    {
        tester.parse("").isBad { ParseError.UnexpectedToken(EndOfFile) }
        tester.parse("val").isWip(1).isBad { ParseError.UnexpectedToken(EndOfFile) }
        tester.parse("val foo =").isWip(3).isBad { ParseError.UnexpectedToken(EndOfFile) }
    }
}