package com.cynergisuite.middleware.general.ledger.control

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import java.time.LocalDate

data class GeneralLedgerControlEntity(
   val id: Long? = null,
   val periodFrom: LocalDate,
   val periodTo: LocalDate,
   val defaultProfitCenter: Int,
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
         periodFrom = dto.periodFrom!!,
         periodTo = dto.periodTo!!,
         defaultProfitCenter = dto.defaultProfitCenter!!,
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
