package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.extensions.truncate
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.department.DepartmentFactory
import com.cynergisuite.middleware.department.DepartmentFactoryService
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeFactory
import com.cynergisuite.middleware.employee.EmployeeFactoryService
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
   fun stream(numberIn: Int = 1, dayOfWeekIn: DayOfWeek? = null, storesIn: List<StoreEntity>? = null, employeeIn: EmployeeEntity? = null, datasetIn: String? = null): Stream<ScheduleEntity> {
      val faker = Faker()
      val chuckNorris = faker.chuckNorris()
      val number = if (numberIn > 0) numberIn else 1
      val dayOfWeek = dayOfWeekIn ?: DayOfWeek.values().random()
      val stores = if ( !storesIn.isNullOrEmpty() ) storesIn else listOf(StoreFactory.random())
      val employee = employeeIn ?: EmployeeFactory.single()
      val arguments = mutableSetOf<ScheduleArgumentEntity>()
      val dataset = datasetIn ?: "tstds1"

      for (store in stores) {
         arguments.add(
            ScheduleArgumentEntity(
               value = store.number.toString(),
               description = "storeNumber"
            )
         )
      }

      arguments.add(
         ScheduleArgumentEntity(
            dataset,
            "dataset"
         )
      )
      arguments.add(
         ScheduleArgumentEntity(
            employee.number.toString(),
            "employeeNumber"
         )
      )

      return IntStream.range(0, number).mapToObj {
         ScheduleEntity(
            title = chuckNorris.fact().truncate(36)!!,
            description = chuckNorris.fact().truncate(256),
            schedule = dayOfWeek.name,
            command = ScheduleCommandTypeFactory.auditSchedule(),
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
   private val employeeFactoryService: EmployeeFactoryService,
   private val scheduleRepository: ScheduleRepository,
   private val storeFactoryService: StoreFactoryService
) {

   fun stream(numberIn: Int = 1, dayOfWeekIn: DayOfWeek? = null, storesIn: List<StoreEntity>? = null, employeeIn: EmployeeEntity? = null, dataset: String? = null): Stream<ScheduleEntity> {
      val stores = if ( !storesIn.isNullOrEmpty() ) storesIn else listOf(storeFactoryService.random())
      val employee = employeeIn ?: employeeFactoryService.single()

      return AuditScheduleFactory.stream(numberIn, dayOfWeekIn, stores, employee, dataset)
         .map { scheduleRepository.insert(it) }
   }

   fun single(dayOfWeekIn: DayOfWeek? = null, storesIn: List<StoreEntity>? = null, employeeIn: EmployeeEntity? = null, dataset: String? = null) :ScheduleEntity {
      return stream(1, dayOfWeekIn, storesIn, employeeIn, dataset).findFirst().orElseThrow { Exception("Unable to create Audit Schedule") }
   }
}
