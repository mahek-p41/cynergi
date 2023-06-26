package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.LegacyIdentifiable
import com.cynergisuite.middleware.authentication.user.SecurityGroupDTO
import com.cynergisuite.middleware.store.StoreDTO
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@Introspected
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(value = ["passCode", "store", "active"], allowSetters = true)
@Schema(name = "Employee", title = "Employee/User", description = "Describes an employee and user within the system")
data class EmployeeValueObject(

   @field:Positive
   @field:Schema(name = "id", description = "System generated ID for the Employee/User")
   var id: Long? = null,

   @field:JsonIgnoreProperties(allowSetters = true)
   @field:NotNull
   @field:Schema(name = "type", description = "Where the employee definition's data came from")
   var type: String? = null,

   @field:NotNull
   @field:Min(1)
   @field:Schema(name = "number", description = "System generated number for the employee", minimum = "1", required = true, nullable = false)
   var number: Int? = null,

   @field:NotNull
   @field:Size(min = 2, max = 15)
   @field:Schema(name = "lastName", description = "Employee's family name", minLength = 2, maxLength = 15, required = true, nullable = false)
   var lastName: String? = null,

   @field:Size(min = 0, max = 15)
   @field:Schema(name = "firstNameMi", description = "Employee's given name", minLength = 2, maxLength = 15, required = true, nullable = false)
   var firstNameMi: String? = null,

   @field:JsonIgnoreProperties
   @field:NotNull
   @field:Size(min = 3)
   @field:Schema(name = "passCode", description = "Hidden passcode not visible to calling clients associated with an employee/user", minimum = "3", hidden = true)
   var passCode: String? = null,

   @field:JsonIgnoreProperties
   @field:Schema(name = "store", description = "Default store Employee is associated with", hidden = true)
   var store: StoreDTO? = null,

   @field:NotNull
   @field:Size(min = 1, max = 1)
   @field:Schema(name = "altStoreIndicator", description = "Employee's alternate store indicator, must be N|R|D|A", minLength = 1, maxLength = 1, required = true, nullable = false)
   var alternativeStoreIndicator: String? = null,

   @field:NotNull
   @field:Min(value = 1)
   @field:Schema(name = "alternativeArea", description = "Employee's alternate area")
   var alternativeArea: Long? = null,

   @field:JsonIgnoreProperties
   @field:NotNull
   @field:Schema(name = "active", description = "true|false value describing whether an employee/user is active or not", hidden = true)
   var active: Boolean? = true,

   @field:NotNull
   @field:Schema(name = "securityGroup", description = "Employee's security groups", hidden = true)
   var securityGroups: MutableList<SecurityGroupDTO>? = null,

) : LegacyIdentifiable {

   constructor(id: Long, type: String, number: Int, lastName: String, firstNameMi: String, passCode: String, store: StoreDTO, active: Boolean, altStoreIndicator: String, alternativeArea: Long, securityGroups: MutableList<SecurityGroupDTO>?) :
      this(
         id = id,
         type = type,
         number = number,
         lastName = lastName,
         firstNameMi = firstNameMi,
         passCode = passCode,
         store = store,
         active = active,
         alternativeStoreIndicator = altStoreIndicator,
         alternativeArea = alternativeArea,
         securityGroups = securityGroups
      )

   constructor(entity: EmployeeEntity) :
      this(
         id = entity.id,
         type = entity.type,
         number = entity.number,
         lastName = entity.lastName,
         firstNameMi = entity.firstNameMi,
         passCode = entity.passCode,
         store = entity.store?.let { StoreDTO(it) },
         active = entity.active,
         alternativeStoreIndicator = entity.alternativeStoreIndicator,
         alternativeArea = entity.alternativeArea,
         securityGroups = entity.securityGroups?.map{SecurityGroupDTO(it)} as MutableList<SecurityGroupDTO>
      )
   override fun myId(): Long? = id

   override fun equals(other: Any?): Boolean =
      if (other is EmployeeValueObject) {
         this.number == other.number
      } else {
         false
      }

   override fun hashCode(): Int = number.hashCode()
}
