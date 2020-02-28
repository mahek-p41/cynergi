package com.cynergisuite.middleware.store

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreService @Inject constructor(
   private val storeRepository: StoreRepository
) {
   fun fetchById(id: Long, company: Company): StoreValueObject? =
      storeRepository.findOne(id, company)?.let { StoreValueObject(entity = it) }

   fun fetchByNumber(number: Int, company: Company): StoreValueObject? =
      storeRepository.findOne(number, company)?.let { StoreValueObject(entity = it) }

   fun fetchAll(pageRequest: PageRequest, company: Company): Page<StoreValueObject> {
      val stores = storeRepository.findAll(pageRequest, company)

      return stores.toPage { store ->
         StoreValueObject(store)
      }
   }

   fun exists(id: Long, company: Company): Boolean =
      storeRepository.exists(id = id, company = company)

   fun exists(number: Int, company: Company): Boolean =
      storeRepository.exists(number = number, company = company)
}
