package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.AuditFactory
import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.detail.infrastructure.AuditDetailRepository
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanArea
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaFactory
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeFactory
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object AuditDetailFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, audit: AuditEntity, scannedByIn: EmployeeEntity? = null, scanAreaIn: AuditScanArea? = null): Stream<AuditDetailEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val scannedBy = scannedByIn ?: EmployeeFactory.single(audit.store.company)
      val faker = Faker()
      val barcode = faker.code()
      val commerce = faker.commerce()
      val company = faker.company()
      val idNumber = faker.idNumber()
      val scanArea = scanAreaIn ?: AuditScanAreaFactory.random()

      if (scannedBy.company != audit.store.company) {
         throw Exception("scannedBy.company did not match audit.store.company")
      }

      return IntStream.range(0, number).mapToObj {
         AuditDetailEntity(
            scanArea = scanArea,
            barcode = barcode.asin(),
            productCode = commerce.productName(),
            altId = barcode.asin(),
            serialNumber = idNumber.valid(),
            inventoryBrand = company.name(),
            inventoryModel = commerce.productName(),
            scannedBy = scannedBy,
            audit = SimpleIdentifiableEntity(audit)
         )
      }
   }

   @JvmStatic
   fun single(auditIn: AuditEntity, scannedByIn: EmployeeEntity? = null): AuditDetailEntity {
      return stream(1, auditIn, scannedByIn).findFirst().orElseThrow { Exception("Unable to create AuditDetail") }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AuditDetailFactoryService @Inject constructor(
   private val auditFactoryService: AuditFactoryService,
   private val auditDetailRepository: AuditDetailRepository,
   private val employeeFactoryService: EmployeeFactoryService
) {

   fun stream(numberIn: Int = 1, audit: AuditEntity, scannedByIn: EmployeeEntity? = null, scanAreaIn: AuditScanArea? = null): Stream<AuditDetailEntity> {
      val scannedIn = scannedByIn ?: employeeFactoryService.single(audit.store)

      return AuditDetailFactory.stream(numberIn, audit, scannedIn, scanAreaIn)
         .map {
            auditDetailRepository.insert(it)
         }
   }

   fun generate(numberIn: Int = 1, audit: AuditEntity, scannedByIn: EmployeeEntity? = null, scanAreaIn: AuditScanArea? = null) =
      stream(numberIn, audit, scannedByIn, scanAreaIn).forEach {  }

   fun single(audit: AuditEntity, scannedByIn: EmployeeEntity? = null): AuditDetailEntity {
      return single(audit, scannedByIn, null)
   }

   fun single(audit: AuditEntity, scannedByIn: EmployeeEntity? = null, scanAreaIn: AuditScanArea? = null): AuditDetailEntity {
      return stream(1, audit, scannedByIn, scanAreaIn).findFirst().orElseThrow { Exception("Unable to create AuditDetail") }
   }
}
