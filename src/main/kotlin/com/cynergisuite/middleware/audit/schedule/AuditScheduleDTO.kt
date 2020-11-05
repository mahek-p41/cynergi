package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.store.StoreDTO
import io.swagger.v3.oas.annotations.media.Schema
import java.time.DayOfWeek
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@Schema(name = "AuditSchedule", title = "An audit schedule", description = "An audit schedule, the department that is supposed to do the audit at the desired stores")
data class AuditScheduleDTO(

   @field:Positive
   @field:Schema(name = "id", description = "System generated ID for the associated schedule")
   var id: Long? = null, // equates to Schedule.id

   @field:NotNull
   @field:Size(min = 3, max = 64)
   @field:Schema(name = "title", description = "Short title to describe the schedule to the user who is setting it up")
   var title: String? = null, // equates to Schedule.title

   @field:NotNull
   @field:Size(min = 3, max = 256)
   @field:Schema(name = "description", description = "Long description of the schedule the user entered")
   var description: String? = null, // equates to Schedule.description

   @field:NotNull
   @field:Schema(name = "schedule", description = "Day of week the audit needs to be scheduled for", allowableValues = ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"])
   var schedule: DayOfWeek? = null, // equates to Schedule.schedule

   @field:Valid
   @field:NotEmpty
   @field:Schema(name = "stores", description = "Set of stores the audit schedule is supposed to run against")
   var stores: List<StoreDTO> = mutableListOf(), // is from a schedule argument that is collected together

   @field:NotNull
   @field:Schema(name = "enabled", description = "Whether the audit is enabled or not")
   var enabled: Boolean? = true

) : Identifiable {
   override fun myId(): Long? = id
}
