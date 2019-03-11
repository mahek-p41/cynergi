package com.hightouchinc.cynergi.middleware.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.hightouchinc.cynergi.middleware.entity.NotificationDto
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Validation.NOT_NULL
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Deprecated("This just needs to be removed when notifications gets moved to cynergi-client")
@DataTransferObject
@JsonInclude(NON_NULL)
data class NotificationRequestDto(

   @field:Valid
   @field:NotNull(message = NOT_NULL)
   var notification: NotificationDto
)
