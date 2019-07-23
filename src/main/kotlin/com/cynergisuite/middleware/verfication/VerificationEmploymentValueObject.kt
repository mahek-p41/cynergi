package com.cynergisuite.middleware.verfication

import com.cynergisuite.domain.ValueObjectBase
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "VerificationEmployment", description = "A verification for employment")
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

   @field:Size(max= 50)
   @field:JsonProperty("emp_title")
   var title: String? = null

) : ValueObjectBase<VerificationEmploymentValueObject>() {

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

   override fun copyMe(): VerificationEmploymentValueObject = copy()

   override fun valueObjectId(): Long? = id
}
