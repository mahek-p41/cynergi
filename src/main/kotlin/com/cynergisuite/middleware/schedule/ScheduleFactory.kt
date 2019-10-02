package com.cynergisuite.middleware.schedule

import com.cynergisuite.middleware.schedule.repository.ScheduleRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object ScheduleFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, scheduleTypeIn: ScheduleType? = null): Stream<Schedule> {
      val number = if (numberIn > 0) numberIn else 1
      val scheduleType = scheduleTypeIn ?: ScheduleTypeFactory.random()
      val faker = Faker()
      val team = faker.team()

      return IntStream.range(0, number).mapToObj {
         Schedule(
            title = team.name(),
            description = team.sport(),
            schedule = team.creature(),
            command = team.state(),
            type = scheduleType
         )
      }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class ScheduleFactoryService @Inject constructor(
   private val scheduleRepository: ScheduleRepository
) {

   fun stream(numberIn: Int = 1, scheduleTypeIn: ScheduleType? = null): Stream<Schedule> {
      return ScheduleFactory.stream(numberIn, scheduleTypeIn)
         .map { scheduleRepository.insert(it) }
   }
}
