package com.cynergisuite.middleware.audit.discrepancy.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableValueObject
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.audit.Audit
import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.discrepancy.AuditDiscrepancyFactory
import com.cynergisuite.middleware.audit.discrepancy.AuditDiscrepancyFactoryService
import com.cynergisuite.middleware.audit.discrepancy.AuditDiscrepancyValueObject
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.error.ErrorValueObject
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class AuditDiscrepancyControllerSpecification extends ControllerSpecificationBase {

   @Inject AuditFactoryService auditFactoryService
   @Inject AuditDiscrepancyFactoryService auditDiscrepancyFactoryService

   void "fetch one audit discrepancy by id" () {
      given:
      final savedAuditDiscrepancy = auditDiscrepancyFactoryService.single()

      when:
      def result = get("/audit/discrepancy/${savedAuditDiscrepancy.id}")

      then:
      notThrown(HttpClientException)
      result.id == savedAuditDiscrepancy.id
      result.barCode == savedAuditDiscrepancy.barCode
      result.inventoryId == savedAuditDiscrepancy.inventoryId
      result.inventoryBrand == savedAuditDiscrepancy.inventoryBrand
      result.inventoryModel == savedAuditDiscrepancy.inventoryModel
      result.scannedBy.number == savedAuditDiscrepancy.scannedBy.number
      result.scannedBy.lastName == savedAuditDiscrepancy.scannedBy.lastName
      result.scannedBy.firstNameMi == savedAuditDiscrepancy.scannedBy.firstNameMi
      result.audit.id == savedAuditDiscrepancy.audit.entityId()
   }

   void "fetch all discrepancies for a single audit" () {
      given:
      final audit = auditFactoryService.single()
      final twentyAuditDiscrepancies = auditDiscrepancyFactoryService.stream(20, audit, authenticatedEmployee).map { new AuditDiscrepancyValueObject(it) }.toList()
      final pageOne = new PageRequest(1, 5, "id", "ASC")
      final pageTwo = new PageRequest(2, 5, "id", "ASC")
      final pageFive = new PageRequest(5, 5, "id", "ASC")
      final firstFiveDiscrepancies = twentyAuditDiscrepancies[0..4]
      final secondFiveDiscrepancies = twentyAuditDiscrepancies[5..9]

      when:
      def pageOneResult = get("/audit/${audit.id}/discrepancy$pageOne")

      then:
      notThrown(HttpClientResponseException)
      new PageRequest(pageOneResult.requested) == pageOne
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.elements.each{ it['audit'] = new SimpleIdentifiableValueObject(it.audit.id) }.collect { new AuditDiscrepancyValueObject(it) }.containsAll(firstFiveDiscrepancies)

      when:
      def pageTwoResult = get("/audit/${audit.id}/discrepancy$pageTwo")

      then:
      notThrown(HttpClientResponseException)
      new PageRequest(pageTwoResult.requested) == pageTwo
      pageTwoResult.elements != null
      pageTwoResult.elements.size() == 5
      pageTwoResult.elements.each{ it['audit'] = new SimpleIdentifiableValueObject(it.audit.id) }.collect { new AuditDiscrepancyValueObject(it) }.containsAll(secondFiveDiscrepancies)

      when:
      get("/audit/${audit.id}/discrepancy$pageFive")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NOT_FOUND
      final notFoundResult = notFoundException.response.bodyAsJson()
      notFoundResult.size() == 1
      notFoundResult.message == "Request with Page 5, Size 5, Sort By id and Sort Direction ASC produced no results"

      when:
      get("/audit/${audit.id + 1}/discrepancy$pageOne")

      then:
      final auditNotFoundException = thrown(HttpClientResponseException)
      auditNotFoundException.status == NOT_FOUND
      final auditNotFoundResult = auditNotFoundException.response.bodyAsJson()
      auditNotFoundResult.size() == 1
      auditNotFoundResult.message == "${audit.id + 1} was unable to be found"
   }

   void "fetch all audit discrepancies when more than one audit exists" () {
      given:
      final store = authenticatedEmployee.store
      final auditOne = auditFactoryService.single(store, authenticatedEmployee, [AuditStatusFactory.opened(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      final auditOneDiscrepancy = auditDiscrepancyFactoryService.single(auditOne, authenticatedEmployee)
      final auditTwo = auditFactoryService.single(store, authenticatedEmployee, [AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set)
      final List<AuditDiscrepancyValueObject> threeAuditDiscrepanciesAuditTwo = auditDiscrepancyFactoryService.stream(3, auditTwo, authenticatedEmployee).map { new AuditDiscrepancyValueObject(it) }.toList()

      when:
      def pageOneResult = get("/audit/${auditTwo.id}/discrepancy")

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.elements.size() == 3
      pageOneResult.elements.each {it['audit'] = new SimpleIdentifiableValueObject(it.audit.id)}.collect { new AuditDiscrepancyValueObject(it) }.toSorted { o1, o2 -> o2.id <=> o2.id } == threeAuditDiscrepanciesAuditTwo
   }

   void "fetch one audit discrepancy by id not found" () {
      when:
      get("/audit/discrepancy/0")

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.body().with { parseResponse(it) }
      response.size() == 1
      response.message == "0 was unable to be found"
   }

   void "create audit discrepancy" () {
      given:
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set)
      final discrepancy = AuditDiscrepancyFactory.single(audit).with { new AuditDiscrepancyValueObject(it) }.each { it.audit = null; it.scannedBy = null }

      when:
      def result = post("/audit/${audit.id}/discrepancy", discrepancy)

      then:
      notThrown(HttpClientResponseException)
      result.id != null
      result.id > 0
      result.barCode == discrepancy.barCode
      result.inventoryId == discrepancy.inventoryId
      result.inventoryBrand == discrepancy.inventoryBrand
      result.inventoryModel == discrepancy.inventoryModel
      result.scannedBy.number == authenticatedEmployee.number
      result.notes == discrepancy.notes
      result.audit.id == audit.id
   }

   void "create invalid audit discrepancy" () {
      given:
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set)
      final discrepancy = new AuditDiscrepancyValueObject(null, null, null, null, null, null, null, null)
      final secondDiscrepancy = AuditDiscrepancyFactory.single(new Audit(audit.id + 1, audit), null).with { new AuditDiscrepancyValueObject(it) }.each { it.scannedBy = null; it.audit = null }

      when:
      post("/audit/${audit.id}/discrepancy", discrepancy)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 10
      response.collect { new ErrorValueObject(it) }.sort { o1, o2 -> o1 <=> o2 } == [
         new ErrorValueObject("Is required", "barCode"),
         new ErrorValueObject("Cannot be blank", "barCode"),
         new ErrorValueObject("Cannot be blank", "inventoryBrand"),
         new ErrorValueObject("Is required", "inventoryBrand"),
         new ErrorValueObject("Cannot be blank", "inventoryId"),
         new ErrorValueObject("Is required", "inventoryId"),
         new ErrorValueObject("Cannot be blank", "inventoryModel"),
         new ErrorValueObject("Is required", "inventoryModel"),
         new ErrorValueObject("Cannot be blank", "notes"),
         new ErrorValueObject("Is required", "notes"),
      ].sort { o1, o2 -> o1 <=> o2 }

      when:
      post("/audit/${audit.id + 1}/discrepancy", secondDiscrepancy)

      then:
      final auditNotFoundException = thrown(HttpClientResponseException)
      auditNotFoundException.status == BAD_REQUEST
      final auditNotFoundResponse = auditNotFoundException.response.bodyAsJson()
      auditNotFoundResponse.size() == 1
      auditNotFoundResponse.collect {new ErrorValueObject(it)} == [
         new ErrorValueObject("${audit.id + 1} was unable to be found", "audit.id")
      ]
   }

   void "create audit discrepancy when audit is in state OPENED" () {
      given:
      final audit = auditFactoryService.single()
      final discrepancy = AuditDiscrepancyFactory.single(audit).with { new AuditDiscrepancyValueObject(it) }.each { it.audit = null; it.scannedBy = null }

      when:
      post("/audit/${audit.id}/discrepancy", discrepancy)

      then:
      final def exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final def response = exception.response.bodyAsJson()
      response.size() == 1
      response.collect { new ErrorValueObject(it) }.containsAll(
         [
            new ErrorValueObject("Audit ${audit.id} must be In Progress to modify its discrepancies", "audit.status")
         ]
      )
   }

   void "update audit discrepancy"() {
      given:
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set)
      final discrepancy = auditDiscrepancyFactoryService.single().with { new AuditDiscrepancyValueObject(it) }.each { it.audit = null; it.scannedBy = null }

      when:
      discrepancy.notes = "Updated notes"
      def result = put("/audit/${audit.id}/discrepancy", discrepancy)

      then:
      notThrown(HttpClientResponseException)
      result.id == discrepancy.id
      result.barCode == discrepancy.barCode
      result.inventoryId == discrepancy.inventoryId
      result.inventoryBrand == discrepancy.inventoryBrand
      result.inventoryModel == discrepancy.inventoryModel
      result.scannedBy.number == authenticatedEmployee.number
      result.notes == "Updated notes"
      result.audit.id == audit.id
   }

   void "update invalid audit discrepancy" () {
      given:
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress()] as Set)
      final discrepancy = new AuditDiscrepancyValueObject(null, null, null, null, null, null, null, null)
      final secondDiscrepancy = AuditDiscrepancyFactory.single(new Audit(audit.id + 1, audit), null).with { new AuditDiscrepancyValueObject(it) }.each { it.scannedBy = null; it.audit = null }

      when:
      put("/audit/${audit.id}/discrepancy", discrepancy)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 10
      response.collect { new ErrorValueObject(it) }.sort { o1, o2 -> o1 <=> o2 } == [
         new ErrorValueObject("Is required", "barCode"),
         new ErrorValueObject("Cannot be blank", "barCode"),
         new ErrorValueObject("Cannot be blank", "inventoryBrand"),
         new ErrorValueObject("Is required", "inventoryBrand"),
         new ErrorValueObject("Cannot be blank", "inventoryId"),
         new ErrorValueObject("Is required", "inventoryId"),
         new ErrorValueObject("Cannot be blank", "inventoryModel"),
         new ErrorValueObject("Is required", "inventoryModel"),
         new ErrorValueObject("Cannot be blank", "notes"),
         new ErrorValueObject("Is required", "notes"),
      ].sort { o1, o2 -> o1 <=> o2 }

      when:
      put("/audit/${audit.id + 1}/discrepancy", secondDiscrepancy)

      then:
      final auditNotFoundException = thrown(HttpClientResponseException)
      auditNotFoundException.status == BAD_REQUEST
      final auditNotFoundResponse = auditNotFoundException.response.bodyAsJson()
      auditNotFoundResponse.size() == 2
      auditNotFoundResponse.collect { new ErrorValueObject(it) }.sort { o1, o2 -> o1 <=> o2 } == [
         new ErrorValueObject("${audit.id + 1} was unable to be found", "audit.id"),
         new ErrorValueObject("Is required", "id")
      ].sort { o1, o2 -> o1 <=> o2 }
   }

   void "update audit discrepancy on completed audit" () {
      given:
      final audit = auditFactoryService.single([AuditStatusFactory.opened(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      final discrepancy = auditDiscrepancyFactoryService.single().with { new AuditDiscrepancyValueObject(it) }.each { it.audit = null; it.scannedBy = null }

      when:
      discrepancy.notes = "Updated notes"
      put("/audit/${audit.id}/discrepancy", discrepancy)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response.collect {new ErrorValueObject(it)} == [
         new ErrorValueObject("Audit ${audit.id} must be In Progress to modify its discrepancies", "audit.status")
      ]
   }
}
