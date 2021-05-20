package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.domain.TypeDomainEntity

data class AccountPayablePaymentTypeType(
   val id: Int,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<AccountPayablePaymentTypeType> {

   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
