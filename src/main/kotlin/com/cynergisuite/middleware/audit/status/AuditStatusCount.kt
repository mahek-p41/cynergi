package com.cynergisuite.middleware.audit.status

import com.cynergisuite.domain.TypeDomainEntity

data class AuditStatusCount(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String,
   val color: String,
   val count: Int
) : TypeDomainEntity<AuditStatusCount> {
   override fun entityId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
