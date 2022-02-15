package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaDTO
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "AuditDetail", title = "Single item associated with an Audit", description = "Single line item that has been successfully found during the audit process")
data class AuditDetailValueObject(

   var id: UUID? = null,

   @field:NotNull
   @field:Schema(required = true)
   var scanArea: AuditScanAreaDTO? = null,

   @field:Size(min = 2, max = 200)
   var lookupKey: String? = null,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 1, max = 200)
   var barcode: String? = null,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 3, max = 100)
   var serialNumber: String? = null,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 3, max = 100)
   var productCode: String? = null,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 3, max = 100)
   var altId: String? = null,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 3, max = 100)
   var inventoryBrand: String? = null,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 3, max = 100)
   var inventoryModel: String? = null,

   @field:NotNull
   var scannedBy: EmployeeValueObject? = null,

   @field:NotNull
   var audit: Identifiable? = null

) : Identifiable {

   constructor(entity: AuditDetailEntity, auditScanArea: AuditScanAreaDTO) :
      this(
         entity = entity,
         audit = SimpleIdentifiableDTO(entity.audit),
         auditScanArea = auditScanArea
      )

   constructor(entity: AuditDetailEntity, audit: Identifiable? = null, auditScanArea: AuditScanAreaDTO) :
      this(
         id = entity.id,
         scanArea = auditScanArea,
         lookupKey = entity.lookupKey,
         barcode = entity.barcode,
         productCode = entity.productCode,
         altId = entity.altId,
         serialNumber = entity.serialNumber,
         inventoryBrand = entity.inventoryBrand,
         inventoryModel = entity.inventoryModel,
         scannedBy = EmployeeValueObject(entity.scannedBy),
         audit = audit
      )

   override fun myId(): UUID? = id
}
