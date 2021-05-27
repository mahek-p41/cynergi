package com.cynergisuite.middleware.audit.exception

import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaEntity
import com.cynergisuite.middleware.audit.exception.infrastructure.AuditExceptionRepository
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeFactory
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
   fun stream(numberIn: Int = 1, audit: AuditEntity, scanAreaIn: AuditScanAreaEntity, scannedByIn: EmployeeEntity? = null, approvedIn: Boolean? = null): Stream<AuditExceptionEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val random = faker.random()
      val lorem = faker.lorem()
      val scannedBy = scannedByIn ?: EmployeeFactory.single(audit.store.myCompany())
      val approved = approvedIn ?: random.nextBoolean()
      val approvedBy = if (approved) scannedByIn else null

      return IntStream.range(0, number).mapToObj {
         AuditExceptionEntity(
            scanArea = scanAreaIn,
            barcode = if (random.nextBoolean()) lorem.characters(10).toUpperCase() else null,
            productCode = if (random.nextBoolean()) lorem.characters(2, 3).toUpperCase() else null,
            altId = if (random.nextBoolean()) lorem.characters(5, 10).toUpperCase() else null,
            serialNumber = if (random.nextBoolean()) lorem.characters(10, 15).toUpperCase() else null,
            inventoryBrand = lorem.characters(3),
            inventoryModel = if (random.nextBoolean()) lorem.characters(10, 18) else null,
            scannedBy = scannedBy,
            exceptionCode = randomExceptionCode(),
            approved = approved,
            approvedBy = approvedBy,
            lookupKey = lorem.characters(10).toUpperCase(),
            audit = SimpleIdentifiableEntity(audit)
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AuditExceptionFactoryService @Inject constructor(
   private val auditExceptionRepository: AuditExceptionRepository
) {

   fun stream(numberIn: Int = 1, audit: AuditEntity, scanAreaIn: AuditScanAreaEntity): Stream<AuditExceptionEntity> {
      return AuditExceptionFactory.stream(numberIn, audit, scanAreaIn)
         .map { auditExceptionRepository.insert(it) }
   }

   fun stream(numberIn: Int = 1, audit: AuditEntity, scanAreaIn: AuditScanAreaEntity, scannedBy: EmployeeEntity, approved: Boolean): Stream<AuditExceptionEntity> {
      return AuditExceptionFactory.stream(numberIn, audit = audit, scannedByIn = scannedBy, approvedIn = approved, scanAreaIn = scanAreaIn)
         .map { auditExceptionRepository.insert(it) }
   }

   fun single(audit: AuditEntity, scanAreaIn: AuditScanAreaEntity, scannedBy: EmployeeEntity, approved: Boolean = false): AuditExceptionEntity {
      return AuditExceptionFactory.stream(audit = audit, scannedByIn = scannedBy, approvedIn = approved, scanAreaIn = scanAreaIn)
         .map { auditExceptionRepository.insert(it) }
         .findFirst().orElseThrow { Exception("Unable to create AuditExceptionEntity") }
   }

   fun stream(numberIn: Int, audit: AuditEntity, scanAreaIn: AuditScanAreaEntity, scannedBy: EmployeeEntity): Stream<AuditExceptionEntity> {
      return AuditExceptionFactory.stream(numberIn = numberIn, audit = audit, scannedByIn = scannedBy, scanAreaIn = scanAreaIn)
         .map { auditExceptionRepository.insert(it) }
   }

   fun generate(numberIn: Int, audit: AuditEntity, scanAreaIn: AuditScanAreaEntity, scannedBy: EmployeeEntity) {
      return stream(numberIn = numberIn, audit = audit, scannedBy = scannedBy, scanAreaIn = scanAreaIn).forEach { }
   }
}
