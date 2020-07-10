package com.cynergisuite.middleware.department

import com.cynergisuite.middleware.company.Company

data class DepartmentEntity(
   val id: Long,
   val code: String,
   val description: String?,
   val company: Company // aka company
) : Department, Comparable<Department> {
   override fun myId(): Long? = id
   override fun myCode(): String = code
   override fun myCompany(): Company = company

   override fun hashCode(): Int =
      code.hashCode()

   override fun equals(other: Any?): Boolean =
      if (other is Department) {
         code == other.myCode()
      } else {
         false
      }

   override fun toString(): String =
      code

   override fun compareTo(other: Department): Int =
      code.compareTo(other.myCode())
}
