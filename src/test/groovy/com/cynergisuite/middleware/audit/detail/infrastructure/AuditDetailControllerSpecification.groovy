package com.cynergisuite.middleware.audit.detail.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableValueObject
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.AuditService
import com.cynergisuite.middleware.audit.detail.AuditDetailCreateValueObject
import com.cynergisuite.middleware.audit.detail.AuditDetailFactoryService
import com.cynergisuite.middleware.audit.detail.AuditDetailValueObject
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaFactory
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaFactoryService
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaValueObject
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.error.ErrorDataTransferObject
import com.cynergisuite.middleware.inventory.InventoryService
import com.cynergisuite.middleware.inventory.infrastructure.InventoryPageRequest
import com.cynergisuite.middleware.store.StoreFactoryService
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import org.apache.commons.lang3.RandomUtils

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class AuditDetailControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/audit/detail"

   @Inject AuditDetailFactoryService auditDetailFactoryService
   @Inject AuditFactoryService auditFactoryService
   @Inject AuditService auditService
   @Inject AuditScanAreaFactoryService auditScanAreaFactoryService
   @Inject EmployeeFactoryService employeeFactoryService
   @Inject InventoryService inventoryService
   @Inject StoreFactoryService storeFactoryService

   void "fetch one audit detail by id" () {
      given:
      final savedAuditDetail = auditDetailFactoryService.single()

      when:
      def result = get("$path/${savedAuditDetail.id}")

      then:
      notThrown(HttpClientException)
      result.id == savedAuditDetail.id
      result.scanArea.value == savedAuditDetail.scanArea.value
      result.scanArea.description == savedAuditDetail.scanArea.description
      result.barcode == savedAuditDetail.barcode
      result.serialNumber == savedAuditDetail.serialNumber
      result.inventoryBrand == savedAuditDetail.inventoryBrand
      result.inventoryModel == savedAuditDetail.inventoryModel
      result.scannedBy.number == savedAuditDetail.scannedBy.number
   }

   void "fetch all audit details related to an audit" () {
      given:
      final employee = employeeFactoryService.single()
      final audit = auditFactoryService.single()
      final twentyAuditDetails = auditDetailFactoryService.stream(20, audit, employee, null).sorted { o1, o2 -> o1.id <=> o2.id }.map { new AuditDetailValueObject(it, new AuditScanAreaValueObject(it.scanArea, it.scanArea.description)) }.toList()
      final pageOne = new PageRequest(1, 5, "id", "ASC")
      final pageTwo = new PageRequest(2, 5, "id", "ASC")
      final pageFive = new PageRequest(5, 5, "id", "ASC")
      final firstFiveDetails = twentyAuditDetails[0..4]
      final secondFiveDetails = twentyAuditDetails[5..9]

      when:
      def pageOneResult = get("/audit/${audit.id}/detail${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      new PageRequest(pageOneResult.requested) == pageOne
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == 20
      pageOneResult.elements.each {it['audit'] = new SimpleIdentifiableValueObject(it.audit.id)}.collect {
         new AuditDetailValueObject(it)
      } == firstFiveDetails

      when:
      def pageTwoResult = get("/audit/${audit.id}/detail/${pageTwo}")

      then:
      notThrown(HttpClientResponseException)
      new PageRequest(pageTwoResult.requested) == pageTwo
      pageTwoResult.elements != null
      pageTwoResult.elements.size() == 5
      pageTwoResult.elements.each {it['audit'] = new SimpleIdentifiableValueObject(it.audit.id)}.collect {
         new AuditDetailValueObject(it)
      } == secondFiveDetails

      when:
      get("/audit/${audit.id}/detail${pageFive}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "fetch all audit details related to an audit where there are 2 audits both have details" () {
      given:
      final employee = employeeFactoryService.single()
      final store = storeFactoryService.store(1)
      final List<AuditEntity> audits = auditFactoryService.stream(2, store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set).toList()
      final audit = audits[0]
      final secondAudit = audits[1]
      final twelveAuditDetails = auditDetailFactoryService.stream(12, audit, employee, null).sorted { o1, o2 -> o1.id <=> o2.id }.map { new AuditDetailValueObject(it, new AuditScanAreaValueObject(it.scanArea, it.scanArea.description)) }.toList()
      final firstTenDetails = twelveAuditDetails[0..9]
      auditDetailFactoryService.stream(12, secondAudit, employee, null).sorted { o1, o2 -> o1.id <=> o2.id }.map { new AuditDetailValueObject(it, new AuditScanAreaValueObject(it.scanArea, it.scanArea.description)) }.toList()

      when:
      def result = get("/audit/${audit.id}/detail")

      then:
      notThrown(HttpClientResponseException)
      result.elements != null
      result.elements.size() == 10
      result.totalElements == 12
      result.elements.each{ it['audit'] = new SimpleIdentifiableValueObject(it.audit.id) }.collect { new AuditDetailValueObject(it) } == firstTenDetails
   }

   void "fetch all audit details related to an audit where there are 2 different scan areas" () {
      given:
      final pageOne = new PageRequest(1, 10, "ID", "ASC")
      final employee = employeeFactoryService.single()
      final store = storeFactoryService.store(1)
      final warehouse = auditScanAreaFactoryService.warehouse()
      final showroom = auditScanAreaFactoryService.showroom()
      final storeroom = auditScanAreaFactoryService.storeroom()
      final audit = auditFactoryService.single(store, employee, [AuditStatusFactory.created()] as Set)
      final auditDetailsWarehouse = auditDetailFactoryService.stream(11, audit, employee, warehouse).map { new AuditDetailValueObject(it, new AuditScanAreaValueObject(it.scanArea, it.scanArea.description)) }.toList()
      final auditDetailsShowroom = auditDetailFactoryService.stream(5, audit, employee, showroom).map { new AuditDetailValueObject(it, new AuditScanAreaValueObject(it.scanArea, it.scanArea.description)) }.toList()
      final auditDetailsStoreroom = auditDetailFactoryService.stream(5, audit, employee, storeroom).map { new AuditDetailValueObject(it, new AuditScanAreaValueObject(it.scanArea, it.scanArea.description)) }.toList()

      when:
      def result = get("/audit/${audit.id}/detail${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      result.elements != null
      result.elements.size() == 10
      result.totalElements == 21
      result.totalPages == 3
      result.elements.each{ it['audit'] = new SimpleIdentifiableValueObject(it.audit.id) }.collect { new AuditDetailValueObject(it) }.sort { o1, o2 -> o1.id <=> o2.id } == auditDetailsWarehouse[0..9]
   }

   void "fetch one audit detail by id not found" () {
      when:
      get("$path/0")

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "0 was unable to be found"
   }

   void "create audit detail" () {
      given:
      final locale = Locale.US
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 25, sortBy: "id", sortDirection: "ASC", storeNumber: authenticatedEmployee.store.number, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), locale).elements
      final inventoryItem = inventoryListing[RandomUtils.nextInt(0, inventoryListing.size())]
      final audit = auditFactoryService.single([AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      final scanArea = AuditScanAreaFactory.random()

      when:
      def result = post("/audit/${audit.id}/detail", new AuditDetailCreateValueObject(new SimpleIdentifiableValueObject(inventoryItem.id), new AuditScanAreaValueObject(scanArea)))

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.id > 0
      result.scanArea.value == scanArea.value
      result.barcode == inventoryItem.barcode
      result.serialNumber == inventoryItem.serialNumber
      result.inventoryBrand == inventoryItem.brand
      result.inventoryModel == inventoryItem.modelNumber
      result.scannedBy.number == authenticatedEmployee.number
      result.audit.id == audit.id
   }

   void "create invalid audit detail" () {
      given:
      final scanArea = AuditScanAreaFactory.random()
      final audit = auditFactoryService.single([AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      final detail = new AuditDetailCreateValueObject(null, null)
      final secondDetail = new AuditDetailCreateValueObject(new SimpleIdentifiableValueObject([id: null]), new AuditScanAreaValueObject([value: null]))
      final thirdDetail = new AuditDetailCreateValueObject(new SimpleIdentifiableValueObject([id: 800000]), new AuditScanAreaValueObject(scanArea))

      when:
      post("/audit/${audit.id}/detail", detail)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 2
      response.collect { new ErrorDataTransferObject(it) }.sort {o1, o2 -> o1 <=> o2 } == [
         new ErrorDataTransferObject("Is required", "inventory"),
         new ErrorDataTransferObject("Is required", "scanArea"),
      ].sort { o1, o2 -> o1 <=> o2 }

      when:
      post("/audit/${audit.id}/detail", secondDetail)

      then:
      final secondException = thrown(HttpClientResponseException)
      secondException.status == BAD_REQUEST
      final secondResponse = secondException.response.bodyAsJson()
      secondResponse.size() == 2
      secondResponse.collect { new ErrorDataTransferObject(it) }.sort {o1, o2 -> o1 <=> o2 } == [
         new ErrorDataTransferObject("Is required", "inventory.id"),
         new ErrorDataTransferObject("Is required", "scanArea.value"),
      ].sort { o1, o2 -> o1 <=> o2 }

      when: // an unknown audit id
      post("/audit/${audit.id + 1}/detail", thirdDetail)

      then:
      final auditNotFoundException = thrown(HttpClientResponseException)
      auditNotFoundException.status == NOT_FOUND
      final auditNotFoundResponse = auditNotFoundException.response.bodyAsJson()
      new ErrorDataTransferObject(auditNotFoundResponse) == new ErrorDataTransferObject("${audit.id + 1} was unable to be found", null)

      when: // an unknown Inventory item
      post("/audit/${audit.id}/detail", thirdDetail)

      then:
      final inventoryNotFoundException = thrown(HttpClientResponseException)
      inventoryNotFoundException.status == BAD_REQUEST
      final inventoryNotFoundResponse = inventoryNotFoundException.response.bodyAsJson()
      inventoryNotFoundResponse.size() == 1
      inventoryNotFoundResponse.collect { new ErrorDataTransferObject(it) } == [new ErrorDataTransferObject("800,000 was unable to be found", "inventory.id") ]
   }

   void "create audit detail when audit is in state OPENED" () {
      given:
      final def audit = auditFactoryService.single()
      final locale = Locale.US
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 25, sortBy: "id", sortDirection: "ASC", storeNumber: authenticatedEmployee.store.number, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), locale).elements
      final inventoryItem = inventoryListing[RandomUtils.nextInt(0, inventoryListing.size())]
      final scanArea = AuditScanAreaFactory.random()

      when:
      post("/audit/${audit.id}/detail", new AuditDetailCreateValueObject(new SimpleIdentifiableValueObject(inventoryItem.id), new AuditScanAreaValueObject(scanArea)))

      then:
      final def exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final def response = exception.response.bodyAsJson()
      response.size() == 1
      response.collect { new ErrorDataTransferObject(it) } == [
         new ErrorDataTransferObject("Audit ${audit.id} must be In Progress to modify its details", "audit.status")
      ]
   }
}
