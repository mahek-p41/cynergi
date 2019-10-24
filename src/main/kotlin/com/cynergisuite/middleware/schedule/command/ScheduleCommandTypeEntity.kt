package com.cynergisuite.middleware.schedule.command

import com.cynergisuite.domain.TypeDomainEntity

data class ScheduleCommandTypeEntity(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<ScheduleCommandTypeEntity> {
   override fun entityId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
