package com.cynergisuite.middleware.accounting.general.ledger.detail

import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure.GeneralLedgerDetailRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.Store
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Inject
import jakarta.inject.Singleton

import java.math.RoundingMode
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class GeneralLedgerDetailDataLoader {

   static Stream<GeneralLedgerDetailEntity> stream(
      int numberIn = 1,
      AccountEntity account,
      Store profitCenter,
      GeneralLedgerSourceCodeEntity source
   ) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final dateLocalDate = faker.date().future(5, TimeUnit.DAYS).toInstant().atZone(ZoneId.of("-05:00")).toLocalDate()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerDetailEntity(
            null,
            account,
            profitCenter,
            dateLocalDate,
            source,
            random.nextInt(0, 1_000_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            faker.lorem().sentence(),
            random.nextInt(1, 1_000_000),
            random.nextInt(1, 1_000_000)
         )
      }
   }

   static Stream<GeneralLedgerDetailDTO> streamDTO(
      int numberIn = 1,
      AccountEntity account,
      Store profitCenter,
      GeneralLedgerSourceCodeEntity source
   ) {
      return stream(numberIn, account, profitCenter, source).map {new GeneralLedgerDetailDTO(it) }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class GeneralLedgerDetailDataLoaderService {
   private GeneralLedgerDetailRepository generalLedgerDetailRepository

   @Inject
   GeneralLedgerDetailDataLoaderService(GeneralLedgerDetailRepository generalLedgerDetailRepository) {
      this.generalLedgerDetailRepository = generalLedgerDetailRepository
   }

   Stream<GeneralLedgerDetailEntity> stream(
      int numberIn = 1,
      CompanyEntity company,
      AccountEntity account,
      Store profitCenter,
      GeneralLedgerSourceCodeEntity source
   ) {
      return GeneralLedgerDetailDataLoader.stream(
         numberIn,
         account,
         profitCenter,
         source
      ).map {
         generalLedgerDetailRepository.insert(it, company)
      }
   }

   Stream<GeneralLedgerDetailDTO> streamDTO(
      int numberIn = 1,
      AccountEntity account,
      Store profitCenter,
      GeneralLedgerSourceCodeEntity source
   ) {
      return GeneralLedgerDetailDataLoader.streamDTO(
         numberIn,
         account,
         profitCenter,
         source
      )
   }

   GeneralLedgerDetailEntity single(
      CompanyEntity company,
      AccountEntity account,
      Store profitCenter,
      GeneralLedgerSourceCodeEntity source
   ) {
      return stream(
         1,
         company,
         account,
         profitCenter,
         source
      ).findFirst().orElseThrow { new Exception("Unable to create GeneralLedgerDetail") }
   }

   GeneralLedgerDetailDTO singleDTO(
      AccountEntity account,
      Store profitCenter,
      GeneralLedgerSourceCodeEntity source
   ) {
      return GeneralLedgerDetailDataLoader.streamDTO(
         1,
         account,
         profitCenter,
         source
      ).findFirst().orElseThrow { new Exception("Unable to create GeneralLedgerDetailDTO") }
   }
}
