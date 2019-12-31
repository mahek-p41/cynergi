package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.LoginCredentials
import com.cynergisuite.middleware.employee.EmployeeService
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
   @Inject StoreService storeService

   void "login successful"() {
      given:
      final employee = employeeService.fetchUserByAuthentication(111, 'pass', 'tstds1', null).blockingGet()
      final store = storeService.fetchByNumber(3, 'tstds1')

      when:
      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login",new LoginCredentials(employee.number.toString(), employee.passCode, store.number, 'tstds1')),
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
      response.employeeNumber == '111'
      response.loginStatus == '111 is now logged in'
      response.storeNumber == 3
      response.dataset == 'tstds1'
   }

   void "login failure due to invalid store" () {
      given:
      final validEmployee = employeeService.fetchUserByAuthentication(111, 'pass', 'tstds1', null).blockingGet()

      when:
      httpClient.toBlocking()
         .exchange(
            POST("/login",new LoginCredentials(validEmployee.number.toString(), validEmployee.passCode, 75, validEmployee.dataset)),
            Argument.of(String),
            Argument.of(String)
         )

      then:
      final error = thrown(HttpClientResponseException)
      error.status == UNAUTHORIZED
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
   }
}
