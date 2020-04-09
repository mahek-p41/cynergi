package com.cynergisuite.middleware.store

import com.cynergisuite.middleware.company.Company

data class SimpleStore(
   val id: Long,
   val number: Int,
   val name: String,
   val company: Company
): Store {
   override fun myId(): Long  = id
   override fun myNumber(): Int = number
   override fun myName(): String = name
   override fun myCompany(): Company = company
}
