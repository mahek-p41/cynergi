package com.cynergisuite.middleware.schedule.argument

import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.ScheduleTestDataLoaderService
import com.cynergisuite.middleware.schedule.argument.infrastructure.ScheduleArgumentRepository
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import jakarta.inject.Inject
import java.util.stream.IntStream
import java.util.stream.Stream
import jakarta.inject.Singleton

@CompileStatic
class ScheduleArgumentTestDataLoader {

   static Stream<ScheduleArgumentEntity> stream(int numberIn = 1) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final code = faker.team()

      return IntStream.range(0, number).mapToObj {
         new ScheduleArgumentEntity(
            null,
            code.sport(),
            code.creature(),
            false,
         )
      }
   }

   static ScheduleArgumentEntity single() {
      return stream(1).findFirst().orElseThrow { new Exception("Unable to create ScheduleArgument") }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["demo", "test"])
class ScheduleArgumentTestDataLoaderService {
   private final ScheduleArgumentRepository scheduleArgumentRepository
   private final ScheduleTestDataLoaderService scheduleTestDataLoaderService

   @Inject
   ScheduleArgumentTestDataLoaderService(ScheduleArgumentRepository scheduleArgumentRepository, ScheduleTestDataLoaderService scheduleTestDataLoaderService) {
      this.scheduleArgumentRepository = scheduleArgumentRepository
      this.scheduleTestDataLoaderService = scheduleTestDataLoaderService
   }

   Stream<ScheduleArgumentEntity> stream(int numberIn = 1, ScheduleEntity scheduleIn = null, CompanyEntity company) {
      final schedule = scheduleIn ?: scheduleTestDataLoaderService.single(company)

      return ScheduleArgumentTestDataLoader.stream(numberIn)
         .map { scheduleArgumentRepository.insert(schedule, it) }
   }

   ScheduleArgumentEntity single(ScheduleEntity scheduleIn = null, CompanyEntity company) {
      return stream(1, scheduleIn, company).findFirst().orElseThrow { new Exception("Unable to create ScheduleArgument") }
   }
}
