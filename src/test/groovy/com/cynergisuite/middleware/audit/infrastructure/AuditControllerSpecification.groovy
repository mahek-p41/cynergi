package com.cynergisuite.middleware.audit.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.audit.AuditCreateValueObject
import com.cynergisuite.middleware.audit.AuditFactory
import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.AuditUpdateValueObject
import com.cynergisuite.middleware.audit.AuditValueObject
import com.cynergisuite.middleware.audit.action.AuditActionValueObject
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.audit.status.AuditStatusValueObject
import com.cynergisuite.middleware.error.ErrorValueObject
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.store.StoreFactoryService
import com.cynergisuite.middleware.store.StoreValueObject
import io.micronaut.core.type.Argument
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject
import java.time.OffsetDateTime

import static io.micronaut.http.HttpRequest.PUT
import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class AuditControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/audit"
   private static final Locale locale = Locale.US

   @Inject AuditFactoryService auditFactoryService
   @Inject AuditRepository auditRepository
   @Inject LocalizationService localizationService
   @Inject StoreFactoryService storeFactoryService

   void "fetch one audit by id" () {
      given:
      final store = storeFactoryService.random()
      final savedAudit = auditFactoryService.single(store)

      when:
      def result = get("$path/${savedAudit.id}")

      then:
      notThrown(HttpClientResponseException)
      result.id == savedAudit.id
      result.timeCreated.with { OffsetDateTime.parse(it) } == savedAudit.timeCreated
      result.timeUpdated.with { OffsetDateTime.parse(it) } == savedAudit.timeUpdated
      result.currentStatus.value == 'OPENED'
      result.store.storeNumber == store.number
      result.store.name == store.name
      result.store.dataset == store.dataset
      result.actions.size() == 1
      result.actions[0].id > 0
      result.actions[0].status.value == 'OPENED'
      result.actions[0].status.description == 'Opened'
      result.actions[0].changedBy.number == savedAudit.actions[0].changedBy.number
      result.actions[0].timeCreated.with { OffsetDateTime.parse(it) } == savedAudit.actions[0].timeCreated
      result.actions[0].timeUpdated.with { OffsetDateTime.parse(it) } == savedAudit.actions[0].timeUpdated
   }

   void "fetch one audit by id not found" () {
      when:
      get("$path/0")

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.body().with { parseResponse(it) }
      response.size() == 1
      response.message == "0 was unable to be found"
   }

   void "fetch all audits for store 1" () {
      given:
      final store = storeFactoryService.store(1)
      final def twentyAudits = auditFactoryService.stream(20, store).collect { new AuditValueObject(it, locale, localizationService) }
      final def pageOne = new AuditPageRequest([page: 1, size:  5, sortBy:  "id", sortDirection: "ASC", storeNumber:  store.number])
      final def pageTwo = new AuditPageRequest([page:  2, size:  5, sortBy:  "id", sortDirection:  "ASC", storeNumber: store.number])
      final def pageFive = new AuditPageRequest([page:  5, size:  5, sortBy:  "id", sortDirection:  "ASC", storeNumber: store.number])
      final def firstFiveAudits = twentyAudits[0..4]
      final def secondFiveAudits = twentyAudits[5..9]

      when:
      def pageOneResult = get("${path}${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.requested.storeNumber == store.number
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.elements[0].id == firstFiveAudits[0].id
      pageOneResult.elements[0].store.id == store.id
      pageOneResult.elements[0].actions.size() == 1
      pageOneResult.elements[0].actions[0].status.value == "OPENED"
      pageOneResult.elements[0].actions[0].status.color == "FF0000"
      pageOneResult.elements[0].actions[0].id == firstFiveAudits[0].actions[0].id

      when:
      def pageTwoResult = get("${path}${pageTwo}")

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.requested.storeNumber == store.number
      pageTwoResult.elements != null
      pageTwoResult.elements.size() == 5
      pageTwoResult.elements[0].actions.size() == 1
      pageTwoResult.elements[0].actions[0].status.value == "OPENED"
      pageTwoResult.elements[0].actions[0].status.color == "FF0000"
      pageTwoResult.elements[0].id == secondFiveAudits[0].id

      when:
      get("${path}${pageFive}")

      then:
      final def notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NOT_FOUND
      final def notFoundResult = notFoundException.response.bodyAsJson()
      notFoundResult.size() == 1
      notFoundResult.message == "Request with Page 5, Size 5, Sort By id and Sort Direction ASC produced no results"
   }

   void "fetch all audits by store" () {
      given:
      final storeOne = storeFactoryService.store(1)
      final storeThree = storeFactoryService.store(3)
      final def fiveAuditsStoreOne = auditFactoryService.stream(5, storeOne).collect { new AuditValueObject(it, locale, localizationService) }
      final def tenAuditsStoreThree = auditFactoryService.stream(10, storeThree).collect { new AuditValueObject(it, locale, localizationService) }

      when:
      def storeOneFilterResult = get("${path}" + new AuditPageRequest([page: 1, size: 5, sortBy: 'id', storeNumber: 1]))

      then:
      notThrown(HttpClientResponseException)
      storeOneFilterResult.requested.storeNumber == storeOne.number
      storeOneFilterResult.totalElements == 5
      storeOneFilterResult.totalPages == 1
      storeOneFilterResult.first == true
      storeOneFilterResult.last == true
      storeOneFilterResult.elements != null
      storeOneFilterResult.elements.size() == 5
      storeOneFilterResult.elements[0].id > 0
      storeOneFilterResult.elements[0].store.storeNumber == storeOne.number
      storeOneFilterResult.elements[0].actions[0].id == fiveAuditsStoreOne[0].actions[0].id
      storeOneFilterResult.elements[0].actions[0].status.value == fiveAuditsStoreOne[0].actions[0].status.value
      storeOneFilterResult.elements[0].actions[0].status.description == fiveAuditsStoreOne[0].actions[0].status.description
      storeOneFilterResult.elements[0].actions[0].changedBy.number == fiveAuditsStoreOne[0].actions[0].changedBy.number
      storeOneFilterResult.elements[0].actions[0].changedBy.lastName == fiveAuditsStoreOne[0].actions[0].changedBy.lastName
      storeOneFilterResult.elements[0].actions[0].changedBy.firstNameMi == fiveAuditsStoreOne[0].actions[0].changedBy.firstNameMi
      storeOneFilterResult.elements[4].actions[0].id == fiveAuditsStoreOne[4].actions[0].id
      storeOneFilterResult.elements[4].actions[0].status.value == fiveAuditsStoreOne[4].actions[0].status.value
      storeOneFilterResult.elements[4].actions[0].status.description == fiveAuditsStoreOne[4].actions[0].status.description
      storeOneFilterResult.elements[4].actions[0].changedBy.number == fiveAuditsStoreOne[4].actions[0].changedBy.number
      storeOneFilterResult.elements[4].actions[0].changedBy.lastName == fiveAuditsStoreOne[4].actions[0].changedBy.lastName
      storeOneFilterResult.elements[4].actions[0].changedBy.firstNameMi == fiveAuditsStoreOne[4].actions[0].changedBy.firstNameMi

      when:
      def storeThreeFilterResult = get("$path" + new AuditPageRequest([page: 1, size: 5, sortBy: 'id', storeNumber: 3]))

      then:
      notThrown(HttpClientResponseException)
      storeThreeFilterResult.elements != null
      storeThreeFilterResult.elements.size() == 5
      storeThreeFilterResult.requested.storeNumber == 3
      storeThreeFilterResult.totalElements == 10
      storeThreeFilterResult.totalPages == 2
      storeThreeFilterResult.first == true
      storeThreeFilterResult.last == false
      storeThreeFilterResult.elements[0].store.storeNumber == storeThree.number
      storeThreeFilterResult.elements[0].actions[0].id == tenAuditsStoreThree[0].actions[0].id
      storeThreeFilterResult.elements[0].actions[0].status.value == tenAuditsStoreThree[0].actions[0].status.value
      storeThreeFilterResult.elements[0].actions[0].status.description == tenAuditsStoreThree[0].actions[0].status.description
      storeThreeFilterResult.elements[0].actions[0].changedBy.number == tenAuditsStoreThree[0].actions[0].changedBy.number
      storeThreeFilterResult.elements[0].actions[0].changedBy.lastName == tenAuditsStoreThree[0].actions[0].changedBy.lastName
      storeThreeFilterResult.elements[0].actions[0].changedBy.firstNameMi == tenAuditsStoreThree[0].actions[0].changedBy.firstNameMi
      storeThreeFilterResult.elements[4].actions[0].id == tenAuditsStoreThree[4].actions[0].id
      storeThreeFilterResult.elements[4].actions[0].status.value == tenAuditsStoreThree[4].actions[0].status.value
      storeThreeFilterResult.elements[4].actions[0].status.description == tenAuditsStoreThree[4].actions[0].status.description
      storeThreeFilterResult.elements[4].actions[0].changedBy.number == tenAuditsStoreThree[4].actions[0].changedBy.number
      storeThreeFilterResult.elements[4].actions[0].changedBy.lastName == tenAuditsStoreThree[4].actions[0].changedBy.lastName
      storeThreeFilterResult.elements[4].actions[0].changedBy.firstNameMi == tenAuditsStoreThree[4].actions[0].changedBy.firstNameMi
   }

   void "fetch all by status" () {
      given:
      final storeOne = storeFactoryService.store(1)
      final storeThree = storeFactoryService.store(3)
      final storeOneOpenAuditOne = auditFactoryService.single(storeOne, authenticatedEmployee, [AuditStatusFactory.opened()] as Set).with { new AuditValueObject(it, locale, localizationService) }
      final storeThreeOpenAuditOne = auditFactoryService.single(storeThree, authenticatedEmployee, [AuditStatusFactory.opened()] as Set).with { new AuditValueObject(it, locale, localizationService) }
      final storeThreeInProgressAudit = auditFactoryService.single(storeThree, authenticatedEmployee, [AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set).with { new AuditValueObject(it, locale, localizationService) }

      when:
      def openedResult = get("$path" + new AuditPageRequest([page: 1, size: 5, sortBy: 'id', status: 'OPENED']))

      then:
      notThrown(HttpClientResponseException)
      openedResult.elements != null
      openedResult.elements.size() == 2
      openedResult.elements.collect { it.id } == [storeOneOpenAuditOne.id, storeThreeOpenAuditOne.id]

      when:
      def inProgressResult = get("$path" + new AuditPageRequest([page: 1, size: 5, sortBy: 'id', storeNumber: 3, status: 'IN-PROGRESS']))

      then:
      notThrown(HttpClientResponseException)
      inProgressResult.elements != null
      inProgressResult.elements.size() == 1
      inProgressResult.elements.collect { it.id } == [storeThreeInProgressAudit.id]
   }

   void "fetch all when each audit has multiple statuses" () {
      given:
      final storeOne = storeFactoryService.store(1)
      final sixAudits = auditFactoryService.stream(6, storeOne, authenticatedEmployee, [AuditStatusFactory.opened(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set).map { new AuditValueObject(it, locale, localizationService) }.toList()

      when:
      def firstFiveAudits = get("${path}${new PageRequest([page: 1, size: 5, sortBy: 'id'])}")

      then:
      notThrown(HttpClientResponseException)
      firstFiveAudits.elements != null
      firstFiveAudits.elements.size() == 5
      firstFiveAudits.totalElements == 6
      firstFiveAudits.totalPages == 2
   }

   void "create new audit" () {
      when:
      def result = post("/$path", new AuditCreateValueObject())

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.id > 0
      result.store.storeNumber == authenticatedEmployee.store.number
      result.actions.size() == 1
      result.actions[0].status.value == "OPENED"
      result.actions[0].status.description == "Opened"
      result.actions[0].changedBy.number == authenticatedEmployee.number
   }

   void "create new audit with invalid store" () {
      when:
      post("/$path", new AuditCreateValueObject([store:  new StoreValueObject([number: 13])]))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "storeNumber"
      response[0].message == "13 was unable to be found"
   }

   void "create new audit on store with already open audit" () {

      given:
      final store = storeFactoryService.store(1)
      final existingAudit = auditFactoryService.single(store)
      final newAudit = AuditFactory.single(store).with { new AuditCreateValueObject([store: new StoreValueObject(it.store)]) }

      when:
      post("/$path", newAudit)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response.collect { new ErrorValueObject(it) } == [new ErrorValueObject("Store ${store.number} has an audit already in progress", "storeNumber")]
   }

   void "update opened audit to in progress" () {
      given:
      final store = storeFactoryService.store(1)
      final audit = auditFactoryService.single(store)

      when:
      def result = put("/$path", new AuditUpdateValueObject(['id': audit.id, 'status': new AuditStatusValueObject([value: 'IN-PROGRESS'])]))

      then:
      notThrown(HttpClientResponseException)
      result.id == audit.id
      result.store.storeNumber == store.number
      result.actions.size() == 2
      def resultActions = result.actions
         .each{ it['timeCreated'] = OffsetDateTime.parse(it['timeCreated']) }
         .each{ it['timeUpdated'] = OffsetDateTime.parse(it['timeUpdated']) }
         .collect{ new AuditActionValueObject(it) }
         .sort { o1, o2 -> o1.id <=> o2.id }
      resultActions[0].id != null
      resultActions[0].id > 0
      resultActions[0].status.value == "OPENED"
      resultActions[0].status.description == "Opened"
      resultActions[0].changedBy.number == audit.actions[0].changedBy.number
      resultActions[1].id != null
      resultActions[1].id > 0
      resultActions[1].id > resultActions[0].id
      resultActions[1].status.value == "IN-PROGRESS"
      resultActions[1].status.description == "In Progress"
      resultActions[1].changedBy.number == authenticatedEmployee.number
   }

   void "update opened audit to canceled" () {
      given:
      final audit = auditFactoryService.single()

      when:
      def result = put("/$path", new AuditUpdateValueObject([id: audit.id, status: new AuditStatusValueObject([value: "CANCELED"])]))

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.id == audit.id
      result.store.storeNumber == audit.store.number
      result.actions.size() == 2
      def resultActions = result.actions
         .each{ it['timeCreated'] = OffsetDateTime.parse(it['timeCreated']) }
         .each{ it['timeUpdated'] = OffsetDateTime.parse(it['timeUpdated']) }
         .collect{ new AuditActionValueObject(it) }
         .sort { o1, o2 -> o1.id <=> o2.id }
      resultActions[0].id != null
      resultActions[0].id > 0
      resultActions[0].status.value == "OPENED"
      resultActions[0].status.description == "Opened"
      resultActions[0].changedBy.number == audit.actions[0].changedBy.number
      resultActions[1].id != null
      resultActions[1].id > 0
      resultActions[1].id > resultActions[0].id
      resultActions[1].status.value == "CANCELED"
      resultActions[1].status.description == "Canceled"
      resultActions[1].changedBy.number == authenticatedEmployee.number
   }

   void "update opened to completed" () {
      given:
      final audit = auditFactoryService.single()

      when:
      put("/$path", new AuditUpdateValueObject([id: audit.id, status:  new AuditStatusValueObject([value: "COMPLETED"])]))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "status"
      response[0].message == "Audit ${String.format("%,d", audit.id)} cannot be changed from Opened to Completed"
   }

   void "update in progress to opened" () {
      given:
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set)

      when:
      put("/$path", new AuditUpdateValueObject([id: audit.id, status: new AuditStatusValueObject([value: "OPENED"])]))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "status"
      response[0].message == "Audit ${String.format("%,d", audit.id)} cannot be changed from In Progress to Opened"
   }

   void "update completed to opened" () {
      given:
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)

      when:
      put("/$path", new AuditUpdateValueObject([id: audit.id, status:  new AuditStatusValueObject([value: "OPENED"])]))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "status"
      response[0].message == "Audit ${String.format("%,d", audit.id)} cannot be changed from Completed to Opened"
   }

   void "update opened to null status" () {
      given:
      final audit = auditFactoryService.single([AuditStatusFactory.opened()] as Set)

      when:
      client.exchange(
         PUT("/${path}", """{ "id": ${audit.id}, "status": { "value": null } }""").header("Authorization", "Bearer $cynergiAccessToken"),
         Argument.of(String),
         Argument.of(String)
      )

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
   }

   void "update audit without an id" () {
      when:
      put("/$path", new AuditUpdateValueObject([status : new AuditStatusValueObject([value: "IN-PROGRESS"])]))

      then:
      final def exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "audit.id"
      response[0].message == "Is required"
   }

   void "update audit with a non-existent id" () {
      given:
      final def savedAudit = auditFactoryService.single()
      final def missingId = savedAudit.id * 100

      when:
      put("/$path", new AuditUpdateValueObject([id: missingId, status : new AuditStatusValueObject([value: "IN-PROGRESS"])]))

      then:
      final def exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "id"
      response[0].message == "${String.format("%,d", missingId)} was unable to be found"
   }

   void "update opened audit to invalid progress" () {
      given:
      final store = storeFactoryService.store(1)
      final audit = auditFactoryService.single(store)

      when:
      put("/$path", new AuditUpdateValueObject(['id': audit.id, 'status': new AuditStatusValueObject(['value': 'INVALID'])]))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "status"
      response[0].message == "Audit status INVALID was unable to be found"
   }

   void "process audit from OPENED to IN-PROGRESS finally to COMPLETED" () {
      when:
      def openedResult = post("/$path", new AuditCreateValueObject([store: new StoreValueObject(number: 3)]))

      then:
      notThrown(HttpClientResponseException)
      openedResult.id != null
      openedResult.id > 0
      openedResult.store.storeNumber == 3
      openedResult.actions.size() == 1
      def openActions = openedResult.actions
         .each{ it['timeCreated'] = OffsetDateTime.parse(it['timeCreated']) }
         .each{ it['timeUpdated'] = OffsetDateTime.parse(it['timeUpdated']) }
         .collect{ new AuditActionValueObject(it) }
         .sort { o1, o2 -> o1.id <=> o2.id }
      openActions[0].id != null
      openActions[0].id > 0
      openActions[0].status.value == "OPENED"
      openActions[0].status.description == "Opened"
      openActions[0].changedBy.number == authenticatedEmployee.number

      when:
      def inProgressResult = put("/$path", new AuditUpdateValueObject([id: openedResult.id, status: new AuditStatusValueObject([value: "IN-PROGRESS"])]))

      then:
      notThrown(HttpClientResponseException)
      inProgressResult.id != null
      inProgressResult.id > 0
      inProgressResult.store.storeNumber == 3
      inProgressResult.actions.size() == 2
      def inProgressActions = inProgressResult.actions
         .each{ it['timeCreated'] = OffsetDateTime.parse(it['timeCreated']) }
         .each{ it['timeUpdated'] = OffsetDateTime.parse(it['timeUpdated']) }
         .collect{ new AuditActionValueObject(it) }
         .sort { o1, o2 -> o1.id <=> o2.id }
      inProgressActions[0].id != null
      inProgressActions[0].id > 0
      inProgressActions[0].status.value == "OPENED"
      inProgressActions[0].status.description == "Opened"
      inProgressActions[0].changedBy.number == authenticatedEmployee.number
      inProgressActions[1].id != null
      inProgressActions[1].id > 0
      inProgressActions[1].id > inProgressActions[0].id
      inProgressActions[1].status.value == "IN-PROGRESS"
      inProgressActions[1].status.description == "In Progress"
      inProgressActions[1].changedBy.number == authenticatedEmployee.number

      when:
      def completedResult = put("/$path", new AuditUpdateValueObject([id: openedResult.id, status: new AuditStatusValueObject([value: "COMPLETED"])]))

      then:
      notThrown(HttpClientResponseException)
      completedResult.id != null
      completedResult.id > 0
      completedResult.store.storeNumber == 3
      completedResult.actions.size() == 3
      def completedActions = completedResult.actions
         .each{ it['timeCreated'] = OffsetDateTime.parse(it['timeCreated']) }
         .each{ it['timeUpdated'] = OffsetDateTime.parse(it['timeUpdated']) }
         .collect{ new AuditActionValueObject(it) }
         .sort { o1, o2 -> o1.id <=> o2.id }
      completedActions[0].id != null
      completedActions[0].id > 0
      completedActions[0].status.value == "OPENED"
      completedActions[0].status.description == "Opened"
      completedActions[0].changedBy.number == authenticatedEmployee.number
      completedActions[1].id != null
      completedActions[1].id > 0
      completedActions[1].id > completedActions[0].id
      completedActions[1].status.value == "IN-PROGRESS"
      completedActions[1].status.description == "In Progress"
      completedActions[1].changedBy.number == authenticatedEmployee.number
      completedActions[2].id != null
      completedActions[2].id > 0
      completedActions[2].id > completedActions[1].id
      completedActions[2].status.value == "COMPLETED"
      completedActions[2].status.description == "Completed"
      completedActions[2].changedBy.number == authenticatedEmployee.number
   }

   void "create new audit after existing open audit was COMPLETED" () {
      given:
      final audit = auditFactoryService.single(authenticatedEmployee.store, authenticatedEmployee, [AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set)

      when:
      put("/$path", new AuditUpdateValueObject([id: audit.id, status:  new AuditStatusValueObject([value: "COMPLETED"])]))

      then:
      notThrown(HttpClientResponseException)
      auditRepository.countAuditsNotCompleted(audit.store.number) == 0

      when:
      def result = post("/$path", new AuditCreateValueObject())

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.id > audit.id
      result.store.storeNumber == authenticatedEmployee.store.number
      result.actions.size() == 1
      result.actions[0].status.value == "OPENED"
      result.actions[0].status.description == "Opened"
      result.actions[0].changedBy.number == authenticatedEmployee.number
   }
}
