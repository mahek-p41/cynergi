package com.cynergisuite.middleware.purchase.order

import com.cynergisuite.domain.TypeDomainEntity

data class PurchaseOrderType(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<PurchaseOrderType> {

   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
