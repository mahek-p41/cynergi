package com.cynergisuite.middleware.audit.exception

import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.AuditFactory
import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanArea
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaFactory
import com.cynergisuite.middleware.audit.exception.infrastructure.AuditExceptionRepository
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeFactory
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import org.apache.commons.lang3.RandomUtils
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object AuditExceptionFactory {

   @JvmStatic
   private val exceptionCodes = listOf(
      "Not found in inventory",
      "Unit is currently on rent",
      "Unit was not scanned",
      "Unit at different location",
      "Pending transfer"
   )

   @JvmStatic
   fun randomExceptionCode(): String {
      return exceptionCodes[RandomUtils.nextInt(0, exceptionCodes.size)]
   }

   @JvmStatic
   fun stream(numberIn: Int = 1, auditIn: AuditEntity? = null, scannedByIn: EmployeeEntity? = null, scanAreaIn: AuditScanArea? = null): Stream<AuditExceptionEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val random = faker.random()
      val lorem = faker.lorem()
      val scannedBy = scannedByIn ?: EmployeeFactory.single()
      val scanArea = scanAreaIn ?: AuditScanAreaFactory.random()
      val audit = auditIn ?: AuditFactory.single()

      return IntStream.range(0, number).mapToObj {
         AuditExceptionEntity(
            scanArea = scanArea,
            barcode = lorem.characters(10).toUpperCase(),
            productCode = if(random.nextBoolean()) lorem.characters(2, 3).toUpperCase() else null,
            altId = if (random.nextBoolean()) lorem.characters(5, 10).toUpperCase() else null,
            serialNumber = if (random.nextBoolean()) lorem.characters(10, 15).toUpperCase() else null,
            inventoryBrand = lorem.characters(3),
            inventoryModel = if (random.nextBoolean()) lorem.characters(10, 18) else null,
            scannedBy = scannedBy,
            exceptionCode = randomExceptionCode(),
            signedOff = random.nextBoolean(),
            lookupKey = if (random.nextBoolean()) lorem.characters(10).toUpperCase() else null,
            audit = SimpleIdentifiableEntity(audit)
         )
      }
   }

   @JvmStatic
   fun single(): AuditExceptionEntity {
      return single(AuditFactory.single())
   }

   @JvmStatic
   fun single(auditIn: AuditEntity): AuditExceptionEntity {
      return single(auditIn, scannedByIn = null)
   }

   @JvmStatic
   fun single(auditIn: AuditEntity, scannedByIn: EmployeeEntity?): AuditExceptionEntity {
      return stream(1, auditIn, scannedByIn).findFirst().orElseThrow { Exception("Unable to create AuditDiscrepancy") }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AuditExceptionFactoryService @Inject constructor(
   private val auditFactoryService: AuditFactoryService,
   private val auditExceptionRepository: AuditExceptionRepository,
   private val employeeFactoryService: EmployeeFactoryService
) {

   fun stream(numberIn: Int = 1, auditIn: AuditEntity? = null, scannedByIn: EmployeeEntity? = null, scanAreaIn: AuditScanArea? = null): Stream<AuditExceptionEntity> {
      val audit = auditIn ?: auditFactoryService.single()
      val scannedBy = scannedByIn ?: employeeFactoryService.single()

      return AuditExceptionFactory.stream(numberIn, audit, scannedBy)
         .map {
            auditExceptionRepository.insert(it)
         }
   }

   fun generate(numberIn: Int = 1, auditIn: AuditEntity? = null, scannedByIn: EmployeeEntity? = null, scanAreaIn: AuditScanArea? = null) =
      stream(numberIn, auditIn, scannedByIn, scanAreaIn).forEach {  }

   fun single(): AuditExceptionEntity =
      single(auditFactoryService.single())

   fun single(auditIn: AuditEntity): AuditExceptionEntity =
      single(auditIn, null)

   fun single(auditIn: AuditEntity, scannedByIn: EmployeeEntity?): AuditExceptionEntity =
      stream(1, auditIn, scannedByIn).findFirst().orElseThrow { Exception("Unable to create AuditDiscrepancy") }
}
