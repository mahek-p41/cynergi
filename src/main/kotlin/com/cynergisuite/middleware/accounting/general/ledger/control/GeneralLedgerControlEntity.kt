package com.cynergisuite.middleware.accounting.general.ledger.control

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.Store
import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class GeneralLedgerControlEntity(
   val id: UUID? = null,
   val company: CompanyEntity,
   val defaultProfitCenter: Store,
   val defaultAccountPayableAccount: AccountEntity? = null,
   val defaultAccountPayableDiscountAccount: AccountEntity? = null,
   val defaultAccountReceivableAccount: AccountEntity? = null,
   val defaultAccountReceivableDiscountAccount: AccountEntity? = null,
   val defaultAccountMiscInventoryAccount: AccountEntity? = null,
   val defaultAccountSerializedInventoryAccount: AccountEntity? = null,
   val defaultAccountUnbilledInventoryAccount: AccountEntity? = null,
   val defaultAccountFreightAccount: AccountEntity? = null
) : Identifiable {

   override fun myId(): UUID? = id
}