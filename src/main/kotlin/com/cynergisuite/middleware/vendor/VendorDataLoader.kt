package com.cynergisuite.middleware.vendor

import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeFactory
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.StoreFactory
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object VendorFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, changedByIn: EmployeeEntity? = null): Stream<VendorEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val changedBy = changedByIn ?: EmployeeFactory.testEmployee()
      val faker = Faker()
      val company = faker.company()
      val address = faker.address()
      val lorem = faker.lorem()
      val numbers = faker.idNumber()
      val random = faker.random()
      val fakeDate = faker.date()

      //TODO Need to pull in a real/random vendor_terms for paymentTerms.
      //TODO And the same for shipVia and address ids. How?
      //TODO faker for dates and boolean?
      return IntStream.range(0, number).mapToObj {
         VendorEntity(
            companyId = ,
            number = random.nextInt(1,1000),
            nameKey = company.name(),
            address = ,
            ourAccountNumber = random.nextInt(1,1000),
            payTo = random.nextInt(1,100),
            freightOnBoardTypeId = lorem.characters(1).toUpperCase(),
            paymentTerms = ,
            floatDays = random.nextInt(1,180),
            normalDays = random.nextInt(1,180),
            returnPolicy = lorem.characters(1).toUpperCase(),
            shipViaId = ,
            vendGroup = lorem.characters(8).toUpperCase(),
            shutdownFrom = fakeDate.between(),
            shutdownThru = ,
            minimumQuantity = random.nextInt(1,500),
            minimumAmount = ,
            freeShipQuantity = random.nextInt(500,2000),
            freeShipAmount = ,
            vendor1099 = ,
            federalIdNumber = lorem.characters(12).toUpperCase(),
            salesRepName = lorem.characters(20).toUpperCase(),
            salesRepFax = ,
            separateCheck = ,
            bumpPercent = ,
            freightCalcMethodType = lorem.characters(1).toUpperCase(),
            freightPercent = ,
            freightAmount = ,
            chargeInvTax1 = ,
            chargeInvTax2 = ,
            chargeInvTax3 = ,
            chargeInvTax4 = ,
            federalIdNumberVerification = ,
            emailAddress =
         )
      }
   }

   @JvmStatic
   fun single(): VendorEntity =
      single(storeIn = StoreFactory.random())

   @JvmStatic
   fun single(storeIn: StoreEntity): VendorEntity =
      stream(1, storeIn).findFirst().orElseThrow { Exception("Unable to create Audit") }
}

@Singleton
@Requires(env = ["develop", "test"])
class AuditFactoryService @Inject constructor(
   private val vendorRepository: VendorRepository,
   private val employeeFactoryService: EmployeeFactoryService
) {

   fun stream(numberIn: Int = 1, storeIn: StoreEntity? = null): Stream<VendorEntity> =
      stream(numberIn, storeIn, null, null)

   fun stream(numberIn: Int = 1, storeIn: StoreEntity? = null, changedByIn: EmployeeEntity? = null, statusesIn: Set<AuditStatus>?): Stream<AuditEntity> {
      val changedBy = changedByIn ?: employeeFactoryService.single(datasetIn = store.dataset)

      return VendorFactory.stream(numberIn, changedBy)
         .map {
            vendorRepository.insert(it)
         }
   }

   fun generate(numberIn: Int = 1, storeIn: StoreEntity? = null, changedByIn: EmployeeEntity? = null, statusesIn: Set<AuditStatus>?) {
      stream(numberIn, storeIn, changedByIn, statusesIn).forEach {  } // exercise the stream with the terminal forEach
   }

   fun single(): VendorEntity =
      single(storeIn = null)

   fun single(storeIn: StoreEntity? = null): VendorEntity =
      stream(storeIn = storeIn).findFirst().orElseThrow { Exception("Unable to create Vendor") }

   fun single(storeIn: StoreEntity? = null, changedByIn: EmployeeEntity? = null): VendorEntity =
      single(storeIn = storeIn, changedByIn = changedByIn, statusesIn = null)

   fun single(storeIn: StoreEntity? = null, changedByIn: EmployeeEntity?, statusesIn: Set<AuditStatus>?): VendorEntity =
      stream(storeIn = storeIn, statusesIn = statusesIn).findFirst().orElseThrow { Exception("Unable to create Vendor") }

   fun single(statusesIn: Set<AuditStatus>?): VendorEntity =
      stream(1, null, null, statusesIn).findFirst().orElseThrow { Exception("Unable to create Vendor") }
}
