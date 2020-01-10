package com.cynergisuite.middleware.store

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreService @Inject constructor(
   private val storeRepository: StoreRepository
) {
   fun fetchById(id: Long, dataset: String): StoreValueObject? =
      storeRepository.findOne(id = id, dataset = dataset)?.let { StoreValueObject(entity = it) }

   fun fetchByNumber(number: Int, dataset: String): StoreValueObject? =
      storeRepository.findOne(number, dataset)?.let { StoreValueObject(entity = it) }

   fun fetchAll(pageRequest: PageRequest, dataset: String): Page<StoreValueObject> {
      val stores = storeRepository.findAll(pageRequest, dataset)

      return stores.toPage { store ->
         StoreValueObject(store)
      }
   }

   fun exists(id: Long): Boolean =
      storeRepository.exists(id = id)

   fun exists(number: Int): Boolean =
      storeRepository.exists(number = number)
}
