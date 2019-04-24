package com.cynergisuite.middleware.notification

import com.github.javafaker.Faker
import com.cynergisuite.middleware.notification.infrastructure.NotificationRecipientRepository
import io.micronaut.context.annotation.Requires

import javax.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject

object NotificationRecipientTestDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, notificationIn: Notification? = null): Stream<NotificationRecipient> {
      val number = if (numberIn > 0) numberIn else 1
      val notification = notificationIn ?: NotificationTestDataLoader.stream(1).findFirst().orElseThrow { Exception("Unable to create Notification") }
      val faker = Faker()
      val lorem = faker.lorem()
      val name = faker.name()

      return IntStream.range(0, number).mapToObj {
         NotificationRecipient(
            lorem.characters(0, 255),
            name.username(),
            notification
         )
      }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class NotificationRecipientDataLoaderService @Inject constructor(
   private val notificationRecipientRepository: NotificationRecipientRepository,
   private val notificationDataLoaderService: NotificationDataLoaderService

) {

   fun stream(numberIn: Int = 1): Stream<NotificationRecipient> {
      return stream(numberIn, null)
   }

   fun stream(numberIn: Int = 1, notificationIn: Notification? = null): Stream<NotificationRecipient> {
      val notification = notificationIn ?: notificationDataLoaderService.stream(1).findFirst().orElseThrow { Exception("Unable to create Notification") }

      return NotificationRecipientTestDataLoader.stream(numberIn, notification)
         .map {
            notificationRecipientRepository.insert(it)
         }
   }
}
