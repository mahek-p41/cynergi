package com.cynergisuite.middleware.accounting.bank.reconciliation

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.bank.BankEntity
import com.cynergisuite.middleware.accounting.bank.reconciliation.infrastructure.BankReconciliationRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDataLoader
import com.cynergisuite.middleware.company.CompanyEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import java.math.RoundingMode
import java.time.LocalDate
import java.util.stream.IntStream
import java.util.stream.Stream
import jakarta.inject.Singleton

@CompileStatic
class BankReconciliationDataLoader {

   static Stream<BankReconciliationEntity> stream(int numberIn = 1, BankEntity bankIn, LocalDate dateIn, LocalDate clearedDateIn = null) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final numbers = faker.number()
      final lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         new BankReconciliationEntity(
            null,
            bankIn,
            BankReconciliationTypeDataLoader.random(),
            dateIn,
            clearedDateIn,
            numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            lorem.characters(3, 15),
            lorem.characters(3, 20)
         )
      }
   }

   static Stream<BankReconciliationDTO> streamDTO(int numberIn = 1, BankEntity bankIn, LocalDate dateIn, LocalDate clearedDateIn = null) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final numbers = faker.number()
      final lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         new BankReconciliationDTO([
            'bank': new SimpleIdentifiableDTO(bankIn.id),
            'type': new BankReconciliationTypeDTO(BankReconciliationTypeDataLoader.random()),
            'date': dateIn,
            'clearedDate': clearedDateIn,
            'amount': numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            'description': lorem.characters(3, 15),
            'document': lorem.characters(3, 20)
         ])
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class BankReconciliationDataLoaderService {
   private BankReconciliationRepository repository

   BankReconciliationDataLoaderService(BankReconciliationRepository repository) {
      this.repository = repository
   }

   Stream<BankReconciliationEntity> stream(int numberIn = 1, CompanyEntity companyIn, BankEntity bankIn, LocalDate dateIn, LocalDate clearedDateIn = null) {
      return BankReconciliationDataLoader.stream(numberIn, bankIn, dateIn, clearedDateIn).map {
         repository.insert(it, companyIn)
      }
   }

   BankReconciliationEntity single(CompanyEntity companyIn, BankEntity bankIn, LocalDate dateIn, LocalDate clearedDateIn = null) {
      return stream(1, companyIn, bankIn, dateIn, clearedDateIn).findFirst().orElseThrow { new Exception("Unable to create BankReconciliation") }
   }

   BankReconciliationDTO singleDTO(BankEntity bankIn, LocalDate dateIn, LocalDate clearedDateIn = null) {
      return BankReconciliationDataLoader.streamDTO(1, bankIn, dateIn, clearedDateIn).findFirst().orElseThrow { new Exception("Unable to create BankReconciliation") }
   }
}
