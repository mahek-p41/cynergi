package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.*
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorValidator @Inject constructor(
   private val vendorRepository: VendorRepository,
   private val companyRepository: CompanyRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(VendorValidator::class.java)


   @Throws(ValidationException::class)
   fun validateCreate(vo: VendorValueObject, company: Company): VendorEntity {
      logger.trace("Validating Save Vendor {}", vo)

      doValidation { errors -> doSharedValidation(errors, vo, company) }

      return VendorEntity(vo = vo, company = company)
   }

   private fun doSharedValidation(errors: MutableSet<ValidationError>, vo: VendorValueObject, company: Company) {
      if((vo.shutdownFrom != null) && vo.shutdownThru == null) {
         errors.add(ValidationError("shutdownThru", NotNull("shutdownThru")))
      }

      if((vo.shutdownThru != null) && vo.shutdownFrom == null) {
         errors.add(ValidationError("shutdownFrom", NotNull("shutdownFrom")))
      }

   }

}
