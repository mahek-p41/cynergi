package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.detail.infrastructure.AuditDetailRepository
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.status.IN_PROGRESS
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.inventory.infrastructure.InventoryRepository
import com.cynergisuite.middleware.localization.AuditMustBeInProgressDetails
import com.cynergisuite.middleware.localization.Duplicate
import com.cynergisuite.middleware.localization.NotFound
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

@Singleton
class AuditDetailValidator @Inject constructor (
   private val auditRepository: AuditRepository,
   private val auditDetailRepository: AuditDetailRepository,
   private val auditScanAreaRepository: AuditScanAreaRepository,
   private val employeeRepository: EmployeeRepository,
   private val inventoryRepository: InventoryRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AuditDetailValidator::class.java)

   fun validateDuplicateDetail(auditId: UUID, dto: AuditDetailCreateUpdateDTO, scannedBy: User): AuditDetailEntity? {
      val inventory = inventoryRepository.findOne(dto.inventory!!.id!!, scannedBy.myCompany()) ?: throw NotFoundException(dto.inventory.id!!)

      return auditDetailRepository.findOne(auditId, inventory, scannedBy.myCompany())
   }

   @Throws(ValidationException::class)
   fun validateCreate(auditId: UUID, scannedBy: User, dto: AuditDetailCreateUpdateDTO): AuditDetailEntity {
      logger.debug("Validating Create AuditDetail {}", dto)

      return doValidation(false, dto, auditId, scannedBy)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(auditId: UUID, scannedBy: User, dto: AuditDetailCreateUpdateDTO): AuditDetailEntity {
      logger.debug("Validating Update AuditDetail {}", dto)

      auditDetailRepository.findOne(dto.id!!, scannedBy.myCompany()) ?: throw NotFoundException(dto.id!!)

      return doValidation(true, dto, auditId, scannedBy)
   }

   private fun doValidation(isUpdate: Boolean, dto: AuditDetailCreateUpdateDTO, auditId: UUID, scannedBy: User): AuditDetailEntity {
      val inventory = inventoryRepository.findOne(dto.inventory!!.id!!, scannedBy.myCompany()) ?: throw NotFoundException(dto.inventory.id!!)
      val scanArea = auditScanAreaRepository.findOne(dto.scanArea!!.id!!, scannedBy.myCompany()) ?: throw NotFoundException(dto.scanArea!!.id!!)

      doValidation { errors ->

         if (isUpdate && auditDetailRepository.exists(auditId, inventory) && inventory.id == dto.inventory.id) {
            errors.add(ValidationError("inventory.lookupKey", Duplicate(inventory.lookupKey)))
         }

         val inventoryId = dto.inventory.id!!

         validateAudit(auditId, scannedBy.myCompany(), errors)

         if (!auditScanAreaRepository.exists(dto.scanArea!!.id!!)) errors.add(
            ValidationError(
               "audit.scanArea.id",
               NotFound(dto.scanArea!!.id!!)
            )
         )

         if (inventoryRepository.doesNotExist(inventoryId, scannedBy.myCompany())) {
            errors.add(
               ValidationError("inventory.id", NotFound(inventoryId))
            )
         }

         if (employeeRepository.doesNotExist(scannedBy)) {
            errors.add(ValidationError("user", NotFound(scannedBy.myEmployeeNumber())))
         }
      }

      return AuditDetailEntity(
         id = dto.id,
         inventory = inventory,
         scanArea = scanArea,
         scannedBy = employeeRepository.findOne(scannedBy)!!,
         audit = SimpleIdentifiableEntity(auditId)
      )
   }

   private fun validateAudit(auditId: UUID, company: CompanyEntity, errors: MutableSet<ValidationError>) {
      val audit: AuditEntity = auditRepository.findOne(auditId, company) ?: throw NotFoundException(auditId)
      val auditStatus = audit.currentStatus()

      if (auditStatus != IN_PROGRESS) {
         errors.add(
            ValidationError("audit.status", AuditMustBeInProgressDetails(auditId))
         )
      }
   }
}
