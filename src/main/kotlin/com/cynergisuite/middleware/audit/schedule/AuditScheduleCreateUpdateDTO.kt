package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.location.Location
import io.swagger.v3.oas.annotations.media.Schema
import java.time.DayOfWeek
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@Schema(name = "AuditScheduleCreateUpdate", title = "Requirements for creating an audit schedule", description = "Payload for creating a schedule for an audit associated with what stores and which department is supposed to do the audit")
data class AuditScheduleCreateUpdateDTO(

   @field:Positive
   @field:Schema(name = "id", description = "System generated ID for the associated schedule")
   var id: Long? = null,

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
   var stores: Set<SimpleIdentifiableDTO> = mutableSetOf(), // is from a schedule argument that is collected together

   @field:NotNull
   @field:Schema(name = "enabled", description = "Whether the audit is enabled or not")
   var enabled: Boolean? = null

) {
   constructor(title: String, description: String, schedule: DayOfWeek, stores: Set<Location>) :
      this(
         title = title,
         description = description,
         schedule = schedule,
         stores = stores.asSequence().map { SimpleIdentifiableDTO(it.myId()) }.toSet(),
         enabled = true
      )

   constructor(title: String, description: String, schedule: DayOfWeek, stores: Set<Location>, enabled: Boolean = true) :
      this(
         title = title,
         description = description,
         schedule = schedule,
         stores = stores.asSequence().map { SimpleIdentifiableDTO(it.myId()) }.toSet(),
         enabled = enabled
      )

   constructor(id: Long, title: String, description: String, schedule: DayOfWeek, stores: Set<Location>) :
      this(
         id = id,
         title = title,
         description = description,
         schedule = schedule,
         stores = stores.asSequence().map { SimpleIdentifiableDTO(it.myId()) }.toSet(),
         enabled = true
      )
}
