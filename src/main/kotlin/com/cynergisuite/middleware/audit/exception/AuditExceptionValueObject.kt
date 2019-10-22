package com.cynergisuite.middleware.audit.exception

import com.cynergisuite.domain.IdentifiableValueObject
import com.cynergisuite.domain.SimpleIdentifiableValueObject
import com.cynergisuite.domain.ValueObjectBase
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaValueObject
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNoteValueObject
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(
   name = "AuditException",
   title = "A single exception encountered during an audit",
   description = "A single item associated with an Exception encountered during an Audit.  It describes the problem as best can be determined by the data at hand.",
   requiredProperties = ["barcode", "exceptionCode", "audit", "signedOff"]
)
data class AuditExceptionValueObject (

   @field:Positive
   @field:Schema(name = "id", description = "System generated ID", example = "1")
   var id: Long? = null,

   @field:Schema(name = "timeCreated", description = "The time when this exception record was created")
   var timeCreated: OffsetDateTime? = null,

   @field:Schema(name = "timeUpdated", description = "The last time this exception record was created or changed")
   var timeUpdated: OffsetDateTime? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(name = "scanArea", description = "The optional location where the exception was encountered")
   var scanArea: AuditScanAreaValueObject? = null,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 2, max = 200)
   @field:Schema(name = "barcode", description = "The barcode associated with the item that was scanned", example = "00112164", minLength = 2, maxLength = 200)
   var barcode: String? = null,

   @field:NotBlank
   @field:Size(min = 2, max = 200)
   @field:Schema(name = "productCode", description = "Product code associated with the AuditException", minLength = 2, maxLength = 200)
   var productCode: String? = null,

   @field:NotBlank
   @field:Size(min = 2, max = 200)
   @field:Schema(name = "altId", description = "Alternate ID for Inventory item being associated with an AuditException", minLength = 2, maxLength = 200)
   var altId: String? = null,

   @field:Size(min = 2, max = 100)
   @field:Schema(name = "serialNumber", description = "The serial number associated with the item what was scanned or missing", example = "00112164", minLength = 2, maxLength = 100)
   var serialNumber: String? = null,

   @field:Size(min = 2, max = 100)
   @field:Schema(name = "serialNumber", description = "The serial number associated with the item what was scanned or missing", example = "00112164", minLength = 2, maxLength = 100)
   var inventoryBrand: String? = null,

   @field:Size(min = 2, max = 100)
   @field:Schema(name = "serialNumber", description = "The serial number associated with the item what was scanned or missing", example = "00112164", minLength = 2, maxLength = 100)
   var inventoryModel: String? = null,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 2, max = 100)
   @field:Schema(name = "exceptionCode", description = "The exception code that describes the problem", example = "Not found in inventory file", minLength = 2, maxLength = 100)
   var exceptionCode: String? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "scannedBy", description = "The Employee who is reporting the exception.  This is filled in by the system based on login credentials")
   var scannedBy: EmployeeValueObject? = null,  // this will be filled out by the system based on how they are logged in

   @field:NotNull
   @field:Schema(name = "signedOff", description = "Whether this exception has been signed off by the designated employee", example = "true", defaultValue = "false")
   var signedOff: Boolean = false,

   @field:Schema(name = "notes", description = "Listing of notes associated with an AuditException")
   var notes: MutableList<AuditExceptionNoteValueObject> = mutableListOf(),

   @field:Valid
   @field:Schema(name = "audit", description = "The Audit this exception is associated with", implementation = SimpleIdentifiableValueObject::class)
   var audit: IdentifiableValueObject? = null

) : ValueObjectBase<AuditExceptionValueObject>() {
   constructor(entity: AuditExceptionEntity, scanArea: AuditScanAreaValueObject?) :
      this(
         id = entity.id,
         timeCreated = entity.timeCreated,
         timeUpdated = entity.timeUpdated,
         scanArea = scanArea,
         barcode = entity.barcode,
         productCode = entity.productCode,
         altId = entity.altId,
         serialNumber = entity.serialNumber,
         inventoryBrand = entity.inventoryBrand,
         inventoryModel = entity.inventoryModel,
         scannedBy = EmployeeValueObject(entity.scannedBy),
         exceptionCode = entity.exceptionCode,
         signedOff = entity.signedOff,
         notes = entity.notes.asSequence().map { AuditExceptionNoteValueObject(it) }.toMutableList(),
         audit = SimpleIdentifiableValueObject(entity.audit)
      )

   override fun valueObjectId(): Long? = id
   override fun copyMe(): AuditExceptionValueObject = copy()
}
