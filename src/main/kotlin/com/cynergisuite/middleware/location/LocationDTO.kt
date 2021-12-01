package com.cynergisuite.middleware.location

import com.cynergisuite.domain.LegacyIdentifiable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "Location", title = "A location where rental inventory is processed", description = "A location within Cynergi where rental items in inventory are managed.", requiredProperties = ["number"])
data class LocationDTO(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, nullable = true, description = "System generated ID")
   var id: Long? = 0,

   @field:Positive
   @field:NotNull
   @field:JsonProperty("locationNumber")
   @field:Schema(name = "number", minimum = "1", required = true, nullable = false, description = "Location number")
   var number: Int? = null,

   @field:NotNull
   @field:Schema(name = "name", required = false, nullable = true, description = "Human readable name for a location")
   var name: String? = null

) : LegacyIdentifiable {

   constructor(entity: LocationEntity) :
      this(
         id = entity.id,
         number = entity.number,
         name = entity.name
      )

   override fun myId(): Long? = id
}
