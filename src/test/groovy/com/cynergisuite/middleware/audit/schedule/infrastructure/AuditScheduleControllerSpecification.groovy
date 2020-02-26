package com.cynergisuite.middleware.audit.schedule.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDataTransferObject
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.audit.schedule.AuditScheduleCreateUpdateDataTransferObject
import com.cynergisuite.middleware.audit.schedule.AuditScheduleFactoryService
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.store.StoreFactoryService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NO_CONTENT
import static java.time.DayOfWeek.*

@MicronautTest(transactional = false)
class AuditScheduleControllerSpecification extends ControllerSpecificationBase {
   @Inject AuditScheduleFactoryService auditScheduleFactoryService
   @Inject ScheduleRepository scheduleRepository
   @Inject StoreFactoryService storeFactoryService
   @Inject EmployeeFactoryService employeeFactoryService

   void "fetch one"() {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.random(company)
      final employee = employeeFactoryService.singleUser(store)
      final auditSchedule = auditScheduleFactoryService.single(FRIDAY, [store], employee, company)

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
      result.enabled == true
   }

   void "fetch one two stores"() {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final storeThree = storeFactoryService.store(3, company)
      final employee = employeeFactoryService.singleUser(storeOne)
      final auditSchedule = auditScheduleFactoryService.single(TUESDAY, [storeOne, storeThree], employee, company)

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
      result.enabled == true
   }

   void "fetch all"() {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.random(company)
      final emp = employeeFactoryService.single(store)
      final List<ScheduleEntity> auditSchedules = auditScheduleFactoryService.stream(10, FRIDAY, [store], emp, company).toList()
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      final pageThree = new StandardPageRequest(3, 5, "id", "ASC")

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
      pageOneResult.elements[0].enabled == true
      pageOneResult.elements[4].id == auditSchedules[4].id
      pageOneResult.elements[4].schedule == "FRIDAY"
      pageOneResult.elements[4].title == auditSchedules[4].title
      pageOneResult.elements[4].description == auditSchedules[4].description
      pageOneResult.elements[4].schedule == auditSchedules[4].schedule
      pageOneResult.elements[4].stores.size() == 1
      pageOneResult.elements[4].stores[0].storeNumber == store.number
      pageOneResult.elements[4].enabled == true

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
      pageTwoResult.elements[0].enabled == true
      pageTwoResult.elements[4].id == auditSchedules[9].id
      pageOneResult.elements[4].schedule == "FRIDAY"
      pageTwoResult.elements[4].title == auditSchedules[9].title
      pageTwoResult.elements[4].description == auditSchedules[9].description
      pageTwoResult.elements[4].schedule == auditSchedules[9].schedule
      pageTwoResult.elements[4].stores.size() == 1
      pageTwoResult.elements[4].stores[0].storeNumber == store.number
      pageTwoResult.elements[4].enabled == true

      when:
      get("/audit/schedule${pageThree}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "fetch all with multiple stores" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final storeThree = storeFactoryService.store(3, company)
      final emp = employeeFactoryService.single(storeOne)
      final List<ScheduleEntity> auditSchedules = auditScheduleFactoryService.stream(10, TUESDAY, [storeOne, storeThree], emp, company).toList()
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageThree = new StandardPageRequest(3, 5, "id", "ASC")

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
      pageOneResult.elements[4].id == auditSchedules[4].id
      pageOneResult.elements[4].schedule == "TUESDAY"
      pageOneResult.elements[4].title == auditSchedules[4].title
      pageOneResult.elements[4].description == auditSchedules[4].description
      pageOneResult.elements[4].schedule == auditSchedules[4].schedule
      pageOneResult.elements[4].stores.size() == 2
      pageOneResult.elements[4].stores.collect { it.storeNumber }.sort() == [storeOne.number, storeThree.number]

      when:
      get("/audit/schedule${pageThree}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create audit schedule"() {
      given:
      final company = companyFactoryService.random()
      final store = storeFactoryService.random(company)

      when:
      def result = post("/audit/schedule", new AuditScheduleCreateUpdateDataTransferObject("test schedule", "test schedule description", TUESDAY, [store] as Set))

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.id > 0
      result.title == "test schedule"
      result.description == "test schedule description"
      result.schedule == "TUESDAY"
      result.stores.size() == 1
      result.stores[0].storeNumber == store.number
      result.enabled == true
   }

   void "create disabled audit schedule" () {
      given:
      final company = companyFactoryService.random()
      final store = storeFactoryService.random(company)

      when:
      def result = post("/audit/schedule", new AuditScheduleCreateUpdateDataTransferObject("test schedule", "test schedule description", TUESDAY, [store] as Set, false))

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.id > 0
      result.title == "test schedule"
      result.description == "test schedule description"
      result.schedule == "TUESDAY"
      result.stores.size() == 1
      result.stores[0].storeNumber == store.number
      result.enabled == false
   }

   void "create audit schedule with invalid store" () {
      when:
      post("/audit/schedule", new AuditScheduleCreateUpdateDataTransferObject([title: "test schedule", description: "test schedule description", schedule: TUESDAY, stores: [new SimpleIdentifiableDataTransferObject(42L)] as Set, enabled: true]))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "store[0].id"
      response[0].message == "42 was unable to be found"
   }

   void "create audit schedule with no stores" () {
      when:
      post("/audit/schedule", new AuditScheduleCreateUpdateDataTransferObject("test schedule", "test schedule description", TUESDAY, [] as Set, true))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "stores"
      response[0].message == "Is not allowed to be empty"
   }

   void "update audit schedule add store" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final storeThree = storeFactoryService.store(3, company)
      final employee = employeeFactoryService.singleUser(storeOne)
      final schedule = auditScheduleFactoryService.single(MONDAY, [storeOne], employee, company)

      when:
      def result = put("/audit/schedule", new AuditScheduleCreateUpdateDataTransferObject(schedule.id,"Updated title", "Updated description", TUESDAY, [storeOne, storeThree] as Set))
      def loadedSchedule = scheduleRepository.findOne(schedule.id)

      then:
      notThrown(HttpClientResponseException)
      result.title == "Updated title"
      result.description == "Updated description"
      result.schedule == "TUESDAY"
      result.stores.size() == 2
      result.stores.collect { it.storeNumber }.sort() == [storeOne.number, storeThree.number]
      result.enabled == true

      loadedSchedule.arguments.size() == 4
      loadedSchedule.arguments.find { it.description == "employeeNumber" && it.value == authenticatedEmployee.number.toString() } != null
      loadedSchedule.arguments.find { it.description == "locale" } != null
      loadedSchedule.arguments.find { it.description == "storeNumber" && it.value == storeOne.number.toString() } != null
      loadedSchedule.arguments.find { it.description == "storeNumber" && it.value == storeThree.number.toString() } != null
   }

   void "update audit schedule change from enabled to disabled" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final employee = employeeFactoryService.singleUser(storeOne)
      final schedule = auditScheduleFactoryService.single(MONDAY, [storeOne], employee, company)

      when:
      def result = put("/audit/schedule", new AuditScheduleCreateUpdateDataTransferObject([id: schedule.id, title: "Updated title", description:  "Updated description", schedule:  TUESDAY, stores: [new SimpleIdentifiableDataTransferObject(storeOne)] as Set, enabled: false]))
      def loadedSchedule = scheduleRepository.findOne(schedule.id)

      then:
      notThrown(Exception)
      result.id == schedule.id
      result.title == "Updated title"
      result.description == "Updated description"
      result.schedule == "TUESDAY"
      result.stores.size() == 1
      result.stores.collect { it.storeNumber }.sort() == [storeOne.number]
      result.enabled == false

      loadedSchedule.arguments.size() == 3
      loadedSchedule.arguments.find { it.description == "employeeNumber" && it.value == authenticatedEmployee.number.toString() } != null
      loadedSchedule.arguments.find { it.description == "locale" } != null
      loadedSchedule.arguments.find { it.description == "storeNumber" && it.value == storeOne.number.toString() } != null
   }

   void "update audit schedule remove store" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final storeThree = storeFactoryService.store(3, company)
      final employee = employeeFactoryService.singleUser(storeOne)
      final schedule = auditScheduleFactoryService.single(MONDAY, [storeOne, storeThree], employee, company)

      when:
      def result = put("/audit/schedule", new AuditScheduleCreateUpdateDataTransferObject(schedule.id,"Updated title", "Updated description", TUESDAY, [storeOne] as Set))

      then:
      notThrown(HttpClientResponseException)
      result.id == schedule.id
      result.title == "Updated title"
      result.description == "Updated description"
      result.schedule == "TUESDAY"
      result.stores.size() == 1
      result.stores.collect { it.storeNumber }.sort() == [storeOne.number]
      result.enabled == true

      when:
      result = get("/audit/schedule/${schedule.id}")
      def loadedSchedule = scheduleRepository.findOne(schedule.id)

      then:
      notThrown(HttpClientResponseException)
      result.id == schedule.id
      result.title == "Updated title"
      result.description == "Updated description"
      result.schedule == "TUESDAY"
      result.stores.size() == 1
      result.stores.collect { it.storeNumber }.sort() == [storeOne.number]
      result.enabled == true

      loadedSchedule.arguments.size() == 3
      loadedSchedule.arguments.find { it.description == "employeeNumber" } != null
      loadedSchedule.arguments.find { it.description == "locale" } != null
      loadedSchedule.arguments.find { it.description == "storeNumber" } != null
   }

   void "update audit schedule without id" () {
      given:
      final company = companyFactoryService.random()
      final store = storeFactoryService.random(company)

      when:
      put("/audit/schedule", new AuditScheduleCreateUpdateDataTransferObject("test schedule", "test schedule description", TUESDAY, [store] as Set))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "id"
      response[0].message == "Is required"
   }
}
