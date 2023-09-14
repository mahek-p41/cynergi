package com.cynergisuite.middleware.accounting.general.ledger.reversal.entry

import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalEntity
import com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.GeneralLedgerReversalDistributionEntity
import com.cynergisuite.middleware.store.Store
import java.math.BigDecimal
import java.util.UUID

data class GeneralLedgerReversalEntryEntity(
   var generalLedgerReversal: GeneralLedgerReversalEntity,
   var generalLedgerReversalDistributions: List<GeneralLedgerReversalDistributionEntity>,
   val balance: BigDecimal

) {

   constructor(generalLedgerReversal: GeneralLedgerReversalEntity, generalLedgerReversalDistributions: MutableList<GeneralLedgerReversalDistributionEntity>) :
      this(
         generalLedgerReversal = generalLedgerReversal,
         generalLedgerReversalDistributions = generalLedgerReversalDistributions,
         balance = BigDecimal.ZERO
      )

   constructor(
      dto: GeneralLedgerReversalEntryDTO,
      glReversalId: UUID?,
      glSourceCode: GeneralLedgerSourceCodeEntity,
      glReversalDists: List<GeneralLedgerReversalDistributionEntity>?,
      glReversalDistAccts: List<AccountEntity>,
      glReversalDistProfitCenters: List<Store>
   ) :
      this(
         generalLedgerReversal = GeneralLedgerReversalEntity(
            id = glReversalId,
            dto = dto.generalLedgerReversal!!,
            source = glSourceCode
         ),
         generalLedgerReversalDistributions = dto.generalLedgerReversalDistributions.asSequence().mapIndexed { index, it ->
            GeneralLedgerReversalDistributionEntity(
               id = glReversalDists?.get(index)?.id,
               generalLedgerReversal = GeneralLedgerReversalEntity(
                  id = glReversalId,
                  dto = dto.generalLedgerReversal!!,
                  source = glSourceCode
               ),
               generalLedgerReversalDistributionAccount = glReversalDistAccts[index],
               generalLedgerReversalDistributionProfitCenter = glReversalDistProfitCenters[index],
               generalLedgerReversalDistributionAmount = it.generalLedgerReversalDistributionAmount!!
            )
         }.toMutableList()
      )
}
