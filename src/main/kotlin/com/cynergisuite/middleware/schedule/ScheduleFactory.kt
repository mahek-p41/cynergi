package com.cynergisuite.middleware.schedule

import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.schedule.type.ScheduleTypeEntity
import com.cynergisuite.middleware.schedule.type.ScheduleTypeFactory
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object ScheduleFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, scheduleTypeIn: ScheduleTypeEntity? = null): Stream<ScheduleEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val scheduleType = scheduleTypeIn ?: ScheduleTypeFactory.random()
      val faker = Faker()
      val team = faker.team()

      return IntStream.range(0, number).mapToObj {
         ScheduleEntity(
            title = team.name(),
            description = team.sport(),
            schedule = team.creature(),
            command = team.state(),
            type = scheduleType
         )
      }
   }

   @JvmStatic
   fun single(scheduleTypeIn: ScheduleTypeEntity? = null) : ScheduleEntity {
      return stream(1, scheduleTypeIn).findFirst().orElseThrow{ Exception("Unable to create Schedule") }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class ScheduleFactoryService @Inject constructor(
   private val scheduleRepository: ScheduleRepository
) {

   fun stream(numberIn: Int = 1, scheduleTypeIn: ScheduleTypeEntity? = null): Stream<ScheduleEntity> {
      return ScheduleFactory.stream(numberIn, scheduleTypeIn)
         .map { scheduleRepository.insert(it) }
   }

   fun single(): ScheduleEntity {
      return stream(1).findFirst().orElseThrow { Exception("Unable to create Schedule") }
   }
}
