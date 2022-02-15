package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Introspected
@Schema(name = "AuditDetailCreate", title = "Requirements for creating an Audit Detail", description = "Payload required to create an audit detail entity")
data class AuditDetailCreateUpdateDTO(

   @field:Schema(name = "id", minimum = "1", description = "System generated ID")
   var id: UUID? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "inventory", description = "Inventory item being associated with a new AuditDetail")
   val inventory: SimpleLegacyIdentifiableDTO?,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "scanArea", description = "AuditScanAreaId where Inventory item was scanned")
   var scanArea: SimpleIdentifiableDTO?

) : Identifiable {
   constructor(inventory: SimpleLegacyIdentifiableDTO?, scanArea: SimpleIdentifiableDTO?) :
      this(
         id = null,
         inventory = inventory,
         scanArea = scanArea
      )

   override fun myId(): UUID? = this.id
}
