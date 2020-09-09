package com.cynergisuite.middleware.location

import com.cynergisuite.domain.Identifiable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

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

) : Identifiable {

   constructor(entity: LocationEntity) :
      this(
         id = entity.id,
         number = entity.number,
         name = entity.name
      )

   constructor(location: Location) :
      this(
         id = location.myId(),
         number = location.myNumber(),
         name = location.myName()
      )

   override fun myId(): Long? = id
}
