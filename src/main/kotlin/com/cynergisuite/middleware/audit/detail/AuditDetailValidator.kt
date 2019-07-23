package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.middleware.audit.Audit
import com.cynergisuite.middleware.audit.detail.infrastructure.AuditDetailRepository
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.AuditMustBeInProgressDetails
import com.cynergisuite.middleware.localization.AuditScanAreaNotFound
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditDetailValidator @Inject constructor (
   private val auditDetailRepository: AuditDetailRepository,
   private val auditRepository: AuditRepository,
   private val auditScanAreaRepository: AuditScanAreaRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditDetailValidator::class.java)

   @Throws(ValidationException::class)
   fun validateSave(vo: AuditDetailValueObject) {
      logger.debug("Validating Save AuditDetail {}", vo)

      val errors = doSharedValidation(vo)

      if (errors.isNotEmpty()) {
         logger.debug("Validating Save AuditDetail {} had errors {}", vo, errors)

         throw ValidationException(errors)
      }
   }

   @Throws(ValidationException::class)
   fun validateUpdate(vo: AuditDetailValueObject) {
      logger.debug("Validating Update AuditDetail {}", vo)

      val errors = doSharedValidation(vo)
      val id = vo.id

      if (id == null) {
         errors.add(element = ValidationError("id", NotNull("id")))
      } else if ( !auditDetailRepository.exists(id = id) ) {
         errors.add(ValidationError("id", NotFound(id)))
      }

      if (errors.isNotEmpty()) {
         logger.debug("Validating Update AuditDetail {} had errors {}", vo, errors)

         throw ValidationException(errors)
      }
   }

   private fun doSharedValidation(vo: AuditDetailValueObject): MutableSet<ValidationError> {
      val errors = mutableSetOf<ValidationError>()
      val auditId = vo.audit!!.valueObjectId()!!
      val audit: Audit? = auditRepository.findOne(auditId)
      val auditStatus = audit?.currentStatus()

      if ( audit == null ) {
         errors.add(
            ValidationError("audit.id", NotFound(auditId))
         )
      } else if (auditStatus != null && "IN-PROGRESS" != auditStatus.value) {
         errors.add(
            ValidationError("audit.status", AuditMustBeInProgressDetails(auditId))
         )
      }

      if ( !auditScanAreaRepository.exists(vo.scanArea!!.value!!) ) {
         errors.add(
            ValidationError("scanArea", AuditScanAreaNotFound(vo.scanArea!!.value))
         )
      }

      return errors
   }
}
