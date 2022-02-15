package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleEntity
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleTestDataLoader
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

import static java.math.RoundingMode.HALF_EVEN

@CompileStatic
class VendorPaymentTermTestDataLoader {

   static Stream<VendorPaymentTermEntity> stream(int numberIn = 1, CompanyEntity company) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final lorem = faker.lorem()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new VendorPaymentTermEntity(
            null,
            company,
            lorem.characters(3, 30),
            random.nextInt(1, 12),
            random.nextInt(1, 30),
            random.nextDouble().toBigDecimal().setScale(2, HALF_EVEN),
            []
         )
      }
   }

   static VendorPaymentTermEntity single(CompanyEntity company) {
      return stream(company).findFirst().orElseThrow { new Exception("Unable to create VendorPaymentTermEntity") }
   }

   static VendorPaymentTermEntity singleWithVendorPaymentTermSchedule(CompanyEntity company, List<VendorPaymentTermScheduleEntity> vendorPaymentTermSchedules) {
      final lorem = new Faker().lorem()

      return new VendorPaymentTermEntity(
         null,
         company,
         lorem.characters(3, 30),
         null,
         null,
         null,
         []
      )
   }

   static VendorPaymentTermEntity singleWithSingle90DaysPayment(CompanyEntity company) {
      return singleWithVendorPaymentTermSchedule(company, VendorPaymentTermScheduleTestDataLoader.single90DaysPayment())
   }

   static VendorPaymentTermEntity singleWithTwoMonthPayments(CompanyEntity company) {
      return singleWithVendorPaymentTermSchedule(company, VendorPaymentTermScheduleTestDataLoader.twoMonthPayments())
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class VendorPaymentTermTestDataLoaderService {
   private final VendorPaymentTermRepository vendorPaymentTermRepository

   @Inject
   VendorPaymentTermTestDataLoaderService(VendorPaymentTermRepository vendorPaymentTermRepository) {
      this.vendorPaymentTermRepository = vendorPaymentTermRepository
   }

   Stream<VendorPaymentTermEntity> stream(int numberIn = 1, CompanyEntity company) {
      return VendorPaymentTermTestDataLoader.stream(numberIn, company).map {
         vendorPaymentTermRepository.insert(it)
      }
   }

   VendorPaymentTermEntity single(CompanyEntity company) {
      stream(1, company).findFirst().orElseThrow { new Exception("Unable to create VendorPaymentTerm") }
   }

   VendorPaymentTermEntity singleWithSingle90DaysPayment(CompanyEntity company) {
      vendorPaymentTermRepository.insert(VendorPaymentTermTestDataLoader.singleWithSingle90DaysPayment(company))
   }

   VendorPaymentTermEntity singleWithTwoMonthPayments(CompanyEntity company) {
      vendorPaymentTermRepository.insert(VendorPaymentTermTestDataLoader.singleWithTwoMonthPayments(company))
   }
}
