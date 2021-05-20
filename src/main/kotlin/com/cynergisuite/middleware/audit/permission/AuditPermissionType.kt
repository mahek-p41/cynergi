package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.domain.TypeDomainEntity

data class AuditPermissionType(
   val id: Int,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<AuditPermissionType> {
   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
   override fun hashCode(): Int = super.basicHashCode()
   override fun equals(other: Any?): Boolean =
      if (other is AuditPermissionType) {
         super.basicEquality(other)
      } else {
         false
      }
}
