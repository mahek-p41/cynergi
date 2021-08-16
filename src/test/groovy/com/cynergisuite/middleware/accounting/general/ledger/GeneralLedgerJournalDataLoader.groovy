package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerJournalRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.Store
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import java.math.RoundingMode
import java.time.LocalDate
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Singleton

@CompileStatic
class GeneralLedgerJournalDataLoader {

   static Stream<GeneralLedgerJournalEntity> stream(int numberIn = 1, AccountEntity accountIn, Store profitCenterIn, LocalDate dateIn, GeneralLedgerSourceCodeEntity sourceIn) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final numbers = faker.number()

      return IntStream.range(0, number).mapToObj {
         final amount = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
         final message = faker.lorem().sentence()

         new GeneralLedgerJournalEntity(
            null,
            accountIn,
            profitCenterIn,
            dateIn,
            sourceIn,
            amount,
            message
         )
      }
   }

   static Stream<GeneralLedgerJournalDTO> streamDTO(int numberIn = 1, SimpleIdentifiableDTO accountIn, SimpleLegacyIdentifiableDTO profitCenterIn, LocalDate dateIn, GeneralLedgerSourceCodeDTO sourceIn) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final numbers = faker.number()
      final amount = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
      final message = faker.lorem().sentence()

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerJournalDTO([
            'account': accountIn,
            'profitCenter': profitCenterIn,
            'date': dateIn,
            'source': sourceIn,
            'amount': amount,
            'message': message
         ])
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class GeneralLedgerJournalDataLoaderService {
   private final GeneralLedgerJournalRepository repository

   GeneralLedgerJournalDataLoaderService(GeneralLedgerJournalRepository repository) {
      this.repository = repository
   }

   Stream<GeneralLedgerJournalEntity> stream(int numberIn = 1, CompanyEntity company, AccountEntity accountIn, Store profitCenterIn, LocalDate dateIn, GeneralLedgerSourceCodeEntity sourceIn) {
      return GeneralLedgerJournalDataLoader.stream(numberIn, accountIn, profitCenterIn, dateIn, sourceIn)
         .map { repository.insert(it, company) }
   }

   GeneralLedgerJournalEntity single(CompanyEntity company, AccountEntity accountIn, Store profitCenterIn, LocalDate dateIn, GeneralLedgerSourceCodeEntity sourceIn) {
      return stream(1, company, accountIn, profitCenterIn, dateIn, sourceIn).findFirst().orElseThrow { Exception("Unable to find GeneralLedgerJournal") }
   }

   GeneralLedgerJournalDTO singleDTO(SimpleIdentifiableDTO accountIn, SimpleLegacyIdentifiableDTO profitCenterIn, LocalDate dateIn, GeneralLedgerSourceCodeDTO sourceIn) {
      return GeneralLedgerJournalDataLoader.streamDTO(1, accountIn, profitCenterIn, dateIn, sourceIn).findFirst().orElseThrow { Exception("Unable to create GeneralLedgerJournal") }
   }
}
