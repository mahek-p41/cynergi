package com.cynergisuite.middleware.schedule

import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.schedule.command.ScheduleCommandType
import com.cynergisuite.middleware.schedule.command.ScheduleCommandTypeKt
import com.cynergisuite.middleware.schedule.command.ScheduleCommandTypeTestDataLoader
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.schedule.type.ScheduleType
import com.cynergisuite.middleware.schedule.type.ScheduleTypeKt
import com.cynergisuite.middleware.schedule.type.ScheduleTypeTestDataLoader
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Inject
import jakarta.inject.Singleton

import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class ScheduleTestDataLoader {

   static Stream<ScheduleEntity> stream(int numberIn = 1, ScheduleType scheduleTypeIn = null, ScheduleCommandType commandIn = null, CompanyEntity company) {
      final number = numberIn > 0 ? numberIn : 1
      final scheduleType = scheduleTypeIn != null ? ScheduleTypeKt.toEntity(scheduleTypeIn) : ScheduleTypeTestDataLoader.random()
      final faker = new Faker()
      final team = faker.team()
      final command = commandIn != null ? ScheduleCommandTypeKt.toEntity(commandIn) : ScheduleCommandTypeTestDataLoader.random()

      return IntStream.range(0, number).mapToObj {
         new ScheduleEntity(
            team.name(),
            team.sport(),
            team.creature(),
            command,
            scheduleType,
            company
         )
      }
   }

   static ScheduleEntity single(ScheduleType scheduleTypeIn = null, CompanyEntity company) {
      return stream(1, scheduleTypeIn, company).findFirst().orElseThrow { new Exception("Unable to create Schedule") }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["demo", "test"])
class ScheduleTestDataLoaderService {
   private final ScheduleRepository scheduleRepository

   @Inject
   ScheduleTestDataLoaderService(ScheduleRepository scheduleRepository) {
      this.scheduleRepository = scheduleRepository
   }

   Stream<ScheduleEntity> stream(int numberIn = 1, ScheduleType scheduleTypeIn = null, CompanyEntity company) {
      return ScheduleTestDataLoader.stream(numberIn, scheduleTypeIn, company)
         .map { scheduleRepository.insert(it) }
   }

   Stream<ScheduleEntity> stream(int numberIn = 1, ScheduleType scheduleTypeIn = null, ScheduleCommandType scheduleCommandTypeIn, CompanyEntity company) {
      return ScheduleTestDataLoader.stream(numberIn, scheduleTypeIn, scheduleCommandTypeIn, company)
         .map {  scheduleRepository.insert(it) }
   }

   ScheduleEntity single(CompanyEntity company) {
      return stream(1, null, null, company).findFirst().orElseThrow { new Exception("Unable to create Schedule") }
   }
}
