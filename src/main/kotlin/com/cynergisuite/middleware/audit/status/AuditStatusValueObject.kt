package com.cynergisuite.middleware.audit.status

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "AuditStatus", title = "Status definition associated", description = "Status definition associated with an audit action")
data class AuditStatusValueObject(

   @field:Positive
   @field:Schema(name = "id", description = "This is a database driven primary key value defining the id of the status")
   var id: Int? = null,

   @field:NotNull
   @field:Size(min = 3, max = 15)
   @field:Schema(name = "value", description = "This is a database driven with the original values being CREATED, IN-PROGRESS, COMPLETED, CANCELED and APPROVED")
   var value: String? = null,

   @field:Size(min = 3, max = 50)
   @field:Schema(name = "description", description = "A localized description suitable for showing the user")
   var description: String? = null,

   @field:Size(min = 6, max = 6)
   @field:Schema(name = "color", description = "A hex color code describing what color to be used when displaying this status")
   var color: String? = null

) {
   constructor(entity: AuditStatus) :
      this(
         entity = entity,
         localizedDescription = entity.description
      )

   constructor(entity: AuditStatus, localizedDescription: String) :
      this(
         id = entity.id,
         value = entity.value,
         description = localizedDescription,
         color = entity.color
      )
}
