package com.cynergisuite.middleware.vendor.group

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotNull
import com.cynergisuite.middleware.vendor.group.infrastructure.VendorGroupRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorGroupValidator @Inject constructor(
   private val vendorGroupRepository: VendorGroupRepository,
   private val companyRepository: CompanyRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(VendorGroupValidator::class.java)


   @Throws(ValidationException::class)
   fun validateCreate(vo: VendorGroupValueObject, company: Company): VendorGroupEntity {
      logger.trace("Validating Save VendorGroup {}", vo)

      doValidation { errors -> doSharedValidation(errors, vo) }

      return VendorGroupEntity(vo = vo, company = company)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: Long, vo: VendorGroupValueObject, company: Company): VendorGroupEntity {
      logger.trace("Validating Update VendorGroup {}", vo)

      val existing = vendorGroupRepository.findOne(id, company) ?: throw NotFoundException(id)

      doValidation { errors -> doSharedValidation(errors, vo) }

      return VendorGroupEntity(source = existing, updateWith = vo)
   }

   private fun doSharedValidation(errors: MutableSet<ValidationError>, vo: VendorGroupValueObject) {
      if(vo.value == null) {
         errors.add(ValidationError("value", NotNull("value")))
      }

      if(vo.description == null) {
         errors.add(ValidationError("description", NotNull("description")))
      }

   }

}
