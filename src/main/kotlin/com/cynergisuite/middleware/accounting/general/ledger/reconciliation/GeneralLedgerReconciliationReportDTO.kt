package com.cynergisuite.middleware.accounting.general.ledger.reconciliation

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import org.jetbrains.annotations.NotNull

@Introspected
@Schema(name = "GeneralLedgerReconciliationReport", title = "Defines a general ledger reconciliation report", description = "Defines a general ledger reconciliation report")
data class GeneralLedgerReconciliationReportDTO(

   @field:Schema(description = "List of Inventory")
   var inventory: MutableList<GeneralLedgerReconciliationInventoryDTO> = mutableListOf(),

   @field:NotNull
   @field:Schema(description = "Total balance for each date range column")
   var inventoryTotals: List<GeneralLedgerReconciliationInventoryEntity>? = listOf(),
) {
   constructor(entity: GeneralLedgerReconciliationReportEntity) :
      this(
         inventory = entity.inventory.asSequence().map { vendorDetailEntity ->
            GeneralLedgerReconciliationInventoryDTO(vendorDetailEntity)
         }.toMutableList(),
         inventoryTotals = entity.inventoryTotals
      )
}
