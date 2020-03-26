package com.cynergisuite.middleware.division

import com.cynergisuite.middleware.company.CompanyValueObject
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Schema(name = "Division", title = "Division", description = "A division of a company.")
data class DivisionValueObject(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", description = "System generated ID")
   var id: Long? = null,

   @field:Positive
   @field:JsonProperty("divisionNumber")
   @field:Schema(name = "number", minimum = "1", description = "Division number")
   var number: Int? = null,

   @field:NotNull
   @field:Schema(name = "name", description = "Human readable name for a division")
   var name: String? = null,

   @field:NotNull
   @field:Schema(name = "description", description = "Division description")
   var description: String? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "company", description = "Company that a division belong to")
   var company: CompanyValueObject? = null
)
