package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentRepository
import com.cynergisuite.middleware.accounting.bank.BankEntity
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.vendor.VendorDTO
import com.cynergisuite.middleware.vendor.VendorEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

import java.time.LocalDate
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class AccountPayablePaymentTestDataLoader {

   static Stream<AccountPayablePaymentEntity> stream(
      int numberIn = 1,
      BankEntity bank,
      VendorEntity vendor,
      AccountPayablePaymentStatusType status,
      AccountPayablePaymentTypeType type,
      LocalDate paymentDateIn = null,
      LocalDate dateVoidedIn = null,
      BigDecimal amountIn = null,
      Boolean isCleared
   ) {
      final number = numberIn < 0 ? 1 : numberIn
      final faker = new Faker()
      final random = faker.random()
      final pmtNum = random.nextInt(1000)
      final paymentDate = paymentDateIn ? paymentDateIn : LocalDate.now().minusDays(random.nextLong(15))
      final amount = amountIn ? amountIn : random.nextInt(1000).toBigDecimal()

      return IntStream.range(0, number).mapToObj {
         new AccountPayablePaymentEntity(
            null,
            bank,
            vendor,
            status ?: AccountPayablePaymentStatusTypeDataLoader.random(),
            type ?: AccountPayablePaymentTypeTypeDataLoader.random(),
            paymentDate,
            isCleared ? LocalDate.now().minusDays(random.nextLong(15)) : null,
            dateVoidedIn,
            faker.lorem().characters(0,2) + random.nextInt(1000) + faker.lorem().characters(0, 2),
            amount,
            Set.of()
         )
      }
   }

   static Stream<AccountPayablePaymentDTO> streamDTO(
      int numberIn = 1,
      BankEntity bank,
      VendorEntity vendor
   ) {
      final number = numberIn < 0 ? 1 : numberIn
      final faker = new Faker()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new AccountPayablePaymentDTO(
            null,
            new SimpleIdentifiableDTO(bank),
            new VendorDTO(vendor),
            new AccountPayablePaymentStatusTypeDTO(AccountPayablePaymentStatusTypeDataLoader.random()),
            new AccountPayablePaymentTypeTypeDTO(AccountPayablePaymentTypeTypeDataLoader.random()),
            LocalDate.now().minusDays(random.nextLong(60)),
            LocalDate.now().minusDays(random.nextLong(60)),
            LocalDate.now().minusDays(random.nextLong(60)),
            random.nextInt(1000).toString(),
            random.nextInt(10000).toBigDecimal(),
            Set.of()
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AccountPayablePaymentDataLoaderService {
   private final AccountPayablePaymentRepository apPaymentRepository

   AccountPayablePaymentDataLoaderService(AccountPayablePaymentRepository apPaymentRepository) {
      this.apPaymentRepository = apPaymentRepository
   }

   Stream<AccountPayablePaymentEntity> stream(
      int numberIn = 1,
      CompanyEntity company,
      BankEntity bank,
      VendorEntity vendor,
      AccountPayablePaymentStatusType status = null,
      AccountPayablePaymentTypeType type = null,
      LocalDate paymentDateIn = null,
      LocalDate dateVoidedIn = null,
      BigDecimal amountIn = null,
      Boolean isCleared = false
   ) {
      return AccountPayablePaymentTestDataLoader.stream(numberIn, bank, vendor, status, type, paymentDateIn, dateVoidedIn, amountIn, isCleared)
         .map { apPaymentRepository.insert(it, company) }
   }

   AccountPayablePaymentEntity single(
      CompanyEntity company,
      BankEntity bank,
      VendorEntity vendor
   ) {
      return stream(1, company, bank, vendor).findFirst().orElseThrow { new Exception("Unable to create Account Payable Payment") }
   }

   AccountPayablePaymentEntity single(
      CompanyEntity company,
      BankEntity bank,
      VendorEntity vendor,
      AccountPayablePaymentStatusType status,
      AccountPayablePaymentTypeType type,
      LocalDate paymentDateIn = null,
      LocalDate dateVoidedIn = null,
      BigDecimal amountIn = null
   ) {
      return stream(1, company, bank, vendor, status, type, paymentDateIn, dateVoidedIn, amountIn).findFirst().orElseThrow { new Exception("Unable to create Account Payable Payment") }
   }

   AccountPayablePaymentDTO singleDTO(
      BankEntity bank,
      VendorEntity vendor
   ) {
      return AccountPayablePaymentTestDataLoader.streamDTO(1, bank, vendor).findFirst().orElseThrow { new Exception("Unable to create Account Payable Payment") }
   }
}
