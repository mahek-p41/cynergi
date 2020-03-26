package com.cynergisuite.middleware.audit

import com.cynergisuite.middleware.audit.action.AuditActionEntity
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.status.AuditStatus
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeFactory
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.StoreFactoryService
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object AuditFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, changedByIn: EmployeeEntity? = null, store: Store, statusesIn: Set<AuditStatus>? = null): Stream<AuditEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val random = faker.random()
      val changedBy = changedByIn ?: EmployeeFactory.single(store.myCompany())
      val statuses: Set<AuditStatus> = statusesIn ?: mutableSetOf(AuditStatusFactory.created())

      if (changedBy.company != store.myCompany()) {
         throw Exception("changedBy company does not equal store company")
      }

      return IntStream.range(0, number).mapToObj {
         AuditEntity(
            store = store,
            number = random.nextInt(1, 1000),
            totalDetails = random.nextInt(1, 1000),
            totalExceptions = random.nextInt(1, 100),
            hasExceptionNotes = random.nextBoolean(),
            lastUpdated = OffsetDateTime.now(),
            inventoryCount = random.nextInt(0, 1000),
            actions = statuses.map { AuditActionEntity(status = it, changedBy = changedBy) }.toCollection(LinkedHashSet())
         )
      }
   }

   @JvmStatic
   fun single(store: Store): AuditEntity {
      return stream(store = store).findFirst().orElseThrow { Exception("Unable to create AuditEntity") }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AuditFactoryService @Inject constructor(
   private val auditRepository: AuditRepository,
   private val employeeFactoryService: EmployeeFactoryService,
   private val employeeRepository: EmployeeRepository,
   private val storeFactoryService: StoreFactoryService,
   private val storeRepository: StoreRepository
) {

   fun stream(numberIn: Int = 1, store: Store): Stream<AuditEntity> {
      val changedBy = employeeFactoryService.single(store)

      return AuditFactory.stream(numberIn = numberIn, store = store, changedByIn = changedBy)
         .map { auditRepository.insert(it) }
   }

   fun stream(numberIn: Int = 1, store: Store, changedBy: EmployeeEntity, statusesIn: Set<AuditStatus>? = null): Stream<AuditEntity> {
      return AuditFactory.stream(numberIn = numberIn, store = store, changedByIn = changedBy, statusesIn = statusesIn)
         .map { auditRepository.insert(it) }
   }

   fun single(changedBy: EmployeeEntity, statusesIn: Set<AuditStatus>): AuditEntity {
      val store = changedBy.store ?: storeFactoryService.random(changedBy.company)

      return stream(store = store, changedBy = changedBy, statusesIn = statusesIn).findFirst().orElseThrow { Exception("Unable to create AuditEntity") }
   }

   fun single(changedByIn: AuthenticatedEmployee, statusesIn: Set<AuditStatus>): AuditEntity {
      val location = changedByIn.myLocation()
      val store = storeRepository.findOne(location.myNumber(), location.myCompany()) ?: throw Exception("Unable to create AuditEntity due to invalid location on changedBy")
      val changedBy = employeeRepository.findOne(changedByIn) ?: throw Exception("Unable to create AuditEntity due to invalid changedBy")

      return stream(store = store, changedBy = changedBy, statusesIn = statusesIn).findFirst().orElseThrow { Exception("Unable to create AuditEntity") }
   }

   fun single(store: StoreEntity, changedBy: EmployeeEntity, statusesIn: Set<AuditStatus>): AuditEntity {
      return stream(store = store, changedBy = changedBy, statusesIn = statusesIn).findFirst().orElseThrow { Exception("Unable to create AuditEntity") }
   }

   fun single(store: StoreEntity, changedByIn: AuthenticatedEmployee, statusesIn: Set<AuditStatus>): AuditEntity {
      val changedBy = employeeRepository.findOne(changedByIn) ?: throw Exception("Unable to create AuditEntity due to invalid changedBy")

      return stream(store = store, changedBy = changedBy, statusesIn = statusesIn).findFirst().orElseThrow { Exception("Unable to create AuditEntity") }
   }

   fun single(store: Store): AuditEntity {
      return stream(store = store).findFirst().orElseThrow { Exception("Unable to create AuditEntity") }
   }

   fun single(store: StoreEntity, changedBy: EmployeeEntity): AuditEntity {
      return stream(store = store, changedBy = changedBy).findFirst().orElseThrow { Exception("Unable to create AuditEntity") }
   }

   fun generate(numberIn: Int, changedBy: EmployeeEntity, statuses: Set<AuditStatus>) {
      val company = changedBy.company
      val store = changedBy.store ?: storeFactoryService.random(company)

      stream(numberIn = numberIn, store= store, changedBy = changedBy, statusesIn = statuses).forEach {  }
   }

   fun generate(numberIn: Int, store: StoreEntity, changedBy: EmployeeEntity, statuses: Set<AuditStatus>) {
      stream(numberIn = numberIn, store= store, changedBy = changedBy, statusesIn = statuses).forEach {  }
   }
}
