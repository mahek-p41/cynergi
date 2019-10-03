package com.cynergisuite.middleware.schedule.infrastructure

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.schedule.Schedule
import com.cynergisuite.middleware.schedule.ScheduleFactoryService
import com.cynergisuite.middleware.schedule.ScheduleType
import com.cynergisuite.middleware.schedule.ScheduleTypeFactory
import com.cynergisuite.middleware.schedule.repository.ScheduleRepository
import com.github.javafaker.Faker
import io.micronaut.test.annotation.MicronautTest
import org.springframework.dao.DataIntegrityViolationException

import javax.inject.Inject

@MicronautTest(transactional = false)
class ScheduleRepositorySpecification extends ServiceSpecificationBase {
   @Inject ScheduleRepository scheduleRepository
   @Inject ScheduleFactoryService scheduleFactoryService

   void "insert single hourly schedule" () {
      setup:
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

   void "insert schedule when schedule_type doesnt exist" () {
      setup:
      final ScheduleType badType = null

      when:
      scheduleRepository.insert(new Schedule("Test", "Test schedule", "Jan1st", "runYearly", badType))

      then:
      def exception = thrown(IllegalArgumentException)
      exception.message.contains("Parameter specified as non-null is null")
   }

   void "insert single daily schedule is good" () {
      setup:
      final def type = new ScheduleType(2, "DAILY", "Daily", "schedule.daily")
      final def schedule = new Schedule("Test", "Test schedule", "ONCE PER DAY", "runDailyJob", type)

      when:
      def inserted = scheduleRepository.insert(schedule)

      then:
      notThrown(Exception)
      inserted != null
   }

   void "insert single daily schedule is invalid" () {
      setup:
      final def type = new ScheduleType(-1, "DAILY", "Daily", "schedule.daily")
      final def schedule = new Schedule("Test", "Test schedule", "ONCE PER DAY", "runDailyJob", type)

      when:
      def inserted = scheduleRepository.insert(schedule)

      then:
      def exception = thrown(DataIntegrityViolationException)
      exception.message.contains("ERROR: insert or update on table \"schedule\" violates foreign key constraint \"schedule_type_id_fkey\"")
   }

   void "find one is found" () {
      setup:
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

   void "not found is null" () {
      setup:
      final List<Schedule> schedules = scheduleFactoryService.stream(5, null).toList()
      final Schedule schedule = schedules[4]
      final Long i = schedule.id + 1

      when:
      Schedule found = scheduleRepository.findOne(i)

      then:
      notThrown(Exception)
      found == null
   }

   void "Query a random schedule" () {
      setup:
      def faker = new Faker()
      def random = faker.random()
      def number = random.nextInt(0,4)
      final List<Schedule> schedules = scheduleFactoryService.stream(5, null).toList()
      final Schedule schedule = schedules[number]
      final Integer i = schedule.id

      when:
      Schedule found = scheduleRepository.findOne(i)

      then:
      notThrown(Exception)
      found != null
      found == schedule
   }
}
