package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.company.Company
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object GeneralLedgerSourceCodeDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, company: Company): Stream<GeneralLedgerSourceCodeEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()

      return IntStream.range(0, number).mapToObj {
         val description = faker.lorem().word()
         val value = faker.lorem().characters(3).toUpperCase()
         GeneralLedgerSourceCodeEntity(
            company = company,
            value = value,
            description = description
         )
      }
   }

   @JvmStatic
   fun single(company: Company): GeneralLedgerSourceCodeEntity =
      stream(1, company).findFirst().orElseThrow { Exception("Unable to create GeneralLedgerSourceCodeEntity") }

   @JvmStatic
   fun streamDTO(numberIn: Int = 1): Stream<GeneralLedgerSourceCodeDTO> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val description = faker.lorem().word()
      val value = faker.lorem().characters(3).toUpperCase()

      return IntStream.range(0, number).mapToObj {
         GeneralLedgerSourceCodeDTO(
            value = value,
            description = description
         )
      }
   }

   @JvmStatic
   fun singleDTO(): GeneralLedgerSourceCodeDTO =
      streamDTO(1).findFirst().orElseThrow { Exception("Unable to create AccountPayableControl") }
}

@Singleton
@Requires(env = ["develop", "test"])
class GeneralLedgerSourceCodeDataLoaderService @Inject constructor(
   private val repository: GeneralLedgerSourceCodeRepository
) {

   fun stream(numberIn: Int = 1, company: Company): Stream<GeneralLedgerSourceCodeEntity> {
      return GeneralLedgerSourceCodeDataLoader.stream(numberIn, company)
         .filter {
            it.company.myDataset() == company.myDataset()
         }
         .map { repository.insert(it, company) }
   }

   fun single(company: Company): GeneralLedgerSourceCodeEntity =
      stream(1, company).findFirst().orElseThrow { Exception("Unable to find GeneralLedgerSourceCode") }
}
