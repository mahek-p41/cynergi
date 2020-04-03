package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.math.RoundingMode
import java.math.RoundingMode.HALF_EVEN
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object VendorPaymentTermDataLoader {

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
            numberOfPayments = random.nextInt(1, 6),
            dueMonth1 = random.nextInt(1, 12),
            dueMonth2 = random.nextInt(1, 12),
            dueMonth3 = random.nextInt(1, 12),
            dueMonth4 = random.nextInt(1, 12),
            dueMonth5 = random.nextInt(1, 12),
            dueMonth6 = random.nextInt(1, 12),
            dueDays1 = random.nextInt(1, 30),
            dueDays2 = random.nextInt(1, 30),
            dueDays3 = random.nextInt(1, 30),
            dueDays4 = random.nextInt(1, 30),
            dueDays5 = random.nextInt(1, 30),
            dueDays6 = random.nextInt(1, 30),
            duePercent1 = random.nextDouble().toBigDecimal().setScale(4, HALF_EVEN),
            duePercent2 = random.nextDouble().toBigDecimal().setScale(4, HALF_EVEN),
            duePercent3 = random.nextDouble().toBigDecimal().setScale(4, HALF_EVEN),
            duePercent4 = random.nextDouble().toBigDecimal().setScale(4, HALF_EVEN),
            duePercent5 = random.nextDouble().toBigDecimal().setScale(4, HALF_EVEN),
            duePercent6 = random.nextDouble().toBigDecimal().setScale(4, HALF_EVEN),
            discountMonth = random.nextInt(1, 12),
            discountDays = random.nextInt(1, 30),
            discountPercent = random.nextDouble().toBigDecimal().setScale(2, HALF_EVEN)
         )
      }
   }

   @JvmStatic
   fun single(company: Company): VendorPaymentTermEntity {
      return stream(company = company).findFirst().orElseThrow { Exception("Unable to create VendorPaymentTermEntity") }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class VendorPaymentTermDataLoaderService @Inject constructor(
   private val vendorPaymentTermRepository: VendorPaymentTermRepository
) {

   fun stream(numberIn: Int = 1, company: Company): Stream<VendorPaymentTermEntity> {
      return VendorPaymentTermDataLoader.stream(numberIn, company).map {
         vendorPaymentTermRepository.insert(it)
      }
   }

   fun single(company: Company): VendorPaymentTermEntity {
      return stream(1, company).findFirst().orElseThrow { Exception("Unable to create VendorPaymentTerm")}
   }
}
