package com.cynergisuite.middleware.schedule

import com.cynergisuite.middleware.schedule.infrastructure.ScheduleArgRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object ScheduleArgFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1): Stream<ScheduleArg> {
      val faker = Faker()
      val book = faker.book()

      return IntStream.range(0, numberIn).mapToObj {
         ScheduleArg(
            value = book.author(),
            description = book.title()
         )

      }
   }

}

@Singleton
@Requires(env = ["demo", "test"])
class ScheduleArgFactoryService @Inject constructor(
   private val scheduleArgRepository: ScheduleArgRepository
) {

   fun stream(numberIn: Int = 1): Stream<ScheduleArg> {
      return ScheduleArgFactory.stream(numberIn).map { scheduleArgRepository.insert(it) }
   }
}
