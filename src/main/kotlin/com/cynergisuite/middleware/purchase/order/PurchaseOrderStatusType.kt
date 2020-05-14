package com.cynergisuite.middleware.purchase.order

import com.cynergisuite.domain.TypeDomainEntity

data class PurchaseOrderStatusType(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String,
   val possibleDefault: Boolean
) : TypeDomainEntity<PurchaseOrderStatusType> {

   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
   fun myPossibleDefault(): Boolean = possibleDefault
}
