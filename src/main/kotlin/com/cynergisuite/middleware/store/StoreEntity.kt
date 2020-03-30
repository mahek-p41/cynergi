package com.cynergisuite.middleware.store

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.region.RegionEntity

/**
 * Represents a Store as loaded from fastinfo_prod_import.store_vw
 */
data class StoreEntity(
   val id: Long,
   val number: Int,
   val name: String,
   val region: RegionEntity
) : Store {
   override fun myId(): Long = id
   override fun myNumber(): Int = number
   override fun myName(): String = name
   override fun myCompany(): Company = region.division.company
}
