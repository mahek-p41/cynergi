package com.cynergisuite.middleware.accounting.financial.calendar.type

import com.cynergisuite.domain.TypeDomain
import io.micronaut.core.annotation.Introspected

@Introspected
data class OverallPeriodType(
   val id: Int,
   val value: String,
   val abbreviation: String,
   val description: String,
   val localizationCode: String
) : TypeDomain() {

   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
