package com.cynergisuite.middleware.accounting.routine.type

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "OverallPeriod", title = "Overall period", description = "Overall period type")
data class OverallPeriodTypeDTO(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Overall period type")
   var value: String,

   @field:NotNull
   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized abbreviation for overall period")
   var abbreviation: String? = null,

   @field:NotNull
   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for overall period")
   var description: String? = null

) {

   constructor(type: OverallPeriodType) :
      this(
         value = type.value,
         abbreviation = type.abbreviation,
         description = type.description
      )

   constructor(type: OverallPeriodType, localizedDescription: String) :
      this(
         value = type.value,
         abbreviation = type.abbreviation,
         description = localizedDescription
      )
}
