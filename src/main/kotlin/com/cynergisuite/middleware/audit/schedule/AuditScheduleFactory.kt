package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.extensions.truncate
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.command.ScheduleCommandTypeFactory
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.schedule.type.ScheduleTypeFactory
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
   fun single(dayOfWeek: DayOfWeek, stores: List<Store>, user: User, company: Company): ScheduleEntity {
      val faker = Faker()
      val bool = faker.bool()
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
            "en-US",
            "locale"
         )
      )

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
         title = chuckNorris.fact().truncate(36)!!,
         description = if (bool.bool()) chuckNorris.fact().truncate(255) else null,
         schedule = dayOfWeek.name,
         command = ScheduleCommandTypeFactory.auditSchedule(),
         type = ScheduleTypeFactory.weekly(),
         arguments = arguments
      )
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AuditScheduleFactoryService @Inject constructor(
   private val scheduleRepository: ScheduleRepository
) {

   fun stream(numberIn: Int = 1, dayOfWeek: DayOfWeek, stores: List<StoreEntity>, user: User, company: Company): Stream<ScheduleEntity> {
      val number = if (numberIn > 0) numberIn else 1

      return IntStream.range(0, number).mapToObj {
         single(dayOfWeek, stores, user, company)
      }
   }

   fun single(dayOfWeek: DayOfWeek, stores: List<Store>, user: User, company: Company): ScheduleEntity {
      return AuditScheduleFactory.single(dayOfWeek, stores, user, company)
         .let { scheduleRepository.insert(it) }
   }
}
