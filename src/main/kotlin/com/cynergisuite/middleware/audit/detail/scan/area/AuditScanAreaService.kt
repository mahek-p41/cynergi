package com.cynergisuite.middleware.audit.detail.scan.area

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditScanAreaService @Inject constructor(
   private val auditScanAreaValidator: AuditScanAreaValidator,
   private val auditScanAreaRepository: AuditScanAreaRepository
) {

   fun fetchOne(company: Company, id: Long): AuditScanAreaDTO? =
      auditScanAreaRepository.findOne(id, company)?.let { AuditScanAreaDTO(it) }

   fun fetchAll(user: User): List<AuditScanAreaDTO> =
      auditScanAreaRepository.findAll(user).map(::AuditScanAreaDTO)

   fun fetchAll(company: Company, storeId: Long, pageRequest: StandardPageRequest): Page<AuditScanAreaDTO> =
      auditScanAreaRepository.findAll(company, storeId, pageRequest).toPage { AuditScanAreaDTO(it) }

   fun create(auditScanAreaDTO: AuditScanAreaDTO, company: Company): AuditScanAreaDTO {
      val toCreate = auditScanAreaValidator.validateCreate(auditScanAreaDTO, company)

      return AuditScanAreaDTO(auditScanAreaRepository.insert(toCreate))
   }

   fun update(id: Long, auditScanAreaDTO: AuditScanAreaDTO, company: Company): AuditScanAreaDTO {
      val toUpdate = auditScanAreaValidator.validateUpdate(id, auditScanAreaDTO, company)

      return AuditScanAreaDTO(auditScanAreaRepository.update(toUpdate))
   }
}
