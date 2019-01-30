package com.hightouchinc.cynergi.test.data.loader

import com.hightouchinc.cynergi.middleware.entity.NotificationTypeDomain
import com.hightouchinc.cynergi.middleware.repository.NotificationTypeDomainRepository
import groovy.transform.CompileStatic
import org.apache.commons.lang3.RandomUtils

import javax.inject.Singleton
import java.util.stream.Stream

import static java.util.Collections.unmodifiableList

@CompileStatic
class NotificationTypeDomainTestDataLoader {
   private static final List<NotificationTypeDomain> typeDomainValues = unmodifiableList(
      [
         new NotificationTypeDomain(1, "S", "Store"),
         new NotificationTypeDomain(2, "E", "Employee"),
         new NotificationTypeDomain(3, "D", "Department"),
         new NotificationTypeDomain(4, "A", "All")
      ]
   )

   static List<NotificationTypeDomain> values() {
      return typeDomainValues
   }

   static NotificationTypeDomain random() {
      return typeDomainValues[RandomUtils.nextInt(0, typeDomainValues.size())]
   }

   static Stream<NotificationTypeDomain> stream(int number = 1) {
      final notificationTypes = typeDomainValues
      final int value = number > 0 || number <= values().size() ? number : 1

      return notificationTypes.stream().limit(value)
   }
}

@Singleton
@CompileStatic
class NotificationTypeDomainDataLoaderService {
   private final NotificationTypeDomainRepository notificationTypeDomainRepository

   NotificationTypeDomainDataLoaderService(NotificationTypeDomainRepository notificationTypeDomainRepository) {
      this.notificationTypeDomainRepository = notificationTypeDomainRepository
   }

   Stream<NotificationTypeDomain> stream(int number = 1) {
      NotificationTypeDomainTestDataLoader.stream(number)
         .map { notificationTypeDomainRepository.findOne(it.id) } // since these are already saved in the database via a migration script just take the hard coded ones and look them up
         .filter { it != null } // filter any that weren't found
   }
}
