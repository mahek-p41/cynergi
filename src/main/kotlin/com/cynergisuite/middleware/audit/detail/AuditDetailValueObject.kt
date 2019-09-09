package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.IdentifiableValueObject
import com.cynergisuite.domain.SimpleIdentifiableValueObject
import com.cynergisuite.domain.ValueObjectBase
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanArea
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaValueObject
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "AuditDetail", title = "Single item associated with an Audit", description = "Single line item that has been successfully found during the audit process")
data class AuditDetailValueObject (

   @field:Positive
   var id: Long?,

   @field:NotNull
   @field:Schema(required = true)
   var scanArea: AuditScanAreaValueObject?,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 1, max = 200)
   var barcode: String?,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 3, max = 100)
   var serialNumber: String?,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 3, max = 100)
   var productCode: String?,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 3, max = 100)
   var altId: String?,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 3, max = 100)
   var inventoryBrand: String?,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 3, max = 100)
   var inventoryModel: String?,

   @field:NotNull
   var scannedBy: EmployeeValueObject?,

   @field:NotNull
   var audit: IdentifiableValueObject?

) : ValueObjectBase<AuditDetailValueObject>() {

   constructor(entity: AuditDetail, auditScanArea: AuditScanAreaValueObject) :
      this(
         entity = entity,
         audit = SimpleIdentifiableValueObject(entity.audit),
         auditScanArea = auditScanArea
      )

   constructor(entity: AuditDetail, audit: IdentifiableValueObject? = null, auditScanArea: AuditScanArea) :
      this(
         entity = entity,
         audit = audit,
         auditScanArea = AuditScanAreaValueObject(auditScanArea)
      )

   constructor(entity: AuditDetail, audit: IdentifiableValueObject? = null, auditScanArea: AuditScanAreaValueObject) :
      this(
         id = entity.id,
         scanArea = auditScanArea,
         barcode = entity.barcode,
         productCode = entity.productCode,
         altId = entity.altId,
         serialNumber = entity.serialNumber,
         inventoryBrand = entity.inventoryBrand,
         inventoryModel = entity.inventoryModel,
         scannedBy = EmployeeValueObject(entity.scannedBy),
         audit = audit
      )

   override fun valueObjectId(): Long? = id
   override fun copyMe(): AuditDetailValueObject = copy()
}
