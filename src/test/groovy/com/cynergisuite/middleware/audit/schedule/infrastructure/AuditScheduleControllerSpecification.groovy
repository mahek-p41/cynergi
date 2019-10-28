package com.cynergisuite.middleware.audit.schedule.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.audit.schedule.AuditScheduleFactoryService
import com.cynergisuite.middleware.department.DepartmentFactoryService
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.store.StoreFactoryService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.NOT_FOUND
import static java.time.DayOfWeek.FRIDAY
import static java.time.DayOfWeek.TUESDAY

@MicronautTest(transactional = false)
class AuditScheduleControllerSpecification extends ControllerSpecificationBase {
   @Inject AuditScheduleFactoryService auditScheduleFactoryService
   @Inject DepartmentFactoryService departmentFactoryService
   @Inject StoreFactoryService storeFactoryService

   void "fetch one"() {
      given:
      final department = departmentFactoryService.random()
      final store = storeFactoryService.random()
      final auditSchedule = auditScheduleFactoryService.single(FRIDAY, [store], department)

      when:
      def result = get("/audit/schedule/${auditSchedule.id}")

      then:
      notThrown(HttpClientResponseException)
      result.id == auditSchedule.id
      result.title == auditSchedule.title
      result.description == auditSchedule.description
      result.schedule == "FRIDAY"
      result.stores.size() == 1
      result.stores[0].storeNumber == store.number
      result.department.code == department.code
   }

   void "fetch one two stores"() {
      given:
      final department = departmentFactoryService.random()
      final storeOne = storeFactoryService.storeOne()
      final storeThree = storeFactoryService.storeThree()
      final auditSchedule = auditScheduleFactoryService.single(TUESDAY, [storeOne, storeThree], department)

      when:
      def result = get("/audit/schedule/${auditSchedule.id}")

      then:
      notThrown(HttpClientResponseException)
      result.id == auditSchedule.id
      result.title == auditSchedule.title
      result.description == auditSchedule.description
      result.schedule == "TUESDAY"
      result.stores.size() == 2
      result.stores.collect { it.storeNumber }.sort() == [storeOne.number, storeThree.number]
      result.department.code == department.code
   }

   void "fetch all"() {
      given:
      final department = departmentFactoryService.random()
      final store = storeFactoryService.random()
      final List<ScheduleEntity> auditSchedules = auditScheduleFactoryService.stream(10, FRIDAY, [store], department).toList()
      final pageOne = new PageRequest(1, 5, "id", "ASC")
      final pageTwo = new PageRequest(2, 5, "id", "ASC")
      final pageThree = new PageRequest(3, 5, "id", "ASC")

      when:
      def pageOneResult = get("/audit/schedule${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.totalElements == 10
      pageOneResult.totalPages == 2
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.elements[0].id == auditSchedules[0].id
      pageOneResult.elements[0].schedule == "FRIDAY"
      pageOneResult.elements[0].title == auditSchedules[0].title
      pageOneResult.elements[0].description == auditSchedules[0].description
      pageOneResult.elements[0].schedule == auditSchedules[0].schedule
      pageOneResult.elements[0].stores.size() == 1
      pageOneResult.elements[0].stores[0].storeNumber == store.number
      pageOneResult.elements[0].department.id == department.id
      pageOneResult.elements[0].department.code == department.code
      pageOneResult.elements[4].id == auditSchedules[4].id
      pageOneResult.elements[4].schedule == "FRIDAY"
      pageOneResult.elements[4].title == auditSchedules[4].title
      pageOneResult.elements[4].description == auditSchedules[4].description
      pageOneResult.elements[4].schedule == auditSchedules[4].schedule
      pageOneResult.elements[4].stores.size() == 1
      pageOneResult.elements[4].stores[0].storeNumber == store.number
      pageOneResult.elements[4].department.id == department.id
      pageOneResult.elements[4].department.code == department.code

      when:
      def pageTwoResult = get("/audit/schedule${pageTwo}")

      then:
      notThrown(HttpClientResponseException)
      pageTwoResult.totalElements == 10
      pageTwoResult.totalPages == 2
      pageTwoResult.first == false
      pageTwoResult.last == true
      pageTwoResult.elements != null
      pageTwoResult.elements.size() == 5
      pageTwoResult.elements[0].id == auditSchedules[5].id
      pageOneResult.elements[0].schedule == "FRIDAY"
      pageTwoResult.elements[0].title == auditSchedules[5].title
      pageTwoResult.elements[0].description == auditSchedules[5].description
      pageTwoResult.elements[0].schedule == auditSchedules[5].schedule
      pageTwoResult.elements[0].stores.size() == 1
      pageTwoResult.elements[0].stores[0].storeNumber == store.number
      pageTwoResult.elements[0].department.id == department.id
      pageTwoResult.elements[0].department.code == department.code
      pageTwoResult.elements[4].id == auditSchedules[9].id
      pageOneResult.elements[4].schedule == "FRIDAY"
      pageTwoResult.elements[4].title == auditSchedules[9].title
      pageTwoResult.elements[4].description == auditSchedules[9].description
      pageTwoResult.elements[4].schedule == auditSchedules[9].schedule
      pageTwoResult.elements[4].stores.size() == 1
      pageTwoResult.elements[4].stores[0].storeNumber == store.number
      pageTwoResult.elements[4].department.id == department.id
      pageTwoResult.elements[4].department.code == department.code

      when:
      get("/audit/schedule${pageThree}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NOT_FOUND
      final notFoundResult = notFoundException.response.bodyAsJson()
      notFoundResult.size() == 1
      notFoundResult.message == "Request with Page 3, Size 5, Sort By id and Sort Direction ASC produced no results"
   }

   void "fetch all with multiple stores" () {
      given:
      final department = departmentFactoryService.random()
      final storeOne = storeFactoryService.storeOne()
      final storeThree = storeFactoryService.storeThree()
      final List<ScheduleEntity> auditSchedules = auditScheduleFactoryService.stream(10, TUESDAY, [storeOne, storeThree], department).toList()
      final pageOne = new PageRequest(1, 5, "id", "ASC")
      final pageThree = new PageRequest(3, 5, "id", "ASC")

      when:
      def pageOneResult = get("/audit/schedule${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.totalElements == 10
      pageOneResult.totalPages == 2
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.elements[0].id == auditSchedules[0].id
      pageOneResult.elements[0].schedule == "TUESDAY"
      pageOneResult.elements[0].title == auditSchedules[0].title
      pageOneResult.elements[0].description == auditSchedules[0].description
      pageOneResult.elements[0].schedule == auditSchedules[0].schedule
      pageOneResult.elements[0].stores.size() == 2
      pageOneResult.elements[0].stores.collect { it.storeNumber }.sort() == [storeOne.number, storeThree.number]
      pageOneResult.elements[0].department.id == department.id
      pageOneResult.elements[0].department.code == department.code
      pageOneResult.elements[4].id == auditSchedules[4].id
      pageOneResult.elements[4].schedule == "TUESDAY"
      pageOneResult.elements[4].title == auditSchedules[4].title
      pageOneResult.elements[4].description == auditSchedules[4].description
      pageOneResult.elements[4].schedule == auditSchedules[4].schedule
      pageOneResult.elements[4].stores.size() == 2
      pageOneResult.elements[4].stores.collect { it.storeNumber }.sort() == [storeOne.number, storeThree.number]
      pageOneResult.elements[4].department.id == department.id
      pageOneResult.elements[4].department.code == department.code

      when:
      get("/audit/schedule${pageThree}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NOT_FOUND
      final notFoundResult = notFoundException.response.bodyAsJson()
      notFoundResult.size() == 1
      notFoundResult.message == "Request with Page 3, Size 5, Sort By id and Sort Direction ASC produced no results"
   }
}
