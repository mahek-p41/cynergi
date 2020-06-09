package com.cynergisuite.middleware.store

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreService @Inject constructor(
   private val storeRepository: StoreRepository
) {
   fun fetchById(id: Long, company: Company): StoreDTO? =
      storeRepository.findOne(id, company)?.let { StoreDTO(entity = it) }

   fun fetchAll(pageRequest: PageRequest, user: User): Page<StoreDTO> {
      val stores = storeRepository.findAll(pageRequest, user)

      return stores.toPage { store ->
         StoreDTO(store)
      }
   }

   fun exists(id: Long, company: Company): Boolean =
      storeRepository.exists(id = id, company = company)

   fun exists(number: Int, company: Company): Boolean =
      storeRepository.exists(number = number, company = company)
}
