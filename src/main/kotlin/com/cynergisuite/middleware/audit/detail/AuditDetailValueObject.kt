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
@Schema(name = "AuditDetail", description = "A single item associated with an Audit")
data class AuditDetailValueObject (

   @field:Positive
   var id: Long?,

   @field:NotNull
   @field:Schema(required = true)
   var scanArea: AuditScanAreaValueObject?,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 1, max = 200)
   var barCode: String?,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 3, max = 100)
   var inventoryId: String?,

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
   @field:NotBlank
   @field:Size(min = 3, max = 100)
   var inventoryStatus: String?,

   @field:NotNull
   var audit: IdentifiableValueObject?

) : ValueObjectBase<AuditDetailValueObject>() {

   constructor(entity: AuditDetail, auditScanAreaValueObject: AuditScanAreaValueObject) :
      this(
         entity = entity,
         audit = SimpleIdentifiableValueObject(entity.audit),
         auditScanAreaValueObject = auditScanAreaValueObject
      )

   constructor(entity: AuditDetail, audit: IdentifiableValueObject? = null, auditScanArea: AuditScanArea) :
      this(
         entity = entity,
         audit = audit,
         auditScanAreaValueObject = AuditScanAreaValueObject(auditScanArea)
      )

   constructor(entity: AuditDetail, audit: IdentifiableValueObject? = null, auditScanAreaValueObject: AuditScanAreaValueObject) :
      this(
         id = entity.id,
         scanArea = auditScanAreaValueObject,
         barCode = entity.barCode,
         inventoryId = entity.inventoryId,
         inventoryBrand = entity.inventoryBrand,
         inventoryModel = entity.inventoryModel,
         scannedBy = EmployeeValueObject(entity.scannedBy),
         inventoryStatus = entity.inventoryStatus,
         audit = audit
      )

   override fun valueObjectId(): Long? = id
   override fun copyMe(): AuditDetailValueObject = copy()
}
