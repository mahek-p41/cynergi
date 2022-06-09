package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SearchPageRequest
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.vendor.infrastructure.VendorPageRequest
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class VendorService @Inject constructor(
   private val vendorRepository: VendorRepository,
   private val vendorValidator: VendorValidator
) {

   fun fetchById(id: UUID, company: CompanyEntity): VendorDTO? =
      vendorRepository.findOne(id, company)?.let { VendorDTO(entity = it) }

   fun create(dto: VendorDTO, company: CompanyEntity): VendorDTO {
      val toCreate = vendorValidator.validateCreate(dto, company)

      return VendorDTO(
         entity = vendorRepository.insert(entity = toCreate)
      )
   }

   fun fetchAll(company: CompanyEntity, pageRequest: VendorPageRequest): Page<VendorDTO> {
      val found = vendorRepository.findAll(company, pageRequest)

      return found.toPage { vendor: VendorEntity ->
         VendorDTO(vendor)
      }
   }

   fun search(company: CompanyEntity, pageRequest: SearchPageRequest): Page<VendorDTO> {
      val found = vendorRepository.search(company, pageRequest)

      return found.toPage { vendor: VendorEntity ->
         VendorDTO(vendor)
      }
   }

   fun update(id: UUID, dto: VendorDTO, company: CompanyEntity): VendorDTO {
      val (existing, toUpdate) = vendorValidator.validateUpdate(id, dto, company)

      return VendorDTO(
         entity = vendorRepository.update(existing, toUpdate)
      )
   }

   fun delete(id: UUID, company: CompanyEntity) {
      vendorRepository.delete(id, company)
   }
}
