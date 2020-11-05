package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.status.IN_PROGRESS
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.inventory.infrastructure.InventoryRepository
import com.cynergisuite.middleware.localization.AuditMustBeInProgressDetails
import com.cynergisuite.middleware.localization.NotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditDetailValidator @Inject constructor (
   private val auditRepository: AuditRepository,
   private val auditScanAreaRepository: AuditScanAreaRepository,
   private val employeeRepository: EmployeeRepository,
   private val inventoryRepository: InventoryRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AuditDetailValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(auditId: Long, scannedBy: User, dto: AuditDetailCreateDTO): AuditDetailEntity {
      logger.debug("Validating Create AuditDetail {}", dto)

      doValidation { errors ->
         val inventoryId = dto.inventory!!.id!!

         validateAudit(auditId, scannedBy.myCompany(), errors)

         if (!auditScanAreaRepository.exists(dto.scanArea!!.id!!)) errors.add(ValidationError("audit.scanArea.id", NotFound(dto.scanArea!!.id!!)))

         if (inventoryRepository.doesNotExist(inventoryId, scannedBy.myCompany())) {
            errors.add(
               ValidationError("inventory.id", NotFound(inventoryId))
            )
         }

         if (employeeRepository.doesNotExist(scannedBy)) {
            errors.add(ValidationError("user", NotFound(scannedBy.myEmployeeNumber())))
         }
      }

      val scanArea = auditScanAreaRepository.findOne(dto.scanArea!!.id!!, scannedBy.myCompany())!!
      val inventory = inventoryRepository.findOne(dto.inventory!!.id!!, scannedBy.myCompany())!!

      return AuditDetailEntity(
         inventory,
         scanArea = scanArea,
         scannedBy = employeeRepository.findOne(scannedBy)!!,
         audit = SimpleIdentifiableEntity(auditId)
      )
   }

   private fun validateAudit(auditId: Long, company: Company, errors: MutableSet<ValidationError>) {
      val audit: AuditEntity = auditRepository.findOne(auditId, company) ?: throw NotFoundException(auditId)
      val auditStatus = audit.currentStatus()

      if (auditStatus != IN_PROGRESS) {
         errors.add(
            ValidationError("audit.status", AuditMustBeInProgressDetails(auditId))
         )
      }
   }
}
