package com.cynergisuite.middleware.employee

data class SimpleEmployee(
   val id: Long?,
   val number: Int
): Employee {
   override fun myId(): Long? = id
   override fun myNumber(): Int = number
   override fun copyMe(): Employee = copy()
}
