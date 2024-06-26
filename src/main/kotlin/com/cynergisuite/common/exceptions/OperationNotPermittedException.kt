package com.cynergisuite.common.exceptions

class OperationNotPermittedException(
   val path: String? = null,
   val messageTemplate: String
) : Exception()
