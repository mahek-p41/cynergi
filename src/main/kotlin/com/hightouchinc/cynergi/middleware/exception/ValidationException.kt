package com.hightouchinc.cynergi.middleware.exception

import org.eclipse.collections.api.list.ImmutableList
import org.eclipse.collections.impl.factory.Lists

class ValidationException(
   val errors: ImmutableList<ValidationError>
): Exception()

data class ValidationError(
   val path: String,
   val messageTemplate: String,
   val arguments: ImmutableList<Any> = Lists.immutable.empty()
)
