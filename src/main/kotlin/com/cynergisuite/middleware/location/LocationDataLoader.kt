package com.cynergisuite.middleware.location

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.location.infrastructure.LocationRepository
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object LocationDataLoader {

   @JvmStatic
   private val locations = listOf(
      LocationEntity(
         id = 1,
         number = 1,
         name = "Pelham Trading Post, Inc",
         company = CompanyFactory.tstds2()
      ),
      LocationEntity(
         id = 2,
         number = 1,
         name = "KANSAS CITY",
         company = CompanyFactory.tstds1()
      ),
      LocationEntity(
         id = 3,
         number = 2,
         name = "Camilla Trading Post, Inc.",
         company = CompanyFactory.tstds2()
      ),
      LocationEntity(
         id = 4,
         number = 3,
         name = "Arlington Trading Post",
         company = CompanyFactory.tstds2()
      ),
      LocationEntity(
         id = 5,
         number = 3,
         name = "INDEPENDENCE 4",
         company = CompanyFactory.tstds1()
      ),
      LocationEntity(
         id = 6,
         number = 4,
         name = "Moultrie Trading Post, Inc",
         company = CompanyFactory.tstds2()
      ),
      LocationEntity(
         id = 7,
         number = 5,
         name = "Bainbridge Trading Post",
         company = CompanyFactory.tstds2()
      )
   )

   @JvmStatic
   fun random(company: Company): Location =
      locations.filter { it.myCompany().myDataset() == company.myDataset() }.random()

   @JvmStatic
   fun location(number: Int, company: Company): Location =
      locations.filter { it.company.myDataset() == company.myDataset() && it.number == number }.first()

   @JvmStatic
   fun locations(company: Company): List<Location> =
      locations.filter { it.company.myDataset() == company.myDataset() }
}

@Singleton
@Requires(env = ["develop", "test"])
class LocationDataLoaderService(
   private val locationRepository: LocationRepository
) {

   fun location(locationNumber: Int, company: Company): Location =
      locationRepository.findOne(locationNumber, company)
         ?.let {
            LocationEntity(
               id = it.myId(),
               number = it.myNumber(),
               name = it.myName(),
               company = it.myCompany()
            )
         }
         ?: throw Exception("Unable to find Location")

   fun random(company: Company): LocationEntity {
      val randomLocation = LocationDataLoader.random(company)

      assert(company.myDataset() == randomLocation.myCompany().myDataset())

      val location = locationRepository.findOne(randomLocation.myNumber(), company) ?: throw Exception("Unable to find LocationEntity")

      assert(location.myCompany().myDataset() == company.myDataset())

      return location
   }
}
