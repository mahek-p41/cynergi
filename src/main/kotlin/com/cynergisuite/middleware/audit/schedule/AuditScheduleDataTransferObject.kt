package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.DataTransferObject
import com.cynergisuite.domain.ValueObjectBase
import com.cynergisuite.middleware.department.DepartmentValueObject
import com.cynergisuite.middleware.store.StoreValueObject
import java.time.DayOfWeek
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@DataTransferObject
data class AuditScheduleDataTransferObject(

   @field:Positive
   var id: Long? = null, // equates to Schedule.id

   @field:NotNull
   @field:Size(min = 3, max = 64)
   var title: String? = null, // equates to Schedule.title

   @field:NotNull
   @field:Size(min = 3, max = 256)
   var description: String? = null, // equates to Schedule.description

   @field:NotNull
   var schedule: DayOfWeek? = null, // equates to Schedule.schedule

   @field:Valid
   @field:NotEmpty
   var stores: List<StoreValueObject> = mutableListOf(), // is from a schedule argument that is collected together

   @field:Valid
   @field:NotNull
   var department: DepartmentValueObject? = null,

   @field:NotNull
   var enabled: Boolean? = true

) : ValueObjectBase<AuditScheduleDataTransferObject>() {
   override fun myId(): Long? = id
   override fun copyMe(): AuditScheduleDataTransferObject = copy()
}
