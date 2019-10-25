package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.DataTransferObject
import com.cynergisuite.domain.SimpleIdentifiableDataTransferObject
import java.time.DayOfWeek
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@DataTransferObject
data class AuditScheduleCreateDataTransferObject(

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
   val stores: List<SimpleIdentifiableDataTransferObject> = mutableListOf(), // is from a schedule argument that is collected together

   @field:Valid
   @field:NotNull
   val departmentAccess: SimpleIdentifiableDataTransferObject? = null

)
