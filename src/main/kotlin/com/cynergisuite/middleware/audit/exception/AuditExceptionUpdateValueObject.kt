package com.cynergisuite.middleware.audit.exception

import com.cynergisuite.domain.ValueObjectBase
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNoteValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
   name = "AuditExceptionUpdate",
   title = "An update to an existing AuditException",
   description = "Associate an additional note to an AuditException.  Allows for communication about why an AuditException might exist."
)
data class AuditExceptionUpdateValueObject(

   @field:Positive
   @field:NotNull
   @field:Schema(name = "id", description = "System generated ID of AuditException that is being updated", example = "1")
   var id: Long? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "note", description = "Note to be added to the AuditException")
   var note: AuditExceptionNoteValueObject? = null

)  : ValueObjectBase<AuditExceptionUpdateValueObject>() {
   override fun myId(): Long? = id
   override fun copyMe(): AuditExceptionUpdateValueObject = copy()
}
