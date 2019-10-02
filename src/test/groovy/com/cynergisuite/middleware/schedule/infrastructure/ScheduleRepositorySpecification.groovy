package com.cynergisuite.middleware.schedule.infrastructure

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.schedule.Schedule
import com.cynergisuite.middleware.schedule.ScheduleFactoryService
import com.cynergisuite.middleware.schedule.ScheduleType
import com.cynergisuite.middleware.schedule.ScheduleTypeFactory
import com.cynergisuite.middleware.schedule.repository.ScheduleRepository
import io.micronaut.test.annotation.MicronautTest
import org.springframework.dao.DataIntegrityViolationException

import javax.inject.Inject

@MicronautTest(transactional = false)
class ScheduleRepositorySpecification extends ServiceSpecificationBase {
   @Inject ScheduleRepository scheduleRepository
   @Inject ScheduleFactoryService scheduleFactoryService

   void "insert single hourly schedule" () {
      given:
      final def type = ScheduleTypeFactory.hourly()

      when:
      def inserted = scheduleRepository.insert(new Schedule("Test", "Test schedule", "ONCE PER HOUR", "runHourlyJob", type))

      then:
      notThrown(Exception)
      inserted.id != null
      inserted.id > 0
      inserted.uuRowId != null
      inserted.timeCreated != null
      inserted.timeUpdated != null
      inserted.title == "Test"
      inserted.description == "Test schedule"
      inserted.schedule == "ONCE PER HOUR"
      inserted.command == "runHourlyJob"
      inserted.type == type
   }

   void "insert single daily schedule" () {
      given:
      final def type = new ScheduleType(2, "DAILY", "Daily", "schedule.daily")

      when:
      def inserted = scheduleRepository.insert(new Schedule("Test", "Test schedule", "ONCE PER DAY", "runDailyJob", type))

      then:
      def exception = thrown(DataIntegrityViolationException)
      exception.message.contains("ERROR: insert or update on table \"schedule\" violates foreign key constraint \"schedule_type_id_fkey\"")
   }

   void "find one" () {
      given:
      final List<Schedule> schedules = scheduleFactoryService.stream(3, null).toList()
      final Schedule schedule = schedules[0]

      when:
      Schedule found = scheduleRepository.findOne(schedule.id)

      then:
      notThrown(Exception)
      found != null
      !found.is(schedule)
      found == schedule
   }
}
