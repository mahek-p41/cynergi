package com.cynergisuite.middleware.schedule

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.schedule.command.ScheduleCommandTypeEntity
import com.cynergisuite.middleware.schedule.command.ScheduleCommandTypeFactory
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.schedule.type.ScheduleType
import com.cynergisuite.middleware.schedule.type.ScheduleTypeFactory
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object ScheduleFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, scheduleTypeIn: ScheduleType? = null, commandIn: ScheduleCommandTypeEntity? = null, company: Company): Stream<ScheduleEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val scheduleType = scheduleTypeIn ?: ScheduleTypeFactory.random()
      val faker = Faker()
      val team = faker.team()
      val command = commandIn ?: ScheduleCommandTypeFactory.random()

      return IntStream.range(0, number).mapToObj {
         ScheduleEntity(
            title = team.name(),
            description = team.sport(),
            schedule = team.creature(),
            command = command,
            type = scheduleType,
            company = company
         )
      }
   }

   @JvmStatic
   fun single(scheduleTypeIn: ScheduleType? = null, company: Company) : ScheduleEntity {
      return stream(1, scheduleTypeIn, company = company).findFirst().orElseThrow{ Exception("Unable to create Schedule") }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class ScheduleFactoryService @Inject constructor(
   private val scheduleRepository: ScheduleRepository
) {

   fun stream(numberIn: Int = 1, scheduleTypeIn: ScheduleType? = null, company: Company): Stream<ScheduleEntity> {
      return ScheduleFactory.stream(numberIn, scheduleTypeIn, company = company)
         .map { scheduleRepository.insert(it) }
   }

   fun single(company: Company): ScheduleEntity {
      return stream(1, company = company).findFirst().orElseThrow { Exception("Unable to create Schedule") }
   }
}
