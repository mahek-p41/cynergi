package com.cynergisuite.middleware.notification

import com.cynergisuite.middleware.notification.infrastructure.NotificationRecipientRepository
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Inject
import jakarta.inject.Singleton

import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class NotificationRecipientTestDataLoader {

   static Stream<NotificationRecipient> stream(int numberIn = 1, Notification notificationIn = null) {
      final number = numberIn > 0 ? numberIn : 1
      final notification = notificationIn ?: NotificationTestDataLoader.stream(1).findFirst().orElseThrow { new Exception("Unable to create Notification") }
      final faker = new Faker()
      final lorem = faker.lorem()
      final name = faker.name()

      return IntStream.range(0, number).mapToObj {
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
@Requires(env = ["develop", "test"])
class NotificationRecipientDataLoaderService {
   private final NotificationRecipientRepository notificationRecipientRepository
   private final NotificationDataLoaderService notificationDataLoaderService

   @Inject
   NotificationRecipientDataLoaderService(NotificationRecipientRepository notificationRecipientRepository, NotificationDataLoaderService notificationDataLoaderService) {
      this.notificationRecipientRepository = notificationRecipientRepository
      this.notificationDataLoaderService = notificationDataLoaderService
   }

   Stream<NotificationRecipient> stream(int numberIn = 1, Notification notificationIn = null) {
      final notification = notificationIn ?: notificationDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Notification") }

      return NotificationRecipientTestDataLoader.stream(numberIn, notification)
         .map {
            notificationRecipientRepository.insert(it)
         }
   }
}
