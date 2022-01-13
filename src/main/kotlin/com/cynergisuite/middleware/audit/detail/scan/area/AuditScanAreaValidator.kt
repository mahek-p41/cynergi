package com.cynergisuite.middleware.audit.detail.scan.area
import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.Duplicate
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AuditScanAreaValidator @Inject constructor(
   private val storeRepository: StoreRepository,
   private val auditScanAreaRepository: AuditScanAreaRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AuditScanAreaValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(dto: AuditScanAreaDTO, company: CompanyEntity): AuditScanAreaEntity {
      logger.trace("Validating Save AuditScanArea {}", dto)
      val storeEntity = storeRepository.findOne(dto.store!!.id!!, company)

      doValidation { errors ->
         storeEntity ?: errors.add(ValidationError("dto.store.id", NotFound(dto.store!!.id!!)))

         if (auditScanAreaRepository.exists(dto.name!!, storeEntity!!)) {
            errors.add(ValidationError("dto.name", Duplicate(dto.name)))
         }
      }

      return AuditScanAreaEntity(dto = dto, company = company, store = storeEntity!!)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: UUID, dto: AuditScanAreaDTO, company: CompanyEntity): AuditScanAreaEntity {
      logger.trace("Validating Update AuditScanArea {}", dto)
      val storeEntity = storeRepository.findOne(dto.store!!.id!!, company)

      doValidation { errors ->
         if (!auditScanAreaRepository.exists(id)) errors.add(ValidationError("dto.id", NotFound(dto.id!!)))

         storeEntity ?: errors.add(ValidationError("dto.store.id", NotFound(dto.store!!.id!!)))

         if (auditScanAreaRepository.exists(dto.name!!, storeEntity!!)) {
            errors.add(ValidationError("dto.name", Duplicate(dto.name)))
         }
      }

      return AuditScanAreaEntity(id, dto, company, storeEntity!!)
   }
}
