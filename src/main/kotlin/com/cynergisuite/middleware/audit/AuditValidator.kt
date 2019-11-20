package com.cynergisuite.middleware.audit

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.audit.action.AuditAction
import com.cynergisuite.middleware.audit.infrastructure.AuditPageRequest
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.status.AuditStatusService
import com.cynergisuite.middleware.audit.status.CREATED
import com.cynergisuite.middleware.employee.Employee
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.AuditOpenAtStore
import com.cynergisuite.middleware.localization.AuditStatusNotFound
import com.cynergisuite.middleware.localization.AuditUnableToChangeStatusFromTo
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotNull
import com.cynergisuite.middleware.localization.ThruDateIsBeforeFrom
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
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
   private val storeRepository: StoreRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AuditValidator::class.java)

   @Throws(ValidationException::class)
   fun validationFetchAll(pageRequest: AuditPageRequest): AuditPageRequest {
      doValidation { errors ->
         val from = pageRequest.from
         val thru = pageRequest.thru

         if (thru != null && from != null && thru.isBefore(from)) {
            errors.add(ValidationError("from", ThruDateIsBeforeFrom(from, thru)))
         }
      }

      return pageRequest
   }

   @Throws(ValidationException::class)
   fun validateFindAuditStatusCounts(pageRequest: AuditPageRequest) =
      validationFetchAll(pageRequest)

   @Throws(ValidationException::class)
   fun validateCreate(audit: AuditCreateValueObject, employee: EmployeeValueObject): Audit {
      logger.debug("Validating Create Audit {}", audit)

      doValidation { errors ->
         val storeNumber = audit.store?.number

         if (storeNumber != null && !storeRepository.exists(number = storeNumber)) {
            errors.add(ValidationError("storeNumber", NotFound(storeNumber)))
         }

         if (storeNumber != null && auditRepository.countAuditsNotCompletedOrCanceled(storeNumber = storeNumber) > 0) {
            errors.add(ValidationError("storeNumber", AuditOpenAtStore(storeNumber)))
         }
      }

      return Audit(
         store = storeRepository.findOneByNumber(number = audit.store!!.number!!)!!,
         actions = mutableSetOf(
            AuditAction(
               status = CREATED,
               changedBy = Employee(employee)
            )
         )
      )
   }

   @Throws(ValidationException::class)
   fun validateUpdate(audit: AuditUpdateValueObject, employee: EmployeeValueObject, locale: Locale): Pair<AuditAction, Audit> {
      logger.debug("Validating Update Audit {}", audit)

      doValidation { errors ->
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

               if (!auditStatusService.requestedStatusIsValid(currentStatus, requestedStatus)) {
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
      }

      return Pair(
         AuditAction(
            status = auditStatusService.fetchByValue(audit.status!!.value)!!,
            changedBy = Employee(employee)
         ),
         auditRepository.findOne(audit.id!!)!!
      )
   }
}
