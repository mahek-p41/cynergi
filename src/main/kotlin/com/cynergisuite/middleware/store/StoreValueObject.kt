package com.cynergisuite.middleware.store

import com.cynergisuite.domain.ValueObjectBase
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "Store", title = "A location where rental inventory is processed", description = "A location within Cynergi where rental items in inventory are managed.", requiredProperties = ["number"])
data class StoreValueObject (

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, nullable = true, description = "System generated ID")
   var id: Long = 0,

   @field:NotNull
   @field:JsonProperty("storeNumber")
   @field:Schema(name = "number", minimum = "1", required = true, nullable = false, description = "Store number")
   var number: Int? = null,

   @field:Schema(name = "name", minimum = "1", required = false, nullable = true, description = "Human readable name for a store")
   var name: String? = null,

   @field:Schema(name = "dataset", minimum = "1", required = false, nullable = true, description = "Dataset that this store belongs to")
   var dataset: String? = null

) : ValueObjectBase<StoreValueObject>() {

   constructor(entity: StoreEntity) :
      this(
         id = entity.id,
         number = entity.number,
         name = entity.name,
         dataset = entity.dataset
      )

   override fun valueObjectId(): Long? = id
   override fun copyMe(): StoreValueObject = copy()
}
