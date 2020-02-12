package com.cynergisuite.middleware.department

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.Company
import java.time.OffsetDateTime

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
}
