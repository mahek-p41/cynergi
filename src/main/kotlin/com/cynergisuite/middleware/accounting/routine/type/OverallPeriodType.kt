package com.cynergisuite.middleware.accounting.routine.type

import com.cynergisuite.domain.TypeDomainEntity

data class OverallPeriodType(
   val id: Long,
   val value: String,
   val abbreviation: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<OverallPeriodType> {

   override fun myId(): Long = id
   override fun myValue(): String = value
   fun myAbbreviation(): String = abbreviation
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
