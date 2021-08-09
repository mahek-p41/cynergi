package com.cynergisuite.middleware.accounting.bank.reconciliation

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.bank.BankEntity
import com.cynergisuite.middleware.accounting.bank.reconciliation.infrastructure.BankReconciliationRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDataLoader
import com.cynergisuite.middleware.company.Company
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.math.RoundingMode
import java.time.LocalDate
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object BankReconciliationDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, bankIn: BankEntity, dateIn: LocalDate, clearedDateIn: LocalDate? = null): Stream<BankReconciliationEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val numbers = faker.number()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         BankReconciliationEntity(
            bank = bankIn,
            type = BankReconciliationTypeDataLoader.random(),
            date = dateIn,
            clearedDate = clearedDateIn,
            amount = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            description = lorem.word(),
            document = lorem.sentence(1,2)
         )
      }
   }

   @JvmStatic
   fun streamDTO(numberIn: Int = 1, bankIn: BankEntity, dateIn: LocalDate, clearedDateIn: LocalDate? = null): Stream<BankReconciliationDTO> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val numbers = faker.number()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         BankReconciliationDTO(
            bank = SimpleIdentifiableDTO(bankIn.id),
            type = BankReconciliationTypeDTO(BankReconciliationTypeDataLoader.random()),
            date = dateIn,
            clearedDate = clearedDateIn,
            amount = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            description = lorem.word(),
            document = lorem.sentence(1,2)
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class BankReconciliationDataLoaderService @Inject constructor(
   private val repository: BankReconciliationRepository
) {

   fun stream(numberIn: Int = 1, companyIn: Company, bankIn: BankEntity, dateIn: LocalDate, clearedDateIn: LocalDate? = null): Stream<BankReconciliationEntity> {
      return BankReconciliationDataLoader.stream(numberIn, bankIn, dateIn, clearedDateIn).map {
         repository.insert(it, companyIn)
      }
   }

   fun single(companyIn: Company, bankIn: BankEntity, dateIn: LocalDate, clearedDateIn: LocalDate? = null): BankReconciliationEntity {
      return stream(1, companyIn, bankIn, dateIn, clearedDateIn).findFirst().orElseThrow { Exception("Unable to create BankReconciliation") }
   }

   fun singleDTO(bankIn: BankEntity, dateIn: LocalDate, clearedDateIn: LocalDate? = null): BankReconciliationDTO {
      return BankReconciliationDataLoader.streamDTO(1, bankIn, dateIn, clearedDateIn).findFirst().orElseThrow { Exception("Unable to create BankReconciliation") }
   }
}
