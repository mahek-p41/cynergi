package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.vendor.VendorType
import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal
import java.util.UUID

@Introspected
data class VendorBalanceEntity(
   val id: UUID? = null,
   val number: Long,
   val name: String,
   val balance: BigDecimal,
   val invoiceList: List<VendorBalanceInvoiceDTO>
) : Identifiable {

   constructor(
      dto: VendorBalanceDTO
   ) :
      this(
         id = dto.id,
         number = dto.number!!,
         name = dto.name!!,
         balance = dto.balance!!,
         invoiceList = dto.invoiceList!!
      )

   override fun myId(): UUID? = id
}
