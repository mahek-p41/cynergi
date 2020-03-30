package com.cynergisuite.middleware.department

import com.cynergisuite.middleware.company.Company

data class DepartmentEntity(
   val id: Long,
   val code: String,
   val description: String,
   val securityProfile: Int,
   val defaultMenu: String,
   val company: Company // aka company
) : Department {
   override fun myId(): Long? = id
   override fun myCode(): String = code
   override fun myCompany(): Company = company
}
