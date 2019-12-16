package com.cynergisuite.middleware.notification

import com.cynergisuite.middleware.notification.infrastructure.NotificationTypeDomainRepository
import io.micronaut.context.annotation.Requires
import org.apache.commons.lang3.RandomUtils
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object NotificationTypeDomainTestDataLoader {
   private val typeDomainValues = listOf(
      STORE,
      EMPLOYEE,
      DEPARTMENT,
      ALL
   )

   @JvmStatic
   fun values(): List<NotificationType> {
      return typeDomainValues
   }

   @JvmStatic
   fun random(): NotificationType {
      return typeDomainValues[RandomUtils.nextInt(0, typeDomainValues.size)]
   }

   @JvmStatic
   fun stream(number: Int = 1): Stream<NotificationType> {
      val notificationTypes = typeDomainValues
      val value: Long = if (number > 0 || number <= values().size) number.toLong() else 1

      return notificationTypes.stream().limit(value)
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class NotificationTypeDomainDataLoaderService @Inject constructor(
   private val notificationTypeDomainRepository: NotificationTypeDomainRepository
) {

   fun stream(numberIn: Int = 1): Stream<NotificationType> {
      return NotificationTypeDomainTestDataLoader.stream(numberIn)
         .map { notificationTypeDomainRepository.findOne(it.id)!! } // since these are already saved in the database via a migration script just take the hard coded ones and look them up
         .filter { it != null } // filter any that weren't found
   }
}
