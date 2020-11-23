package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.domain.Identifiable

data class AccountEntity(
   val id: Long? = null,
   val number: Long,
   val name: String,
   val type: AccountType,
   val normalAccountBalance: NormalAccountBalanceType,
   val status: AccountStatusType,
   val form1099Field: Int? = null,
   val corporateAccountIndicator: Boolean
) : Identifiable {

   constructor(
      accountDTO: AccountDTO,
      accountType: AccountType,
      normalAccountBalanceType: NormalAccountBalanceType,
      accountStatusType: AccountStatusType
   ) :
      this(
         id = accountDTO.id,
         number = accountDTO.number!!,
         name = accountDTO.name!!,
         type = accountType,
         normalAccountBalance = normalAccountBalanceType,
         status = accountStatusType,
         form1099Field = accountDTO.form1099Field,
         corporateAccountIndicator = accountDTO.corporateAccountIndicator!!
      )

   override fun myId(): Long? = id
}
