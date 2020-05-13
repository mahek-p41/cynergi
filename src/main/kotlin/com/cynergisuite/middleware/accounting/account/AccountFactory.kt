package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.company.Company
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

object AccountFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, company: Company): Stream<AccountEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         AccountEntity(
            company = company,
            description = lorem.sentence(5,3),
            type = AccountTypeFactory.random(),
            normalAccountBalance = NormalAccountBalanceFactory.random(),
            status = AccountStatusFactory.random(),
            form1099Field = Random.nextInt(1,20),
            corporateAccountIndicator = Random.nextBoolean()
         )
      }
   }

   @JvmStatic
   fun streamValueObject(numberIn: Int = 1, company: Company): Stream<AccountDTO> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         AccountDTO(
            description = lorem.sentence(5,3),
            type = AccountTypeValueObject(AccountTypeFactory.random()),
            normalAccountBalance = NormalAccountBalanceTypeValueObject(NormalAccountBalanceFactory.random()),
            status = AccountStatusTypeValueObject(AccountStatusFactory.random()),
            form1099Field = Random.nextInt(1, 20),
            corporateAccountIndicator = Random.nextBoolean()
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AccountFactoryService @Inject constructor(
   private val accountRepository: AccountRepository
) {

   fun stream(numberIn: Int = 1, company: Company): Stream<AccountEntity> {
      return AccountFactory.stream(numberIn, company).map {
         accountRepository.insert(it)
      }
   }

   fun single(company: Company): AccountEntity {
      return stream(1, company).findFirst().orElseThrow { Exception("Unable to create Account")}
   }

   fun singleValueObject(company: Company): AccountDTO {
      return AccountFactory.streamValueObject(1, company).findFirst().orElseThrow { Exception("Unable to create Account")}
   }
}
