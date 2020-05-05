package com.cynergisuite.middleware.vendor.freight.onboard

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "FreightOnboardType", title = "Freight Onboard Type", description = "Freight Onboard Type")
data class FreightOnboardTypeValueObject (

   @field:NotNull
   @field:Size(min = 3, max = 15)
   @field:Schema(description = "Freight Onboard Type")
   var value: String? = null,

   @field:Nullable
   @field:Size(min = 3, max = 50)
   @field:Schema(description = "Freight Onboard Description")
   var description: String? = null

) {

   constructor(currencyType: FreightOnboardTypeEntity) :
      this(
         value = currencyType.value,
         description = currencyType.description
      )

   constructor(currencyType: FreightOnboardTypeEntity, localizedDescription: String) :
      this(
         value = currencyType.value,
         description = localizedDescription
      )
}
