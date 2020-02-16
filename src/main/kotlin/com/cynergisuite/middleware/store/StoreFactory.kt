package com.cynergisuite.middleware.store

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.company.CompanyFactoryService
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object StoreFactory {
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
   fun random(company: Company): StoreEntity =
      stores.filter { it.company == company }.random()

   @JvmStatic
   fun randomNotMatchingDataset(company: Company): StoreEntity =
      stores.filter { it.company != company }.random()

   @JvmStatic
   fun findByNumber(number: Int, company: Company = CompanyFactory.tstds1()): StoreEntity =
      stores.first { it.number == number && it.company == company }

   @JvmStatic
   fun storeOneTstds1(): StoreEntity{
      return stores.first { it.number == 1 && it.company.myDataset() == "tstds1" }
   }

   @JvmStatic
   fun storeThreeTstds1(): StoreEntity{
      return stores.first { it.number == 3 && it.company.myDataset() == "tstds1" }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class StoreFactoryService(
   private val companyFactoryService: CompanyFactoryService,
   private val storeRepository: StoreRepository
) {

   fun store(number: Int): StoreEntity =
      store(number = number, company = CompanyFactory.random())

   fun store(number: Int, company: Company): StoreEntity =
      storeRepository.findOne(number, company) ?: throw Exception("Unable to find store $number with company $company")

   fun random(): StoreEntity {
      val company = CompanyFactory.random()

      return store(StoreFactory.random(company)) ?: throw Exception("Unable to find a random StoreEntity")
   }

   fun randomNotMatchingDataset(company: Company): StoreEntity {
      val store = StoreFactory.randomNotMatchingDataset(company)

      return store(store.number, store.company)
   }

   fun random(company: Company): StoreEntity =
      store(StoreFactory.random(company)) ?: throw Exception("Unable to find a random StoreEntity")

   fun storeOneTstds1(): StoreEntity {
      val company = companyFactoryService.forDatasetCode("tstds1")
      val store = StoreFactory.storeOneTstds1().copy(company = company)

      return store(store) ?: throw Exception("Unable to find Store 1")
   }

   fun storeThreeTstds1(): StoreEntity {
      val company = companyFactoryService.forDatasetCode("tstds1")
      val store = StoreFactory.storeThreeTstds1().copy(company = company)

      return store(store) ?: throw Exception("Unable to find Store 3")
   }

   private fun store(location: StoreEntity): StoreEntity? =
      storeRepository.findOne(location.myNumber(), location.myCompany())
}
