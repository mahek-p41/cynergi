package com.cynergisuite.middleware.verfication

import com.cynergisuite.domain.LegacyIdentifiable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "VerificationEmployment", title = "Employment Verification for a customer", description = "Employment verification for a single customer associated with a Verification")
data class VerificationEmploymentValueObject(

   var id: Long? = null,

   @field:Size(max = 50)
   @field:JsonProperty("emp_dept")
   var department: String? = null,

   @field:JsonProperty("emp_hire_date")
   var hireDate: LocalDate? = null,

   @field:JsonProperty("emp_leave_msg")
   var leaveMessage: Boolean? = null,

   @field:Size(max = 50)
   @field:JsonProperty("emp_name")
   var name: String? = null,

   @field:JsonProperty("emp_reliable")
   var reliable: Boolean?,

   @field:Size(max = 50)
   @field:JsonProperty("emp_title")
   var title: String? = null

) : LegacyIdentifiable {

   constructor(entity: VerificationEmployment) :
      this(
         id = entity.id,
         department = entity.department,
         hireDate = entity.hireDate,
         leaveMessage = entity.leaveMessage,
         name = entity.name,
         reliable = entity.reliable,
         title = entity.title
      )

   override fun myId(): Long? = id
}
