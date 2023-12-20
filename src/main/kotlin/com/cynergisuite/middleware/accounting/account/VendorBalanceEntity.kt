package com.cynergisuite.middleware.accounting.account

import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal
import java.util.LinkedHashSet

@Introspected
data class VendorBalanceEntity(
   val number: Long,
   val name: String,
   val balance: BigDecimal,
   val invoiceList: MutableSet<VendorBalanceInvoiceEntity>? = LinkedHashSet()
)
