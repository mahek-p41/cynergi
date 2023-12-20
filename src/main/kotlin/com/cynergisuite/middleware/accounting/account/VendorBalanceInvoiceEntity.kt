package com.cynergisuite.middleware.accounting.account

import java.math.BigDecimal
import java.time.LocalDate

data class VendorBalanceInvoiceEntity(
   var expenseDate: LocalDate? = null,
   var action: String? = null,
   var invoiceNumber: String? = null,
   var invoiceDate: LocalDate? = null,
   var poNumber: String? = null,
   var amount: BigDecimal? = null,
   var balance: BigDecimal? = null
)
