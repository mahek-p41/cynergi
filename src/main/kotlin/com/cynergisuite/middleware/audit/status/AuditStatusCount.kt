package com.cynergisuite.middleware.audit.status

import com.cynergisuite.domain.TypeDomain

data class AuditStatusCount(
   val id: Int,
   val value: String,
   val description: String,
   val localizationCode: String,
   val color: String,
   val count: Int
) : TypeDomain() {
   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
