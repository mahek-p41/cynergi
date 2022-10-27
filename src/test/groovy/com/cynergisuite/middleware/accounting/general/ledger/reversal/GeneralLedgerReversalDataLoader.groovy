package com.cynergisuite.middleware.accounting.general.ledger.reversal

import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.cynergisuite.middleware.accounting.general.ledger.reversal.infrastructure.GeneralLedgerReversalRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

import java.time.LocalDate
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class GeneralLedgerReversalDataLoader {

   static Stream<GeneralLedgerReversalEntity> stream(int numberIn = 1, GeneralLedgerSourceCodeEntity sourceIn) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final lorem = faker.lorem()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerReversalEntity(
            null,
            sourceIn,
            LocalDate.now(),
            LocalDate.now(),
            lorem.sentence(),
            random.nextInt(1, 12),
            random.nextInt(1, 1000000)
         )
      }
   }

   static Stream<GeneralLedgerReversalDTO> streamDTO(int numberIn = 1, GeneralLedgerSourceCodeDTO sourceIn) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final lorem = faker.lorem()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerReversalDTO([
            'source': sourceIn,
            'date': LocalDate.now(),
            'reversalDate': LocalDate.now(),
            'comment': lorem.sentence(),
            'entryMonth': random.nextInt(1, 12),
            'entryNumber': random.nextInt(1, 1000000)
         ])
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class GeneralLedgerReversalDataLoaderService {
   private final GeneralLedgerReversalRepository repository

   GeneralLedgerReversalDataLoaderService(GeneralLedgerReversalRepository repository) {
      this.repository = repository
   }

   Stream<GeneralLedgerReversalEntity> stream(int numberIn = 1, CompanyEntity company, GeneralLedgerSourceCodeEntity sourceIn) {
      return GeneralLedgerReversalDataLoader.stream(numberIn, sourceIn)
         .map { repository.insert(it, company) }
   }

   GeneralLedgerReversalEntity single(CompanyEntity company, GeneralLedgerSourceCodeEntity sourceIn) {
      return stream(1, company, sourceIn).findFirst().orElseThrow { new Exception("Unable to find GeneralLedgerReversal") }
   }

   GeneralLedgerReversalDTO singleDTO(GeneralLedgerSourceCodeDTO sourceIn) {
      return GeneralLedgerReversalDataLoader.streamDTO(1, sourceIn).findFirst().orElseThrow { new Exception("Unable to create GeneralLedgerReversal") }
   }
}
