package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SearchPageRequest
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class VendorService @Inject constructor(
   private val vendorRepository: VendorRepository,
   private val vendorValidator: VendorValidator
) {

   fun fetchById(id: Long, company: Company): VendorDTO? =
      vendorRepository.findOne(id, company)?.let { VendorDTO(entity = it) }

   @Validated
   fun create(@Valid dto: VendorDTO, company: Company): VendorDTO {
      val toCreate = vendorValidator.validateCreate(dto, company)

      return VendorDTO(
         entity = vendorRepository.insert(entity = toCreate)
      )
   }

   @Validated
   fun fetchAll(company: Company, @Valid pageRequest: PageRequest): Page<VendorDTO> {
      val found = vendorRepository.findAll(company, pageRequest)

      return found.toPage { vendor: VendorEntity ->
         VendorDTO(vendor)
      }
   }

   @Validated
   fun search(company: Company, @Valid pageRequest: SearchPageRequest): Page<VendorDTO> {
      val found = vendorRepository.search(company, pageRequest)

      return found.toPage { vendor: VendorEntity ->
         VendorDTO(vendor)
      }
   }

   @Validated
   fun update(id: Long, @Valid dto: VendorDTO, company: Company): VendorDTO {
      val toUpdate = vendorValidator.validateUpdate(id, dto, company)

      return VendorDTO(
         entity = vendorRepository.update(entity = toUpdate)
      )
   }
}
