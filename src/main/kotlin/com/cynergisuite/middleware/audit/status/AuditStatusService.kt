package com.cynergisuite.middleware.audit.status

import com.cynergisuite.middleware.audit.status.infrastructure.AuditStatusRepository
import javax.inject.Singleton

@Singleton
class AuditStatusService(
   private val auditStatusRepository: AuditStatusRepository
) {
   fun exists(value: String): Boolean =
      auditStatusRepository.exists(value)

   fun fetchOne(id: Long) =
      auditStatusRepository.findOne(id)

   fun fetchByValue(value: String): AuditStatus? =
      auditStatusRepository.findOne(value)

   fun fetchAll(): List<AuditStatus> =
      auditStatusRepository.findAll()

   fun requestedStatusIsValid(currentStatus: AuditStatus, requestedStatus: AuditStatus): Boolean {
      return currentStatus.nextStates.contains(requestedStatus)
   }
}
