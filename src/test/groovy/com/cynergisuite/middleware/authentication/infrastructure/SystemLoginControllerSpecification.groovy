package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.authentication.LoginCredentials
import com.cynergisuite.middleware.department.DepartmentFactoryService
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.store.StoreFactoryService
import com.cynergisuite.middleware.store.StoreService
import io.micronaut.core.type.Argument
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpRequest.*
import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.UNAUTHORIZED

@MicronautTest(transactional = false)
class SystemLoginControllerSpecification extends ServiceSpecificationBase {
   @Inject @Client("/api") RxHttpClient httpClient
   @Inject DepartmentFactoryService departmentFactoryService
   @Inject EmployeeFactoryService employeeFactoryService
   @Inject StoreService storeService
   @Inject StoreFactoryService storeFactoryService

   void "login successful" () {
      given:
      final store = storeFactoryService.storeThreeTstds1()
      final employee = employeeFactoryService.single(store)

      when:
      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login", new LoginCredentials(employee.number.toString(), employee.passCode, employee.store.number, employee.company.myDataset())),
            Argument.of(String),
            Argument.of(String)
         ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      authResponse.access_token != null

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
      response.employeeNumber == employee.myEmployeeNumber().toString()
      response.loginStatus == "${employee.myEmployeeNumber()} is now logged in"
      response.storeNumber == 3
      response.dataset == 'tstds1'
   }

   void "login failure due to invalid store" () {
      given:
      final store = storeFactoryService.storeThreeTstds1()
      final department = departmentFactoryService.random(store.company)
      final validEmployee = employeeFactoryService.single(store, department)

      when:
      httpClient.toBlocking()
         .exchange(
            POST("/login",new LoginCredentials(validEmployee.number.toString(), validEmployee.passCode, 75, validEmployee.company.myDataset())),
            Argument.of(String),
            Argument.of(String)
         )

      then:
      final error = thrown(HttpClientResponseException)
      error.status == UNAUTHORIZED
      final json = error.response.bodyAsJson()
      json.message == "Access denied for ${validEmployee.number} credentials do not match"
   }

   void "login failure due to missing dataset" () {
      given:
      final store = storeFactoryService.storeThreeTstds1()
      final department = departmentFactoryService.random(store.company)
      final validEmployee = employeeFactoryService.single(store, department)

      when:
      httpClient.toBlocking()
         .exchange(
            POST("/login",new LoginCredentials(validEmployee.number.toString(), validEmployee.passCode, validEmployee.store.number, null)),
            Argument.of(String),
            Argument.of(String)
         )

      then:
      final error = thrown(HttpClientResponseException)
      error.status == BAD_REQUEST
      final json = error.response.bodyAsJson()
      json.size() == 1
      json[0].message == "Is required"
      json[0].path == "login.loginCredentials.dataset"
   }

   void "login with user who isn't authorized for tstds2" () {
      given:
      final storeOneTstds1 = storeFactoryService.storeOneTstds1()
      final user = employeeFactoryService.single(storeOneTstds1)

      when:
      httpClient.toBlocking()
         .exchange(
            POST("/login", new LoginCredentials(user.number.toString(), user.passCode, user.store.number, 'tstds2')),
            Argument.of(String),
            Argument.of(String)
         )

      then:
      final error = thrown(HttpClientResponseException)
      error.status == UNAUTHORIZED
      final json = error.response.bodyAsJson()
      json.message == "Access denied for ${user.number} credentials do not match"
   }

   void "login as high touch uber user with dataset tstds1" () {
      given:
      final htUberUserTstds1 = employeeFactoryService.single(998, 'tstds1', 'admin', null, 'word', null, true, null)
      final htUberUserTstds2 = employeeFactoryService.single(998, 'tstds2', 'admin', null, 'word', null, true, null)

      when:
      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login",new LoginCredentials(htUberUserTstds1, 'word', null)),
            Argument.of(String),
            Argument.of(String)
         ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      authResponse.access_token != null

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
      response.employeeNumber == '998'
      response.loginStatus == '998 is now logged in'
      response.storeNumber == 9000
      response.dataset == 'tstds1'
   }

   void "login with superfluous URL parameters" () {
      given:
      final store = storeFactoryService.storeThreeTstds1()
      final department = departmentFactoryService.random(store.company)
      final employee = employeeFactoryService.single(store, department)

      when:
      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login?extraOne=1&extraTwo=two",new LoginCredentials(employee.number.toString(), employee.passCode, employee.store.number, employee.company.myDataset())),
            Argument.of(String),
            Argument.of(String)
         ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      authResponse.access_token != null

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
      response.employeeNumber == '123'
      response.loginStatus == '123 is now logged in'
      response.storeNumber == 3
      response.dataset == 'tstds1'
   }

   void "check authenticated returns 401 when not logged in via HEAD" () {
      when:
      httpClient.toBlocking()
         .exchange(
            HEAD("/authenticated/check"),
            Argument.of(String),
            Argument.of(String)
         )

      then:
      final e = thrown(HttpClientResponseException)
      e.response.body() == null
      e.status == UNAUTHORIZED
   }

   void "check authenticated returns 401 when not logged in via GET" () {
      when:
      httpClient.toBlocking()
         .exchange(
            GET("/authenticated/check"),
            Argument.of(String),
            Argument.of(String)
         )

      then:
      final e = thrown(HttpClientResponseException)
      e.response.body() == null
      e.status == UNAUTHORIZED
   }
}
