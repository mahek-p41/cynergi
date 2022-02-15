package com.cynergisuite.middleware.audit.exception

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaDTO
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNoteValueObject
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@JsonInclude(NON_NULL)
@Schema(
   name = "AuditException",
   title = "A single exception encountered during an audit",
   description = "A single item associated with an Exception encountered during an Audit.  It describes the problem as best can be determined by the data at hand.",
   requiredProperties = ["barcode", "exceptionCode", "audit", "approved"]
)
data class AuditExceptionDTO(

   @field:Schema(name = "id", description = "System generated ID", example = "1")
   var id: UUID? = null,

   @field:Schema(name = "timeCreated", description = "The time when this exception record was created")
   var timeCreated: OffsetDateTime? = null,

   @field:Schema(name = "timeUpdated", description = "The last time this exception record was created or changed")
   var timeUpdated: OffsetDateTime? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(name = "scanArea", description = "The optional location where the exception was encountered")
   var scanArea: AuditScanAreaDTO? = null,

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
   var scannedBy: EmployeeValueObject? = null, // this will be filled out by the system based on how they are logged in

   @field:NotNull
   @field:Schema(name = "approved", description = "Whether this exception has been approved by the designated employee", example = "true", defaultValue = "false")
   var approved: Boolean = false,

   @field:Valid
   @field:Schema(name = "approvedBy", description = "The Employee who approved the exception.  This is filled in by the system based on login credentials")
   var approvedBy: EmployeeValueObject? = null, // this will be filled out by the system based on how they are logged in

   @field:Size(min = 2, max = 200)
   @field:Schema(name = "lookupKey", description = "The key that can be used to determine what inventory entry lines up with this exception")
   var lookupKey: String? = null,

   @field:Schema(name = "notes", description = "Listing of notes associated with an AuditException")
   var notes: MutableList<AuditExceptionNoteValueObject> = mutableListOf(),

   @field:Valid
   @field:Schema(name = "audit", description = "The Audit this exception is associated with", implementation = SimpleIdentifiableDTO::class)
   var audit: Identifiable? = null

) : Identifiable {
   constructor(entity: AuditExceptionEntity, scanArea: AuditScanAreaDTO?) :
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
         approved = entity.approved,
         approvedBy = entity.approvedBy?.let { EmployeeValueObject(it) },
         lookupKey = entity.lookupKey,
         notes = entity.notes.asSequence().map { AuditExceptionNoteValueObject(it) }.toMutableList(),
         audit = SimpleIdentifiableDTO(entity.audit)
      )

   override fun myId(): UUID? = id
}
