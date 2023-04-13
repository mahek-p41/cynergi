package com.cynergisuite.middleware.vendor

import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceInquiryDTO
import com.cynergisuite.middleware.purchase.order.PurchaseOrderDTO
import com.cynergisuite.middleware.vendor.rebate.RebateDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "VendorStatisticsInquiry", title = "Vendor Statistics Inquiry", description = "AP Vendor Statistics Inquiry")
data class VendorStatisticsDTO(

   @field:NotNull
   @field:Schema(name = "vendor", description = "Vendor DTO")
   var vendor: VendorDTO,

   @field:Schema(name = "ytdPaid", description = "Total AP payment detail amount YTD")
   var ytdPaid: BigDecimal? = null,

   @field:Schema(name = "ptdPaid", description = "Total AP payment detail amount PTD")
   var ptdPaid: BigDecimal? = null,

   @field:Schema(name = "balance", description = "Total unpaid amount")
   var balance: BigDecimal? = null,

   @field:Schema(name = "dueColumns", description = "Unpaid amount by AP Invoice due date")
   var dueColumns: VendorStatisticsDueEntity? = null,

   @field:Schema(name = "rebates", description = "List of rebates")
   var rebates: List<RebateDTO>? = null,

   @field:Schema(name = "invoices", description = "List of invoices")
   var invoices: List<AccountPayableInvoiceInquiryDTO>? = null,

   @field:Schema(name = "purchaseOrders", description = "List of purchase orders")
   var purchaseOrders: List<PurchaseOrderDTO>? = null

)
