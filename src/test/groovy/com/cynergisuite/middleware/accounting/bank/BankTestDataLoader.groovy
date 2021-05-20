package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.Store
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

@CompileStatic
class BankFactory {

   private static final AtomicLong bankNumber = new AtomicLong(1)

   static Stream<BankEntity> stream(int numberIn = 1, Store generalLedgerProfitCenter, AccountEntity accountEntity) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()

      return IntStream.range(0, number).mapToObj {
         new BankEntity(
            null,
            bankNumber.getAndIncrement(),
            faker.company().name(),
            generalLedgerProfitCenter,
            accountEntity
         )
      }
   }

   static Stream<BankDTO> streamDTO(int numberIn = 1, Store generalLedgerProfitCenter, AccountEntity accountEntity) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()

      return IntStream.range(0, number).mapToObj {
         new BankDTO([
            'number': bankNumber.getAndIncrement(),
            'name': faker.company().name(),
            'generalLedgerProfitCenter': new SimpleLegacyIdentifiableDTO(generalLedgerProfitCenter.myId()),
            'generalLedgerAccount': new SimpleIdentifiableDTO(accountEntity.id)
         ])
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class BankFactoryService {
   private final BankRepository bankRepository

   @Inject
   BankFactoryService(BankRepository bankRepository) {
      this.bankRepository = bankRepository
   }

   Stream<BankEntity> stream(int numberIn = 1, Store generalLedgerProfitCenter, AccountEntity account) {
      return BankFactory.stream(numberIn, generalLedgerProfitCenter, account).map {
         bankRepository.insert(it)
      }
   }

   BankEntity single(CompanyEntity company, Store generalLedgerProfitCenter, AccountEntity account) {
      return stream(1, generalLedgerProfitCenter, account).findFirst().orElseThrow { new Exception("Unable to create Bank") }
   }

   BankDTO singleDTO(Store generalLedgerProfitCenter, AccountEntity account) {
      return BankFactory.streamDTO(1, generalLedgerProfitCenter, account).findFirst().orElseThrow { new Exception("Unable to create Bank") }
   }
}
