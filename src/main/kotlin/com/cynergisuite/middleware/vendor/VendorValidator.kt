package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.*
import com.cynergisuite.middleware.vendor.freight.infrastructure.FreightMethodTypeRepository
import com.cynergisuite.middleware.vendor.freight.infrastructure.FreightOnboardTypeRepository
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class VendorValidator @Inject constructor(
   private val freightMethodTypeRepository: FreightMethodTypeRepository,
   private val freightOnboardTypeRepository: FreightOnboardTypeRepository,
   private val vendorRepository: VendorRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(VendorValidator::class.java)


   @Throws(ValidationException::class)
   fun validateCreate(@Valid vo: VendorValueObject, company: Company): VendorEntity {
      logger.trace("Validating Save Vendor {}", vo)

      val freightOnboardType = freightOnboardTypeRepository.findOne(value = vo.freightOnboardType.value!!)
      val freightMethodType = freightMethodTypeRepository.findOne(value = vo.freightMethodType.value!!)
      val payTo = vo.payTo?.id?.let { vendorRepository.findOne(it, company) }

      doValidation { errors ->
         doSharedValidation(errors, vo)
         freightOnboardType ?: errors.add(ValidationError("freightOnboardType.value", NotFound(vo.freightOnboardType.value!!)))
         freightMethodType ?: errors.add(ValidationError("freightMethodType.value", NotFound(vo.freightMethodType.value!!)))
      }

      return VendorEntity(vo = vo, company = company, freightOnboardType = freightOnboardType!!, freightMethodType = freightMethodType!!, payTo = payTo)
   }

   fun validateUpdate(id: Long, vo: VendorValueObject, company: Company): VendorEntity {
      logger.trace("Validating Update Vendor {}", vo)

      val freightOnboardType = freightOnboardTypeRepository.findOne(value = vo.freightOnboardType.value!!)
      val freightMethodType = freightMethodTypeRepository.findOne(value = vo.freightMethodType.value!!)

      doValidation { errors ->
         doSharedValidation(errors, vo)
         freightOnboardType ?: errors.add(ValidationError("freightOnboardType.value", NotFound(vo.freightOnboardType.value!!)))
         freightMethodType ?: errors.add(ValidationError("freightMethodType.value", NotFound(vo.freightMethodType.value!!)))
      }

      return VendorEntity(vo = vo, company = company, freightOnboardType = freightOnboardType!!, freightMethodType = freightMethodType!!)
   }

   private fun doSharedValidation(errors: MutableSet<ValidationError>, vo: VendorValueObject) {
      if((vo.shutdownFrom != null) && vo.shutdownThru == null) {
         errors.add(ValidationError("shutdownThru", NotNull("shutdownThru")))
      }

      if((vo.shutdownThru != null) && vo.shutdownFrom == null) {
         errors.add(ValidationError("shutdownFrom", NotNull("shutdownFrom")))
      }
   }

}
