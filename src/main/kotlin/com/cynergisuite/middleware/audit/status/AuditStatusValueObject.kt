package com.cynergisuite.middleware.audit.status

import com.cynergisuite.domain.ValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValueObject
@JsonInclude(NON_NULL)
@Schema(name = "AuditStatus", title = "Status definition associated", description = "Status definition associated with an audit action")
data class AuditStatusValueObject (

   @field:NotNull
   @field:Size(min = 3, max = 15)
   @field:Schema(name = "value", description = "This is a database driven with the original values being OPENED, IN-PROGRESS, COMPLETED, CANCELED and SIGNED-OFF")
   var value: String,

   @field:Size(min = 3, max = 50)
   @field:Schema(name = "description", description = "A localized description suitable for showing the user")
   var description: String? = null

) {

   constructor(entity: AuditStatus, localizedDescription: String) :
      this(
         value = entity.value,
         description = localizedDescription
      )
}
