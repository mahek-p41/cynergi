package com.cynergisuite.middleware.audit

import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.status.AuditStatusService
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.AuditOpenAtStore
import com.cynergisuite.middleware.localization.AuditStatusNotFound
import com.cynergisuite.middleware.localization.AuditUnableToChangeStatusFromTo
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotNull
import com.cynergisuite.middleware.store.StoreService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditValidator @Inject constructor(
   private val auditRepository: AuditRepository,
   private val auditStatusService: AuditStatusService,
   private val localizationService: LocalizationService,
   private val storeService: StoreService
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(audit: AuditCreateValueObject) {
      logger.debug("Validating Create Audit {}", audit)

      val errors = mutableSetOf<ValidationError>()
      val storeNumber = audit.store?.number

      if (storeNumber != null && !storeService.exists(number = storeNumber)) {
         errors.add(ValidationError("storeNumber", NotFound(storeNumber)))
      }

      if (storeNumber != null && auditRepository.countAuditsNotCompleted(storeNumber = storeNumber) > 0) {
         errors.add(ValidationError("storeNumber", AuditOpenAtStore(storeNumber)))
      }

      if (errors.isNotEmpty()) {
         logger.debug("Validating Create Audit {} had errors {}", audit, errors)

         throw ValidationException(errors)
      }
   }

   @Throws(ValidationException::class)
   fun validateUpdate(audit: AuditUpdateValueObject, locale: Locale) {
      logger.debug("Validating Update Audit {}", audit)

      val errors = mutableSetOf<ValidationError>()
      val id = audit.id
      val requestedStatus = auditStatusService.fetchByValue(audit.status!!.value)

      if (id == null) {
         errors.add(element = ValidationError("id", NotNull("id")))
      } else {
         val existingAudit = auditRepository.findOne(id)

         if (existingAudit == null) {
            errors.add(ValidationError("id", NotFound(id)))
         } else if (requestedStatus != null) {
            val currentStatus = existingAudit.currentStatus()

            if ( !auditStatusService.requestedStatusIsValid(currentStatus, requestedStatus) ) {
               errors.add(
                  ValidationError(
                     "status",
                     AuditUnableToChangeStatusFromTo(
                        id,
                        currentStatus.localizeMyDescription(locale, localizationService),
                        requestedStatus.localizeMyDescription(locale, localizationService)
                     )
                  )
               )
            }
         } else {
            errors.add(
               ValidationError("status", AuditStatusNotFound(audit.status!!.value))
            )
         }
      }

      if (errors.isNotEmpty()) {
         logger.debug("Validating Update Audit {} had errors {}", audit, errors)

         throw ValidationException(errors)
      }
   }
}
