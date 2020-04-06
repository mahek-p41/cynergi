package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.extensions.truncate
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.command.ScheduleCommandType
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.schedule.type.ScheduleType
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreEntity
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.time.DayOfWeek
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object AuditScheduleFactory {
   fun single(scheduleCommandType: ScheduleCommandType, scheduleType: ScheduleType, dayOfWeek: DayOfWeek ?= null, stores: List<Store>, user: User, company: Company): ScheduleEntity {
      val faker = Faker()
      val chuckNorris = faker.chuckNorris()
      val arguments = mutableSetOf<ScheduleArgumentEntity>()

      if (user.myCompany() != company) {
         throw Exception("User Company [${user.myCompany()}] does not match provided Company [$company")
      }

      if (stores.isNotEmpty()) {
         for (store in stores) {
            if (store.myCompany() != company) {
               throw Exception("Store dataset [${store.myCompany().myDataset()}] does not match provided Company [${company.myDataset()}] dataset")
            } else {
               arguments.add(
                  ScheduleArgumentEntity(
                     value = store.myNumber().toString(),
                     description = "storeNumber"
                  )
               )
            }
         }
      } else {
         throw Exception("Must provide at least one store")
      }

      arguments.add(
         ScheduleArgumentEntity(
            company.myId()?.toString()!!,
            "companyId"
         )
      )

      arguments.add(
         ScheduleArgumentEntity(
            user.myEmployeeType(),
            "employeeType"
         )
      )

      arguments.add(
         ScheduleArgumentEntity(
            user.myEmployeeNumber().toString(),
            "employeeNumber"
         )
      )

      return ScheduleEntity(
         title = "AuditSchedule",
         description = chuckNorris.fact().truncate(255),
         schedule = dayOfWeek?.name ?: scheduleType.value,
         command = scheduleCommandType,
         type = scheduleType,
         arguments = arguments
      )
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AuditScheduleFactoryService @Inject constructor(
   private val scheduleRepository: ScheduleRepository
) {

   @JvmOverloads
   fun stream(numberIn: Int = 1, scheduleCommandType: ScheduleCommandType, scheduleType: ScheduleType, dayOfWeek: DayOfWeek ?= null, stores: List<StoreEntity>, user: User, company: Company): Stream<ScheduleEntity> {
      val number = if (numberIn > 0) numberIn else 1

      return IntStream.range(0, number).mapToObj {
         single(scheduleCommandType, scheduleType, dayOfWeek, stores, user, company)
      }
   }

   @JvmOverloads
   fun single(scheduleCommandType: ScheduleCommandType, scheduleType: ScheduleType, dayOfWeek: DayOfWeek ?= null, stores: List<Store>, user: User, company: Company): ScheduleEntity {
      return AuditScheduleFactory.single(scheduleCommandType, scheduleType, dayOfWeek, stores, user, company)
         .let { scheduleRepository.insert(it) }
   }
}
