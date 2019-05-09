package com.cynergisuite.middleware.notification

import com.cynergisuite.domain.ValueObjectBase
import com.cynergisuite.middleware.localization.MessageCodes.Validation.POSITIVE
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
data class NotificationTypeDomainValueObject (

   @field:Positive(message = POSITIVE)
   var id: Long? = null,

   val value: String,

   val description: String

) : ValueObjectBase<NotificationTypeDomainValueObject>() {

   constructor(entity: NotificationTypeDomain) :
      this(
         id = entity.id,
         value = entity.value,
         description = entity.description
      )

   override fun dtoId(): Long? = id

   override fun copyMe(): NotificationTypeDomainValueObject = copy()
}
