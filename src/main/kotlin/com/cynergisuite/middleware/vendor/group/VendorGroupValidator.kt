package com.cynergisuite.middleware.vendor.group

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.Duplicate
import com.cynergisuite.middleware.localization.NotNull
import com.cynergisuite.middleware.vendor.group.infrastructure.VendorGroupRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class VendorGroupValidator @Inject constructor(
   private val vendorGroupRepository: VendorGroupRepository,
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(VendorGroupValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(dto: VendorGroupDTO, company: CompanyEntity): VendorGroupEntity {
      logger.trace("Validating Save VendorGroup {}", dto)

      doValidation { errors -> doSharedValidation(errors, dto, company) }

      return VendorGroupEntity(dto = dto, company = company)
   }

   @Throws(ValidationException::class, NotFoundException::class)
   fun validateUpdate(id: UUID, vo: VendorGroupDTO, company: CompanyEntity): VendorGroupEntity {
      logger.trace("Validating Update VendorGroup {}", vo)

      val existing = vendorGroupRepository.findOne(id, company) ?: throw NotFoundException(id)

      doValidation { errors -> doSharedValidation(errors, vo, company, existing) }

      return existing.copy(value = vo.value!!, description = vo.description!!)
   }

   private fun doSharedValidation(errors: MutableSet<ValidationError>, dto: VendorGroupDTO, company: CompanyEntity, existing: VendorGroupEntity? = null) {
      if (dto.value == null) {
         errors.add(ValidationError("value", NotNull("value")))
      }

      if (dto.description == null) {
         errors.add(ValidationError("description", NotNull("description")))
      }

      val vgByValue = vendorGroupRepository.findOne(dto.value!!, company)

      if ((existing == null && vgByValue != null) ||
         (existing != null && existing.id != vgByValue?.id && dto.value == vgByValue?.value)
      ) {
         errors.add(ValidationError("value", Duplicate("value")))
      }
   }
}
