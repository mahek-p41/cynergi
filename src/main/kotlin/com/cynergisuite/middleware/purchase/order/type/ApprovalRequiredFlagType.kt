package com.cynergisuite.middleware.purchase.order.type

import com.cynergisuite.domain.TypeDomainEntity

data class ApprovalRequiredFlagType(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<ApprovalRequiredFlagType> {

   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
