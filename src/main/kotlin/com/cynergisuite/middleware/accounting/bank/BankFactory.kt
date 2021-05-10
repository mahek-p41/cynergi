package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.store.Store
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object BankFactory {

   @JvmStatic
   private val bankNumber = AtomicLong(1)

   @JvmStatic
   fun stream(numberIn: Int = 1, generalLedgerProfitCenter: Store, accountEntity: AccountEntity): Stream<BankEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()

      return IntStream.range(0, number).mapToObj {
         BankEntity(
            number = bankNumber.getAndIncrement(),
            name = faker.company().name(),
            generalLedgerProfitCenter = generalLedgerProfitCenter,
            generalLedgerAccount = accountEntity
         )
      }
   }

   @JvmStatic
   fun streamDTO(numberIn: Int = 1, generalLedgerProfitCenter: Store, accountEntity: AccountEntity): Stream<BankDTO> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()

      return IntStream.range(0, number).mapToObj {
         BankDTO(
            number = bankNumber.getAndIncrement(),
            name = faker.company().name(),
            generalLedgerProfitCenter = SimpleIdentifiableDTO(generalLedgerProfitCenter.myId()),
            generalLedgerAccount = SimpleIdentifiableDTO(accountEntity.id)
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class BankFactoryService @Inject constructor(
   private val bankRepository: BankRepository
) {

   fun stream(numberIn: Int = 1, generalLedgerProfitCenter: Store, account: AccountEntity): Stream<BankEntity> {
      return BankFactory.stream(numberIn, generalLedgerProfitCenter, account).map {
         bankRepository.insert(it)
      }
   }

   fun single(company: Company, generalLedgerProfitCenter: Store, account: AccountEntity): BankEntity {
      return stream(1, generalLedgerProfitCenter, account).findFirst().orElseThrow { Exception("Unable to create Bank") }
   }

   fun singleDTO(generalLedgerProfitCenter: Store, account: AccountEntity): BankDTO {
      return BankFactory.streamDTO(1, generalLedgerProfitCenter, account).findFirst().orElseThrow { Exception("Unable to create Bank") }
   }
}
