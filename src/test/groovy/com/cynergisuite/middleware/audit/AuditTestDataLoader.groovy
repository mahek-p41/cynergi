package com.cynergisuite.middleware.audit

import com.cynergisuite.middleware.audit.action.AuditActionEntity
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.status.AuditStatus
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeTestDataLoader
import com.cynergisuite.middleware.employee.EmployeeTestDataLoaderService
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreTestDataLoaderService
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.OffsetDateTime
import java.util.stream.Collectors
import java.util.stream.IntStream
import java.util.stream.Stream

class AuditTestDataLoader {

   static Stream<AuditEntity> stream(int numberIn = 1, EmployeeEntity changedByIn = null, Store store, Set<AuditStatus> statusesIn = null) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final random = faker.random()
      final changedBy = changedByIn ?: EmployeeTestDataLoader.single(store.myCompany())
      final Set<AuditStatus> statuses = statusesIn ?: new LinkedHashSet([AuditStatusFactory.created()])

      if (changedBy.company != store.myCompany()) {
         throw new Exception("changedBy company does not equal store company")
      }

      return IntStream.range(0, number).mapToObj {
         new AuditEntity(
            null,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            store,
            random.nextInt(1, 1000),
            random.nextInt(1, 1000),
            random.nextInt(1, 100),
            null,
            random.nextBoolean(),
            null,
            random.nextInt(0, 1000),
            statuses.stream().map { AuditStatus status -> new AuditActionEntity(null, OffsetDateTime.now(), OffsetDateTime.now(), status, changedBy) }.collect(Collectors.toCollection({ new LinkedHashSet() })) as Set
         )
      }
   }

   static AuditEntity single(Store store) {
      return stream(store).findFirst().orElseThrow { new Exception("Unable to create AuditEntity") }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AuditTestDataLoaderService {
   private final AuditRepository auditRepository
   private final EmployeeRepository employeeRepository
   private final EmployeeTestDataLoaderService employeeTestDataLoaderService
   private final StoreRepository storeRepository
   private final StoreTestDataLoaderService storeTestDataLoaderService

   @Inject
   AuditTestDataLoaderService(AuditRepository auditRepository, EmployeeRepository employeeRepository, EmployeeTestDataLoaderService employeeTestDataLoaderService, StoreRepository storeRepository, StoreTestDataLoaderService storeTestDataLoaderService) {
      this.auditRepository = auditRepository
      this.employeeRepository = employeeRepository
      this.employeeTestDataLoaderService = employeeTestDataLoaderService
      this.storeRepository = storeRepository
      this.storeTestDataLoaderService = storeTestDataLoaderService
   }

   Stream<AuditEntity> stream(int numberIn = 1, Store store, Set<AuditStatus> statusesIn = null) {
      final changedBy = employeeTestDataLoaderService.single(store.myCompany() as CompanyEntity)

      return AuditTestDataLoader.stream(numberIn, changedBy, store, statusesIn)
         .map { auditRepository.insert(it) }
   }

   Stream<AuditEntity> stream(int numberIn = 1, Store store, EmployeeEntity changedBy, Set<AuditStatus> statusesIn = null) {
      return AuditTestDataLoader.stream(numberIn, changedBy, store, statusesIn)
         .map { auditRepository.insert(it) }
   }

   AuditEntity single(EmployeeEntity changedBy, Set<AuditStatus> statusesIn) {
      final store = changedBy.store ?: storeTestDataLoaderService.random(changedBy.company)

      return stream(1, store, changedBy, statusesIn).findFirst().orElseThrow { new Exception("Unable to create AuditEntity") }
   }

   AuditEntity single(AuthenticatedEmployee changedByIn, Set<AuditStatus> statusesIn) {
      final location = changedByIn.myLocation()
      final store = Objects.requireNonNull(storeRepository.findOne(location.myNumber(), changedByIn.myCompany())) { throw new Exception("Unable to create AuditEntity due to invalid location on changedBy") }
      final changedBy = Objects.requireNonNull(employeeRepository.findOne(changedByIn)) { throw new Exception("Unable to create AuditEntity due to invalid changedBy") }

      return stream(1, store, changedBy, statusesIn).findFirst().orElseThrow { new Exception("Unable to create AuditEntity") }
   }

   AuditEntity single(Store store, Set<AuditStatus> statusesIn) {
      return stream(1, store, statusesIn).findFirst().orElseThrow { new Exception("Unable to create AuditEntity") }
   }

   AuditEntity single(Store store, EmployeeEntity changedBy, Set<AuditStatus> statusesIn) {
      return stream(1, store, changedBy, statusesIn).findFirst().orElseThrow { new Exception("Unable to create AuditEntity") }
   }

   AuditEntity single(Store store, AuthenticatedEmployee changedByIn, Set<AuditStatus> statusesIn) {
      final changedBy = Objects.requireNonNull(employeeRepository.findOne(changedByIn)) { throw new Exception("Unable to create AuditEntity due to invalid changedBy") }

      return stream(1, store, changedBy, statusesIn).findFirst().orElseThrow { new Exception("Unable to create AuditEntity") }
   }

   AuditEntity single(Store store) {
      return stream(store).findFirst().orElseThrow { new Exception("Unable to create AuditEntity") }
   }

   AuditEntity single(Store store, EmployeeEntity changedBy) {
      return stream(1, store, changedBy).findFirst().orElseThrow { new Exception("Unable to create AuditEntity") }
   }

   def generate(int numberIn, EmployeeEntity changedBy, Set<AuditStatus> statuses) {
      final company = changedBy.company
      final store = changedBy.store ?: storeTestDataLoaderService.random(company)

      stream(numberIn, store, changedBy, statuses).forEach { }
   }

   def generate(int numberIn, Store store, EmployeeEntity changedBy, Set<AuditStatus> statuses) {
      stream(numberIn, store, changedBy, statuses).forEach { }
   }
}
