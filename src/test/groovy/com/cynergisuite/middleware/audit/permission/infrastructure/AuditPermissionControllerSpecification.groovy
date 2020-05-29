package com.cynergisuite.middleware.audit.permission.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDataTransferObject
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.permission.AuditPermissionCreateDataTransferObject
import com.cynergisuite.middleware.audit.permission.AuditPermissionFactoryService
import com.cynergisuite.middleware.audit.permission.AuditPermissionTypeFactory
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.FORBIDDEN
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class AuditPermissionControllerSpecification extends ControllerSpecificationBase {
   @Inject AuditFactoryService auditFactoryService
   @Inject AuditPermissionFactoryService auditPermissionFactoryService

   void "fetch all permission types" () {
      given:
      final pageRequest = new StandardPageRequest(1, 100, "id", "ASC")

      when:
      def result = get("/audit/permission/type${pageRequest}")

      then:
      notThrown(Exception)
      result.requested.page == 1
      result.requested.size == 100
      result.requested.sortBy == "id"
      result.requested.sortDirection == "ASC"
      result.totalElements == 2
      result.totalPages == 1
      result.first == true
      result.last == true
      result.elements.size() == 2
      result.elements[0].id == 1
      result.elements[0].value == "audit-approver"
      result.elements[0].description == "Approve Audits"
      result.elements[1].id == 2
      result.elements[1].value == "audit-permission-manager"
      result.elements[1].description == "Change Audit Permissions"
   }

   void "fetch one by ID" () {
      given:
      def company = companyFactoryService.forDatasetCode("tstds1")
      def department = departmentFactoryService.random(company)
      def permissionType = AuditPermissionTypeFactory.findByValue("audit-fetchOne")
      def permission = auditPermissionFactoryService.single(department, permissionType, company)

      when:
      def result = get("/audit/permission/${permission.id}")

      then:
      notThrown(Exception)
      result.id == permission.id
   }

   void "fetch one by ID that doesn't exist" () {
      given:
      def company = companyFactoryService.forDatasetCode("tstds1")
      def department = departmentFactoryService.random(company)
      def permissionType = AuditPermissionTypeFactory.findByValue("audit-fetchOne")
      def permission = auditPermissionFactoryService.single(department, permissionType, company)

      when:
      get("/audit/permission/${permission.id + 1}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == NOT_FOUND
      exception.response.bodyAsJson().message == "${permission.id + 1} was unable to be found"
   }

   void "fetch all permissions" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final assistantManager = departmentFactoryService.department('AM', company)
      final accountRep = departmentFactoryService.department('AR', company)
      final deliveryDriver = departmentFactoryService.department('DE', company)
      final auditApprover = AuditPermissionTypeFactory.findByValue('audit-approver')
      final auditPermissionManager = AuditPermissionTypeFactory.findByValue('audit-permission-manager')
      final assistantManagerAuditApprover = auditPermissionFactoryService.single(assistantManager, auditApprover, company)
      final assistantManagerAuditPermissionManager = auditPermissionFactoryService.single(assistantManager, auditPermissionManager, company)
      final accountRepAuditApprover = auditPermissionFactoryService.single(accountRep, auditApprover, company)
      final deliveryDriverAuditApprover = auditPermissionFactoryService.single(deliveryDriver, auditApprover, company)
      final firstPageRequest = new StandardPageRequest(1, 5, "id", "ASC")
      final notAPage = new StandardPageRequest(5, 10, "id", "ASC")

      when:
      def firstPage = get("/audit/permission${firstPageRequest}")

      then:
      notThrown(Exception)
      firstPage.requested.page == 1
      firstPage.requested.size == 5
      firstPage.requested.sortBy == "id"
      firstPage.requested.sortDirection == "ASC"
      firstPage.totalElements == 4
      firstPage.elements.size() == 4
      firstPage.elements[0].id == assistantManagerAuditApprover.id
      firstPage.elements[0].type.id == assistantManagerAuditApprover.type.id
      firstPage.elements[1].id == assistantManagerAuditPermissionManager.id
      firstPage.elements[1].type.id == assistantManagerAuditPermissionManager.type.id
      firstPage.elements[2].id == accountRepAuditApprover.id
      firstPage.elements[2].type.id == accountRepAuditApprover.type.id
      firstPage.elements[3].id == deliveryDriverAuditApprover.id
      firstPage.elements[3].type.id == deliveryDriverAuditApprover.type.id

      when:
      get("/audit/permission${notAPage}")

      then:
      final ex = thrown(HttpClientResponseException)
      ex.status == NO_CONTENT
   }

   void "fetch all permissions of a certain type" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final assistantManager = departmentFactoryService.department('AM', company)
      final accountRep = departmentFactoryService.department('AR', company)
      final deliveryDriver = departmentFactoryService.department('DE', company)
      final auditApprover = AuditPermissionTypeFactory.findByValue('audit-approver')
      final auditPermissionManager = AuditPermissionTypeFactory.findByValue('audit-permission-manager')
      final assistantManagerAuditApprover = auditPermissionFactoryService.single(assistantManager, auditApprover, company)
      final assistantManagerAuditPermissionManager = auditPermissionFactoryService.single(assistantManager, auditPermissionManager, company)
      final accountRepAuditApprover = auditPermissionFactoryService.single(accountRep, auditApprover, company)
      final deliveryDriverAuditApprover = auditPermissionFactoryService.single(deliveryDriver, auditApprover, company)
      final firstPageRequest = new StandardPageRequest(1, 5, "id", "ASC")

      when:
      def allFirstPage = get("/audit/permission/type/${auditApprover.id}/${firstPageRequest}")

      then:
      notThrown(HttpClientResponseException)
      allFirstPage.totalElements == 3
      allFirstPage.totalPages == 1
      allFirstPage.first == true
      allFirstPage.last == true
      allFirstPage.elements.size() == 3
      allFirstPage.elements[0].id == assistantManagerAuditApprover.id
      allFirstPage.elements[0].type.id == assistantManagerAuditApprover.type.id
      allFirstPage.elements[1].id == accountRepAuditApprover.id
      allFirstPage.elements[1].type.id == accountRepAuditApprover.type.id
      allFirstPage.elements[2].id == deliveryDriverAuditApprover.id
      allFirstPage.elements[2].type.id == deliveryDriverAuditApprover.type.id
   }

   void "check association of audit-permission-manager allows access and denies access to user with only audit-approver" () {
      given:
      final company = companyFactoryService.forDatasetCode("tstds1")
      final store = storeFactoryService.random(company)
      final assistantManager = departmentFactoryService.department('AM', company)
      final deliveryDriver = departmentFactoryService.department('DE', company)
      final auditApprover = AuditPermissionTypeFactory.findByValue('audit-approver')
      final assistantManagerAuditApprover = auditPermissionFactoryService.single(assistantManager, auditApprover, company)
      final assistantManagerEmployee = employeeFactoryService.singleAuthenticated(company, store, assistantManager)
      final deliveryDriverEmployee = employeeFactoryService.singleAuthenticated(company, store, deliveryDriver)
      final assistantManagerLogin = loginEmployee(assistantManagerEmployee)
      final deliveryDriverLogin = loginEmployee(deliveryDriverEmployee)
      final auditOne = auditFactoryService.single(store, assistantManagerEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      final auditTwo = auditFactoryService.single(store, assistantManagerEmployee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)

      when:
      def auditOneApproval = put("/audit/approve", new SimpleIdentifiableDataTransferObject(auditOne), assistantManagerLogin)

      then:
      notThrown(Exception)
      auditOneApproval.id == auditOne.id

      when:
      put("/audit/sign-off", new SimpleIdentifiableDataTransferObject(auditTwo), deliveryDriverLogin)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == FORBIDDEN
   }

   void "associate audit-approver with Sales Associate" () {
      given:
      final company = companyFactoryService.forDatasetCode("tstds1")
      final store = storeFactoryService.random(company)
      final salesAssociateDepartment = departmentFactoryService.department("SA", company)
      final deliveryDriverDepartment = departmentFactoryService.department("DE", company)
      final salesAssociate = employeeFactoryService.singleAuthenticated(company, store, salesAssociateDepartment)
      final deliveryDriver = employeeFactoryService.singleAuthenticated(company, store, deliveryDriverDepartment)
      final salesAssociateLogin = loginEmployee(salesAssociate)
      final deliveryDriverLogin = loginEmployee(deliveryDriver)
      final permissionType = AuditPermissionTypeFactory.findByValue("audit-approver")
      final auditOne = auditFactoryService.single(store, salesAssociate, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      final auditTwo = auditFactoryService.single(store, salesAssociate, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)

      when:
      def permission = post("/audit/permission", new AuditPermissionCreateDataTransferObject(permissionType, salesAssociateDepartment))

      then:
      notThrown(Exception)
      permission.id > 0
      permission.type.id == permissionType.id
      permission.type.value == permissionType.value
      permission.department.id == salesAssociateDepartment.id
      permission.department.code == salesAssociateDepartment.code

      when:
      def auditOneApproval = put("/audit/approve", new SimpleIdentifiableDataTransferObject(auditOne), salesAssociateLogin)

      then:
      notThrown(Exception)
      auditOneApproval.id == auditOne.id

      when:
      put("/audit/sign-off", new SimpleIdentifiableDataTransferObject(auditTwo), deliveryDriverLogin)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == FORBIDDEN
   }

   void "delete association of audit-approver with Sales Associate" () {
      given:
      final company = companyFactoryService.forDatasetCode("tstds1")
      final store = storeFactoryService.random(company)
      final salesAssociateDepartment = departmentFactoryService.department("SA", company)
      final deliveryDriverDepartment = departmentFactoryService.department("DE", company)
      final salesAssociate = employeeFactoryService.singleAuthenticated(company, store, salesAssociateDepartment)
      final salesAssociateLogin = loginEmployee(salesAssociate)
      final permissionType = AuditPermissionTypeFactory.findByValue("audit-approver")
      final permission = auditPermissionFactoryService.single(deliveryDriverDepartment, permissionType, company)
      final audit = auditFactoryService.single(store, salesAssociate, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)

      when:
      put("/audit/approve", new SimpleIdentifiableDataTransferObject(audit), salesAssociateLogin)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == FORBIDDEN

      when:
      def deletedAuditSignOffPermission = delete("/audit/permission/${permission.id}")

      then:
      notThrown(Exception)
      deletedAuditSignOffPermission.id == permission.id
      deletedAuditSignOffPermission.type.id == permissionType.id
      deletedAuditSignOffPermission.type.value == permissionType.value
      deletedAuditSignOffPermission.department.id == deliveryDriverDepartment.id
      deletedAuditSignOffPermission.department.code == deliveryDriverDepartment.code

      when:
      def salesAssociateSignOffAudit = put("/audit/approve", new SimpleIdentifiableDataTransferObject(audit), salesAssociateLogin)

      then:
      notThrown(Exception)
      salesAssociateSignOffAudit.id == audit.id
   }

   void "delete one by ID that doesn't exist" () {
      given:
      final company = companyFactoryService.forDatasetCode("tstds1")
      final department = departmentFactoryService.random(company)
      final permissionType = AuditPermissionTypeFactory.findByValue("audit-approver")
      final permission = auditPermissionFactoryService.single(department, permissionType, company)

      when:
      delete("/audit/permission/${permission.id + 1}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == NOT_FOUND
      exception.response.bodyAsJson().message == "${permission.id + 1} was unable to be found"
   }
}
