package com.cynergisuite.middleware.schedule

import com.cynergisuite.domain.SimpleIdentifiableDTO
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Schema(name = "Schedule", description = "Describes the minimum requirements for creating a scheduled job in the system")
data class ScheduleCreateDataTransferObject(

   @field:Size(min = 2, max = 64)
   @field:NotNull
   @field:NotBlank
   @field:Schema(name = "title", description = "Human readable title that describes what the schedule will be doing")
   var title: String? = null,

   @field:Size(min = 2, max = 64)
   @field:Schema(name = "description", description = "Human readable description of what the schedule has been created for")
   var description: String? = null,

   @field:NotNull
   @field:NotBlank
   @field:Schema(name = "schedule", description = "Time slot when the schedule is supposed to execute")
   var schedule: String? = null,

   @field:Valid
   @field:NotNull
   var type: SimpleIdentifiableDTO? = null

)
