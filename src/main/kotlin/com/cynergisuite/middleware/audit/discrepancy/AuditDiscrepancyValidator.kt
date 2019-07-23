package com.cynergisuite.middleware.audit.discrepancy

import com.cynergisuite.middleware.audit.Audit
import com.cynergisuite.middleware.audit.discrepancy.infrastructure.AuditDiscrepancyRepository
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.AuditMustBeInProgressDiscrepancy
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditDiscrepancyValidator @Inject constructor (
   private val auditRepository: AuditRepository,
   private val auditExceptionsRepository: AuditDiscrepancyRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditDiscrepancyValidator::class.java)

   @Throws(ValidationException::class)
   fun validateSave(vo: AuditDiscrepancyValueObject) {
      logger.debug("Validating Save AuditDiscrepancy {}", vo)

      val errors = doSharedValidation(vo)

      if (errors.isNotEmpty()) {
         logger.debug("Validating Save AuditDiscrepancy {} had errors {}", vo, errors)

         throw ValidationException(errors)
      }
   }

   @Throws(ValidationException::class)
   fun validateUpdate(vo: AuditDiscrepancyValueObject) {
      logger.debug("Validating Update AuditDiscrepancy {}", vo)

      val errors = doSharedValidation(vo)
      val id = vo.id

      if (id == null) {
         errors.add(element = ValidationError("id", NotNull("id")))
      } else {
         if ( !auditExceptionsRepository.exists(id = id) ) {
            errors.add(ValidationError("id", NotFound(id)))
         }
      }

      if (errors.isNotEmpty()) {
         logger.debug("Validating Update AuditDiscrepancy {} had errors {}", vo, errors)

         throw ValidationException(errors)
      }
   }

   private fun doSharedValidation(vo: AuditDiscrepancyValueObject): MutableSet<ValidationError> {
      val errors = mutableSetOf<ValidationError>()
      val auditId = vo.audit!!.valueObjectId()!!
      val audit: Audit? = auditRepository.findOne(auditId)
      val auditStatus = audit?.currentStatus()

      if (audit == null) {
         errors.add(
            ValidationError("audit.id", NotFound(auditId))
         )
      } else if (auditStatus != null && "IN-PROGRESS" != auditStatus.value) {
         errors.add(
            ValidationError("audit.status", AuditMustBeInProgressDiscrepancy(auditId))
         )
      }

      return errors
   }
}
