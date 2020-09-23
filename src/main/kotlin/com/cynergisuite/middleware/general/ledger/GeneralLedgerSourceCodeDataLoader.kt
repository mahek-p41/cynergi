package com.cynergisuite.middleware.general.ledger

import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaEntity
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaFactory
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreEntity
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object GeneralLedgerSourceCodeDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, company: Company): Stream<GeneralLedgerSourceCodeEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         GeneralLedgerSourceCodeEntity(
            company = company as CompanyEntity,
            value = ,
            description = description
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class GeneralLedgerSourceCodeDataLoaderService @Inject constructor(
   private val repository: GeneralLedgerSourceCodeRepository
) {

   fun stream(numberIn: Int = 1, company: Company): Stream<GeneralLedgerSourceCodeEntity> {
      return GeneralLedgerSourceCodeDataLoader.stream(numberIn, company)
         .filter {
            it.company.myDataset() == company.myDataset()
         }
         .map { repository.insert(it, company) }
   }

   fun single(company: Company): GeneralLedgerSourceCodeEntity =
      stream(1, company).findFirst().orElseThrow { Exception("Unable to find GeneralLedgerSourceCode") }
}
