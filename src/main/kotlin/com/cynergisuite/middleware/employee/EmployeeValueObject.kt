package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.ValueObjectBase
import com.cynergisuite.middleware.store.StoreValueObject
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "Employee", title = "Employee/User",  description = "Describes an employee and user within the system")
data class EmployeeValueObject(

   @field:JsonIgnore
   @field:Positive
   @field:Schema(name = "id", description = "System generated ID for the Employee/User", hidden = true)
   var id: Long? = null,

   @field:JsonIgnore
   @field:NotNull
   @field:Schema(name = "type", description = "Where the employee definition's data came from", required = true, nullable = false, hidden = true)
   var type: String?,

   @field:NotNull
   @field:Min(1)
   @field:Schema(name = "number", description = "System generated number for the employee", minimum = "1", required = true, nullable = false)
   var number: Int?,

   @field:NotNull
   @field:Size(min = 6, max = 6)
   @field:Schema(name = "dataset", description = "Key for determining a user's company via a dataset", minimum = "6", maximum = "6")
   var company: Company?,

   @field:NotNull
   @field:Size(min = 2, max = 15)
   @field:Schema(name = "lastName", description = "Employee's family name", minLength = 2, maxLength = 15, required = true, nullable = false)
   var lastName: String?,

   @field:Size(min = 0, max = 15)
   @field:Schema(name = "firstNameMi", description = "Employee's given name", minLength = 2, maxLength = 15, required = true, nullable = false)
   var firstNameMi: String?,

   @field:JsonIgnore
   @field:NotNull
   @field:Size(min = 3)
   @field:Schema(name = "passCode", description = "Hidden passcode not visible to calling clients associated with an employee/user", minimum = "3", hidden = true)
   var passCode: String?,

   @field:JsonIgnore
   @field:Schema(name = "store", description = "Default store Employee is associated with", hidden = true)
   var store: StoreValueObject?,

   @field:JsonIgnore
   @field:NotNull
   @field:Schema(name = "active", description = "true|false value describing whether an employee/user is active or not", hidden = true)
   var active: Boolean? = true,

   @field:JsonIgnore
   @field:NotNull
   @field:Schema(name = "allowAutoStoreAssign", description = "true|false value describing whether an employee/user can have a store auto assigned by the system or not", hidden = true)
   var allowAutoStoreAssign: Boolean? = false

) : ValueObjectBase<EmployeeValueObject>() {

   constructor(type: String, number: Int, company: Company, lastName: String, firstNameMi: String, passCode: String, store: StoreValueObject, allowAutoStoreAssign: Boolean, active: Boolean) :
      this(
         id = null,
         type = type,
         number = number,
         dataset = dataset,
         lastName = lastName,
         firstNameMi = firstNameMi,
         passCode = passCode,
         store = store,
         active = active,
         allowAutoStoreAssign = allowAutoStoreAssign
      )

   constructor(entity: EmployeeEntity) :
      this(
         id = entity.id,
         type = entity.type,
         number = entity.number,
         dataset = entity.dataset,
         lastName = entity.lastName,
         firstNameMi = entity.firstNameMi,
         passCode = entity.passCode,
         store = entity.store?.let { StoreValueObject(it) },
         active = entity.active,
         allowAutoStoreAssign = entity.allowAutoStoreAssign
      )

   override fun myId(): Long? = id
   override fun copyMe(): EmployeeValueObject = copy()

   override fun equals(other: Any?): Boolean =
      if (other is EmployeeValueObject) {
         this.number == other.number
      } else {
         false
      }

   override fun hashCode(): Int = number.hashCode()
}
