package com.cynergisuite.middleware.accounting.account.payable.distribution

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableEntity
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure.AccountPayableDistributionRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreDTO
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import jakarta.inject.Singleton


import static java.math.RoundingMode.HALF_EVEN

@CompileStatic
class AccountPayableDistributionDataLoader {

   static Stream<AccountPayableDistributionEntity> stream(int numberIn = 1, Store profitCenter, AccountEntity account, String name = null) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final lorem = faker.lorem()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new AccountPayableDistributionEntity(
            null,
            name ?: lorem.characters(5, 10),
            profitCenter,
            account,
            random.nextInt(1, 100).toBigDecimal().divide(BigDecimal.valueOf(100)).setScale(7, HALF_EVEN)
         )
      }
   }

   static Stream<AccountPayableDistributionDTO> streamDTO(int numberIn = 1, SimpleLegacyIdentifiableDTO profitCenter, AccountDTO account, String name = null) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final lorem = faker.lorem()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new AccountPayableDistributionDTO(
            [
               'name': name ?: lorem.characters(5, 10),
               'profitCenter': profitCenter,
               'account': account,
               'percent': random.nextInt(1, 100).toBigDecimal().divide(BigDecimal.valueOf(100)).setScale(7, HALF_EVEN)
            ]
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AccountPayableDistributionDataLoaderService {
   private final AccountPayableDistributionRepository repository

   AccountPayableDistributionDataLoaderService(AccountPayableDistributionRepository repository) {
      this.repository = repository
   }

   Stream<AccountPayableDistributionEntity> stream(int numberIn = 1, Store profitCenterIn, AccountEntity accountIn, CompanyEntity companyIn) {
      return AccountPayableDistributionDataLoader.stream(numberIn, profitCenterIn, accountIn).map {
         repository.insert(it, companyIn)
      }
   }

   AccountPayableDistributionEntity single(Store profitCenterIn, AccountEntity accountIn, CompanyEntity companyIn, String nameIn = null) {
      return AccountPayableDistributionDataLoader.stream(1, profitCenterIn, accountIn, nameIn)
         .map { repository.insert(it, companyIn) }
         .findFirst().orElseThrow { new Exception("Unable to create AccountPayableDistribution") }
   }

   AccountPayableDistributionDTO singleDTO(SimpleLegacyIdentifiableDTO profitCenterIn, AccountDTO accountIn, String nameIn = null) {
      return AccountPayableDistributionDataLoader.streamDTO(1, profitCenterIn, accountIn, nameIn).findFirst().orElseThrow { new Exception("Unable to create AccountPayableDistribution") }
   }
}
