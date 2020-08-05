package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.Company

data class AccountEntity(
   val id: Long? = null,
   val company: Company,
   val name: String,
   val type: AccountType,
   val normalAccountBalance: NormalAccountBalanceType,
   val status: AccountStatusType,
   val form1099Field: Int,
   val corporateAccountIndicator: Boolean
) : Identifiable {

   constructor(
      accountDTO: AccountDTO,
      company: Company,
      accountType: AccountType,
      normalAccountBalanceType: NormalAccountBalanceType,
      accountStatusType: AccountStatusType
   ) :
      this(
         id = accountDTO.id,
         company = company,
         name = accountDTO.name!!,
         type = accountType,
         normalAccountBalance = normalAccountBalanceType,
         status = accountStatusType,
         form1099Field = accountDTO.form1099Field!!,
         corporateAccountIndicator = accountDTO.corporateAccountIndicator!!
      )

   override fun myId(): Long? = id
}
