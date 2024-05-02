package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(
   name = "InventoryInvoiceFilterRequest",
   title = "Inventory Invoice Filter Request",
   description = "Filter request for Account Payable Inventory Invoice",
   allOf = [PageRequestBase::class]
)
@Introspected
class InventoryInvoiceFilterRequest(

   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Schema(name = "recvLoc", description = "Receiving location")
   var recvLoc: Int? = null,

   @field:Schema(name = "serialNbr", description = "Serial number")
   var serialNbr: String? = null,

   @field:Schema(name = "modelNbr", description = "Model number")
   var modelNbr: String? = null,

   @field:Schema(name = "poNbr", description = "Purchase order number")
   var poNbr: String? = null,

   @field:Schema(name = "invoiceNbr", description = "Invoice number")
   var invoiceNbr: String? = null,

   @field:Schema(name = "receivedDate", description = "Received date")
   var receivedDate: LocalDate? = null,

   @field:Schema(name = "beginAltId", description = "Beginning alternate ID")
   var beginAltId: String? = null,

   @field:Schema(name = "endAltId", description = "Ending alternate ID")
   var endAltId: String? = null,

   @field:Schema(name = "vendor", description = "Vendor number")
   var vendor: String? = null

) : PageRequestBase<InventoryInvoiceFilterRequest>(page, size, sortBy, sortDirection) {

   override fun sortByMe(): String = sortBy()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): InventoryInvoiceFilterRequest =
      InventoryInvoiceFilterRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         recvLoc = this.recvLoc,
         serialNbr = this.serialNbr,
         modelNbr = this.modelNbr,
         poNbr = this.poNbr,
         invoiceNbr = this.invoiceNbr,
         receivedDate = this.receivedDate,
         beginAltId = this.beginAltId,
         endAltId = this.endAltId,
         vendor = this.vendor
      )

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "recvLoc" to recvLoc,
         "serialNbr" to serialNbr,
         "modelNbr" to modelNbr,
         "poNbr" to poNbr,
         "invoiceNbr" to invoiceNbr,
         "receivedDate" to receivedDate,
         "beginAltId" to beginAltId,
         "endAltId" to endAltId,
         "vendor" to vendor
      )
}