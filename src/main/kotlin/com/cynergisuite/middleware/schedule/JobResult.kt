package com.cynergisuite.middleware.schedule

interface JobResult {
   fun scheduleName(): String
   fun failureReason(): String? = null
}
