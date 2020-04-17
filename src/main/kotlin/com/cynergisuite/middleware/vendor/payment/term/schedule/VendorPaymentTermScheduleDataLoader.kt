package com.cynergisuite.middleware.vendor.payment.term.schedule

import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import com.cynergisuite.middleware.vendor.payment.term.schedule.infrastructure.VendorPaymentTermScheduleRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.math.RoundingMode.HALF_EVEN
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object VendorPaymentTermScheduleDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, vendorPaymentTerm: VendorPaymentTermEntity): Stream<VendorPaymentTermScheduleEntity> {
      val number = if (numberIn > 0) numberIn else return Stream.empty()
      val faker = Faker()
      val random = faker.random()

      //TODO Remember that this was asking for id because the entity had the ?, but not the = null!
      return IntStream.range(0, number).mapToObj {
         VendorPaymentTermScheduleEntity(
            dueMonth = null,
            dueDays = random.nextInt(1, 90),
            duePercent = random.nextDouble().toBigDecimal().setScale(2, HALF_EVEN),
            scheduleOrderNumber = random.nextInt(1, 6)
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class VendorPaymentTermScheduleDataLoaderService @Inject constructor(
   private val vendorPaymentTermScheduleRepository: VendorPaymentTermScheduleRepository
) {

}
