package com.cynergisuite.middleware.division

import com.cynergisuite.domain.Identifiable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "Division", title = "A subset of company", description = "An entity containing a rental division.")
data class DivisionValueObject(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, nullable = true, description = "System generated ID")
   var id: Long? = null,

   @field:Positive
   @field:NotNull
   @field:JsonProperty("number")
   @field:Schema(name = "number", minimum = "1", required = true, nullable = false, description = "Unique division number")
   var number: Int? = null,

   @field:NotNull
   @field:Schema(name = "name", required = false, nullable = false, description = "Human readable name for a division")
   var name: String? = null,

   @field:Positive
   @field:NotNull
   @field:JsonProperty("employeeNumber")
   @field:Schema(name = "number", minimum = "1", required = true, nullable = false, description = "Employee Number for the division head")
   var employeeNumber: Int? = null,

   @field:NotNull
   @field:Schema(name = "description", required = true, nullable = false, description = "Division description")
   var description: String? = null

) : Identifiable {

   constructor(number: Int, name: String?, employeeNumber: Int?, description: String?) :
      this(
         id = null,
         number = number,
         name = name,
         employeeNumber = employeeNumber,
         description = description
      )

   constructor(entity: DivisionEntity) :
      this(
         id = entity.id,
         number = entity.number,
         name = entity.name,
         employeeNumber = entity.employeeNumber,
         description = entity.description
      )

   override fun myId(): Long? = id
}
