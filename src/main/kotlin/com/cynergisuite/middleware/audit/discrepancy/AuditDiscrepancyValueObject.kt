package com.cynergisuite.middleware.audit.discrepancy

import com.cynergisuite.domain.IdentifiableValueObject
import com.cynergisuite.domain.SimpleIdentifiableValueObject
import com.cynergisuite.domain.ValueObjectBase
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
data class AuditDiscrepancyValueObject (

   @field:Positive
   var id: Long? = null,

   @field:NotNull
   @field:NotBlank
   var barCode: String? = null,

   @field:NotNull
   @field:NotBlank
   var inventoryId: String? = null,

   @field:NotNull
   @field:NotBlank
   var inventoryBrand: String? = null,

   @field:NotNull
   @field:NotBlank
   var inventoryModel: String? = null,

   @field:NotNull
   var scannedBy: EmployeeValueObject? = null,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 2, max = 500)
   var notes: String? = null,

   @field:NotNull
   var audit: IdentifiableValueObject? = null

) : ValueObjectBase<AuditDiscrepancyValueObject>() {

   constructor(entity: AuditDiscrepancy) :
      this(
         id = entity.id,
         barCode = entity.barCode,
         inventoryId = entity.inventoryId,
         inventoryBrand = entity.inventoryBrand,
         inventoryModel = entity.inventoryModel,
         scannedBy = EmployeeValueObject(entity.scannedBy),
         notes = entity.notes,
         audit = SimpleIdentifiableValueObject(entity.audit)
      )

   override fun valueObjectId(): Long? = id
   override fun copyMe(): AuditDiscrepancyValueObject = copy()
}
