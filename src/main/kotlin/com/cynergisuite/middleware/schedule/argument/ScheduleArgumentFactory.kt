package com.cynergisuite.middleware.schedule.argument

import com.cynergisuite.middleware.schedule.Schedule
import com.cynergisuite.middleware.schedule.ScheduleFactoryService
import com.cynergisuite.middleware.schedule.argument.infrastructure.ScheduleArgumentRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Singleton
import kotlin.streams.asSequence

object ScheduleArgumentFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1): Stream<ScheduleArgument> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val code = faker.team()

      0..number

      return IntStream.range(0, number).mapToObj {
         ScheduleArgument(
            id = null,
            value = code.sport(),
            description = code.creature()
         )
      }
   }

   @JvmStatic
   fun single(): ScheduleArgument {
      return stream(1).findFirst().orElseThrow { Exception("Unable to create ScheduleArgument") }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class ScheduleArgumentFactoryService(
   private val scheduleArgumentRepository: ScheduleArgumentRepository,
   private val scheduleFactoryService: ScheduleFactoryService
) {

   fun stream(numberIn: Int = 1, scheduleIn: Schedule? = null): Stream<ScheduleArgument> {
      val schedule = scheduleIn ?: scheduleFactoryService.single()

      return ScheduleArgumentFactory.stream(1)
         .map { scheduleArgumentRepository.insert(schedule, it) }
   }

   fun single(scheduleIn: Schedule? = null): ScheduleArgument {
      return stream(1, scheduleIn).findFirst().orElseThrow { Exception("Unable to create ScheduleArgument") }
   }
}
