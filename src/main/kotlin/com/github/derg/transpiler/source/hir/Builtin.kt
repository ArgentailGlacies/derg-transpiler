package com.github.derg.transpiler.source.hir

import com.github.derg.transpiler.phases.resolver.*
import com.github.derg.transpiler.source.*
import java.util.*

/**
 * The collection of all builtin types, functions, literals, and everything else. The builtin scope contains everything
 * which is needed to compile any source code into a valid program.
 */
object Builtin
{
    /**
     * Main entrance to any sort of symbol resolution begins at the global scope level. All other symbols must be
     * registered in a scope nested from the global scope in order to be valid. The global scope will contain all
     * builtin symbols, and should never have any non-builtin symbols registered into it.
     */
    val GLOBAL_SCOPE = Scope(null)
    
    /**
     * Void represents a special type that can never be instantiated. This type is used to represent the absence of a
     * value, where a value would normally be expected. This is commonly used to model functions which has no return
     * value or error, and to capture errors where an expression is used as statements.
     */
    // TODO: Remove the void type; it should never appear in the type system.
    private val VOID = registerStruct(VOID_TYPE_NAME)
    private val VOID_TYPE = typeOf(VOID)
    
    val DIVIDE_BY_ZERO = registerStruct(DIVIDE_BY_ZERO_TYPE_NAME)
    val DIVIDE_BY_ZERO_TYPE = typeOf(DIVIDE_BY_ZERO)
    
    val BOOL = registerStruct(BOOL_TYPE_NAME)
    val BOOL_TYPE = typeOf(BOOL)
    val BOOL_AND = registerInfixOp(Symbol.AND, BOOL_TYPE, BOOL_TYPE, null)
    val BOOL_EQ = registerInfixOp(Symbol.EQUAL, BOOL_TYPE, BOOL_TYPE, null)
    val BOOL_NE = registerInfixOp(Symbol.NOT_EQUAL, BOOL_TYPE, BOOL_TYPE, null)
    val BOOL_NOT = registerPrefixOp(Symbol.NOT, BOOL_TYPE, BOOL_TYPE, null)
    val BOOL_OR = registerInfixOp(Symbol.OR, BOOL_TYPE, BOOL_TYPE, null)
    val BOOL_XOR = registerInfixOp(Symbol.XOR, BOOL_TYPE, BOOL_TYPE, null)
    
    val INT32 = registerStruct(INT32_TYPE_NAME)
    val INT32_TYPE = typeOf(INT32)
    val INT32_LIT = registerLiteral(INT32_LIT_NAME, INT32_TYPE)
    val INT32_EQ = registerInfixOp(Symbol.EQUAL, INT32_TYPE, BOOL_TYPE, null)
    val INT32_GE = registerInfixOp(Symbol.GREATER_EQUAL, INT32_TYPE, BOOL_TYPE, null)
    val INT32_GT = registerInfixOp(Symbol.GREATER, INT32_TYPE, BOOL_TYPE, null)
    val INT32_LE = registerInfixOp(Symbol.LESS_EQUAL, INT32_TYPE, BOOL_TYPE, null)
    val INT32_LT = registerInfixOp(Symbol.LESS, INT32_TYPE, BOOL_TYPE, null)
    val INT32_NE = registerInfixOp(Symbol.NOT_EQUAL, INT32_TYPE, BOOL_TYPE, null)
    val INT32_ADD = registerInfixOp(Symbol.PLUS, INT32_TYPE, INT32_TYPE, VOID_TYPE)
    val INT32_DIV = registerInfixOp(Symbol.DIVIDE, INT32_TYPE, INT32_TYPE, DIVIDE_BY_ZERO_TYPE)
    val INT32_MOD = registerInfixOp(Symbol.MODULO, INT32_TYPE, INT32_TYPE, DIVIDE_BY_ZERO_TYPE)
    val INT32_MUL = registerInfixOp(Symbol.MULTIPLY, INT32_TYPE, INT32_TYPE, VOID_TYPE)
    val INT32_NEG = registerPrefixOp(Symbol.MINUS, INT32_TYPE, INT32_TYPE, VOID_TYPE)
    val INT32_POS = registerPrefixOp(Symbol.PLUS, INT32_TYPE, INT32_TYPE, VOID_TYPE)
    val INT32_SUB = registerInfixOp(Symbol.MINUS, INT32_TYPE, INT32_TYPE, VOID_TYPE)
    
    val INT64 = registerStruct(INT64_TYPE_NAME)
    val INT64_TYPE = typeOf(INT64)
    val INT64_LIT = registerLiteral(INT64_LIT_NAME, INT64_TYPE)
    val INT64_EQ = registerInfixOp(Symbol.EQUAL, INT64_TYPE, BOOL_TYPE, null)
    val INT64_GE = registerInfixOp(Symbol.GREATER_EQUAL, INT64_TYPE, BOOL_TYPE, null)
    val INT64_GT = registerInfixOp(Symbol.GREATER, INT64_TYPE, BOOL_TYPE, null)
    val INT64_LE = registerInfixOp(Symbol.LESS_EQUAL, INT64_TYPE, BOOL_TYPE, null)
    val INT64_LT = registerInfixOp(Symbol.LESS, INT64_TYPE, BOOL_TYPE, null)
    val INT64_NE = registerInfixOp(Symbol.NOT_EQUAL, INT64_TYPE, BOOL_TYPE, null)
    val INT64_ADD = registerInfixOp(Symbol.PLUS, INT64_TYPE, INT64_TYPE, VOID_TYPE)
    val INT64_DIV = registerInfixOp(Symbol.DIVIDE, INT64_TYPE, INT64_TYPE, DIVIDE_BY_ZERO_TYPE)
    val INT64_MOD = registerInfixOp(Symbol.MODULO, INT64_TYPE, INT64_TYPE, DIVIDE_BY_ZERO_TYPE)
    val INT64_MUL = registerInfixOp(Symbol.MULTIPLY, INT64_TYPE, INT64_TYPE, VOID_TYPE)
    val INT64_NEG = registerPrefixOp(Symbol.MINUS, INT64_TYPE, INT64_TYPE, VOID_TYPE)
    val INT64_POS = registerPrefixOp(Symbol.PLUS, INT64_TYPE, INT64_TYPE, VOID_TYPE)
    val INT64_SUB = registerInfixOp(Symbol.MINUS, INT64_TYPE, INT64_TYPE, VOID_TYPE)
    
    // TODO: Support strings somehow
    val STR = registerStruct(STR_TYPE_NAME)
    val STR_TYPE = typeOf(STR)
    val STR_LIT = registerLiteral(STR_LIT_NAME, STR_TYPE)
}

/**
 * Generates a type based on the [struct].
 */
private fun typeOf(struct: HirStruct) = HirTypeData(
    name = struct.name,
    generics = emptyList(),
    mutability = Mutability.IMMUTABLE,
)

/**
 * Registers a new type with the given [name].
 */
private fun registerStruct(name: String) = HirStruct(
    id = UUID.randomUUID(),
    name = name,
    visibility = Visibility.EXPORTED,
    fields = emptyList(),
    methods = emptyList(),
    generics = emptyList(),
).also(Builtin.GLOBAL_SCOPE::register)

/**
 * Defines a new literal with the given [name] and [parameter]. The literal will return the same type as the parameter
 * itself.
 */
private fun registerLiteral(name: String, parameter: HirTypeData) = HirLiteral(
    id = UUID.randomUUID(),
    name = name,
    type = HirTypeCall(value = parameter, error = null, parameters = listOf("" to parameter)),
    visibility = Visibility.EXPORTED,
    instructions = emptyList(),
    variables = emptyList(),
    parameter = paramOf("raw", parameter),
).also(Builtin.GLOBAL_SCOPE::register)

/**
 * Defines a new infix operator for the given [operator]. The [parameter] type will be the same for both the left- and
 * the right-hand side expressions. The operator returns a value of the given [value] and [error] types.
 */
private fun registerInfixOp(operator: Symbol, parameter: HirType, value: HirType, error: HirType?) = HirFunction(
    id = UUID.randomUUID(),
    name = operator.symbol,
    type = HirTypeCall(
        value = value,
        error = error,
        parameters = listOf("lhs" to parameter, "rhs" to parameter),
    ),
    visibility = Visibility.EXPORTED,
    instructions = emptyList(),
    generics = emptyList(),
    variables = emptyList(),
    parameters = listOf(paramOf("lhs", parameter), paramOf("rhs", parameter)),
).also(Builtin.GLOBAL_SCOPE::register)

/**
 * Defines a new prefix operator for the given [operator]. The [parameter] type determines which expressions are legal.
 * The operator returns a value of the given [value] and [error] types.
 */
private fun registerPrefixOp(operator: Symbol, parameter: HirType, value: HirType, error: HirType?) = HirFunction(
    id = UUID.randomUUID(),
    name = operator.symbol,
    type = HirTypeCall(
        value = value,
        error = error,
        parameters = listOf("rhs" to parameter),
    ),
    visibility = Visibility.EXPORTED,
    instructions = emptyList(),
    generics = emptyList(),
    variables = emptyList(),
    parameters = listOf(paramOf("rhs", parameter)),
).also(Builtin.GLOBAL_SCOPE::register)

/**
 * Defines a new function parameter of the given [type].
 */
private fun paramOf(name: String, type: HirType) = HirParameter(
    id = UUID.randomUUID(),
    name = name,
    type = type,
    value = null,
    passability = Passability.IN,
)
