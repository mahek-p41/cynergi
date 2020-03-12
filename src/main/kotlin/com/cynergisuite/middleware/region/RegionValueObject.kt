package com.cynergisuite.middleware.region

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.division.DivisionValueObject
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "Region", title = "A subset of a division", description = "An entity containing a rental region.")
data class RegionValueObject(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, nullable = true, description = "System generated ID")
   var id: Long? = null,

   @field:JsonIgnore
   @field:Schema(name = "division", description = "Division over this region", hidden = true)
   var division: DivisionEntity? = null,

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

   constructor(division: DivisionEntity, number: Int, name: String?, employeeNumber: Int?, description: String?) :
      this(
         id = null,
         division = division,
         number = number,
         name = name,
         employeeNumber = employeeNumber,
         description = description
      )

   constructor(entity: RegionEntity) :
      this(
         id = entity.id,
         division = entity.division,
         number = entity.number,
         name = entity.name,
         employeeNumber = entity.employeeNumber,
         description = entity.description
      )

   override fun myId(): Long? = id
}
