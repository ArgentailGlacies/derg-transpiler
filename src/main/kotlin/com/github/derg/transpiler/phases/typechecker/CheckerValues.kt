package com.github.derg.transpiler.phases.typechecker

import com.github.derg.transpiler.source.thir.*
import com.github.derg.transpiler.utils.*

/**
 * The value checker ensures that the value examined is valid in the context it is used in.
 */
internal class CheckerValue
{
    /**
     * Performs type-checking on the instruction [node]. If there are any type errors detected, an error is returned.
     */
    fun check(node: ThirValue): Result<Unit, TypeError> = when (node)
    {
        is ThirCall       -> handle(node)
        is ThirCatch      -> TODO()
        is ThirLoad       -> TODO()
        is ThirValueBool  -> Unit.toSuccess()
        is ThirValueInt32 -> Unit.toSuccess()
        is ThirValueInt64 -> Unit.toSuccess()
    }
    
    private fun handle(node: ThirCall): Result<Unit, TypeError>
    {
        // Call instances cannot evaluate to an error type.
        if (node.instance.error != null)
            return TypeError.CallContainsError(node.instance).toFailure()
        
        // The instance must evaluate to a callable type.
        if (node.instance.value == null)
            return TypeError.CallMissingValue(node.instance).toFailure()
        if (node.instance.value !is ThirTypeCall)
            return TypeError.CallWrongType(node.instance).toFailure()
        
        // TODO: Support callable structs.
        return Unit.toSuccess()
    }
}