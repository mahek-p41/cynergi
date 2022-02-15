package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.IntStream
import java.util.stream.Stream
import jakarta.inject.Inject
import jakarta.inject.Singleton

@CompileStatic
class AccountTestDataLoader {

   private static final AtomicLong accountNumber = new AtomicLong(1)

   static Stream<AccountEntity> stream(int numberIn = 1, CompanyEntity company, String name = null) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final lorem = faker.lorem()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new AccountEntity(
            null,
            accountNumber.getAndIncrement(),
            name ?: lorem.sentence(5, 3),
            AccountTypeFactory.random(),
            NormalAccountBalanceFactory.random(),
            AccountStatusFactory.random(),
            random.nextInt(1, 20),
            random.nextBoolean()
         )
      }
   }

   static Stream<AccountDTO> streamDTO(int numberIn = 1) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final lorem = faker.lorem()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new AccountDTO(
            null,
            lorem.sentence(5, 3),
            accountNumber.getAndIncrement(),
            new AccountTypeValueObject(AccountTypeFactory.random()),
            new NormalAccountBalanceTypeValueObject(NormalAccountBalanceFactory.random()),
            new AccountStatusTypeValueObject(AccountStatusFactory.random()),
            random.nextInt(1, 20),
            random.nextBoolean()
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AccountTestDataLoaderService {
   AccountRepository accountRepository

   @Inject
   AccountTestDataLoaderService(AccountRepository accountRepository) {
      this.accountRepository = accountRepository
   }

   Stream<AccountEntity> stream(int numberIn = 1, CompanyEntity company) {
      return AccountTestDataLoader.stream(numberIn, company).map {
         accountRepository.insert(it, company)
      }
   }

   AccountEntity single(CompanyEntity company, String name = null) {
      return AccountTestDataLoader.stream(1, company, name)
         .map { accountRepository.insert(it, company) }
         .findFirst().orElseThrow { new Exception("Unable to create Account") }
   }

   AccountDTO singleDTO(CompanyEntity company) {
      return AccountTestDataLoader.streamDTO(1).findFirst().orElseThrow { new Exception("Unable to create Account") }
   }
}
