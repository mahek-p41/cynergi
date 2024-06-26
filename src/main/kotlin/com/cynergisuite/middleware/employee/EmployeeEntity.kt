package com.cynergisuite.middleware.employee

data class EmployeeEntity(
   val id: Long? = null,
   val type: String,
   val number: Int,
   val lastName: String,
   val firstNameMi: String?,
   val passCode: String,
   val active: Boolean,
   val cynergiSystemAdmin: Boolean = false,

)
