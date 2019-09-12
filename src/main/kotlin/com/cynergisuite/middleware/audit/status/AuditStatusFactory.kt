package com.cynergisuite.middleware.audit.status

import org.apache.commons.lang3.RandomUtils
import java.util.stream.Stream

object AuditStatusFactory {
   private val statuses = listOf(
      AuditStatus(1, "OPENED", "Opened", "audit.status.opened"),
      AuditStatus(2, "IN-PROGRESS", "In Progress", "audit.status.in-progress"),
      AuditStatus(3, "COMPLETED", "Completed", "audit.status.completed"),
      AuditStatus(4, "CANCELED", "Canceled", "audit.status.canceled"),
      AuditStatus(5, "SIGNED-OFF", "Signed Off", "audit.status.signed-off")
   )

   @JvmStatic
   fun opened(): AuditStatus = statuses.first { it.value == "OPENED" }

   @JvmStatic
   fun inProgress(): AuditStatus = statuses.first { it.value == "IN-PROGRESS" }

   @JvmStatic
   fun completed(): AuditStatus = statuses.first { it.value == "COMPLETED" }

   @JvmStatic
   fun signedOff(): AuditStatus = statuses.first { it .value == "SIGNED-OFF" }

   @JvmStatic
   fun values(): List<AuditStatus> {
      return statuses
   }

   @JvmStatic
   fun random(): AuditStatus {
      return statuses[RandomUtils.nextInt(0, statuses.size)]
   }

   @JvmStatic
   fun stream(numberIn: Int = 1): Stream<AuditStatus> {
      val number = if (numberIn > 0 || numberIn <= statuses.size) numberIn else 1

      return statuses.stream().limit(number.toLong())
   }

   @JvmStatic
   fun single(): AuditStatus {
      return stream(1).findFirst().orElseThrow { Exception("Unable to find AuditStatusTypeDomain") }
   }
}
