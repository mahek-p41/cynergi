package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.ValueObjectBase
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
data class EmployeeValueObject(

   @field:JsonIgnore
   @field:Positive
   var id: Long? = null,

   @field:JsonIgnore
   @field:NotNull
   var loc: String?,

   @field:NotNull
   @field:Min(1)
   var number: Int?,

   @field:JsonIgnore
   @field:NotNull
   @field:Size(min = 3, max = 6)
   var passCode: String?,

   @field:JsonIgnore
   @field:NotNull
   var active: Boolean? = true

) : ValueObjectBase<EmployeeValueObject>() {

   constructor(loc: String, number: Int, passCode: String, active: Boolean) :
      this(
         id = null,
         loc = loc,
         number = number,
         passCode = passCode,
         active = active
      )

   constructor(entity: Employee) :
      this(
         id = entity.id,
         loc = entity.loc,
         number = entity.number,
         passCode = entity.passCode,
         active = entity.active
      )

   override fun valueObjectId(): Long? = id
   override fun copyMe(): EmployeeValueObject = copy()
}
