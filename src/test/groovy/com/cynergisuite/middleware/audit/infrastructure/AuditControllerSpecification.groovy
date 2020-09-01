package com.cynergisuite.middleware.audit.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDataTransferObject
import com.cynergisuite.domain.SimpleIdentifiableValueObject
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.audit.AuditCreateValueObject
import com.cynergisuite.middleware.audit.AuditFactory
import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.AuditStatusCountDataTransferObject
import com.cynergisuite.middleware.audit.AuditUpdateValueObject
import com.cynergisuite.middleware.audit.AuditValueObject
import com.cynergisuite.middleware.audit.action.AuditActionValueObject
import com.cynergisuite.middleware.audit.detail.AuditDetailEntity
import com.cynergisuite.middleware.audit.detail.AuditDetailFactoryService
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaFactoryService
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaDTO
import com.cynergisuite.middleware.audit.exception.AuditExceptionEntity
import com.cynergisuite.middleware.audit.exception.AuditExceptionFactoryService
import com.cynergisuite.middleware.audit.exception.AuditExceptionValueObject
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNoteFactoryService
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.audit.status.AuditStatusValueObject
import com.cynergisuite.middleware.audit.status.Created
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.department.DepartmentFactoryService
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.error.ErrorDataTransferObject
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.store.StoreValueObject
import io.micronaut.core.type.Argument
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import spock.lang.Unroll

import javax.inject.Inject
import java.time.OffsetDateTime

import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpRequest.PUT
import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT
import static io.micronaut.http.HttpStatus.OK
import static java.util.Locale.US

@MicronautTest(transactional = false)
class AuditControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/audit"
   private static final Locale locale = US

   @Inject AuditDetailFactoryService auditDetailFactoryService
   @Inject AuditExceptionFactoryService auditExceptionFactoryService
   @Inject AuditExceptionNoteFactoryService auditExceptionNoteFactoryService
   @Inject AuditFactoryService auditFactoryService
   @Inject AuditRepository auditRepository
   @Inject AuditScanAreaFactoryService auditScanAreaFactoryService
   @Inject DepartmentFactoryService departmentFactoryService
   @Inject EmployeeFactoryService employeeFactoryService
   @Inject NamedParameterJdbcTemplate jdbc
   @Inject LocalizationService localizationService

   void "fetch one audit by id" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final savedAudit = auditFactoryService.single(store)

      when:
      def result = get("$path/${savedAudit.id}")

      then:
      notThrown(HttpClientResponseException)
      result.inventoryCount == 260
      result.id == savedAudit.id
      result.timeCreated.with { OffsetDateTime.parse(it) } == savedAudit.timeCreated
      result.lastUpdated == null
      result.currentStatus.value == 'CREATED'
      result.totalDetails == 0
      result.totalExceptions == 0
      result.store.storeNumber == store.number
      result.store.name == store.name
      result.actions.size() == 1
      result.actions[0].id > 0
      result.actions[0].status.value == 'CREATED'
      result.actions[0].status.description == 'Created'
      result.actions[0].changedBy.number == savedAudit.actions[0].changedBy.number
      result.actions[0].timeCreated.with { OffsetDateTime.parse(it) } == savedAudit.actions[0].timeCreated
      result.actions[0].timeUpdated.with { OffsetDateTime.parse(it) } == savedAudit.actions[0].timeUpdated
   }

   void "fetch one audit by id with superfluous URL parameters" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final savedAudit = auditFactoryService.single(store)

      when:
      def result = get("$path/${savedAudit.id}?extraOne=1&extraTwo=two")

      then:
      notThrown(HttpClientResponseException)
      result.inventoryCount == 260
      result.id == savedAudit.id
      result.timeCreated.with { OffsetDateTime.parse(it) } == savedAudit.timeCreated
      result.lastUpdated == null
      result.currentStatus.value == 'CREATED'
      result.totalDetails == 0
      result.totalExceptions == 0
      result.store.storeNumber == store.number
      result.store.name == store.name
      result.actions.size() == 1
      result.actions[0].id > 0
      result.actions[0].status.value == 'CREATED'
      result.actions[0].status.description == 'Created'
      result.actions[0].changedBy.number == savedAudit.actions[0].changedBy.number
      result.actions[0].timeCreated.with { OffsetDateTime.parse(it) } == savedAudit.actions[0].timeCreated
      result.actions[0].timeUpdated.with { OffsetDateTime.parse(it) } == savedAudit.actions[0].timeUpdated
   }

   void "fetch one audit by id that has associated exceptions" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(1, company)
      final savedAudit = auditFactoryService.single(store)
      final storeroom = auditScanAreaFactoryService.storeroom(store, company)
      final List<AuditExceptionEntity> auditExceptions = auditExceptionFactoryService.stream(20, savedAudit, storeroom).toList()

      when:
      def result = get("$path/${savedAudit.id}")

      then:
      notThrown(HttpClientResponseException)
      result.id == savedAudit.id
      result.inventoryCount == 423
      result.timeCreated.with { OffsetDateTime.parse(it) } == savedAudit.timeCreated
      result.lastUpdated != null
      result.currentStatus.value == 'CREATED'
      result.totalDetails == 0
      result.totalExceptions == 20
      result.hasExceptionNotes == false
      result.store.storeNumber == store.number
      result.store.name == store.name
      result.actions.size() == 1
      result.actions[0].id > 0
      result.actions[0].status.value == 'CREATED'
      result.actions[0].status.description == 'Created'
      result.actions[0].changedBy.number == savedAudit.actions[0].changedBy.number
      result.actions[0].timeCreated.with { OffsetDateTime.parse(it) } == savedAudit.actions[0].timeCreated
      result.actions[0].timeUpdated.with { OffsetDateTime.parse(it) } == savedAudit.actions[0].timeUpdated
      result.lastUpdated != null
      result.lastUpdated.with { OffsetDateTime.parse(it) } == auditExceptions.last().timeUpdated
   }

   void "fetch one audit by id that has associated exceptions and notes" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(1, company)
      final scanningEmployee = employeeFactoryService.single(store)
      final savedAudit = auditFactoryService.single(store)
      final storeroom = auditScanAreaFactoryService.storeroom(store, company)
      final exceptionsWithoutNotes = auditExceptionFactoryService.stream(19, savedAudit, storeroom).toList()
      final exceptionWithNotes = auditExceptionFactoryService.single(savedAudit, storeroom, scanningEmployee, false)
      final exceptionNotes = auditExceptionNoteFactoryService.stream(2, exceptionWithNotes, scanningEmployee).toList()

      when:
      def result = get("$path/${savedAudit.id}")

      then:
      notThrown(HttpClientResponseException)
      result.id == savedAudit.id
      result.inventoryCount == 423
      result.timeCreated.with { OffsetDateTime.parse(it) } == savedAudit.timeCreated
      result.lastUpdated != null
      result.currentStatus.value == 'CREATED'
      result.totalDetails == 0
      result.totalExceptions == 20
      result.hasExceptionNotes == true
      result.store.storeNumber == store.number
      result.store.name == store.name
      result.actions.size() == 1
      result.actions[0].id > 0
      result.actions[0].status.value == 'CREATED'
      result.actions[0].status.description == 'Created'
      result.actions[0].changedBy.number == savedAudit.actions[0].changedBy.number
      result.actions[0].timeCreated.with { OffsetDateTime.parse(it) } == savedAudit.actions[0].timeCreated
      result.actions[0].timeUpdated.with { OffsetDateTime.parse(it) } == savedAudit.actions[0].timeUpdated
   }

   void "fetch one audit by id that has associated details" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.random(company)
      final savedAudit = auditFactoryService.single(store)
      final storeroom = auditScanAreaFactoryService.storeroom(store, company)
      final List<AuditDetailEntity> auditDetails = auditDetailFactoryService.stream(20, savedAudit, storeroom).toList()

      when:
      def result = get("$path/${savedAudit.id}")

      then:
      notThrown(HttpClientResponseException)
      result.inventoryCount > 0
      result.inventoryCount == savedAudit.inventoryCount
      result.totalDetails == 20
      result.totalExceptions == 0
      result.lastUpdated != null
      result.lastUpdated.with { OffsetDateTime.parse(it) } == auditDetails.last().timeUpdated
   }

   void "fetch one audit by id that has associated details and exceptions" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.random(company)
      final savedAudit = auditFactoryService.single(store)
      final storeroom = auditScanAreaFactoryService.storeroom(store, company)
      final List<AuditDetailEntity> auditDetails = auditDetailFactoryService.stream(20, savedAudit, storeroom).toList()
      final List<AuditExceptionEntity> auditExceptions = auditExceptionFactoryService.stream(20, savedAudit, storeroom).toList()

      when:
      def result = get("$path/${savedAudit.id}")

      then:
      notThrown(HttpClientResponseException)
      result.totalDetails == 20
      result.totalExceptions == 20
      result.lastUpdated != null
      result.lastUpdated.with { OffsetDateTime.parse(it) } == auditExceptions.last().timeUpdated
   }

   void "fetch one audit by id not found" () {
      when:
      get("$path/0")

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "0 was unable to be found"
   }

   void "fetch all audits for store 1" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(1, company)
      final storeNumbers = [store.id]
      final statuses = AuditStatusFactory.values().collect { it.value }
      final twentyAudits = auditFactoryService.stream(20, store).collect { new AuditValueObject(it, locale, localizationService) }
      final pageOne = new AuditPageRequest([page: 1, size:  5, sortBy:  "id", sortDirection: "ASC", storeNumber:  storeNumbers, status: statuses])
      final pageTwo = new AuditPageRequest([page:  2, size:  5, sortBy:  "id", sortDirection:  "ASC", storeNumber: storeNumbers, status: statuses])
      final pageFive = new AuditPageRequest([page:  5, size:  5, sortBy:  "id", sortDirection:  "ASC", storeNumber: storeNumbers, status: statuses])
      final firstFiveAudits = twentyAudits[0..4]
      final secondFiveAudits = twentyAudits[5..9]

      when:
      def pageOneResult = get("${path}${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.requested.storeNumber == storeNumbers
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.elements[0].id == firstFiveAudits[0].id
      pageOneResult.elements[0].inventoryCount > 0
      pageOneResult.elements[0].inventoryCount == firstFiveAudits[0].inventoryCount
      pageOneResult.elements[0].store.id == store.id
      pageOneResult.elements[0].actions.size() == 1
      pageOneResult.elements[0].actions[0].status.value == Created.INSTANCE.value
      pageOneResult.elements[0].actions[0].status.color == Created.INSTANCE.color
      pageOneResult.elements[0].actions[0].id == firstFiveAudits[0].actions[0].id

      when:
      def pageTwoResult = get("${path}${pageTwo}")

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.requested.storeNumber == storeNumbers
      pageTwoResult.elements != null
      pageTwoResult.elements.size() == 5
      pageTwoResult.elements[0].id == secondFiveAudits[0].id
      pageTwoResult.elements[0].inventoryCount > 0
      pageTwoResult.elements[0].inventoryCount == secondFiveAudits[0].inventoryCount
      pageTwoResult.elements[0].store.id == store.id
      pageTwoResult.elements[0].actions.size() == 1
      pageTwoResult.elements[0].actions[0].status.value == Created.INSTANCE.value
      pageTwoResult.elements[0].actions[0].status.color == Created.INSTANCE.color
      pageTwoResult.elements[0].actions[0].id == secondFiveAudits[0].actions[0].id

      when:
      get("${path}${pageFive}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "fetch all audits by store" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final storeThree = storeFactoryService.store(3, company)
      final fiveAuditsStoreOne = auditFactoryService.stream(5, storeOne).collect { new AuditValueObject(it, locale, localizationService) }
      final tenAuditsStoreThree = auditFactoryService.stream(10, storeThree).collect { new AuditValueObject(it, locale, localizationService) }

      when:
      def storeOneFilterResult = get(path + new AuditPageRequest([page: 1, size: 5, sortBy: 'id', storeNumber: [1]]))

      then:
      notThrown(HttpClientResponseException)
      storeOneFilterResult.requested.storeNumber == [storeOne.number]
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
      def storeThreeFilterResult = get(path + new AuditPageRequest([page: 1, size: 5, sortBy: 'id', storeNumber: [3]]))

      then:
      notThrown(HttpClientResponseException)
      storeThreeFilterResult.elements != null
      storeThreeFilterResult.elements.size() == 5
      storeThreeFilterResult.requested.storeNumber == [storeThree.number]
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

   void "fetch all audits based on login with alt store indicator of 'N'" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final salesAssociate = departmentFactoryService.department('SA', company)
      final storeOne = storeFactoryService.store(1, company)
      final storeThree = storeFactoryService.store(3, company)
      final employeeStoreOneAltStoreN = employeeFactoryService.singleAuthenticated(company, storeOne, salesAssociate, 'N', 0) //should only be able to access audits for the store they are assigned
      final employeeStoreThreeAltStoreN = employeeFactoryService.singleAuthenticated(company, storeThree, salesAssociate, 'N', 0) //should only be able to access audits for the store they are assigned
      final employeeStoreOneAltStoreNAuth = loginEmployee(employeeStoreOneAltStoreN)
      final employeeStoreThreeAltStoreNAuth = loginEmployee(employeeStoreThreeAltStoreN)
      final storeOneAudit = auditFactoryService.single(storeOne)
      final storeThreeAudit = auditFactoryService.single(storeThree)

      when:
      def audits = get(path, employeeStoreOneAltStoreNAuth)

      then:
      notThrown(Exception)
      audits.elements != null
      audits.elements.size() == 1
      audits.elements[0].store.storeNumber == storeOne.number
      audits.elements[0].id == storeOneAudit.id

      when:
      audits = get(path, employeeStoreThreeAltStoreNAuth)

      then:
      notThrown(Exception)
      audits.elements != null
      audits.elements.size() == 1
      audits.elements[0].store.storeNumber == storeThree.number
      audits.elements[0].id == storeThreeAudit.id

      when:
      audits = get(path) // use 998 which should see both audits

      then:
      notThrown(Exception)
      audits.elements != null
      audits.elements.size() == 2
      audits.elements[0].store.storeNumber == storeOne.number
      audits.elements[0].id == storeOneAudit.id
      audits.elements[1].store.storeNumber == storeThree.number
      audits.elements[1].id == storeThreeAudit.id
   }

   void "fetch all audits based on login with alt store indicator of 'R'" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final regionalManager = departmentFactoryService.department('RM', company)
      final storeOne = storeFactoryService.store(1, company)
      final storeThree = storeFactoryService.store(3, company)
      final regionalManagerEmployee = employeeFactoryService.singleAuthenticated(company, storeOne, regionalManager, 'R', regions[0].number) //should only be able to access audits for the store they are assigned
      final regionalManagerEmployeeAuth = loginEmployee(regionalManagerEmployee)
      final storeOneAudit = auditFactoryService.single(storeOne)
      final storeThreeAudit = auditFactoryService.single(storeThree)

      when:
      def audits = get(path, regionalManagerEmployeeAuth) // use 998 which should see both audits

      then:
      notThrown(Exception)
      audits.elements != null
      audits.elements.size() == 2
      audits.elements[0].store.storeNumber == storeOne.number
      audits.elements[0].id == storeOneAudit.id
      audits.elements[1].store.storeNumber == storeThree.number
      audits.elements[1].id == storeThreeAudit.id
   }

   void "fetch all audits based on login with alt store indicator of 'D'" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final executive = departmentFactoryService.department('EX', company)
      final storeOne = storeFactoryService.store(1, company)
      final storeThree = storeFactoryService.store(3, company)
      final executiveEmployee = employeeFactoryService.singleAuthenticated(company, storeOne, executive, 'D', divisions[0].number) //should only be able to access audits for the store they are assigned
      final executiveEmployeeAuth = loginEmployee(executiveEmployee)
      final storeOneAudit = auditFactoryService.single(storeOne)
      final storeThreeAudit = auditFactoryService.single(storeThree)

      when:
      def audits = get(path, executiveEmployeeAuth) // use 998 which should see both audits

      then:
      notThrown(Exception)
      audits.elements != null
      audits.elements.size() == 2
      audits.elements[0].store.storeNumber == storeOne.number
      audits.elements[0].id == storeOneAudit.id
      audits.elements[1].store.storeNumber == storeThree.number
      audits.elements[1].id == storeThreeAudit.id
   }

   @Unroll
   void "fetch all audits by store with storeNumber #storeNumberValuesIn" () {
      given:
      final def company = companyFactoryService.forDatasetCode('tstds1')
      final def storeOne = storeFactoryService.store(1, company)
      final def storeThree = storeFactoryService.store(3, company)
      auditFactoryService.stream(5, storeOne).forEach { }
      auditFactoryService.stream(10, storeThree).forEach { }

      when:
      def storeFilterResult = get(path + new AuditPageRequest([page: 1, size: pageSizeIn, sortBy: 'id', storeNumber: storeNumberValuesIn]))

      then:
      storeFilterResult.totalElements == totalElementCount
      storeFilterResult.elements.size() == pageElementCount
      storeFilterResult.requested.storeNumber == requestedStoreNumbers
      storeFilterResult.elements.stream().map{ el -> el.store.storeNumber }.toSet() == storeNumberInElements as Set

      where:
      storeNumberValuesIn | pageSizeIn | pageElementCount | totalElementCount | requestedStoreNumbers | storeNumberInElements
      [1]                 | 5          | 5                | 5                 | [1]                   | [1]
      [3]                 | 5          | 5                | 10                | [3]                   | [3]
      [1, 3]              | 10         | 10               | 15                | [1, 3]                | [1, 3]
      [1, 3, 10]          | 15         | 15               | 15                | [1, 3, 10]            | [1, 3]
   }

   void "fetch all audits store 1 with one audit that has exceptions and notes" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final storeNumbers = [storeOne.myId()]
      final scannedBy = employeeFactoryService.single(storeOne)
      final singleAudit = auditFactoryService.single(storeOne, scannedBy)
      final warehouse = auditScanAreaFactoryService.warehouse(storeOne, company)
      final singleAuditExceptionWithNote = auditExceptionFactoryService.single(singleAudit, warehouse, scannedBy, false)
      final singleNote = auditExceptionNoteFactoryService.single(singleAuditExceptionWithNote, scannedBy)
      final fiveAuditsStoreOne = auditFactoryService.stream(5, storeOne).collect { new AuditValueObject(it, locale, localizationService) }

      when:
      def storeOneFilterResult = get(path + new AuditPageRequest([page: 1, size: 5, sortBy: 'id', storeNumber: [1]]))

      then:
      notThrown(HttpClientResponseException)
      storeOneFilterResult.requested.storeNumber == storeNumbers
      storeOneFilterResult.totalElements == 6
      storeOneFilterResult.totalPages == 2
      storeOneFilterResult.first == true
      storeOneFilterResult.last == false
      storeOneFilterResult.elements != null
      storeOneFilterResult.elements.size() == 5
      storeOneFilterResult.elements[0].id > 0
      storeOneFilterResult.elements[0].store.storeNumber == storeOne.myNumber()
      storeOneFilterResult.elements[0].hasExceptionNotes == true
      storeOneFilterResult.elements[0].actions[0].id == singleAudit.actions[0].id
      storeOneFilterResult.elements[0].actions[0].status.value == singleAudit.actions[0].status.value
      storeOneFilterResult.elements[0].actions[0].status.description == singleAudit.actions[0].status.description
      storeOneFilterResult.elements[0].actions[0].changedBy.number == singleAudit.actions[0].changedBy.number
      storeOneFilterResult.elements[0].actions[0].changedBy.lastName == singleAudit.actions[0].changedBy.lastName
      storeOneFilterResult.elements[0].actions[0].changedBy.firstNameMi == singleAudit.actions[0].changedBy.firstNameMi
      storeOneFilterResult.elements[4].actions[0].id == fiveAuditsStoreOne[3].actions[0].id
      storeOneFilterResult.elements[4].hasExceptionNotes == false
      storeOneFilterResult.elements[4].actions[0].status.value == fiveAuditsStoreOne[3].actions[0].status.value
      storeOneFilterResult.elements[4].actions[0].status.description == fiveAuditsStoreOne[3].actions[0].status.description
      storeOneFilterResult.elements[4].actions[0].changedBy.number == fiveAuditsStoreOne[3].actions[0].changedBy.number
      storeOneFilterResult.elements[4].actions[0].changedBy.lastName == fiveAuditsStoreOne[3].actions[0].changedBy.lastName
      storeOneFilterResult.elements[4].actions[0].changedBy.firstNameMi == fiveAuditsStoreOne[3].actions[0].changedBy.firstNameMi
   }

   void "fetch all by status" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final storeThree = storeFactoryService.store(3, company)
      final employee = employeeFactoryService.single(company)
      final storeOneOpenAuditOne = auditFactoryService.single(storeOne, employee, [AuditStatusFactory.created()] as Set).with { new AuditValueObject(it, locale, localizationService) }
      final storeThreeOpenAuditOne = auditFactoryService.single(storeThree, employee, [AuditStatusFactory.created()] as Set).with { new AuditValueObject(it, locale, localizationService) }
      final storeThreeInProgressAudit = auditFactoryService.single(storeThree, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set).with { new AuditValueObject(it, locale, localizationService) }

      when:
      def openedResult = get(path + new AuditPageRequest([page: 1, size: 5, sortBy: 'id', status: ['CREATED'] as Set]))

      then:
      notThrown(HttpClientResponseException)
      openedResult.elements != null
      openedResult.elements.size() == 2
      openedResult.elements.collect { it.id } == [storeOneOpenAuditOne.id, storeThreeOpenAuditOne.id]

      when:
      def inProgressResult = get(path + new AuditPageRequest([page: 1, size: 5, sortBy: 'id', status: ['IN-PROGRESS'] as Set]))

      then:
      notThrown(HttpClientResponseException)
      inProgressResult.elements != null
      inProgressResult.elements.size() == 1
      inProgressResult.elements.collect { it.id } == [storeThreeInProgressAudit.id]
   }

   void "fetch all open audits from last week" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final employee = employeeFactoryService.single(company)
      final storeOneOpenAuditOne = auditFactoryService.single(storeOne, employee, [AuditStatusFactory.created()] as Set).with { new AuditValueObject(it, locale, localizationService) }

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
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1,company)
      final employee = employeeFactoryService.single(company)
      final storeOneInProgressAuditOne = auditFactoryService.single(storeOne, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set).with { new AuditValueObject(it, locale, localizationService) }

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
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final storeThree = storeFactoryService.store(3, company)
      final storeOneEmployee = employeeFactoryService.single(storeOne)
      final storeThreeEmployee = employeeFactoryService.single(storeThree)
      final storeOneWarehouse = auditScanAreaFactoryService.warehouse(storeOne, company)
      final storeOneShowroom = auditScanAreaFactoryService.showroom(storeOne, company)
      final storeOneStoreroom = auditScanAreaFactoryService.storeroom(storeOne, company)
      final storeThreeWarehouse = auditScanAreaFactoryService.warehouse(storeThree, company)
      final storeThreeShowroom = auditScanAreaFactoryService.showroom(storeThree, company)
      final storeThreeStoreroom = auditScanAreaFactoryService.storeroom(storeThree, company)

      // setup store one open audit
      final openStoreOneAudit = auditFactoryService.single(storeOne, storeOneEmployee)
      auditDetailFactoryService.generate(11, openStoreOneAudit, storeOneEmployee, storeOneWarehouse)
      auditDetailFactoryService.generate(5, openStoreOneAudit, storeOneEmployee, storeOneStoreroom)
      auditDetailFactoryService.generate(5, openStoreOneAudit, storeOneEmployee, storeOneShowroom)
      auditExceptionFactoryService.generate(25, openStoreOneAudit, storeOneWarehouse, storeOneEmployee)

      // setup store three open audit
      final openStoreThreeAudit = auditFactoryService.single(storeThree, storeThreeEmployee)
      auditDetailFactoryService.generate(9, openStoreThreeAudit, storeThreeEmployee, storeThreeWarehouse)
      auditDetailFactoryService.generate(5, openStoreThreeAudit, storeThreeEmployee, storeThreeShowroom)
      auditDetailFactoryService.generate(5, openStoreThreeAudit, storeThreeEmployee, storeThreeStoreroom)
      auditExceptionFactoryService.generate(26, openStoreThreeAudit, storeThreeShowroom, storeThreeEmployee)

      // setup store one canceled audit
      auditFactoryService.single(storeOne, storeOneEmployee, [AuditStatusFactory.created(), AuditStatusFactory.canceled()] as Set)

      // setup store three canceled audit
      auditFactoryService.single(storeThree, storeThreeEmployee, [AuditStatusFactory.created(), AuditStatusFactory.canceled()] as Set)

      // setup store one completed off audits
      auditFactoryService.generate(3, storeOne, storeOneEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)

      // setup store three completed off audits
      auditFactoryService.generate(4, storeThree, storeThreeEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)

      // setup store one approved audits
      auditFactoryService.generate(3, storeOne, storeOneEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed(), AuditStatusFactory.approved()] as Set)

      // setup store three approved audits
      auditFactoryService.generate(4, storeThree, storeThreeEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed(), AuditStatusFactory.approved()] as Set)

      when:
      def twoCreatedAudits = get(path + new AuditPageRequest([page: 1, size: 5, sortBy: 'id', from: OffsetDateTime.now(), thru: OffsetDateTime.now(), status: [AuditStatusFactory.created().value] as Set]))

      then:
      notThrown(HttpClientResponseException)
      twoCreatedAudits.elements[0].totalDetails == 21
      twoCreatedAudits.elements[0].totalExceptions == 25
      twoCreatedAudits.elements[1].totalDetails == 19
      twoCreatedAudits.elements[1].totalExceptions == 26
      twoCreatedAudits.elements != null
      twoCreatedAudits.elements.size() == 2
      twoCreatedAudits.totalElements == 2
      twoCreatedAudits.totalPages == 1
      twoCreatedAudits.first == true
      twoCreatedAudits.last == true
   }

   void "fetch all when each audit has multiple statuses" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final employee = employeeFactoryService.single(company)
      auditFactoryService.generate(6, storeOne, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)

      when:
      def firstFiveAudits = get(path + new StandardPageRequest([page: 1, size: 5, sortBy: 'id']))

      then:
      notThrown(HttpClientResponseException)
      firstFiveAudits.elements != null
      firstFiveAudits.elements.size() == 5
      firstFiveAudits.totalElements == 6
      firstFiveAudits.totalPages == 2
   }

   void "fetch audit status counts using defaults for request parameters" () {
      setup:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final employee = employeeFactoryService.single(company)
      auditFactoryService.generate(1, employee, [AuditStatusFactory.created()] as Set)
      auditFactoryService.generate(2, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      auditFactoryService.generate(3, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.canceled()] as Set)
      auditFactoryService.generate(4, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      auditFactoryService.generate(5, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed(), AuditStatusFactory.approved()] as Set)

      when:
      def counts = get("${path}/counts").collect { new AuditStatusCountDataTransferObject(it.count, new AuditStatusValueObject(it.status)) }.sort { o1, o2 -> o1.getStatus().id <=> o2.getStatus().id }

      then:
      notThrown(HttpClientResponseException)
      counts.size() == 5
      counts == [
         new AuditStatusCountDataTransferObject(1, new AuditStatusValueObject(AuditStatusFactory.created())),
         new AuditStatusCountDataTransferObject(2, new AuditStatusValueObject(AuditStatusFactory.inProgress())),
         new AuditStatusCountDataTransferObject(4, new AuditStatusValueObject(AuditStatusFactory.completed())),
         new AuditStatusCountDataTransferObject(3, new AuditStatusValueObject(AuditStatusFactory.canceled())),
         new AuditStatusCountDataTransferObject(5, new AuditStatusValueObject(AuditStatusFactory.approved()))
      ]
   }

   void "fetch audit status counts using specified from" () {
      setup:
      final def from = OffsetDateTime.now().minusDays(1)
      final company = companyFactoryService.forDatasetCode('tstds1')
      final employee = employeeFactoryService.single(company)
      auditFactoryService.generate(1, employee, [AuditStatusFactory.created()] as Set)
      auditFactoryService.generate(2, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      auditFactoryService.generate(3, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.canceled()] as Set)
      auditFactoryService.generate(4, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      auditFactoryService.generate(5, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed(), AuditStatusFactory.approved()] as Set)

      when:
      def counts = get("${path}/counts?from=${from}").collect { new AuditStatusCountDataTransferObject(it.count, new AuditStatusValueObject(it.status)) }.sort { o1, o2 -> o1.getStatus().id <=> o2.getStatus().id }

      then:
      notThrown(HttpClientResponseException)
      counts.size() == 5
      counts == [
         new AuditStatusCountDataTransferObject(1, new AuditStatusValueObject(AuditStatusFactory.created())),
         new AuditStatusCountDataTransferObject(2, new AuditStatusValueObject(AuditStatusFactory.inProgress())),
         new AuditStatusCountDataTransferObject(4, new AuditStatusValueObject(AuditStatusFactory.completed())),
         new AuditStatusCountDataTransferObject(3, new AuditStatusValueObject(AuditStatusFactory.canceled())),
         new AuditStatusCountDataTransferObject(5, new AuditStatusValueObject(AuditStatusFactory.approved()))
      ]
   }

   void "fetch audit status counts using specified from/thru and statuses" () {
      setup:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final employee = employeeFactoryService.single(company)
      auditFactoryService.generate(1, employee, [AuditStatusFactory.created()] as Set)
      auditFactoryService.generate(2, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      auditFactoryService.generate(3, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.canceled()] as Set)
      auditFactoryService.generate(4, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      auditFactoryService.generate(5, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed(), AuditStatusFactory.approved()] as Set)

      final def from = OffsetDateTime.now().minusDays(1)
      final def thru = OffsetDateTime.now()

      when:
      def counts = get("${path}/counts?from=$from&thru=$thru&status=CREATED&status=IN-PROGRESS").collect { new AuditStatusCountDataTransferObject(it.count, new AuditStatusValueObject(it.status)) }.sort { o1, o2 -> o1.getStatus().id <=> o2.getStatus().id }

      then:
      notThrown(HttpClientResponseException)
      counts.size() == 2
      counts == [
         new AuditStatusCountDataTransferObject(1, new AuditStatusValueObject(AuditStatusFactory.created())),
         new AuditStatusCountDataTransferObject(2, new AuditStatusValueObject(AuditStatusFactory.inProgress())),
      ]
   }

   @Unroll
   void "fetch audit status counts using specified from/thru and statuses #statusValuesIn and store numbers #storeNumberValuesIn" () {
      given:
      final def company = companyFactoryService.forDatasetCode('tstds1')
      final def storeOne = storeFactoryService.store(1, company)
      final def storeThree = storeFactoryService.store(3, company)
      final def employee = employeeFactoryService.single(company)

      auditFactoryService.generate(1, storeOne, employee,
            [AuditStatusFactory.created()] as Set)
      auditFactoryService.generate(2, storeOne, employee,
            [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)

      final inProgressAuditFromLastWeek = auditFactoryService.single(storeOne, employee,
            [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)

      auditFactoryService.generate(3, storeOne, employee,
            [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      auditFactoryService.generate(2, storeThree, employee,
            [AuditStatusFactory.created()] as Set)
      auditFactoryService.generate(2, storeThree, employee,
            [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)

      final completedAuditFromLastWeek = auditFactoryService.single(storeThree, employee,
            [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)

      final def from = OffsetDateTime.now().minusDays(3)
      final def thru = OffsetDateTime.now()

      when:
      jdbc.update("UPDATE audit set time_created = :time_created WHERE id = :id", [time_created: inProgressAuditFromLastWeek.timeCreated.minusDays(8), id: inProgressAuditFromLastWeek.id])
      jdbc.update("UPDATE audit set time_created = :time_created WHERE id = :id", [time_created: completedAuditFromLastWeek.timeCreated.minusDays(8), id: completedAuditFromLastWeek.id])
      def countsResult = get("${path}/counts" + new AuditPageRequest([from: from, thru:thru, status: statusValuesIn, storeNumber: storeNumberValuesIn]))
         .collect { new AuditStatusCountDataTransferObject(it.count, new AuditStatusValueObject(it.status)) }

      then:
      notThrown(HttpClientResponseException)
      countsResult.stream().filter({ it -> it.getStatus().getValue() == 'CREATED' })
         .findFirst().map({ it -> it.count }).orElse(null) == createdCount

      countsResult.stream().filter({ it -> it.getStatus().getValue() == 'IN-PROGRESS' })
         .findFirst().map({ it -> it.count }).orElse(null) == inProgressCount

      countsResult.stream().filter({ it -> it.getStatus().getValue() == 'COMPLETED' })
         .findFirst().map({ it -> it.count }).orElse(null) == completedCount

      where:
      storeNumberValuesIn  | statusValuesIn              | createdCount | inProgressCount | completedCount
      [1]                  | ['CREATED', 'IN-PROGRESS']  | 1            | 2               | null
      [3]                  | ['CREATED', 'IN-PROGRESS']  | 2            | null            | null
      [1, 3]               | ['CREATED', 'IN-PROGRESS']  | 3            | 2               | null
      [1, 3]               | ['COMPLETED']               | null         | null            | 5
   }

   void "create new audit" () {
      given:
      final tstds1StoreManagerDepartment = departmentFactoryService.department('SM', tstds1)
      final store1Tstds1Employee = employeeFactoryService.single(store1Tstds1, tstds1StoreManagerDepartment)
      final store1Tstds1AuthenticatedEmployee = userService.fetchUserByAuthentication(store1Tstds1Employee.number, store1Tstds1Employee.passCode, tstds1.myDataset(), store1Tstds1Employee.store.number).blockingGet().with { new AuthenticatedEmployee(it, store1Tstds1Employee.passCode) }
      final store1Tstds1UserAccessToken = loginEmployee(store1Tstds1AuthenticatedEmployee)

      when:
      def result = post(path, new AuditCreateValueObject(), store1Tstds1UserAccessToken)

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.id > 0
      result.store.storeNumber == 1
      result.actions.size() == 1
      result.actions[0].status.value == "CREATED"
      result.actions[0].status.description == "Created"
      result.actions[0].changedBy.number == store1Tstds1AuthenticatedEmployee.number
   }

   void "create new audits and verify audit numbers are sequential" () {
      when:
      def firstAudit = post(path, new AuditCreateValueObject([store:  new StoreValueObject([storeNumber: 3])]))
      post(path, new AuditCreateValueObject([store:  new StoreValueObject([storeNumber: 1])]))
      put(path, new AuditUpdateValueObject([id: firstAudit.id, status: new AuditStatusValueObject([value: "CANCELED"])]))
      def secondAudit = post(path, new AuditCreateValueObject([store:  new StoreValueObject([storeNumber: 3])]))

      then:
      notThrown(HttpClientResponseException)
      firstAudit.auditNumber > 0
      firstAudit.auditNumber + 1 == secondAudit.auditNumber
   }

   void "create new audit when previous audit was cancelled" () {
      given:
      final tstds1StoreManagerDepartment = departmentFactoryService.department('SM', tstds1)
      final store1Tstds1Employee = employeeFactoryService.single(store1Tstds1, tstds1StoreManagerDepartment)
      final store1Tstds1AuthenticatedEmployee = userService.fetchUserByAuthentication(store1Tstds1Employee.number, store1Tstds1Employee.passCode, tstds1.myDataset(), store1Tstds1Employee.store.number).blockingGet().with { new AuthenticatedEmployee(it, store1Tstds1Employee.passCode) }
      final store1Tstds1UserAccessToken = loginEmployee(store1Tstds1AuthenticatedEmployee)
      final store = storeFactoryService.random(tstds1)
      final employee = employeeFactoryService.single(tstds1)
      auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.canceled()] as Set)

      when:
      def result = post(path, new AuditCreateValueObject(), store1Tstds1UserAccessToken)

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.id > 0
      result.store.storeNumber == 1
      result.actions.size() == 1
      result.actions[0].status.value == "CREATED"
      result.actions[0].status.description == "Created"
      result.actions[0].changedBy.number == store1Tstds1AuthenticatedEmployee.number
   }

   void "create new audit when previous audit was approved" () {
      given:
      final tstds1StoreManagerDepartment = departmentFactoryService.department('SM', tstds1)
      final store1Tstds1Employee = employeeFactoryService.single(store1Tstds1, tstds1StoreManagerDepartment)
      final store1Tstds1AuthenticatedEmployee = userService.fetchUserByAuthentication(store1Tstds1Employee.number, store1Tstds1Employee.passCode, tstds1.myDataset(), store1Tstds1Employee.store.number).blockingGet().with { new AuthenticatedEmployee(it, store1Tstds1Employee.passCode) }
      final store1Tstds1UserAccessToken = loginEmployee(store1Tstds1AuthenticatedEmployee)
      auditFactoryService.single(store1Tstds1, store1Tstds1Employee, [AuditStatusFactory.created(), AuditStatusFactory.canceled(), AuditStatusFactory.approved()] as Set)

      when:
      def result = post(path, new AuditCreateValueObject(), store1Tstds1UserAccessToken)

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.id > 0
      result.store.storeNumber == 1
      result.actions.size() == 1
      result.actions[0].status.value == "CREATED"
      result.actions[0].status.description == "Created"
      result.actions[0].changedBy.number == store1Tstds1AuthenticatedEmployee.number
   }

   void "create new audit with invalid store" () {
      when:
      post(path, new AuditCreateValueObject([store:  new StoreValueObject([storeNumber: 13])]))

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
      final tstds1StoreManagerDepartment = departmentFactoryService.department('SM', tstds1)
      final store1Tstds1Employee = employeeFactoryService.single(store1Tstds1, tstds1StoreManagerDepartment)
      final store1Tstds1AuthenticatedEmployee = userService.fetchUserByAuthentication(store1Tstds1Employee.number, store1Tstds1Employee.passCode, tstds1.myDataset(), store1Tstds1Employee.store.number).blockingGet().with { new AuthenticatedEmployee(it, store1Tstds1Employee.passCode) }
      final store1Tstds1UserAccessToken = loginEmployee(store1Tstds1AuthenticatedEmployee)
      auditFactoryService.single(store1Tstds1)
      final newAudit = AuditFactory.single(store1Tstds1).with { new AuditCreateValueObject([store: new StoreValueObject(it.store)]) }

      when:
      post(path, newAudit, store1Tstds1UserAccessToken)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response.collect { new ErrorDataTransferObject(it.message, it.path) } == [new ErrorDataTransferObject("Store 1 has an audit already in progress", "storeNumber")]
   }

   void "update opened audit to in progress" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(1, company)
      final audit = auditFactoryService.single(store)

      when:
      def result = put(path, new AuditUpdateValueObject(['id': audit.id, 'status': new AuditStatusValueObject([value: 'IN-PROGRESS'])]))

      then:
      notThrown(HttpClientResponseException)
      result.id == audit.id
      result.store.storeNumber == store.myNumber()
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
      resultActions[1].changedBy.number == nineNineEightAuthenticatedEmployee.number
   }

   void "update opened audit to canceled" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(1, company)
      final audit = auditFactoryService.single(store)

      when:
      def result = put(path, new AuditUpdateValueObject([id: audit.id, status: new AuditStatusValueObject([value: "CANCELED"])]))

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.id == audit.id
      result.store.storeNumber == audit.store.myNumber()
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
      resultActions[1].changedBy.number == nineNineEightAuthenticatedEmployee.number
   }

   void "update opened to completed" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(1, company)
      final audit = auditFactoryService.single(store)

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
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(1, company)
      final employee = employeeFactoryService.single(store)
      final audit = auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)

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
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(1, company)
      final employee = employeeFactoryService.single(store)
      final audit = auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)

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
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(1, company)
      final employee = employeeFactoryService.single(store)
      final audit = auditFactoryService.single(store, employee, [AuditStatusFactory.created()] as Set)

      when:
      client.exchange(
         PUT("/${path}", """{ "id": ${audit.id}, "status": { "value": null } }""").header("Authorization", "Bearer $nineNineEightAccessToken"),
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
      response[0].path == "id"
      response[0].message == "Is required"
   }

   void "update audit with a non-existent id" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(1, company)
      final savedAudit = auditFactoryService.single(store)
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
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(1, company)
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
      def openedResult = post(path, new AuditCreateValueObject([store: new StoreValueObject(storeNumber: 3)]))

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
      openActions[0].changedBy.number == nineNineEightAuthenticatedEmployee.number

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
      inProgressActions[0].changedBy.number == nineNineEightAuthenticatedEmployee.number
      inProgressActions[1].id != null
      inProgressActions[1].id > 0
      inProgressActions[1].id > inProgressActions[0].id
      inProgressActions[1].status.value == "IN-PROGRESS"
      inProgressActions[1].status.description == "In Progress"
      inProgressActions[1].changedBy.number == nineNineEightAuthenticatedEmployee.number

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
      completedActions[0].changedBy.number == nineNineEightAuthenticatedEmployee.number
      completedActions[1].id != null
      completedActions[1].id > 0
      completedActions[1].id > completedActions[0].id
      completedActions[1].status.value == "IN-PROGRESS"
      completedActions[1].status.description == "In Progress"
      completedActions[1].changedBy.number == nineNineEightAuthenticatedEmployee.number
      completedActions[2].id != null
      completedActions[2].id > 0
      completedActions[2].id > completedActions[1].id
      completedActions[2].status.value == "COMPLETED"
      completedActions[2].status.description == "Completed"
      completedActions[2].changedBy.number == nineNineEightAuthenticatedEmployee.number
   }

   void "create new audit after existing open audit was COMPLETED" () {
      given:
      final tstds1StoreManagerDepartment = departmentFactoryService.department('SM', tstds1)
      final store1Tstds1Employee = employeeFactoryService.single(store1Tstds1, tstds1StoreManagerDepartment)
      final store1Tstds1AuthenticatedEmployee = userService.fetchUserByAuthentication(store1Tstds1Employee.number, store1Tstds1Employee.passCode, tstds1.myDataset(), store1Tstds1Employee.store.myNumber()).blockingGet().with { new AuthenticatedEmployee(it, store1Tstds1Employee.passCode) }
      final store1Tstds1UserAccessToken = loginEmployee(store1Tstds1AuthenticatedEmployee)
      final audit = auditFactoryService.single(store1Tstds1, store1Tstds1Employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)

      when:
      put(path, new AuditUpdateValueObject([id: audit.id, status:  new AuditStatusValueObject([value: "COMPLETED"])]))

      then:
      notThrown(HttpClientResponseException)
      auditRepository.countAuditsNotCompletedOrCanceled(audit.store.myNumber(), audit.store.myCompany()) == 0

      when:
      def result = post(path, new AuditCreateValueObject(), store1Tstds1UserAccessToken)

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.id > audit.id
      result.store.storeNumber == 1
      result.actions.size() == 1
      result.actions[0].status.value == "CREATED"
      result.actions[0].status.description == "Created"
      result.actions[0].changedBy.number == store1Tstds1AuthenticatedEmployee.number
   }

   void "update completed audit to approved and approve exceptions" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final department = departmentFactoryService.random(company)
      final store = storeFactoryService.store(1, company)
      final employee = employeeFactoryService.single(store, department)
      final audit = auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      final storeThreeStoreroom = auditScanAreaFactoryService.storeroom(store, company)
      final List<AuditExceptionValueObject> threeAuditExceptions = auditExceptionFactoryService.stream(3, audit, storeThreeStoreroom, employee, false).map { new AuditExceptionValueObject(it, new AuditScanAreaDTO(it.scanArea)) }.toList()

      when:
      def result = put("$path/approve", new AuditUpdateValueObject(['id': audit.id, 'status': new AuditStatusValueObject([value: 'APPROVED'])]))

      then:
      notThrown(HttpClientResponseException)
      result.id == audit.id
      result.store.storeNumber == store.number
      result.actions.size() == 4
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
      resultActions[1].changedBy.number == audit.actions[1].changedBy.number
      resultActions[2].id != null
      resultActions[2].id > 0
      resultActions[2].id > resultActions[0].id
      resultActions[2].status.value == "COMPLETED"
      resultActions[2].status.description == "Completed"
      resultActions[2].changedBy.number == audit.actions[2].changedBy.number
      resultActions[3].id != null
      resultActions[3].id > 0
      resultActions[3].id > resultActions[0].id
      resultActions[3].status.value == "APPROVED"
      resultActions[3].status.description == "Approved"
      resultActions[3].changedBy.number == nineNineEightAuthenticatedEmployee.number

      when:
      def pageOneResult = get("/audit/${audit.id}/exception")

      then:
      notThrown(HttpClientResponseException)
      audit.number == 1
      pageOneResult.elements.size() == 3
      pageOneResult.elements.each {it['audit'] = new SimpleIdentifiableValueObject(it.audit.id)}
         .each { it['timeCreated'] = OffsetDateTime.parse(it['timeCreated']) }
         .each { it['timeUpdated'] = OffsetDateTime.parse(it['timeUpdated']) }
         .each { it['approved'] = true }

      when: 'Test Audit Exception Report'
      def auditExceptionPdf = client.exchange(GET("/${path}/${audit.id}/report/exception").header("Authorization", "Bearer $nineNineEightAccessToken"), Argument.of(byte[]))

      then:
      notThrown(HttpClientResponseException)
      auditExceptionPdf != null
      auditExceptionPdf.status == OK
      new String(auditExceptionPdf.getBody(byte[]).get()).startsWith("%PDF-1.5")

      when: 'Test Unscanned Idle Inventory Report'
      def unscannedIdleInventoryPdf = client.exchange(GET("/${path}/${audit.id}/report/unscanned")
                                     .header("Authorization", "Bearer $nineNineEightAccessToken"), Argument.of(byte[]))

      then:
      notThrown(HttpClientResponseException)
      unscannedIdleInventoryPdf != null
      unscannedIdleInventoryPdf.status == OK
      new String(unscannedIdleInventoryPdf.getBody(byte[]).get()).startsWith("%PDF-1.5")
   }

   void "Confirm exceptions approved when audit is approved" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final employee = employeeFactoryService.single(company)
      final store = storeFactoryService.store(1, company)
      final auditOne = auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      final storeStoreroom = auditScanAreaFactoryService.storeroom(store, company)
      final List<AuditExceptionValueObject> threeAuditDiscrepanciesAuditOne = auditExceptionFactoryService.stream(3, auditOne, storeStoreroom, employee, false).map { new AuditExceptionValueObject(it, new AuditScanAreaDTO(it.scanArea)) }.toList()

      when:
      put("$path/approve", new AuditUpdateValueObject([id: auditOne.id, status: new AuditStatusValueObject([value: "APPROVED"])]))
      def pageOneResult = get("/audit/${auditOne.id}/exception")

      then:
      notThrown(HttpClientResponseException)
      auditOne.number > 0
      pageOneResult.elements != null
      pageOneResult.elements.size() == 3
      pageOneResult.elements.each{ it['audit'] = new SimpleIdentifiableValueObject(it.audit.id) }
         .each { it['timeCreated'] = OffsetDateTime.parse(it['timeCreated']) }
         .each { it['timeUpdated'] = OffsetDateTime.parse(it['timeUpdated']) }
         .each { it['approved'] = true }
   }

   void "approve all audit exceptions" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(1, company)
      final employee = employeeFactoryService.single(store)
      final audit = auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      final storeStoreroom = auditScanAreaFactoryService.storeroom(store, company)
      final auditExceptions = auditExceptionFactoryService.stream(9, audit, storeStoreroom, employee, false).toList()

      when:
      def result = put("$path/approve/exceptions", new SimpleIdentifiableDataTransferObject(audit.myId()))

      then:
      notThrown(HttpClientResponseException)
      result != null
      result.approved == 9
   }

   void "approve when all audit exceptions are already approved" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(1, company)
      final employee = employeeFactoryService.single(store)
      final audit = auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      final storeStoreroom = auditScanAreaFactoryService.storeroom(store, company)
      final auditExceptions = auditExceptionFactoryService.stream(9, audit, storeStoreroom, employee, true).toList()

      when:
      def result = put("$path/approve/exceptions", new SimpleIdentifiableDataTransferObject(audit.myId()))

      then:
      notThrown(HttpClientResponseException)
      result != null
      result.approved == 0
   }
}
