package com.cynergisuite.middleware.inventory

import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.inventory.infrastructure.InventoryEomRepository
import com.cynergisuite.middleware.store.Store
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

import java.math.RoundingMode
import java.time.LocalDate
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class InventoryEndOfMonthDataLoader {

   static Stream<InventoryEndOfMonthEntity> stream(int numberIn = 1, AccountEntity assetAccountIn, AccountEntity contraAccountIn, CompanyEntity companyIn, Store profitCenterIn, LocalDate dateIn) {
      final number = numberIn > 0 ? numberIn : 1
      final lorem = new Faker().lorem()
      final random = new Faker().random()
      return IntStream.range(0, number).mapToObj {
         new InventoryEndOfMonthEntity(
            null,
            companyIn.id,
            profitCenterIn.myNumber(),
            dateIn.year.toInteger(),
            dateIn.month.value,
            lorem.word(),
            random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            assetAccountIn.id,
            contraAccountIn.id,
            lorem.word(),
            lorem.word(),
            random.nextInt(1,4),
            random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            dateIn.plusYears(1),
            random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            BigDecimal.ZERO,
            BigDecimal.ZERO
         )
      }
   }

   static Stream<InventoryEndOfMonthDTO> streamDTO(int numberIn = 1, AccountEntity assetAccountIn, AccountEntity contraAccountIn, CompanyEntity companyIn, Store profitCenterIn, LocalDate dateIn) {
      final number = numberIn > 0 ? numberIn : 1
      final random = new Faker().random()
      final lorem = new Faker().lorem()


      return IntStream.range(0, number).mapToObj {
         new InventoryEndOfMonthDTO(
            null,
            companyIn.id,
            profitCenterIn.myNumber(),
            dateIn.year.toInteger(),
            dateIn.month.value,
            lorem.word(),
            random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            assetAccountIn.id,
            contraAccountIn.id,
            lorem.word(),
            lorem.word(),
            random.nextInt(1,4),
            random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            dateIn.plusYears(1),
            random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            BigDecimal.ZERO,
            BigDecimal.ZERO
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class InventoryEndOfMonthDataLoaderService {
   private final InventoryEomRepository inventoryEomRepository

   InventoryEndOfMonthDataLoaderService(InventoryEomRepository inventoryEomRepository) {
      this.inventoryEomRepository = inventoryEomRepository
   }

   Stream<InventoryEndOfMonthEntity> stream(int numberIn = 1, CompanyEntity companyIn, AccountEntity assetAccountIn, AccountEntity contraAccountIn, Store profitCenterIn, LocalDate dateIn) {
      return InventoryEndOfMonthDataLoader.stream(numberIn, assetAccountIn, contraAccountIn, companyIn, profitCenterIn, dateIn).map {
         inventoryEomRepository.insert(it, companyIn)
      }
   }

}
