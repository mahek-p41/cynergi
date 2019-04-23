package com.cynergisuite.middleware.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.cynergisuite.middleware.entity.NotificationDto
import com.cynergisuite.middleware.localization.MessageCodes.Validation.NOT_NULL
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Deprecated("This just needs to be removed when notifications gets moved to cynergi-client")
@com.cynergisuite.middleware.dto.DataTransferObject
@JsonInclude(NON_NULL)
data class NotificationRequestDto(

   @field:Valid
   @field:NotNull(message = NOT_NULL)
   var notification: NotificationDto
)
