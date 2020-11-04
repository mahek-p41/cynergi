package com.cynergisuite.middleware.vendor.rebate

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.AccountStatusType
import java.math.BigDecimal

data class RebateEntity(
   val id: Long? = null,
   val vendor: Identifiable,
   val status: AccountStatusType,
   val description: String,
   val rebate: RebateType,
   val percent: BigDecimal?,
   val amountPerUnit: BigDecimal?,
   val accrualIndicator: Boolean,
   val generalLedgerDebitAccount: AccountEntity?,
   val generalLedgerCreditAccount: AccountEntity?
) : Identifiable {

   constructor(id: Long? = null, dto: RebateDTO, vendor: Identifiable, status: AccountStatusType, rebate: RebateType, generalLedgerDebitAccount: AccountEntity?, generalLedgerCreditAccount: AccountEntity?) :
      this(
         id = id ?: dto.myId(),
         vendor = vendor,
         status = status,
         description = dto.description!!,
         rebate = rebate,
         percent = dto.percent,
         amountPerUnit = dto.amountPerUnit,
         accrualIndicator = dto.accrualIndicator!!,
         generalLedgerDebitAccount = generalLedgerDebitAccount,
         generalLedgerCreditAccount = generalLedgerCreditAccount
      )

   constructor(existingRebate: RebateEntity, dto: RebateDTO, vendor: Identifiable, status: AccountStatusType, rebate: RebateType, generalLedgerDebitAccount: AccountEntity?, generalLedgerCreditAccount: AccountEntity?) :
      this(
         id = existingRebate.id,
         dto = dto,
         vendor = vendor,
         status = status,
         rebate = rebate,
         generalLedgerDebitAccount = generalLedgerDebitAccount,
         generalLedgerCreditAccount = generalLedgerCreditAccount
      )

   override fun myId(): Long? = id
}
