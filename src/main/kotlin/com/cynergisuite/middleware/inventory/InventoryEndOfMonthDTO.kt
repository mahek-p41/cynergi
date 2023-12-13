package com.cynergisuite.middleware.inventory

import com.fasterxml.jackson.annotation.JsonView
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Introspected
@JsonView
@Schema(name = "Inventory End Of Month", title = "Inventory End of Month Item", description = "Single item in inventory end of month")
data class InventoryEndOfMonthDTO(

   @Schema(name = "id", description = "id of the inventory end of month item")
   var id: UUID? = null,

   @Schema(name = "companyId", description = "company id of the inventory end of month item")
   var companyId: UUID? = null,

   @Schema(name = "storeNumber", description = "store number of the inventory end of month item")
   var storeNumber: Long? = null,

   @Schema(name = "year", description = "year of the inventory end of month item")
   var year: Int? = null,

   @Schema(name = "month", description = "month of the inventory end of month item")
   var month: Int? = null,

   @Schema(name = "serialNumber", description = "serial number of the inventory end of month item")
   var serialNumber: String? = null,

   @Schema(name = "cost", description = "cost of the inventory end of month item")
   var cost: BigDecimal? = null,

   @Schema(name = "netBookValue", description = "net book value of the inventory end of month item")
   var netBookValue: BigDecimal? = null,

   @Schema(name = "bookDepreciation", description = "book depreciation of the inventory end of month item")
   var bookDepreciation: BigDecimal? = null,

   @Schema(name = "assetAccountId", description = "asset account id of the inventory end of month item")
   var assetAccountId: UUID? = null,

   @Schema(name = "contraAssetAccountId", description = "contra asset account id of the inventory end of month item")
   var contraAssetAccountId: UUID? = null,

   @Schema(name = "model", description = "model of the inventory end of month item")
   var model: String? = null,

   @Schema(name = "alternateId", description = "alternate id of the inventory end of month item")
   var alternateId: String? = null,

   @Schema(name = "currentInvIndr", description = "current inventory indicator of the inventory end of month item")
   var currentInvIndr: Int? = null,

   @Schema(name = "macrsPreviousFiscalYearEndCost", description = "macrs previous fiscal year end cost of the inventory end of month item")
   var macrsPreviousFiscalYearEndCost: BigDecimal? = null,

   @Schema(name = "macrsPreviousFiscalYearEndDepr", description = "macrs previous fiscal year end depreciation of the inventory end of month item")
   var macrsPreviousFiscalYearEndDepr: BigDecimal? = null,

   @Schema(name = "macrsPreviousFiscalYearEndAmtDepr", description = "macrs previous fiscal year end amount depreciation of the inventory end of month item")
   var macrsPreviousFiscalYearEndAmtDepr: BigDecimal? = null,

   @Schema(name = "macrsPreviousFiscalYearEndDate", description = "macrs previous fiscal year end date of the inventory end of month item")
   var macrsPreviousFiscalYearEndDate: LocalDate? = null,

   @Schema(name = "macrsLatestFiscalYearEndCost", description = "macrs latest fiscal year end cost of the inventory end of month item")
   var macrsLatestFiscalYearEndCost: BigDecimal? = null,

   @Schema(name = "macrsLatestFiscalYearEndDepr", description = "macrs latest fiscal year end depreciation of the inventory end of month item")
   var macrsLatestFiscalYearEndDepr: BigDecimal? = null,

   @Schema(name = "macrsLatestFiscalYearEndAmtDepr", description = "macrs latest fiscal year end amount depreciation of the inventory end of month item")
   var macrsLatestFiscalYearEndAmtDepr: BigDecimal? = null,

   @Schema(name = "macrsPreviousFiscalYearBonus", description = "macrs previous fiscal year bonus of the inventory end of month item")
   var macrsPreviousFiscalYearBonus: BigDecimal? = null,

   @Schema(name = "macrsLatestFiscalYearBonus", description = "macrs latest fiscal year bonus of the inventory end of month item")
   var macrsLatestFiscalYearBonus: BigDecimal? = null,

   )  {
   constructor(entity: InventoryEndOfMonthEntity) :
      this(
         id = entity.id,
         companyId = entity.companyId,
         storeNumber = entity.storeNumber,
         year = entity.year,
         month = entity.month,
         serialNumber = entity.serialNumber,
         cost = entity.cost,
         netBookValue = entity.netBookValue,
         bookDepreciation = entity.bookDepreciation,
         assetAccountId = entity.assetAccountId,
         contraAssetAccountId = entity.contraAssetAccountId,
         model = entity.model,
         alternateId = entity.alternateId,
         currentInvIndr = entity.currentInvIndr,
         macrsPreviousFiscalYearEndCost = entity.macrsPreviousFiscalYearEndCost,
         macrsPreviousFiscalYearEndDepr = entity.macrsPreviousFiscalYearEndDepr,
         macrsPreviousFiscalYearEndAmtDepr = entity.macrsPreviousFiscalYearEndAmtDepr,
         macrsPreviousFiscalYearEndDate = entity.macrsPreviousFiscalYearEndDate,
         macrsLatestFiscalYearEndCost = entity.macrsLatestFiscalYearEndCost,
         macrsLatestFiscalYearEndDepr = entity.macrsLatestFiscalYearEndDepr,
         macrsLatestFiscalYearEndAmtDepr = entity.macrsLatestFiscalYearEndAmtDepr,
         macrsPreviousFiscalYearBonus = entity.macrsPreviousFiscalYearBonus,
         macrsLatestFiscalYearBonus = entity.macrsLatestFiscalYearBonus,
      )
}
