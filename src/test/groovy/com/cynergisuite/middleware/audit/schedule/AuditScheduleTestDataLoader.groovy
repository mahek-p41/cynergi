package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.command.ScheduleCommandTypeTestDataLoader
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.schedule.type.ScheduleTypeTestDataLoader
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

import java.time.DayOfWeek
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class AuditScheduleTestDataLoader {
   static ScheduleEntity single(DayOfWeek dayOfWeek, List<Store> stores, User user, CompanyEntity company) {
      final faker = new Faker()
      final bool = faker.bool()
      final chuckNorris = faker.chuckNorris()
      final arguments = new LinkedHashSet()

      if (user.myCompany() != company) {
         throw new Exception("User Company [${user.myCompany()}] does not match provided Company [$company")
      }

      if (!stores.isEmpty()) {
         for (store in stores) {
            if (store.myCompany() != company) {
               throw new Exception("Store dataset [${store.myCompany().datasetCode}] does not match provided Company [${company.datasetCode}] dataset")
            } else {
               arguments.add(
                  new ScheduleArgumentEntity(
                     store.myNumber().toString(),
                     "storeNumber",
                     false,
                  )
               )
            }
         }
      } else {
         throw new Exception("Must provide at least one store")
      }

      arguments.add(
         new ScheduleArgumentEntity(
            "en-US",
            "locale",
            false,
         )
      )

      arguments.add(
         new ScheduleArgumentEntity(
            user.myEmployeeType(),
            "employeeType",
            false,
         )
      )

      arguments.add(
         new ScheduleArgumentEntity(
            user.myEmployeeNumber().toString(),
            "employeeNumber",
            false,
         )
      )

      return new ScheduleEntity(
         null,
         chuckNorris.fact().take(36),
         chuckNorris.fact().take(255),
         dayOfWeek.name(),
         ScheduleCommandTypeTestDataLoader.auditSchedule(),
         ScheduleTypeTestDataLoader.daily(),
         true,
         user.myCompany(),
         arguments
      )
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AuditScheduleTestDataLoaderService {
   private final ScheduleRepository scheduleRepository

   AuditScheduleTestDataLoaderService(ScheduleRepository scheduleRepository) {
      this.scheduleRepository = scheduleRepository
   }

   Stream<ScheduleEntity> stream(int numberIn = 1, DayOfWeek dayOfWeek, List<StoreEntity> stores, User user, CompanyEntity company) {
      final number = numberIn > 0 ? numberIn : 1

      return IntStream.range(0, number).mapToObj {
         single(dayOfWeek, stores as List<Store>, user, company)
      }
   }

   ScheduleEntity single(DayOfWeek dayOfWeek, List<Store> stores, User user, CompanyEntity company) {
      return AuditScheduleTestDataLoader.single(dayOfWeek, stores, user, company)
         .with { scheduleRepository.insert(it) }
   }
}
