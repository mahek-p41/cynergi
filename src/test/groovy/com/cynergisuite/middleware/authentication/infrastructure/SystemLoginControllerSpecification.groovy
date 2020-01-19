package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.LoginCredentials
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.employee.EmployeeService
import com.cynergisuite.middleware.store.StoreFactory
import com.cynergisuite.middleware.store.StoreFactoryService
import com.cynergisuite.middleware.store.StoreService
import io.micronaut.core.type.Argument
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import javax.inject.Inject
import spock.lang.Specification


import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpRequest.HEAD
import static io.micronaut.http.HttpRequest.POST
import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.UNAUTHORIZED

@MicronautTest(transactional = false)
class SystemLoginControllerSpecification extends Specification {
   @Inject @Client("/api") RxHttpClient httpClient
   @Inject EmployeeService employeeService
   @Inject EmployeeFactoryService employeeFactoryService
   @Inject StoreService storeService
   @Inject StoreFactoryService storeFactoryService

   void "login successful" () {
      given:
      final store = storeFactoryService.storeThreeTstds1()
      final employee = employeeFactoryService.single(123, 'test', 'username', store, 'word', true, false)

      when:
      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login",new LoginCredentials(employee, 'word')),
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

   void "login failure due to invalid store" () {
      given:
      final store = storeFactoryService.storeThreeTstds1()
      final validEmployee = employeeFactoryService.single(123, 'test', 'username', store, 'word', true, false)

      when:
      httpClient.toBlocking()
         .exchange(
            POST("/login",new LoginCredentials(validEmployee.number.toString(), 'word', 75, validEmployee.dataset)),
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
      final validEmployee = employeeService.fetchUserByAuthentication(111, 'pass', 'tstds1', null).blockingGet()

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
      final storeOneTstds1 = StoreFactory.storeOneTstds1()
      final user = employeeFactoryService.single(storeOneTstds1)

      when:
      httpClient.toBlocking()
         .exchange(
            POST("/login",new LoginCredentials(user.number.toString(), user.passCode, user.store.number, 'tstds2')),
            Argument.of(String),
            Argument.of(String)
         )

      then:
      final error = thrown(HttpClientResponseException)
      error.status == UNAUTHORIZED
      final json = error.response.bodyAsJson()
      json.message == "Access denied for ${user.number} credentials do not match"
   }
}
