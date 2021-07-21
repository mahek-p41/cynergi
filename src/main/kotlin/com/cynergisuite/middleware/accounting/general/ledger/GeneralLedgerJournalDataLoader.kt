package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerJournalRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.store.Store
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.math.RoundingMode
import java.time.LocalDate
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object GeneralLedgerJournalDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, accountIn: AccountEntity, profitCenterIn: Store, dateIn: LocalDate, sourceIn: GeneralLedgerSourceCodeEntity): Stream<GeneralLedgerJournalEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val numbers = faker.number()

      return IntStream.range(0, number).mapToObj {
         val amount = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
         val message = faker.lorem().sentence()

         GeneralLedgerJournalEntity(account = accountIn, profitCenter = profitCenterIn, date = dateIn, source = sourceIn, amount = amount, message = message)
      }
   }

   @JvmStatic
   fun streamDTO(numberIn: Int = 1, accountIn: SimpleIdentifiableDTO, profitCenterIn: SimpleLegacyIdentifiableDTO, dateIn: LocalDate, sourceIn: GeneralLedgerSourceCodeDTO): Stream<GeneralLedgerJournalDTO> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val numbers = faker.number()
      val amount = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
      val message = faker.lorem().sentence()

      return IntStream.range(0, number).mapToObj {
         GeneralLedgerJournalDTO(account = accountIn, profitCenter = profitCenterIn, date = dateIn, source = sourceIn, amount = amount, message = message)
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class GeneralLedgerJournalDataLoaderService @Inject constructor(
   private val repository: GeneralLedgerJournalRepository
) {

   fun stream(numberIn: Int = 1, company: Company, accountIn: AccountEntity, profitCenterIn: Store, dateIn: LocalDate, sourceIn: GeneralLedgerSourceCodeEntity): Stream<GeneralLedgerJournalEntity> {
      return GeneralLedgerJournalDataLoader.stream(numberIn, accountIn, profitCenterIn, dateIn, sourceIn)
         .map { repository.insert(it, company) }
   }

   fun single(company: Company, accountIn: AccountEntity, profitCenterIn: Store, dateIn: LocalDate, sourceIn: GeneralLedgerSourceCodeEntity): GeneralLedgerJournalEntity {
      return stream(1, company, accountIn, profitCenterIn, dateIn, sourceIn).findFirst().orElseThrow { Exception("Unable to find GeneralLedgerJournal") }
   }

   fun singleDTO(accountIn: SimpleIdentifiableDTO, profitCenterIn: SimpleLegacyIdentifiableDTO, dateIn: LocalDate, sourceIn: GeneralLedgerSourceCodeDTO): GeneralLedgerJournalDTO {
      return GeneralLedgerJournalDataLoader.streamDTO(1, accountIn, profitCenterIn, dateIn, sourceIn).findFirst().orElseThrow { Exception("Unable to create GeneralLedgerJournal") }
   }
}
