package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.Notification
import com.hightouchinc.cynergi.middleware.entity.NotificationTypeDomain
import com.hightouchinc.cynergi.middleware.repository.NotificationRepository
import com.hightouchinc.cynergi.middleware.repository.NotificationTypeDomainRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class NotificationTestDataLoader {
   static Stream<Notification> stream(int number = 1, String company = "corrto", LocalDate startDateIn = null, LocalDate expirationDateIn = null, NotificationTypeDomain type = null, String sendingEmployee = null) {
      final int value = number > 0 ? number : 1
      final String companyId = company != null ? company : "corrto"
      final def faker = new Faker()
      final def date = faker.date()
      final def lorem = faker.lorem()
      final def name = faker.name()
      final def typeDomain = type ?: NotificationTypeDomainTestDataLoader.random()
      final Date startDate = startDateIn != null ? Date.from(startDateIn.atStartOfDay(ZoneId.systemDefault()).toInstant()) : date.future(5, TimeUnit.DAYS) // hopefully this isn't lossy
      final LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
      final LocalDate expirationLocalDate = expirationDateIn ?: date.future(100, TimeUnit.DAYS, startDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

      return IntStream.range(0, value).mapToObj {
         new Notification(
            startLocalDate,
            expirationLocalDate,
            lorem.characters(1, 500),
            sendingEmployee != null ? sendingEmployee : name.username(),
            companyId,
            typeDomain
         )
      }
   }
}

@Singleton
@CompileStatic
class NotificationDataLoaderService {
   private final NotificationRepository notificationsRepository
   private final NotificationTypeDomainRepository notificationTypeDomainRepository

   NotificationDataLoaderService(
      NotificationRepository notificationsRepository,
      NotificationTypeDomainRepository notificationTypeDomainRepository
   ) {
      this.notificationsRepository = notificationsRepository
      this.notificationTypeDomainRepository = notificationTypeDomainRepository
   }

   Stream<Notification> stream(int number = 1, String company = "corrto", LocalDate startDate = null, LocalDate expirationDate = null, NotificationTypeDomain type = null, String sendingEmployee = null) {
      return NotificationTestDataLoader.stream(number, company, startDate, expirationDate, type, sendingEmployee)
         .filter { notificationTypeDomainRepository.findOne(it.notificationDomainType.id).basicEquality(it.notificationDomainType) } // filter out anything that doesn't match the hard coded values for the ID, value and description from the NotificationTypeDomainTestDataLoader
         .map { notificationsRepository.insert(it) }
   }
}
