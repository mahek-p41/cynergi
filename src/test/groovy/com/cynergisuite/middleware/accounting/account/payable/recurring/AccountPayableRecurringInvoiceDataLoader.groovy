package com.cynergisuite.middleware.accounting.account.payable.recurring

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableRecurringInvoiceStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableRecurringInvoiceStatusTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.recurring.infrastructure.AccountPayableRecurringInvoiceRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.vendor.VendorEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Singleton

@CompileStatic
class AccountPayableRecurringInvoiceDataLoader {

   static Stream<AccountPayableRecurringInvoiceEntity> stream(int numberIn = 1, VendorEntity vendor, VendorEntity payTo) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final random = faker.random()
      final lorem = faker.lorem()
      final numbers = faker.number()
      final date = faker.date()
      final lastTransferToCreateInvoiceDate = date.past(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         new AccountPayableRecurringInvoiceEntity(
            null,
            vendor,
            lorem.characters(3, 20),
            numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            random.nextBoolean(),
            random.nextInt(1, 1000),
            lorem.sentence(),
            lorem.characters(3),
            "E",
            payTo,
            lastTransferToCreateInvoiceDate,
            AccountPayableRecurringInvoiceStatusTypeDataLoader.random(),
            random.nextInt(1, 100),
            random.nextBoolean(),
            random.nextBoolean(),
            ExpenseMonthCreationTypeDataLoader.random(),
            random.nextInt(1, 100),
            random.nextInt(1, 100),
            null,
            lastTransferToCreateInvoiceDate.plusDays(random.nextInt(10).toLong()),
            lastTransferToCreateInvoiceDate.plusDays(random.nextInt(10, 30).toLong()),
            lastTransferToCreateInvoiceDate.plusDays(random.nextInt(30, 60).toLong()),
            lastTransferToCreateInvoiceDate.plusDays(random.nextInt(60, 100).toLong())
         )
      }
   }

   static Stream<AccountPayableRecurringInvoiceDTO> streamDTO(int numberIn = 1, VendorEntity vendor, VendorEntity payTo) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final random = faker.random()
      final lorem = faker.lorem()
      final numbers = faker.number()
      final date = faker.date()
      final lastTransferToCreateInvoiceDate = date.past(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         new AccountPayableRecurringInvoiceDTO(
            null,
            new SimpleIdentifiableDTO(vendor.id),
            lorem.characters(5, 20),
            numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            random.nextBoolean(),
            random.nextInt(1, 1000),
            lorem.sentence(),
            lorem.characters(1, 3),
            "E",
            new SimpleIdentifiableDTO(payTo.id),
            lastTransferToCreateInvoiceDate,
            new AccountPayableRecurringInvoiceStatusTypeDTO(AccountPayableRecurringInvoiceStatusTypeDataLoader.random()),
            random.nextInt(1, 100),
            random.nextBoolean(),
            random.nextBoolean(),
            new ExpenseMonthCreationTypeDTO(ExpenseMonthCreationTypeDataLoader.random()),
            random.nextInt(1, 100),
            random.nextInt(1, 100),
            null,
            lastTransferToCreateInvoiceDate.plusDays(random.nextInt(10).toLong()),
            lastTransferToCreateInvoiceDate.plusDays(random.nextInt(10, 30).toLong()),
            lastTransferToCreateInvoiceDate.plusDays(random.nextInt(30, 60).toLong()),
            lastTransferToCreateInvoiceDate.plusDays(random.nextInt(60, 100).toLong())
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AccountPayableRecurringInvoiceDataLoaderService {
   private final AccountPayableRecurringInvoiceRepository accountPayableRecurringInvoiceRepository

   AccountPayableRecurringInvoiceDataLoaderService(AccountPayableRecurringInvoiceRepository accountPayableRecurringInvoiceRepository) {
      this.accountPayableRecurringInvoiceRepository = accountPayableRecurringInvoiceRepository
   }

   Stream<AccountPayableRecurringInvoiceEntity> stream(int numberIn = 1, CompanyEntity company, VendorEntity vendor, VendorEntity payTo) {
      return AccountPayableRecurringInvoiceDataLoader.stream(numberIn, vendor, payTo)
         .map { accountPayableRecurringInvoiceRepository.insert(it, company) }
   }

   AccountPayableRecurringInvoiceEntity single(CompanyEntity company, VendorEntity vendor, VendorEntity payTo) {
      return stream(1, company, vendor, payTo).findFirst().orElseThrow { new Exception("Unable to create Account Payable Recurring Invoice Entity") }
   }

   AccountPayableRecurringInvoiceDTO singleDTO(VendorEntity vendor, VendorEntity payTo) {
      return AccountPayableRecurringInvoiceDataLoader.streamDTO(1, vendor, payTo).findFirst().orElseThrow { new Exception("Unable to create Account Payable Recurring Invoice") }
   }
}
