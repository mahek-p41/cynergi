package com.cynergisuite.middleware.audit.exception.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.audit.AuditTestDataLoaderService
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaDTO
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaFactoryService
import com.cynergisuite.middleware.audit.exception.AuditExceptionCreateDTO
import com.cynergisuite.middleware.audit.exception.AuditExceptionDTO
import com.cynergisuite.middleware.audit.exception.AuditExceptionEntity
import com.cynergisuite.middleware.audit.exception.AuditExceptionTestDataLoader
import com.cynergisuite.middleware.audit.exception.AuditExceptionTestDataLoaderService
import com.cynergisuite.middleware.audit.exception.AuditExceptionUpdateDTO
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNoteFactoryService
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNoteValueObject
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.error.ErrorDTO
import com.cynergisuite.middleware.inventory.InventoryEntity
import com.cynergisuite.middleware.inventory.InventoryService
import com.cynergisuite.middleware.inventory.infrastructure.InventoryPageRequest
import com.cynergisuite.middleware.inventory.location.InventoryLocationFactory
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.apache.commons.lang3.RandomUtils

import java.time.OffsetDateTime

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT
import static org.apache.commons.lang3.StringUtils.EMPTY

@MicronautTest(transactional = false)
class AuditExceptionControllerSpecification extends ControllerSpecificationBase {

   @Inject AuditExceptionRepository auditExceptionRepository
   @Inject AuditExceptionTestDataLoaderService auditExceptionFactoryService
   @Inject AuditExceptionNoteFactoryService auditExceptionNoteFactoryService
   @Inject AuditTestDataLoaderService auditFactoryService
   @Inject AuditScanAreaFactoryService auditScanAreaFactoryService
   @Inject InventoryService inventoryService

   void "fetch one audit exception by id with no attached notes" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final audit = auditFactoryService.single(store)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final storeroom = auditScanAreaFactoryService.storeroom(store, company)
      final auditException = auditExceptionFactoryService.single(audit, storeroom, employee, false)

      when:
      def result = get("/audit/exception/${auditException.id}")

      then:
      notThrown(HttpClientException)
      result.id == auditException.id
      result.timeCreated == auditException.timeCreated
      result.timeUpdated == auditException.timeUpdated
      result.barcode == auditException.barcode
      result.serialNumber == auditException.serialNumber
      result.exceptionCode == auditException.exceptionCode
      result.inventoryBrand == auditException.inventoryBrand
      result.inventoryModel == auditException.inventoryModel
      result.scanArea.name == auditException.scanArea.name
      result.scanArea.store.storeNumber == auditException.scanArea.store.number
      result.scanArea.store.name == auditException.scanArea.store.name
      result.scannedBy.number == auditException.scannedBy.number
      result.scannedBy.lastName == auditException.scannedBy.lastName
      result.scannedBy.firstNameMi == auditException.scannedBy.firstNameMi
      result.lookupKey == auditException.lookupKey
      result.notes.size() == 0
      result.audit.id == auditException.audit.myId()
   }

   void "fetch one audit exception with a single attached note" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final audit = auditFactoryService.single(store)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final storeroom = auditScanAreaFactoryService.storeroom(store, company)
      final auditException = auditExceptionFactoryService.single(audit, storeroom, employee, false)
      final auditNote = auditExceptionNoteFactoryService.single(auditException, employee)

      when:
      def result = get("/audit/exception/${auditNote.auditException.myId()}")

      then:
      notThrown(HttpClientResponseException)
      result.id == auditNote.auditException.myId()
      result.notes.size() == 1
      result.notes[0].id == auditNote.id
      result.notes[0].note == auditNote.note
      result.notes[0].enteredBy.number == auditNote.enteredBy.number
      result.notes[0].timeCreated == auditNote.timeCreated
      result.notes[0].timeUpdated == auditNote.timeUpdated
   }

   void "fetch one audit exception with a two attached notes" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final audit = auditFactoryService.single(store)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final warehouse = auditScanAreaFactoryService.warehouse(store, company)
      final auditException = auditExceptionFactoryService.single(audit, warehouse, employee, false)
      final auditNotes = auditExceptionNoteFactoryService.stream(2, auditException, employee).map { new AuditExceptionNoteValueObject(it) }.sorted{ o1, o2 -> o1.id <=> o2.id  }.toList()
      final auditExceptionId = auditException.myId()

      when:
      def result = get("/audit/exception/${auditExceptionId}")

      then:
      notThrown(HttpClientResponseException)
      result.notes.size() == 2
      result.id == auditException.id
      result.audit.id == auditException.audit.myId()
      result.notes.collect { new AuditExceptionNoteValueObject(it) }.sort { o1, o2 -> o1.id <=> o2.id } == auditNotes
   }

   void "fetch all exceptions for a single audit with default paging" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final audit = auditFactoryService.single(store)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final warehouse = auditScanAreaFactoryService.warehouse(store, company)
      final twentyAuditDiscrepancies = auditExceptionFactoryService.stream(20, audit, warehouse, employee, false).map { new AuditExceptionDTO(it, new AuditScanAreaDTO(it.scanArea)) }.toList()
      final firstTenDiscrepancies = twentyAuditDiscrepancies[0..9]

      when:
      def pageOneResult = get("/audit/${audit.id}/exception")

      then:
      notThrown(HttpClientResponseException)
      audit.number > 0
      pageOneResult.requested.page == 1
      pageOneResult.requested.size == 10
      pageOneResult.requested.sortBy == "id"
      pageOneResult.requested.sortDirection == "ASC"
      pageOneResult.elements != null
      pageOneResult.elements.size() == 10
      pageOneResult.elements.each{ it['audit'] = new SimpleIdentifiableDTO(it.audit.id) }
         .collect { new AuditExceptionDTO(it) } == firstTenDiscrepancies
   }

   void "fetch all exceptions for a single audit" () {
      given:
      final nonExistentId = UUID.randomUUID()
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final audit = auditFactoryService.single(store)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final warehouse = auditScanAreaFactoryService.warehouse(store, company)
      final twentyAuditDiscrepancies = auditExceptionFactoryService.stream(20, audit, warehouse, employee, false).map { new AuditExceptionDTO(it, new AuditScanAreaDTO(it.scanArea)) }.toList()
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      final pageFive = new StandardPageRequest(5, 5, "id", "ASC")
      final firstFiveDiscrepancies = twentyAuditDiscrepancies[0..4]
      final secondFiveDiscrepancies = twentyAuditDiscrepancies[5..9]

      when:
      def pageOneResult = get("/audit/${audit.id}/exception$pageOne")

      then:
      notThrown(HttpClientResponseException)
      audit.number > 0
      pageOneResult.requested.page == 1
      pageOneResult.requested.size == 5
      pageOneResult.requested.sortBy == "id"
      pageOneResult.requested.sortDirection == "ASC"
      new StandardPageRequest(pageOneResult.requested) == pageOne
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.elements.each{ it['audit'] = new SimpleIdentifiableDTO(it.audit.id) }
         .collect { new AuditExceptionDTO(it) } == firstFiveDiscrepancies

      when:
      def pageTwoResult = get("/audit/${audit.id}/exception$pageTwo")

      then:
      notThrown(HttpClientResponseException)
      new StandardPageRequest(pageTwoResult.requested) == pageTwo
      pageTwoResult.elements != null
      pageTwoResult.elements.size() == 5
      pageTwoResult.elements.each{ it['audit'] = new SimpleIdentifiableDTO(it.audit.id) }
         .collect { new AuditExceptionDTO(it) } == secondFiveDiscrepancies

      when:
      get("/audit/${audit.id}/exception$pageFive")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT

      when:
      get("/audit/$nonExistentId/exception$pageOne")

      then:
      final auditNotFoundException = thrown(HttpClientResponseException)
      auditNotFoundException.status == NOT_FOUND
      final auditNotFoundResult = auditNotFoundException.response.bodyAsJson()
      with(auditNotFoundResult) {
         message == "$nonExistentId was unable to be found"
         code == 'system.not.found'
      }
   }

   void "fetch all audit exceptions for a single audit where each exception has 2 notes attached" () {
      given:
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final audit = auditFactoryService.single(store)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final warehouse = auditScanAreaFactoryService.warehouse(store, company)
      final twentyAuditDiscrepancies = auditExceptionFactoryService.stream(20, audit, warehouse, employee, false)
         .peek{ it.notes.addAll(auditExceptionNoteFactoryService.stream(2, it, employee).toList()) } // create some notes and save them
         .map { new AuditExceptionDTO(it, new AuditScanAreaDTO(it.scanArea)) }
         .toList()
      final firstFiveDiscrepancies = twentyAuditDiscrepancies[0..4]

      when:
      def pageOneResult = get("/audit/${audit.id}/exception$pageOne")

      then:
      notThrown(HttpClientResponseException)
      new StandardPageRequest(pageOneResult.requested) == pageOne
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      final pageOneAuditExceptions = pageOneResult.elements.each{ it['audit'] = new SimpleIdentifiableDTO(it.audit.id) }
         .collect { new AuditExceptionDTO(it) }
      pageOneAuditExceptions[0].notes.size() == 2
      pageOneAuditExceptions[0].notes[0].id == firstFiveDiscrepancies[0].notes[0].id
      pageOneAuditExceptions[0].notes[0].note == firstFiveDiscrepancies[0].notes[0].note
      pageOneAuditExceptions[0].notes[0].timeCreated == firstFiveDiscrepancies[0].notes[0].timeCreated
      pageOneAuditExceptions[0].notes[0].timeUpdated == firstFiveDiscrepancies[0].notes[0].timeUpdated
      pageOneAuditExceptions[0].notes[0].enteredBy.number == employee.number
      pageOneAuditExceptions[0].notes[1].id == firstFiveDiscrepancies[0].notes[1].id
      pageOneAuditExceptions[0].notes[1].note == firstFiveDiscrepancies[0].notes[1].note
      pageOneAuditExceptions[0].notes[1].timeCreated == firstFiveDiscrepancies[0].notes[1].timeCreated
      pageOneAuditExceptions[0].notes[1].timeUpdated == firstFiveDiscrepancies[0].notes[1].timeUpdated
      pageOneAuditExceptions[0].notes[1].enteredBy.number == employee.number

      pageOneAuditExceptions[1].notes.size() == 2
      pageOneAuditExceptions[1].notes[0].id == firstFiveDiscrepancies[1].notes[0].id
      pageOneAuditExceptions[1].notes[0].note == firstFiveDiscrepancies[1].notes[0].note
      pageOneAuditExceptions[1].notes[0].timeCreated == firstFiveDiscrepancies[1].notes[0].timeCreated
      pageOneAuditExceptions[1].notes[0].timeUpdated == firstFiveDiscrepancies[1].notes[0].timeUpdated
      pageOneAuditExceptions[1].notes[0].enteredBy.number == employee.number
      pageOneAuditExceptions[1].notes[1].id == firstFiveDiscrepancies[1].notes[1].id
      pageOneAuditExceptions[1].notes[1].note == firstFiveDiscrepancies[1].notes[1].note
      pageOneAuditExceptions[1].notes[1].timeCreated == firstFiveDiscrepancies[1].notes[1].timeCreated
      pageOneAuditExceptions[1].notes[1].timeUpdated == firstFiveDiscrepancies[1].notes[1].timeUpdated
      pageOneAuditExceptions[1].notes[1].enteredBy.number == employee.number

      pageOneAuditExceptions[2].notes.size() == 2
      pageOneAuditExceptions[2].notes[0].id == firstFiveDiscrepancies[2].notes[0].id
      pageOneAuditExceptions[2].notes[0].note == firstFiveDiscrepancies[2].notes[0].note
      pageOneAuditExceptions[2].notes[0].timeCreated == firstFiveDiscrepancies[2].notes[0].timeCreated
      pageOneAuditExceptions[2].notes[0].timeUpdated == firstFiveDiscrepancies[2].notes[0].timeUpdated
      pageOneAuditExceptions[2].notes[0].enteredBy.number == employee.number
      pageOneAuditExceptions[2].notes[1].id == firstFiveDiscrepancies[2].notes[1].id
      pageOneAuditExceptions[2].notes[1].note == firstFiveDiscrepancies[2].notes[1].note
      pageOneAuditExceptions[2].notes[1].timeCreated == firstFiveDiscrepancies[2].notes[1].timeCreated
      pageOneAuditExceptions[2].notes[1].timeUpdated == firstFiveDiscrepancies[2].notes[1].timeUpdated
      pageOneAuditExceptions[2].notes[1].enteredBy.number == employee.number

      pageOneAuditExceptions[3].notes.size() == 2
      pageOneAuditExceptions[3].notes[0].id == firstFiveDiscrepancies[3].notes[0].id
      pageOneAuditExceptions[3].notes[0].note == firstFiveDiscrepancies[3].notes[0].note
      pageOneAuditExceptions[3].notes[0].timeCreated == firstFiveDiscrepancies[3].notes[0].timeCreated
      pageOneAuditExceptions[3].notes[0].timeUpdated == firstFiveDiscrepancies[3].notes[0].timeUpdated
      pageOneAuditExceptions[3].notes[0].enteredBy.number == employee.number
      pageOneAuditExceptions[3].notes[1].id == firstFiveDiscrepancies[3].notes[1].id
      pageOneAuditExceptions[3].notes[1].note == firstFiveDiscrepancies[3].notes[1].note
      pageOneAuditExceptions[3].notes[1].timeCreated == firstFiveDiscrepancies[3].notes[1].timeCreated
      pageOneAuditExceptions[3].notes[1].timeUpdated == firstFiveDiscrepancies[3].notes[1].timeUpdated
      pageOneAuditExceptions[3].notes[1].enteredBy.number == employee.number

      pageOneAuditExceptions[4].notes.size() == 2
      pageOneAuditExceptions[4].notes[0].id == firstFiveDiscrepancies[4].notes[0].id
      pageOneAuditExceptions[4].notes[0].note == firstFiveDiscrepancies[4].notes[0].note
      pageOneAuditExceptions[4].notes[0].timeCreated == firstFiveDiscrepancies[4].notes[0].timeCreated
      pageOneAuditExceptions[4].notes[0].timeUpdated == firstFiveDiscrepancies[4].notes[0].timeUpdated
      pageOneAuditExceptions[4].notes[0].enteredBy.number == employee.number
      pageOneAuditExceptions[4].notes[1].id == firstFiveDiscrepancies[4].notes[1].id
      pageOneAuditExceptions[4].notes[1].note == firstFiveDiscrepancies[4].notes[1].note
      pageOneAuditExceptions[4].notes[1].timeCreated == firstFiveDiscrepancies[4].notes[1].timeCreated
      pageOneAuditExceptions[4].notes[1].timeUpdated == firstFiveDiscrepancies[4].notes[1].timeUpdated
      pageOneAuditExceptions[4].notes[1].enteredBy.number == employee.number
   }

   void "fetch all audit exceptions when more than one audit exists" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final auditOne = auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      final auditTwo = auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      final warehouse = auditScanAreaFactoryService.warehouse(store, company)
      final List<AuditExceptionDTO> threeAuditDiscrepanciesAuditTwo = auditExceptionFactoryService.stream(3, auditTwo, warehouse, employee, false).map { new AuditExceptionDTO(it, new AuditScanAreaDTO(it.scanArea)) }.toList()

      when:
      def pageOneResult = get("/audit/${auditTwo.id}/exception")

      then:
      notThrown(HttpClientResponseException)
      auditOne.number == 1
      auditTwo.number == 2
      pageOneResult.elements.size() == 3
      pageOneResult.elements.each {it['audit'] = new SimpleIdentifiableDTO(it.audit.id)}
         .collect { new AuditExceptionDTO(it) }.toSorted {o1, o2 -> o2.id <=> o2.id } == threeAuditDiscrepanciesAuditTwo
   }

   void "fetch one audit exception by id not found" () {
      given:
      final nonExistentId = UUID.randomUUID()

      when:
      get("/audit/exception/$nonExistentId")

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      with(response) {
         message == "$nonExistentId was unable to be found"
         code == 'system.not.found'
      }
   }

   void "create audit exception" () {
      given:
      final locale = Locale.US
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 25, sortBy: "id", sortDirection: "ASC", storeNumber: store.number, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), company, locale).elements
      final inventoryItem = inventoryListing[RandomUtils.nextInt(0, inventoryListing.size())]
      final audit = auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      final warehouse = auditScanAreaFactoryService.warehouse(store, company)
      final exceptionCode = AuditExceptionTestDataLoader.randomExceptionCode()
      final exception = new AuditExceptionCreateDTO([inventory: new SimpleLegacyIdentifiableDTO(inventoryItem), scanArea: new SimpleIdentifiableDTO(warehouse), exceptionCode: exceptionCode])

      when:
      def result = post("/audit/${audit.id}/exception", exception)

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.timeCreated != null
      result.timeUpdated != null
      result.scanArea.name == warehouse.name
      result.scanArea.store.storeNumber == warehouse.store.number
      result.scanArea.store.name == warehouse.store.name
      result.barcode == inventoryItem.barcode
      result.productCode == inventoryItem.productCode
      result.altId == inventoryItem.altId
      result.serialNumber == inventoryItem.serialNumber
      result.inventoryBrand == inventoryItem.brand
      result.inventoryModel == inventoryItem.modelNumber
      result.exceptionCode == exceptionCode
      result.approved == false
      result.notes.size() == 0
      result.audit.id == audit.id
   }

   void "create audit exception using employee that doesn't have a first name" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final noFirstNameGuy = employeeFactoryService.singleAuthenticated(company, store, department, 'TEST', EMPTY)
      final authToken = loginEmployee(noFirstNameGuy)
      final locale = Locale.US
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 25, sortBy: "id", sortDirection: "ASC", storeNumber: store.number, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), company, locale).elements
      final inventoryItem = inventoryListing[RandomUtils.nextInt(0, inventoryListing.size())]
      final audit = auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      final scanArea = auditScanAreaFactoryService.single('Custom Area', store, company)
      final exceptionCode = AuditExceptionTestDataLoader.randomExceptionCode()
      final exception = new AuditExceptionCreateDTO([inventory: new SimpleLegacyIdentifiableDTO(inventoryItem), scanArea: new SimpleIdentifiableDTO(scanArea), exceptionCode: exceptionCode])

      when:
      def result = post("/audit/${audit.id}/exception", exception, authToken)

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.timeCreated != null
      result.timeUpdated != null
      result.scanArea.name == scanArea.name
      result.scanArea.store.storeNumber == scanArea.store.number
      result.scanArea.store.name == scanArea.store.name
      result.barcode == inventoryItem.barcode
      result.productCode == inventoryItem.productCode
      result.altId == inventoryItem.altId
      result.serialNumber == inventoryItem.serialNumber
      result.inventoryBrand == inventoryItem.brand
      result.inventoryModel == inventoryItem.modelNumber
      result.exceptionCode == exceptionCode
      result.approved == false
      result.notes.size() == 0
      result.audit.id == audit.id
   }

   void "create valid audit exception without scan area" () {
      given:
      final locale = Locale.US
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 25, sortBy: "id", sortDirection: "ASC", storeNumber: store.number, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), company, locale).elements
      final inventoryItem = inventoryListing[RandomUtils.nextInt(0, inventoryListing.size())]
      final audit = auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      final exceptionCode = AuditExceptionTestDataLoader.randomExceptionCode()
      final exception = new AuditExceptionCreateDTO([inventory: new SimpleLegacyIdentifiableDTO(inventoryItem), exceptionCode: exceptionCode])

      when:
      def result = post("/audit/${audit.id}/exception", exception)

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.timeCreated != null
      result.timeUpdated != null
      result.scanArea == null
      result.barcode == inventoryItem.barcode
      result.productCode == inventoryItem.productCode
      result.altId == inventoryItem.altId
      result.serialNumber == inventoryItem.serialNumber
      result.inventoryBrand == inventoryItem.brand
      result.inventoryModel == inventoryItem.modelNumber
      result.exceptionCode == exceptionCode
      result.approved == false
      result.notes.size() == 0
      result.audit.id == audit.id
   }

   void "create invalid audit exception with non-exist scan area" () {
      given:
      final nonExistentAreaId = UUID.randomUUID()
      final locale = Locale.US
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 25, sortBy: "id", sortDirection: "ASC", storeNumber: store.number, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), company, locale).elements
      final inventoryItem = inventoryListing[RandomUtils.nextInt(0, inventoryListing.size())]
      final audit = auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      final exceptionCode = AuditExceptionTestDataLoader.randomExceptionCode()
      final exception = new AuditExceptionCreateDTO([inventory: new SimpleLegacyIdentifiableDTO(inventoryItem), scanArea: new SimpleIdentifiableDTO(nonExistentAreaId), exceptionCode: exceptionCode])

      when:
      post("/audit/${audit.id}/exception", exception)

      then:
      final ex = thrown(HttpClientResponseException)
      ex.status == BAD_REQUEST
      final response = ex.response.bodyAsJson()
      response.size() == 1
      new ErrorDTO(response[0].message, response[0].code, response[0].path) == new ErrorDTO("$nonExistentAreaId was unable to be found", 'system.not.found', 'audit.scanArea.id')
   }

   void "create invalid audit exception" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final audit = auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      final exception = new AuditExceptionCreateDTO()

      when:
      post("/audit/${audit.id}/exception", exception)

      then:
      final e = thrown(HttpClientResponseException)
      e.status == BAD_REQUEST
      final response = e.response.bodyAsJson()
      response.size() == 2
      response.collect { new ErrorDTO(it.message, it.code, it.path) }.sort { o1, o2 -> o1 <=> o2 } == [
         new ErrorDTO("Cannot be blank", "javax.validation.constraints.NotBlank.message", "exceptionCode"),
         new ErrorDTO("Is required","javax.validation.constraints.NotNull.message", "exceptionCode"),
      ].sort { o1, o2 -> o1 <=> o2 }
   }

   void "create audit exception where audit is not found" () {
      given:
      final nonExistentAuditId = UUID.randomUUID()
      final locale = Locale.US
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 25, sortBy: "id", sortDirection: "ASC", storeNumber: store.number, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), company, locale).elements
      final inventoryItem = inventoryListing[RandomUtils.nextInt(0, inventoryListing.size())]
      final exceptionCode = AuditExceptionTestDataLoader.randomExceptionCode()
      final scanArea = auditScanAreaFactoryService.single('Custom Area', store, company)

      when:
      post("/audit/$nonExistentAuditId/exception", new AuditExceptionCreateDTO([inventory: new SimpleLegacyIdentifiableDTO(inventoryItem.id), scanArea: new SimpleIdentifiableDTO(scanArea), exceptionCode: exceptionCode]))

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NOT_FOUND
      final notFoundResponse = notFoundException.response.bodyAsJson()
      new ErrorDTO(notFoundResponse.message, notFoundResponse.code, notFoundResponse.path) == new ErrorDTO("$nonExistentAuditId was unable to be found", 'system.not.found', null)
   }

   void "create audit exception where inventory id is null" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final audit = auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      final exceptionCode = AuditExceptionTestDataLoader.randomExceptionCode()

      when:
      post("/audit/${audit.id}/exception", new AuditExceptionCreateDTO([inventory: new SimpleLegacyIdentifiableDTO(null as Long), exceptionCode: exceptionCode]))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response.collect { new ErrorDTO(it.message, it.code, it.path) }.sort { o1, o2 -> o1 <=> o2 } == [
         new ErrorDTO("Is required", "javax.validation.constraints.NotNull.message", "inventory.id"),
      ].sort { o1, o2 -> o1 <=> o2 }
   }

   void "create audit exception when audit is in state OPENED" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final audit = auditFactoryService.single(store)
      final locale = Locale.US
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 25, sortBy: "id", sortDirection: "ASC", storeNumber: store.number, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), company, locale).elements
      final inventoryItem = inventoryListing[RandomUtils.nextInt(0, inventoryListing.size())]
      final scanArea = auditScanAreaFactoryService.single("Custom Area", store, company)
      final exceptionCode = AuditExceptionTestDataLoader.randomExceptionCode()

      when:
      post("/audit/${audit.id}/exception", new AuditExceptionCreateDTO([inventory: new SimpleLegacyIdentifiableDTO(inventoryItem), scanArea: new SimpleIdentifiableDTO(scanArea), exceptionCode: exceptionCode]))

      then:
      final e = thrown(HttpClientResponseException)
      e.status == BAD_REQUEST
      final response = e.response.bodyAsJson()
      response.size() == 1
      response.collect { new ErrorDTO(it.message, it.code, it.path) } == [
         new ErrorDTO("Audit ${audit.id} must be In Progress to modify its exceptions", "cynergi.audit.must.be.in.progress.exception", "audit.status")
      ]
   }

   void "create audit exception where matching Inventory item wasn't found" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final audit = auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      final lookupKey = "12345689521028"
      final scanArea = auditScanAreaFactoryService.single("Custom Area", store, company)

      when:
      def result = post("/audit/${audit.id}/exception", new AuditExceptionCreateDTO([scanArea: new SimpleIdentifiableDTO(scanArea), barcode: lookupKey, exceptionCode: "Not found in inventory"]))

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.timeCreated != null
      result.timeUpdated != null
      result.lookupKey == lookupKey
      result.serialNumber == null
      result.exceptionCode == "Not found in inventory"
      result.inventoryBrand == null
      result.inventoryModel == null
      result.scanArea.name == scanArea.name
      result.scanArea.store.storeNumber == scanArea.store.number
      result.scanArea.store.name == scanArea.store.name
      result.scannedBy.number == 998
      result.scannedBy.lastName == 'man'
      result.scannedBy.firstNameMi == 'super'
      result.audit.id == audit.myId()
   }

   void "create audit exception where no inventory.id and no barcode is passed" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final scanArea = auditScanAreaFactoryService.single("Custom Area", store, company)
      final audit = auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)

      when:
      post("/audit/${audit.id}/exception", new AuditExceptionCreateDTO([scanArea: new SimpleIdentifiableDTO(scanArea), exceptionCode: "Not found in inventory"]))

      then:
      final e = thrown(HttpClientResponseException)
      e.status == BAD_REQUEST
      final response = e.response.bodyAsJson()
      response.size() == 1
      response.collect { new ErrorDTO(it.message, it.code, it.path) } == [
         new ErrorDTO("Must provide either an Inventory item or a barcode", "cynergi.audit.exception.inventory.or.barcode", "barcode")
      ]
   }

   void "create duplicate exception" () {
      given:
      final locale = Locale.US
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 25, sortBy: "id", sortDirection: "ASC", storeNumber: store.number, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), company, locale).elements
      final inventoryItem = inventoryListing[RandomUtils.nextInt(0, inventoryListing.size())]
      final inventoryLocationType = InventoryLocationFactory.findByValue(inventoryItem.locationType.value)
      final audit = auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      final exceptionCode = AuditExceptionTestDataLoader.randomExceptionCode()
      final auditException = auditExceptionRepository.insert(new AuditExceptionEntity(audit.id, new InventoryEntity(inventoryItem, store, store, inventoryLocationType), null, employee, exceptionCode))
      final exception = new AuditExceptionCreateDTO([inventory: new SimpleLegacyIdentifiableDTO(inventoryItem), exceptionCode: exceptionCode])

      when:
      post("/audit/${audit.id}/exception", exception)

      then:
      final ex = thrown(HttpClientResponseException)
      ex.status == BAD_REQUEST
   }

   void "update audit exception with a new note" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final audit = auditFactoryService.single(store)
      final warehouse = auditScanAreaFactoryService.warehouse(store, company)
      final savedAuditException = auditExceptionFactoryService.single(audit, warehouse, employee, false)
      final noteText = "Test Note"

      when:
      def result = put("/audit/${audit.myId()}/exception", new AuditExceptionUpdateDTO([id: savedAuditException.id, note: new AuditExceptionNoteValueObject([note: noteText])]))

      then:
      notThrown(HttpClientResponseException)
      result.id == savedAuditException.id
      OffsetDateTime.parse(result.timeCreated as String) == savedAuditException.timeCreated
      OffsetDateTime.parse(result.timeUpdated as String) == savedAuditException.timeUpdated
      new AuditScanAreaDTO(result.scanArea) == new AuditScanAreaDTO(savedAuditException.scanArea)
      result.barcode == savedAuditException.barcode
      result.productCode == savedAuditException.productCode
      result.altId == savedAuditException.altId
      result.serialNumber == savedAuditException.serialNumber
      result.inventoryBrand == savedAuditException.inventoryBrand
      result.inventoryModel == savedAuditException.inventoryModel
      result.exceptionCode == savedAuditException.exceptionCode
      new EmployeeValueObject(result.scannedBy) == new EmployeeValueObject(savedAuditException.scannedBy)
      result.approved == savedAuditException.approved
      result.notes.size() == 1
      result.notes[0].id != null
      result.notes[0].note == noteText
      result.audit.id == savedAuditException.audit.myId()
   }

   void "update approved audit exception with a new note" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final audit = auditFactoryService.single(store)
      final warehouse = auditScanAreaFactoryService.warehouse(store, company)
      final savedAuditException = auditExceptionFactoryService.single(audit, warehouse, employee, true)
      final noteText = "Test Note"

      when:
      put("/audit/${audit.myId()}/exception", new AuditExceptionUpdateDTO([id: savedAuditException.id, note: new AuditExceptionNoteValueObject([note: noteText])]))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].message == "Audit Exception ${savedAuditException.id} has already been Approved. No new notes allowed"
   }

   void "update audit exception to approved" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final audit = auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      final warehouse = auditScanAreaFactoryService.warehouse(store, company)
      final auditException = auditExceptionFactoryService.single(audit, warehouse, employee, false)

      when:
      def result = put("/audit/${audit.myId()}/exception", new AuditExceptionUpdateDTO([id: auditException.id, approved: true]))

      then:
      notThrown(HttpClientResponseException)
      result.id == auditException.id
      result.approved == true
   }

   void "update audit without note or approved" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final audit = auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      final warehouse = auditScanAreaFactoryService.warehouse(store, company)
      final auditException = auditExceptionFactoryService.single(audit, warehouse, employee, false)

      when:
      put("/audit/${audit.myId()}/exception", new AuditExceptionUpdateDTO([id: auditException.id, approved: null, note: null]))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].message == "Audit update requires either approved or a note"
   }

   void "update audit exception that has been approved" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final audit = auditFactoryService.single(store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.approved()] as Set)
      final warehouse = auditScanAreaFactoryService.warehouse(store, company)
      final auditException = auditExceptionFactoryService.single(audit, warehouse, employee, false)

      when:
      put("/audit/${audit.myId()}/exception", new AuditExceptionUpdateDTO([id: auditException.id, note: new AuditExceptionNoteValueObject([note: "Should fail to be added note"])]))

      then:
      def e = thrown(HttpClientResponseException)
      e.status == BAD_REQUEST
      final response = e.response.bodyAsJson()
      response.size() == 1
      response.collect { new ErrorDTO(it.message, it.code, it.path) } == [
         new ErrorDTO("Audit ${audit.id} has already been Approved. No new notes allowed", "cynergi.audit.has.been.approved.no.new.notes.allowed", null)
      ]
   }
}
