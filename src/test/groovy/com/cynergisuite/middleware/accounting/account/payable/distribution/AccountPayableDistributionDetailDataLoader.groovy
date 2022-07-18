package com.cynergisuite.middleware.accounting.account.payable.distribution

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure.AccountPayableDistributionDetailRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreDTO
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

import java.util.stream.IntStream
import java.util.stream.Stream

import static java.math.RoundingMode.HALF_EVEN

@CompileStatic
class AccountPayableDistributionDetailDataLoader {

   static Stream<AccountPayableDistributionDetailEntity> stream(int numberIn = 1, Store profitCenter, AccountEntity account, AccountPayableDistributionTemplateEntity template) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final lorem = faker.lorem()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new AccountPayableDistributionDetailEntity(
            null,
            profitCenter,
            account,
            random.nextInt(1, 100).toBigDecimal().divide(BigDecimal.valueOf(100)).setScale(7, HALF_EVEN),
            template
         )
      }
   }

   static Stream<AccountPayableDistributionDetailDTO> streamDTO(int numberIn = 1, StoreDTO profitCenter, AccountDTO account, AccountPayableDistributionTemplateDTO template) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final lorem = faker.lorem()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new AccountPayableDistributionDetailDTO(
            [
               'profitCenter': profitCenter,
               'account': account,
               'percent': random.nextInt(1, 100).toBigDecimal().divide(BigDecimal.valueOf(100)).setScale(7, HALF_EVEN),
               'distributionTemplate': template
            ]
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AccountPayableDistributionDetailDataLoaderService {
   private final AccountPayableDistributionDetailRepository repository

   AccountPayableDistributionDetailDataLoaderService(AccountPayableDistributionDetailRepository repository) {
      this.repository = repository
   }

   Stream<AccountPayableDistributionDetailEntity> stream(int numberIn = 1, Store profitCenterIn, AccountEntity accountIn, CompanyEntity companyIn, AccountPayableDistributionTemplateEntity template) {
      return AccountPayableDistributionDetailDataLoader.stream(numberIn, profitCenterIn, accountIn, template).map {
         repository.insert(it, companyIn)
      }
   }

   AccountPayableDistributionDetailEntity single(Store profitCenterIn, AccountEntity accountIn, CompanyEntity companyIn, AccountPayableDistributionTemplateEntity template) {
      return AccountPayableDistributionDetailDataLoader.stream(1, profitCenterIn, accountIn, template)
         .map { repository.insert(it, companyIn) }
         .findFirst().orElseThrow { new Exception("Unable to create AccountPayableDistribution") }
   }

   AccountPayableDistributionDetailDTO singleDTO(StoreDTO profitCenterIn, AccountDTO accountIn, AccountPayableDistributionTemplateDTO template) {
      return AccountPayableDistributionDetailDataLoader.streamDTO(1, profitCenterIn, accountIn, template).findFirst().orElseThrow { new Exception("Unable to create AccountPayableDistributionDetail") }
   }
}
