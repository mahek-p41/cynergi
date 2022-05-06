package com.cynergisuite.middleware.accounting.general.ledger.recurring.entries

import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringEntity
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringType
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.GeneralLedgerRecurringDistributionEntity
import com.cynergisuite.middleware.store.Store
import java.math.BigDecimal
import java.util.UUID

data class GeneralLedgerRecurringEntriesEntity(
   var generalLedgerRecurring: GeneralLedgerRecurringEntity,
   var generalLedgerRecurringDistributions: List<GeneralLedgerRecurringDistributionEntity>,
   val balance: BigDecimal

) {

   constructor(generalLedgerRecurring: GeneralLedgerRecurringEntity, generalLedgerRecurringDistributions: MutableList<GeneralLedgerRecurringDistributionEntity>) :
      this(
         generalLedgerRecurring = generalLedgerRecurring,
         generalLedgerRecurringDistributions = generalLedgerRecurringDistributions,
         balance = BigDecimal.ZERO
      )

   constructor(
      dto: GeneralLedgerRecurringEntriesDTO,
      glRecurringId: UUID?,
      glRecurringSourceCode: GeneralLedgerSourceCodeEntity,
      glRecurringType: GeneralLedgerRecurringType,
      glDistributions: List<GeneralLedgerRecurringDistributionEntity>?,
      glDistributionAccts: List<AccountEntity>,
      glDistributionProfitCenters: List<Store>
   ) :
      this(
         generalLedgerRecurring = GeneralLedgerRecurringEntity(
            id = glRecurringId,
            dto = dto.generalLedgerRecurring!!,
            source = glRecurringSourceCode,
            type = glRecurringType
         ),
         generalLedgerRecurringDistributions = dto.generalLedgerRecurringDistributions.asSequence().mapIndexed { index, it ->
            GeneralLedgerRecurringDistributionEntity(
               id = glDistributions?.get(index)?.id,
               generalLedgerRecurring = GeneralLedgerRecurringEntity(
                  id = glRecurringId,
                  dto = dto.generalLedgerRecurring!!,
                  source = glRecurringSourceCode,
                  type = glRecurringType
               ),
               generalLedgerDistributionAccount = glDistributionAccts[index],
               generalLedgerDistributionProfitCenter = glDistributionProfitCenters[index],
               generalLedgerDistributionAmount = it.generalLedgerDistributionAmount!!
            )
         }.toMutableList()
      )
}
