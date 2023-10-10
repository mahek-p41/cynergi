package com.cynergisuite.middleware.address

import com.cynergisuite.domain.Identifiable
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.util.UUID

@MappedEntity("address")
data class AddressEntity(

   @field:Id
   @field:GeneratedValue
   val id: UUID? = null,
   val name: String,
   val address1: String,
   val address2: String?,
   val city: String,
   val state: String,
   val postalCode: String,
   val latitude: Double?,
   val longitude: Double?,
   val country: String,
   val county: String?,
   val phone: String?,
   val fax: String?
) : Identifiable {

   constructor(address: AddressDTO) :
      this (
         id = address.id,
         name = address.name!!,
         address1 = address.address1!!,
         address2 = address.address2,
         city = address.city!!,
         state = address.state!!,
         postalCode = address.postalCode!!,
         latitude = address.latitude,
         longitude = address.longitude,
         country = address.country!!,
         county = address.county,
         phone = address.phone,
         fax = address.fax
      )

   override fun myId(): UUID? = id
}