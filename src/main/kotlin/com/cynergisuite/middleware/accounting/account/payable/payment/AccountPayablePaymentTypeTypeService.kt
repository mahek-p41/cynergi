package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentTypeTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AccountPayablePaymentTypeTypeService @Inject constructor(
   private val repository: AccountPayablePaymentTypeTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<AccountPayablePaymentTypeType> =
      repository.findAll()
}
