package com.cynergisuite.middleware.authentication

import com.cynergisuite.middleware.localization.SystemCode
import io.micronaut.security.authentication.Authentication

class AccessException(
   val error: SystemCode,
   val user: String?
): Exception(error.getCode()) {

   constructor(error: SystemCode, authentication: Authentication?) :
      this(
         error = error,
         user = authentication?.name
      )
}
