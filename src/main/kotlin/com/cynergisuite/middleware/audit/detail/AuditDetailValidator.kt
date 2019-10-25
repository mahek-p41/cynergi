package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.audit.Audit
import com.cynergisuite.middleware.audit.detail.infrastructure.AuditDetailRepository
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.inventory.infrastructure.InventoryRepository
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
   private val auditScanAreaRepository: AuditScanAreaRepository,
   private val inventoryRepository: InventoryRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AuditDetailValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(auditId: Long, auditDetailCreate: AuditDetailCreateValueObject) {
      logger.debug("Validating Create AuditDetail {}", auditDetailCreate)

      doValidation { errors ->
         val inventoryId = auditDetailCreate.inventory!!.id!!

         validateAudit(auditId, errors)
         validateScanArea(auditDetailCreate.scanArea!!.value!!, errors)

         if (inventoryRepository.doesNotExist(inventoryId)) {
            errors.add(
               ValidationError("inventory.id", NotFound(inventoryId))
            )
         }
      }
   }

   @Throws(ValidationException::class)
   fun validateUpdate(vo: AuditDetailValueObject) {
      logger.debug("Validating Update AuditDetail {}", vo)

      doValidation { errors ->
         val auditId = vo.audit!!.myId()!!
         val id = vo.id

         validateAudit(auditId, errors)
         validateScanArea(vo.scanArea!!.value!!, errors)

         if (id == null) {
            errors.add(element = ValidationError("id", NotNull("id")))
         } else if ( auditDetailRepository.doesNotExist(id) /*!auditDetailRepository.exists(id = id)*/ ) {
            errors.add(ValidationError("id", NotFound(id)))
         }
      }
   }

   private fun validateScanArea(scanAreaValue: String, errors: MutableSet<ValidationError>) {
      if ( !auditScanAreaRepository.exists(scanAreaValue) ) {
         errors.add(
            ValidationError("scanArea", AuditScanAreaNotFound(scanAreaValue))
         )
      }
   }

   private fun validateAudit(auditId: Long, errors: MutableSet<ValidationError>) {
      val audit: Audit = auditRepository.findOne(auditId) ?: throw NotFoundException(auditId)
      val auditStatus = audit.currentStatus()

      if ("IN-PROGRESS" != auditStatus.value) {
         errors.add(
            ValidationError("audit.status", AuditMustBeInProgressDetails(auditId))
         )
      }
   }
}
