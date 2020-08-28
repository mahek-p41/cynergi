package com.cynergisuite.middleware.audit.detail.scan.area

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.store.StoreValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "AuditScanAreaEntity", title = "Area where an item was scanned", description = "Possible location within a store where an item was scanned as part of an audit")
data class AuditScanAreaDTO(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", description = "System generated ID")
   var id: Long? = null,

   @field:NotNull
   @field:Schema(description = "Scan area name")
   var name: String? = null,

   @field:NotNull
   @field:Schema(description = "A store that scan area belong to")
   var store: StoreValueObject? = null

) : Identifiable {
   override fun myId(): Long? = this.id

   constructor(entity: AuditScanAreaEntity) :
      this(
         id = entity.myId(),
         name = entity.name,
         store = entity.store?.let { StoreValueObject(it) }
      )
}
