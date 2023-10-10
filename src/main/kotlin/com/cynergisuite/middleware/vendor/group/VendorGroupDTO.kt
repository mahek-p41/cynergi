package com.cynergisuite.middleware.vendor.group

import com.cynergisuite.domain.Identifiable
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@Schema(name = "VendorGroup", title = "Defines a vendor group", description = "Defines a single vendor grouping that can be assigned to vendors")
data class VendorGroupDTO(

   @field:Schema(name = "id", minimum = "1", required = false, description = "System generated ID")
   var id: UUID? = null,

   @field:NotNull
   @field:Size(min = 2, max = 10)
   @field:Schema(name = "value", minimum = "2", maximum = "10", description = "Describes the vendor group")
   var value: String? = null,

   @field:NotNull
   @field:Size(min = 2, max = 50)
   @field:Schema(name = "description", minimum = "2", maximum = "50", description = "Describes the vendor group")
   var description: String? = null

) : Identifiable {
   constructor(entity: VendorGroupEntity) :
      this(
         id = entity.id,
         value = entity.value,
         description = entity.description
      )

   override fun myId(): UUID? = id
}