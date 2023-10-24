package com.cynergisuite.middleware.vendor.rebate

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import com.cynergisuite.middleware.vendor.rebate.infrastructure.RebateRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class RebateService @Inject constructor(
   private val rebateRepository: RebateRepository,
   private val rebateValidator: RebateValidator,
   private val vendorRepository: VendorRepository
) {

   fun fetchById(id: UUID, company: CompanyEntity): RebateDTO? =
      rebateRepository.findOne(id, company)?.let { RebateDTO(entity = it) }

   fun create(dto: RebateDTO, company: CompanyEntity): RebateDTO {
      val toCreate = rebateValidator.validateCreate(dto, company)

      return transformEntity(rebateRepository.insert(toCreate, company))
   }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<RebateDTO> {
      val found = rebateRepository.findAll(company, pageRequest)

      return found.toPage { rebateEntity: RebateEntity ->
         RebateDTO(rebateEntity)
      }
   }

   fun update(id: UUID, dto: RebateDTO, company: CompanyEntity): RebateDTO {
      val toUpdate = rebateValidator.validateUpdate(id, dto, company)

      return transformEntity(rebateRepository.update(toUpdate, company))
   }

   fun assignVendorToRebate(rebateId: UUID, dto: SimpleIdentifiableDTO, company: CompanyEntity) {
      val rebate = rebateRepository.findOne(rebateId, company) ?: throw NotFoundException(rebateId)
      val vendor = vendorRepository.findOne(dto.id!!, company) ?: throw NotFoundException(dto.id!!)

      rebateRepository.assignVendorToRebate(rebate, vendor)
   }

   fun disassociateVendorFromRebate(rebateId: UUID, vendorId: UUID, company: CompanyEntity) {
      val rebate = rebateRepository.findOne(rebateId, company) ?: throw NotFoundException(rebateId)
      val vendor = vendorRepository.findOne(vendorId, company) ?: throw NotFoundException(vendorId)

      rebateRepository.disassociateVendorFromRebate(rebate, vendor)
   }

   private fun transformEntity(rebateEntity: RebateEntity): RebateDTO {
      return RebateDTO(entity = rebateEntity)
   }
}
