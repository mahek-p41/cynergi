package com.cynergisuite.middleware.audit.exception

import com.cynergisuite.middleware.audit.Audit
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.audit.exception.infrastructure.AuditExceptionRepository
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.inventory.infrastructure.InventoryRepository
import com.cynergisuite.middleware.localization.AuditExceptionMustHaveInventoryOrBarcode
import com.cynergisuite.middleware.localization.AuditMustBeInProgressDiscrepancy
import com.cynergisuite.middleware.localization.AuditScanAreaNotFound
import com.cynergisuite.middleware.localization.NotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditExceptionValidator @Inject constructor (
   private val auditRepository: AuditRepository,
   private val auditExceptionsRepository: AuditExceptionRepository,
   private val inventoryRepository: InventoryRepository,
   private val scanAreaRepository: AuditScanAreaRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditExceptionValidator::class.java)

   @Throws(ValidationException::class, NotFoundException::class)
   fun validateCreate(auditId: Long, auditException: AuditExceptionCreateValueObject) {
      val inventoryId = auditException.inventory?.id
      val barcode = auditException.barcode
      val errors = doSharedValidation(auditId)
      val audit: Audit = auditRepository.findOne(auditId)!!
      val auditStatus = audit.currentStatus()
      val scanArea = auditException.scanArea

      if (inventoryId != null && inventoryRepository.doesNotExist(inventoryId)) {
         errors.add(
            ValidationError("inventory.id", NotFound(inventoryId))
         )
      } else if (inventoryId == null && barcode == null) {
         errors.add(
            ValidationError("barcode", AuditExceptionMustHaveInventoryOrBarcode())
         )
      }

      if ("IN-PROGRESS" != auditStatus.value) {
         errors.add(
            ValidationError("audit.status", AuditMustBeInProgressDiscrepancy(auditId))
         )
      }

      if (scanArea != null) {
         val scanAreaValue = scanArea.value

         if (scanAreaValue != null && scanAreaRepository.doesNotExist(scanAreaValue) ) {
            errors.add(
               ValidationError("audit.scanArea", AuditScanAreaNotFound(scanAreaValue))
            )
         }
      }

      if (errors.isNotEmpty()) {
         logger.warn("Validating Create for AuditException {} had errors {} for audit {}", auditException, errors, auditId)

         throw ValidationException(errors)
      }
   }

   @Throws(ValidationException::class, NotFoundException::class)
   fun validateAddNote(auditId: Long, auditExceptionUpdate: AuditExceptionUpdateValueObject) {
      val auditExceptionId = auditExceptionUpdate.id!!
      val errors = doSharedValidation(auditId)

      if (auditExceptionsRepository.doesNotExist(auditExceptionId)) {
         errors.add(
            ValidationError("id", NotFound(auditExceptionId))
         )
      }

      if (errors.isNotEmpty()) {
         logger.warn("Validating AddNote for AuditException {} had errors {} for audit {}", auditExceptionUpdate, errors, auditId)

         throw ValidationException(errors)
      }
   }

   private fun doSharedValidation(auditId: Long): MutableSet<ValidationError> {
      val errors = mutableSetOf<ValidationError>()

      if (auditRepository.doesNotExist(auditId)) {
         throw NotFoundException(auditId)
      }

      return errors
   }
}
