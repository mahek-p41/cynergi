package com.cynergisuite.middleware.darwill

import com.cynergisuite.middleware.schedule.JobResult

class DarwillJobResult(
   private val schedName: String,
   private val failureReason: String? = null,
   private val rowCount: Int,
) : JobResult {
   override fun scheduleName(): String = schedName
   override fun failureReason(): String? = failureReason
   fun rowCount(): Int = rowCount
}
