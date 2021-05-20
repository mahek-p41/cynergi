package com.cynergisuite.middleware.region

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.region.infrastructure.RegionRepository
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegionService @Inject constructor(
   private val regionRepository: RegionRepository,
   private val regionValidator: RegionValidator,
   private val storeRepository: StoreRepository
) {

   fun fetchById(id: UUID, company: CompanyEntity): RegionDTO? =
      regionRepository.findOne(id, company)?.let { RegionDTO(it) }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<RegionDTO> {
      val found = regionRepository.findAll(company, pageRequest)

      return found.toPage { region: RegionEntity ->
         RegionDTO(region)
      }
   }

   fun create(dto: RegionDTO, company: CompanyEntity): RegionDTO {
      val toCreate = regionValidator.validateCreate(dto, company)

      return RegionDTO(regionRepository.insert(toCreate))
   }

   fun update(id: UUID, dto: RegionDTO, company: CompanyEntity): RegionDTO {
      val toUpdate = regionValidator.validateUpdate(id, dto, company)

      return RegionDTO(regionRepository.update(id, toUpdate))
   }

   fun delete(id: UUID, company: CompanyEntity): RegionDTO? {
      return regionRepository.delete(id, company)?.let { RegionDTO(it) }
   }

   fun assignStoreToRegion(regionId: UUID, dto: SimpleLegacyIdentifiableDTO, company: CompanyEntity) {
      val region = regionRepository.findOne(regionId, company) ?: throw NotFoundException(regionId)
      val store = storeRepository.findOne(dto.id!!, company) ?: throw NotFoundException(dto.id!!)

      if (regionRepository.isStoreAssignedToRegion(store, company)) {
         regionRepository.reassignStoreToRegion(region, store, company)
      } else {
         regionRepository.assignStoreToRegion(region, store, company)
      }
   }

   fun disassociateStoreFromRegion(regionId: UUID, storeId: Long, company: CompanyEntity) {
      val region = regionRepository.findOne(regionId, company) ?: throw NotFoundException(regionId)
      val store = storeRepository.findOne(storeId, company) ?: throw NotFoundException(storeId)

      regionRepository.disassociateStoreFromRegion(region, store, company)
   }
}
