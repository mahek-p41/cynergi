package com.cynergisuite.middleware.vendor.rebate

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.AccountStatusType
import java.math.BigDecimal

data class RebateEntity(
   val id: Long? = null,
   val vendors: List<Identifiable>? = listOf(),
   val status: AccountStatusType,
   val description: String,
   val rebate: RebateType,
   val percent: BigDecimal?,
   val amountPerUnit: BigDecimal?,
   val accrualIndicator: Boolean,
   val generalLedgerDebitAccount: AccountEntity?,
   val generalLedgerCreditAccount: AccountEntity
) : Identifiable {

   constructor(id: Long? = null, dto: RebateDTO, vendors: List<Identifiable>?, status: AccountStatusType, rebate: RebateType, generalLedgerDebitAccount: AccountEntity?, generalLedgerCreditAccount: AccountEntity) :
      this(
         id = id ?: dto.myId(),
         vendors = vendors,
         status = status,
         description = dto.description!!,
         rebate = rebate,
         percent = dto.percent,
         amountPerUnit = dto.amountPerUnit,
         accrualIndicator = dto.accrualIndicator!!,
         generalLedgerDebitAccount = generalLedgerDebitAccount,
         generalLedgerCreditAccount = generalLedgerCreditAccount
      )

   override fun myId(): Long? = id
}
