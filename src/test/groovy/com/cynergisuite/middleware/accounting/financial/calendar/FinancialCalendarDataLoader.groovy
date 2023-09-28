package com.cynergisuite.middleware.accounting.financial.calendar

import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodType
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDTO
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDataLoader
import com.cynergisuite.middleware.company.CompanyEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class FinancialCalendarDataLoader {

   static Stream<FinancialCalendarEntity> stream(int numberIn = 1) {
      final number = numberIn > 0 ? numberIn : 1
      final overallPeriod = OverallPeriodTypeDataLoader.random()
      final periodCounter = new AtomicInteger(1)
      final faker = new Faker()
      final random = faker.random()
      final date = faker.date()
      final beginDate = date.past(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.of("-05:00")).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         new FinancialCalendarEntity(
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

   static Stream<FinancialCalendarDTO> streamDTO(int numberIn = 1) {
      final number = numberIn > 0 ? numberIn : 1
      final overallPeriod = OverallPeriodTypeDataLoader.random()
      final periodCounter = new AtomicInteger(1)
      final faker = new Faker()
      final random = faker.random()
      final date = faker.date()
      final beginDate = date.past(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.of("-05:00")).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         new FinancialCalendarDTO([
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

   static Stream<FinancialCalendarEntity> streamFiscalYear(OverallPeriodType overallPeriodType, LocalDate startingDate = null, Boolean glOpen = null, Boolean apOpen = null) {
      final number = 12
      final periodCounter = new AtomicInteger(1)
      final faker = new Faker()
      final random = faker.random()
      final date = faker.date()
      final beginDate = startingDate ?: date.past(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.of("-05:00")).toLocalDate()
      final setGL = glOpen != null ? glOpen : random.nextBoolean() //If glOpen is not null, then glOpen, otherwise random (NOTE: elvis does not work with boolean!)
      final setAP = apOpen != null ? apOpen : random.nextBoolean() //If apOpen is not null, then apOpen, otherwise random (NOTE: elvis does not work with boolean!)


      return IntStream.range(0, number).mapToObj {
         new FinancialCalendarEntity(
            null,
            overallPeriodType,
            periodCounter.getAndIncrement(),
            beginDate.plusMonths(it.toLong()),
            beginDate.plusMonths(it.toLong() + 1).minusDays(1),
            beginDate.year,
            setGL,
            setAP
         )
      }
   }

   static Stream<FinancialCalendarDTO> streamFiscalYearDTO(int numberIn = 1) {
      final number = numberIn < 0 ? 12 : numberIn * 12
      final overallPeriod = OverallPeriodTypeDataLoader.predefined().find {it.value == "C" }
      final periodCounter = new AtomicInteger(1)
      final faker = new Faker()
      final random = faker.random()
      final date = faker.date()
      final beginDate = date.past(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.of("-05:00")).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         new FinancialCalendarDTO([
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
class FinancialCalendarDataLoaderService {
   private final FinancialCalendarRepository financialCalendarRepository

   FinancialCalendarDataLoaderService(FinancialCalendarRepository financialCalendarRepository) {
      this.financialCalendarRepository = financialCalendarRepository
   }

   Stream<FinancialCalendarEntity> stream(int numberIn = 1, CompanyEntity company) {
      return FinancialCalendarDataLoader.stream(numberIn)
         .map { financialCalendarRepository.insert(it, company) }
   }

   FinancialCalendarEntity single(CompanyEntity company) {
      return stream(1, company).findFirst().orElseThrow { new Exception("Unable to create Financial Calendar Entity") }
   }

   FinancialCalendarDTO singleDTO() {
      return FinancialCalendarDataLoader.streamDTO(1).findFirst().orElseThrow { new Exception("Unable to create Financial Calendar") }
   }

   Stream<FinancialCalendarEntity> streamFiscalYear(CompanyEntity company, OverallPeriodType overallPeriodType, LocalDate startingDate = null, Boolean glOpen = null, Boolean apOpen = null) {
      return FinancialCalendarDataLoader.streamFiscalYear(overallPeriodType, startingDate, glOpen, apOpen)
         .map { financialCalendarRepository.insert(it, company) }
   }
}

