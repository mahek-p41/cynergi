package com.cynergisuite.domain

import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.NotNull

@Schema(
   name = "Vendor1099FilterRequest",
   title = "Vendor 1099 Filter Request",
   description = "Filter request for Vendor 1099",
   allOf = [PageRequestBase::class]
)
@Introspected
class Vendor1099FilterRequest(
   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @JsonInclude(JsonInclude.Include.NON_DEFAULT)
   @field:NotNull
   @field:Schema(name = "form1099Type", description = "The form 1099 type", required = true)
   var form1099Type: String? = null,

   @JsonInclude(JsonInclude.Include.NON_DEFAULT)
   @field:Schema(name = "beginningAccountNumber", description = "The beginning vendor account number", required = false)
   var beginningAccountNumber: String? = null,

   @JsonInclude(JsonInclude.Include.NON_DEFAULT)
   @field:Schema(name = "endingAccountNumber", description = "The ending vendor account number", required = false)
   var endingAccountNumber: String? = null,

   @field:Schema(name = "vendorGroup", description = "Vendor group", required = false)
   var vendorGroup: UUID? = null,

   @field:Schema(name = "beginningPaymentDate", description = "Beginning account payable payment date", required = false)
   var beginningPaymentDate: LocalDate? = null,

   @field:Schema(name = "endingPaymentDate", description = "Ending account payable payment date", required = false)
   var endingPaymentDate: LocalDate? = null,

   @field:Schema(name = "excludeBelow", description = "Amount below which to exclude a vendor's data (distribution amounts minus discounts taken)", required = false)
   var excludeBelow: BigDecimal? = null

   ) : PageRequestBase<Vendor1099FilterRequest>(page, size, sortBy, sortDirection) {

   override fun sortByMe(): String = sortBy()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): Vendor1099FilterRequest =
      Vendor1099FilterRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         form1099Type = this.form1099Type,
         beginningAccountNumber = this.beginningAccountNumber,
         endingAccountNumber = this.endingAccountNumber,
         vendorGroup = this.vendorGroup,
         beginningPaymentDate = this.beginningPaymentDate,
         endingPaymentDate = this.endingPaymentDate,
         excludeBelow = this.excludeBelow
      )

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "form1099Type" to form1099Type,
         "beginningAccountNumber" to beginningAccountNumber,
         "endingAccountNumber" to endingAccountNumber,
         "vendorGroup" to vendorGroup,
         "beginningPaymentDate" to beginningPaymentDate,
         "endingPaymentDate" to endingPaymentDate,
         "excludeBelow" to excludeBelow
      )
}
