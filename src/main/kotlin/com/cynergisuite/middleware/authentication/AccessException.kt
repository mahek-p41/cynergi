package com.cynergisuite.middleware.authentication

import com.cynergisuite.middleware.localization.SystemCode

class AccessException(
   val error: SystemCode,
   val user: String?
): Exception(error.code)
