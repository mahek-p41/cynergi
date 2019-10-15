package com.cynergisuite.middleware.schedule.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.schedule.Schedule
import com.cynergisuite.middleware.schedule.ScheduleFactoryService
import com.cynergisuite.middleware.schedule.ScheduleType
import com.cynergisuite.middleware.schedule.ScheduleTypeFactory
import io.micronaut.test.annotation.MicronautTest
import org.apache.commons.lang3.RandomUtils
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
      scheduleRepository.insert(schedule)

      then:
      def exception = thrown(DataIntegrityViolationException)
      exception.message.contains("ERROR: insert or update on table \"schedule\" violates foreign key constraint \"schedule_type_id_fkey\"")
   }

   void "update a schedule where all five fields changed" () {
      given:
      final ScheduleType hourly = ScheduleTypeFactory.hourly()
      final ScheduleType monthly = ScheduleTypeFactory.monthly()
      ScheduleType typeValue

      final List<Schedule> schedules = scheduleFactoryService.stream(3, null).toList()
      final Schedule one = schedules[RandomUtils.nextInt(0, 2)]
      final String titleValue = "New Title"
      final String descValue = "New Description"
      final String scheduleValue = "New Schedule"
      final String commandValue = "New Command"
      if (one.type == hourly) {
         typeValue = monthly
      } else {
         typeValue = hourly
      }

      final Schedule temp = new Schedule(one.id, one.uuRowId, one.timeCreated, one.timeUpdated,
                                         titleValue, descValue, scheduleValue, commandValue, typeValue)

      when:
      Schedule returnedSchedule = scheduleRepository.update(temp)

      then:
      notThrown(Exception)
      returnedSchedule != null
      returnedSchedule.id          == one.id
      returnedSchedule.uuRowId     == one.uuRowId
      returnedSchedule.timeCreated == one.timeCreated
      returnedSchedule.timeUpdated.isAfter(one.timeUpdated)
      returnedSchedule.title       == titleValue
      returnedSchedule.description == descValue
      returnedSchedule.schedule    == scheduleValue
      returnedSchedule.command     == commandValue
      returnedSchedule.type        == typeValue
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
      final def number = RandomUtils.nextInt(0,4)
      final List<Schedule> schedules = scheduleFactoryService.stream(5, null).toList()
      final Schedule schedule = schedules[number]
      final Long i = schedule.id

      when:
      Schedule found = scheduleRepository.findOne(i)

      then:
      notThrown(Exception)
      found != null
      found == schedule
   }

   void "get one page that isnt a full page" () {
      setup:
      final def savedSchedules = scheduleFactoryService.stream(6, null).toList()

      when:
      RepositoryPage<Schedule> currentPage = scheduleRepository.fetchAll(new PageRequest())

      then:
      notThrown(Exception)
      currentPage != null
      currentPage.elements.size == 6
      currentPage.elements[0] == savedSchedules[0]
      currentPage.elements[5] == savedSchedules[5]
   }

   void "get page one" () {
      setup:
      final def savedSchedules = scheduleFactoryService.stream(50, null).toList()

      when:
      RepositoryPage<Schedule> currentPage = scheduleRepository.fetchAll(new PageRequest(1, 10, "id", "ASC"))

      then:
      notThrown(Exception)
      currentPage != null
      currentPage.elements.size == 10
      currentPage.elements[0] == savedSchedules[0]
      currentPage.elements[9] == savedSchedules[9]
   }

   void "get random page and random page size" () {
      setup:
      final def maxElements = RandomUtils.nextInt(100,110)
      final def savedSchedules = scheduleFactoryService.stream(maxElements, null).toList()
      final def pageNumber = RandomUtils.nextInt(1,3)
      final def pageSize = RandomUtils.nextInt(10,30)
      final Integer firstRow = (pageNumber - 1) * pageSize
      final Integer lastRow = firstRow + (pageSize - 1)

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
      scheduleFactoryService.stream(10, null).toList()

      when:
      RepositoryPage<Schedule> onePage = scheduleRepository.fetchAll(new PageRequest(2, 10, "id", "ASC"))

      then:
      notThrown(Exception)
      onePage.elements.size == 0
   }

   void "delete 1 schedule" () {
      setup:
      final List<Schedule> buildSchedules = scheduleFactoryService.stream(5, null).toList()
      final Schedule oneSchedule = buildSchedules[1]
      final Long deleteId = oneSchedule.id
      final Schedule original = scheduleRepository.findOne(deleteId)

      when:
      Integer deleteCount = scheduleRepository.delete(deleteId)

      then:
      notThrown(Exception)
      scheduleRepository.findOne(deleteId) == null
      oneSchedule == original
      deleteCount != 0
   }

   void "delete schedules" () {
      setup:
      final List<Schedule> buildSchedules = scheduleFactoryService.stream(10, null).toList()
      List<Schedule> deleteSchedules = new ArrayList<Schedule>()
      deleteSchedules.add(buildSchedules[1])
      deleteSchedules.add(buildSchedules[3])
      deleteSchedules.add(buildSchedules[8])

      when:
      Integer deleteCount = scheduleRepository.deleteList(deleteSchedules)

      then:
      notThrown(Exception)
      deleteCount == 3
      scheduleRepository.findOne(buildSchedules[0].id) != null
      scheduleRepository.findOne(buildSchedules[2].id) != null
      scheduleRepository.findOne(buildSchedules[4].id) != null
      scheduleRepository.findOne(buildSchedules[5].id) != null
      scheduleRepository.findOne(buildSchedules[6].id) != null
      scheduleRepository.findOne(buildSchedules[7].id) != null
      scheduleRepository.findOne(buildSchedules[9].id) != null

      scheduleRepository.findOne(buildSchedules[1].id) == null
      scheduleRepository.findOne(buildSchedules[3].id) == null
      scheduleRepository.findOne(buildSchedules[8].id) == null
   }


}
