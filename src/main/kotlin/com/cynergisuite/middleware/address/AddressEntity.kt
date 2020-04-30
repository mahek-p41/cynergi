package com.cynergisuite.middleware.address

import com.cynergisuite.domain.Identifiable
import java.time.OffsetDateTime

data class AddressEntity(
   val id: Long? = null,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val number: Int? = null,
   val name: String,
   val address1: String,
   val address2: String,
   val city: String,
   val state: String,
   val postalCode: String,
   val latitude: Double,
   val longitude: Double,
   val country: String,
   val county: String,
   val phone: String,
   val fax: String
) : Identifiable {

   constructor(address: AddressValueObject) :
      this (
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
         county = address.county,
         phone = address.phone,
         fax = address.fax
      )

   override fun myId(): Long? = id
}
