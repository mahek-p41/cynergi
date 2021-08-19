package com.cynergisuite.middleware.audit.exception

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNoteValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
   name = "AuditExceptionUpdate",
   title = "An update to an existing AuditException",
   description = "Associate an additional note to an AuditException.  Allows for communication about why an AuditException might exist."
)
data class AuditExceptionUpdateValueObject(

   @field:NotNull
   @field:Schema(name = "id", description = "System generated ID of AuditException that is being updated", example = "1")
   var id: UUID? = null,

   @field:Valid
   @field:Schema(name = "note", description = "Note to be added to the AuditException")
   var note: AuditExceptionNoteValueObject? = null,

   @field:Schema(name = "approved", description = "AuditException boolean for approved")
   var approved: Boolean? = null

) : Identifiable {
   override fun myId(): UUID? = id
}
