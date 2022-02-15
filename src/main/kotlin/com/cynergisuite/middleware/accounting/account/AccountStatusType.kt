package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.domain.TypeDomainEntity
import io.micronaut.core.annotation.Introspected

@Introspected
data class AccountStatusType(
   val id: Int,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<AccountStatusType> {

   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
