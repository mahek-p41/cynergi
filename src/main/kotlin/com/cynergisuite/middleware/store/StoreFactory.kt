package com.cynergisuite.middleware.store

import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object StoreFactory {

   private val stores = listOf( // list of stores defined in cynergi-inittestdb.sql
      StoreEntity(
         id = 1,
         number = 1,
         name = "KANSAS CITY",
         dataset = "testds"
      ),
      StoreEntity(
         id = 2,
         number = 3,
         name = "INDEPENDENCE",
         dataset = "testds"
      ),
      StoreEntity(
         id = 3,
         number = 9000,
         name = "HOME OFFICE",
         dataset = "testds"
      )
   )

   @JvmStatic
   fun random(): StoreEntity = stores.random()

   @JvmStatic
   fun findByNumber(number: Int): StoreEntity = stores.first { it.number == number }

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

   fun store(number: Int): StoreEntity =
      storeRepository.findOneByNumber(number) ?: throw Exception("Unable to find store $number")

   fun random(): StoreEntity =
      storeRepository.findOneByNumber(StoreFactory.random().number) ?: throw Exception("Unable to find random Store")

   fun storeOne(): StoreEntity =
      storeRepository.findOneByNumber(StoreFactory.storeOne().number) ?: throw Exception("Unable to find Store 1")

   fun storeThree(): StoreEntity =
      storeRepository.findOneByNumber(StoreFactory.storeThree().number) ?: throw Exception("Unable to find Store 3")
}
