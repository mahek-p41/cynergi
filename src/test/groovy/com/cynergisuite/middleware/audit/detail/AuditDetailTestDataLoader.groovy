package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.detail.infrastructure.AuditDetailRepository
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeTestDataLoader
import com.cynergisuite.middleware.employee.EmployeeTestDataLoaderService
import com.cynergisuite.middleware.inventory.InventoryDTO
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import javax.inject.Inject
import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Singleton

class AuditDetailTestDataLoader {

   static Stream<AuditDetailEntity> stream(int numberIn = 1, AuditEntity audit, AuditScanAreaEntity scanAreaIn, EmployeeEntity scannedByIn = null, List<InventoryDTO> inventories = null) {
      final number = numberIn > 0 ? numberIn : 1
      final scannedBy = scannedByIn ?: EmployeeTestDataLoader.single(audit.store.myCompany())
      final faker = new Faker()

      if (scannedBy.company != audit.store.myCompany()) {
         throw new Exception("scannedBy.company did not match audit.store.company")
      }

      return IntStream.range(0, number).mapToObj {
         final inventory = inventories?.get(it)
         final lookupKey = inventory?.lookupKey ?: "${faker.code().asin()}$it"
         final barcode = inventory?.barcode ?: "${faker.code().asin()}$it"
         final productCode = inventory?.productCode ?: faker.commerce().productName()
         final inventoryBrand = inventory?.brand ?: faker.company().name()
         final serialNumber = inventory?.serialNumber ?: faker.idNumber().valid()

         new AuditDetailEntity(
            null,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            scanAreaIn,
            lookupKey,
            barcode,
            barcode,
            productCode,
            serialNumber,
            inventoryBrand,
            productCode,
            scannedBy,
            new SimpleIdentifiableEntity(audit)
         )
      }
   }

   static AuditDetailEntity single(AuditEntity auditIn, AuditScanAreaEntity scanAreaIn, EmployeeEntity scannedByIn = null) {
      return stream(1, auditIn, scanAreaIn, scannedByIn).findFirst().orElseThrow { new Exception("Unable to create AuditDetail") }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AuditDetailTestDataLoaderService {
   private final AuditDetailRepository auditDetailRepository
   private final EmployeeTestDataLoaderService employeeTestDataLoaderService

   @Inject
   AuditDetailTestDataLoaderService(AuditDetailRepository auditDetailRepository, EmployeeTestDataLoaderService employeeTestDataLoaderService) {
      this.auditDetailRepository = auditDetailRepository
      this.employeeTestDataLoaderService = employeeTestDataLoaderService
   }

   Stream<AuditDetailEntity> stream(int numberIn = 1, AuditEntity audit, AuditScanAreaEntity scanAreaIn, EmployeeEntity scannedByIn, List<InventoryDTO> inventories) {
      final scannedIn = scannedByIn ?: employeeTestDataLoaderService.single(audit.store.myCompany())

      return AuditDetailTestDataLoader.stream(numberIn, audit, scanAreaIn, scannedIn, inventories)
         .map {
            auditDetailRepository.insert(it)
         }
   }

   Stream<AuditDetailEntity> stream(int numberIn = 1, AuditEntity audit, AuditScanAreaEntity scanAreaIn, List<InventoryDTO> inventories) {
      final scannedIn = employeeTestDataLoaderService.single(audit.store.myCompany())

      return AuditDetailTestDataLoader.stream(numberIn, audit, scanAreaIn, scannedIn, inventories)
         .map {
            auditDetailRepository.insert(it)
         }
   }

   def generate(int numberIn = 1, AuditEntity audit, EmployeeEntity scannedBy, AuditScanAreaEntity scanArea, List<InventoryDTO> inventories) {
      AuditDetailTestDataLoader.stream(numberIn, audit, scanArea, scannedBy, inventories)
         .forEach { auditDetailRepository.insert(it) }
   }

   AuditDetailEntity single(AuditEntity audit, AuditScanAreaEntity scanAreaIn, InventoryDTO inventory) {
      return stream(1, audit, scanAreaIn, [inventory]).findFirst().orElseThrow { new Exception("Unable to create AuditDetail") }
   }

   AuditDetailEntity single(AuditEntity audit, AuditScanAreaEntity scanAreaIn, List<InventoryDTO> inventories) {
      return stream(1, audit, scanAreaIn, inventories).findFirst().orElseThrow { new Exception("Unable to create AuditDetail") }
   }

   AuditDetailEntity single(AuditEntity audit, AuditScanAreaEntity scanArea, EmployeeEntity scannedByIn) {
      return AuditDetailTestDataLoader.stream(1, audit, scanArea, scannedByIn)
         .map { auditDetailRepository.insert(it) }
         .findFirst().orElseThrow { new Exception("Unable to create AuditDetailEntity") }
   }

   AuditDetailEntity single(AuditEntity audit, AuditScanAreaEntity scanArea, EmployeeEntity scannedByIn, List<InventoryDTO> inventories) {
      return AuditDetailTestDataLoader.stream(1, audit, scanArea, scannedByIn, inventories)
         .map { auditDetailRepository.insert(it) }
         .findFirst().orElseThrow { new Exception("Unable to create AuditDetailEntity") }
   }
}
