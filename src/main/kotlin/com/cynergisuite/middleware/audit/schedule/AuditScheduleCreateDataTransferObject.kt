package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.DataTransferObject
import com.cynergisuite.domain.SimpleIdentifiableDataTransferObject
import io.swagger.v3.oas.annotations.media.Schema
import java.time.DayOfWeek
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@DataTransferObject
@Schema(name = "AuditScheduleCreate", description = "Payload for creating a schedule for an audit associated with what stores and which department is supposed to do the audit")
data class AuditScheduleCreateDataTransferObject(

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
   val department: SimpleIdentifiableDataTransferObject? = null

)
