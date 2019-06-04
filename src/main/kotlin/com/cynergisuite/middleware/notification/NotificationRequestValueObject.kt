package com.cynergisuite.middleware.notification

import com.cynergisuite.domain.ValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Deprecated("This just needs to be removed when notifications gets moved to cynergi-client")
@ValueObject
@JsonInclude(NON_NULL)
data class NotificationRequestValueObject(

   @field:Valid
   @field:NotNull
   var notification: NotificationValueObject
)
