package com.cynergisuite.middleware.audit.detail.infrastructure


import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableValueObject
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.audit.Audit
import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.AuditService
import com.cynergisuite.middleware.audit.detail.AuditDetailFactory
import com.cynergisuite.middleware.audit.detail.AuditDetailFactoryService
import com.cynergisuite.middleware.audit.detail.AuditDetailValueObject
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaFactory
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaValueObject
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.error.ErrorValueObject
import com.cynergisuite.middleware.store.StoreFactoryService
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class AuditDetailControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/audit/detail"

   @Inject AuditService auditService
   @Inject AuditFactoryService auditFactoryService
   @Inject AuditDetailFactoryService auditDetailFactoryService
   @Inject EmployeeFactoryService employeeFactoryService
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
      result.barCode == savedAuditDetail.barCode
      result.inventoryId == savedAuditDetail.inventoryId
      result.inventoryBrand == savedAuditDetail.inventoryBrand
      result.inventoryModel == savedAuditDetail.inventoryModel
      result.scannedBy.number == savedAuditDetail.scannedBy.number
      result.inventoryStatus == savedAuditDetail.inventoryStatus
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
      notFoundException.status == NOT_FOUND
      final notFoundResult = notFoundException.response.bodyAsJson()
      notFoundResult.size() == 1
      notFoundResult.message == "Request with Page 5, Size 5, Sort By id and Sort Direction ASC produced no results"
   }

   void "fetch all audit details related to an audit where there are 2 audits both have details" () {
      given:
      final employee = employeeFactoryService.single()
      final store = storeFactoryService.store(1)
      final List<Audit> audits = auditFactoryService.stream(2, store, employee, [AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set).toList()
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
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set)
      final detail = AuditDetailFactory.single(audit, authenticatedEmployee)

      when:
      def result = post("/audit/${audit.id}/detail", new AuditDetailValueObject(detail, null, new AuditScanAreaValueObject(detail.scanArea)))

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.id > 0
      result.scanArea.value == detail.scanArea.value
      result.barCode == detail.barCode
      result.inventoryId == detail.inventoryId
      result.inventoryBrand == detail.inventoryBrand
      result.inventoryModel == detail.inventoryModel
      result.scannedBy.number == authenticatedEmployee.number
      result.inventoryStatus == detail.inventoryStatus
      result.audit.id == audit.id
   }

   void "create invalid audit detail" () {
      given:
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set)
      final detail = new AuditDetailValueObject(null, null, null, null, null, null, null, null, null)
      final secondDetail = AuditDetailFactory.single(new Audit(audit.id + 1, audit), authenticatedEmployee).with { new AuditDetailValueObject(it, null, new AuditScanAreaValueObject(it.scanArea)) }

      when:
      post("/audit/${audit.id}/detail", detail)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 11
      response.collect { new ErrorValueObject(it) }.sort { o1, o2 -> o1 <=> o2 } == [
         new ErrorValueObject("Cannot be blank", "inventoryStatus"),
         new ErrorValueObject("Is required", "inventoryStatus"),
         new ErrorValueObject("Is required", "barCode"),
         new ErrorValueObject("Cannot be blank", "barCode"),
         new ErrorValueObject("Cannot be blank", "inventoryBrand"),
         new ErrorValueObject("Is required", "inventoryBrand"),
         new ErrorValueObject("Cannot be blank", "inventoryId"),
         new ErrorValueObject("Is required", "inventoryId"),
         new ErrorValueObject("Cannot be blank", "inventoryModel"),
         new ErrorValueObject("Is required", "inventoryModel"),
         new ErrorValueObject("Is required", "scanArea"),
      ].sort { o1, o2 -> o1 <=> o2 }

      when:
      post("/audit/${audit.id + 1}/detail", secondDetail)

      then:
      final auditNotFoundException = thrown(HttpClientResponseException)
      auditNotFoundException.status == BAD_REQUEST
      final auditNotFoundResponse = auditNotFoundException.response.bodyAsJson()
      auditNotFoundResponse.size() == 1
      auditNotFoundResponse.collect { new ErrorValueObject(it) }.containsAll(
         [
            new ErrorValueObject("${audit.id + 1} was unable to be found", "audit.id")
         ]
      )
   }

   void "create audit detail when audit is in state OPENED" () {
      given:
      final def audit = auditFactoryService.single()
      final def detail = AuditDetailFactory.single(audit, authenticatedEmployee)

      when:
      post("/audit/${audit.id}/detail", new AuditDetailValueObject(detail, null, detail.scanArea))

      then:
      final def exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final def response = exception.response.bodyAsJson()
      response.size() == 1
      response.collect { new ErrorValueObject(it) }.containsAll(
         [
            new ErrorValueObject("Audit ${audit.id} must be In Progress to modify its details", "audit.status")
         ]
      )
   }

   void "update audit detail" () {
      given:
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set)
      final detail = auditDetailFactoryService.single(audit, authenticatedEmployee)
      final detailToUpdate = new AuditDetailValueObject(detail, null, detail.scanArea)

      when:
      detailToUpdate.inventoryStatus = "Missing"
      def result = put("/audit/${audit.id}/detail", detailToUpdate)

      then:
      notThrown(HttpClientResponseException)
      result.id == detail.id
      result.scanArea.value == detail.scanArea.value
      result.barCode == detail.barCode
      result.inventoryId == detail.inventoryId
      result.inventoryBrand == detail.inventoryBrand
      result.inventoryModel == detail.inventoryModel
      result.scannedBy.number == authenticatedEmployee.number
      result.inventoryStatus == "Missing"
   }

   void "update invalid audit detail" () {
      given:
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set)
      final detail = new AuditDetailValueObject(null, null, null, null, null, null, null, null, null)
      final secondDetail = AuditDetailFactory.single(new Audit(audit.id + 1, audit), authenticatedEmployee).with { new AuditDetailValueObject(it, null, it.scanArea) }

      when:
      put("/audit/${audit.id}/detail", detail)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 12
      response.collect {new ErrorValueObject(it)}.sort { o1, o2 -> o1 <=> o2 } == [
         new ErrorValueObject("Cannot be blank", "inventoryStatus"),
         new ErrorValueObject("Is required", "inventoryStatus"),
         new ErrorValueObject("Is required", "barCode"),
         new ErrorValueObject("Cannot be blank", "barCode"),
         new ErrorValueObject("Cannot be blank", "inventoryBrand"),
         new ErrorValueObject("Is required", "inventoryBrand"),
         new ErrorValueObject("Cannot be blank", "inventoryId"),
         new ErrorValueObject("Is required", "inventoryId"),
         new ErrorValueObject("Cannot be blank", "inventoryModel"),
         new ErrorValueObject("Is required", "inventoryModel"),
         new ErrorValueObject("Is required", "scanArea"),
         new ErrorValueObject("Is required", "scannedBy"),
      ].sort { o1, o2 -> o1 <=> o2 }

      when:
      put("/audit/${audit.id + 1}/detail", secondDetail)

      then:
      final def auditNotFoundException = thrown(HttpClientResponseException)
      auditNotFoundException.status == BAD_REQUEST
      final def auditNotFoundResponse = auditNotFoundException.response.bodyAsJson()
      auditNotFoundResponse.size() == 2
      auditNotFoundResponse.collect {new ErrorValueObject(it)}.sort {o1, o2 -> o1 <=> o2} == [
         new ErrorValueObject("${audit.id + 1} was unable to be found", "audit.id"),
         new ErrorValueObject("Is required", "id")
      ].sort { o1, o2 -> o1 <=> o2 }
   }

   void "update audit detail on completed audit" () {
      given:
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      final detail = auditDetailFactoryService.single(audit, authenticatedEmployee)
      final detailToUpdate = new AuditDetailValueObject(detail, null, detail.scanArea)

      when:
      detailToUpdate.inventoryStatus = "Still missing"
      put("/audit/${audit.id}/detail", detailToUpdate)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response.collect {new ErrorValueObject(it)} == [
         new ErrorValueObject("Audit ${audit.id} must be In Progress to modify its details", "audit.status")
      ]
   }

   void "update audit detail location" () {
      given:
      final showroomScanArea = AuditScanAreaFactory.showroom()
      final warehouseScanArea = AuditScanAreaFactory.warehouse()
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set)
      final detail = auditDetailFactoryService.single(audit, authenticatedEmployee, showroomScanArea)
      final detailToUpdate = new AuditDetailValueObject(detail, null, detail.scanArea)

      when:
      detailToUpdate.scanArea = new AuditScanAreaValueObject(warehouseScanArea)
      def result = put("/audit/${audit.id}/detail", detailToUpdate)

      then:
      notThrown(HttpClientResponseException)
      result.id == detail.id
      result.scanArea.value == warehouseScanArea.value
      result.barCode == detail.barCode
      result.inventoryId == detail.inventoryId
      result.inventoryBrand == detail.inventoryBrand
      result.inventoryModel == detail.inventoryModel
      result.scannedBy.number == authenticatedEmployee.number
      result.inventoryStatus == detail.inventoryStatus
   }
}
