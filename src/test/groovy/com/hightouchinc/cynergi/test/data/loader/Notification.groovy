package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.Notification
import com.hightouchinc.cynergi.middleware.repository.NotificationRepository
import com.hightouchinc.cynergi.middleware.repository.NotificationTypeDomainRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class NotificationTestDataLoader {
   static Stream<Notification> stream(int number = 1) {
      final int value = number > 0 ? number : 1
      final def faker = new Faker()
      final def date = faker.date()
      final def lorem = faker.lorem()
      final def name = faker.name()

      return IntStream.range(0, value).mapToObj {
         new Notification(
            null,
            UUID.randomUUID(),
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            lorem.characters(6),
            date.future(1000, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            lorem.characters(1, 500),
            name.username(),
            date.future(10, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            NotificationTypeDomainTestDataLoader.stream(1).findFirst().orElseThrow { new Exception("Unable to load NotificationTypeDomain") }
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

   Stream<Notification> stream(int number = 1) {
      return NotificationTestDataLoader.stream(number)
         .filter { notificationTypeDomainRepository.findOne(it.notificationDomainType.id).basicEquality(it.notificationDomainType) } // filter out anything that doesn't match the hard coded values for the ID, value and description from the NotificationTypeDomainTestDataLoader
         .map { notificationsRepository.insert(it) }
   }
}
