package com.cynergisuite.middleware.vendor.group

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.ValidatorBase.Companion.logger
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.vendor.group.infrastructure.VendorGroupRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class VendorGroupService @Inject constructor(
   private val vendorGroupRepository: VendorGroupRepository,
   private val vendorGroupValidator: VendorGroupValidator
) {

   fun fetchById(id: Long, company: Company): VendorGroupDTO? =
      vendorGroupRepository.findOne(id, company)?.let{ VendorGroupDTO(entity = it) }

   fun fetchAll(company: Company, pageRequest: PageRequest): Page<VendorGroupDTO> {
      val found = vendorGroupRepository.findAll(pageRequest, company)

      return found.toPage { vendorGroup: VendorGroupEntity ->
         VendorGroupDTO(vendorGroup)
      }
   }

   @Validated
   fun create(@Valid vo: VendorGroupDTO, company: Company): VendorGroupDTO {
      val toCreate = vendorGroupValidator.validateCreate(vo, company)

      return VendorGroupDTO(
         entity = vendorGroupRepository.insert(entity = toCreate)
      )
   }

   @Validated
   fun update(id: Long, @Valid vo: VendorGroupDTO, company: Company): VendorGroupDTO {
      val toUpdate = vendorGroupValidator.validateUpdate(id, vo, company)

      return VendorGroupDTO(
         entity = vendorGroupRepository.update(entity = toUpdate)
      )
   }
}
