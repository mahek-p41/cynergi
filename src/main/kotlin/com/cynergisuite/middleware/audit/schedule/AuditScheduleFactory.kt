package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.extensions.truncate
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.command.ScheduleCommandTypeFactory
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.schedule.type.ScheduleTypeFactory
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.StoreFactoryService
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.time.DayOfWeek
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object AuditScheduleFactory {
   fun single(dayOfWeek: DayOfWeek, stores: List<StoreEntity>, company: Company): ScheduleEntity {
      val faker = Faker()
      val bool = faker.bool()
      val chuckNorris = faker.chuckNorris()

      return ScheduleEntity(
         title = chuckNorris.fact().truncate(36)!!,
         description = if (bool.bool()) chuckNorris.fact().truncate(255) else null,
         schedule = dayOfWeek.name,
         command = ScheduleCommandTypeFactory.auditSchedule(),
         type = ScheduleTypeFactory.weekly()
      )
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AuditScheduleFactoryService @Inject constructor(
   private val employeeFactoryService: EmployeeFactoryService,
   private val scheduleRepository: ScheduleRepository,
   private val storeFactoryService: StoreFactoryService
) {


}
