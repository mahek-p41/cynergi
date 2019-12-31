package com.cynergisuite.middleware.audit

import com.cynergisuite.middleware.audit.action.AuditAction
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.status.AuditStatus
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeFactory
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.StoreFactory
import com.cynergisuite.middleware.store.StoreFactoryService
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object AuditFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, storeIn: StoreEntity? = null, changedByIn: EmployeeEntity? = null, statusesIn: Set<AuditStatus>? = null): Stream<AuditEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val store = storeIn?: StoreFactory.random()
      val statuses: Set<AuditStatus> = statusesIn ?: mutableSetOf(AuditStatusFactory.created())
      val changedBy = changedByIn ?: EmployeeFactory.testEmployee()

      return IntStream.range(0, number).mapToObj {
         AuditEntity(
            store = store,
            actions = statuses.map { AuditAction(status = it, changedBy = changedBy) }.toCollection(LinkedHashSet<AuditAction>()),
            dataset = store.dataset
         )
      }
   }

   @JvmStatic
   fun single(): AuditEntity {
      return single(storeIn = StoreFactory.random())
   }

   @JvmStatic
   fun single(storeIn: StoreEntity): AuditEntity {
      return stream(1, storeIn).findFirst().orElseThrow { Exception("Unable to create Audit") }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AuditFactoryService @Inject constructor(
   private val auditRepository: AuditRepository,
   private val employeeFactoryService: EmployeeFactoryService,
   private val storeFactoryService: StoreFactoryService
) {

   fun stream(numberIn: Int = 1, storeIn: StoreEntity? = null): Stream<AuditEntity> =
      stream(numberIn, storeIn, null, null)

   fun stream(numberIn: Int = 1, storeIn: StoreEntity? = null, changedByIn: EmployeeEntity? = null, statusesIn: Set<AuditStatus>?): Stream<AuditEntity> {
      val store = storeIn ?: storeFactoryService.random()
      val changedBy = changedByIn ?: employeeFactoryService.single()

      return AuditFactory.stream(numberIn, store, changedBy, statusesIn)
         .map {
            auditRepository.insert(it)
         }
   }

   fun generate(numberIn: Int = 1, storeIn: StoreEntity? = null, changedByIn: EmployeeEntity? = null, statusesIn: Set<AuditStatus>?) {
      stream(numberIn, storeIn, changedByIn, statusesIn).forEach {  } // exercise the stream with the terminal forEach
   }

   fun single(): AuditEntity =
      single(storeIn = null)

   fun single(storeIn: StoreEntity? = null): AuditEntity =
      stream(storeIn = storeIn).findFirst().orElseThrow { Exception("Unable to create Audit") }

   fun single(storeIn: StoreEntity? = null, changedByIn: EmployeeEntity? = null): AuditEntity =
      single(storeIn = storeIn, changedByIn = changedByIn, statusesIn = null)

   fun single(storeIn: StoreEntity? = null, changedByIn: EmployeeEntity?, statusesIn: Set<AuditStatus>?): AuditEntity =
      stream(storeIn = storeIn, statusesIn = statusesIn).findFirst().orElseThrow { Exception("Unable to create Audit") }

   fun single(statusesIn: Set<AuditStatus>?): AuditEntity =
      stream(1, null, null, statusesIn).findFirst().orElseThrow { Exception("Unable to create Audit") }
}
