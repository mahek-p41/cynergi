package com.cynergisuite.middleware.inventory

import com.cynergisuite.domain.LegacyIdentifiable
import com.cynergisuite.middleware.accounting.account.payable.cashflow.CashFlowBalanceEntity
import com.cynergisuite.middleware.accounting.account.payable.cashflow.CashFlowReportInvoiceDetailEntity
import com.cynergisuite.middleware.inventory.location.InventoryLocationType
import com.cynergisuite.middleware.location.Location
import com.cynergisuite.middleware.store.Store
import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal
import java.time.LocalDate

@Introspected
data class InventoryEndOfMonthEntity(
   var accountNumber: Int? = null,
   var accountName: String? = null,
   var storeNumber: Int? = null,
   var deprUnits: BigDecimal? = null,
   var nonDepr: BigDecimal? = null,
   var reportTotal: Int? = null,
   var glBalance: Int? = null,
   var difference: Int? = null,
)
