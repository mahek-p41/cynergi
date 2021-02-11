package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaEntity
import com.cynergisuite.middleware.store.StoreValueObject
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Schema(name = "AuditDetailCreate", title = "Requirements for creating an Audit Detail", description = "Payload required to create an audit detail entity")
data class AuditDetailCreateUpdateDTO(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", description = "System generated ID")
   var id: Long? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "inventory", description = "Inventory item being associated with a new AuditDetail")
   val inventory: SimpleIdentifiableDTO?,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "scanArea", description = "AuditScanAreaId where Inventory item was scanned")
   var scanArea: SimpleIdentifiableDTO?
): Identifiable {
   override fun myId(): Long? = this.id

   constructor(inventory: SimpleIdentifiableDTO?, scanArea: SimpleIdentifiableDTO?) :
      this(
         id = null,
         inventory = inventory,
         scanArea = scanArea
      )
}
