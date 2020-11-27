package com.cynergisuite.middleware.accounting.general.ledger.control

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.store.Store

data class GeneralLedgerControlEntity(
   val id: Long? = null,
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

   constructor(
      dto: GeneralLedgerControlDTO,
      defaultProfitCenter: Store,
      defaultAccountPayableAccount: AccountEntity?,
      defaultAccountPayableDiscountAccount: AccountEntity?,
      defaultAccountReceivableAccount: AccountEntity?,
      defaultAccountReceivableDiscountAccount: AccountEntity?,
      defaultAccountMiscInventoryAccount: AccountEntity?,
      defaultAccountSerializedInventoryAccount: AccountEntity?,
      defaultAccountUnbilledInventoryAccount: AccountEntity?,
      defaultAccountFreightAccount: AccountEntity?
   ) :
      this(
         id = dto.id,
         defaultProfitCenter = defaultProfitCenter,
         defaultAccountPayableAccount = defaultAccountPayableAccount,
         defaultAccountPayableDiscountAccount = defaultAccountPayableDiscountAccount,
         defaultAccountReceivableAccount = defaultAccountReceivableAccount,
         defaultAccountReceivableDiscountAccount = defaultAccountReceivableDiscountAccount,
         defaultAccountMiscInventoryAccount = defaultAccountMiscInventoryAccount,
         defaultAccountSerializedInventoryAccount = defaultAccountSerializedInventoryAccount,
         defaultAccountUnbilledInventoryAccount = defaultAccountUnbilledInventoryAccount,
         defaultAccountFreightAccount = defaultAccountFreightAccount
      )

   override fun myId(): Long? = id
}
