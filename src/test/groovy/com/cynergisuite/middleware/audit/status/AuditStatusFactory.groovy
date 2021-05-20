package com.cynergisuite.middleware.audit.status


import java.util.stream.Stream

class AuditStatusFactory {
   private static final List<AuditStatus> statuses = [
      Created.INSTANCE,
      InProgress.INSTANCE,
      Completed.INSTANCE,
      Canceled.INSTANCE,
      Approved.INSTANCE
   ]

   static AuditStatus created() { statuses.find { it == Created.INSTANCE } }

   static AuditStatus inProgress() { statuses.find { it == InProgress.INSTANCE } }

   static AuditStatus canceled() { statuses.find { it == Canceled.INSTANCE } }

   static AuditStatus completed() { statuses.find { it == Completed.INSTANCE } }

   static AuditStatus approved() { statuses.find { it == Approved.INSTANCE } }

   static List<AuditStatus> values() { statuses }

   static AuditStatus random() {
      return statuses.random()
   }

   static Stream<AuditStatus> stream(int numberIn = 1) {
      final number = numberIn > 0 || numberIn <= statuses.size() ? numberIn : 1

      return statuses.stream().limit(number as Long)
   }

   static AuditStatus single() {
      return stream(1).findFirst().orElseThrow { new Exception("Unable to find AuditStatusTypeDomain") }
   }
}
