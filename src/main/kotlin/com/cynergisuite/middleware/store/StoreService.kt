package com.cynergisuite.middleware.store

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SearchPageRequest
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.Locale

@Singleton
class StoreService @Inject constructor(
   private val storeRepository: StoreRepository
) {
   fun fetchById(id: Long, company: CompanyEntity): StoreDTO? =
      storeRepository.findOne(id, company)?.let { StoreDTO(entity = it) }

   fun fetchAll(pageRequest: PageRequest, user: User): Page<StoreDTO> {
      val stores = storeRepository.findAll(pageRequest, user)

      return stores.toPage { store ->
         StoreDTO(store)
      }
   }

   fun search(company: CompanyEntity, pageRequest: SearchPageRequest, locale: Locale): Page<StoreDTO> {
      val found = storeRepository.search(company, pageRequest)

      return found.toPage { store ->
         StoreDTO(store)
      }
   }

   private fun transformEntity(storeEntity: StoreEntity): StoreDTO {

      return StoreDTO(
         id = storeEntity.id,
         storeNumber = storeEntity.number,
         name = storeEntity.name,
         region = storeEntity.region!!.toValueObject(),
      )
   }

   fun exists(id: Long, company: CompanyEntity): Boolean =
      storeRepository.exists(id = id, company = company)

   fun exists(number: Int, company: CompanyEntity): Boolean =
      storeRepository.exists(number = number, company = company)
}
