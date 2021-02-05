package com.cynergisuite.middleware.vendor.group

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.vendor.group.infrastructure.VendorGroupRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorGroupService @Inject constructor(
   private val vendorGroupRepository: VendorGroupRepository,
   private val vendorGroupValidator: VendorGroupValidator
) {

   fun fetchById(id: Long, company: Company): VendorGroupDTO? =
      vendorGroupRepository.findOne(id, company)?.let { VendorGroupDTO(entity = it) }

   fun fetchAll(company: Company, pageRequest: PageRequest): Page<VendorGroupDTO> {
      val found = vendorGroupRepository.findAll(pageRequest, company)

      return found.toPage { vendorGroup: VendorGroupEntity ->
         VendorGroupDTO(vendorGroup)
      }
   }

   fun create(dto: VendorGroupDTO, company: Company): VendorGroupDTO {
      val toCreate = vendorGroupValidator.validateCreate(dto, company)

      return VendorGroupDTO(
         entity = vendorGroupRepository.insert(toCreate, company)
      )
   }

   fun update(id: Long, dto: VendorGroupDTO, company: Company): VendorGroupDTO {
      val toUpdate = vendorGroupValidator.validateUpdate(id, dto, company)

      return VendorGroupDTO(
         entity = vendorGroupRepository.update(entity = toUpdate)
      )
   }

   fun delete(id: Long, company: Company) {
      vendorGroupRepository.delete(id, company)
   }
}
