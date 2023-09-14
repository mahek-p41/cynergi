package com.cynergisuite.middleware.accounting.account.payable.invoice

import java.math.BigDecimal

data class AccountPayableCheckPreviewEntity(
   val vendorList: List<AccountPayableCheckPreviewVendorsEntity>,

   val gross: BigDecimal,

   val discount: BigDecimal,

   val deduction: BigDecimal,

   val netPaid: BigDecimal,
)
