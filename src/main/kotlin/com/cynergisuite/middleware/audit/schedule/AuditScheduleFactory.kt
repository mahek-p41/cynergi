package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.middleware.audit.schedule.infrastruture.AuditScheduleRepository
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.department.DepartmentFactory
import com.cynergisuite.middleware.department.DepartmentFactoryService
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.type.ScheduleTypeFactory
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.StoreFactory
import com.cynergisuite.middleware.store.StoreFactoryService
import io.micronaut.context.annotation.Requires
import java.time.DayOfWeek
import java.time.format.TextStyle.FULL
import java.util.Locale.US
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Singleton

object AuditScheduleFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, dayOfWeekIn: DayOfWeek? = null, storeIn: StoreEntity? = null, departmentAccessIn: DepartmentEntity? = null) : Stream<AuditScheduleEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val store = storeIn ?: StoreFactory.random()
      val dayOfWeek = dayOfWeekIn ?: randomDayOfWeek()
      val departmentAccess = departmentAccessIn ?: DepartmentFactory.random()
      val schedule = ScheduleEntity(
         id = null,
         title = "${dayOfWeek.getDisplayName(FULL, US)} Store Audit",
         description = "${dayOfWeek.getDisplayName(FULL, US)} Store Audit",
         schedule = dayOfWeek.name,
         command = "AuditCreator",
         type = ScheduleTypeFactory.weekly(),
         arguments = mutableListOf(
            ScheduleArgumentEntity(
               description = "storeNumber",
               value = store.number.toString()
            ),
            ScheduleArgumentEntity(
               description = "departmentAccess",
               value = departmentAccess.code
            )
         )
      )

      return IntStream.range(0, number).mapToObj {
         AuditScheduleEntity(
            id = null,
            store = store,
            departmentAccess = departmentAccess,
            schedule = schedule
         )
      }
   }

   @JvmStatic
   fun randomDayOfWeek(): DayOfWeek = DayOfWeek.values().random()
}

@Singleton
@Requires(env = ["develop", "test"])
class AuditScheduleFactoryService(
   private val auditScheduleRepository: AuditScheduleRepository,
   private val departmentFactoryService: DepartmentFactoryService,
   private val storeFactoryService: StoreFactoryService
) {

   fun stream(numberIn: Int = 1, dayOfWeekIn: DayOfWeek? = null, storeIn: StoreEntity? = null, departmentAccessIn: DepartmentEntity? = null): Stream<AuditScheduleEntity> {
      val store = storeIn ?: storeFactoryService.random()
      val departmentAccess = departmentAccessIn ?: departmentFactoryService.random()

      return AuditScheduleFactory.stream(numberIn, dayOfWeekIn, store, departmentAccess)
         .map { auditScheduleRepository.insert(it) }
   }
}
