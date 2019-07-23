package com.cynergisuite.middleware.authentication

import com.cynergisuite.middleware.localization.SystemCode
import io.micronaut.security.authentication.Authentication

class AccessException(
   val error: SystemCode,
   val user: String?
): Exception(error.getCode()) {

   constructor(error: SystemCode, user: Authentication?) :
      this(
         error = error,
         user = user?.name
      )
}
