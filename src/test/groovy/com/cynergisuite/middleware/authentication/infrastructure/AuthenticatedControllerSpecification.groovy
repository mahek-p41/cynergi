package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.audit.permission.AuditPermissionFactoryService
import com.cynergisuite.middleware.audit.permission.AuditPermissionTypeFactory
import com.cynergisuite.middleware.authentication.LoginCredentials
import io.micronaut.core.type.Argument
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpRequest.HEAD
import static io.micronaut.http.HttpRequest.POST

@MicronautTest(transactional = false)
class AuthenticatedControllerSpecification extends ServiceSpecificationBase {
   @Inject @Client("/api") RxHttpClient httpClient
   @Inject AuditPermissionFactoryService auditPermissionFactoryService

   void "Get user permissions with dataset 1" () {
      given: 'Setup employee with audit-permission-manager permission'
      def company = companyFactoryService.forDatasetCode('tstds1')
      def store = storeFactoryService.store(3, company)
      def department = departmentFactoryService.random(store.myCompany())
      def employee = employeeFactoryService.single(store, department)
      def permissionType = AuditPermissionTypeFactory.findByValue("audit-permission-manager")
      auditPermissionFactoryService.single(department, permissionType, company)
      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login", new LoginCredentials(employee.number.toString(), employee.passCode, employee.store.number, employee.company.myDataset())),
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
   }

   void "Get user permissions with dataset 2" () {
      given: 'Setup audit-permission-manager permission assigned to other department'
      def tstds1 = companyFactoryService.forDatasetCode('tstds1')
      def tstds2 = companyFactoryService.forDatasetCode('tstds2')
      def permissionType = AuditPermissionTypeFactory.findByValue("audit-permission-manager")
      def otherDepartment = departmentFactoryService.department("AM", tstds2)
      auditPermissionFactoryService.single(otherDepartment, permissionType, tstds2)

      def store = storeFactoryService.store(3, tstds1)
      def department = departmentFactoryService.department("AM", store.myCompany())

      def employee = employeeFactoryService.single(store, department)

      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login", new LoginCredentials(employee.number.toString(), employee.passCode, employee.store.number, employee.company.myDataset())),
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
   }

}
