package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import jakarta.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class GeneralLedgerSourceCodeDataLoader {

   static Stream<GeneralLedgerSourceCodeEntity> stream(int numberIn = 1) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()

      return IntStream.range(0, number).mapToObj {
         final description = faker.lorem().word()
         final value = faker.lorem().characters(3).toUpperCase()

         new GeneralLedgerSourceCodeEntity(
            null,
            value,
            description
          )
      }
   }

    static Stream<GeneralLedgerSourceCodeDTO> streamDTO(int numberIn = 1) {
       final number = numberIn > 0 ? numberIn : 1
       final faker = new Faker()
       final description = faker.lorem().word()
       final value = faker.lorem().characters(3).toUpperCase()

        return IntStream.range(0, number).mapToObj {
            new GeneralLedgerSourceCodeDTO([
               'value': value,
               'description': description
            ])
        }
    }

   static GeneralLedgerSourceCodeDTO singleDTO() {
      streamDTO(1).findFirst().orElseThrow { new Exception("Unable to create GeneralLedgerSourceCode") }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class GeneralLedgerSourceCodeDataLoaderService {
   private GeneralLedgerSourceCodeRepository repository

   GeneralLedgerSourceCodeDataLoaderService(GeneralLedgerSourceCodeRepository repository) {
      this.repository = repository
   }

   Stream<GeneralLedgerSourceCodeEntity> stream(int numberIn = 1, CompanyEntity company) {
      return GeneralLedgerSourceCodeDataLoader.stream(numberIn)
         .map { repository.insert(it, company) }
   }

   GeneralLedgerSourceCodeEntity single(CompanyEntity company) {
      stream(1, company).findFirst().orElseThrow { new Exception("Unable to find GeneralLedgerSourceCode") }
   }

   GeneralLedgerSourceCodeDTO singleDTO( ) {
      return GeneralLedgerSourceCodeDataLoader.singleDTO()
   }
}
