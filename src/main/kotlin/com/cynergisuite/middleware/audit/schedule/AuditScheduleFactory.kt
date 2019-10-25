package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.department.DepartmentFactory
import com.cynergisuite.middleware.department.DepartmentFactoryService
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.command.ScheduleCommandTypeFactory
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.schedule.type.ScheduleTypeFactory
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.StoreFactory
import com.cynergisuite.middleware.store.StoreFactoryService
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.time.DayOfWeek
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object AuditScheduleFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, dayOfWeekIn: DayOfWeek? = null, storesIn: List<StoreEntity>? = null, departmentIn: DepartmentEntity? = null): Stream<ScheduleEntity> {
      val faker = Faker()
      val lorem = faker.lorem()
      val number = if (numberIn > 0) numberIn else 1
      val dayOfWeek = dayOfWeekIn ?: DayOfWeek.values().random()
      val stores = if ( !storesIn.isNullOrEmpty() ) storesIn else listOf(StoreFactory.random())
      val department = departmentIn ?: DepartmentFactory.random()
      val arguments = mutableListOf(ScheduleArgumentEntity(value = department.code, description = "departmentAccess"))

      for (store in stores) {
         arguments.add(ScheduleArgumentEntity(value = store.number.toString(), description = "storeNumber"))
      }

      return IntStream.range(0, number).mapToObj {
         ScheduleEntity(
            title = lorem.characters(3, 64),
            description = lorem.characters(3, 256),
            schedule = dayOfWeek.name,
            command = ScheduleCommandTypeFactory.scheduleAudit(),
            type = ScheduleTypeFactory.weekly(),
            arguments = arguments
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AuditScheduleFactoryService @Inject constructor(
   private val departmentFactoryService: DepartmentFactoryService,
   private val scheduleRepository: ScheduleRepository,
   private val storeFactoryService: StoreFactoryService
) {

   fun stream(numberIn: Int = 1, dayOfWeekIn: DayOfWeek? = null, storesIn: List<StoreEntity>? = null, departmentIn: DepartmentEntity? = null): Stream<ScheduleEntity> {
      val stores = if ( !storesIn.isNullOrEmpty() ) storesIn else listOf(storeFactoryService.random())
      val department = departmentIn ?: departmentFactoryService.random()

      return AuditScheduleFactory.stream(numberIn, dayOfWeekIn, stores, department)
         .map { scheduleRepository.insert(it) }
   }
}
