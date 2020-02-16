package com.cynergisuite.middleware.audit

import com.cynergisuite.middleware.audit.action.AuditActionEntity
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.status.AuditStatus
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.company.CompanyFactory
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
   fun stream(numberIn: Int = 1, changedByIn: EmployeeEntity? = null, storeIn: StoreEntity? = null, statusesIn: Set<AuditStatus>? = null): Stream<AuditEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val random = faker.random()
      val changedBy = changedByIn ?: EmployeeFactory.testEmployee(CompanyFactory.random())
      val store = storeIn ?: StoreFactory.random(changedBy.company)
      val statuses: Set<AuditStatus> = statusesIn ?: mutableSetOf(AuditStatusFactory.created())

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
   fun single(changedByIn: EmployeeEntity?): AuditEntity {
      return stream(1, changedByIn = changedByIn).findFirst().orElseThrow { Exception("Unable to create Audit") }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AuditFactoryService @Inject constructor(
   private val auditRepository: AuditRepository,
   private val employeeFactoryService: EmployeeFactoryService,
   private val storeFactoryService: StoreFactoryService
) {
   fun stream(numberIn: Int = 1, changedByIn: EmployeeEntity? = null, storeIn: StoreEntity? = null, statusesIn: Set<AuditStatus>? = null): Stream<AuditEntity> {
      val changedBy = changedByIn ?: employeeFactoryService.single(storeIn)
      val store = storeFactoryService.random(changedBy.company)

      return AuditFactory.stream(numberIn, changedBy, store, statusesIn)
         .map { auditRepository.insert(it) }
   }

   fun single(): AuditEntity {
      return stream(1, changedByIn = null, storeIn = null, statusesIn = null).findFirst().orElseThrow { Exception("Unable to create Audit") }
   }

   fun single(changedByIn: EmployeeEntity? = null, statusesIn: Set<AuditStatus>? = null): AuditEntity {
      return stream(numberIn = 1, changedByIn = changedByIn, storeIn = null, statusesIn = statusesIn).findFirst().orElseThrow { Exception("Unable to create Audit") }
   }

   fun generate(numberIn: Int = 1, changedByIn: EmployeeEntity? = null, statusesIn: Set<AuditStatus>? = null) {
      stream(numberIn = numberIn, changedByIn = changedByIn, statusesIn = statusesIn).forEach {  }
   }
}
