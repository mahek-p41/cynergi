package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.domain.TypeDomainEntity

data class AuditPermissionType(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<AuditPermissionType> {
   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
