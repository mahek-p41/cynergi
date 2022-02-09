package com.cynergisuite.middleware.schedule

data class ErrorJobResult(
   val exception: Throwable,
   val scheduleName: String,
) : JobResult {
   override fun scheduleName(): String = scheduleName
   override fun failureReason(): String? = exception.message
}
