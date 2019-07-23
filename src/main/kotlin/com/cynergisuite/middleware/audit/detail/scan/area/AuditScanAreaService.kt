package com.cynergisuite.middleware.audit.detail.scan.area

import com.cynergisuite.domain.TypeDomainService
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditScanAreaService @Inject constructor(
   private val auditScanAreaTypeDomainRepository: AuditScanAreaRepository
) : TypeDomainService<AuditScanArea> {
   override fun exists(value: String): Boolean =
      auditScanAreaTypeDomainRepository.exists(value)

   override fun fetchByValue(value: String): AuditScanArea? =
      auditScanAreaTypeDomainRepository.findOne(value)

   override fun fetchAll(): List<AuditScanArea> =
      auditScanAreaTypeDomainRepository.findAll()
}
