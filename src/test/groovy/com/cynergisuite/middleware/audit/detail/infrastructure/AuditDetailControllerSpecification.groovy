package com.cynergisuite.middleware.audit.detail.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.detail.AuditDetailCreateUpdateDTO
import com.cynergisuite.middleware.audit.detail.AuditDetailFactoryService
import com.cynergisuite.middleware.audit.detail.AuditDetailValueObject
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaDTO
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaFactoryService
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.error.ErrorDTO
import com.cynergisuite.middleware.inventory.InventoryService
import com.cynergisuite.middleware.inventory.infrastructure.InventoryPageRequest
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.apache.commons.lang3.RandomUtils

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NOT_MODIFIED
import static io.micronaut.http.HttpStatus.NO_CONTENT
import static java.util.Locale.US

@MicronautTest(transactional = false)
class AuditDetailControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/audit/detail"
   private static final Locale locale = US

   @Inject AuditDetailFactoryService auditDetailFactoryService
   @Inject AuditFactoryService auditFactoryService
   @Inject AuditScanAreaFactoryService auditScanAreaFactoryService
   @Inject InventoryService inventoryService

   void "fetch one audit detail by id" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final audit = auditFactoryService.single(store)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final storeWarehouse = auditScanAreaFactoryService.warehouse(store, company)
      final savedAuditDetail = auditDetailFactoryService.single(audit, storeWarehouse, employee)

      when:
      def result = get("$path/${savedAuditDetail.id}")

      then:
      notThrown(HttpClientException)
      result.id == savedAuditDetail.id
      result.scanArea.name == savedAuditDetail.scanArea.name
      result.scanArea.store.storeNumber == savedAuditDetail.scanArea.store.number
      result.scanArea.store.name == savedAuditDetail.scanArea.store.name
      result.lookupKey == savedAuditDetail.lookupKey
      result.barcode == savedAuditDetail.barcode
      result.serialNumber == savedAuditDetail.serialNumber
      result.inventoryBrand == savedAuditDetail.inventoryBrand
      result.inventoryModel == savedAuditDetail.inventoryModel
      result.scannedBy.number == savedAuditDetail.scannedBy.number
   }

   void "fetch all audit details related to an audit" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final audit = auditFactoryService.single(store)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final storeWarehouse = auditScanAreaFactoryService.warehouse(store, company)
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 50, sortBy: "id", sortDirection: "ASC", storeNumber: 3, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), company, locale).elements

      final twentyAuditDetails = auditDetailFactoryService.stream(20, audit, storeWarehouse, employee, inventoryListing).sorted { o1, o2 -> o1.id <=> o2.id }.map { new AuditDetailValueObject(it, new AuditScanAreaDTO(storeWarehouse)) }.toList()
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      final pageFive = new StandardPageRequest(5, 5, "id", "ASC")
      final firstFiveDetails = twentyAuditDetails[0..4]
      final secondFiveDetails = twentyAuditDetails[5..9]

      when:
      def pageOneResult = get("/audit/${audit.id}/detail${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      new StandardPageRequest(pageOneResult.requested) == pageOne
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == 20
      pageOneResult.elements.each {it['audit'] = new SimpleIdentifiableDTO(it.audit.id)}.collect {
         new AuditDetailValueObject(it)
      } == firstFiveDetails

      when:
      def pageTwoResult = get("/audit/${audit.id}/detail/${pageTwo}")

      then:
      notThrown(HttpClientResponseException)
      new StandardPageRequest(pageTwoResult.requested) == pageTwo
      pageTwoResult.elements != null
      pageTwoResult.elements.size() == 5
      pageTwoResult.elements.each {it['audit'] = new SimpleIdentifiableDTO(it.audit.id)}.collect {
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
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)

      final List<AuditEntity> audits = auditFactoryService.stream(2, store, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set).toList()
      final audit = audits[0]
      final secondAudit = audits[1]
      final storeShowroom = auditScanAreaFactoryService.showroom(store, company)
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 50, sortBy: "id", sortDirection: "ASC", storeNumber: 3, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), company, locale).elements
      final twelveAuditDetails = auditDetailFactoryService.stream(12, audit, storeShowroom, employee, inventoryListing[0..24]).sorted { o1, o2 -> o1.id <=> o2.id }.map { new AuditDetailValueObject(it, new AuditScanAreaDTO(storeShowroom)) }.toList()
      final firstTenDetails = twelveAuditDetails[0..9]
      auditDetailFactoryService.generate(12, secondAudit, employee, storeShowroom, inventoryListing[25..49])

      when:
      def result = get("/audit/${audit.id}/detail")

      then:
      notThrown(HttpClientResponseException)
      result.elements != null
      result.elements.size() == 10
      result.totalElements == 12
      result.elements.each{ it['audit'] = new SimpleIdentifiableDTO(it.audit.id) }.collect { new AuditDetailValueObject(it) } == firstTenDetails
   }

   void "fetch all audit details related to an audit where there are 2 different scan areas" () {
      given:
      final pageOne = new StandardPageRequest(1, 10, "ID", "ASC")
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(1, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final warehouse = auditScanAreaFactoryService.warehouse(store, company)
      final showroom = auditScanAreaFactoryService.showroom(store, company)
      final storeroom = auditScanAreaFactoryService.storeroom(store, company)
      final audit = auditFactoryService.single(employee, [AuditStatusFactory.created()] as Set)
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 50, sortBy: "id", sortDirection: "ASC", storeNumber: 1, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), company, locale).elements
      final auditDetailsWarehouse = auditDetailFactoryService.stream(11, audit, warehouse, employee, inventoryListing[0..19]).map { new AuditDetailValueObject(it, new AuditScanAreaDTO(warehouse)) }.toList()
      final auditDetailsShowroom = auditDetailFactoryService.stream(5, audit, showroom, employee, inventoryListing[20..30]).map { new AuditDetailValueObject(it, new AuditScanAreaDTO(showroom)) }.toList()
      final auditDetailsStoreroom = auditDetailFactoryService.stream(5, audit, storeroom, employee, inventoryListing[30..40]).map { new AuditDetailValueObject(it, new AuditScanAreaDTO(storeroom)) }.toList()

      when:
      def result = get("/audit/${audit.id}/detail${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      result.elements != null
      result.elements.size() == 10
      result.totalElements == 21
      result.totalPages == 3
      result.elements.each{ it['audit'] = new SimpleIdentifiableDTO(it.audit.id) }.collect { new AuditDetailValueObject(it) }.sort {o1, o2 -> o1.id <=> o2.id } == auditDetailsWarehouse[0..9]
   }

   void "fetch one audit detail by id not found" () {
      given:
      final nonExistentId = UUID.randomUUID()
      when:
      get("$path/$nonExistentId")

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      final response = exception.response.bodyAsJson()
      with(response) {
         message == "$nonExistentId was unable to be found"
         code == "system.not.found"
      }
   }

   void "create audit detail" () {
      given:
      final locale = Locale.US
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 25, sortBy: "id", sortDirection: "ASC", storeNumber: store.number, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), company, locale).elements
      final inventoryItem = inventoryListing[RandomUtils.nextInt(0, inventoryListing.size())]
      final audit = auditFactoryService.single(employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      final scanArea = auditScanAreaFactoryService.single("Custom Area", store, company)

      when:
      def result = post("/audit/${audit.id}/detail", new AuditDetailCreateUpdateDTO(new SimpleLegacyIdentifiableDTO(inventoryItem.id), new SimpleIdentifiableDTO(scanArea)))

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.scanArea.name == scanArea.name
      result.scanArea.store.storeNumber == scanArea.store.number
      result.scanArea.store.name == scanArea.store.name
      result.lookupKey == inventoryItem.lookupKey
      result.barcode == inventoryItem.barcode
      result.serialNumber == inventoryItem.serialNumber
      result.inventoryBrand == inventoryItem.brand
      result.inventoryModel == inventoryItem.modelNumber
      result.scannedBy.number == nineNineEightAuthenticatedEmployee.number
      result.audit.id == audit.id
   }

   void "create duplicate audit detail" () {
      given:
      final locale = Locale.US
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 25, sortBy: "id", sortDirection: "ASC", storeNumber: store.number, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), company, locale).elements
      final inventoryItem = inventoryListing[RandomUtils.nextInt(0, inventoryListing.size())]
      final audit = auditFactoryService.single(employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      final scanArea = auditScanAreaFactoryService.single("Custom Area", store, company)
      final storeWarehouse = auditScanAreaFactoryService.warehouse(store, company)
      final savedAuditDetail = auditDetailFactoryService.single(audit, storeWarehouse, employee, inventoryItem)

      when:
      def response = postForResponse("/audit/${audit.id}/detail", new AuditDetailCreateUpdateDTO(new SimpleLegacyIdentifiableDTO(inventoryItem.id), new SimpleIdentifiableDTO(scanArea)))

      then:
      notThrown(Exception)
      response.status == NOT_MODIFIED
      final result = response.bodyAsJson()
      result == null // 304 doesn't return a body
   }

   void "create invalid audit detail" () {
      given:
      final invalidId = UUID.randomUUID()
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final scanArea = auditScanAreaFactoryService.single("Custom Area", store, company)
      final audit = auditFactoryService.single(employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      final detail = new AuditDetailCreateUpdateDTO(new SimpleLegacyIdentifiableDTO([id: 9998]), new SimpleIdentifiableDTO([id: invalidId]))
      final secondDetail = new AuditDetailCreateUpdateDTO(new SimpleLegacyIdentifiableDTO([id: 9998]), new SimpleIdentifiableDTO(scanArea))

      when:
      post("/audit/${audit.id}/detail", detail)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == NOT_FOUND
      final response = exception.response.bodyAsJson()
      response.message == "9998 was unable to be found"

      when:
      post("/audit/${audit.id}/detail", secondDetail)

      then:
      final inventoryNotFoundException = thrown(HttpClientResponseException)
      inventoryNotFoundException.status == NOT_FOUND
      exception.status == NOT_FOUND
      response == exception.response.bodyAsJson()
      response.message == "9998 was unable to be found"
   }

   void "create audit detail when audit is in state OPENED (CREATED?)" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final def audit = auditFactoryService.single(store, employee, [AuditStatusFactory.created()] as Set)
      final locale = Locale.US
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 25, sortBy: "id", sortDirection: "ASC", storeNumber: store.number, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), company, locale).elements
      final inventoryItem = inventoryListing[RandomUtils.nextInt(0, inventoryListing.size())]
      final scanArea = auditScanAreaFactoryService.single("Custom Area", store, company)

      when:
      post("/audit/${audit.id}/detail", new AuditDetailCreateUpdateDTO(new SimpleLegacyIdentifiableDTO(inventoryItem.id), new SimpleIdentifiableDTO(scanArea)))

      then:
      final def exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final def response = exception.response.bodyAsJson()
      response.size() == 1
      response.collect { new ErrorDTO(it.message, it.code, it.path) } == [
         new ErrorDTO("Audit ${audit.id} must be In Progress to modify its details", "cynergi.audit.must.be.in.progress.details", "audit.status")
      ]
   }

   void "update audit detail" () {
      given:
      final locale = Locale.US
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 25, sortBy: "id", sortDirection: "ASC", storeNumber: store.number, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), company, locale).elements
      final inventoryItem = inventoryListing[RandomUtils.nextInt(0, inventoryListing.size())]
      final audit = auditFactoryService.single(employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      final showroom = auditScanAreaFactoryService.showroom(store, company)
      final warehouse = auditScanAreaFactoryService.warehouse(store, company)
      final existingAuditDetail = auditDetailFactoryService.single(audit, showroom)

      when:
      def result = put("/audit/${audit.id}/detail/${existingAuditDetail.id}", new AuditDetailCreateUpdateDTO(existingAuditDetail.myId(), new SimpleLegacyIdentifiableDTO(inventoryItem.id), new SimpleIdentifiableDTO(warehouse)))

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.scanArea.name == warehouse.name
      result.scanArea.store.storeNumber == warehouse.store.number
      result.scanArea.store.name == warehouse.store.name
      result.lookupKey == inventoryItem.lookupKey
      result.barcode == inventoryItem.barcode
      result.serialNumber == inventoryItem.serialNumber
      result.inventoryBrand == inventoryItem.brand
      result.inventoryModel == inventoryItem.modelNumber
      result.scannedBy.number == nineNineEightAuthenticatedEmployee.number
      result.audit.id == audit.id
   }

   void "update invalid audit detail" () {
      given:
      final locale = Locale.US
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)
      final inventoryListing = inventoryService.fetchAll(new InventoryPageRequest([page: 1, size: 25, sortBy: "id", sortDirection: "ASC", storeNumber: store.number, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]]), company, locale).elements
      final inventoryItem = inventoryListing[RandomUtils.nextInt(0, inventoryListing.size())]
      final inventoryItem2 = inventoryListing[RandomUtils.nextInt(0, inventoryListing.size())]
      final audit = auditFactoryService.single(employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      final showroom = auditScanAreaFactoryService.showroom(store, company)
      final warehouse = auditScanAreaFactoryService.warehouse(store, company)
      final existingAuditDetail = auditDetailFactoryService.single(audit, showroom, inventoryItem)
      auditDetailFactoryService.single(audit, showroom, inventoryItem2)

      when:
      put("/audit/${audit.id}/detail/${existingAuditDetail.id}", new AuditDetailCreateUpdateDTO(existingAuditDetail.myId(), new SimpleLegacyIdentifiableDTO(inventoryItem2.id), new SimpleIdentifiableDTO(warehouse)))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "inventory.lookupKey"
      response[0].message == "${inventoryItem2.lookupKey} already exists"
   }
}
