package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentStatusTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountPayablePaymentStatusTypeService @Inject constructor(
   private val repository: AccountPayablePaymentStatusTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<AccountPayablePaymentStatusType> =
      repository.findAll()
}
