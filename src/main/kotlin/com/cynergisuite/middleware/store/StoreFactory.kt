package com.cynergisuite.middleware.store

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

@Singleton
@Requires(env = ["develop", "test"])
class StoreFactoryService(
   private val storeRepository: StoreRepository
) {

   fun store(storeNumber: Int, company: Company): StoreEntity =
      storeRepository.findOne(storeNumber, company) ?: throw Exception("Unable to find StoreEntity")
}
