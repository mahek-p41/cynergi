package com.cynergisuite.middleware.vendor.group

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.vendor.group.infrastructure.VendorGroupRepository
import java.util.UUID
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class VendorGroupService @Inject constructor(
   private val vendorGroupRepository: VendorGroupRepository,
   private val vendorGroupValidator: VendorGroupValidator
) {

   fun fetchById(id: UUID, company: CompanyEntity): VendorGroupDTO? =
      vendorGroupRepository.findOne(id, company)?.let { VendorGroupDTO(entity = it) }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<VendorGroupDTO> {
      val found = vendorGroupRepository.findAll(pageRequest, company)

      return found.toPage { vendorGroup: VendorGroupEntity ->
         VendorGroupDTO(vendorGroup)
      }
   }

   fun create(dto: VendorGroupDTO, company: CompanyEntity): VendorGroupDTO {
      val toCreate = vendorGroupValidator.validateCreate(dto, company)

      return VendorGroupDTO(
         entity = vendorGroupRepository.insert(toCreate, company)
      )
   }

   fun update(id: UUID, dto: VendorGroupDTO, company: CompanyEntity): VendorGroupDTO {
      val toUpdate = vendorGroupValidator.validateUpdate(id, dto, company)

      return VendorGroupDTO(
         entity = vendorGroupRepository.update(entity = toUpdate)
      )
   }

   fun delete(id: UUID, company: CompanyEntity) {
      vendorGroupRepository.delete(id, company)
   }
}
