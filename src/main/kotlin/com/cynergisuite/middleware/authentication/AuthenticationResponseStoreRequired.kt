package com.cynergisuite.middleware.authentication

import io.micronaut.security.authentication.AuthenticationFailed
import java.util.Optional

class AuthenticationResponseStoreRequired(
   private val employeeNumber: Int
) : AuthenticationFailed() {
   override fun getMessage(): Optional<String> = Optional.of(employeeNumber.toString())
}
