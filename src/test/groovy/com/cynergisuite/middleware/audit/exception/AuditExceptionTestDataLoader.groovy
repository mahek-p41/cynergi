package com.cynergisuite.middleware.audit.exception

import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaEntity
import com.cynergisuite.middleware.audit.exception.infrastructure.AuditExceptionRepository
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeTestDataLoader
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import org.apache.commons.lang3.RandomUtils

import javax.inject.Inject
import javax.inject.Singleton
import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream

class AuditExceptionTestDataLoader {

   private static final List<String> exceptionCodes = [
      "Not found in inventory",
      "Unit is currently on rent",
      "Unit was not scanned",
      "Unit at different location",
      "Pending transfer"
   ]

   static String randomExceptionCode() {
      return exceptionCodes[RandomUtils.nextInt(0, exceptionCodes.size())]
   }

   static Stream<AuditExceptionEntity> stream(int numberIn = 1, AuditEntity audit, AuditScanAreaEntity scanAreaIn, EmployeeEntity scannedByIn = null, Boolean approvedIn = null) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final random = faker.random()
      final lorem = faker.lorem()
      final brand = faker.company()
      final model = faker.book()
      final scannedBy = scannedByIn ?: EmployeeTestDataLoader.single(audit.store.myCompany())
      final approved = approvedIn != null ? approvedIn : random.nextBoolean()
      final approvedBy = approved ? scannedByIn : null

      return IntStream.range(0, number).mapToObj {
         new AuditExceptionEntity(
            null,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            scanAreaIn,
            lorem.characters(10).toUpperCase(),
            random.nextBoolean() ? lorem.characters(2, 3).toUpperCase() : null,
            random.nextBoolean() ? lorem.characters(5, 10).toUpperCase(Locale.getDefault()) : null,
            random.nextBoolean() ? lorem.characters(10, 15).toUpperCase(Locale.getDefault()) : null,
            brand.name(),
            model.title(),
            scannedBy,
            randomExceptionCode(),
            approved,
            approvedBy,
            lorem.characters(10).toUpperCase(),
            List.of(),
            new SimpleIdentifiableEntity(audit)
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AuditExceptionTestDataLoaderService {
   private final AuditExceptionRepository auditExceptionRepository

   @Inject
   AuditExceptionTestDataLoaderService(AuditExceptionRepository auditExceptionRepository) {
      this.auditExceptionRepository = auditExceptionRepository
   }

   Stream<AuditExceptionEntity> stream(int numberIn = 1, AuditEntity audit, AuditScanAreaEntity scanAreaIn) {
      return AuditExceptionTestDataLoader.stream(numberIn, audit, scanAreaIn)
         .map { auditExceptionRepository.insert(it) }
   }

   Stream<AuditExceptionEntity> stream(int numberIn = 1, AuditEntity audit, AuditScanAreaEntity scanAreaIn, EmployeeEntity scannedBy, boolean approved) {
      return AuditExceptionTestDataLoader.stream(numberIn, audit, scanAreaIn, scannedBy, approved)
         .map { auditExceptionRepository.insert(it) }
   }

   AuditExceptionEntity single(AuditEntity audit, AuditScanAreaEntity scanAreaIn, EmployeeEntity scannedBy, boolean approved = false) {
      return AuditExceptionTestDataLoader.stream(1, audit, scanAreaIn, scannedBy, approved)
         .map { auditExceptionRepository.insert(it) }
         .findFirst().orElseThrow { new Exception("Unable to create AuditExceptionEntity") }
   }

   Stream<AuditExceptionEntity> stream(int numberIn, AuditEntity audit, AuditScanAreaEntity scanAreaIn, EmployeeEntity scannedBy) {
      return AuditExceptionTestDataLoader.stream(numberIn, audit, scanAreaIn, scannedBy)
         .map { auditExceptionRepository.insert(it) }
   }

   def generate(int numberIn, AuditEntity audit, AuditScanAreaEntity scanAreaIn, EmployeeEntity scannedBy) {
      return stream(numberIn, audit, scanAreaIn, scannedBy).forEach { }
   }
}
