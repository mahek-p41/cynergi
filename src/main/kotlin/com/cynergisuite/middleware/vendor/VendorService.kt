package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SearchPageRequest
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorService @Inject constructor(
   private val vendorRepository: VendorRepository,
   private val vendorValidator: VendorValidator
) {

   fun fetchById(id: Long, company: Company): VendorDTO? =
      vendorRepository.findOne(id, company)?.let { VendorDTO(entity = it) }

   fun create(dto: VendorDTO, company: Company): VendorDTO {
      val toCreate = vendorValidator.validateCreate(dto, company)

      return VendorDTO(
         entity = vendorRepository.insert(entity = toCreate)
      )
   }

   fun fetchAll(company: Company, pageRequest: PageRequest): Page<VendorDTO> {
      val found = vendorRepository.findAll(company, pageRequest)

      return found.toPage { vendor: VendorEntity ->
         VendorDTO(vendor)
      }
   }

   fun search(company: Company, pageRequest: SearchPageRequest): Page<VendorDTO> {
      val found = vendorRepository.search(company, pageRequest)

      return found.toPage { vendor: VendorEntity ->
         VendorDTO(vendor)
      }
   }

   fun update(id: Long, dto: VendorDTO, company: Company): VendorDTO {
      val (existing, toUpdate) = vendorValidator.validateUpdate(id, dto, company)

      return VendorDTO(
         entity = vendorRepository.update(existing, toUpdate)
      )
   }
}
