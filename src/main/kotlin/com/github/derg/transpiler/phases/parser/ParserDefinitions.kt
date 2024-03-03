package com.github.derg.transpiler.phases.parser

import com.github.derg.transpiler.source.*
import com.github.derg.transpiler.source.ast.*

/**
 * Parses a single statement from the token stream.
 */
fun definitionParserOf(): Parser<AstSymbol> = ParserAnyOf(
    functionParserOf(),
    structParserOf(),
    variableParserOf(),
)

/**
 * Parses a function definition from the token stream.
 */
fun functionParserOf(): Parser<AstFunction> =
    ParserPattern(::functionPatternOf, ::functionOutcomeOf)

private fun functionPatternOf() = ParserSequence(
    "visibility" to visibilityParserOf(),
    "fun" to ParserSymbol(Symbol.FUN),
    "name" to ParserName(),
    "open_parenthesis" to ParserSymbol(Symbol.OPEN_PARENTHESIS),
    "parameters" to ParserRepeating(parameterParserOf(), ParserSymbol(Symbol.COMMA)),
    "close_parenthesis" to ParserSymbol(Symbol.CLOSE_PARENTHESIS),
    "error" to ParserOptional(typeParserOf(Symbol.COLON)),
    "value" to ParserOptional(typeParserOf(Symbol.ARROW)),
    "open_brace" to ParserSymbol(Symbol.OPEN_BRACE),
    "statements" to ParserRepeating(statementParserOf()),
    "close_brace" to ParserSymbol(Symbol.CLOSE_BRACE),
)

private fun functionOutcomeOf(values: Parsers) = AstFunction(
    name = values["name"],
    valueType = values["value"],
    errorType = values["error"],
    parameters = values["parameters"],
    visibility = values["visibility"],
    statements = values["statements"],
)

/**
 * Parses a struct definition from the token stream.
 */
fun structParserOf(): Parser<AstStruct> =
    ParserPattern(::structPatternOf, ::structOutcomeOf)

private fun structPatternOf() = ParserSequence(
    "visibility" to visibilityParserOf(),
    "type" to ParserSymbol(Symbol.TYPE),
    "name" to ParserName(),
    "open_brace" to ParserSymbol(Symbol.OPEN_BRACE),
    "properties" to ParserRepeating(propertyParserOf()),
    "close_brace" to ParserSymbol(Symbol.CLOSE_BRACE),
)

private fun structOutcomeOf(values: Parsers) = AstStruct(
    name = values["name"],
    visibility = values["visibility"],
    properties = values["properties"],
)

/**
 * Parses a variable definition from the token stream.
 */
fun variableParserOf(): Parser<AstVariable> =
    ParserPattern(::variablePatternOf, ::variableOutcomeOf)

private fun variablePatternOf() = ParserSequence(
    "visibility" to visibilityParserOf(),
    "assignability" to assignabilityParserOf(),
    "name" to ParserName(),
    "type" to ParserOptional(typeParserOf(Symbol.COLON)),
    "op" to ParserSymbol(Symbol.ASSIGN),
    "value" to expressionParserOf(),
)

private fun variableOutcomeOf(values: Parsers) = AstVariable(
    name = values["name"],
    type = values["type"],
    value = values["value"],
    visibility = values["visibility"],
    assignability = values["assignability"],
)
