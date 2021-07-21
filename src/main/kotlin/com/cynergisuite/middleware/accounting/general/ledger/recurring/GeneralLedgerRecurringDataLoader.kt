package com.cynergisuite.middleware.accounting.general.ledger.recurring

import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure.GeneralLedgerRecurringRepository
import com.cynergisuite.middleware.company.Company
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.time.ZoneId
import java.util.Date
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object GeneralLedgerRecurringDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, source: GeneralLedgerSourceCodeEntity): Stream<GeneralLedgerRecurringEntity> {
      val number = if (numberIn < 0) 1 else numberIn
      val faker = Faker()
      val random = faker.random()
      val lorem = faker.lorem()
      val date = faker.date()
      val defaultZoneId = ZoneId.systemDefault()
      val beginLocalDate = date.past(365, TimeUnit.DAYS).toInstant().atZone(defaultZoneId).toLocalDate()
      val endLocalDate = beginLocalDate.plusDays(random.nextInt(30).toLong())
      val beginDate = Date.from(beginLocalDate.atStartOfDay(defaultZoneId).toInstant())
      val endDate = Date.from(endLocalDate.atStartOfDay(defaultZoneId).toInstant())

      return IntStream.range(0, number).mapToObj {
         GeneralLedgerRecurringEntity(
            source = source,
            type = GeneralLedgerRecurringTypeDataLoader.random(),
            reverseIndicator = random.nextBoolean(),
            message = lorem.characters(),
            beginDate = beginLocalDate,
            endDate = endLocalDate,
            lastTransferDate = date.between(beginDate, endDate).toInstant().atZone(defaultZoneId).toLocalDate()
         )
      }
   }

   @JvmStatic
   fun streamDTO(numberIn: Int = 1, source: GeneralLedgerSourceCodeEntity): Stream<GeneralLedgerRecurringDTO> {
      val number = if (numberIn < 0) 1 else numberIn
      val faker = Faker()
      val random = faker.random()
      val lorem = faker.lorem()
      val date = faker.date()
      val defaultZoneId = ZoneId.systemDefault()
      val beginLocalDate = date.past(365, TimeUnit.DAYS).toInstant().atZone(defaultZoneId).toLocalDate()
      val endLocalDate = beginLocalDate.plusDays(random.nextInt(30).toLong())
      val beginDate = Date.from(beginLocalDate.atStartOfDay(defaultZoneId).toInstant())
      val endDate = Date.from(endLocalDate.atStartOfDay(defaultZoneId).toInstant())

      return IntStream.range(0, number).mapToObj {
         GeneralLedgerRecurringDTO(
            source = GeneralLedgerSourceCodeDTO(source),
            type = GeneralLedgerRecurringTypeDTO(GeneralLedgerRecurringTypeDataLoader.random()),
            reverseIndicator = random.nextBoolean(),
            message = lorem.characters(),
            beginDate = beginLocalDate,
            endDate = endLocalDate,
            lastTransferDate = date.between(beginDate, endDate).toInstant().atZone(defaultZoneId).toLocalDate()
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class GeneralLedgerRecurringDataLoaderService @Inject constructor(
   private val generalLedgerRecurringRepository: GeneralLedgerRecurringRepository
) {

   fun stream(numberIn: Int = 1, company: Company, source: GeneralLedgerSourceCodeEntity): Stream<GeneralLedgerRecurringEntity> {
      return GeneralLedgerRecurringDataLoader.stream(numberIn, source)
         .map { generalLedgerRecurringRepository.insert(it, company) }
   }

   fun single(company: Company, source: GeneralLedgerSourceCodeEntity): GeneralLedgerRecurringEntity {
      return stream(1, company, source).findFirst().orElseThrow { Exception("Unable to create General Ledger Recurring  Entity") }
   }

   fun singleDTO(company: Company, source: GeneralLedgerSourceCodeEntity): GeneralLedgerRecurringDTO {
      return GeneralLedgerRecurringDataLoader.streamDTO(1, source).findFirst().orElseThrow { Exception("Unable to create General Ledger Recurring ") }
   }
}
