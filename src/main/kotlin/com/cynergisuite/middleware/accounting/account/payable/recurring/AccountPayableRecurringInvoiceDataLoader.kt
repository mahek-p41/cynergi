package com.cynergisuite.middleware.accounting.account.payable.recurring

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableRecurringInvoiceStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableRecurringInvoiceStatusTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.recurring.infrastructure.AccountPayableRecurringInvoiceRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.vendor.VendorEntity
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object AccountPayableRecurringInvoiceDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, vendor: VendorEntity, payTo: VendorEntity): Stream<AccountPayableRecurringInvoiceEntity> {
      val number = if (numberIn < 0) 1 else numberIn
      val faker = Faker()
      val random = faker.random()
      val lorem = faker.lorem()
      val numbers = faker.number()
      val date = faker.date()
      val lastTransferToCreateInvoiceDate = date.past(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         AccountPayableRecurringInvoiceEntity(
            vendor = vendor,
            invoice = lorem.characters(3, 20),
            invoiceAmount = numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            fixedAmountIndicator = random.nextBoolean(),
            employeeNumberId = random.nextInt(1, 1000),
            message = lorem.sentence(),
            codeIndicator = lorem.characters(3),
            type = "E",
            payTo = payTo,
            lastTransferToCreateInvoiceDate = lastTransferToCreateInvoiceDate,
            status = AccountPayableRecurringInvoiceStatusTypeDataLoader.random(),
            dueDays = random.nextInt(1, 100),
            automatedIndicator = random.nextBoolean(),
            separateCheckIndicator = random.nextBoolean(),
            expenseMonthCreationIndicator = ExpenseMonthCreationTypeDataLoader.random(),
            invoiceDay = random.nextInt(1, 100),
            expenseDay = random.nextInt(1, 100),
            schedule = null,
            lastCreatedInPeriod = lastTransferToCreateInvoiceDate.plusDays(random.nextInt(10).toLong()),
            nextCreationDate = lastTransferToCreateInvoiceDate.plusDays(random.nextInt(10, 30).toLong()),
            nextInvoiceDate = lastTransferToCreateInvoiceDate.plusDays(random.nextInt(30, 60).toLong()),
            nextExpenseDate = lastTransferToCreateInvoiceDate.plusDays(random.nextInt(60, 100).toLong())
         )
      }
   }

   @JvmStatic
   fun streamDTO(numberIn: Int = 1, vendor: VendorEntity, payTo: VendorEntity): Stream<AccountPayableRecurringInvoiceDTO> {
      val number = if (numberIn < 0) 1 else numberIn
      val faker = Faker()
      val random = faker.random()
      val lorem = faker.lorem()
      val numbers = faker.number()
      val date = faker.date()
      val lastTransferToCreateInvoiceDate = date.past(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         AccountPayableRecurringInvoiceDTO(
            vendor = SimpleIdentifiableDTO(vendor.id),
            invoice = lorem.characters(5, 20),
            invoiceAmount = numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            fixedAmountIndicator = random.nextBoolean(),
            employeeNumberId = random.nextInt(1, 1000),
            message = lorem.sentence(),
            codeIndicator = lorem.characters(1, 3),
            type = "E",
            payTo = SimpleIdentifiableDTO(payTo.id),
            lastTransferToCreateInvoiceDate = lastTransferToCreateInvoiceDate,
            status = AccountPayableRecurringInvoiceStatusTypeDTO(AccountPayableRecurringInvoiceStatusTypeDataLoader.random()),
            dueDays = random.nextInt(1, 100),
            automatedIndicator = random.nextBoolean(),
            separateCheckIndicator = random.nextBoolean(),
            expenseMonthCreationIndicator = ExpenseMonthCreationTypeDTO(ExpenseMonthCreationTypeDataLoader.random()),
            invoiceDay = random.nextInt(1, 100),
            expenseDay = random.nextInt(1, 100),
            schedule = null,
            lastCreatedInPeriod = lastTransferToCreateInvoiceDate.plusDays(random.nextInt(10).toLong()),
            nextCreationDate = lastTransferToCreateInvoiceDate.plusDays(random.nextInt(10, 30).toLong()),
            nextInvoiceDate = lastTransferToCreateInvoiceDate.plusDays(random.nextInt(30, 60).toLong()),
            nextExpenseDate = lastTransferToCreateInvoiceDate.plusDays(random.nextInt(60, 100).toLong())
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AccountPayableRecurringInvoiceDataLoaderService @Inject constructor(
   private val accountPayableRecurringInvoiceRepository: AccountPayableRecurringInvoiceRepository
) {

   fun stream(numberIn: Int = 1, company: Company, vendor: VendorEntity, payTo: VendorEntity): Stream<AccountPayableRecurringInvoiceEntity> {
      return AccountPayableRecurringInvoiceDataLoader.stream(numberIn, vendor, payTo)
         .map { accountPayableRecurringInvoiceRepository.insert(it, company) }
   }

   fun single(company: Company, vendor: VendorEntity, payTo: VendorEntity): AccountPayableRecurringInvoiceEntity {
      return stream(1, company, vendor, payTo).findFirst().orElseThrow { Exception("Unable to create Account Payable Recurring Invoice Entity") }
   }

   fun singleDTO(company: Company, vendor: VendorEntity, payTo: VendorEntity): AccountPayableRecurringInvoiceDTO {
      return AccountPayableRecurringInvoiceDataLoader.streamDTO(1, vendor, payTo).findFirst().orElseThrow { Exception("Unable to create Account Payable Recurring Invoice") }
   }
}
