package com.github.derg.transpiler.phases.resolver

import com.github.derg.transpiler.source.ast.*
import com.github.derg.transpiler.source.thir.*
import com.github.derg.transpiler.source.thir.Function
import com.github.derg.transpiler.util.*

/**
 * Defines the function based on the contents provided by the function definition. Once defined, the function will be
 * ready for usage in all other use-cases, such as optimization and code generation.
 */
internal class DefinerFunction(private val symbols: SymbolTable)
{
    operator fun invoke(function: Function, statements: List<AstStatement>): Result<Unit, ResolveError>
    {
        function.scope = symbols.resolveScope(statements).valueOr { return failureOf(it) }
        function.params.forEach { function.scope.symbols.register(it) }
        return successOf()
    }
}

/**
 *
 */
internal class DefinerType(private val symbols: SymbolTable)
{
    operator fun invoke(type: Type, definition: AstType): Result<Unit, ResolveError>
    {
        // TODO: Implement me
        return successOf()
    }
}
