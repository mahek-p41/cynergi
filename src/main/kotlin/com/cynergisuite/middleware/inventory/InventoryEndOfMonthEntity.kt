package com.cynergisuite.middleware.inventory

import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Introspected
data class InventoryEndOfMonthEntity(
   var id: UUID? = null,
   var companyId: UUID? = null,
   var storeNumber: Long? = null,
   var year: Int? = null,
   var month: Int? = null,
   var serialNumber: String? = null,
   var cost: BigDecimal? = null,
   var netBookValue: BigDecimal? = null,
   var bookDepreciation: BigDecimal? = null,
   var assetAccountId: UUID? = null,
   var contraAssetAccountId: UUID? = null,
   var model: String? = null,
   var alternateId: String? = null,
   var currentInvIndr: Int? = null,
   var macrsPreviousFiscalYearEndCost: BigDecimal? = null,
   var macrsPreviousFiscalYearEndDepr: BigDecimal? = null,
   var macrsPreviousFiscalYearEndAmtDepr: BigDecimal? = null,
   var macrsPreviousFiscalYearEndDate: LocalDate? = null,
   var macrsLatestFiscalYearEndCost: BigDecimal? = null,
   var macrsLatestFiscalYearEndDepr: BigDecimal? = null,
   var macrsLatestFiscalYearEndAmtDepr: BigDecimal? = null,
   var macrsPreviousFiscalYearBonus: BigDecimal? = null,
   var macrsLatestFiscalYearBonus: BigDecimal? = null,
)
