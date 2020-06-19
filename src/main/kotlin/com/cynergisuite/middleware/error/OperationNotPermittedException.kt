package com.cynergisuite.middleware.error

class OperationNotPermittedException(
   val path: String? = null,
   val messageTemplate: String
) : Exception()
