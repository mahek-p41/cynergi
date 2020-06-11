package com.cynergisuite.middleware.region

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.region.infrastructure.RegionRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class RegionService @Inject constructor(
   private val regionRepository: RegionRepository,
   private val regionValidator: RegionValidator
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

   fun assignStoreToRegion(regionId: Long, storeNumber: Int, myCompany: Company) {
      regionRepository.assignStoreToRegion(regionId, storeNumber, myCompany)
   }

   fun unassignStoreToRegion(regionId: Long, storeNumber: Int, myCompany: Company) {
      regionRepository.unassignStoreToRegion(regionId, storeNumber, myCompany)
   }
}
