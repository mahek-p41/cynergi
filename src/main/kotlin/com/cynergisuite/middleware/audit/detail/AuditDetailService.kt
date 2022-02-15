package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.middleware.audit.detail.infrastructure.AuditDetailRepository
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID
import javax.validation.Valid

@Singleton
class AuditDetailService @Inject constructor(
   private val auditRepository: AuditRepository,
   private val auditDetailRepository: AuditDetailRepository
) {
   fun fetchById(id: UUID, company: CompanyEntity): AuditDetailEntity? =
      auditDetailRepository.findOne(id, company)

   @Validated
   fun fetchAll(auditId: UUID, company: CompanyEntity, @Valid pageRequest: PageRequest): RepositoryPage<AuditDetailEntity, PageRequest> {
      val audit = auditRepository.findOne(auditId, company) ?: throw NotFoundException(auditId)

      return auditDetailRepository.findAll(audit, company, pageRequest)
   }

   @Validated
   fun create(@Valid auditDetail: AuditDetailEntity, scannedBy: User) =
      auditDetailRepository.insert(auditDetail)

   @Validated
   fun update(@Valid auditDetail: AuditDetailEntity, scannedBy: User): AuditDetailEntity =
      auditDetailRepository.update(auditDetail)
}
