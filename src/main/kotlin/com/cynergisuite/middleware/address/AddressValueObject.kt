package com.cynergisuite.middleware.address

import com.cynergisuite.domain.Identifiable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "AddressValueObject", title = "An entity containing a address information", description = "An entity containing a address information.")
data class AddressValueObject (

   @field:Positive
   var id: Long? = null,

   @field:Positive
   var number: Int? = null,

   @field:NotNull
   @field:Schema(name = "name", description = "Human readable name for a bank.")
   var name: String,

   @field:NotNull
   @field:Schema(name = "address1", description = "Human readable address1 for a bank.")
   var address1: String,

   @field:NotNull
   @field:Schema(name = "address2", description = "Human readable address2 for a bank.")
   var address2: String,

   @field:NotNull
   @field:Schema(name = "city", description = "City of a address.")
   var city: String,

   @field:NotNull
   @field:Schema(name = "state", description = "State of a address.")
   var state: String,

   @field:NotNull
   @field:Schema(name = "postalCode", description = "Postal code of a address.")
   var postalCode: String,

   @field:NotNull
   @field:Schema(name = "latitude", description = "Latitude of a address.")
   var latitude: Double,

   @field:NotNull
   @field:Schema(name = "longitude", description = "Longitude of a address.")
   var longitude: Double,

   @field:NotNull
   @field:Schema(name = "country", description = "Country of a address.")
   var country: String,

   @field:NotNull
   @field:Schema(name = "county", description = "County of a address.")
   var county: String

   ) : Identifiable {
   constructor(address: AddressEntity) : this(
      id = address.id,
      number = address.number,
      name = address.name,
      address1 = address.address1,
      address2 = address.address2,
      city = address.city,
      state = address.state,
      postalCode = address.postalCode,
      latitude = address.latitude,
      longitude = address.longitude,
      country = address.country,
      county = address.county
   )

   override fun myId(): Long? = id
}
