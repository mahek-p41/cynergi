package com.cynergisuite.middleware.audit.exception.note

import com.cynergisuite.domain.ValueObjectBase
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(
   name = "AuditExceptionNote",
   title = "A note associated with an AuditException",
   description = "A single note associated with an AuditException encountered during an Audit.  Allows for communication about why an AuditException might exist."
)
data class AuditExceptionNoteValueObject(

   @field:Positive
   @field:Schema(name = "id", description = "System generated ID", example = "1")
   var id: Long? = null,

   @field:Schema(name = "timeCreated", description = "The time when the note was created")
   var timeCreated: OffsetDateTime? = null,

   @field:Schema(name = "timeUpdated", description = "The last time the note was created or changed")
   var timeUpdated: OffsetDateTime? = null,

   @field:NotNull
   @field:Size(min = 5, max = 200)
   @field:Schema(name = "note", description = "One of possibly many notes to be associated with an AuditException")
   var note: String? = null

) : ValueObjectBase<AuditExceptionNoteValueObject>() {

   constructor(entity: AuditExceptionNote) :
      this(
         id = entity.id,
         note = entity.note
      )

   override fun valueObjectId(): Long? = id;
   override fun copyMe(): AuditExceptionNoteValueObject = copy()
}
