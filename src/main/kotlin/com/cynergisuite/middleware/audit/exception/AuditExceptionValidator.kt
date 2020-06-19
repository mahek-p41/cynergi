package com.cynergisuite.middleware.audit.exception

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanArea
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaValueObject
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.audit.exception.infrastructure.AuditExceptionRepository
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNote
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.status.APPROVED
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.inventory.infrastructure.InventoryRepository
import com.cynergisuite.middleware.localization.AuditExceptionHasNotBeenApproved
import com.cynergisuite.middleware.localization.AuditExceptionMustHaveInventoryOrBarcode
import com.cynergisuite.middleware.localization.AuditHasBeenApprovedNoNewNotesAllowed
import com.cynergisuite.middleware.localization.AuditMustBeInProgressDiscrepancy
import com.cynergisuite.middleware.localization.AuditScanAreaNotFound
import com.cynergisuite.middleware.localization.AuditUpdateRequiresApprovedOrNote
import com.cynergisuite.middleware.localization.NotFound
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditExceptionValidator @Inject constructor (
   private val auditRepository: AuditRepository,
   private val auditExceptionRepository: AuditExceptionRepository,
   private val auditScanAreaRepository: AuditScanAreaRepository,
   private val employeeRepository: EmployeeRepository,
   private val inventoryRepository: InventoryRepository,
   private val scanAreaRepository: AuditScanAreaRepository
) : ValidatorBase() {

   @Throws(ValidationException::class, NotFoundException::class)
   fun validateCreate(auditId: Long, auditException: AuditExceptionCreateValueObject, enteredBy: User): AuditExceptionEntity {
      doValidation { errors ->
         doSharedValidation(auditId)

         val inventoryId = auditException.inventory?.id
         val barcode = auditException.barcode
         val audit: AuditEntity = auditRepository.findOne(auditId, enteredBy.myCompany())!!
         val auditStatus = audit.currentStatus()
         val scanArea = auditException.scanArea

         if (inventoryId != null && inventoryRepository.doesNotExist(inventoryId, enteredBy.myCompany())) {
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

            if (scanAreaValue != null && scanAreaRepository.doesNotExist(scanAreaValue)) {
               errors.add(
                  ValidationError("audit.scanArea", AuditScanAreaNotFound(scanAreaValue))
               )
            }
         }

         if (employeeRepository.doesNotExist(enteredBy)) {
            errors.add(
               ValidationError("scannedBy", NotFound(enteredBy))
            )
         }
      }

      val inventoryId = auditException.inventory?.id
      val barcode = auditException.barcode
      val scanArea = auditException.scanArea

      return createAuditException(auditId, enteredBy, auditException.exceptionCode!!, inventoryId, barcode, scanArea)
   }

   private fun createAuditException(auditId: Long, enteredBy: User, exceptionCode: String, inventoryId: Long?, barcode: String?, scanArea: AuditScanAreaValueObject?): AuditExceptionEntity {
      val employeeUser = employeeRepository.findOne(enteredBy)!!

      return if (inventoryId != null) {
         val inventory = inventoryRepository.findOne(inventoryId, enteredBy.myCompany())!!

         AuditExceptionEntity(auditId, inventory, createScanArea(scanArea), employeeUser, exceptionCode)
      } else {
         AuditExceptionEntity(auditId, barcode!!, createScanArea(scanArea), employeeUser, exceptionCode)
      }
   }

   private fun createScanArea(scanArea: AuditScanAreaValueObject?): AuditScanArea? =
      scanArea?.let { auditScanAreaRepository.findOne(it.value!!) }

   @Throws(ValidationException::class, NotFoundException::class)
   fun validateUpdate(auditId: Long, auditExceptionUpdate: AuditExceptionUpdateValueObject, enteredBy: User): AuditExceptionEntity {
      doValidation { errors ->
         doSharedValidation(auditId)

         val auditExceptionId = auditExceptionUpdate.id!!
         val approved = auditExceptionUpdate.approved
         val note = auditExceptionUpdate.note
         val audit: AuditEntity = auditRepository.findOne(auditId, enteredBy.myCompany())!!

         if (auditExceptionRepository.doesNotExist(auditExceptionId)) {
            errors.add(
               ValidationError("id", NotFound(auditExceptionId))
            )
         }

         if (approved == null && note == null) {
            errors.add(
               ValidationError(null, AuditUpdateRequiresApprovedOrNote())
            )
         }

         if (audit.currentStatus() == APPROVED) {
            errors.add(
               ValidationError(null, AuditHasBeenApprovedNoNewNotesAllowed(auditId))
            )
         }

         if (employeeRepository.doesNotExist(enteredBy)) {
            errors.add(
               ValidationError("scannedBy", NotFound(enteredBy))
            )
         }

         if (auditExceptionRepository.isApproved(auditExceptionId)) {
            errors.add(
               ValidationError(null, AuditExceptionHasNotBeenApproved(auditExceptionId))
            )
         }
      }

      val employeeUser = employeeRepository.findOne(enteredBy)!!
      val auditException = auditExceptionRepository.findOne(auditExceptionUpdate.id!!, enteredBy.myCompany())!!
      val auditExceptionNote = auditExceptionUpdate.note
      val notes = auditException.notes

      if (auditExceptionNote != null) {
         notes.add(AuditExceptionNote(auditExceptionNote, employeeUser, auditException))
      }

      return auditException.copy(
         approved = auditExceptionUpdate.approved ?: false,
         notes = notes
      )
   }

   private fun doSharedValidation(auditId: Long) {
      if (auditRepository.doesNotExist(auditId)) {
         throw NotFoundException(auditId)
      }
   }
}
