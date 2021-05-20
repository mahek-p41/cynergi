package com.cynergisuite.middleware.accounting.routine

import com.cynergisuite.middleware.accounting.routine.infrastructure.RoutineRepository
import com.cynergisuite.middleware.accounting.routine.type.OverallPeriodType
import com.cynergisuite.middleware.accounting.routine.type.OverallPeriodTypeDTO
import com.cynergisuite.middleware.accounting.routine.type.OverallPeriodTypeDataLoader
import com.cynergisuite.middleware.company.CompanyEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import javax.inject.Singleton
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class RoutineDataLoader {

   static Stream<RoutineEntity> stream(int numberIn = 1) {
      final number = numberIn > 0 ? numberIn : 1
      final overallPeriod = OverallPeriodTypeDataLoader.random()
      final periodCounter = new AtomicInteger(1)
      final faker = new Faker()
      final random = faker.random()
      final date = faker.date()
      final beginDate = date.past(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         new RoutineEntity(
            null,
            overallPeriod,
            periodCounter.getAndIncrement(),
            beginDate,
            beginDate.plusMonths(it.toLong() + 1).minusDays(1),
            beginDate.year,
            random.nextBoolean(),
            random.nextBoolean()
         )
      }
   }

   static Stream<RoutineDTO> streamDTO(int numberIn = 1) {
      final number = numberIn > 0 ? numberIn : 1
      final overallPeriod = OverallPeriodTypeDataLoader.random()
      final periodCounter = new AtomicInteger(1)
      final faker = new Faker()
      final random = faker.random()
      final date = faker.date()
      final beginDate = date.past(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         new RoutineDTO([
            'overallPeriod': new OverallPeriodTypeDTO(overallPeriod),
            'period': periodCounter.getAndIncrement(),
            'periodFrom': beginDate,
            'periodTo': beginDate.plusMonths(it.toLong() + 1).minusDays(1),
            'fiscalYear': beginDate.year,
            'generalLedgerOpen': random.nextBoolean(),
            'accountPayableOpen': random.nextBoolean()
         ])
      }
   }

   static Stream<RoutineEntity> streamFiscalYear(OverallPeriodType overallPeriodType, LocalDate startingDate = null) {
      final number = 12
      final periodCounter = new AtomicInteger(1)
      final faker = new Faker()
      final random = faker.random()
      final date = faker.date()
      final beginDate = startingDate ?: date.past(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         new RoutineEntity(
            null,
            overallPeriodType,
            periodCounter.getAndIncrement(),
            beginDate.plusMonths(it.toLong()),
            beginDate.plusMonths(it.toLong() + 1).minusDays(1),
            beginDate.year,
            random.nextBoolean(),
            random.nextBoolean()
         )
      }
   }

   static Stream<RoutineDTO> streamFiscalYearDTO(int numberIn = 1) {
      final number = numberIn < 0 ? 12 : numberIn * 12
      final overallPeriod = OverallPeriodTypeDataLoader.predefined().find {it.value == "C" }
      final periodCounter = new AtomicInteger(1)
      final faker = new Faker()
      final random = faker.random()
      final date = faker.date()
      final beginDate = date.past(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         new RoutineDTO([
            'overallPeriod': new OverallPeriodTypeDTO(overallPeriod),
            'period': periodCounter.getAndIncrement(),
            'periodFrom': beginDate.plusMonths(it.toLong()),
            'periodTo': beginDate.plusMonths(it.toLong()).minusDays(1),
            'fiscalYear': beginDate.year,
            'generalLedgerOpen': random.nextBoolean(),
            'accountPayableOpen': random.nextBoolean()
         ])
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class RoutineDataLoaderService {
   private final RoutineRepository routineRepository

   RoutineDataLoaderService(RoutineRepository routineRepository) {
      this.routineRepository = routineRepository
   }

   Stream<RoutineEntity> stream(int numberIn = 1, CompanyEntity company) {
      return RoutineDataLoader.stream(numberIn)
         .map { routineRepository.insert(it, company) }
   }

   RoutineEntity single(CompanyEntity company) {
      return stream(1, company).findFirst().orElseThrow { new Exception("Unable to create Routine Entity") }
   }

   RoutineDTO singleDTO() {
      return RoutineDataLoader.streamDTO(1).findFirst().orElseThrow { new Exception("Unable to create Routine") }
   }

   Stream<RoutineEntity> streamFiscalYear(CompanyEntity company, OverallPeriodType overallPeriodType, LocalDate startingDate = null) {
      return RoutineDataLoader.streamFiscalYear(overallPeriodType, startingDate)
         .map { routineRepository.insert(it, company) }
   }
}
