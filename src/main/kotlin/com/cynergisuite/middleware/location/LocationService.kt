package com.cynergisuite.middleware.location

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.location.infrastructure.LocationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationService @Inject constructor(
   private val locationRepository: LocationRepository
) {
   fun fetchById(id: Long, company: CompanyEntity): LocationDTO? =
      locationRepository.findOne(id, company)?.let { LocationDTO(entity = it) }

   fun fetchAll(pageRequest: PageRequest, company: CompanyEntity): Page<LocationDTO> {
      val locations = locationRepository.findAll(pageRequest, company)

      return locations.toPage { location ->
         LocationDTO(location)
      }
   }

   fun exists(id: Long, company: CompanyEntity): Boolean =
      locationRepository.exists(id = id, company = company)

   fun exists(number: Int, company: CompanyEntity): Boolean =
      locationRepository.exists(number = number, company = company)
}
