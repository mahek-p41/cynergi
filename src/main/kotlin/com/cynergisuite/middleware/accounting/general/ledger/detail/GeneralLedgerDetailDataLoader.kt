package com.cynergisuite.middleware.accounting.general.ledger.detail

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure.GeneralLedgerDetailRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreEntity
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.math.RoundingMode
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

object GeneralLedgerDetailDataLoader {

   @JvmStatic
   fun stream(
      numberIn: Int = 1,
      account: AccountEntity,
      profitCenter: Store,
      source: GeneralLedgerSourceCodeEntity
   ): Stream<GeneralLedgerDetailEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val dateLocalDate = faker.date().future(5, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         GeneralLedgerDetailEntity(
            account = account,
            date = dateLocalDate,
            profitCenter = StoreEntity(profitCenter.myId(), profitCenter.myNumber(), profitCenter.myName(), profitCenter.myRegion(), profitCenter.myCompany()),
            source = source,
            amount = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            message = faker.lorem().sentence(),
            employeeNumberId = Random.nextInt(1, 1000000),
            journalEntryNumber = Random.nextInt(1, 1000000)
         )
      }
   }

   @JvmStatic
   fun streamDTO(
      numberIn: Int = 1,
      account: AccountEntity,
      profitCenter: Store,
      source: GeneralLedgerSourceCodeEntity
   ): Stream<GeneralLedgerDetailDTO> {
      return this.stream(numberIn, account, profitCenter, source).map { GeneralLedgerDetailDTO(it) }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class GeneralLedgerDetailDataLoaderService @Inject constructor(
   private val generalLedgerDetailRepository: GeneralLedgerDetailRepository
) {
   fun stream(
      numberIn: Int = 1,
      company: Company,
      account: AccountEntity,
      profitCenter: Store,
      source: GeneralLedgerSourceCodeEntity
   ): Stream<GeneralLedgerDetailEntity> {
      return GeneralLedgerDetailDataLoader.stream(
         numberIn,
         account,
         profitCenter,
         source
      ).map {
         generalLedgerDetailRepository.insert(it, company)
      }
   }

   fun single(
      company: Company,
      account: AccountEntity,
      profitCenter: Store,
      source: GeneralLedgerSourceCodeEntity
   ): GeneralLedgerDetailEntity {
      return stream(
         1,
         company,
         account,
         profitCenter,
         source
      ).findFirst().orElseThrow { Exception("Unable to create GeneralLedgerDetail") }
   }
}
