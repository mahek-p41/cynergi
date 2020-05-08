package com.cynergisuite.middleware.vendor

import com.cynergisuite.middleware.company.Company
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

object VendorDataLoader {

   /*
   @JvmStatic
   fun stream(numberIn: Int = 1, company: Company): Stream<VendorEntity> {
      val number = if (numberIn > 0) numberIn else 1
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
            company = company,
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
   fun single(company: Company): VendorEntity {
      return stream(company = company).findFirst().orElseThrow { Exception("Unable to create VendorEntity") }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class VendorDataLoaderService @Inject constructor(
   private val vendorRepository: VendorRepository
) {

   fun stream(numberIn: Int = 1, company: Company): Stream<VendorEntity> {
      return VendorDataLoader.stream(numberIn, company).map {
         vendorRepository.insert(it)
      }
   }

   fun single(company: Company): VendorEntity {
      return stream(1, company).findFirst().orElseThrow { Exception("Unable to create Vendor")}
   }

    */
}
