package com.cynergisuite.middleware.notification

import com.cynergisuite.domain.TypeDomainEntity
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "NotificationType", description = "The type describing a Notification")
data class NotificationTypeDomain(
   val id: Long ,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<NotificationTypeDomain> {

   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
