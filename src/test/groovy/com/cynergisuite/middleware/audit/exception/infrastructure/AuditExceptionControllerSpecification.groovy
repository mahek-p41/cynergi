package com.cynergisuite.middleware.audit.exception.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableValueObject
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaFactory
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaValueObject
import com.cynergisuite.middleware.audit.exception.AuditExceptionCreateValueObject
import com.cynergisuite.middleware.audit.exception.AuditExceptionFactory
import com.cynergisuite.middleware.audit.exception.AuditExceptionFactoryService
import com.cynergisuite.middleware.audit.exception.AuditExceptionUpdateValueObject
import com.cynergisuite.middleware.audit.exception.AuditExceptionValueObject
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNoteFactoryService
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNoteValueObject
import com.cynergisuite.middleware.audit.exception.note.infrastructure.AuditExceptionNoteRepository
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.employee.Employee
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.error.ErrorDataTransferObject
import com.cynergisuite.middleware.inventory.InventoryService
import com.cynergisuite.middleware.inventory.infrastructure.InventoryPageRequest
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import org.apache.commons.lang3.RandomUtils

import javax.inject.Inject
import java.time.OffsetDateTime

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static org.apache.commons.lang3.StringUtils.EMPTY

@MicronautTest(transactional = false)
class AuditExceptionControllerSpecification extends ControllerSpecificationBase {

   @Inject AuditExceptionFactoryService auditExceptionFactoryService
   @Inject AuditExceptionNoteRepository auditExceptionNoteRepository
   @Inject AuditExceptionNoteFactoryService auditExceptionNoteFactoryService
   @Inject AuditFactoryService auditFactoryService
   @Inject EmployeeRepository employeeRepository
   @Inject InventoryService inventoryService

   void "fetch one audit exception by id with no attached notes" () {
      given:
      final auditException = auditExceptionFactoryService.single()

      when:
      def result = get("/audit/exception/${auditException.id}")

      then:
      notThrown(HttpClientException)
      result.id == auditException.id
      result.timeCreated.with { OffsetDateTime.parse(it) } == auditException.timeCreated
      result.timeUpdated.with { OffsetDateTime.parse(it) } == auditException.timeUpdated
      result.barcode == auditException.barcode
      result.serialNumber == auditException.serialNumber
      result.exceptionCode == auditException.exceptionCode
      result.inventoryBrand == auditException.inventoryBrand
      result.inventoryModel == auditException.inventoryModel
      result.scanArea.value == auditException.scanArea?.value
      result.scanArea.description == auditException.scanArea?.description
      result.scannedBy.number == auditException.scannedBy.number
      result.scannedBy.lastName == auditException.scannedBy.lastName
      result.scannedBy.firstNameMi == auditException.scannedBy.firstNameMi
      result.notes.size() == 0
      result.audit.id == auditException.audit.myId()
   }

   void "fetch one audit exception with a single attached note" () {
      given:
      final auditNote = auditExceptionNoteFactoryService.single()

      when:
      def result = get("/audit/exception/${auditNote.auditException.myId()}")

      then:
      notThrown(HttpClientResponseException)
      result.id == auditNote.auditException.myId()
      result.notes.size() == 1
      result.notes[0].id == auditNote.id
      result.notes[0].note == auditNote.note
      result.notes[0].enteredBy.number == auditNote.enteredBy.number
      result.notes[0].timeCreated.with { OffsetDateTime.parse(it) } == auditNote.timeCreated
      result.notes[0].timeUpdated.with { OffsetDateTime.parse(it) } == auditNote.timeUpdated
   }

   void "fetch one audit exception with a two attached notes" () {
      given:
      final auditException = auditExceptionFactoryService.single()
      final auditNotes = auditExceptionNoteFactoryService.stream(2, auditException, authenticatedEmployee).map { new AuditExceptionNoteValueObject(it) }.sorted{ o1, o2 -> o1.id <=> o2.id  }.toList()
      final auditExceptionId = auditException.myId()

      when:
      def result = get("/audit/exception/${auditExceptionId}")

      then:
      notThrown(HttpClientResponseException)
      result.notes.size() == 2
      result.id == auditException.id
      result.audit.id == auditException.audit.myId()
      result.notes
         .each{ it['timeCreated'] = OffsetDateTime.parse(it['timeCreated']) }
         .each{ it['timeUpdated'] = OffsetDateTime.parse(it['timeUpdated']) }
         .collect { new AuditExceptionNoteValueObject(it) }.sort { o1, o2 -> o1.id <=> o2.id } == auditNotes
   }

   void "fetch all exceptions for a single audit" () {
      given:
      final audit = auditFactoryService.single()
      final twentyAuditDiscrepancies = auditExceptionFactoryService.stream(20, audit, authenticatedEmployee, null).map { new AuditExceptionValueObject(it, new AuditScanAreaValueObject(it.scanArea)) }.toList()
      final pageOne = new PageRequest(1, 5, "id", "ASC")
      final pageTwo = new PageRequest(2, 5, "id", "ASC")
      final pageFive = new PageRequest(5, 5, "id", "ASC")
      final firstFiveDiscrepancies = twentyAuditDiscrepancies[0..4]
      final secondFiveDiscrepancies = twentyAuditDiscrepancies[5..9]

      when:
      def pageOneResult = get("/audit/${audit.id}/exception$pageOne")

      then:
      notThrown(HttpClientResponseException)
      new PageRequest(pageOneResult.requested) == pageOne
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.elements.each{ it['audit'] = new SimpleIdentifiableValueObject(it.audit.id) }
         .each { it['timeCreated'] = OffsetDateTime.parse(it['timeCreated']) }
         .each { it['timeUpdated'] = OffsetDateTime.parse(it['timeUpdated']) }
         .collect { new AuditExceptionValueObject(it) } == firstFiveDiscrepancies

      when:
      def pageTwoResult = get("/audit/${audit.id}/exception$pageTwo")

      then:
      notThrown(HttpClientResponseException)
      new PageRequest(pageTwoResult.requested) == pageTwo
      pageTwoResult.elements != null
      pageTwoResult.elements.size() == 5
      pageTwoResult.elements.each{ it['audit'] = new SimpleIdentifiableValueObject(it.audit.id) }
         .each { it['timeCreated'] = OffsetDateTime.parse(it['timeCreated']) }
         .each { it['timeUpdated'] = OffsetDateTime.parse(it['timeUpdated']) }
         .collect { new AuditExceptionValueObject(it) } == secondFiveDiscrepancies

      when:
      get("/audit/${audit.id}/exception$pageFive")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NOT_FOUND
      final notFoundResult = notFoundException.response.bodyAsJson()
      notFoundResult.size() == 1
      notFoundResult.message == "Request with Page 5, Size 5, Sort By id and Sort Direction ASC produced no results"

      when:
      get("/audit/${audit.id + 1}/exception$pageOne")

      then:
      final auditNotFoundException = thrown(HttpClientResponseException)
      auditNotFoundException.status == NOT_FOUND
      final auditNotFoundResult = auditNotFoundException.response.bodyAsJson()
      auditNotFoundResult.size() == 1
      auditNotFoundResult.message == "${audit.id + 1} was unable to be found"
   }

   void "fetch all audit exceptions for a single audit where each exception has 2 notes attached" () {
      given:
      final pageOne = new PageRequest(1, 5, "id", "ASC")
      final audit = auditFactoryService.single()
      final twentyAuditDiscrepancies = auditExceptionFactoryService.stream(20, audit, authenticatedEmployee, null)
         .peek{ it.notes.addAll(auditExceptionNoteFactoryService.stream(2, it, authenticatedEmployee).toList()) } // create some notes and save them
         .map { new AuditExceptionValueObject(it, new AuditScanAreaValueObject(it.scanArea)) }
         .toList()
      final firstFiveDiscrepancies = twentyAuditDiscrepancies[0..4]

      when:
      def pageOneResult = get("/audit/${audit.id}/exception$pageOne")

      then:
      notThrown(HttpClientResponseException)
      new PageRequest(pageOneResult.requested) == pageOne
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      final pageOneAuditExceptions = pageOneResult.elements.each{ it['audit'] = new SimpleIdentifiableValueObject(it.audit.id) }
         .each { it['timeCreated'] = OffsetDateTime.parse(it['timeCreated']) }
         .each { it['timeUpdated'] = OffsetDateTime.parse(it['timeUpdated']) }
         .collect { new AuditExceptionValueObject(it) }
      pageOneAuditExceptions[0].notes.size() == 2
      pageOneAuditExceptions[0].notes[0].id == firstFiveDiscrepancies[0].notes[0].id
      pageOneAuditExceptions[0].notes[0].note == firstFiveDiscrepancies[0].notes[0].note
      pageOneAuditExceptions[0].notes[0].timeCreated.with { OffsetDateTime.parse(it) } == firstFiveDiscrepancies[0].notes[0].timeCreated
      pageOneAuditExceptions[0].notes[0].timeUpdated.with { OffsetDateTime.parse(it) } == firstFiveDiscrepancies[0].notes[0].timeUpdated
      pageOneAuditExceptions[0].notes[0].enteredBy.number == authenticatedEmployee.number
      pageOneAuditExceptions[0].notes[1].id == firstFiveDiscrepancies[0].notes[1].id
      pageOneAuditExceptions[0].notes[1].note == firstFiveDiscrepancies[0].notes[1].note
      pageOneAuditExceptions[0].notes[1].timeCreated.with { OffsetDateTime.parse(it) } == firstFiveDiscrepancies[0].notes[1].timeCreated
      pageOneAuditExceptions[0].notes[1].timeUpdated.with { OffsetDateTime.parse(it) } == firstFiveDiscrepancies[0].notes[1].timeUpdated
      pageOneAuditExceptions[0].notes[1].enteredBy.number == authenticatedEmployee.number

      pageOneAuditExceptions[1].notes.size() == 2
      pageOneAuditExceptions[1].notes[0].id == firstFiveDiscrepancies[1].notes[0].id
      pageOneAuditExceptions[1].notes[0].note == firstFiveDiscrepancies[1].notes[0].note
      pageOneAuditExceptions[1].notes[0].timeCreated.with { OffsetDateTime.parse(it) } == firstFiveDiscrepancies[1].notes[0].timeCreated
      pageOneAuditExceptions[1].notes[0].timeUpdated.with { OffsetDateTime.parse(it) } == firstFiveDiscrepancies[1].notes[0].timeUpdated
      pageOneAuditExceptions[1].notes[0].enteredBy.number == authenticatedEmployee.number
      pageOneAuditExceptions[1].notes[1].id == firstFiveDiscrepancies[1].notes[1].id
      pageOneAuditExceptions[1].notes[1].note == firstFiveDiscrepancies[1].notes[1].note
      pageOneAuditExceptions[1].notes[1].timeCreated.with { OffsetDateTime.parse(it) } == firstFiveDiscrepancies[1].notes[1].timeCreated
      pageOneAuditExceptions[1].notes[1].timeUpdated.with { OffsetDateTime.parse(it) } == firstFiveDiscrepancies[1].notes[1].timeUpdated
      pageOneAuditExceptions[1].notes[1].enteredBy.number == authenticatedEmployee.number

      pageOneAuditExceptions[2].notes.size() == 2
      pageOneAuditExceptions[2].notes[0].id == firstFiveDiscrepancies[2].notes[0].id
      pageOneAuditExceptions[2].notes[0].note == firstFiveDiscrepancies[2].notes[0].note
      pageOneAuditExceptions[2].notes[0].timeCreated.with { OffsetDateTime.parse(it) } == firstFiveDiscrepancies[2].notes[0].timeCreated
      pageOneAuditExceptions[2].notes[0].timeUpdated.with { OffsetDateTime.parse(it) } == firstFiveDiscrepancies[2].notes[0].timeUpdated
      pageOneAuditExceptions[2].notes[0].enteredBy.number == authenticatedEmployee.number
      pageOneAuditExceptions[2].notes[1].id == firstFiveDiscrepancies[2].notes[1].id
      pageOneAuditExceptions[2].notes[1].note == firstFiveDiscrepancies[2].notes[1].note
      pageOneAuditExceptions[2].notes[1].timeCreated.with { OffsetDateTime.parse(it) } == firstFiveDiscrepancies[2].notes[1].timeCreated
      pageOneAuditExceptions[2].notes[1].timeUpdated.with { OffsetDateTime.parse(it) } == firstFiveDiscrepancies[2].notes[1].timeUpdated
      pageOneAuditExceptions[2].notes[1].enteredBy.number == authenticatedEmployee.number

      pageOneAuditExceptions[3].notes.size() == 2
      pageOneAuditExceptions[3].notes[0].id == firstFiveDiscrepancies[3].notes[0].id
      pageOneAuditExceptions[3].notes[0].note == firstFiveDiscrepancies[3].notes[0].note
      pageOneAuditExceptions[3].notes[0].timeCreated.with { OffsetDateTime.parse(it) } == firstFiveDiscrepancies[3].notes[0].timeCreated
      pageOneAuditExceptions[3].notes[0].timeUpdated.with { OffsetDateTime.parse(it) } == firstFiveDiscrepancies[3].notes[0].timeUpdated
      pageOneAuditExceptions[3].notes[0].enteredBy.number == authenticatedEmployee.number
      pageOneAuditExceptions[3].notes[1].id == firstFiveDiscrepancies[3].notes[1].id
      pageOneAuditExceptions[3].notes[1].note == firstFiveDiscrepancies[3].notes[1].note
      pageOneAuditExceptions[3].notes[1].timeCreated.with { OffsetDateTime.parse(it) } == firstFiveDiscrepancies[3].notes[1].timeCreated
      pageOneAuditExceptions[3].notes[1].timeUpdated.with { OffsetDateTime.parse(it) } == firstFiveDiscrepancies[3].notes[1].timeUpdated
      pageOneAuditExceptions[3].notes[1].enteredBy.number == authenticatedEmployee.number

      pageOneAuditExceptions[4].notes.size() == 2
      pageOneAuditExceptions[4].notes[0].id == firstFiveDiscrepancies[4].notes[0].id
      pageOneAuditExceptions[4].notes[0].note == firstFiveDiscrepancies[4].notes[0].note
      pageOneAuditExceptions[4].notes[0].timeCreated.with { OffsetDateTime.parse(it) } == firstFiveDiscrepancies[4].notes[0].timeCreated
      pageOneAuditExceptions[4].notes[0].timeUpdated.with { OffsetDateTime.parse(it) } == firstFiveDiscrepancies[4].notes[0].timeUpdated
      pageOneAuditExceptions[4].notes[0].enteredBy.number == authenticatedEmployee.number
      pageOneAuditExceptions[4].notes[1].id == firstFiveDiscrepancies[4].notes[1].id
      pageOneAuditExceptions[4].notes[1].note == firstFiveDiscrepancies[4].notes[1].note
      pageOneAuditExceptions[4].notes[1].timeCreated.with { OffsetDateTime.parse(it) } == firstFiveDiscrepancies[4].notes[1].timeCreated
      pageOneAuditExceptions[4].notes[1].timeUpdated.with { OffsetDateTime.parse(it) } == firstFiveDiscrepancies[4].notes[1].timeUpdated
      pageOneAuditExceptions[4].notes[1].enteredBy.number == authenticatedEmployee.number
   }

   void "fetch all audit exceptions when more than one audit exists" () {
      given:
      final store = authenticatedEmployee.store
      final auditOne = auditFactoryService.single(store, authenticatedEmployee, [AuditStatusFactory.opened(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      final auditTwo = auditFactoryService.single(store, authenticatedEmployee, [AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set)
      final List<AuditExceptionValueObject> threeAuditDiscrepanciesAuditTwo = auditExceptionFactoryService.stream(3, auditTwo, authenticatedEmployee, null).map { new AuditExceptionValueObject(it, new AuditScanAreaValueObject(it.scanArea)) }.toList()

      when:
      def pageOneResult = get("/audit/${auditTwo.id}/exception")

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.elements.size() == 3
      pageOneResult.elements.each {it['audit'] = new SimpleIdentifiableValueObject(it.audit.id)}
         .each { it['timeCreated'] = OffsetDateTime.parse(it['timeCreated']) }
         .each { it['timeUpdated'] = OffsetDateTime.parse(it['timeUpdated']) }
         .collect { new AuditExceptionValueObject(it) }.toSorted {o1, o2 -> o2.id <=> o2.id } == threeAuditDiscrepanciesAuditTwo
   }

   void "fetch one audit exception by id not found" () {
      when:
      get("/audit/exception/0")

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.body().with { parseResponse(it) }
      response.size() == 1
      response.message == "0 was unable to be found"
   }

   void "create audit exception" () {
      given:
      final locale = Locale.US
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 25, sortBy: "id", sortDirection: "ASC", storeNumber: authenticatedEmployee.store.number, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), locale).elements
      final inventoryItem = inventoryListing[RandomUtils.nextInt(0, inventoryListing.size())]
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set)
      final scanArea = AuditScanAreaFactory.random()
      final exceptionCode = AuditExceptionFactory.randomExceptionCode()
      final exception = new AuditExceptionCreateValueObject([inventory: new SimpleIdentifiableValueObject(inventoryItem), scanArea: new AuditScanAreaValueObject(scanArea), exceptionCode: exceptionCode])

      when:
      def result = post("/audit/${audit.id}/exception", exception)

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.id > 0
      result.timeCreated != null
      result.timeUpdated != null
      result.scanArea.value == scanArea.value
      result.scanArea.description == scanArea.description
      result.barcode == inventoryItem.barcode
      result.productCode == inventoryItem.productCode
      result.altId == inventoryItem.altId
      result.serialNumber == inventoryItem.serialNumber
      result.inventoryBrand == inventoryItem.brand
      result.inventoryModel == inventoryItem.modelNumber
      result.exceptionCode == exceptionCode
      result.signedOff == false
      result.notes.size() == 0
      result.audit.id == audit.id
   }

   void "create audit exception using employee that doesn't have a first name" () {
      given:
      final noFirstNameGuy = employeeRepository.insert(new Employee(null, OffsetDateTime.now(), OffsetDateTime.now(), "int", 7890, "test", EMPTY, "7890", authenticatedEmployee.store, true, null))
      final authToken = loginEmployee(noFirstNameGuy)
      final locale = Locale.US
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 25, sortBy: "id", sortDirection: "ASC", storeNumber: authenticatedEmployee.store.number, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), locale).elements
      final inventoryItem = inventoryListing[RandomUtils.nextInt(0, inventoryListing.size())]
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set)
      final scanArea = AuditScanAreaFactory.random()
      final exceptionCode = AuditExceptionFactory.randomExceptionCode()
      final exception = new AuditExceptionCreateValueObject([inventory: new SimpleIdentifiableValueObject(inventoryItem), scanArea: new AuditScanAreaValueObject(scanArea), exceptionCode: exceptionCode])

      when:
      def result = post("/audit/${audit.id}/exception", exception, authToken)

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.id > 0
      result.timeCreated != null
      result.timeUpdated != null
      result.scanArea.value == scanArea.value
      result.scanArea.description == scanArea.description
      result.barcode == inventoryItem.barcode
      result.productCode == inventoryItem.productCode
      result.altId == inventoryItem.altId
      result.serialNumber == inventoryItem.serialNumber
      result.inventoryBrand == inventoryItem.brand
      result.inventoryModel == inventoryItem.modelNumber
      result.exceptionCode == exceptionCode
      result.signedOff == false
      result.notes.size() == 0
      result.audit.id == audit.id
   }

   void "create audit exception without scan area" () {
      given:
      final locale = Locale.US
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 25, sortBy: "id", sortDirection: "ASC", storeNumber: authenticatedEmployee.store.number, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), locale).elements
      final inventoryItem = inventoryListing[RandomUtils.nextInt(0, inventoryListing.size())]
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set)
      final exceptionCode = AuditExceptionFactory.randomExceptionCode()
      final exception = new AuditExceptionCreateValueObject([inventory: new SimpleIdentifiableValueObject(inventoryItem), exceptionCode: exceptionCode])

      when:
      def result = post("/audit/${audit.id}/exception", exception)

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.id > 0
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
      result.signedOff == false
      result.notes.size() == 0
      result.audit.id == audit.id
   }

   void "create invalid audit exception" () {
      given:
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set)
      final exception = new AuditExceptionCreateValueObject()

      when:
      post("/audit/${audit.id}/exception", exception)

      then:
      final e = thrown(HttpClientResponseException)
      e.status == BAD_REQUEST
      final response = e.response.bodyAsJson()
      response.size() == 2
      response.collect { new ErrorDataTransferObject(it) }.sort {o1, o2 -> o1 <=> o2 } == [
         new ErrorDataTransferObject("Cannot be blank", "exceptionCode"),
         new ErrorDataTransferObject("Is required", "exceptionCode"),
      ].sort { o1, o2 -> o1 <=> o2 }
   }

   void "create audit exception where audit is not found" () {
      given:
      final locale = Locale.US
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 25, sortBy: "id", sortDirection: "ASC", storeNumber: authenticatedEmployee.store.number, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), locale).elements
      final inventoryItem = inventoryListing[RandomUtils.nextInt(0, inventoryListing.size())]
      final exceptionCode = AuditExceptionFactory.randomExceptionCode()

      when:
      post("/audit/-1/exception", new AuditExceptionCreateValueObject([inventory: new SimpleIdentifiableValueObject(inventoryItem.id), exceptionCode: exceptionCode]))

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NOT_FOUND
      final notFoundResponse = notFoundException.response.bodyAsJson()
      new ErrorDataTransferObject(notFoundResponse) == new ErrorDataTransferObject("-1 was unable to be found", null)
   }

   void "create audit exception where inventory id is null" () {
      given:
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set)
      final exceptionCode = AuditExceptionFactory.randomExceptionCode()

      when:
      post("/audit/${audit.id}/exception", new AuditExceptionCreateValueObject([inventory: new SimpleIdentifiableValueObject(), exceptionCode: exceptionCode]))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response.collect { new ErrorDataTransferObject(it) } == [new ErrorDataTransferObject("Is required", "inventory.id") ]
   }

   void "create audit exception when audit is in state OPENED" () {
      given:
      final audit = auditFactoryService.single()
      final locale = Locale.US
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 25, sortBy: "id", sortDirection: "ASC", storeNumber: authenticatedEmployee.store.number, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), locale).elements
      final inventoryItem = inventoryListing[RandomUtils.nextInt(0, inventoryListing.size())]
      final scanArea = AuditScanAreaFactory.random().with { new AuditScanAreaValueObject(it) }
      final exceptionCode = AuditExceptionFactory.randomExceptionCode()

      when:
      post("/audit/${audit.id}/exception", new AuditExceptionCreateValueObject([inventory: new SimpleIdentifiableValueObject(inventoryItem), scanArea: scanArea, exceptionCode: exceptionCode]))

      then:
      final e = thrown(HttpClientResponseException)
      e.status == BAD_REQUEST
      final response = e.response.bodyAsJson()
      response.size() == 1
      response.collect { new ErrorDataTransferObject(it) } == [
         new ErrorDataTransferObject("Audit ${audit.id} must be In Progress to modify its exceptions", "audit.status")
      ]
   }

   void "create audit exception where matching Inventory item wasn't found" () {
      given:
      final barcode = "12345689521028"
      final scanArea = AuditScanAreaFactory.random().with { new AuditScanAreaValueObject(it) }
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set)

      when:
      def result = post("/audit/${audit.id}/exception", new AuditExceptionCreateValueObject([scanArea: scanArea, barcode: barcode, exceptionCode: "Not found in inventory"]))

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.id > 0
      result.timeCreated != null
      result.timeUpdated != null
      result.barcode == barcode
      result.serialNumber == null
      result.exceptionCode == "Not found in inventory"
      result.inventoryBrand == null
      result.inventoryModel == null
      result.scanArea.value == scanArea.value
      result.scanArea.description == scanArea.description
      result.scannedBy.number == authenticatedEmployee.number
      result.scannedBy.lastName == authenticatedEmployee.lastName
      result.scannedBy.firstNameMi == authenticatedEmployee.firstNameMi
      result.audit.id == audit.myId()
   }

   void "create audit exception where no inventory.id and no barcode is passed" () {
      given:
      final scanArea = AuditScanAreaFactory.random().with { new AuditScanAreaValueObject(it) }
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set)

      when:
      post("/audit/${audit.id}/exception", new AuditExceptionCreateValueObject([scanArea: scanArea, exceptionCode: "Not found in inventory"]))

      then:
      final e = thrown(HttpClientResponseException)
      e.status == BAD_REQUEST
      final response = e.response.bodyAsJson()
      response.size() == 1
      response.collect { new ErrorDataTransferObject(it) } == [
         new ErrorDataTransferObject("Must provide either an Inventory item or a barcode", "barcode")
      ]
   }

   void "update audit exception with a new note" () {
      given:
      final savedAuditException = auditExceptionFactoryService.single()
      final audit = savedAuditException.getAudit()
      final noteText = "Test Note"

      when:
      def result = put("/audit/${audit.myId()}/exception", new AuditExceptionUpdateValueObject([id: savedAuditException.id, note: new AuditExceptionNoteValueObject([note: noteText])]))

      then:
      notThrown(HttpClientResponseException)
      result.id == savedAuditException.id
      OffsetDateTime.parse(result.timeCreated as String) == savedAuditException.timeCreated
      OffsetDateTime.parse(result.timeUpdated as String) == savedAuditException.timeUpdated
      new AuditScanAreaValueObject(result.scanArea) == new AuditScanAreaValueObject(savedAuditException.scanArea)
      result.barcode == savedAuditException.barcode
      result.productCode == savedAuditException.productCode
      result.altId == savedAuditException.altId
      result.serialNumber == savedAuditException.serialNumber
      result.inventoryBrand == savedAuditException.inventoryBrand
      result.inventoryModel == savedAuditException.inventoryModel
      result.exceptionCode == savedAuditException.exceptionCode
      new EmployeeValueObject(result.scannedBy) == new EmployeeValueObject(savedAuditException.scannedBy)
      result.signedOff == savedAuditException.signedOff
      result.notes.size() == 1
      result.notes[0].id != null
      result.notes[0].id > 0
      result.notes[0].note == noteText
      result.audit.id == savedAuditException.audit.myId()
   }

   void "update audit exception that has been signed-off" () {
      given:
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress(), AuditStatusFactory.signedOff()] as Set)
      final auditException = auditExceptionFactoryService.single(audit)

      when:
      put("/audit/${audit.myId()}/exception", new AuditExceptionUpdateValueObject([id: auditException.id, note: new AuditExceptionNoteValueObject([note: "Should fail to be added note"])]))

      then:
      def e = thrown(HttpClientResponseException)
      e.status == BAD_REQUEST
      final response = e.response.bodyAsJson()
      response.size() == 1
      response.collect { new ErrorDataTransferObject(it) } == [
         new ErrorDataTransferObject("Audit ${audit.id} has already been Signed Off. No new notes allowed", null)
      ]
   }
}
