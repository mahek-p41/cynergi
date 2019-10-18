package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.middleware.schedule.Schedule
import com.cynergisuite.middleware.schedule.ScheduleFactory
import com.cynergisuite.middleware.store.StoreFactory
import com.github.javafaker.Faker
import java.util.stream.IntStream
import java.util.stream.Stream

object AuditScheduleFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, scheduleIn: Schedule? = null) : Stream<AuditSchedule> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()
      val store = StoreFactory.random()
      val schedule = ScheduleFactory.single()


      return IntStream.range(0, number).mapToObj {
         AuditSchedule(
            id = null,
            store = store,
            departmentAccess = lorem.characters(2, 2),
            schedule = schedule
         )
      }
   }
}
