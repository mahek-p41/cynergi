package com.cynergisuite.middleware.accounting.general.ledger.reversal

import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.cynergisuite.middleware.accounting.general.ledger.reversal.infrastructure.GeneralLedgerReversalRepository
import com.cynergisuite.middleware.company.Company
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.time.LocalDate
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

object GeneralLedgerReversalDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, sourceIn: GeneralLedgerSourceCodeEntity): Stream<GeneralLedgerReversalEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         GeneralLedgerReversalEntity(
            source = sourceIn,
            date = LocalDate.now(),
            reversalDate = LocalDate.now(),
            comment = lorem.sentence(),
            entryMonth = Random.nextInt(1, 12),
            entryNumber = Random.nextInt(1, 1000000)
         )
      }
   }

   @JvmStatic
   fun streamDTO(numberIn: Int = 1, sourceIn: GeneralLedgerSourceCodeDTO): Stream<GeneralLedgerReversalDTO> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         GeneralLedgerReversalDTO(
            source = sourceIn,
            date = LocalDate.now(),
            reversalDate = LocalDate.now(),
            comment = lorem.sentence(),
            entryMonth = Random.nextInt(1, 12),
            entryNumber = Random.nextInt(1, 1000000)
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class GeneralLedgerReversalDataLoaderService @Inject constructor(
   private val repository: GeneralLedgerReversalRepository
) {

   fun stream(numberIn: Int = 1, company: Company, sourceIn: GeneralLedgerSourceCodeEntity): Stream<GeneralLedgerReversalEntity> {
      return GeneralLedgerReversalDataLoader.stream(numberIn, sourceIn)
         .map { repository.insert(it, company) }
   }

   fun single(company: Company, sourceIn: GeneralLedgerSourceCodeEntity): GeneralLedgerReversalEntity {
      return stream(1, company, sourceIn).findFirst().orElseThrow { Exception("Unable to find GeneralLedgerReversal") }
   }

   fun singleDTO(sourceIn: GeneralLedgerSourceCodeDTO): GeneralLedgerReversalDTO {
      return GeneralLedgerReversalDataLoader.streamDTO(1, sourceIn).findFirst().orElseThrow { Exception("Unable to create GeneralLedgerReversal") }
   }
}
