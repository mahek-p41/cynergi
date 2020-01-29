package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.AuthenticatedUser
import io.micronaut.core.type.Argument

interface AccessControlProvider {
   fun canUserAccess(user: AuthenticatedUser, asset: String, arguments: Array<Argument<Any>>): Boolean = false
}
