package com.cynergisuite.test.data.loader

import com.github.javafaker.Faker
import com.cynergisuite.middleware.entity.Notification
import com.cynergisuite.middleware.entity.NotificationRecipient
import com.cynergisuite.middleware.repository.NotificationRecipientRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class NotificationRecipientTestDataLoader {
   static Stream<NotificationRecipient> stream(int number = 1, Notification notificationIn = null) {
      final int value = number > 0 ? number : 1
      final def notification = notificationIn ?: NotificationTestDataLoader.stream(1).findFirst().orElseThrow { new Exception("Unable to create Notification") }
      final def faker = new Faker()
      final def lorem = faker.lorem()
      final def name = faker.name()

      return IntStream.range(0, value).mapToObj {
         new NotificationRecipient(
            lorem.characters(0, 255),
            name.username(),
            notification
         )
      }
   }
}

@Singleton
@CompileStatic
class NotificationRecipientDataLoaderService {
   private final NotificationRecipientRepository notificationRecipientRepository
   private final NotificationDataLoaderService notificationDataLoaderService

   NotificationRecipientDataLoaderService(
      NotificationRecipientRepository notificationRecipientRepository,
      NotificationDataLoaderService notificationDataLoaderService
   ) {
      this.notificationRecipientRepository = notificationRecipientRepository
      this.notificationDataLoaderService = notificationDataLoaderService
   }

   Stream<NotificationRecipient> stream(int number = 1, Notification notificationIn = null) {
      final def notification = notificationIn != null ? notificationIn : notificationDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Notification") }

      return NotificationRecipientTestDataLoader.stream(number, notification)
         .map {
            notificationRecipientRepository.insert(it)
         }
   }
}
