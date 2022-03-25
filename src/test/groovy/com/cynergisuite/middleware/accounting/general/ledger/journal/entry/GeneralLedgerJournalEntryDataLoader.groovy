package com.cynergisuite.middleware.accounting.general.ledger.journal.entry

import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class GeneralLedgerJournalEntryDataLoader {

   private static final AtomicLong journalEntryNumber = new AtomicLong(1)

   static Stream<GeneralLedgerJournalEntryEntity> stream(
      int numberIn = 1,
      GeneralLedgerSourceCodeEntity sourceIn,
      Boolean reverseIn,
      List<GeneralLedgerJournalEntryDetailEntity> glDetailsIn,
      Boolean postReversingEntryIn
   ) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final random = faker.random()
      final message = faker.lorem().sentence()

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerJournalEntryEntity(
            LocalDate.now(),
            sourceIn,
            reverseIn,
            random.nextBoolean(),
            journalEntryNumber.getAndIncrement() as int,
            glDetailsIn,
            BigDecimal.ZERO,
            message,
            postReversingEntryIn
         )
      }
   }

   static Stream<GeneralLedgerJournalEntryDTO> streamDTO(
      int numberIn = 1,
      GeneralLedgerSourceCodeDTO sourceIn,
      Boolean reverseIn,
      List<GeneralLedgerJournalEntryDetailDTO> glDetailsIn,
      Boolean postReversingEntryIn
   ) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final random = faker.random()
      final message = faker.lorem().sentence()

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerJournalEntryDTO(
            LocalDate.now(),
            sourceIn,
            reverseIn,
            random.nextBoolean(),
            journalEntryNumber.getAndIncrement() as int,
            glDetailsIn,
            BigDecimal.ZERO,
            message,
            postReversingEntryIn
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class GeneralLedgerJournalEntryDataLoaderService {

   static GeneralLedgerJournalEntryDTO singleDTO(
      GeneralLedgerSourceCodeDTO sourceIn,
      Boolean reverseIn,
      List<GeneralLedgerJournalEntryDetailDTO> glDetailsIn,
      Boolean postReversingEntryIn
   ) {
      return GeneralLedgerJournalEntryDataLoader.streamDTO(1, sourceIn, reverseIn, glDetailsIn, postReversingEntryIn)
         .findFirst().orElseThrow { new Exception("Unable to create General Ledger Journal Entry DTO") }
   }
}
