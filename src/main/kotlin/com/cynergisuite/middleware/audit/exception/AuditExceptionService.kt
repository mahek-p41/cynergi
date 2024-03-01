package com.cynergisuite.middleware.audit.exception

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.middleware.audit.exception.infrastructure.AuditExceptionRepository
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class AuditExceptionService @Inject constructor(
   private val auditRepository: AuditRepository,
   private val auditExceptionRepository: AuditExceptionRepository
) {
   fun fetchById(id: UUID, company: CompanyEntity): AuditExceptionEntity? =
      auditExceptionRepository.findOne(id = id, company = company)

   fun fetchAll(auditId: UUID, company: CompanyEntity, includeUnscanned: Boolean = false , pageRequest: PageRequest): RepositoryPage<AuditExceptionEntity, PageRequest> {
      val audit = auditRepository.findOne(auditId, company) ?: throw NotFoundException(auditId)

      return auditExceptionRepository.findAll(audit, company, includeUnscanned, pageRequest)
   }

   fun exists(id: UUID): Boolean =
      auditExceptionRepository.exists(id = id)

   fun create(auditException: AuditExceptionEntity): AuditExceptionEntity =
      auditExceptionRepository.insert(auditException)

   fun update(auditException: AuditExceptionEntity): AuditExceptionEntity =
      auditExceptionRepository.update(auditException)
}
