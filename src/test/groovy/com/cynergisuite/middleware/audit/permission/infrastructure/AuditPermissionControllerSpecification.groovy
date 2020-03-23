package com.cynergisuite.middleware.audit.permission.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.permission.AuditPermissionCreateDataTransferObject
import com.cynergisuite.middleware.audit.permission.AuditPermissionFactoryService
import com.cynergisuite.middleware.audit.permission.AuditPermissionTypeFactory
import com.cynergisuite.middleware.department.DepartmentFactoryService
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.store.StoreFactoryService
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
   @Inject DepartmentFactoryService departmentFactoryService
   @Inject EmployeeFactoryService employeeFactoryService
   @Inject StoreFactoryService storeFactoryService

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
      result.totalElements == 25
      result.totalPages == 1
      result.first == true
      result.last == true
      result.elements.size() == 25
      result.elements[0].id == 1
      result.elements[0].value == "audit-fetchOne"
      result.elements[0].description == "Find audit by ID"
      result.elements[1].id == 2
      result.elements[1].value == "audit-fetchAll"
      result.elements[1].description == "List audits"
      result.elements[2].id == 3
      result.elements[2].value == "audit-fetchAllStatusCounts"
      result.elements[2].description == "List stats for audits"
      result.elements[3].id == 4
      result.elements[3].value == "audit-create"
      result.elements[3].description == "Create an audit"
      result.elements[4].id == 5
      result.elements[4].value == "audit-completeOrCancel"
      result.elements[4].description == "Complete or Cancel an audit"
      result.elements[5].id == 6
      result.elements[5].value == "audit-updateApproved"
      result.elements[5].description == "Update an audit's status"
      result.elements[6].id == 7
      result.elements[6].value == "audit-updateApprovedAllExceptions"
      result.elements[6].description == "Update an audit's status"
      result.elements[7].id == 8
      result.elements[7].value == "auditDetail-fetchOne"
      result.elements[7].description == "Find an audit inventory item by ID"
      result.elements[8].id == 9
      result.elements[8].value == "auditDetail-fetchAll"
      result.elements[8].description == "List audit inventory items"
      result.elements[9].id == 10
      result.elements[9].value == "auditDetail-save"
      result.elements[9].description == "Create a found inventory item"
      result.elements[10].id == 11
      result.elements[10].value == "auditException-fetchOne"
      result.elements[10].description == "Find an audit exception by ID"
      result.elements[11].id == 12
      result.elements[11].value == "auditException-fetchAll"
      result.elements[11].description == "List audit exceptions"
      result.elements[12].id == 13
      result.elements[12].value == "auditException-create"
      result.elements[12].description == "Create an audit exception"
      result.elements[13].id == 14
      result.elements[13].value == "auditException-update"
      result.elements[13].description == "Update an audit exception note or status"
      result.elements[14].id == 15
      result.elements[14].value == "auditException-approved"
      result.elements[14].description == "Allow user to approve an audit"
      result.elements[15].id == 16
      result.elements[15].value == "auditSchedule-fetchOne"
      result.elements[15].description == "Allow user to fetch a single audit schedule"
      result.elements[16].id == 17
      result.elements[16].value == "auditSchedule-fetchAll"
      result.elements[16].description == "Allow user to fetch all audit schedules"
      result.elements[17].id == 18
      result.elements[17].value == "auditSchedule-create"
      result.elements[17].description == "Allow user to create an audit schedule"
      result.elements[18].id == 19
      result.elements[18].value == "auditSchedule-update"
      result.elements[18].description == "Allow user to update an audit schedule"
      result.elements[19].id == 20
      result.elements[19].value == "auditPermission-fetchOne"
      result.elements[19].description == "Allow user to fetch a single audit permission"
      result.elements[20].id == 21
      result.elements[20].value == "auditPermission-fetchAll"
      result.elements[20].description == "Allow user to fetch a a listing of audit permissions"
      result.elements[21].id == 22
      result.elements[21].value == "auditPermissionType-fetchAll"
      result.elements[21].description == "Allow user to fetch a a listing of audit permission types"
      result.elements[22].id == 23
      result.elements[22].value == "auditPermission-create"
      result.elements[22].description == "Allow user to create an audit permission"
      result.elements[23].id == 24
      result.elements[23].value == "auditPermission-delete"
      result.elements[23].description == "Allow user to delete an audit permission"
      result.elements[24].id == 25
      result.elements[24].value == "audit-fetchAuditExceptionReport"
      result.elements[24].description == "Allow user to generate Audit Exception Report"
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
      def excludePermission = AuditPermissionTypeFactory.findByValue("auditPermission-fetchAll") // because we don't want to impede our ability to call the fetchAll endpoint right now
      def company = companyFactoryService.forDatasetCode("tstds1")
      def permissions = auditPermissionFactoryService.stream(21, company, excludePermission).toList()
      def firstPageRequest = new StandardPageRequest(1, 5, "id", "ASC")
      def notAPage = new StandardPageRequest(5, 10, "id", "ASC")

      when:
      def firstPage = get("/audit/permission${firstPageRequest}")

      then:
      notThrown(Exception)
      firstPage.requested.page == 1
      firstPage.requested.size == 5
      firstPage.requested.sortBy == "id"
      firstPage.requested.sortDirection == "ASC"
      firstPage.totalElements == 21
      firstPage.elements.size() == 5
      firstPage.elements[0].id == permissions[0].id
      firstPage.elements[0].type.id == permissions[0].type.id
      firstPage.elements[1].id == permissions[1].id
      firstPage.elements[1].type.id == permissions[1].type.id
      firstPage.elements[2].id == permissions[2].id
      firstPage.elements[2].type.id == permissions[2].type.id
      firstPage.elements[3].id == permissions[3].id
      firstPage.elements[3].type.id == permissions[3].type.id
      firstPage.elements[4].id == permissions[4].id
      firstPage.elements[4].type.id == permissions[4].type.id

      when:
      get("/audit/permission${notAPage}")

      then:
      final ex = thrown(HttpClientResponseException)
      ex.status == NO_CONTENT
   }

   void "fetch all permissions of a certain type" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final accountManager = departmentFactoryService.department('AM', company)
      final accountRep = departmentFactoryService.department('AR', company)
      final auditFetchOne = AuditPermissionTypeFactory.findByValue('audit-fetchOne')
      final auditFetchOneAccountManager = auditPermissionFactoryService.single(accountManager, auditFetchOne, company)
      final auditFetchAllAccountManager = auditPermissionFactoryService.single(accountManager, AuditPermissionTypeFactory.findByValue('audit-fetchAll'), company)
      final auditFetchAllStatusCountsAccountManager = auditPermissionFactoryService.single(accountManager, AuditPermissionTypeFactory.findByValue('audit-fetchAllStatusCounts'), company)
      final auditDetailFetchOneAccountRep = auditPermissionFactoryService.single(accountRep, auditFetchOne, company)
      final firstPageRequest = new StandardPageRequest(1, 3, "id", "ASC")

      when:
      def allFirstPage = get("/audit/permission/type/${auditFetchOne.id}/${firstPageRequest}")

      then:
      notThrown(HttpClientResponseException)
      allFirstPage.totalElements == 2
      allFirstPage.totalPages == 1
      allFirstPage.first == true
      allFirstPage.last == true
      allFirstPage.elements.size() == 2
      allFirstPage.elements[0].id == auditFetchOneAccountManager.id
      allFirstPage.elements[0].type.id == auditFetchOneAccountManager.type.id
      allFirstPage.elements[0].type.value == auditFetchOneAccountManager.type.value
      allFirstPage.elements[0].department.id == auditFetchOneAccountManager.department.id
      allFirstPage.elements[0].department.code == auditFetchOneAccountManager.department.code
      allFirstPage.elements[1].id == auditDetailFetchOneAccountRep.id
      allFirstPage.elements[1].type.id == auditDetailFetchOneAccountRep.type.id
      allFirstPage.elements[1].type.value == auditDetailFetchOneAccountRep.type.value
      allFirstPage.elements[1].department.id == auditDetailFetchOneAccountRep.department.id
      allFirstPage.elements[1].department.code == auditDetailFetchOneAccountRep.department.code
   }

   void "check association of audit-fetchOne with Sales Associate" () {
      given:
      final company = companyFactoryService.forDatasetCode("tstds1")
      final store = storeFactoryService.random(company)
      final salesAssociateDepartment = departmentFactoryService.department("SA", company)
      final deliveryDriverDepartment = departmentFactoryService.department("DE", company)
      final salesAssociate = employeeFactoryService.singleAuthenticated(company, store, salesAssociateDepartment)
      final deliveryDriver = employeeFactoryService.singleAuthenticated(company, store, deliveryDriverDepartment)
      final salesAssociateLogin = loginEmployee(salesAssociate)
      final deliveryDriverLogin = loginEmployee(deliveryDriver)
      final permissionType = AuditPermissionTypeFactory.findByValue("audit-fetchOne")
      final permission = auditPermissionFactoryService.single(salesAssociateDepartment, permissionType, company)
      final audit = auditFactoryService.single(store)

      when:
      def salesAssociateAudit = get("/audit/${audit.id}", salesAssociateLogin)

      then:
      notThrown(Exception)
      salesAssociateAudit.id == audit.id

      when:
      get("/audit/${audit.id}", deliveryDriverLogin)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == FORBIDDEN
   }

   void "associate audit-fetchOne with Sales Associate" () {
      given:
      final company = companyFactoryService.forDatasetCode("tstds1")
      final store = storeFactoryService.random(company)
      final salesAssociateDepartment = departmentFactoryService.department("SA", company)
      final deliveryDriverDepartment = departmentFactoryService.department("DE", company)
      final salesAssociate = employeeFactoryService.singleAuthenticated(company, store, salesAssociateDepartment)
      final deliveryDriver = employeeFactoryService.singleAuthenticated(company, store, deliveryDriverDepartment)
      final salesAssociateLogin = loginEmployee(salesAssociate)
      final deliveryDriverLogin = loginEmployee(deliveryDriver)
      final permissionType = AuditPermissionTypeFactory.findByValue("audit-fetchOne")
      final audit = auditFactoryService.single(store)

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
      def salesAssociateAudit = get("/audit/${audit.id}", salesAssociateLogin)

      then:
      notThrown(Exception)
      salesAssociateAudit.id == audit.id

      when:
      get("/audit/${audit.id}", deliveryDriverLogin)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == FORBIDDEN
   }

   void "delete association of audit-fetchOne with Sales Associate" () {
      given:
      final company = companyFactoryService.forDatasetCode("tstds1")
      final store = storeFactoryService.random(company)
      final salesAssociateDepartment = departmentFactoryService.department("SA", company)
      final deliveryDriverDepartment = departmentFactoryService.department("DE", company)
      final salesAssociate = employeeFactoryService.singleAuthenticated(company, store, salesAssociateDepartment)
      final deliveryDriver = employeeFactoryService.singleAuthenticated(company, store, deliveryDriverDepartment)
      final salesAssociateLogin = loginEmployee(salesAssociate)
      final deliveryDriverLogin = loginEmployee(deliveryDriver)
      final permissionType = AuditPermissionTypeFactory.findByValue("audit-fetchOne")
      final permission = auditPermissionFactoryService.single(salesAssociateDepartment, permissionType, company)
      final audit = auditFactoryService.single(store)

      when:
      def salesAssociateAudit = get("/audit/${audit.id}", salesAssociateLogin)

      then:
      notThrown(Exception)
      salesAssociateAudit.id == audit.id

      when:
      get("/audit/${audit.id}", deliveryDriverLogin)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == FORBIDDEN

      when:
      def deletedAudit = delete("/audit/permission/${permission.id}")

      then:
      notThrown(Exception)
      deletedAudit.id == permission.id
      deletedAudit.type.id == permissionType.id
      deletedAudit.type.value == permissionType.value
      deletedAudit.department.id == salesAssociateDepartment.id
      deletedAudit.department.code == salesAssociateDepartment.code

      when:
      def deliveryAuditResult = get("/audit/${audit.id}", deliveryDriverLogin)

      then:
      notThrown(Exception)
      deliveryAuditResult.id == audit.id
   }

   void "delete one by ID that doesn't exist" () {
      given:
      final company = companyFactoryService.forDatasetCode("tstds1")
      final department = departmentFactoryService.random(company)
      final permissionType = AuditPermissionTypeFactory.findByValue("audit-fetchOne")
      final permission = auditPermissionFactoryService.single(department, permissionType, company)

      when:
      delete("/audit/permission/${permission.id + 1}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == NOT_FOUND
      exception.response.bodyAsJson().message == "${permission.id + 1} was unable to be found"
   }
}
