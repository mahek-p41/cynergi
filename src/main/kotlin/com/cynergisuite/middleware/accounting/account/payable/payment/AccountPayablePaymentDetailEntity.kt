package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceEntity
import com.cynergisuite.middleware.vendor.VendorEntity
import java.math.BigDecimal
import java.util.UUID

data class AccountPayablePaymentDetailEntity(
   val id: UUID? = null,
   val vendor: VendorEntity? = null,
   val invoice: AccountPayableInvoiceEntity,
   val payment: AccountPayablePaymentEntity?,
   val amount: BigDecimal,
   val discount: BigDecimal?,
) : Identifiable {

   constructor(
      dto: AccountPayablePaymentDetailDTO,
      vendor: VendorEntity? = null,
      invoice: AccountPayableInvoiceEntity,
      payment: AccountPayablePaymentEntity
   ) :
      this(
         id = dto.id,
         vendor = vendor,
         invoice = invoice,
         payment = payment,
         amount = dto.invoiceAmount!!,
         discount = dto.discountAmount
      )

   override fun myId(): UUID? = id
}
