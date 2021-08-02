package com.cynergisuite.middleware.audit.exception

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.middleware.audit.exception.infrastructure.AuditExceptionRepository
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditExceptionService @Inject constructor(
   private val auditRepository: AuditRepository,
   private val auditExceptionRepository: AuditExceptionRepository
) {
   fun fetchById(id: UUID, company: Company): AuditExceptionEntity? =
      auditExceptionRepository.findOne(id = id, company = company)

   fun fetchAll(auditId: UUID, company: Company, pageRequest: PageRequest): RepositoryPage<AuditExceptionEntity, PageRequest> {
      val audit = auditRepository.findOne(auditId, company) ?: throw NotFoundException(auditId)

      return auditExceptionRepository.findAll(audit, company, pageRequest)
   }

   fun exists(id: UUID): Boolean =
      auditExceptionRepository.exists(id = id)

   fun create(auditException: AuditExceptionEntity): AuditExceptionEntity =
      auditExceptionRepository.insert(auditException)

   fun update(auditException: AuditExceptionEntity): AuditExceptionEntity =
      auditExceptionRepository.update(auditException)
}
