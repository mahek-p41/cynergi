package com.cynergisuite.middleware.wow

import com.cynergisuite.middleware.company.CompanyEntity

data class WowActiveInventoryEntity(
   val company: CompanyEntity,
   val storeNumber: Int, // FIXME Convert to using Store when data is in cynergidb and not coming from Fastinfo
   val sku: String,
   val itemName: String?,
   val itemDescription: String?,
   val totalQuantity: Int
)
