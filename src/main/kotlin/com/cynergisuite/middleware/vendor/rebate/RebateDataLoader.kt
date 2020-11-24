package com.cynergisuite.middleware.vendor.rebate

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.AccountStatusFactory
import com.cynergisuite.middleware.accounting.account.AccountStatusTypeValueObject
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.vendor.rebate.infrastructure.RebateRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object RebateDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, vendor: Identifiable, generalLedgerDebitAccount: AccountEntity, generalLedgerCreditAccount: AccountEntity): Stream<RebateEntity> {
      val number = if (numberIn < 0) 1 else numberIn
      val faker = Faker()
      val random = faker.random()
      val lorem = faker.lorem()
      val numbers = faker.number()
      val percent = random.nextInt(1, 100).toBigDecimal().divide(BigDecimal(100)).setScale(7, RoundingMode.HALF_EVEN)
      val amountPerUnit = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
      val boolean = random.nextBoolean()
      val accrualIndicator = random.nextBoolean()

      return IntStream.range(0, number).mapToObj {
         RebateEntity(
            id = null,
            vendor = vendor,
            status = AccountStatusFactory.random(),
            description = lorem.words(2).toString(),
            rebate = RebateTypeDataLoader.random(),
            percent = if (boolean) percent else null,
            amountPerUnit = if (!boolean) amountPerUnit else null,
            accrualIndicator = accrualIndicator,
            generalLedgerDebitAccount = if (accrualIndicator) generalLedgerDebitAccount else if (boolean) generalLedgerDebitAccount else null,
            generalLedgerCreditAccount = generalLedgerCreditAccount
         )
      }
   }

   @JvmStatic
   fun streamDTO(numberIn: Int = 1, vendorIn: SimpleIdentifiableDTO, generalLedgerDebitAccountIn: SimpleIdentifiableDTO, generalLedgerCreditAccountIn: SimpleIdentifiableDTO): Stream<RebateDTO> {
      val number = if (numberIn < 0) 1 else numberIn
      val faker = Faker()
      val random = faker.random()
      val lorem = faker.lorem()
      val numbers = faker.number()
      val percent = random.nextInt(1, 100).toBigDecimal().divide(BigDecimal(100)).setScale(7, RoundingMode.HALF_EVEN)
      val amountPerUnit = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
      val boolean = random.nextBoolean()

      return IntStream.range(0, number).mapToObj {
         RebateDTO(
            id = null,
            vendor = vendorIn,
            status = AccountStatusTypeValueObject(AccountStatusFactory.random()),
            description = lorem.words(2).toString(),
            type = RebateTypeDTO(RebateTypeDataLoader.random()),
            percent = if (boolean) percent else null,
            amountPerUnit = if (!boolean) amountPerUnit else null,
            accrualIndicator = random.nextBoolean(),
            generalLedgerDebitAccount = generalLedgerDebitAccountIn,
            generalLedgerCreditAccount = generalLedgerCreditAccountIn
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class RebateDataLoaderService @Inject constructor(
   private val rebateRepository: RebateRepository
) {

   fun stream(numberIn: Int = 1, companyIn: Company, vendorIn: Identifiable, generalLedgerDebitAccountIn: AccountEntity, generalLedgerCreditAccountIn: AccountEntity): Stream<RebateEntity> {
      return RebateDataLoader.stream(numberIn, vendorIn, generalLedgerDebitAccountIn, generalLedgerCreditAccountIn)
         .map { rebateRepository.insert(it, companyIn) }
   }

   fun single(companyIn: Company, vendorIn: Identifiable, generalLedgerDebitAccountIn: AccountEntity, generalLedgerCreditAccountIn: AccountEntity): RebateEntity {
      return stream(companyIn = companyIn, vendorIn = vendorIn, generalLedgerDebitAccountIn = generalLedgerDebitAccountIn, generalLedgerCreditAccountIn = generalLedgerCreditAccountIn).findFirst().orElseThrow { Exception("Unable to create RebateEntity") }
   }

   fun singleDTO(vendorIn: SimpleIdentifiableDTO, generalLedgerDebitAccountIn: SimpleIdentifiableDTO, generalLedgerCreditAccountIn: SimpleIdentifiableDTO): RebateDTO {
      return RebateDataLoader.streamDTO(1, vendorIn, generalLedgerDebitAccountIn, generalLedgerCreditAccountIn).findFirst().orElseThrow { Exception("Unable to create Rebate") }
   }
}
