package com.cynergisuite.middleware.store

import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object StoreFactory {

   private val stores = listOf( // list of stores defined in cynergi-inittestdb.sql
      Store(
         id = 1,
         number = 1,
         name = "KANSAS CITY",
         dataset = "testds"
      ),
      Store(
         id = 2,
         number = 3,
         name = "INDEPENDENCE",
         dataset = "testds"
      ),
      Store(
         id = 3,
         number = 9000,
         name = "HOME OFFICE",
         dataset = "testds"
      )
   )

   @JvmStatic
   fun random(): Store {
      val faker = Faker()
      val random = faker.random()

      return stores[random.nextInt(0, stores.size-1)]
   }

   @JvmStatic
   fun findByNumber(number: Int): Store =
      stores.firstOrNull { it.number == number } ?: throw Exception("Unable to find store $number")
}

@Singleton
@Requires(env = ["demo", "test"])
class StoreFactoryService (
   private val storeRepository: StoreRepository
) {

   fun store(number: Int) : Store =
      storeRepository.findByNumber(number) ?: throw Exception("Unable to find store $number")

   fun random() : Store =
      storeRepository.findByNumber(StoreFactory.random().number) ?: throw Exception("Unable to find random Store")
}
