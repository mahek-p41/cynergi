package com.cynergisuite.middleware.audit.detail.scan.area

import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditScanAreaService @Inject constructor(
   private val auditScanAreaTypeDomainRepository: AuditScanAreaRepository
) {

   fun exists(value: String): Boolean =
      auditScanAreaTypeDomainRepository.exists(value)

   fun fetchAll(): List<AuditScanArea> =
      auditScanAreaTypeDomainRepository.findAll()
}
