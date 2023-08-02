package com.cynergisuite.middleware.wow.schedule

import com.cynergisuite.middleware.schedule.JobResult

class WowJobResult(
   private val schedName: String,
   private val failureReason: String? = null,
   private val rowCount: Int,
) : JobResult {
   override fun scheduleName(): String = schedName
   override fun failureReason(): String? = failureReason
   fun rowCount(): Int = rowCount
}
