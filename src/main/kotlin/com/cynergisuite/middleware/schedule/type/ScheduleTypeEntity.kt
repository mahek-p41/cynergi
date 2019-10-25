package com.cynergisuite.middleware.schedule.type

import com.cynergisuite.domain.TypeDomainEntity

data class ScheduleTypeEntity(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<ScheduleTypeEntity> {
   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
