package com.cynergisuite.middleware.notification

import com.cynergisuite.middleware.notification.infrastructure.NotificationTypeDomainRepository
import io.micronaut.context.annotation.Requires
import org.apache.commons.lang3.RandomUtils
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object NotificationTypeDomainTestDataLoader {
   private val typeDomainValues = listOf(
      NotificationTypeDomain(1, "S", "Store", "notification.store"),
      NotificationTypeDomain(2, "E", "Employee", "notification.employee"),
      NotificationTypeDomain(3, "D", "Department", "notification.department"),
      NotificationTypeDomain(4, "A", "All", "notification.all")
   )

   @JvmStatic
   fun values(): List<NotificationTypeDomain> {
      return typeDomainValues
   }

   @JvmStatic
   fun random(): NotificationTypeDomain {
      return typeDomainValues[RandomUtils.nextInt(0, typeDomainValues.size)]
   }

   @JvmStatic
   fun stream(number: Int = 1): Stream<NotificationTypeDomain> {
      val notificationTypes = typeDomainValues
      val value: Long = if (number > 0 || number <= values().size) number.toLong() else 1

      return notificationTypes.stream().limit(value)
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class NotificationTypeDomainDataLoaderService @Inject constructor(
   private val notificationTypeDomainRepository: NotificationTypeDomainRepository
) {

   fun stream(numberIn: Int = 1): Stream<NotificationTypeDomain> {
      return NotificationTypeDomainTestDataLoader.stream(numberIn)
         .map { notificationTypeDomainRepository.findOne(it.id!!)!! } // since these are already saved in the database via a migration script just take the hard coded ones and look them up
         .filter { it != null } // filter any that weren't found
   }
}
