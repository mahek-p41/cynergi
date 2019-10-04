package com.cynergisuite.middleware.notification

import com.cynergisuite.middleware.notification.infrastructure.NotificationRepository
import com.cynergisuite.middleware.notification.infrastructure.NotificationTypeDomainRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.lang.Exception
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object NotificationTestDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, company: String? = "testco", startDateIn: LocalDate?  = null, expirationDateIn: LocalDate? = null, type: NotificationTypeDomain? = null, sendingEmployee: String? = null): Stream<Notification> {
      val number = if (numberIn > 0) numberIn else 1
      val companyId = company ?: "testco"
      val faker = Faker()
      val date = faker.date()
      val lorem = faker.lorem()
      val name = faker.name()
      val typeDomain = type ?: NotificationTypeDomainTestDataLoader.random()
      val startDate = if (startDateIn != null) Date.from(startDateIn.atStartOfDay(ZoneId.systemDefault()).toInstant()) else date.future(5, TimeUnit.DAYS) // hopefully this isn't lossy
      val startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
      val expirationLocalDate = expirationDateIn ?: date.future(100, TimeUnit.DAYS, startDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         Notification(
            startDate = startLocalDate,
            expirationDate = expirationLocalDate,
            message = lorem.characters(1, 500),
            sendingEmployee = sendingEmployee ?: name.username(),
            company = companyId,
            notificationDomainType = typeDomain
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class NotificationDataLoaderService @Inject constructor(
   private val notificationsRepository: NotificationRepository,
   private val notificationTypeDomainRepository: NotificationTypeDomainRepository
) {

   fun stream(numberIn: Int = 1): Stream<Notification> {
      return stream(numberIn, "testco", null, null, null, null)
   }

   fun stream(numberIn: Int = 1, company: String  = "testco", startDate: LocalDate? = null, expirationDate: LocalDate? = null, type: NotificationTypeDomain? = null, sendingEmployee: String? = null): Stream<Notification> {
      return NotificationTestDataLoader.stream(numberIn, company, startDate, expirationDate, type, sendingEmployee)
         .filter { notificationTypeDomainRepository.findOne(it.notificationDomainType.id)!!.basicEquality(it.notificationDomainType) } // filter out anything that doesn't match the hard coded values for the ID, value and description from the NotificationTypeDomainTestDataLoader
         .map { notificationsRepository.insert(it) }
   }

   fun single(): Notification {
      return stream(1).findFirst().orElseThrow { Exception("Unable to create Notification") }
   }
}
