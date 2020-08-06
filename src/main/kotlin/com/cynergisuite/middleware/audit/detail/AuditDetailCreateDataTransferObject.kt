package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.SimpleIdentifiableDataTransferObject
import com.cynergisuite.domain.SimpleIdentifiableValueObject
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaDTO
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Schema(name = "AuditDetailCreate", title = "Requirements for creating an Audit Detail", description = "Payload required to create an audit detail entity")
data class AuditDetailCreateDataTransferObject(

   @field:Valid
   @field:NotNull
   @field:Schema(name = "inventory", description = "Inventory item being associated with a new AuditDetail")
   val inventory: SimpleIdentifiableValueObject?,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "scanArea", description = "AuditScanAreaId where Inventory item was scanned")
   var scanArea: SimpleIdentifiableDataTransferObject?
)
