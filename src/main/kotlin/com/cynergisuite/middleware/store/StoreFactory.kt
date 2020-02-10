package com.cynergisuite.middleware.store

import com.cynergisuite.middleware.location.Location
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
   fun random() = random(dataset = "tstds1")

   @JvmStatic
   fun random(dataset: String): StoreEntity =
      stores.filter { it.dataset == dataset }.random()

   @JvmStatic
   fun randomNotMatchingDataset(dataset: String): StoreEntity =
      stores.filter { it.dataset != dataset }.random()

   @JvmStatic
   fun findByNumber(number: Int, dataset: String = "tstds1"): StoreEntity =
      stores.first { it.number == number && it.dataset == dataset }

   @JvmStatic
   fun storeOneTstds1(): StoreEntity = stores[0]

   @JvmStatic
   fun storeThreeTstds1(): StoreEntity = stores[1]
}

@Singleton
@Requires(env = ["develop", "test"])
class StoreFactoryService(
   private val storeRepository: StoreRepository
) {

   fun store(number: Int): StoreEntity =
      store(number = number, dataset = "tstds1")

   fun store(number: Int, dataset: String): StoreEntity =
      storeRepository.findOne(number, dataset) ?: throw Exception("Unable to find store $number")

   fun random(): StoreEntity =
      store(StoreFactory.random()) ?: throw Exception("Unable to find a random StoreEntity")

   fun randomNotMatchingDataset(dataset: String): StoreEntity {
      val store = StoreFactory.randomNotMatchingDataset(dataset)

      return store(store.number, store.dataset)
   }

   fun random(dataset: String): StoreEntity =
      store(StoreFactory.random(dataset)) ?: throw Exception("Unable to find a random StoreEntity")

   fun storeOneTstds1(): StoreEntity =
      store(StoreFactory.storeOneTstds1()) ?: throw Exception("Unable to find Store 1")

   fun storeThreeTstds1(): StoreEntity =
      store(StoreFactory.storeThreeTstds1()) ?: throw Exception("Unable to find Store 3")

   private fun store(location: Location): StoreEntity? =
      storeRepository.findOne(location.myNumber(), location.myDataset())
}
