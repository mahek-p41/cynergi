package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.detail.infrastructure.AuditDetailRepository
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.status.IN_PROGRESS
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
   fun validateCreate(auditId: Long, company: Company, auditDetailCreate: AuditDetailCreateValueObject) {
      logger.debug("Validating Create AuditDetail {}", auditDetailCreate)

      doValidation { errors ->
         val inventoryId = auditDetailCreate.inventory!!.id!!

         validateAudit(auditId, dataset, errors)
         validateScanArea(auditDetailCreate.scanArea!!.value!!, errors)

         if (inventoryRepository.doesNotExist(inventoryId, dataset)) {
            errors.add(
               ValidationError("inventory.id", NotFound(inventoryId))
            )
         }
      }
   }

   @Throws(ValidationException::class)
   fun validateUpdate(vo: AuditDetailValueObject, company: Company) {
      logger.debug("Validating Update AuditDetail {}", vo)

      doValidation { errors ->
         val auditId = vo.audit!!.myId()!!
         val id = vo.id

         validateAudit(auditId, dataset, errors)
         validateScanArea(vo.scanArea!!.value!!, errors)

         if (id == null) {
            errors.add(element = ValidationError("id", NotNull("id")))
         } else if ( auditDetailRepository.doesNotExist(id)) {
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

   private fun validateAudit(auditId: Long, company: Company, errors: MutableSet<ValidationError>) {
      val audit: AuditEntity = auditRepository.findOne(auditId, dataset) ?: throw NotFoundException(auditId)
      val auditStatus = audit.currentStatus()

      if (IN_PROGRESS != auditStatus) {
         errors.add(
            ValidationError("audit.status", AuditMustBeInProgressDetails(auditId))
         )
      }
   }
}
