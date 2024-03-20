package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.store.StoreEntity
import java.math.BigDecimal
import java.util.UUID

data class AccountPayableInvoiceDistributionEntity(
   val id: UUID? = null,
   val invoiceId: UUID,
   val accountId: UUID,
   val profitCenter: Int,
   val amount: BigDecimal
) : Identifiable {

   override fun myId(): UUID? = id
}
