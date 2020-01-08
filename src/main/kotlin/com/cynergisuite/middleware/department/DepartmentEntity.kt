package com.cynergisuite.middleware.department

import com.cynergisuite.domain.Identifiable
import java.time.OffsetDateTime

data class DepartmentEntity(
   val id: Long,
   val code: String,
   val description: String,
   val securityProfile: Int,
   val defaultMenu: String,
   val dataset: String // aka company
) : Identifiable {
   override fun myId(): Long? = id
}
