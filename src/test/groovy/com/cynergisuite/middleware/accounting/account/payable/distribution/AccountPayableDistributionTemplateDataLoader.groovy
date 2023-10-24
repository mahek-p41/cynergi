package com.cynergisuite.middleware.accounting.account.payable.distribution

import com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure.AccountPayableDistributionTemplateRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class AccountPayableDistributionTemplateDataLoader {

   static Stream<AccountPayableDistributionTemplateEntity> stream(int numberIn = 1) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         new AccountPayableDistributionTemplateEntity(
            null,
            lorem.characters(3, 10)
         )
      }
   }

   static Stream<AccountPayableDistributionTemplateDTO> streamDTO(int numberIn = 1) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         new AccountPayableDistributionTemplateDTO(
            [
               'name': lorem.characters(3, 10)
            ]
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AccountPayableDistributionTemplateDataLoaderService {
   private final AccountPayableDistributionTemplateRepository repository

   AccountPayableDistributionTemplateDataLoaderService(AccountPayableDistributionTemplateRepository repository) {
      this.repository = repository
   }

   Stream<AccountPayableDistributionTemplateEntity> stream(int numberIn = 1, CompanyEntity companyIn) {
      return AccountPayableDistributionTemplateDataLoader.stream(numberIn ).map {
         repository.insert(it, companyIn)
      }
   }

   AccountPayableDistributionTemplateEntity single(int numberIn = 1, CompanyEntity companyIn) {
      return AccountPayableDistributionTemplateDataLoader.stream(numberIn)
         .map { repository.insert(it, companyIn) }
         .findFirst().orElseThrow { new Exception("Unable to create AccountPayableDistribution") }
   }

   AccountPayableDistributionTemplateDTO singleDTO() {
      return AccountPayableDistributionTemplateDataLoader.streamDTO(1).findFirst().orElseThrow { new Exception("Unable to create AccountPayableDistributionTemplate") }
   }
}
