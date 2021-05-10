package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.detail.infrastructure.AuditDetailRepository
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeFactory
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.inventory.InventoryDTO
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object AuditDetailFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, audit: AuditEntity, scanAreaIn: AuditScanAreaEntity, scannedByIn: EmployeeEntity? = null, inventory: InventoryDTO? = null): Stream<AuditDetailEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val scannedBy = scannedByIn ?: EmployeeFactory.single(audit.store.myCompany())
      val faker = Faker()

      if (scannedBy.company != audit.store.myCompany()) {
         throw Exception("scannedBy.company did not match audit.store.company")
      }

      return IntStream.range(0, number).mapToObj {
         val lookupKey = inventory?.lookupKey ?: "${faker.code().asin()}$it"
         val barcode = inventory?.barcode ?: "${faker.code().asin()}$it"
         val productCode = inventory?.productCode ?: faker.commerce().productName()
         val inventoryBrand = inventory?.brand ?: faker.company().name()
         val serialNumber = inventory?.serialNumber ?: faker.idNumber().valid()

         AuditDetailEntity(
            scanArea = scanAreaIn,
            lookupKey = lookupKey,
            barcode = barcode,
            productCode = productCode,
            altId = barcode,
            serialNumber = serialNumber,
            inventoryBrand = inventoryBrand,
            inventoryModel = productCode,
            scannedBy = scannedBy,
            audit = SimpleIdentifiableEntity(audit)
         )
      }
   }

   @JvmStatic
   fun single(auditIn: AuditEntity, scanAreaIn: AuditScanAreaEntity, scannedByIn: EmployeeEntity? = null): AuditDetailEntity {
      return stream(1, auditIn, scanAreaIn, scannedByIn).findFirst().orElseThrow { Exception("Unable to create AuditDetail") }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AuditDetailFactoryService @Inject constructor(
   private val auditDetailRepository: AuditDetailRepository,
   private val employeeFactoryService: EmployeeFactoryService
) {

   fun stream(numberIn: Int = 1, audit: AuditEntity, scanAreaIn: AuditScanAreaEntity, scannedByIn: EmployeeEntity? = null): Stream<AuditDetailEntity> {
      val scannedIn = scannedByIn ?: employeeFactoryService.single(audit.store)

      return AuditDetailFactory.stream(numberIn, audit, scanAreaIn, scannedIn)
         .map {
            auditDetailRepository.insert(it)
         }
   }

   fun stream(numberIn: Int = 1, audit: AuditEntity, scanAreaIn: AuditScanAreaEntity): Stream<AuditDetailEntity> {
      val scannedIn = employeeFactoryService.single(audit.store)

      return AuditDetailFactory.stream(numberIn, audit, scanAreaIn, scannedIn)
         .map {
            auditDetailRepository.insert(it)
         }
   }

   fun stream(numberIn: Int = 1, audit: AuditEntity, scanAreaIn: AuditScanAreaEntity, inventory: InventoryDTO?): Stream<AuditDetailEntity> {
      val scannedIn = employeeFactoryService.single(audit.store)

      return AuditDetailFactory.stream(numberIn, audit, scanAreaIn, scannedIn, inventory)
         .map {
            auditDetailRepository.insert(it)
         }
   }

   fun generate(numberIn: Int = 1, audit: AuditEntity, scannedBy: EmployeeEntity, scanArea: AuditScanAreaEntity) {
      AuditDetailFactory.stream(numberIn = numberIn, audit = audit, scanAreaIn = scanArea, scannedByIn = scannedBy)
         .forEach { auditDetailRepository.insert(it) }
   }

   fun single(audit: AuditEntity, scanAreaIn: AuditScanAreaEntity): AuditDetailEntity {
      return stream(1, audit, scanAreaIn).findFirst().orElseThrow { Exception("Unable to create AuditDetail") }
   }

   fun single(audit: AuditEntity, scanAreaIn: AuditScanAreaEntity, inventory: InventoryDTO): AuditDetailEntity {
      return stream(1, audit, scanAreaIn, inventory).findFirst().orElseThrow { Exception("Unable to create AuditDetail") }
   }

   fun single(audit: AuditEntity, scanArea: AuditScanAreaEntity, scannedByIn: EmployeeEntity): AuditDetailEntity {
      return AuditDetailFactory.stream(audit = audit, scanAreaIn = scanArea, scannedByIn = scannedByIn)
         .map { auditDetailRepository.insert(it) }
         .findFirst().orElseThrow { Exception("Unable to create AuditDetailEntity") }
   }

   private fun single(
      audit: AuditEntity,
      scanArea: AuditScanAreaEntity,
      scannedByIn: EmployeeEntity,
      inventory: InventoryDTO
   ): AuditDetailEntity {
      return AuditDetailFactory.stream(audit = audit, scanAreaIn = scanArea, scannedByIn = scannedByIn, inventory = inventory)
         .map { auditDetailRepository.insert(it) }
         .findFirst().orElseThrow { Exception("Unable to create AuditDetailEntity") }
   }
}
