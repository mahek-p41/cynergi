package com.cynergisuite.middleware.audit

import com.cynergisuite.middleware.audit.action.AuditActionEntity
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.status.AuditStatus
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeFactory
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.StoreFactory
import com.cynergisuite.middleware.store.StoreFactoryService
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object AuditFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, changedByIn: EmployeeEntity? = null, store: StoreEntity, statusesIn: Set<AuditStatus>? = null): Stream<AuditEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val random = faker.random()
      val changedBy = changedByIn ?: EmployeeFactory.single(store.company)
      val statuses: Set<AuditStatus> = statusesIn ?: mutableSetOf(AuditStatusFactory.created())

      if (changedBy.company != store.company) {
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
   fun single(store: StoreEntity): AuditEntity {
      return stream(store = store).findFirst().orElseThrow { Exception("Unable to create AuditEntity") }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AuditFactoryService @Inject constructor(
   private val auditRepository: AuditRepository,
   private val employeeFactoryService: EmployeeFactoryService,
   private val storeFactoryService: StoreFactoryService
) {

   fun stream(numberIn: Int = 1, store: StoreEntity): Stream<AuditEntity> {
      val changedBy = employeeFactoryService.single(store)

      return AuditFactory.stream(numberIn = numberIn, store = store, changedByIn = changedBy)
         .map { auditRepository.insert(it) }
   }

   fun stream(numberIn: Int = 1, store: StoreEntity, changedByIn: EmployeeEntity, statusesIn: Set<AuditStatus>? = null): Stream<AuditEntity> {
      val changedBy = changedByIn

      return AuditFactory.stream(numberIn = numberIn, store = store, changedByIn = changedBy, statusesIn = statusesIn)
         .map { auditRepository.insert(it) }
   }

   fun single(changedBy: EmployeeEntity, statusesIn: Set<AuditStatus>): AuditEntity {
      val company = changedBy.company
      val store = changedBy.store ?: storeFactoryService.random(company)

      return stream(store = store, changedByIn = changedBy, statusesIn = statusesIn).findFirst().orElseThrow { Exception("Unable to create AuditEntity") }
   }

   fun single(store: StoreEntity, changedBy: EmployeeEntity, statusesIn: Set<AuditStatus>): AuditEntity {
      return stream(store = store, changedByIn = changedBy, statusesIn = statusesIn).findFirst().orElseThrow { Exception("Unable to create AuditEntity") }
   }

   fun single(store: StoreEntity): AuditEntity {
      return stream(store = store).findFirst().orElseThrow { Exception("Unable to create AuditEntity") }
   }

   fun single(store: StoreEntity, changedBy: EmployeeEntity): AuditEntity {
      return stream(store = store, changedByIn = changedBy).findFirst().orElseThrow { Exception("Unable to create AuditEntity") }
   }

   fun generate(numberIn: Int, changedBy: EmployeeEntity, statuses: Set<AuditStatus>) {
      val company = changedBy.company
      val store = changedBy.store ?: storeFactoryService.random(company)

      stream(numberIn = numberIn, store= store, changedByIn = changedBy, statusesIn = statuses).forEach {  }
   }

   fun generate(numberIn: Int, store: StoreEntity, changedBy: EmployeeEntity, statuses: Set<AuditStatus>) {
      stream(numberIn = numberIn, store= store, changedByIn = changedBy, statusesIn = statuses).forEach {  }
   }
}
