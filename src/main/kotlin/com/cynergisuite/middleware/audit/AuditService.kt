package com.cynergisuite.middleware.audit

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.middleware.audit.action.AuditAction
import com.cynergisuite.middleware.audit.infrastructure.AuditPageRequest
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.status.AuditStatusService
import com.cynergisuite.middleware.employee.Employee
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.validation.Validated
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class AuditService @Inject constructor(
   private val auditRepository: AuditRepository,
   private val auditStatusService: AuditStatusService,
   private val auditValidator: AuditValidator,
   private val localizationService: LocalizationService,
   private val storeRepository: StoreRepository
) {
   fun fetchById(id: Long, locale: Locale): AuditValueObject? =
      auditRepository.findOne(id)?.let { AuditValueObject(it, locale, localizationService) }

   @Validated
   fun fetchAll(@Valid pageRequest: AuditPageRequest, locale: Locale): Page<AuditValueObject> {
      auditValidator.validateFetchAll(pageRequest)

      val found: RepositoryPage<Audit> = auditRepository.findAll(pageRequest = pageRequest)

      return found.toPage(pageRequest) {
         AuditValueObject(it, locale, localizationService)
      }
   }

   fun exists(id: Long): Boolean =
      auditRepository.exists(id = id)

   @Validated
   fun create(@Valid vo: AuditValueObject, @Valid employee: EmployeeValueObject, locale: Locale): AuditValueObject {
      auditValidator.validateCreate(vo)

      val audit = auditRepository.insert(
         Audit(
            store = storeRepository.findByNumber(number = vo.store!!.number!!)!!,
            actions = mutableSetOf(
               AuditAction(
                  status = auditStatusService.fetchOpened(),
                  changedBy = Employee(employee)
               )
            )
         )
      )

      return AuditValueObject(audit, locale, localizationService)
   }

   @Validated
   fun update(@Valid audit: AuditUpdateValueObject, @Valid employee: EmployeeValueObject, locale: Locale): AuditValueObject {
      auditValidator.validateUpdate(audit, locale)

      val existingAudit = auditRepository.findOne(audit.id!!)!!

      existingAudit.actions.add(
         AuditAction(
            status = auditStatusService.fetchByValue(audit.status!!.value)!!,
            changedBy = Employee(employee)
         )
      )

      return AuditValueObject(auditRepository.update(existingAudit), locale, localizationService)
   }
}
