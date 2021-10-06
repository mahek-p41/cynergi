package com.cynergisuite.middleware.audit.detail.scan.area

import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import javax.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class AuditScanAreaFactory {

   static Stream<AuditScanAreaEntity> stream(int numberIn = 1, String nameIn, Store store, CompanyEntity company) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         final name = nameIn ?: lorem.word().capitalize()

         new AuditScanAreaEntity(
            null,
            name,
            store as StoreEntity,
            company
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AuditScanAreaFactoryService {
   private final AuditScanAreaRepository auditScanAreaTypeDomainRepository

   AuditScanAreaFactoryService(AuditScanAreaRepository auditScanAreaTypeDomainRepository) {
      this.auditScanAreaTypeDomainRepository = auditScanAreaTypeDomainRepository
   }

   Stream<AuditScanAreaEntity> stream(int numberIn = 1, String name, Store store, CompanyEntity company) {
      return AuditScanAreaFactory.stream(numberIn, name, store, company)
         .map { auditScanAreaTypeDomainRepository.insert(it) }
   }

   AuditScanAreaEntity single(String name, Store store, CompanyEntity company) {
      stream(1, name, store, company).findFirst().orElseThrow { new Exception("Unable to find AuditScanAreaTypeDomain") }
   }

   AuditScanAreaEntity showroom(Store store, CompanyEntity company) {
      single("Showroom", store, company)
   }

   AuditScanAreaEntity warehouse(Store store, CompanyEntity company) {
      single("Warehouse", store, company)
   }

   AuditScanAreaEntity storeroom(Store store, CompanyEntity company) {
      single("Storeroom", store, company)
   }
}
