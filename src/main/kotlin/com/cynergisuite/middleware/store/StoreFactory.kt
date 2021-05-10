package com.cynergisuite.middleware.store

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.location.Location
import com.cynergisuite.middleware.region.RegionEntity
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.context.annotation.Requires
import java.util.stream.Stream
import javax.inject.Singleton

object StoreFactory {

   @JvmStatic
   private val stores = listOf( // list of stores defined in cynergi-inittestdb.sql that aren't HOME OFFICE
      StoreEntity(
         id = 1,
         number = 1,
         name = "KANSAS CITY",
         company = CompanyFactory.tstds1(),
      ),
      StoreEntity(
         id = 2,
         number = 3,
         name = "INDEPENDENCE",
         company = CompanyFactory.tstds1(),
      ),
      StoreEntity(
         id = 4,
         number = 1,
         name = "Pelham Trading Post, Inc",
         company = CompanyFactory.tstds2(),
      ),
      StoreEntity(
         id = 5,
         number = 2,
         name = "Camilla Trading Post, Inc.",
         company = CompanyFactory.tstds2(),
      ),
      StoreEntity(
         id = 6,
         number = 3,
         name = "Arlington Trading Post",
         company = CompanyFactory.tstds2(),
      ),
      StoreEntity(
         id = 7,
         number = 4,
         name = "Moultrie Trading Post, Inc",
         company = CompanyFactory.tstds2(),
      ),
      StoreEntity(
         id = 8,
         number = 5,
         name = "Bainbridge Trading Post",
         company = CompanyFactory.tstds2(),
      )
   )

   @JvmStatic
   fun random(company: Company): Store {
      return stores.filter { it.myCompany().myDataset() == company.myDataset() }.random()
   }

   @JvmStatic
   fun store(number: Int, company: Company): Store {
      return stores.first { it.company.myDataset() == company.myDataset() && it.number == number }
   }

   fun stores(company: Company): List<Store> {
      return stores.filter { it.company.myDataset() == company.myDataset() }
   }

   fun storesDevelop(company: Company): List<Store> {
      return stores
         .map { store ->
            when (store.company.myDataset()) {
               CompanyFactory.tstds1().datasetCode -> store.copy(company = CompanyFactory.corrto())
               CompanyFactory.tstds2().datasetCode -> store.copy(company = CompanyFactory.corptp())
               else -> store
            }
         }
         .filter { it.company.myDataset() == company.myDataset() }
         .toList()
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class StoreFactoryService(
   private val storeRepository: StoreRepository
) {

   fun store(storeNumber: Int, company: Company): Store =
      storeRepository.findOne(storeNumber, company)
         ?.let {
            StoreEntity(
               id = it.myId(),
               name = it.myName(),
               number = it.myNumber(),
               company = it.myCompany(),
            )
         }
         ?: throw Exception("Unable to find Store")

   fun companyStoresToRegion(region: RegionEntity, limit: Long): Stream<Pair<RegionEntity, Location>> {
      return StoreFactory.stores(region.division.company).stream().limit(limit)
         .map { storeRepository.assignToRegion(it, region, region.division.company.myId()!!) }
   }

   fun companyStoresToRegion(region: RegionEntity, vararg stores: Store): List<Pair<RegionEntity, Location>> {
      return stores.map { storeRepository.assignToRegion(it, region, region.division.company.myId()!!) }
   }

   fun random(company: Company): StoreEntity {
      val randomStore = StoreFactory.random(company)

      assert(company.myDataset() == randomStore.myCompany().myDataset())

      val store = storeRepository.findOne(randomStore.myNumber(), company) ?: throw Exception("Unable to find StoreEntity")

      assert(store.myCompany().myDataset() == company.myDataset())

      return store
   }
}
