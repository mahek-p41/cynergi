package com.cynergisuite.middleware.audit.detail.scan.area

import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreEntity
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object AuditScanAreaFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, nameIn: String?, store: Store, company: Company): Stream<AuditScanAreaEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         val name = nameIn ?: lorem.word().capitalize()
         AuditScanAreaEntity(
            name = name,
            store = store as StoreEntity,
            company = company as CompanyEntity
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AuditScanAreaFactoryService @Inject constructor(
   private val auditScanAreaTypeDomainRepository: AuditScanAreaRepository
) {

   fun stream(numberIn: Int = 1, name: String?, store: Store, company: Company): Stream<AuditScanAreaEntity> {
      return AuditScanAreaFactory.stream(numberIn, name, store, company)
         .map { auditScanAreaTypeDomainRepository.insert(it) }
   }

   fun single(name: String, store: Store, company: Company): AuditScanAreaEntity =
      stream(1, name, store, company).findFirst().orElseThrow { Exception("Unable to find AuditScanAreaTypeDomain") }

   fun showroom(store: Store, company: Company): AuditScanAreaEntity =
      single("Showroom", store, company)

   fun warehouse(store: Store, company: Company): AuditScanAreaEntity =
      single("Warehouse", store, company)

   fun storeroom(store: Store, company: Company): AuditScanAreaEntity =
      single("Storeroom", store, company)
}
