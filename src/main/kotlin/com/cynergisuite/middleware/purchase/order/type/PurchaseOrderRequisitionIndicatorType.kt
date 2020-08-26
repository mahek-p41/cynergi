package com.cynergisuite.middleware.purchase.order.type

import com.cynergisuite.domain.TypeDomainEntity

data class PurchaseOrderRequisitionIndicatorType(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<PurchaseOrderRequisitionIndicatorType> {

   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
