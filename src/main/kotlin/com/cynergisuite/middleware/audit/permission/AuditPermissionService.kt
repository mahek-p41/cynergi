package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.audit.permission.infrastructure.AuditPermissionRepository
import com.cynergisuite.middleware.authentication.User
import com.cynergisuite.middleware.localization.LocalizationService
import io.micronaut.validation.Validated
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid
import javax.validation.ValidationException

@Singleton
class AuditPermissionService @Inject constructor(
   private val auditPermissionRepository: AuditPermissionRepository,
   private val auditPermissionValidator: AuditPermissionValidator,
   private val localizationService: LocalizationService
) {

   fun fetchById(id: Long, dataset: String, locale: Locale): AuditPermissionValueObject? {
      return auditPermissionRepository.findById(id, dataset)?.let { AuditPermissionValueObject(it, locale, localizationService) }
   }

   fun fetchAllPermissionTypes(pageRequest: StandardPageRequest, locale: Locale): Page<AuditPermissionTypeValueObject> {
      return auditPermissionRepository.findAllPermissionTypes(pageRequest).toPage {
         AuditPermissionTypeValueObject(it, it.localizeMyDescription(locale, localizationService))
      }
   }

   @Validated
   @Throws(ValidationException::class)
   fun create(@Valid permission: AuditPermissionCreateUpdateDataTransferObject, user: User, locale: Locale): AuditPermissionValueObject {
      val auditPermission = auditPermissionValidator.validateCreate(permission, user)

      return auditPermissionRepository.insert(auditPermission).let { AuditPermissionValueObject(it, locale, localizationService) }
   }
}
