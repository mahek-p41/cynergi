package com.cynergisuite.middleware.region

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.region.infrastructure.RegionRepository
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegionService @Inject constructor(
   private val regionRepository: RegionRepository,
   private val regionValidator: RegionValidator,
   private val storeRepository: StoreRepository
) {

   fun fetchById(id: Long, company: Company): RegionDTO? =
      regionRepository.findOne(id, company)?.let { RegionDTO(it) }

   fun fetchAll(company: Company, pageRequest: PageRequest): Page<RegionDTO> {
      val found = regionRepository.findAll(company, pageRequest)

      return found.toPage { region: RegionEntity ->
         RegionDTO(region)
      }
   }

   fun create(dto: RegionDTO, company: Company): RegionDTO {
      val toCreate = regionValidator.validateCreate(dto, company)

      return RegionDTO(regionRepository.insert(toCreate))
   }

   fun update(id: Long, dto: RegionDTO, company: Company): RegionDTO {
      val toUpdate = regionValidator.validateUpdate(id, dto, company)

      return RegionDTO(regionRepository.update(id, toUpdate))
   }

   fun delete(id: Long, company: Company): RegionDTO? {
      return regionRepository.delete(id, company)?.let { RegionDTO(it) }
   }

   fun assignStoreToRegion(regionId: Long, dto: SimpleIdentifiableDTO, company: Company) {
      val region = regionRepository.findOne(regionId, company) ?: throw NotFoundException(regionId)
      val store = storeRepository.findOne(dto.id!!, company) ?: throw NotFoundException(dto.id!!)

      if (regionRepository.isStoreAssignedToRegion(store, company)) {
         regionRepository.reassignStoreToRegion(region, store, company)
      } else {
         regionRepository.assignStoreToRegion(region, store, company)
      }
   }

   @Throws(NotFoundException::class)
   fun unassignStoreToRegion(regionId: Long, storeId: Long, company: Company) {
      val region = regionRepository.findOne(regionId, company) ?: throw NotFoundException(regionId)
      val store = storeRepository.findOne(storeId, company) ?: throw NotFoundException(storeId)

      regionRepository.unassignStoreToRegion(region, store, company)
   }
}
