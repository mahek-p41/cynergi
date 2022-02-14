package com.cynergisuite.middleware.store

import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.region.RegionEntity

/**
 * Represents a Store as loaded from system_stores_fimvw
 */
data class StoreEntity(
   val id: Long,
   val number: Int,
   val name: String,
   val region: RegionEntity? = null,
   val company: CompanyEntity,
) : Store {
   override fun myId(): Long = id
   override fun myNumber(): Int = number
   override fun myName(): String = name
   override fun myRegion(): RegionEntity? = region
   override fun myCompany(): CompanyEntity = region?.division?.company ?: company

   fun copyWithNewCompany(company: CompanyEntity) = copy(company = company)
}
