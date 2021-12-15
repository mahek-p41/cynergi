package com.cynergisuite.middleware.vendor.rebate

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.AccountStatusFactory
import com.cynergisuite.middleware.accounting.account.AccountStatusTypeValueObject
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.vendor.VendorEntity
import com.cynergisuite.middleware.vendor.rebate.infrastructure.RebateRepository
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import javax.inject.Inject
import javax.inject.Singleton
import java.math.RoundingMode
import java.util.stream.IntStream
import java.util.stream.Stream

class RebateTestDataLoader {

   static Stream<RebateEntity> stream(int numberIn = 1, List<Identifiable> vendors, AccountEntity generalLedgerDebitAccount, AccountEntity generalLedgerCreditAccount) {
      final number = numberIn < 0 ? 1 : numberIn
      final faker = new Faker()
      final random = faker.random()
      final lorem = faker.lorem()
      final numbers = faker.number()
      final percent = random.nextInt(1, 100).toBigDecimal().divide(new BigDecimal(100)).setScale(7, RoundingMode.HALF_EVEN)
      final amountPerUnit = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
      final bool = random.nextBoolean()
      final accrualIndicator = random.nextBoolean()

      return IntStream.range(0, number).mapToObj {
         new RebateEntity(
            null,
            vendors as List<Identifiable>,
            AccountStatusFactory.random(),
            lorem.words(2).toString(),
            RebateTypeDataLoader.random(),
            bool ? percent : null,
            !bool ? amountPerUnit : null,
            accrualIndicator,
            generalLedgerDebitAccount ?: findIt(accrualIndicator, generalLedgerCreditAccount, bool),
            generalLedgerCreditAccount
         )
      }
   }

   private static findIt(boolean accrualIndicator, AccountEntity generalLedgerDebitAccount, boolean bool) {
      if (accrualIndicator) {
         return generalLedgerDebitAccount
      } else if (bool) {
         return generalLedgerDebitAccount
      } else {
         return null
      }
   }

   static Stream<RebateDTO> streamDTO(int numberIn = 1, List<SimpleIdentifiableDTO> vendorsIn, AccountDTO generalLedgerDebitAccountIn, AccountDTO generalLedgerCreditAccountIn) {
      final number = numberIn < 0 ? 1 : numberIn
      final faker = new Faker()
      final random = faker.random()
      final lorem = faker.lorem()
      final numbers = faker.number()
      final percent = random.nextInt(1, 100).toBigDecimal().divide(new BigDecimal(100)).setScale(7, RoundingMode.HALF_EVEN)
      final amountPerUnit = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
      final bool = random.nextBoolean()

      return IntStream.range(0, number).mapToObj {
         new RebateDTO(
            null,
            vendorsIn,
            new AccountStatusTypeValueObject(AccountStatusFactory.random()),
            lorem.words(2).toString(),
            new RebateTypeDTO(RebateTypeDataLoader.random()),
            bool ? percent : null,
            !bool ? amountPerUnit : null,
            random.nextBoolean(),
            generalLedgerDebitAccountIn,
            generalLedgerCreditAccountIn
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class RebateTestDataLoaderService {
   private final RebateRepository rebateRepository

   @Inject
   RebateTestDataLoaderService(RebateRepository rebateRepository) {
      this.rebateRepository = rebateRepository
   }

   Stream<RebateEntity> stream(int numberIn = 1, CompanyEntity companyIn, List<Identifiable> vendorsIn, AccountEntity generalLedgerDebitAccountIn, AccountEntity generalLedgerCreditAccountIn) {
      return RebateTestDataLoader.stream(numberIn, vendorsIn, generalLedgerDebitAccountIn, generalLedgerCreditAccountIn)
         .map { rebateRepository.insert(it, companyIn) }
   }

   RebateEntity single(CompanyEntity companyIn, List<Identifiable> vendorsIn, AccountEntity generalLedgerDebitAccountIn, AccountEntity generalLedgerCreditAccountIn) {
      return stream(1, companyIn, vendorsIn, generalLedgerDebitAccountIn, generalLedgerCreditAccountIn).findFirst().orElseThrow { new Exception("Unable to create RebateEntity") }
   }

   RebateDTO singleDTO(List<SimpleIdentifiableDTO> vendorsIn, AccountDTO generalLedgerDebitAccountIn, AccountDTO generalLedgerCreditAccountIn) {
      return RebateTestDataLoader.streamDTO(1, vendorsIn, generalLedgerDebitAccountIn, generalLedgerCreditAccountIn).findFirst().orElseThrow { new Exception("Unable to create Rebate") }
   }

   def assignVendorsToRebate(RebateEntity rebate, List<VendorEntity> vendors) {
      vendors.forEach { rebateRepository.assignVendorToRebate(rebate, new SimpleIdentifiableDTO(it)) }
   }

   def disassociateVendorFromRebate(RebateEntity rebate, List<VendorEntity> vendors) {
      vendors.forEach { rebateRepository.disassociateVendorFromRebate(rebate, new SimpleIdentifiableDTO(it)) }
   }
}
