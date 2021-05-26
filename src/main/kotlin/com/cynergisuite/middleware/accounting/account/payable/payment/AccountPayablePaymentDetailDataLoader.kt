package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceEntity
import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentDetailRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.vendor.VendorDTO
import com.cynergisuite.middleware.vendor.VendorEntity
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

object AccountPayablePaymentDetailDataLoader {

   @JvmStatic
   fun stream(
      numberIn: Int = 1,
      vendorIn: VendorEntity? = null,
      invoiceIn: AccountPayableInvoiceEntity,
      apPaymentIn: AccountPayablePaymentEntity
   ): Stream<AccountPayablePaymentDetailEntity> {
      val number = if (numberIn < 0) 1 else numberIn

      return IntStream.range(0, number).mapToObj {
         AccountPayablePaymentDetailEntity(
            vendor = vendorIn,
            invoice = invoiceIn,
            payment = apPaymentIn,
            amount = Random.nextInt(1, 100000).toBigDecimal(),
            discount = Random.nextInt(1, 100000).toBigDecimal()
         )
      }
   }

   @JvmStatic
   fun streamDTO(
      numberIn: Int = 1,
      vendorIn: SimpleIdentifiableDTO? = null,
      invoiceIn: AccountPayableInvoiceDTO,
      paymentIn: SimpleIdentifiableDTO
   ): Stream<AccountPayablePaymentDetailDTO> {
      val number = if (numberIn < 0) 1 else numberIn

      return IntStream.range(0, number).mapToObj {
         AccountPayablePaymentDetailDTO(
            vendor = vendorIn?.let { VendorDTO(id = it.id) },
            invoice = invoiceIn,
            payment = paymentIn,
            invoiceAmount = Random.nextInt(1, 100000).toBigDecimal(),
            discountAmount = Random.nextInt(1, 100000).toBigDecimal()
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AccountPayablePaymentDetailDataLoaderService @Inject constructor(
   private val accountPayablePaymentDetailRepository: AccountPayablePaymentDetailRepository
) {

   fun stream(
      numberIn: Int = 1,
      company: Company,
      vendorIn: VendorEntity? = null,
      invoiceIn: AccountPayableInvoiceEntity,
      apPaymentIn: AccountPayablePaymentEntity
   ): Stream<AccountPayablePaymentDetailEntity> {
      return AccountPayablePaymentDetailDataLoader.stream(numberIn, vendorIn, invoiceIn, apPaymentIn)
         .map { accountPayablePaymentDetailRepository.insert(it, company) }
   }

   fun single(
      company: Company,
      vendorIn: VendorEntity,
      invoiceIn: AccountPayableInvoiceEntity,
      apPaymentIn: AccountPayablePaymentEntity
   ): AccountPayablePaymentDetailEntity {
      return stream(1, company, vendorIn, invoiceIn, apPaymentIn).findFirst().orElseThrow { Exception("Unable to create Account Payable Payment Detail") }
   }

   fun singleDTO(
      vendorIn: SimpleIdentifiableDTO? = null,
      invoiceIn: AccountPayableInvoiceDTO,
      apPaymentIn: SimpleIdentifiableDTO
   ): AccountPayablePaymentDetailDTO {
      return AccountPayablePaymentDetailDataLoader.streamDTO(1, vendorIn, invoiceIn, apPaymentIn).findFirst().orElseThrow { Exception("Unable to create Account Payable Payment Detail") }
   }
}
