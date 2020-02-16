package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.extensions.truncate
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeFactory
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
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


}

@Singleton
@Requires(env = ["develop", "test"])
class AuditScheduleFactoryService @Inject constructor(
   private val employeeFactoryService: EmployeeFactoryService,
   private val scheduleRepository: ScheduleRepository,
   private val storeFactoryService: StoreFactoryService
) {


}
