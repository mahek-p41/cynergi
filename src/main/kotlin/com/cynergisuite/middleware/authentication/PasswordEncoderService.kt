package com.cynergisuite.middleware.authentication

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import javax.inject.Singleton

@Singleton
class PasswordEncoderService {
   private val encoder = BCryptPasswordEncoder(10)

   fun encode(rawPassword: String?): String =
      encoder.encode(rawPassword)

   fun matches(rawPassword: String?, encodedPassword: String?): Boolean =
      encoder.matches(rawPassword, encodedPassword)
}
