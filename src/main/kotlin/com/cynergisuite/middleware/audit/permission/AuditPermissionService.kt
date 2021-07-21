package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.audit.permission.infrastructure.AuditPermissionRepository
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.localization.LocalizationService
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ValidationException

@Singleton
class AuditPermissionService @Inject constructor(
   private val auditPermissionRepository: AuditPermissionRepository,
   private val auditPermissionValidator: AuditPermissionValidator,
   private val localizationService: LocalizationService
) {

   fun fetchById(id: UUID, company: Company, locale: Locale): AuditPermissionValueObject? {
      return auditPermissionRepository.findById(id, company)?.let { AuditPermissionValueObject(it, locale, localizationService) }
   }

   fun fetchAll(pageRequest: StandardPageRequest, user: User, locale: Locale): Page<AuditPermissionValueObject> {
      val found = auditPermissionRepository.findAll(pageRequest, user.myCompany())

      return found.toPage { AuditPermissionValueObject(it, locale, localizationService) }
   }

   fun fetchAllByType(typeId: Long, pageRequest: StandardPageRequest, user: User, locale: Locale): Page<AuditPermissionValueObject> {
      val found = auditPermissionRepository.findAllByType(pageRequest, user.myCompany(), typeId)

      return found.toPage { AuditPermissionValueObject(it, locale, localizationService) }
   }

   fun fetchAllPermissionTypes(pageRequest: StandardPageRequest, locale: Locale): Page<AuditPermissionTypeValueObject> {
      return auditPermissionRepository.findAllPermissionTypes(pageRequest).toPage {
         AuditPermissionTypeValueObject(
            it,
            it.localizeMyDescription(locale, localizationService)
         )
      }
   }

   @Throws(ValidationException::class)
   fun create(dto: AuditPermissionCreateDTO, user: User, locale: Locale): AuditPermissionValueObject {
      val auditPermission = auditPermissionValidator.validateCreate(dto, user)

      return AuditPermissionValueObject(
         entity = auditPermissionRepository.insert(auditPermission),
         locale = locale,
         localizationService = localizationService
      )
   }

   fun deleteById(id: UUID, company: Company, locale: Locale): AuditPermissionValueObject? {
      return auditPermissionRepository.deleteById(id, company)?.let { AuditPermissionValueObject(it, locale, localizationService) }
   }
}
