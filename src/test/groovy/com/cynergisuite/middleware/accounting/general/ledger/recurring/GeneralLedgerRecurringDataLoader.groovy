package com.cynergisuite.middleware.accounting.general.ledger.recurring

import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure.GeneralLedgerRecurringRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import javax.inject.Singleton
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class GeneralLedgerRecurringDataLoader {

   static Stream<GeneralLedgerRecurringEntity> stream(int numberIn = 1, GeneralLedgerSourceCodeEntity source) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final random = faker.random()
      final lorem = faker.lorem()
      final date = faker.date()
      final defaultZoneId = ZoneId.systemDefault()
      final beginLocalDate = date.past(365, TimeUnit.DAYS).toInstant().atZone(defaultZoneId).toLocalDate()
      final endLocalDate = beginLocalDate.plusDays(random.nextInt(30).toLong())
      final beginDate = Date.from(beginLocalDate.atStartOfDay(defaultZoneId).toInstant())
      final endDate = Date.from(endLocalDate.atStartOfDay(defaultZoneId).toInstant())

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerRecurringEntity(
            null,
            source,
            GeneralLedgerRecurringTypeDataLoader.random(),
            random.nextBoolean(),
            lorem.characters(),
            beginLocalDate,
            endLocalDate,
            date.between(beginDate, endDate).toInstant().atZone(defaultZoneId).toLocalDate()
         )
      }
   }

   static Stream<GeneralLedgerRecurringDTO> streamDTO(int numberIn = 1, GeneralLedgerSourceCodeEntity source) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final random = faker.random()
      final lorem = faker.lorem()
      final date = faker.date()
      final defaultZoneId = ZoneId.systemDefault()
      final beginLocalDate = date.past(365, TimeUnit.DAYS).toInstant().atZone(defaultZoneId).toLocalDate()
      final endLocalDate = beginLocalDate.plusDays(random.nextInt(30).toLong())
      final beginDate = Date.from(beginLocalDate.atStartOfDay(defaultZoneId).toInstant())
      final endDate = Date.from(endLocalDate.atStartOfDay(defaultZoneId).toInstant())

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerRecurringDTO([
            'source': new GeneralLedgerSourceCodeDTO(source),
            'type': new GeneralLedgerRecurringTypeDTO(GeneralLedgerRecurringTypeDataLoader.random()),
            'reverseIndicator': random.nextBoolean(),
            'message': lorem.characters(),
            'beginDate': beginLocalDate,
            'endDate': endLocalDate,
            'lastTransferDate': date.between(beginDate, endDate).toInstant().atZone(defaultZoneId).toLocalDate()
         ])
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class GeneralLedgerRecurringDataLoaderService {
   private final GeneralLedgerRecurringRepository generalLedgerRecurringRepository

   GeneralLedgerRecurringDataLoaderService(GeneralLedgerRecurringRepository generalLedgerRecurringRepository) {
      this.generalLedgerRecurringRepository = generalLedgerRecurringRepository
   }

   Stream<GeneralLedgerRecurringEntity> stream(int numberIn = 1, CompanyEntity company, GeneralLedgerSourceCodeEntity source) {
      return GeneralLedgerRecurringDataLoader.stream(numberIn, source)
         .map { generalLedgerRecurringRepository.insert(it, company) }
   }

   GeneralLedgerRecurringEntity single(CompanyEntity company,  GeneralLedgerSourceCodeEntity source) {
      return stream(1, company, source).findFirst().orElseThrow { new Exception("Unable to create General Ledger Recurring  Entity") }
   }

   GeneralLedgerRecurringDTO singleDTO(GeneralLedgerSourceCodeEntity source) {
      return GeneralLedgerRecurringDataLoader.streamDTO(1, source).findFirst().orElseThrow { new Exception("Unable to create General Ledger Recurring ") }
   }
}
