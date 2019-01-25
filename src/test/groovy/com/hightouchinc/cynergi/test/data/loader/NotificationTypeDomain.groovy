package com.hightouchinc.cynergi.test.data.loader

import com.hightouchinc.cynergi.middleware.entity.NotificationTypeDomain
import com.hightouchinc.cynergi.middleware.repository.NotificationTypeDomainRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.util.stream.Stream

@CompileStatic
class NotificationTypeDomainTestDataLoader {
   static Stream<NotificationTypeDomain> stream(int number = 1) {
      final int value = number > 0 || number <= 4 ? number : 1

      return Stream.of(
         new NotificationTypeDomain(1, "S", "Store"),
         new NotificationTypeDomain(2, "E", "Employee"),
         new NotificationTypeDomain(3, "D", "Department"),
         new NotificationTypeDomain(4, "A", "All")
      ).limit(value)
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
