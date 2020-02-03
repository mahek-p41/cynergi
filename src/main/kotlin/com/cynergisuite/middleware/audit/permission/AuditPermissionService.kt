package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.audit.permission.infrastructure.AuditPermissionRepository
import com.cynergisuite.middleware.localization.LocalizationService
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditPermissionService @Inject constructor(
   private val auditPermissionRepository: AuditPermissionRepository,
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
}
