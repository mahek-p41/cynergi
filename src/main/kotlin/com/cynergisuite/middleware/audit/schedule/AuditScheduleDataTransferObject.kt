package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.ValueObject
import com.cynergisuite.domain.ValueObjectBase
import com.cynergisuite.middleware.department.DepartmentValueObject
import com.cynergisuite.middleware.schedule.type.ScheduleTypeValueObject
import com.cynergisuite.middleware.store.StoreValueObject
import java.time.DayOfWeek
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@ValueObject
data class AuditScheduleDataTransferObject(

   @field:Positive
   val id: Long? = null, // equates to Schedule.id

   @field:NotNull
   @field:Size(min = 3, max = 64)
   val title: String? = null, // equates to Schedule.title

   @field:NotNull
   @field:Size(min = 3, max = 256)
   val description: String? = null, // equates to Schedule.description

   @field:NotNull
   val schedule: DayOfWeek? = null, // equates to Schedule.schedule

   @field:Valid
   @field:NotEmpty
   val stores: List<StoreValueObject> = mutableListOf(), // is from a schedule argument that is collected together

   @field:Valid
   @field:NotNull
   val departmentAccess: DepartmentValueObject? = null,

   @field:Valid
   @field:NotNull
   val type: ScheduleTypeValueObject? = null

) : ValueObjectBase<AuditScheduleDataTransferObject>() {
   override fun myId(): Long? = id
   override fun copyMe(): AuditScheduleDataTransferObject = copy()
}
