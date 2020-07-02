package com.cynergisuite.middleware.region

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.region.infrastructure.RegionRepository
import com.cynergisuite.middleware.store.StoreDTO
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class RegionService @Inject constructor(
   private val regionRepository: RegionRepository,
   private val regionValidator: RegionValidator,
   private val storeRepository: StoreRepository
) {


   fun fetchById(id: Long, company: Company): RegionDTO? =
      regionRepository.findOne(id, company)?.let { RegionDTO(it) }

   @Validated
   fun fetchAll(company: Company, @Valid pageRequest: PageRequest): Page<RegionDTO> {
      val found = regionRepository.findAll(company, pageRequest)

      return found.toPage { region: RegionEntity ->
         RegionDTO(region)
      }
   }

   @Validated
   fun create(@Valid regionDTO: RegionDTO, company: Company): RegionDTO {
      val toCreate = regionValidator.validateCreate(regionDTO, company)

      return RegionDTO(regionRepository.insert(toCreate))
   }

   @Validated
   fun update(id: Long, @Valid regionDTO: RegionDTO, company: Company): RegionDTO {
      val toUpdate = regionValidator.validateUpdate(id, regionDTO, company)

      return RegionDTO(regionRepository.update(toUpdate))
   }

   fun delete(id: Long, company: Company): RegionDTO? {
      return regionRepository.delete(id, company)?.let { RegionDTO(it) }
   }

   @Validated
   fun assignStoreToRegion(regionId: Long, @Valid storeDTO: SimpleIdentifiableDTO, company: Company) {
      val region = regionRepository.findOne(regionId, company) ?: throw NotFoundException(regionId)
      val store = storeRepository.findOne(storeDTO.id!!, company) ?: throw NotFoundException(storeDTO.id!!)

      regionRepository.assignStoreToRegion(region, store, company)
   }

   @Throws(NotFoundException::class)
   fun unassignStoreToRegion(regionId: Long, storeId: Long, company: Company) {
      val region = regionRepository.findOne(regionId, company) ?: throw NotFoundException(regionId)
      val store = storeRepository.findOne(storeId, company) ?: throw NotFoundException(storeId)

      regionRepository.unassignStoreToRegion(region, store, company)
   }
}
