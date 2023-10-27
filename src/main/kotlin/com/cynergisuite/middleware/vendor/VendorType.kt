package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.TypeDomain

data class VendorType(
   val id: Int,
   val value: Int,
   val description: String,
   val localizationCode: String,
) : TypeDomain() {

   override fun myValue(): String = value.toString()
   override fun myDescription(): String = description
   override fun myId(): Int = id
   override fun myLocalizationCode(): String = localizationCode
}
