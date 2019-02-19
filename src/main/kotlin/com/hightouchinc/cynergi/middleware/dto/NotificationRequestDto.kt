package com.hightouchinc.cynergi.middleware.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.hightouchinc.cynergi.middleware.entity.NotificationDto
import javax.validation.Valid

@Deprecated("This just needs to be removed when notifications is moved to cynergi-client")
@DataTransferObject
@JsonInclude(NON_NULL)
data class NotificationRequestDto(
   @field:Valid var notification: NotificationDto
)
