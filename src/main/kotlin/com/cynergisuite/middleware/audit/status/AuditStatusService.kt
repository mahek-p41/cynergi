package com.cynergisuite.middleware.audit.status

import com.cynergisuite.domain.TypeDomainService
import com.cynergisuite.middleware.audit.status.infrastructure.AuditStatusRepository
import javax.inject.Singleton

@Singleton
class AuditStatusService(
   private val auditStatusRepository: AuditStatusRepository
) : TypeDomainService<AuditStatus> {
   override fun exists(value: String): Boolean =
      auditStatusRepository.exists(value)

   fun fetchOne(id: Long) =
      auditStatusRepository.findOne(id)

   override fun fetchByValue(value: String): AuditStatus? =
      auditStatusRepository.findOne(value)

   fun fetchOpened(): AuditStatus =
      auditStatusRepository.findOne("OPENED")!!

   override fun fetchAll(): List<AuditStatus> =
      auditStatusRepository.findAll()

   fun requestedStatusIsValid(currentStatus: AuditStatus, requestedStatus: AuditStatus) : Boolean {
      return currentStatus.nextStates.contains(requestedStatus)
   }
}
