package com.cynergisuite.middleware.accounting.routine

import com.cynergisuite.middleware.accounting.routine.infrastructure.RoutineRepository
import com.cynergisuite.middleware.accounting.routine.type.OverallPeriodType
import com.cynergisuite.middleware.accounting.routine.type.OverallPeriodTypeDTO
import com.cynergisuite.middleware.accounting.routine.type.OverallPeriodTypeDataLoader
import com.cynergisuite.middleware.company.Company
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object RoutineDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1): Stream<RoutineEntity> {
      val number = if (numberIn < 0) 1 else numberIn
      val overallPeriod = OverallPeriodTypeDataLoader.random()
      val periodCounter = AtomicInteger(1)
      val faker = Faker()
      val random = faker.random()
      val date = faker.date()
      val beginDate = date.past(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         RoutineEntity(
            overallPeriod = overallPeriod,
            period = periodCounter.getAndIncrement(),
            periodFrom = beginDate,
            periodTo = beginDate.plusMonths(it.toLong() + 1).minusDays(1),
            fiscalYear = beginDate.year,
            generalLedgerOpen = random.nextBoolean(),
            accountPayableOpen = random.nextBoolean()
         )
      }
   }

   @JvmStatic
   fun streamDTO(numberIn: Int = 1): Stream<RoutineDTO> {
      val number = if (numberIn < 0) 1 else numberIn
      val overallPeriod = OverallPeriodTypeDataLoader.random()
      val periodCounter = AtomicInteger(1)
      val faker = Faker()
      val random = faker.random()
      val date = faker.date()
      val beginDate = date.past(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         RoutineDTO(
            overallPeriod = OverallPeriodTypeDTO(overallPeriod),
            period = periodCounter.getAndIncrement(),
            periodFrom = beginDate,
            periodTo = beginDate.plusMonths(it.toLong() + 1).minusDays(1),
            fiscalYear = beginDate.year,
            generalLedgerOpen = random.nextBoolean(),
            accountPayableOpen = random.nextBoolean()
         )
      }
   }

   @JvmStatic
   fun streamFiscalYear(overallPeriodType: OverallPeriodType, startingDate: LocalDate?): Stream<RoutineEntity> {
      val number = 12
      val periodCounter = AtomicInteger(1)
      val faker = Faker()
      val random = faker.random()
      val date = faker.date()
      val beginDate = startingDate ?: date.past(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         RoutineEntity(
            overallPeriod = overallPeriodType,
            period = periodCounter.getAndIncrement(),
            periodFrom = beginDate.plusMonths(it.toLong()),
            periodTo = beginDate.plusMonths(it.toLong() + 1).minusDays(1),
            fiscalYear = beginDate.year,
            generalLedgerOpen = random.nextBoolean(),
            accountPayableOpen = random.nextBoolean()
         )
      }
   }

   @JvmStatic
   fun streamFiscalYearDTO(numberIn: Int = 1): Stream<RoutineDTO> {
      val number = if (numberIn < 0) 12 else (numberIn * 12)
      val overallPeriod = OverallPeriodTypeDataLoader.predefined().first { it.value == "C" }
      val periodCounter = AtomicInteger(1)
      val faker = Faker()
      val random = faker.random()
      val date = faker.date()
      val beginDate = date.past(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         RoutineDTO(
            overallPeriod = OverallPeriodTypeDTO(overallPeriod),
            period = periodCounter.getAndIncrement(),
            periodFrom = beginDate.plusMonths(it.toLong()),
            periodTo = beginDate.plusMonths(it.toLong()).minusDays(1),
            fiscalYear = beginDate.year,
            generalLedgerOpen = random.nextBoolean(),
            accountPayableOpen = random.nextBoolean()
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class RoutineDataLoaderService @Inject constructor(
   private val routineRepository: RoutineRepository
) {

   fun stream(numberIn: Int = 1, company: Company): Stream<RoutineEntity> {
      return RoutineDataLoader.stream(numberIn)
         .map { routineRepository.insert(it, company) }
   }

   fun single(company: Company): RoutineEntity {
      return stream(1, company).findFirst().orElseThrow { Exception("Unable to create Routine Entity") }
   }

   fun singleDTO(company: Company): RoutineDTO {
      return RoutineDataLoader.streamDTO(1).findFirst().orElseThrow { Exception("Unable to create Routine") }
   }

   fun streamFiscalYear(company: Company, overallPeriodType: OverallPeriodType, startingDate: LocalDate?): Stream<RoutineEntity> {
      return RoutineDataLoader.streamFiscalYear(overallPeriodType, startingDate)
         .map { routineRepository.insert(it, company) }
   }
}
