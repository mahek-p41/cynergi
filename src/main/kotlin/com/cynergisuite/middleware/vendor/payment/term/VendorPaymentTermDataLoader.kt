package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
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
