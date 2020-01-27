package com.cynergisuite.middleware.company

import com.cynergisuite.domain.ValueObjectBase
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "Company", title = "An entity containing a rental company", description = "An entity containing a rental company.", requiredProperties = ["clientId", "name", "clientId", "datasetCode"])

data class CompanyValueObject(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, nullable = true, description = "System generated ID")
   var id: Long? = null,

   @field:NotNull
   @field:Schema(name = "name", required = false, nullable = true, description = "Human readable name for a company")
   var name: String? = null,

   @field:Schema(name = "doingBusinessAs", required = false, nullable = true, description = "Doing business as")
   var doingBusinessAs: String? = null,

   @field:NotNull
   @field:Schema(name = "clientCode", required = true, nullable = false, description = "Three digit company code")
   var clientCode: String? = null,

   @field:Positive
   @field:NotNull
   @field:JsonProperty("clientId")
   @field:Schema(name = "clientId", minimum = "1000", required = true, nullable = false, description = "Unique host company number, HT wide")
   var clientId: Int? = null,

   @field:NotNull
   @field:Schema(name = "datasetCode", required = false, nullable = true, description = "Dataset that this company belongs to")
   var datasetCode: String? = null,

   @field:Schema(name = "federalTaxNumber", required = true, nullable = false, description = "Federal taxpayer identification number")
   var federalTaxNumber: String? = null

) : ValueObjectBase<CompanyValueObject>() {

   constructor(entity: CompanyEntity) :
      this(
         id = entity.id,
         name = entity.name,
         doingBusinessAs = entity.doingBusinessAs,
         clientCode = entity.clientCode,
         clientId = entity.clientId,
         datasetCode = entity.datasetCode,
         federalTaxNumber = entity.federalTaxNumber
      )

   override fun myId(): Long? = id
   override fun copyMe(): CompanyValueObject = copy()
}
