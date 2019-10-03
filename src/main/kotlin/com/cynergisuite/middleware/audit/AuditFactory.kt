package com.cynergisuite.middleware.audit

import com.cynergisuite.middleware.audit.action.AuditAction
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.status.AuditStatus
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.employee.Employee
import com.cynergisuite.middleware.employee.EmployeeFactory
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreFactory
import com.cynergisuite.middleware.store.StoreFactoryService
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object AuditFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, storeIn: Store? = null, changedByIn: Employee? = null, statusesIn: Set<AuditStatus>? = null): Stream<Audit> {
      val number = if (numberIn > 0) numberIn else 1
      val store = storeIn?: StoreFactory.random()
      val statuses: Set<AuditStatus> = statusesIn ?: mutableSetOf(AuditStatusFactory.opened())
      val changedBy = changedByIn ?: EmployeeFactory.testEmployee()

      return IntStream.range(0, number).mapToObj {
         Audit(
            store = store,
            actions = statuses.map { AuditAction(status = it, changedBy = changedBy) }.toMutableSet()
         )
      }
   }

   @JvmStatic
   fun single(): Audit {
      return single(storeIn = StoreFactory.random())
   }

   @JvmStatic
   fun single(storeIn: Store): Audit {
      return stream(1, storeIn).findFirst().orElseThrow { Exception("Unable to create Audit") }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class AuditFactoryService @Inject constructor(
   private val auditRepository: AuditRepository,
   private val employeeFactoryService: EmployeeFactoryService,
   private val storeFactoryService: StoreFactoryService
) {

   fun stream(numberIn: Int = 1, storeIn: Store? = null): Stream<Audit> =
      stream(numberIn, storeIn, null, null)

   fun stream(numberIn: Int = 1, storeIn: Store? = null, changedByIn: Employee? = null, statusesIn: Set<AuditStatus>?): Stream<Audit> {
      val store = storeIn ?: storeFactoryService.random()
      val changedBy = changedByIn ?: employeeFactoryService.single()

      return AuditFactory.stream(numberIn, store, changedBy, statusesIn)
         .map {
            auditRepository.insert(it)
         }
   }

   fun generate(numberIn: Int = 1, storeIn: Store? = null, changedByIn: Employee? = null, statusesIn: Set<AuditStatus>?) {
      stream(numberIn, storeIn, changedByIn, statusesIn).forEach {  } // exercise the stream with the terminal forEach
   }

   fun single(): Audit =
      single(storeIn = null)

   fun single(storeIn: Store? = null): Audit =
      stream(storeIn = storeIn).findFirst().orElseThrow { Exception("Unable to create Audit") }

   fun single(storeIn: Store? = null, changedByIn: Employee?, statusesIn: Set<AuditStatus>?): Audit =
      stream(storeIn = storeIn, statusesIn = statusesIn).findFirst().orElseThrow { Exception("Unable to create Audit") }

   fun single(statusesIn: Set<AuditStatus>?): Audit =
      stream(1, null, null, statusesIn).findFirst().orElseThrow { Exception("Unable to create Audit") }
}
