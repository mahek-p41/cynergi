package com.cynergisuite.middleware.store

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.region.RegionEntity
import java.time.LocalDate

/**
 * Represents a Store as loaded from fastinfo_prod_import.store_vw
 */
data class StoreEntity(
   val id: Long,
   val number: Int,
   val name: String,
   val region: RegionEntity? = null,
   val company: Company,
   val effectiveDate: LocalDate,
   val endingDate: LocalDate? = null,
) : Store {
   override fun myId(): Long = id
   override fun myNumber(): Int = number
   override fun myName(): String = name
   override fun myRegion(): RegionEntity? = region
   override fun myCompany(): Company = region?.division?.company ?: company
}
