package com.cynergisuite.middleware.store

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.location.Location
import com.cynergisuite.middleware.region.RegionValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "Store", title = "A location where rental inventory is processed", description = "A location within Cynergi where rental items in inventory are managed.", requiredProperties = ["number"])
data class StoreValueObject(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, nullable = true, description = "System generated ID")
   var id: Long = 0,

   @field:Positive
   @field:NotNull
   @field:JsonProperty("storeNumber")
   @field:Schema(name = "storeNumber", minimum = "1", required = true, nullable = false, description = "Store number")
   var storeNumber: Int? = null,

   @field:NotNull
   @field:Schema(name = "name", required = false, nullable = true, description = "Human readable name for a store")
   var name: String? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "region", required = false, nullable = true, description = "Region that a store belong to")
   var region: RegionValueObject? = null

) : Identifiable {

   constructor(entity: StoreEntity) :
      this(
         id = entity.id,
         storeNumber = entity.number,
         name = entity.name,
         region = entity.region?.toValueObject()
      )

   constructor(location: Location) :
      this(
         id = location.myId(),
         storeNumber = location.myNumber(),
         name = location.myName()
      )

   override fun myId(): Long? = id
}
