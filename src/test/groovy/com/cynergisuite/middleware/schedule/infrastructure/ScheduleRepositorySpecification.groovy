package com.cynergisuite.middleware.schedule.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.schedule.Schedule
import com.cynergisuite.middleware.schedule.ScheduleFactoryService
import com.cynergisuite.middleware.schedule.ScheduleType
import com.cynergisuite.middleware.schedule.ScheduleTypeFactory
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

   void "fetch all test" () {
      setup:
      def savedSchedules = scheduleFactoryService.stream(6, null).toList()

      when:
      RepositoryPage<Schedule> foundAll = scheduleRepository.fetchAll(new PageRequest())

      then:
      notThrown(Exception)
      foundAll != null
      foundAll.elements.size == 6
      foundAll.elements == savedSchedules
   }

   void "get page one" () {
      setup:
      def savedSchedules = scheduleFactoryService.stream(50, null).toList()

      when:
      RepositoryPage<Schedule> currentPage = scheduleRepository.fetchAll(new PageRequest(1, 10, "id", "ASC"))

      then:
      notThrown(Exception)
      currentPage != null
      currentPage.elements.size == 10
      currentPage.elements[0] == savedSchedules[0]
      currentPage.elements[9] == savedSchedules[9]
   }

   void "get random page" () {
      setup:
      def maxElements = 100
      def savedSchedules = scheduleFactoryService.stream(maxElements, null).toList()
      def faker = new Faker()
      def random = faker.random()
      def pageNumber = random.nextInt(1,3)
      def pageSize = random.nextInt(10,30)
      def firstRow = (pageNumber - 1) * pageSize
      def lastRow = firstRow + (pageSize - 1)

      when:
      RepositoryPage<Schedule> currentPage = scheduleRepository.fetchAll(new PageRequest(pageNumber, pageSize, "id", "ASC"))

      then:
      notThrown(Exception)
      currentPage != null
      currentPage.elements.size == pageSize
      currentPage.totalElements == maxElements.toLong()
      currentPage.elements[0] == savedSchedules[firstRow]
      currentPage.elements[pageSize - 1] == savedSchedules[lastRow]
   }

   void "out of bounds check" () {
      setup:
      def savedSchedules = scheduleFactoryService.stream(10, null).toList()

      when:
      RepositoryPage<Schedule> foundAll = scheduleRepository.fetchAll(new PageRequest(2, 10, "id", "ASC"))

      then:
      notThrown(Exception)
      foundAll == null
      foundAll.elements.size == 0
   }

}
