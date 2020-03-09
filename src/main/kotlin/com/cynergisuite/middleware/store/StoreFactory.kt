package com.cynergisuite.middleware.store

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.region.RegionEntity
import com.cynergisuite.middleware.store.infrastructure.RegionToStoreRepository
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
         company = CompanyFactory.tstds1()
      ),
      StoreEntity(
         id = 2,
         number = 3,
         name = "INDEPENDENCE",
         company = CompanyFactory.tstds1()
      ),
      StoreEntity(
         id = 4,
         number = 1,
         name = "Pelham Trading Post, Inc",
         company = CompanyFactory.tstds2()
      ),
      StoreEntity(
         id = 5,
         number = 2,
         name = "Camilla Trading Post, Inc.",
         company = CompanyFactory.tstds2()
      ),
      StoreEntity(
         id = 6,
         number = 3,
         name = "Arlington Trading Post",
         company = CompanyFactory.tstds2()
      ),
      StoreEntity(
         id = 7,
         number = 4,
         name = "Moultrie Trading Post, Inc",
         company = CompanyFactory.tstds2()
      ),
      StoreEntity(
         id = 8,
         number = 5,
         name = "Bainbridge Trading Post",
         company = CompanyFactory.tstds2()
      )
   )

   @JvmStatic
   fun random(company: Company): StoreEntity {
      return stores.filter { it.company.myDataset() == company.myDataset() }.random()
   }

   @JvmStatic
   fun store(number: Int, company: Company): StoreEntity {
      return stores.filter { it.company.myDataset() == company.myDataset() && it.number == number}.first()
   }

   fun stores(company: Company): List<StoreEntity> {
      return stores.filter { it.company.myDataset() == company.myDataset() }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class StoreFactoryService(
   private val storeRepository: StoreRepository,
   private val regionToStoreRepository: RegionToStoreRepository
) {

   fun createRegionToStore(store: StoreEntity, region: RegionEntity): Int =
      regionToStoreRepository.insert(RegionToStoreEntity(store = store, region = region)) ?: throw Exception("Unable to insert RegionToStore")

   fun store(storeNumber: Int, company: Company): StoreEntity =
      storeRepository.findOne(storeNumber, company) ?: throw Exception("Unable to find StoreEntity")

   fun companyStoresToRegion(company: Company, region: RegionEntity): Stream<Pair<RegionEntity, StoreEntity>> {
      return StoreFactory.stores(company).stream()
         .map { storeRepository.assignToRegion(it, region) }
   }

   fun random(company: Company): StoreEntity {
      val randomStore = StoreFactory.random(company)

      assert(company.myDataset() == randomStore.company.myDataset())

      val store = storeRepository.findOne(randomStore.number, company) ?: throw Exception("Unable to find StoreEntity")

      assert(store.company.myDataset() == company.myDataset())

      return store
   }
}
