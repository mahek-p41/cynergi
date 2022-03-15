package com.cynergisuite.middleware.schedule.command

import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

data class ScheduleCommandTypeDTO(

   @field:Positive
   var id: Int? = null,

   @field:NotNull
   var value: String? = null,

   @field:NotNull
   var description: String? = null

)
