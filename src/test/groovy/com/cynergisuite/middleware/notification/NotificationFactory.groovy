package com.cynergisuite.middleware.notification

import com.cynergisuite.middleware.notification.infrastructure.NotificationRepository
import com.cynergisuite.middleware.notification.infrastructure.NotificationTypeDomainRepository
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import java.util.stream.Stream
import jakarta.inject.Inject
import jakarta.inject.Singleton

@CompileStatic
class NotificationTestDataLoader {

   static Stream<Notification> stream(int numberIn = 1, String company = "testco", LocalDate startDateIn = null, LocalDate expirationDateIn = null, NotificationType type = null, String sendingEmployee = null) {
      final number = numberIn > 0 ? numberIn : 1
      final companyId = company ?: "testco"
      final faker = new Faker()
      final date = faker.date()
      final lorem = faker.lorem()
      final name = faker.name()
      final typeDomain = type ?: NotificationTypeDomainTestDataLoader.random()
      final startDate = startDateIn != null ? Date.from(startDateIn.atStartOfDay(ZoneId.systemDefault()).toInstant()) : date.future(5, TimeUnit.DAYS) // hopefully this isn't lossy
      final startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
      final expirationLocalDate = expirationDateIn ?: date.future(100, TimeUnit.DAYS, startDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         new Notification(
            startLocalDate,
            expirationLocalDate,
            lorem.characters(1, 500),
            sendingEmployee ?: name.username(),
            companyId,
            typeDomain
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class NotificationDataLoaderService {
   private final NotificationRepository notificationsRepository
   private final NotificationTypeDomainRepository notificationTypeDomainRepository

   NotificationDataLoaderService(NotificationRepository notificationsRepository, NotificationTypeDomainRepository notificationTypeDomainRepository) {
      this.notificationsRepository = notificationsRepository
      this.notificationTypeDomainRepository = notificationTypeDomainRepository
   }

   Stream<Notification> stream(int numberIn = 1, String company = "testco", LocalDate  startDate = null, LocalDate expirationDate = null, NotificationType type = null, String sendingEmployee = null) {
      return NotificationTestDataLoader.stream(numberIn, company, startDate, expirationDate, type, sendingEmployee)
         .filter { notificationTypeDomainRepository.findOne(it.notificationDomainType.id).equals(it.notificationDomainType) } // filter out anything that doesn't match the hard coded values for the ID, value and description from the NotificationTypeDomainTestDataLoader
         .map { notificationsRepository.insert(it) }
   }

   Notification single() {
      return stream(1).findFirst().orElseThrow { new Exception("Unable to create Notification") }
   }
}
