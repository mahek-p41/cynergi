package com.cynergisuite.middleware.notification

import com.cynergisuite.middleware.notification.infrastructure.NotificationTypeDomainRepository
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Inject
import jakarta.inject.Singleton

import java.util.stream.Stream

class NotificationTypeDomainTestDataLoader {
   private static final List<NotificationType> typeDomainValues = [
      Store.INSTANCE,
      Employee.INSTANCE,
      Department.INSTANCE,
      All.INSTANCE
   ]

   static List<NotificationType> values() {
      return typeDomainValues
   }

   static NotificationType random() {
      return typeDomainValues.random()
   }

   static Stream<NotificationType> stream(int number = 1) {
      final notificationTypes = typeDomainValues
      final value = number > 0 || number <= values().size() ? number.toLong() : 1

      return notificationTypes.stream().limit(value)
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class NotificationTypeDomainDataLoaderService {
   private final NotificationTypeDomainRepository notificationTypeDomainRepository

   @Inject
   NotificationTypeDomainDataLoaderService(NotificationTypeDomainRepository notificationTypeDomainRepository) {
      this.notificationTypeDomainRepository = notificationTypeDomainRepository
   }

   Stream<NotificationType> stream(int numberIn = 1) {
      return NotificationTypeDomainTestDataLoader.stream(numberIn)
         .map { notificationTypeDomainRepository.findOne(it.id) } // since these are already saved in the database via a migration script just take the hard coded ones and look them up
         .filter { it != null } // filter any that weren't found
   }
}
