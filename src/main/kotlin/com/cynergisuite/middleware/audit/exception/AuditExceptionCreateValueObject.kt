package com.cynergisuite.middleware.audit.exception

import com.cynergisuite.domain.SimpleIdentifiableDataTransferObject
import com.cynergisuite.domain.SimpleIdentifiableValueObject
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Schema(name = "AuditExceptionCreate", title = "Create an AuditException", description = "Payload required to create an AuditException entity")
data class AuditExceptionCreateValueObject(

   @field:Valid
   @field:Schema(name = "inventory", description = "If this field is not provided then barcode needs to be. Inventory item being associated with a new AuditException.  This indicates that the item was on the list but unable to be found in the store.")
   var inventory: SimpleIdentifiableValueObject? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(name = "scanArea", description = "The optional location where the exception was encountered")
   var scanArea: SimpleIdentifiableDataTransferObject? = null,

   @field:Size(min = 2, max = 100)
   @field:Schema(name = "barcode", description = "If the inventory field is provided inventory must be null.  Holds the scanned barcode that an Inventory item did not exist for.")
   var barcode: String? = null,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 2, max = 100)
   @field:Schema(name = "exceptionCode", description = "The exception code that describes the problem", example = "Not found in inventory file", minLength = 2, maxLength = 100, required = true, nullable = false)
   var exceptionCode: String? = null

)
