package com.github.derg.transpiler.source.ast

import com.github.derg.transpiler.source.Assignability
import com.github.derg.transpiler.source.Mutability
import com.github.derg.transpiler.source.Name
import com.github.derg.transpiler.source.Visibility

/**
 * Structural components within the source code must be defined to describe their behavior fully. While certain objects
 * such as functions may be declared before being defined, all such objects must be defined before being used. The
 * definition of the object varies from object to object.
 */
sealed interface Definition : Node
{
    /**
     * Functions are smaller subroutines of a program which can perform a specialized workload, that are given a [name].
     * Every function may return a [valueType], or raise an [errorType]. The value and error types are not required to
     * be specified. Functions accept any number of [parameters], which allows different outcomes of invoking the
     * function to take place.
     *
     * @param visibility The visibility of the function, to whom it is possible to access.
     * @param statements The executable code which defines the function body.
     */
    data class Function(
        val name: Name,
        val valueType: Name?,
        val errorType: Name?,
        val parameters: List<Parameter>,
        val visibility: Visibility,
        val statements: List<Statement>,
    ) : Definition
    
    /**
     * All data structures are represented as types, which determines the shape of all data within a program. Types are
     * not values within the program, but represents the shapes of values. This shape is given a [name], and may hold
     * any number of additional [properties].
     *
     * @param visibility The visibility of the type, to whom it is possible to access.
     */
    data class Type(
        val name: Name,
        val visibility: Visibility,
        val properties: List<Property>,
    ) : Definition
    
    /**
     * Variables are units which hold a specific [value] and associates the value with a specific [name]. Variables may
     * optionally be given a [type], which is verified against the actual type of the expression. If the [type] is not
     * specified, it is inferred from [value].
     *
     * @param visibility The visibility of the variable, to whom it is possible to access.
     * @param mutability The kind of the variable, to which degree it is mutable.
     */
    data class Variable(
        val name: Name,
        val type: Name?,
        val value: Expression,
        val visibility: Visibility,
        val mutability: Mutability,
        val assignability: Assignability,
    ) : Definition, Statement
}