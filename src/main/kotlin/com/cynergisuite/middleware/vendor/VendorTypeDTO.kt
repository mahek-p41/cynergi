package com.cynergisuite.middleware.vendor

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "VendorType", title = "Vendor 1099 type", description = "Currencies that the banks support")
data class VendorTypeDTO (

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Vendor 1099 code")
   var value: Int? = null,

   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for 1099 Field")
   var description: String? = null

   ) {

      constructor(vendorType: VendorType) :
      this(
         value = vendorType.value,
         description = vendorType.description
      )

      constructor(vendorType: VendorType, localizedDescription: String) :
      this(
         value = vendorType.value,
         description = localizedDescription
      )
}
