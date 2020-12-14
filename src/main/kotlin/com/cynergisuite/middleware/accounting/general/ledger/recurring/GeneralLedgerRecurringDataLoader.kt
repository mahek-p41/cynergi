package com.cynergisuite.middleware.accounting.general.ledger.recurring

import com.cynergisuite.middleware.accounting.account.payable.recurring.infrastructure.GeneralLedgerRecurringRepository
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.cynergisuite.middleware.company.Company
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.time.ZoneId
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
      val beginDate = date.past(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         GeneralLedgerRecurringEntity(
            source = source,
            type = GeneralLedgerRecurringTypeDataLoader.random(),
            reverseIndicator = random.nextBoolean(),
            message = lorem.characters(),
            beginDate = beginDate,
            endDate = beginDate.plusDays(random.nextInt(30).toLong())
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
      val beginDate = date.past(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         GeneralLedgerRecurringDTO(
            source = GeneralLedgerSourceCodeDTO(source),
            type = GeneralLedgerRecurringTypeDTO(GeneralLedgerRecurringTypeDataLoader.random()),
            reverseIndicator = random.nextBoolean(),
            message = lorem.characters(),
            beginDate = beginDate,
            endDate = beginDate.plusDays(random.nextInt(30).toLong())
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
