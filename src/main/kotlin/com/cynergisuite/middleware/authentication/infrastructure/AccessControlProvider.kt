package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.AuthenticatedUser
import io.micronaut.core.type.MutableArgumentValue

interface AccessControlProvider {
   fun canUserAccess(user: AuthenticatedUser, asset: String, parameters: MutableMap<String, MutableArgumentValue<*>>): Boolean = false
}
