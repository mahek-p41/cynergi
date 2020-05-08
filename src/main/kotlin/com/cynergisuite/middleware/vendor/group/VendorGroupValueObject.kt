package com.cynergisuite.middleware.vendor.group

import com.cynergisuite.domain.Identifiable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
data class VendorGroupValueObject(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, description = "System generated ID")
   var id: Long? = null,

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(name = "value", minimum = "1", maximum = "10", description = "Describes the vendor group")
   var value: String? = null,

   @field:NotNull
   @field:Size(min = 1, max = 50)
   @field:Schema(name = "description", minimum = "1", maximum = "50", description = "Describes the vendor group")
   var description: String? = null

) : Identifiable {

   constructor(entity: VendorGroupEntity) :
      this(
         id = entity.id,
         value = entity.value,
         description = entity.description
      )

   override fun myId(): Long? = id
}
