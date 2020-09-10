package com.cynergisuite.middleware.accounting.account.payable

import com.cynergisuite.middleware.accounting.account.payable.infrastructure.DefaultAccountPayableStatusTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultAccountPayableStatusTypeService @Inject constructor(
   private val repository: DefaultAccountPayableStatusTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<DefaultAccountPayableStatusType> =
      repository.findAll()
}
