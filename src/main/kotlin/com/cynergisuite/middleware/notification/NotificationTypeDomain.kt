package com.cynergisuite.middleware.notification

import com.cynergisuite.domain.TypeDomainEntity

data class NotificationTypeDomain(
   val id: Long ,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<NotificationTypeDomain> {

   override fun entityId(): Long? = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
