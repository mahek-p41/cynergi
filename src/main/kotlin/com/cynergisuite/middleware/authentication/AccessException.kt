package com.cynergisuite.middleware.authentication

class AccessException(
   val errorMessage: String,
   val user: String?
): Exception(errorMessage)
