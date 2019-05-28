package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.ValueObjectBase
import com.cynergisuite.middleware.localization.MessageCodes.Validation.MIN
import com.cynergisuite.middleware.localization.MessageCodes.Validation.NOT_NULL
import com.cynergisuite.middleware.localization.MessageCodes.Validation.POSITIVE
import com.cynergisuite.middleware.localization.MessageCodes.Validation.SIZE
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
   @field:Positive(message = POSITIVE)
   var id: Long? = null,

   @field:JsonIgnore
   @field:NotNull(message = NOT_NULL)
   val loc: String?,

   @field:NotNull(message = NOT_NULL)
   @field:Min(1, message = MIN)
   val number: Int?,

   @field:JsonIgnore
   @field:NotNull(message = NOT_NULL)
   @field:Size(min = 3, max = 6, message = SIZE)
   val passCode: String?,

   @field:JsonIgnore
   @field:NotNull(message = NOT_NULL)
   val active: Boolean? = true

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

   override fun dtoId(): Long? = id
   override fun copyMe(): EmployeeValueObject = copy()
}
