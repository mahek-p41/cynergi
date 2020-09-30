package com.cynergisuite.middleware.audit.detail.scan.area.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaDTO
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaDTOV1
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaFactoryService
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.department.DepartmentFactoryService
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.store.StoreValueObject
import io.micronaut.core.type.Argument
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class AuditScanAreaControllerSpecification extends ControllerSpecificationBase {
   private static String path = "/audit/detail/scan-area"
   @Inject AuditScanAreaFactoryService auditScanAreaFactoryService
   @Inject DepartmentFactoryService departmentFactoryService
   @Inject EmployeeFactoryService employeeFactoryService

   void "fetch all audit detail scan areas v1" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final storeThree = storeFactoryService.store(3, company)
      def department = departmentFactoryService.random(storeThree.myCompany())
      def employee = employeeFactoryService.single(storeThree, department)
      final store1Tstds1AuthenticatedEmployee = userService.fetchUserByAuthentication(employee.number, employee.passCode, tstds1.myDataset(), employee.store.number).blockingGet().with { new AuthenticatedEmployee(it, employee.passCode) }
      final regionalManagerEmployeeAuth = loginEmployee(store1Tstds1AuthenticatedEmployee)

      auditScanAreaFactoryService.warehouse(storeOne, company)
      auditScanAreaFactoryService.showroom(storeOne, company)
      auditScanAreaFactoryService.storeroom(storeOne, company)
      final storeThreeWarehouse = auditScanAreaFactoryService.warehouse(storeThree, company)
      final storeThreeShowroom = auditScanAreaFactoryService.showroom(storeThree, company)
      final storeThreeStoreroom = auditScanAreaFactoryService.storeroom(storeThree, company)

      when:
      def response = client.exchange(
            GET(path)
            .headers(["Authorization": "Bearer $regionalManagerEmployeeAuth", "Accept-Version": "1"]),
            Argument.of(String),
            Argument.of(String)
         ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      response.collect { new AuditScanAreaDTOV1(it) } == (
         [
            storeThreeShowroom,
            storeThreeStoreroom,
            storeThreeWarehouse
         ].collect {new AuditScanAreaDTOV1(it) }
      )

      when:
      def response2 = client.exchange(
         GET(path)
            .headers(["Authorization": "Bearer $regionalManagerEmployeeAuth"]),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      response2.collect { new AuditScanAreaDTOV1(it) } == (
         [
            storeThreeShowroom,
            storeThreeStoreroom,
            storeThreeWarehouse
         ].collect {new AuditScanAreaDTOV1(it) }
      )
   }

   void "fetch all audit detail scan areas v2" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final storeThree = storeFactoryService.store(3, company)
      def department = departmentFactoryService.random(storeThree.myCompany())
      def employee = employeeFactoryService.single(storeThree, department)
      final store1Tstds1AuthenticatedEmployee = userService.fetchUserByAuthentication(employee.number, employee.passCode, tstds1.myDataset(), employee.store.number).blockingGet().with { new AuthenticatedEmployee(it, employee.passCode) }
      final regionalManagerEmployeeAuth = loginEmployee(store1Tstds1AuthenticatedEmployee)

      auditScanAreaFactoryService.warehouse(storeOne, company)
      auditScanAreaFactoryService.showroom(storeOne, company)
      auditScanAreaFactoryService.storeroom(storeOne, company)
      final storeThreeWarehouse = auditScanAreaFactoryService.warehouse(storeThree, company)
      final storeThreeShowroom = auditScanAreaFactoryService.showroom(storeThree, company)
      final storeThreeStoreroom = auditScanAreaFactoryService.storeroom(storeThree, company)
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")

      when:
      def response = client.exchange(
         GET("$path${pageOne}&storeId=${storeThree.myId()}")
            .headers(["Authorization": "Bearer $regionalManagerEmployeeAuth", "Accept-Version": "2", "X-API-VERSION": "1"]),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      response.elements.collect { new AuditScanAreaDTO(it) } == (
         [
            storeThreeWarehouse,
            storeThreeShowroom,
            storeThreeStoreroom
         ].collect {new AuditScanAreaDTO(it) }
      )

      when:
      client.exchange(
         GET("$path${pageTwo}&storeId=${storeThree.myId()}")
            .headers(["Authorization": "Bearer $regionalManagerEmployeeAuth", "X-API-VERSION": "2"]),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create a valid scan area" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeThree = storeFactoryService.store(3, company)
      final area = new AuditScanAreaDTO([name: 'Custom Area', store: new StoreValueObject(storeThree)])

      when:
      def result = post(path, area)

      then:
      notThrown(HttpClientResponseException)
      with(result) {
         id > 0
         name == area.name
         with(store) {
            id == storeThree.myId()
            storeNumber == storeThree.myNumber()
            name == storeThree.myName()
         }
      }
   }

   void "create an invalid scan area without name" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeThree = storeFactoryService.store(3, company)
      final area = new AuditScanAreaDTO([store: new StoreValueObject(storeThree)])

      when:
      post(path, area)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'auditScanAreaDTO.name'
      response[0].message == 'Is required'
   }

   void "create an invalid scan area without store" () {
      given:
      final area = new AuditScanAreaDTO([name: 'Custom Area'])

      when:
      post(path, area)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'auditScanAreaDTO.store'
      response[0].message == 'Is required'
   }

   void "update a valid scan area" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final storeThree = storeFactoryService.store(3, company)
      def department = departmentFactoryService.random(storeThree.myCompany())
      def employee = employeeFactoryService.single(storeThree, department)
      final store1Tstds1AuthenticatedEmployee = userService.fetchUserByAuthentication(employee.number, employee.passCode, tstds1.myDataset(), employee.store.number).blockingGet().with { new AuthenticatedEmployee(it, employee.passCode) }
      final regionalManagerEmployeeAuth = loginEmployee(store1Tstds1AuthenticatedEmployee)

      final warehouse = auditScanAreaFactoryService.warehouse(storeOne, company)
      final area = new AuditScanAreaDTO([id: warehouse.myId(), name: 'Custom Area', store: new StoreValueObject(storeOne)])

      when:
      def result = put("$path/${warehouse.myId()}", area)

      then:
      notThrown(HttpClientResponseException)
      with(result) {
         id == warehouse.myId()
         name == area.name
         with(store) {
            id == storeOne.myId()
            storeNumber == storeOne.myNumber()
            name == storeOne.myName()
         }
      }
   }

   void "update an invalid scan area without name" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final storeThree = storeFactoryService.store(3, company)
      def department = departmentFactoryService.random(storeThree.myCompany())
      def employee = employeeFactoryService.single(storeThree, department)
      final store1Tstds1AuthenticatedEmployee = userService.fetchUserByAuthentication(employee.number, employee.passCode, tstds1.myDataset(), employee.store.number).blockingGet().with { new AuthenticatedEmployee(it, employee.passCode) }
      final regionalManagerEmployeeAuth = loginEmployee(store1Tstds1AuthenticatedEmployee)

      final warehouse = auditScanAreaFactoryService.warehouse(storeOne, company)
      final area = new AuditScanAreaDTO([id: warehouse.myId(), store: new StoreValueObject(storeOne)])

      when:
      put("$path/${warehouse.myId()}", area)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'auditScanAreaDTO.name'
      response[0].message == 'Is required'

   }

   void "update an invalid scan area without store" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final storeThree = storeFactoryService.store(3, company)
      def department = departmentFactoryService.random(storeThree.myCompany())
      def employee = employeeFactoryService.single(storeThree, department)
      final store1Tstds1AuthenticatedEmployee = userService.fetchUserByAuthentication(employee.number, employee.passCode, tstds1.myDataset(), employee.store.number).blockingGet().with { new AuthenticatedEmployee(it, employee.passCode) }
      final regionalManagerEmployeeAuth = loginEmployee(store1Tstds1AuthenticatedEmployee)

      final warehouse = auditScanAreaFactoryService.warehouse(storeOne, company)
      final area = new AuditScanAreaDTO([id: warehouse.myId(), name: 'Custom Area'])

      when:
      put("$path/${warehouse.myId()}", area)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'auditScanAreaDTO.store'
      response[0].message == 'Is required'
   }
}
