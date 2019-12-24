package com.cynergisuite.middleware.store

import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object StoreFactory {
   private val stores = listOf( // list of stores defined in cynergi-inittestdb.sql that aren't HOME OFFICE
      StoreEntity(
         id = 1,
         number = 1,
         name = "KANSAS CITY",
         dataset = "tstds1"
      ),
      StoreEntity(
         id = 2,
         number = 3,
         name = "INDEPENDENCE",
         dataset = "tstds1"
      ),
      StoreEntity(
         id = 4,
         number = 1,
         name = "Pelham Trading Post, Inc",
         dataset = "tstds2"
      ),
      StoreEntity(
         id = 5,
         number = 2,
         name = "Camilla Trading Post, Inc.",
         dataset = "tstds2"
      ),
      StoreEntity(
         id = 6,
         number = 3,
         name = "Arlington Trading Post",
         dataset = "tstds2"
      ),
      StoreEntity(
         id = 7,
         number = 4,
         name = "Moultrie Trading Post, Inc",
         dataset = "tstds2"
      ),
      StoreEntity(
         id = 8,
         number = 5,
         name = "Bainbridge Trading Post",
         dataset = "tstds2"
      )
   )

   @JvmStatic
   fun random(): StoreEntity = stores.random()

   @JvmStatic
   fun findByNumber(number: Int, dataset: String = "tstds1"): StoreEntity =
      stores.first { it.number == number && it.dataset == dataset }

   @JvmStatic
   fun storeOne(): StoreEntity = findByNumber(1)

   @JvmStatic
   fun storeThree(): StoreEntity = findByNumber(3)
}

@Singleton
@Requires(env = ["develop", "test"])
class StoreFactoryService(
   private val storeRepository: StoreRepository
) {

   fun store(number: Int, dataset: String = "tstds1"): StoreEntity =
      storeRepository.findOne(number, dataset) ?: throw Exception("Unable to find store $number")

   fun random(): StoreEntity =
      storeRepository.findOne(StoreFactory.random().number, "tstds1") ?: throw Exception("Unable to find random Store")

   fun storeOne(): StoreEntity =
      storeRepository.findOne(StoreFactory.storeOne().number, "tstds1") ?: throw Exception("Unable to find Store 1")

   fun storeThree(): StoreEntity =
      storeRepository.findOne(StoreFactory.storeThree().number, "tstds1") ?: throw Exception("Unable to find Store 3")
}
