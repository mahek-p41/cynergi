package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SearchPageRequest
import com.cynergisuite.domain.ValidatorBase.Companion.logger
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class VendorService @Inject constructor(
   private val vendorRepository: VendorRepository,
   private val vendorValidator: VendorValidator
) {

   fun fetchById(id: Long, company: Company): VendorValueObject? =
      vendorRepository.findOne(id, company)?.let{ VendorValueObject(entity = it) }

   @Validated
   fun create(@Valid vo: VendorValueObject, company: Company): VendorValueObject {
      logger.debug("Vendor Create Before Validation VendorVO {}", vo)
      val toCreate = vendorValidator.validateCreate(vo, company)
      logger.debug("Vendor Create After Validation VendorEntity {}", toCreate)
      return VendorValueObject(
         entity = vendorRepository.insert(entity = toCreate)
      )
   }

   fun fetchAll(company: Company, pageRequest: VendorPageRequest): Page<VendorValueObject> {
      val found = vendorRepository.findAll(company, pageRequest)

      return found.toPage { vendor: VendorEntity ->
         VendorValueObject(vendor)
      }
   }

   fun search(company: Company, pageRequest: SearchPageRequest): Page<VendorValueObject> {
      val found = vendorRepository.search(company, pageRequest)

      return found.toPage { vendor: VendorEntity ->
         VendorValueObject(vendor)
      }
   }

   @Validated
   fun update(id: Long, @Valid vo: VendorValueObject, company: Company): VendorValueObject {
      val toUpdate = vendorValidator.validateUpdate(id, vo, company)

      return VendorValueObject(
         entity = vendorRepository.update(entity = toUpdate)
      )
   }

}
