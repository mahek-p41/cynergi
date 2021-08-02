package com.cynergisuite.middleware.audit.detail.scan.area

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.store.StoreDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "AuditScanAreaEntity", title = "Area where an item was scanned", description = "Possible location within a store where an item was scanned as part of an audit")
data class AuditScanAreaDTO(

   @field:Schema(name = "id", minimum = "1", description = "System generated ID")
   var id: UUID? = null,

   @field:NotNull
   @field:Size(min = 3, max = 50)
   @field:Schema(description = "Scan area name")
   var name: String? = null,

   @field:Nullable
   @field:Size(min = 3, max = 50)
   @field:Schema(description = "This is a database driven with the original values being SHOWROOM, STOREROOM and WAREHOUSE")
   var value: String? = null,

   @field:Nullable
   @field:Size(min = 3, max = 50)
   @field:Schema(description = "A localized description suitable for showing the user")
   var description: String? = null,

   @field:NotNull
   @field:Schema(description = "A store that scan area belong to")
   var store: StoreDTO? = null

) : Identifiable {
   override fun myId(): UUID? = id

   constructor(entity: AuditScanAreaEntity) :
      this(
         id = entity.myId(),
         name = entity.name,
         value = entity.name,
         description = entity.name,
         store = entity.store?.let { StoreDTO(it) }
      )
}
