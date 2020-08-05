package com.cynergisuite.middleware.location

import com.cynergisuite.middleware.company.Company

data class LocationEntity(
   val id: Long,
   val number: Int,
   val name: String,
   val company: Company
) : Location {
   override fun myId(): Long = id
   override fun myNumber(): Int = number
   override fun myName(): String = name
   override fun myCompany(): Company = company
}
