package com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalDTO
import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalEntity
import com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.infrastructure.GeneralLedgerReversalDistributionRepository
import com.cynergisuite.middleware.store.Store
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import javax.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class GeneralLedgerReversalDistributionDataLoader {

   static Stream<GeneralLedgerReversalDistributionEntity> stream(
      int numberIn = 1,
      GeneralLedgerReversalEntity glReversal,
      AccountEntity glReversalDistAcct,
      Store glReversalDistProfitCenter
   ) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final numbers = faker.number()

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerReversalDistributionEntity(
            null,
            glReversal,
            glReversalDistAcct,
            glReversalDistProfitCenter,
            numbers.randomDouble(2, 1, 1000000).toBigDecimal()
         )
      }
   }

   static Stream<GeneralLedgerReversalDistributionDTO> streamDTO(
      int numberIn = 1,
      GeneralLedgerReversalDTO glReversal,
      AccountDTO glReversalDistAcct,
      SimpleLegacyIdentifiableDTO glReversalDistProfitCenter
   ) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final numbers = faker.number()

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerReversalDistributionDTO([
            'generalLedgerReversal': new SimpleIdentifiableDTO(glReversal),
            'generalLedgerReversalDistributionAccount': new SimpleIdentifiableDTO(glReversalDistAcct),
            'generalLedgerReversalDistributionProfitCenter': glReversalDistProfitCenter,
            'generalLedgerReversalDistributionAmount': numbers.randomDouble(2, 1, 1000000).toBigDecimal()
         ])
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class GeneralLedgerReversalDistributionDataLoaderService {
   private final GeneralLedgerReversalDistributionRepository generalLedgerReversalDistributionRepository

   GeneralLedgerReversalDistributionDataLoaderService(GeneralLedgerReversalDistributionRepository generalLedgerReversalDistributionRepository) {
      this.generalLedgerReversalDistributionRepository = generalLedgerReversalDistributionRepository
   }

   Stream<GeneralLedgerReversalDistributionEntity> stream(
      int numberIn = 1,
      GeneralLedgerReversalEntity glReversal,
      AccountEntity glReversalDistAcct,
      Store glReversalDistProfitCenter
   ) {
      return GeneralLedgerReversalDistributionDataLoader.stream(numberIn, glReversal, glReversalDistAcct, glReversalDistProfitCenter)
         .map { generalLedgerReversalDistributionRepository.insert(it) }
   }

   GeneralLedgerReversalDistributionEntity single(GeneralLedgerReversalEntity glReversal, AccountEntity glReversalDistAcct, Store glReversalDistProfitCenter) {
      return stream(1, glReversal, glReversalDistAcct, glReversalDistProfitCenter)
         .findFirst().orElseThrow { new Exception("Unable to create General Ledger Reversal Distribution Entity") }
   }

   GeneralLedgerReversalDistributionDTO singleDTO(GeneralLedgerReversalDTO glReversal, AccountDTO glReversalDistAcct, SimpleLegacyIdentifiableDTO glReversalDistProfitCenter) {
      return GeneralLedgerReversalDistributionDataLoader.streamDTO(1, glReversal, glReversalDistAcct, glReversalDistProfitCenter)
         .findFirst().orElseThrow { new Exception("Unable to create General Ledger Reversal Distribution") }
   }
}
