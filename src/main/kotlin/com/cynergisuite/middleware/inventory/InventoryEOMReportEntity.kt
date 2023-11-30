package com.cynergisuite.middleware.inventory

import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal

@Introspected
data class InventoryEOMReportEntity(
   var accountNumber: Int? = null,
   var accountName: String? = null,
   var storeNumber: Int? = null,
   var deprUnits: BigDecimal? = null,
   var nonDepr: BigDecimal? = null,
   var reportTotal: Int? = null,
   var glBalance: Int? = null,
   var difference: Int? = null,
)
