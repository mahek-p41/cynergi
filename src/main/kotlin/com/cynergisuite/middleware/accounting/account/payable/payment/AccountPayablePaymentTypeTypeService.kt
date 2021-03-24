package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentTypeTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountPayablePaymentTypeTypeService @Inject constructor(
   private val repository: AccountPayablePaymentTypeTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<AccountPayablePaymentTypeType> =
      repository.findAll()
}
