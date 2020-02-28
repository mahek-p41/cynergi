package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.detail.infrastructure.AuditDetailRepository
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
   private val employeeRepository: EmployeeRepository,
   private val inventoryRepository: InventoryRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AuditDetailValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(auditId: Long, scannedBy: User, dto: AuditDetailCreateDataTransferObject): AuditDetailEntity {
      logger.debug("Validating Create AuditDetail {}", dto)

      doValidation { errors ->
         val inventoryId = dto.inventory!!.id!!

         validateAudit(auditId, scannedBy.myCompany(), errors)
         validateScanArea(dto.scanArea!!.value!!, errors)

         if (inventoryRepository.doesNotExist(inventoryId, scannedBy.myCompany())) {
            errors.add(
               ValidationError("inventory.id", NotFound(inventoryId))
            )
         }

         if (employeeRepository.doesNotExist(scannedBy)) {
            errors.add(ValidationError("user", NotFound(scannedBy.myEmployeeNumber())))
         }
      }

      val scanArea = auditScanAreaRepository.findOne(dto.scanArea!!.value!!)!!
      val inventory = inventoryRepository.findOne(dto.inventory!!.id!!, scannedBy.myCompany())!!

      return AuditDetailEntity(
         inventory,
         scanArea = scanArea,
         scannedBy = employeeRepository.findOne(scannedBy)!!,
         audit = SimpleIdentifiableEntity(auditId)
      )
   }

   @Throws(ValidationException::class)
   fun validateUpdate(vo: AuditDetailValueObject, company: Company): AuditDetailEntity {
      logger.debug("Validating Update AuditDetail {}", vo)

      doValidation { errors ->
         val auditId = vo.audit!!.myId()!!
         val id = vo.id

         validateAudit(auditId, company, errors)
         validateScanArea(vo.scanArea!!.value!!, errors)

         if (id == null) {
            errors.add(element = ValidationError("id", NotNull("id")))
         } else if ( auditDetailRepository.doesNotExist(id)) {
            errors.add(ValidationError("id", NotFound(id)))
         }
      }

      val auditDetail = auditDetailRepository.findOne(vo.id!!, company) ?: throw NotFoundException(vo.id!!)

      return auditDetailRepository.update(
         auditDetail.copy(
            scanArea = auditScanAreaRepository.findOne(vo.scanArea!!.value!!)!!
         )
      )
   }

   private fun validateScanArea(scanAreaValue: String, errors: MutableSet<ValidationError>) {
      if ( !auditScanAreaRepository.exists(scanAreaValue) ) {
         errors.add(
            ValidationError("scanArea", AuditScanAreaNotFound(scanAreaValue))
         )
      }
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
