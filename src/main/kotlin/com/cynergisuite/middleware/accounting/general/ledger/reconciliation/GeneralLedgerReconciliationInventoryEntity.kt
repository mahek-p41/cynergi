package com.cynergisuite.middleware.accounting.general.ledger.reconciliation

import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal

@Introspected
data class GeneralLedgerReconciliationInventoryEntity(

   var accountNumber: Int? = null,
   var accountType: String? = null,
   var accountName: String? = null,
   var storeNumber: Int? = null,
   var deprUnits: BigDecimal? = null,
   var nonDepr: BigDecimal? = null,
   var reportTotal: BigDecimal? = null,
   var glBalance: BigDecimal? = null,
   var difference: BigDecimal? = null,
   var currentInvInd: Int? = null,
   var period: Int? = null
)
