package com.cynergisuite.middleware.accounting.account.payable

import com.cynergisuite.domain.TypeDomainEntity

data class AccountPayableRecurringInvoiceStatusType(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<AccountPayableRecurringInvoiceStatusType> {

   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
