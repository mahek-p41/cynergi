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

   fun fetchById(id: Long, company: Company): VendorGroupValueObject? =
      vendorGroupRepository.findOne(id, company)?.let{ VendorGroupValueObject(entity = it) }

   fun fetchAll(company: Company, pageRequest: PageRequest): Page<VendorGroupValueObject> {
      val found = vendorGroupRepository.findAll(pageRequest, company)

      return found.toPage { vendorGroup: VendorGroupEntity ->
         VendorGroupValueObject(vendorGroup)
      }
   }

   @Validated
   fun create(@Valid vo: VendorGroupValueObject, company: Company): VendorGroupValueObject {
      logger.debug("VendorGroup Create Before Validation VendorGroupVO {}", vo)
      val toCreate = vendorGroupValidator.validateCreate(vo, company)
      logger.debug("VendorGroup Create After Validation VendorGroupEntity {}", toCreate)
      return VendorGroupValueObject(
         entity = vendorGroupRepository.insert(entity = toCreate)
      )
   }

   @Validated
   fun update(id: Long, @Valid vo: VendorGroupValueObject, company: Company): VendorGroupValueObject {
      val toUpdate = vendorGroupValidator.validateUpdate(id, vo, company)

      return VendorGroupValueObject(
         entity = vendorGroupRepository.update(entity = toUpdate)
      )
   }

}
