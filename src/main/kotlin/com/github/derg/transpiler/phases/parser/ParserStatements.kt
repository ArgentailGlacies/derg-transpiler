package com.github.derg.transpiler.phases.parser

import com.github.derg.transpiler.source.*
import com.github.derg.transpiler.source.ast.*
import com.github.derg.transpiler.source.lexeme.*

/**
 * Joins together the [name] with the [operator] and the [rhs] expression.
 */
private fun merge(name: Name, operator: SymbolType, rhs: AstExpression): AstAssignment = when (operator)
{
    SymbolType.ASSIGN          -> AstAssign(name, rhs)
    SymbolType.ASSIGN_PLUS     -> AstAssign(name, AstAdd(AstRead(name), rhs))
    SymbolType.ASSIGN_MINUS    -> AstAssign(name, AstSubtract(AstRead(name), rhs))
    SymbolType.ASSIGN_MULTIPLY -> AstAssign(name, AstMultiply(AstRead(name), rhs))
    SymbolType.ASSIGN_MODULO   -> AstAssign(name, AstModulo(AstRead(name), rhs))
    SymbolType.ASSIGN_DIVIDE   -> AstAssign(name, AstDivide(AstRead(name), rhs))
    else                       -> throw IllegalStateException("Illegal operator $operator when parsing assignment")
}

/**
 * Parses a single statement from the token stream.
 */
fun statementParserOf(): Parser<AstStatement> = ParserAnyOf(
    variableParserOf(),
    assignmentParserOf(),
    branchParserOf(),
    invokeParserOf(),
    raiseParserOf(),
    returnParserOf(),
)

/**
 * Parses a single assignment from the token stream.
 */
private fun assignmentParserOf(): Parser<AstStatement> =
    ParserPattern(::assignmentPatternOf, ::assignmentOutcomeOf)

private fun assignmentPatternOf() = ParserSequence(
    "name" to ParserName(),
    "op" to ParserSymbol(
        SymbolType.ASSIGN,
        SymbolType.ASSIGN_PLUS,
        SymbolType.ASSIGN_MINUS,
        SymbolType.ASSIGN_MULTIPLY,
        SymbolType.ASSIGN_DIVIDE,
        SymbolType.ASSIGN_MODULO,
    ),
    "rhs" to expressionParserOf(),
)

private fun assignmentOutcomeOf(values: Parsers): AstAssignment =
    merge(values["name"], values["op"], values["rhs"])

/**
 * Parses a single branch control from the token stream.
 */
private fun branchParserOf(): Parser<AstStatement> =
    ParserPattern(::branchPatternOf, ::branchOutcomeOf)

private fun branchPatternOf() = ParserSequence(
    "if" to ParserSymbol(SymbolType.IF),
    "predicate" to expressionParserOf(),
    "success" to scopeParserOf(),
    "failure" to ParserOptional(ParserPattern(::branchElsePatternOf, ::branchElseOutcomeOf), emptyList()),
)

private fun branchOutcomeOf(values: Parsers): AstBranch =
    AstBranch(values["predicate"], values["success"], values["failure"])

private fun branchElsePatternOf() =
    ParserSequence("symbol" to ParserSymbol(SymbolType.ELSE), "scope" to scopeParserOf())

private fun branchElseOutcomeOf(values: Parsers): List<AstStatement> =
    values["scope"]

/**
 * Parses a single raise control flow from the token stream.
 */
private fun raiseParserOf(): Parser<AstStatement> =
    ParserPattern(::raisePatternOf, ::raiseOutcomeOf)

private fun raisePatternOf() = ParserSequence(
    "symbol" to ParserSymbol(SymbolType.RAISE),
    "expression" to expressionParserOf(),
)

private fun raiseOutcomeOf(values: Parsers): AstReturnError =
    AstReturnError(values["expression"])

/**
 * Parses a single return control flow from the token stream.
 */
private fun returnParserOf(): Parser<AstStatement> =
    ParserPattern(::returnPatternOf, ::returnOutcomeOf)

private fun returnPatternOf() = ParserSequence(
    "symbol" to ParserSymbol(SymbolType.RETURN),
    "expression" to expressionParserOf(),
)

private fun returnOutcomeOf(values: Parsers): AstReturnValue
{
    val expression = values.get<AstExpression>("expression")
    // TODO: The magic string `_` should not be used. Use the type-information of the function to remove it
    return if (expression == AstRead("_")) AstReturnValue(null) else AstReturnValue(expression)
}

/**
 * Parses a single expression statement from the token stream. Note that the parser does not implement analysis to
 * determine whether the expression has any value or error types.
 */
// TODO: Not a correct implementation of the parser - must also function with error handling
private fun invokeParserOf(): Parser<AstStatement> =
    ParserPattern(::functionCallParserOf) { AstEnter(it) }
