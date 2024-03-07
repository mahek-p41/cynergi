package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceSelectedTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceSelectedTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusType
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.purchase.order.PurchaseOrderDTO
import com.cynergisuite.middleware.purchase.order.PurchaseOrderEntity
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.vendor.VendorDTO
import com.cynergisuite.middleware.vendor.VendorEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class AccountPayableInvoiceDataLoader {

   static Stream<AccountPayableInvoiceEntity> stream(
      int numberIn = 1,
      VendorEntity vendorIn,
      PurchaseOrderEntity purchaseOrderIn,
      LocalDate invoiceDateIn = null,
      BigDecimal invoiceAmountIn = null,
      EmployeeEntity employeeIn,
      BigDecimal paidAmountIn = null,
      AccountPayableInvoiceStatusType statusTypeIn = null,
      LocalDate dueDateIn = null,
      VendorEntity payToIn,
      Store locationIn = null,
      BigDecimal discountTakenIn = null,
      LocalDate expenseDateIn = null
   ) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final random = faker.random()
      final lorem = faker.lorem()
      final numbers = faker.number()
      final date = faker.date()
      final invoiceDate = invoiceDateIn ?: date.past(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.of("-05:00")).toLocalDate()
      final invoiceAmount = invoiceAmountIn ?: numbers.randomDouble(2, 1, 1000000).toBigDecimal()
      final discountTaken = discountTakenIn ?: numbers.randomDouble(2, 1, 1000000).toBigDecimal()
      def paidAmount
      if (paidAmountIn >= BigDecimal.ZERO) {
         paidAmount = paidAmountIn
      } else {
         paidAmount = numbers.randomDouble(2, 1, 1000000).toBigDecimal()
      }
      final statusType = statusTypeIn ? statusTypeIn : AccountPayableInvoiceStatusTypeDataLoader.random()

      return IntStream.range(0, number).mapToObj {
         new AccountPayableInvoiceEntity(
            null,
            vendorIn,
            (9100029365 + it).toString(),
            new SimpleIdentifiableEntity(purchaseOrderIn.myId()),
            purchaseOrderIn.number,
            invoiceDate,
            invoiceAmount,
            numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            random.nextInt(1, 100).toBigDecimal().divide(BigDecimal.valueOf(100)).setScale(7, RoundingMode.HALF_EVEN),
            random.nextBoolean(),
            discountTaken,
            LocalDate.now(),
            expenseDateIn ?: LocalDate.now(),
            LocalDate.now(),
            employeeIn,
            numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            lorem.sentence(),
            AccountPayableInvoiceSelectedTypeDataLoader.random(),
            random.nextBoolean(),
            paidAmount,
            numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            AccountPayableInvoiceTypeDataLoader.predefined().find { it.value == 'P' },
            statusType,
            dueDateIn ?: LocalDate.now(),
            payToIn,
            random.nextBoolean(),
            random.nextBoolean(),
            LocalDate.of(2019, 2, 25),
            locationIn?.myId()?.with { new SimpleLegacyIdentifiableDTO(it) }
         )
      }
   }

   static Stream<AccountPayableInvoiceDTO> streamDTO(
      int numberIn = 1,
      VendorDTO vendorIn,
      PurchaseOrderDTO purchaseOrderIn,
      EmployeeValueObject employeeIn,
      VendorDTO payToIn,
      SimpleLegacyIdentifiableDTO locationIn = null
   ) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final random = faker.random()
      final lorem = faker.lorem()
      final numbers = faker.number()
      final date = faker.date()
      final invoiceDate = date.past(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.of("-05:00")).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         new AccountPayableInvoiceDTO(
            null,
            vendorIn,
            lorem.characters(3, 20),
            new SimpleIdentifiableDTO(purchaseOrderIn.myId()),
            purchaseOrderIn.number,
            invoiceDate,
            numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            random.nextInt(1, 99).toBigDecimal().divide(BigDecimal.valueOf(100)).setScale(6, RoundingMode.HALF_EVEN),
            random.nextBoolean(),
            numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            LocalDate.now(),
            LocalDate.now(),
            LocalDate.now(),
            employeeIn,
            numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            lorem.sentence(),
            new AccountPayableInvoiceSelectedTypeDTO(AccountPayableInvoiceSelectedTypeDataLoader.random()),
            random.nextBoolean(),
            numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            new AccountPayableInvoiceTypeDTO(AccountPayableInvoiceTypeDataLoader.random()),
            new AccountPayableInvoiceStatusTypeDTO(AccountPayableInvoiceStatusTypeDataLoader.random()),
            LocalDate.now(),
            payToIn,
            random.nextBoolean(),
            random.nextBoolean(),
            LocalDate.now(),
            locationIn
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AccountPayableInvoiceDataLoaderService {
   private final AccountPayableInvoiceRepository accountPayableInvoiceRepository

   AccountPayableInvoiceDataLoaderService(AccountPayableInvoiceRepository accountPayableInvoiceRepository) {
      this.accountPayableInvoiceRepository = accountPayableInvoiceRepository
   }

   Stream<AccountPayableInvoiceEntity> stream(
      int numberIn = 1,
      CompanyEntity company,
      VendorEntity vendorIn,
      PurchaseOrderEntity purchaseOrderIn = null,
      BigDecimal invoiceAmountIn = null,
      EmployeeEntity employeeIn,
      BigDecimal paidAmountIn = null,
      AccountPayableInvoiceStatusType statusTypeIn = null,
      VendorEntity payToIn,
      Store locationIn = null,
      BigDecimal discountTakenIn = null,
      LocalDate expenseDateIn = null
   ) {
      return AccountPayableInvoiceDataLoader.stream(numberIn, vendorIn, purchaseOrderIn, null, invoiceAmountIn, employeeIn, paidAmountIn, statusTypeIn, null, payToIn, locationIn, discountTakenIn, expenseDateIn)
         .map { accountPayableInvoiceRepository.insert(it, company) }
   }

   Stream<AccountPayableInvoiceEntity> stream(
      int numberIn = 1,
      CompanyEntity company,
      VendorEntity vendorIn,
      VendorEntity payToIn,
      PurchaseOrderEntity purchaseOrderIn = null,
      LocalDate invoiceDateIn = null,
      BigDecimal invoiceAmountIn = null,
      EmployeeEntity employeeIn,
      BigDecimal paidAmountIn = null,
      AccountPayableInvoiceStatusType statusTypeIn = null,
      LocalDate dueDateIn = null,
      Store locationIn = null
   ) {
      return AccountPayableInvoiceDataLoader.stream(numberIn, vendorIn, purchaseOrderIn, invoiceDateIn, invoiceAmountIn, employeeIn, paidAmountIn, statusTypeIn, dueDateIn, payToIn, locationIn)
         .map { accountPayableInvoiceRepository.insert(it, company) }
   }

   AccountPayableInvoiceEntity single(
      CompanyEntity company,
      VendorEntity vendorIn,
      PurchaseOrderEntity purchaseOrderIn = null,
      BigDecimal invoiceAmountIn = null,
      EmployeeEntity employeeIn,
      BigDecimal paidAmountIn = null,
      AccountPayableInvoiceStatusType statusTypeIn = null,
      VendorEntity payToIn,
      Store locationIn = null,
      BigDecimal discountTakenIn = null
   ) {
      return stream(1, company, vendorIn, purchaseOrderIn, invoiceAmountIn, employeeIn, paidAmountIn, statusTypeIn, payToIn, locationIn, discountTakenIn).findFirst().orElseThrow { new Exception("Unable to create Account Payable Invoice Entity") }
   }

   AccountPayableInvoiceDTO singleDTO(
      CompanyEntity company,
      VendorDTO vendorIn,
      PurchaseOrderDTO purchaseOrderIn = null,
      EmployeeValueObject employeeIn,
      VendorDTO payToIn,
      SimpleLegacyIdentifiableDTO locationIn = null
   ) {
      return AccountPayableInvoiceDataLoader.streamDTO(1, vendorIn, purchaseOrderIn, employeeIn, payToIn, locationIn).findFirst().orElseThrow { new Exception("Unable to create Account Payable Invoice") }
   }
}
