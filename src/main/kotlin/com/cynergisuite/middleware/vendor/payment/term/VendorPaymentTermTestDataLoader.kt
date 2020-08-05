package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleEntity
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleTestDataLoader
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.math.RoundingMode
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object VendorPaymentTermTestDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, company: Company): Stream<VendorPaymentTermEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()
      val random = faker.random()

      return IntStream.range(0, number).mapToObj {
         VendorPaymentTermEntity(
            company = company,
            description = lorem.characters(3, 30),
            discountMonth = random.nextInt(1, 12),
            discountDays = random.nextInt(1, 30),
            discountPercent = random.nextDouble().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
         )
      }
   }

   @JvmStatic
   fun single(company: Company): VendorPaymentTermEntity {
      return stream(company = company).findFirst().orElseThrow { Exception("Unable to create VendorPaymentTermEntity") }
   }

   @JvmStatic
   fun singleWithVendorPaymentTermSchedule(company: Company, vendorPaymentTermSchedules: MutableList<VendorPaymentTermScheduleEntity>): VendorPaymentTermEntity {
      val lorem = Faker().lorem()

      return VendorPaymentTermEntity(
         id = null,
         company = company,
         description = lorem.characters(2, 30),
         discountMonth = null,
         discountDays = null,
         discountPercent = null,
         scheduleRecords = vendorPaymentTermSchedules
      )
   }

   @JvmStatic
   fun singleWithSingle90DaysPayment(company: Company): VendorPaymentTermEntity {
      return singleWithVendorPaymentTermSchedule(company, VendorPaymentTermScheduleTestDataLoader.single90DaysPayment())
   }

   @JvmStatic
   fun singleWithTwoMonthPayments(company: Company): VendorPaymentTermEntity {
      return singleWithVendorPaymentTermSchedule(company, VendorPaymentTermScheduleTestDataLoader.twoMonthPayments())
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class VendorPaymentTermTestDataLoaderService @Inject constructor(
   private val vendorPaymentTermRepository: VendorPaymentTermRepository
) {

   fun stream(numberIn: Int = 1, company: Company): Stream<VendorPaymentTermEntity> {
      return VendorPaymentTermTestDataLoader.stream(numberIn, company).map {
         vendorPaymentTermRepository.insert(it)
      }
   }

   fun single(company: Company): VendorPaymentTermEntity =
      stream(1, company).findFirst().orElseThrow { Exception("Unable to create VendorPaymentTerm") }

   fun singleWithSingle90DaysPayment(company: Company): VendorPaymentTermEntity =
      vendorPaymentTermRepository.insert(VendorPaymentTermTestDataLoader.singleWithSingle90DaysPayment(company))

   fun singleWithTwoMonthPayments(company: Company): VendorPaymentTermEntity =
      vendorPaymentTermRepository.insert(VendorPaymentTermTestDataLoader.singleWithTwoMonthPayments(company))
}
