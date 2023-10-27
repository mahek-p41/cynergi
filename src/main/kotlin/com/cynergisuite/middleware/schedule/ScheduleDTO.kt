package com.cynergisuite.middleware.schedule

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentDTO
import com.cynergisuite.middleware.schedule.command.ScheduleCommandTypeDTO
import com.cynergisuite.middleware.schedule.type.ScheduleTypeValueObject
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@Schema(name = "Schedule", description = "A user managed scheduled job in the system")
data class ScheduleDTO(

   @field:Positive
   var id: UUID? = null,

   @field:NotNull
   @field:Size(min = 3, max = 64)
   var title: String? = null,

   @field:NotNull
   @field:Size(min = 3, max = 256)
   var description: String?,

   @field:NotNull
   @field:Size(min = 5, max = 592)
   var schedule: String,

   @field:NotNull
   @field:Size(min = 5, max = 1024)
   var command: ScheduleCommandTypeDTO,

   @field:NotNull
   @field:Valid
   var type: ScheduleTypeValueObject,

   var arguments: MutableList<ScheduleArgumentDTO> = mutableListOf()
) : Identifiable {

   override fun myId(): UUID? = id
}
