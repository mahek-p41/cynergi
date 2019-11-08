package com.cynergisuite.middleware.audit.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.audit.AuditCreateValueObject
import com.cynergisuite.middleware.audit.AuditFactory
import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.AuditStatusCountDataTransferObject
import com.cynergisuite.middleware.audit.AuditUpdateValueObject
import com.cynergisuite.middleware.audit.AuditValueObject
import com.cynergisuite.middleware.audit.action.AuditActionValueObject
import com.cynergisuite.middleware.audit.detail.AuditDetailFactoryService
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaFactoryService
import com.cynergisuite.middleware.audit.exception.AuditExceptionFactoryService
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.audit.status.AuditStatusValueObject
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.error.ErrorDataTransferObject
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.store.StoreFactoryService
import com.cynergisuite.middleware.store.StoreValueObject
import io.micronaut.core.type.Argument
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

import javax.inject.Inject
import java.time.OffsetDateTime

import static com.cynergisuite.extensions.OffsetDateTimeExtensionsKt.beginningOfWeek
import static com.cynergisuite.extensions.OffsetDateTimeExtensionsKt.endOfWeek
import static io.micronaut.http.HttpRequest.PUT
import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class AuditControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/audit"
   private static final Locale locale = Locale.US

   @Inject AuditDetailFactoryService auditDetailFactoryService
   @Inject AuditExceptionFactoryService auditExceptionFactoryService
   @Inject AuditFactoryService auditFactoryService
   @Inject AuditRepository auditRepository
   @Inject AuditScanAreaFactoryService auditScanAreaFactoryService
   @Inject EmployeeFactoryService employeeFactoryService
   @Inject NamedParameterJdbcTemplate jdbc
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
      result.currentStatus.value == 'CREATED'
      result.store.storeNumber == store.number
      result.store.name == store.name
      result.store.dataset == store.dataset
      result.actions.size() == 1
      result.actions[0].id > 0
      result.actions[0].status.value == 'CREATED'
      result.actions[0].status.description == 'Created'
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
      final statuses = AuditStatusFactory.values().collect { it.value }
      final twentyAudits = auditFactoryService.stream(20, store).collect { new AuditValueObject(it, locale, localizationService) }
      final pageOne = new AuditPageRequest([page: 1, size:  5, sortBy:  "id", sortDirection: "ASC", storeNumber:  store.number, status: statuses])
      final pageTwo = new AuditPageRequest([page:  2, size:  5, sortBy:  "id", sortDirection:  "ASC", storeNumber: store.number, status: statuses])
      final pageFive = new AuditPageRequest([page:  5, size:  5, sortBy:  "id", sortDirection:  "ASC", storeNumber: store.number, status: statuses])
      final firstFiveAudits = twentyAudits[0..4]
      final secondFiveAudits = twentyAudits[5..9]

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
      pageOneResult.elements[0].actions[0].status.value == "CREATED"
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
      pageTwoResult.elements[0].actions[0].status.value == "CREATED"
      pageTwoResult.elements[0].actions[0].status.color == "FF0000"
      pageTwoResult.elements[0].id == secondFiveAudits[0].id

      when:
      get("${path}${pageFive}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NOT_FOUND
      final notFoundResult = notFoundException.response.bodyAsJson()
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
      def storeOneFilterResult = get(path + new AuditPageRequest([page: 1, size: 5, sortBy: 'id', storeNumber: 1]))

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
      def storeThreeFilterResult = get(path + new AuditPageRequest([page: 1, size: 5, sortBy: 'id', storeNumber: 3]))

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
      final storeOneOpenAuditOne = auditFactoryService.single(storeOne, authenticatedEmployee, [AuditStatusFactory.created()] as Set).with { new AuditValueObject(it, locale, localizationService) }
      final storeThreeOpenAuditOne = auditFactoryService.single(storeThree, authenticatedEmployee, [AuditStatusFactory.created()] as Set).with { new AuditValueObject(it, locale, localizationService) }
      final storeThreeInProgressAudit = auditFactoryService.single(storeThree, authenticatedEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set).with { new AuditValueObject(it, locale, localizationService) }

      when:
      def openedResult = get(path + new AuditPageRequest([page: 1, size: 5, sortBy: 'id', status: ['CREATED'] as Set]))

      then:
      notThrown(HttpClientResponseException)
      openedResult.elements != null
      openedResult.elements.size() == 2
      openedResult.elements.collect { it.id } == [storeOneOpenAuditOne.id, storeThreeOpenAuditOne.id]

      when:
      def inProgressResult = get(path + new AuditPageRequest([page: 1, size: 5, sortBy: 'id', storeNumber: 3, status: ['IN-PROGRESS'] as Set]))

      then:
      notThrown(HttpClientResponseException)
      inProgressResult.elements != null
      inProgressResult.elements.size() == 1
      inProgressResult.elements.collect { it.id } == [storeThreeInProgressAudit.id]
   }

   void "fetch all open audits from last week" () {
      given:
      final storeOne = storeFactoryService.store(1)
      final storeOneOpenAuditOne = auditFactoryService.single(storeOne, authenticatedEmployee, [AuditStatusFactory.created()] as Set).with { new AuditValueObject(it, locale, localizationService) }

      when:
      def updated = jdbc.update("UPDATE audit set time_created = :time_created WHERE id = :id", [time_created: storeOneOpenAuditOne.timeCreated.minusDays(8), id: storeOneOpenAuditOne.id])
      def openedResult = get(path + new AuditPageRequest([page: 1, size: 5, sortBy: 'id', status: ['CREATED'] as Set]))

      then:
      notThrown(Exception)
      updated == 1 // just a sanity check on the query I just wrote to fudge the db into a state I want
      openedResult.elements != null
      openedResult.elements.collect { it.id } == [storeOneOpenAuditOne.id]
      openedResult.elements[0].timeCreated.with { OffsetDateTime.parse(it) } == storeOneOpenAuditOne.timeCreated.minusDays(8)
   }

   void "fetch in-progress audits from last week" () {
      given:
      final storeOne = storeFactoryService.store(1)
      final storeOneInProgressAuditOne = auditFactoryService.single(storeOne, authenticatedEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set).with { new AuditValueObject(it, locale, localizationService) }

      when:
      def updated = jdbc.update("UPDATE audit set time_created = :time_created WHERE id = :id", [time_created: storeOneInProgressAuditOne.timeCreated.minusDays(8), id: storeOneInProgressAuditOne.id])
      def inProgressResult = get(path + new AuditPageRequest([page: 1, size: 5, sortBy: 'id', status: ['IN-PROGRESS'] as Set]))

      then:
      notThrown(Exception)
      updated == 1 // just a sanity check on the query I just wrote to fudge the db into a state I want
      inProgressResult.elements != null
      inProgressResult.elements.collect { it.id } == [storeOneInProgressAuditOne.id]
      inProgressResult.elements[0].timeCreated.with { OffsetDateTime.parse(it) } == storeOneInProgressAuditOne.timeCreated.minusDays(8)
   }

   void "fetch all opened audits with from thru" () {
      setup:
      final storeOne = storeFactoryService.store(1)
      final storeThree = storeFactoryService.store(3)
      final storeOneEmployee = employeeFactoryService.single(storeOne)
      final storeThreeEmployee = employeeFactoryService.single(storeThree)
      final warehouse = auditScanAreaFactoryService.warehouse()
      final showroom = auditScanAreaFactoryService.showroom()
      final storeroom = auditScanAreaFactoryService.storeroom()

      // setup store one open audit
      final openStoreOneAudit = auditFactoryService.single(storeOne, storeOneEmployee)
      auditDetailFactoryService.generate(11, openStoreOneAudit, storeOneEmployee, warehouse)
      auditDetailFactoryService.generate(5, openStoreOneAudit, storeOneEmployee, showroom)
      auditDetailFactoryService.generate(5, openStoreOneAudit, storeOneEmployee, storeroom)
      auditExceptionFactoryService.generate(25, openStoreOneAudit, storeOneEmployee, null)

      // setup store three open audit
      final openStoreThreeAudit = auditFactoryService.single(storeThree, storeThreeEmployee)
      auditDetailFactoryService.generate(9, openStoreThreeAudit, storeThreeEmployee, warehouse)
      auditDetailFactoryService.generate(5, openStoreThreeAudit, storeThreeEmployee, showroom)
      auditDetailFactoryService.generate(5, openStoreThreeAudit, storeThreeEmployee, storeroom)
      auditExceptionFactoryService.generate(26, openStoreThreeAudit, storeThreeEmployee, null)

      // setup store one canceled audit
      auditFactoryService.single(storeOne, storeOneEmployee, [AuditStatusFactory.created(), AuditStatusFactory.canceled()] as Set)

      // setup store three canceled audit
      auditFactoryService.single(storeThree, storeThreeEmployee, [AuditStatusFactory.created(), AuditStatusFactory.canceled()] as Set)

      // setup store one completed off audits
      auditFactoryService.generate(3, storeOne, storeOneEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)

      // setup store three completed off audits
      auditFactoryService.generate(4, storeThree, storeThreeEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)

      // setup store one signed off audits
      auditFactoryService.generate(3, storeOne, storeOneEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed(), AuditStatusFactory.signedOff()] as Set)

      // setup store three signed off audits
      auditFactoryService.generate(4, storeThree, storeThreeEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed(), AuditStatusFactory.signedOff()] as Set)

      when:
      def twoCreatedAudits = get(path + new AuditPageRequest([page: 1, size: 5, sortBy: 'id', from: beginningOfWeek(OffsetDateTime.now()), thru: endOfWeek(OffsetDateTime.now()), status: [AuditStatusFactory.created().value] as Set]))

      then:
      notThrown(HttpClientResponseException)
      twoCreatedAudits.elements != null
      twoCreatedAudits.elements.size() == 2
      twoCreatedAudits.totalElements == 2
      twoCreatedAudits.totalPages == 1
      twoCreatedAudits.first == true
      twoCreatedAudits.last == true
   }

   void "fetch all when each audit has multiple statuses" () {
      given:
      final storeOne = storeFactoryService.store(1)
      auditFactoryService.generate(6, storeOne, authenticatedEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)

      when:
      def firstFiveAudits = get(path + new PageRequest([page: 1, size: 5, sortBy: 'id']))

      then:
      notThrown(HttpClientResponseException)
      firstFiveAudits.elements != null
      firstFiveAudits.elements.size() == 5
      firstFiveAudits.totalElements == 6
      firstFiveAudits.totalPages == 2
   }

   void "fetch audit status counts using defaults for request parameters" () {
      setup:
      auditFactoryService.generate(1, null, authenticatedEmployee, [AuditStatusFactory.created()] as Set)
      auditFactoryService.generate(2, null, authenticatedEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      auditFactoryService.generate(3, null, authenticatedEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.canceled()] as Set)
      auditFactoryService.generate(4, null, authenticatedEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      auditFactoryService.generate(5, null, authenticatedEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed(), AuditStatusFactory.signedOff()] as Set)

      when:
      def counts = get("${path}/counts").collect { new AuditStatusCountDataTransferObject(it) }.sort { o1, o2 -> o1.getStatus().id <=> o2.getStatus().id }

      then:
      notThrown(HttpClientResponseException)
      counts.size() == 5
      counts == [
         new AuditStatusCountDataTransferObject(1, new AuditStatusValueObject(AuditStatusFactory.created())),
         new AuditStatusCountDataTransferObject(2, new AuditStatusValueObject(AuditStatusFactory.inProgress())),
         new AuditStatusCountDataTransferObject(4, new AuditStatusValueObject(AuditStatusFactory.completed())),
         new AuditStatusCountDataTransferObject(3, new AuditStatusValueObject(AuditStatusFactory.canceled())),
         new AuditStatusCountDataTransferObject(5, new AuditStatusValueObject(AuditStatusFactory.signedOff()))
      ]
   }

   void "fetch audit status counts using specified from" () {
      setup:
      final def from = OffsetDateTime.now().minusDays(1)
      auditFactoryService.generate(1, null, authenticatedEmployee, [AuditStatusFactory.created()] as Set)
      auditFactoryService.generate(2, null, authenticatedEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      auditFactoryService.generate(3, null, authenticatedEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.canceled()] as Set)
      auditFactoryService.generate(4, null, authenticatedEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      auditFactoryService.generate(5, null, authenticatedEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed(), AuditStatusFactory.signedOff()] as Set)

      when:
      def counts = get("${path}/counts?from=${from}").collect { new AuditStatusCountDataTransferObject(it) }.sort { o1, o2 -> o1.getStatus().id <=> o2.getStatus().id }

      then:
      notThrown(HttpClientResponseException)
      counts.size() == 5
      counts == [
         new AuditStatusCountDataTransferObject(1, new AuditStatusValueObject(AuditStatusFactory.created())),
         new AuditStatusCountDataTransferObject(2, new AuditStatusValueObject(AuditStatusFactory.inProgress())),
         new AuditStatusCountDataTransferObject(4, new AuditStatusValueObject(AuditStatusFactory.completed())),
         new AuditStatusCountDataTransferObject(3, new AuditStatusValueObject(AuditStatusFactory.canceled())),
         new AuditStatusCountDataTransferObject(5, new AuditStatusValueObject(AuditStatusFactory.signedOff()))
      ]
   }

   void "fetch audit status counts using specified from and statuses" () {
      setup:
      final def from = OffsetDateTime.now().minusDays(1)
      auditFactoryService.generate(1, null, authenticatedEmployee, [AuditStatusFactory.created()] as Set)
      auditFactoryService.generate(2, null, authenticatedEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      auditFactoryService.generate(3, null, authenticatedEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.canceled()] as Set)
      auditFactoryService.generate(4, null, authenticatedEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      auditFactoryService.generate(5, null, authenticatedEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed(), AuditStatusFactory.signedOff()] as Set)

      when:
      def counts = get("${path}/counts?from=${from}&status=CREATED&status=IN-PROGRESS").collect { new AuditStatusCountDataTransferObject(it) }.sort { o1, o2 -> o1.getStatus().id <=> o2.getStatus().id }

      then:
      notThrown(HttpClientResponseException)
      counts.size() == 2
      counts == [
         new AuditStatusCountDataTransferObject(1, new AuditStatusValueObject(AuditStatusFactory.created())),
         new AuditStatusCountDataTransferObject(2, new AuditStatusValueObject(AuditStatusFactory.inProgress())),
      ]
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
      result.actions[0].status.value == "CREATED"
      result.actions[0].status.description == "Created"
      result.actions[0].changedBy.number == authenticatedEmployee.number
   }

   void "create new audit with invalid store" () {
      when:
      post(path, new AuditCreateValueObject([store:  new StoreValueObject([number: 13])]))

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
      auditFactoryService.single(store)
      final newAudit = AuditFactory.single(store).with { new AuditCreateValueObject([store: new StoreValueObject(it.store)]) }

      when:
      post(path, newAudit)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response.collect { new ErrorDataTransferObject(it) } == [new ErrorDataTransferObject("Store ${store.number} has an audit already in progress", "storeNumber")]
   }

   void "update opened audit to in progress" () {
      given:
      final store = storeFactoryService.store(1)
      final audit = auditFactoryService.single(store)

      when:
      def result = put(path, new AuditUpdateValueObject(['id': audit.id, 'status': new AuditStatusValueObject([value: 'IN-PROGRESS'])]))

      then:
      notThrown(HttpClientResponseException)
      result.id == audit.id
      result.store.storeNumber == store.number
      result.actions.size() == 2
      final resultActions = result.actions
         .each{ it['timeCreated'] = OffsetDateTime.parse(it['timeCreated']) }
         .each{ it['timeUpdated'] = OffsetDateTime.parse(it['timeUpdated']) }
         .collect{ new AuditActionValueObject(it) }
         .sort { o1, o2 -> o1.id <=> o2.id }
      resultActions[0].id != null
      resultActions[0].id > 0
      resultActions[0].status.value == "CREATED"
      resultActions[0].status.description == "Created"
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
      def result = put(path, new AuditUpdateValueObject([id: audit.id, status: new AuditStatusValueObject([value: "CANCELED"])]))

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.id == audit.id
      result.store.storeNumber == audit.store.number
      result.actions.size() == 2
      final resultActions = result.actions
         .each{ it['timeCreated'] = OffsetDateTime.parse(it['timeCreated']) }
         .each{ it['timeUpdated'] = OffsetDateTime.parse(it['timeUpdated']) }
         .collect{ new AuditActionValueObject(it) }
         .sort { o1, o2 -> o1.id <=> o2.id }
      resultActions[0].id != null
      resultActions[0].id > 0
      resultActions[0].status.value == "CREATED"
      resultActions[0].status.description == "Created"
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
      put(path, new AuditUpdateValueObject([id: audit.id, status:  new AuditStatusValueObject([value: "COMPLETED"])]))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "status"
      response[0].message == "Audit ${String.format("%,d", audit.id)} cannot be changed from Created to Completed"
   }

   void "update in progress to opened" () {
      given:
      final audit = auditFactoryService.single([AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)

      when:
      put(path, new AuditUpdateValueObject([id: audit.id, status: new AuditStatusValueObject([value: "CREATED"])]))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "status"
      response[0].message == "Audit ${String.format("%,d", audit.id)} cannot be changed from In Progress to Created"
   }

   void "update completed to opened" () {
      given:
      final audit = auditFactoryService.single([AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)

      when:
      put(path, new AuditUpdateValueObject([id: audit.id, status:  new AuditStatusValueObject([value: "CREATED"])]))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "status"
      response[0].message == "Audit ${String.format("%,d", audit.id)} cannot be changed from Completed to Created"
   }

   void "update opened to null status" () {
      given:
      final audit = auditFactoryService.single([AuditStatusFactory.created()] as Set)

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
      put(path, new AuditUpdateValueObject([status : new AuditStatusValueObject([value: "IN-PROGRESS"])]))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "audit.id"
      response[0].message == "Is required"
   }

   void "update audit with a non-existent id" () {
      given:
      final savedAudit = auditFactoryService.single()
      final missingId = savedAudit.id * 100

      when:
      put(path, new AuditUpdateValueObject([id: missingId, status : new AuditStatusValueObject([value: "IN-PROGRESS"])]))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "id"
      response[0].message == "${String.format("%,d", missingId)} was unable to be found"
   }

   void "update opened audit to invalid progress" () {
      given:
      final store = storeFactoryService.store(1)
      final audit = auditFactoryService.single(store)

      when:
      put(path, new AuditUpdateValueObject(['id': audit.id, 'status': new AuditStatusValueObject(['value': 'INVALID'])]))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "status"
      response[0].message == "Audit status INVALID was unable to be found"
   }

   void "process audit from CREATED to IN-PROGRESS finally to COMPLETED" () {
      when:
      def openedResult = post("/$path", new AuditCreateValueObject([store: new StoreValueObject(number: 3)]))

      then:
      notThrown(HttpClientResponseException)
      openedResult.id != null
      openedResult.id > 0
      openedResult.store.storeNumber == 3
      openedResult.actions.size() == 1
      final openActions = openedResult.actions
         .each{ it['timeCreated'] = OffsetDateTime.parse(it['timeCreated']) }
         .each{ it['timeUpdated'] = OffsetDateTime.parse(it['timeUpdated']) }
         .collect{ new AuditActionValueObject(it) }
         .sort { o1, o2 -> o1.id <=> o2.id }
      openActions[0].id != null
      openActions[0].id > 0
      openActions[0].status.value == "CREATED"
      openActions[0].status.description == "Created"
      openActions[0].changedBy.number == authenticatedEmployee.number

      when:
      def inProgressResult = put(path, new AuditUpdateValueObject([id: openedResult.id, status: new AuditStatusValueObject([value: "IN-PROGRESS"])]))

      then:
      notThrown(HttpClientResponseException)
      inProgressResult.id != null
      inProgressResult.id > 0
      inProgressResult.store.storeNumber == 3
      inProgressResult.actions.size() == 2
      final inProgressActions = inProgressResult.actions
         .each{ it['timeCreated'] = OffsetDateTime.parse(it['timeCreated']) }
         .each{ it['timeUpdated'] = OffsetDateTime.parse(it['timeUpdated']) }
         .collect{ new AuditActionValueObject(it) }
         .sort { o1, o2 -> o1.id <=> o2.id }
      inProgressActions[0].id != null
      inProgressActions[0].id > 0
      inProgressActions[0].status.value == "CREATED"
      inProgressActions[0].status.description == "Created"
      inProgressActions[0].changedBy.number == authenticatedEmployee.number
      inProgressActions[1].id != null
      inProgressActions[1].id > 0
      inProgressActions[1].id > inProgressActions[0].id
      inProgressActions[1].status.value == "IN-PROGRESS"
      inProgressActions[1].status.description == "In Progress"
      inProgressActions[1].changedBy.number == authenticatedEmployee.number

      when:
      def completedResult = put(path, new AuditUpdateValueObject([id: openedResult.id, status: new AuditStatusValueObject([value: "COMPLETED"])]))

      then:
      notThrown(HttpClientResponseException)
      completedResult.id != null
      completedResult.id > 0
      completedResult.store.storeNumber == 3
      completedResult.actions.size() == 3
      final completedActions = completedResult.actions
         .each{ it['timeCreated'] = OffsetDateTime.parse(it['timeCreated']) }
         .each{ it['timeUpdated'] = OffsetDateTime.parse(it['timeUpdated']) }
         .collect{ new AuditActionValueObject(it) }
         .sort { o1, o2 -> o1.id <=> o2.id }
      completedActions[0].id != null
      completedActions[0].id > 0
      completedActions[0].status.value == "CREATED"
      completedActions[0].status.description == "Created"
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
      final audit = auditFactoryService.single(authenticatedEmployee.store, authenticatedEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)

      when:
      put(path, new AuditUpdateValueObject([id: audit.id, status:  new AuditStatusValueObject([value: "COMPLETED"])]))

      then:
      notThrown(HttpClientResponseException)
      auditRepository.countAuditsNotCompleted(audit.store.number) == 0

      when:
      def result = post(path, new AuditCreateValueObject())

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.id > audit.id
      result.store.storeNumber == authenticatedEmployee.store.number
      result.actions.size() == 1
      result.actions[0].status.value == "CREATED"
      result.actions[0].status.description == "Created"
      result.actions[0].changedBy.number == authenticatedEmployee.number
   }
}
