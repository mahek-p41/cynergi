package com.cynergisuite.middleware.accounting.routine.type

import com.cynergisuite.domain.TypeDomainEntity

data class OverallPeriodType(
   val id: Int,
   val value: String,
   val abbreviation: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<OverallPeriodType> {

   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
