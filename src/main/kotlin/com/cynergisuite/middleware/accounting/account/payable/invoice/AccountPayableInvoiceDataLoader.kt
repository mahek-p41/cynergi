package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceSelectedTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceSelectedTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.purchase.order.PurchaseOrderEntity
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.vendor.VendorEntity
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object AccountPayableInvoiceDataLoader {

   @JvmStatic
   fun stream(
      numberIn: Int = 1,
      vendorIn: VendorEntity,
      purchaseOrderIn: PurchaseOrderEntity? = null,
      employeeIn: EmployeeEntity,
      payToIn: VendorEntity,
      locationIn: Store? = null
   ): Stream<AccountPayableInvoiceEntity> {
      val number = if (numberIn < 0) 1 else numberIn
      val faker = Faker()
      val random = faker.random()
      val lorem = faker.lorem()
      val numbers = faker.number()

      return IntStream.range(0, number).mapToObj {
         AccountPayableInvoiceEntity(
            vendor = vendorIn,
            invoice = lorem.characters(3, 20),
            purchaseOrder = purchaseOrderIn?.let { it -> SimpleIdentifiableEntity(it) },
            invoiceDate = LocalDate.now(),
            invoiceAmount = numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            discountAmount = numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            discountPercent = random.nextInt(1, 100).toBigDecimal().divide(BigDecimal(100)).setScale(7, RoundingMode.HALF_EVEN),
            autoDistributionApplied = random.nextBoolean(),
            discountTaken = numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            entryDate = LocalDate.now(),
            expenseDate = LocalDate.now(),
            discountDate = LocalDate.now(),
            employee = employeeIn,
            originalInvoiceAmount = numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            message = lorem.sentence(),
            selected = AccountPayableInvoiceSelectedTypeDataLoader.random(),
            multiplePaymentIndicator = random.nextBoolean(),
            paidAmount = numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            selectedAmount = numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            type = AccountPayableInvoiceTypeDataLoader.random(),
            status = AccountPayableInvoiceStatusTypeDataLoader.random(),
            dueDate = LocalDate.now(),
            payTo = SimpleIdentifiableEntity(payToIn),
            separateCheckIndicator = random.nextBoolean(),
            useTaxIndicator = random.nextBoolean(),
            receiveDate = LocalDate.now(),
            location = locationIn?.myId()?.let { SimpleLegacyIdentifiableDTO(it) }
         )
      }
   }

   @JvmStatic
   fun streamDTO(
      numberIn: Int = 1,
      vendorIn: SimpleIdentifiableDTO,
      purchaseOrderIn: SimpleIdentifiableDTO? = null,
      employeeIn: EmployeeValueObject,
      payToIn: SimpleIdentifiableDTO,
      locationIn: SimpleLegacyIdentifiableDTO? = null
   ): Stream<AccountPayableInvoiceDTO> {
      val number = if (numberIn < 0) 1 else numberIn
      val faker = Faker()
      val random = faker.random()
      val lorem = faker.lorem()
      val numbers = faker.number()

      return IntStream.range(0, number).mapToObj {
         AccountPayableInvoiceDTO(
            vendor = vendorIn,
            invoice = lorem.characters(3, 20),
            purchaseOrder = purchaseOrderIn,
            invoiceDate = LocalDate.now(),
            invoiceAmount = numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            discountAmount = numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            discountPercent = random.nextInt(1, 100).toBigDecimal().divide(BigDecimal(100)).setScale(7, RoundingMode.HALF_EVEN),
            autoDistributionApplied = random.nextBoolean(),
            discountTaken = numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            entryDate = LocalDate.now(),
            expenseDate = LocalDate.now(),
            discountDate = LocalDate.now(),
            employee = employeeIn,
            originalInvoiceAmount = numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            message = lorem.sentence(),
            selected = AccountPayableInvoiceSelectedTypeDTO(AccountPayableInvoiceSelectedTypeDataLoader.random()),
            multiplePaymentIndicator = random.nextBoolean(),
            paidAmount = numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            selectedAmount = numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            type = AccountPayableInvoiceTypeDTO(AccountPayableInvoiceTypeDataLoader.random()),
            status = AccountPayableInvoiceStatusTypeDTO(AccountPayableInvoiceStatusTypeDataLoader.random()),
            dueDate = LocalDate.now(),
            payTo = payToIn,
            separateCheckIndicator = random.nextBoolean(),
            useTaxIndicator = random.nextBoolean(),
            receiveDate = LocalDate.now(),
            location = locationIn
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AccountPayableInvoiceDataLoaderService @Inject constructor(
   private val accountPayableInvoiceRepository: AccountPayableInvoiceRepository
) {

   fun stream(
      numberIn: Int = 1,
      company: Company,
      vendorIn: VendorEntity,
      purchaseOrderIn: PurchaseOrderEntity? = null,
      employeeIn: EmployeeEntity,
      payToIn: VendorEntity,
      locationIn: Store? = null
   ): Stream<AccountPayableInvoiceEntity> {
      return AccountPayableInvoiceDataLoader.stream(numberIn, vendorIn, purchaseOrderIn, employeeIn, payToIn, locationIn)
         .map { accountPayableInvoiceRepository.insert(it, company) }
   }

   fun single(
      company: Company,
      vendorIn: VendorEntity,
      purchaseOrderIn: PurchaseOrderEntity? = null,
      employeeIn: EmployeeEntity,
      payToIn: VendorEntity,
      locationIn: Store? = null
   ): AccountPayableInvoiceEntity {
      return stream(1, company, vendorIn, purchaseOrderIn, employeeIn, payToIn, locationIn).findFirst().orElseThrow { Exception("Unable to create Account Payable Invoice Entity") }
   }

   fun singleDTO(
      company: Company,
      vendorIn: SimpleIdentifiableDTO,
      purchaseOrderIn: SimpleIdentifiableDTO? = null,
      employeeIn: EmployeeValueObject,
      payToIn: SimpleIdentifiableDTO,
      locationIn: SimpleLegacyIdentifiableDTO? = null
   ): AccountPayableInvoiceDTO {
      return AccountPayableInvoiceDataLoader.streamDTO(1, vendorIn, purchaseOrderIn, employeeIn, payToIn, locationIn).findFirst().orElseThrow { Exception("Unable to create Account Payable Invoice") }
   }
}
