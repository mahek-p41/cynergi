package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceEntity
import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentDetailRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.vendor.VendorEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import javax.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class AccountPayablePaymentDetailTestDataLoader {

   static Stream<AccountPayablePaymentDetailEntity> stream(
      int numberIn = 1,
      VendorEntity vendorIn = null,
      AccountPayableInvoiceEntity invoiceIn,
      AccountPayablePaymentEntity apPaymentIn
   ) {
      final number = numberIn < 0 ? 1 : numberIn
      final faker = new Faker()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new AccountPayablePaymentDetailEntity(
            null,
            vendorIn,
            invoiceIn,
            apPaymentIn,
            random.nextInt(1, 100000).toBigDecimal(),
            random.nextInt(1, 100000).toBigDecimal()
         )
      }
   }

   static Stream<AccountPayablePaymentDetailDTO> streamDTO(
      int numberIn = 1,
      SimpleIdentifiableDTO vendorIn = null,
      AccountPayableInvoiceDTO invoiceIn,
      SimpleIdentifiableDTO paymentIn
   ) {
      final number = numberIn < 0 ? 1 : numberIn
      final faker = new Faker()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new AccountPayablePaymentDetailDTO(
            null,
            vendorIn == null ? null : new SimpleIdentifiableDTO(vendorIn.id) ,
            invoiceIn,
            paymentIn,
            random.nextInt(1, 100000).toBigDecimal(),
            random.nextInt(1, 100000).toBigDecimal()
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AccountPayablePaymentDetailDataLoaderService {
   private final AccountPayablePaymentDetailRepository accountPayablePaymentDetailRepository

   AccountPayablePaymentDetailDataLoaderService(AccountPayablePaymentDetailRepository accountPayablePaymentDetailRepository) {
      this.accountPayablePaymentDetailRepository = accountPayablePaymentDetailRepository
   }

   Stream<AccountPayablePaymentDetailEntity> stream(
      int numberIn = 1,
      CompanyEntity company,
      VendorEntity vendorIn = null,
      AccountPayableInvoiceEntity invoiceIn,
      AccountPayablePaymentEntity apPaymentIn
   ) {
      return AccountPayablePaymentDetailTestDataLoader.stream(numberIn, vendorIn, invoiceIn, apPaymentIn)
         .map { accountPayablePaymentDetailRepository.insert(it, company) }
   }

   AccountPayablePaymentDetailEntity single(
      CompanyEntity company,
      VendorEntity vendorIn,
      AccountPayableInvoiceEntity invoiceIn,
      AccountPayablePaymentEntity apPaymentIn
   ) {
      return stream(1, company, vendorIn, invoiceIn, apPaymentIn).findFirst().orElseThrow { new Exception("Unable to create Account Payable Payment Detail") }
   }

   AccountPayablePaymentDetailDTO singleDTO(
      SimpleIdentifiableDTO vendorIn = null,
      AccountPayableInvoiceDTO invoiceIn,
      SimpleIdentifiableDTO apPaymentIn
   ) {
      return AccountPayablePaymentDetailTestDataLoader.streamDTO(1, vendorIn, invoiceIn, apPaymentIn).findFirst().orElseThrow { new Exception("Unable to create Account Payable Payment Detail") }
   }
}
