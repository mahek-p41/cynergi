package com.cynergisuite.middleware.audit.status

import org.apache.commons.lang3.RandomUtils
import java.util.stream.Stream

object AuditStatusFactory {
   private val statuses = listOf(
      CREATED,
      IN_PROGRESS,
      COMPLETED,
      CANCELED,
      SIGNED_OFF
   )

   @JvmStatic
   fun created(): AuditStatus = statuses.first { it == CREATED }

   @JvmStatic
   fun inProgress(): AuditStatus = statuses.first { it == IN_PROGRESS }

   @JvmStatic
   fun canceled(): AuditStatus = statuses.first { it == CANCELED }

   @JvmStatic
   fun completed(): AuditStatus = statuses.first { it == COMPLETED }

   @JvmStatic
   fun signedOff(): AuditStatus = statuses.first { it == SIGNED_OFF }

   @JvmStatic
   fun values(): List<AuditStatus> = statuses

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
