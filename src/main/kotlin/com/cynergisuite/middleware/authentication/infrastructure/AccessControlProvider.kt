package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.user.User
import io.micronaut.core.type.MutableArgumentValue

interface AccessControlProvider {
   fun canUserAccess(user: User, asset: String, parameters: MutableMap<String, MutableArgumentValue<*>>): Boolean = false
}
