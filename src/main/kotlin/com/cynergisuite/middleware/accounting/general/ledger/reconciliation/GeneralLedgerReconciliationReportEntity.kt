package com.cynergisuite.middleware.accounting.general.ledger.reconciliation

import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal
import org.codehaus.groovy.transform.sc.ListOfExpressionsExpression

@Introspected
data class GeneralLedgerReconciliationReportEntity (
   var inventory: MutableList<GeneralLedgerReconciliationInventoryEntity> = mutableListOf(),
   var inventoryTotals: List<GeneralLedgerReconciliationInventoryEntity> = listOf(),
)
