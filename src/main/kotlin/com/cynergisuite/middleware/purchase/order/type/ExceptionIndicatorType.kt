package com.cynergisuite.middleware.purchase.order.type

import com.cynergisuite.domain.TypeDomainEntity

data class ExceptionIndicatorType(
   val id: Int,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<ExceptionIndicatorType> {

   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
