package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.UsernamePasswordStoreCredentials
import com.cynergisuite.middleware.employee.EmployeeService
import com.cynergisuite.middleware.store.StoreService
import io.micronaut.core.type.Argument
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpRequest.HEAD
import static io.micronaut.http.HttpRequest.POST
import static io.micronaut.http.HttpStatus.UNAUTHORIZED

@MicronautTest(transactional = false)
class StoreLoginControllerSpecification extends Specification {
   @Client("/api") @Inject RxHttpClient httpClient
   @Inject EmployeeService employeeService
   @Inject StoreService storeService

   void "login successful"() {
      given:
      final employee = employeeService.fetchUserByAuthentication(123, 'pass', null).blockingGet()
      final store = storeService.fetchByNumber(3)

      when:
      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login/store",new UsernamePasswordStoreCredentials(employee.number.toString(), employee.passCode, store.number)),
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
   }

   void "login failure due to invalid store" () {
      given:
      final validEmployee = employeeService.fetchUserByAuthentication(123, 'pass', null).blockingGet()

      when:
      httpClient.toBlocking()
         .exchange(
            POST("/login/store",new UsernamePasswordStoreCredentials(validEmployee.number.toString(), validEmployee.passCode, 75)),
            Argument.of(String),
            Argument.of(String)
         )

      then:
      final error = thrown(HttpClientResponseException)
      error.status == UNAUTHORIZED
   }
}
