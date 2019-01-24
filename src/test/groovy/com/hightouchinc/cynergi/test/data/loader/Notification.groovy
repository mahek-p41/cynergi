package com.hightouchinc.cynergi.test.data.loader

import com.hightouchinc.cynergi.middleware.entity.Notification
import com.hightouchinc.cynergi.middleware.repository.NotificationRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class NotificationTestDataLoader {
   static Stream<Notification> stream(int number = 1) {
      final int value = number > 0 ? number : 1

      return IntStream.range(0, value).mapToObj {
         new Notification(
            null,
            UUID.randomUUID(),
            OffsetDateTime.now(),
            OffsetDateTime.now()
         )
      }
   }
}

@Singleton
@CompileStatic
class NotificationDataLoaderService {
   private final NotificationRepository notificationsRepository

   NotificationDataLoaderService(NotificationRepository notificationsRepository) {
      this.notificationsRepository = notificationsRepository
   }

   Stream<Notification> stream(int number = 1) {
      return NotificationTestDataLoader.stream(number)
         .map {
            notificationsRepository.insert(it)
         }
   }
}
