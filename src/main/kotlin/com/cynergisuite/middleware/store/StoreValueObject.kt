package com.cynergisuite.middleware.store

import com.cynergisuite.domain.ValueObjectBase
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "Store", description = "A store", requiredProperties = ["number"])
data class StoreValueObject (

   @field:Nullable
   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, description = "System generated ID")
   var id: Long? = null,

   @field:NotNull
   @field:JsonProperty("storeNumber")
   @field:Schema(name = "number", minimum = "1", required = true, description = "Store number")
   var number: Int? = null,

   @field:Nullable
   @field:Schema(name = "name", minimum = "1", required = false, description = "Human readable name for a store")
   var name: String? = null,

   @field:Nullable
   @field:Schema(name = "dataset", minimum = "1", required = false, description = "Dataset that this store belongs to")
   var dataset: String? = null

) : ValueObjectBase<StoreValueObject>() {

   constructor(entity: Store) :
      this(
         id = entity.id,
         number = entity.number,
         name = entity.name,
         dataset = entity.dataset
      )

   override fun valueObjectId(): Long? = id
   override fun copyMe(): StoreValueObject = copy()
}
