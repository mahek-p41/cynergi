package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.vendor.VendorType
import io.micronaut.core.annotation.Introspected
import java.util.UUID

// FIXME add company here rather than passing it in through the repository method
@Introspected
data class AccountEntity(
   val id: UUID? = null,
   val number: Long,
   val name: String,
   val type: AccountType,
   val normalAccountBalance: NormalAccountBalanceType,
   val status: AccountStatusType,
   val form1099Field: VendorType,
   val corporateAccountIndicator: Boolean
) : Identifiable {

   constructor(
      accountDTO: AccountDTO,
      accountType: AccountType,
      normalAccountBalanceType: NormalAccountBalanceType,
      accountStatusType: AccountStatusType,
      vendorType: VendorType
   ) :
      this(
         id = accountDTO.id,
         number = accountDTO.number!!,
         name = accountDTO.name!!,
         type = accountType,
         normalAccountBalance = normalAccountBalanceType,
         status = accountStatusType,
         form1099Field = vendorType,
         corporateAccountIndicator = accountDTO.corporateAccountIndicator!!
      )

   override fun myId(): UUID? = id
}
