package com.cynergisuite.middleware.authentication

import io.micronaut.security.authentication.providers.PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import javax.inject.Singleton

@Singleton
class PasswordEncoderService : PasswordEncoder {
   private val encoder = BCryptPasswordEncoder(10)

   override fun encode(rawPassword: String?): String =
      encoder.encode(rawPassword)

   override fun matches(rawPassword: String?, encodedPassword: String?): Boolean =
      encoder.matches(rawPassword, encodedPassword)
}
