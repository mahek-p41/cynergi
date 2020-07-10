package com.cynergisuite.middleware.schedule.argument

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.ScheduleFactoryService
import com.cynergisuite.middleware.schedule.argument.infrastructure.ScheduleArgumentRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Singleton

object ScheduleArgumentFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1): Stream<ScheduleArgumentEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val code = faker.team()

      return IntStream.range(0, number).mapToObj {
         ScheduleArgumentEntity(
            id = null,
            value = code.sport(),
            description = code.creature()
         )
      }
   }

   @JvmStatic
   fun single(): ScheduleArgumentEntity {
      return stream(1).findFirst().orElseThrow { Exception("Unable to create ScheduleArgument") }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class ScheduleArgumentFactoryService(
   private val scheduleArgumentRepository: ScheduleArgumentRepository,
   private val scheduleFactoryService: ScheduleFactoryService
) {

   fun stream(numberIn: Int = 1, scheduleIn: ScheduleEntity? = null, company: Company): Stream<ScheduleArgumentEntity> {
      val schedule = scheduleIn ?: scheduleFactoryService.single(company)

      return ScheduleArgumentFactory.stream(1)
         .map { scheduleArgumentRepository.insert(schedule, it) }
   }

   fun single(scheduleIn: ScheduleEntity? = null, company: Company): ScheduleArgumentEntity {
      return stream(1, scheduleIn, company).findFirst().orElseThrow { Exception("Unable to create ScheduleArgument") }
   }
}
