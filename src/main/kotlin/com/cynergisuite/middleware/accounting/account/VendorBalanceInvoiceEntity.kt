package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.vendor.VendorType
import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Introspected
data class VendorBalanceInvoiceEntity(
   val id: UUID? = null,
   val expenseDate: LocalDate,
   val action: String?,
   val invoiceNumber: Long?,
   val invoiceDate: LocalDate?,
   val poNumber: String?,
   val amount: BigDecimal?,
   val balance: BigDecimal?
) : Identifiable {

   constructor(
      dto: VendorBalanceInvoiceDTO
   ) :
      this(
         id = dto.id,
         expenseDate = dto.expenseDate!!,
         action = dto.action!!,
         invoiceNumber = dto.invoiceNumber!!,
         invoiceDate = dto.invoiceDate!!,
         poNumber = dto.poNumber!!,
         amount = dto.amount,
         balance = dto.balance
      )

   override fun myId(): UUID? = id
}
