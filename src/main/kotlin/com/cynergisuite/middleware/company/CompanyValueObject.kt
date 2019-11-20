package com.cynergisuite.middleware.company

import com.cynergisuite.domain.ValueObjectBase
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "Company", title = "An entity containing a rental company", description = "An entity containing a rental company.", requiredProperties = ["number"])
data class CompanyValueObject (

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, nullable = true, description = "System generated ID")
   var id: Long = 0,

   @field:Positive
   @field:NotNull
   @field:JsonProperty("companyNumber")
   @field:Schema(name = "number", minimum = "1", required = true, nullable = false, description = "Company number")
   var number: Int? = null,

   @field:Schema(name = "name", required = false, nullable = true, description = "Human readable name for a company")
   var name: String? = null,

   @field:Schema(name = "dataset", required = false, nullable = true, description = "Dataset that this company belongs to")
   var dataset: String? = null

) : ValueObjectBase<CompanyValueObject>() {

   constructor(entity: CompanyEntity) :
      this(
         id = entity.id,
         number = entity.number,
         name = entity.name,
         dataset = entity.dataset
      )

   override fun myId(): Long? = id
   override fun copyMe(): CompanyValueObject = copy()
}
