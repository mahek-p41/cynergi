package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.AuthenticatedUser
import io.micronaut.core.type.MutableArgumentValue
import javax.inject.Singleton

@Singleton
class AlwaysAllowAccessControlProvider : AccessControlProvider {
   override fun canUserAccess(user: AuthenticatedUser, asset: String, parameters: MutableMap<String, MutableArgumentValue<*>>): Boolean = true
}
