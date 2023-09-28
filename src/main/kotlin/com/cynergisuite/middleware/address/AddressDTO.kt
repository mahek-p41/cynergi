package com.cynergisuite.middleware.address

import com.cynergisuite.domain.Identifiable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "Address", title = "An entity containing address information", description = "An entity containing address information.")
data class AddressDTO(

   var id: UUID? = null,

   @field:NotNull
   @field:Size(min = 2, max = 30)
   @field:Schema(name = "name", description = "Human readable name for this address.")
   var name: String? = null,

   @field:NotNull
   @field:Size(min = 2, max = 30)
   @field:Schema(name = "address1", description = "Human readable address1.")
   var address1: String? = null,

   @field:Size(min = 2, max = 30)
   @field:Schema(name = "address2", description = "Human readable address2.")
   var address2: String? = null,

   @field:NotNull
   @field:Size(min = 2, max = 30)
   @field:Schema(name = "city", description = "City of a address.")
   var city: String? = null,

   @field:NotNull
   @field:Size(min = 2, max = 2)
   @field:Schema(name = "state", description = "State of a address.")
   var state: String? = null,

   @field:NotNull
   @field:Size(min = 3, max = 10)
   @field:Schema(name = "postalCode", description = "Postal code of a address.")
   var postalCode: String? = null,

   @field:Schema(name = "latitude", description = "Latitude of a address.")
   var latitude: Double? = null,

   @field:Schema(name = "longitude", description = "Longitude of a address.")
   var longitude: Double? = null,

   @field:NotNull
   @field:Size(min = 2, max = 50)
   @field:Schema(name = "country", description = "Country of a address.")
   var country: String? = null,

   @field:Size(min = 2, max = 50)
   @field:Schema(name = "county", description = "County of a address.")
   var county: String? = null,

   @field:Size(min = 3, max = 21)
   @field:Schema(name = "phone", description = "Phone of a address.")
   var phone: String? = null,

   @field:Size(min = 3, max = 21)
   @field:Schema(name = "county", description = "Fax of a address.")
   var fax: String? = null

) : Identifiable {

   constructor(address: AddressEntity) : this(
      id = address.id,
      name = address.name,
      address1 = address.address1,
      address2 = address.address2,
      city = address.city,
      state = address.state,
      postalCode = address.postalCode,
      latitude = address.latitude,
      longitude = address.longitude,
      country = address.country,
      county = address.county,
      phone = address.phone,
      fax = address.fax
   )

   override fun myId(): UUID? = id
}
