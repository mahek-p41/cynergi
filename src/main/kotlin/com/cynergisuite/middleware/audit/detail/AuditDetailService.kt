package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.middleware.audit.detail.infrastructure.AuditDetailRepository
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import io.micronaut.validation.Validated
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class AuditDetailService @Inject constructor(
   private val auditRepository: AuditRepository,
   private val auditDetailRepository: AuditDetailRepository
) {
   fun fetchById(id: Long, company: Company, locale: Locale): AuditDetailEntity? =
      auditDetailRepository.findOne(id, company)

   @Validated
   fun fetchAll(auditId: Long, company: Company, @Valid pageRequest: PageRequest, locale: Locale): RepositoryPage<AuditDetailEntity, PageRequest> {
      val audit = auditRepository.findOne(auditId, company) ?: throw NotFoundException(auditId)

      return auditDetailRepository.findAll(audit, company, pageRequest)
   }

   @Validated
   fun create(auditId: Long, @Valid auditDetail: AuditDetailEntity, scannedBy: User, locale: Locale) =
      auditDetailRepository.insert(auditDetail)

   @Validated
   fun update(auditId: Long, @Valid auditDetail: AuditDetailEntity, scannedBy: User, locale: Locale): AuditDetailEntity =
      auditDetailRepository.update(auditDetail)
}
