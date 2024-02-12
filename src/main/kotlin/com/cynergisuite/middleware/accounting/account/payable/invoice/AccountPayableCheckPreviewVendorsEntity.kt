package com.cynergisuite.middleware.accounting.account.payable.invoice

import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate

data class AccountPayableCheckPreviewVendorsEntity (
   val vendorNumber: Int,
   val vendorName: String,
   val address1: String?,
   val address2: String?,
   val city: String?,
   val state: String?,
   val postalCode: String?,
   val checkNumber: BigInteger,
   val date: LocalDate,
   val invoiceList: MutableList<AccountPayableCheckPreviewInvoiceEntity>? = mutableListOf(),
   var gross: BigDecimal = BigDecimal.ZERO,
   var discount: BigDecimal = BigDecimal.ZERO,
   var deduction: BigDecimal = BigDecimal.ZERO,
   var netPaid: BigDecimal = BigDecimal.ZERO

   )
