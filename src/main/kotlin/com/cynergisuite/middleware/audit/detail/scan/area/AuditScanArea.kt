package com.cynergisuite.middleware.audit.detail.scan.area

import com.cynergisuite.domain.TypeDomainEntity

data class AuditScanArea(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<AuditScanArea> {

   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
