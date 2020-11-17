package com.cynergisuite.middleware.accounting.bank.reconciliation

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.bank.BankEntity
import com.cynergisuite.middleware.accounting.bank.reconciliation.infrastructure.BankReconciliationRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationType
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
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
   fun stream(numberIn: Int = 1, companyIn: Company, bankIn: BankEntity, typeIn: BankReconciliationType, dateIn: LocalDate, clearedDateIn: LocalDate? = null): Stream<BankReconciliationEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val numbers = faker.number()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         BankReconciliationEntity(
            company = CompanyEntity.create(companyIn)!!,
            bank = bankIn,
            type = typeIn,
            date = dateIn,
            clearedDate = clearedDateIn,
            amount = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            description = lorem.words(2).toString(),
            document = numbers.numberBetween(1, 999)
         )
      }
   }

   @JvmStatic
   fun streamDTO(numberIn: Int = 1, bankIn: BankEntity, typeIn: BankReconciliationType, dateIn: LocalDate, clearedDateIn: LocalDate? = null): Stream<BankReconciliationDTO> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val numbers = faker.number()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         BankReconciliationDTO(
            bank = SimpleIdentifiableDTO(bankIn.id),
            type = SimpleIdentifiableDTO(typeIn.id),
            date = dateIn,
            clearedDate = clearedDateIn,
            amount = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            description = lorem.words(2).toString(),
            document = numbers.numberBetween(1, 999)
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class BankReconciliationDataLoaderService @Inject constructor(
   private val repository: BankReconciliationRepository
) {

   fun stream(numberIn: Int = 1, companyIn: Company, bankIn: BankEntity, typeIn: BankReconciliationType, dateIn: LocalDate, clearedDateIn: LocalDate? = null): Stream<BankReconciliationEntity> {
      return BankReconciliationDataLoader.stream(numberIn, companyIn, bankIn, typeIn, dateIn, clearedDateIn).map {
         repository.insert(it)
      }
   }

   fun single(companyIn: Company, bankIn: BankEntity, typeIn: BankReconciliationType, dateIn: LocalDate, clearedDateIn: LocalDate? = null): BankReconciliationEntity {
      return stream(1, companyIn, bankIn, typeIn, dateIn, clearedDateIn).findFirst().orElseThrow { Exception("Unable to create BankReconciliation") }
   }

   fun singleDTO(bankIn: BankEntity, typeIn: BankReconciliationType, dateIn: LocalDate, clearedDateIn: LocalDate? = null): BankReconciliationDTO {
      return BankReconciliationDataLoader.streamDTO(1, bankIn, typeIn, dateIn, clearedDateIn).findFirst().orElseThrow { Exception("Unable to create BankReconciliation") }
   }
}
