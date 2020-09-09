package com.cynergisuite.middleware.location

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.location.infrastructure.LocationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationService @Inject constructor(
   private val locationRepository: LocationRepository
) {
   fun fetchById(id: Long, company: Company): LocationDTO? =
      locationRepository.findOne(id, company)?.let { LocationDTO(entity = it) }

   fun fetchAll(pageRequest: PageRequest, company: Company): Page<LocationDTO> {
      val locations = locationRepository.findAll(pageRequest, company)

      return locations.toPage { location ->
         LocationDTO(location)
      }
   }

   fun exists(id: Long, company: Company): Boolean =
      locationRepository.exists(id = id, company = company)

   fun exists(number: Int, company: Company): Boolean =
      locationRepository.exists(number = number, company = company)
}
