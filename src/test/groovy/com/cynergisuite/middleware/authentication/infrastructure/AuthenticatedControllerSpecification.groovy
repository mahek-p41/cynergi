package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.audit.permission.AuditPermissionTestDataLoaderService
import com.cynergisuite.middleware.audit.permission.AuditPermissionTypeTestDataLoader
import com.cynergisuite.middleware.authentication.LoginCredentials
import com.cynergisuite.middleware.employee.EmployeeTestDataLoaderService
import io.micronaut.core.type.Argument
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpRequest.HEAD
import static io.micronaut.http.HttpRequest.POST

@MicronautTest(transactional = false)
class AuthenticatedControllerSpecification extends ServiceSpecificationBase {
   @Inject @Client("/api") HttpClient httpClient
   @Inject AuditPermissionTestDataLoaderService auditPermissionFactoryService
   @Inject EmployeeTestDataLoaderService userSetupEmployeeFactoryService

   void "Get user permissions with dataset 1" () {
      given: 'Setup employee with audit-permission-manager permission'
      def company = companyFactoryService.forDatasetCode('coravt')
      def store = storeFactoryService.store(3, company)
      def department = departmentFactoryService.random(store.myCompany())
      def employee = employeeFactoryService.single(store, department)
      def permissionType = AuditPermissionTypeTestDataLoader.findByValue("audit-permission-manager")
      auditPermissionFactoryService.single(department, permissionType, company)
      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login", new LoginCredentials(employee.number.toString(), employee.passCode, employee.store.number, employee.company.datasetCode)),
            Argument.of(String),
            Argument.of(String)
         ).bodyAsJson()

      when:
      httpClient.toBlocking()
         .exchange(
            HEAD("/authenticated/check").header("Authorization", "Bearer ${authResponse.access_token}"),
            Argument.of(String),
            Argument.of(String)
         )

      then:
      notThrown(HttpClientResponseException)

      when:
      def response = httpClient.toBlocking()
         .exchange(
            GET("/authenticated").header("Authorization", "Bearer ${authResponse.access_token}"),
            Argument.of(String),
            Argument.of(String)
         ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      response.employeeNumber == "${employee.number}"
      response.storeNumber == 3
      response.company.with {
         clientCode = company.clientCode
         clientId = company.clientId
         datasetCode = company.datasetCode
         id = company.id
         name = company.name
         federalTaxNumber = null
      }
      response.permissions as Set ==  ["audit-approver", "audit-permission-manager"].toSet()

      and: 'user that has not been configured security level & is not cynergi admin'
      with(response.securityLevels) {
         accountPayableLevel == 0
         purchaseOrderLevel == 0
         generalLedgerLevel == 0
         systemAdministrationLevel == 0
         fileMaintenanceLevel == 0
         bankReconciliationLevel == 0
      }
   }

   void "Get user permissions with dataset 2" () {
      given: 'Setup audit-permission-manager permission assigned to other department'
      def tstds1 = companyFactoryService.forDatasetCode('coravt')
      def tstds2 = companyFactoryService.forDatasetCode('corrto')
      def permissionType = AuditPermissionTypeTestDataLoader.findByValue("audit-permission-manager")
      def otherDepartment = departmentFactoryService.department("AM", tstds2)
      auditPermissionFactoryService.single(otherDepartment, permissionType, tstds2)

      def store = storeFactoryService.store(3, tstds1)
      def department = departmentFactoryService.department("AM", store.myCompany())

      def employee = employeeFactoryService.single(store, department)

      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login", new LoginCredentials(employee.number.toString(), employee.passCode, employee.store.number, employee.company.datasetCode)),
            Argument.of(String),
            Argument.of(String)
         ).bodyAsJson()

      when:
      httpClient.toBlocking()
         .exchange(
            HEAD("/authenticated/check").header("Authorization", "Bearer ${authResponse.access_token}"),
            Argument.of(String),
            Argument.of(String)
         )

      then:
      notThrown(HttpClientResponseException)

      when:
      def response = httpClient.toBlocking()
         .exchange(
            GET("/authenticated").header("Authorization", "Bearer ${authResponse.access_token}"),
            Argument.of(String),
            Argument.of(String)
         ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      response.employeeNumber == "${employee.number}"
      response.storeNumber == 3
      response.company.with {
         clientCode = tstds1.clientCode
         clientId = tstds1.clientId
         datasetCode = tstds1.datasetCode
         id = tstds1.id
         name = tstds1.name
         federalTaxNumber = null
      }
      response.permissions as Set ==  ["audit-approver"].toSet()

      and: 'user that has not been configured security level & is not cynergi admin'
      with(response.securityLevels) {
         accountPayableLevel == 0
         purchaseOrderLevel == 0
         generalLedgerLevel == 0
         systemAdministrationLevel == 0
         fileMaintenanceLevel == 0
         bankReconciliationLevel == 0
      }
   }

   void "Get user security levels with cynergi admin" () {
      given: 'Setup super user'
      def company = companyFactoryService.forDatasetCode('coravt')
      def employee = userSetupEmployeeFactoryService.singleSuperUser(998, company, 'man', 'super', 'pass')
      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login", new LoginCredentials('998', employee.passCode, employee.store?.number, employee.company.datasetCode)),
            Argument.of(String),
            Argument.of(String)
         ).bodyAsJson()

      when:
      httpClient.toBlocking()
         .exchange(
            HEAD("/authenticated/check").header("Authorization", "Bearer ${authResponse.access_token}"),
            Argument.of(String),
            Argument.of(String)
         )

      then:
      notThrown(HttpClientResponseException)

      when:
      def response = httpClient.toBlocking()
         .exchange(
            GET("/authenticated").header("Authorization", "Bearer ${authResponse.access_token}"),
            Argument.of(String),
            Argument.of(String)
         ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      response.employeeNumber == "${employee.number}"
      response.storeNumber == 9000
      response.company.with {
         clientCode = company.clientCode
         clientId = company.clientId
         datasetCode = company.datasetCode
         id = company.id
         name = company.name
         federalTaxNumber = null
      }
      response.permissions as Set ==  ["audit-approver", "audit-permission-manager", "cynergi-system-admin"].toSet()

      with(response.securityLevels) {
         accountPayableLevel == 99
         purchaseOrderLevel == 99
         generalLedgerLevel == 99
         systemAdministrationLevel == 99
         fileMaintenanceLevel == 99
         bankReconciliationLevel == 99
      }
   }

   void "Get user security levels with user has config in operator_vw" () {
      given: 'Login user number 200'
      def company = companyFactoryService.forDatasetCode('coravt')
      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login", new LoginCredentials("200", "54321", 1, company.datasetCode)),
            Argument.of(String),
            Argument.of(String)
         ).bodyAsJson()

      when:
      httpClient.toBlocking()
         .exchange(
            HEAD("/authenticated/check").header("Authorization", "Bearer ${authResponse.access_token}"),
            Argument.of(String),
            Argument.of(String)
         )

      then:
      notThrown(HttpClientResponseException)

      when:
      def response = httpClient.toBlocking()
         .exchange(
            GET("/authenticated").header("Authorization", "Bearer ${authResponse.access_token}"),
            Argument.of(String),
            Argument.of(String)
         ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      response.employeeNumber == "200"
      response.storeNumber == 1
      response.company.with {
         clientCode = company.clientCode
         clientId = company.clientId
         datasetCode = company.datasetCode
         id = company.id
         name = company.name
         federalTaxNumber = null
      }
      response.permissions as Set ==  ["audit-approver", "audit-permission-manager"].toSet()
      with(response.securityLevels) {
         accountPayableLevel == 99
         purchaseOrderLevel == 99
         generalLedgerLevel == 99
         systemAdministrationLevel == 99
         fileMaintenanceLevel == 99
         bankReconciliationLevel == 99
      }
   }

}
