package com.cynergisuite.middleware.location

import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.location.infrastructure.LocationRepository
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class LocationDataLoaderService {
   private final LocationRepository locationRepository

   @Inject
   LocationDataLoaderService(LocationRepository locationRepository) {
      this.locationRepository = locationRepository
   }

   Location location(int locationNumber, CompanyEntity company) {
      final toReturn = locationRepository.findOne(locationNumber, company)
         ?.with {
            new LocationEntity(
               it.myId(),
               it.myNumber(),
               it.myName()
            )
         }

      if (toReturn != null) {
         return toReturn
      } else {
         throw new Exception("Unable to find Location")
      }
   }
}
