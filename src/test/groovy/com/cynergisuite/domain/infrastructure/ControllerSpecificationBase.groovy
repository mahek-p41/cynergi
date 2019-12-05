package com.cynergisuite.domain.infrastructure


import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeService
import groovy.json.JsonSlurper
import io.micronaut.core.type.Argument
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken

import javax.inject.Inject

import static io.micronaut.http.HttpRequest.DELETE
import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpRequest.POST
import static io.micronaut.http.HttpRequest.PUT

abstract class ControllerSpecificationBase extends ServiceSpecificationBase {
   @Client("/api") @Inject RxHttpClient httpClient
   @Inject EmployeeService employeeService

   protected BlockingHttpClient client
   protected String cynergiAccessToken
   protected JsonSlurper jsonSlurper
   protected EmployeeEntity authenticatedEmployee

   void setup() {
      client = httpClient.toBlocking()
      authenticatedEmployee = employeeService.fetchUserByAuthentication(123, 'pass', null).blockingGet()
      cynergiAccessToken = loginEmployee(authenticatedEmployee)
      jsonSlurper = new JsonSlurper()
   }

   String loginEmployee(EmployeeEntity employee) {
      return client.exchange(POST("/login", new UsernamePasswordCredentials(employee.number.toString(), employee.passCode)), BearerAccessRefreshToken).body().accessToken
   }

   Object get(String path) throws HttpClientResponseException {
      return client.exchange(
         GET("/${path}").header("Authorization", "Bearer $cynergiAccessToken"),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()
   }

   Object post(String path, Object body, String accessToken = cynergiAccessToken) throws HttpClientResponseException {
      return client.exchange(
         POST("/${path}", body).header("Authorization", "Bearer $accessToken"),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()
   }

   Object put(String path, Object body) throws HttpClientResponseException {
      return client.exchange(
         PUT("/${path}", body).header("Authorization", "Bearer $cynergiAccessToken"),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()
   }

   Object delete(String path) throws HttpClientResponseException {
      return client.exchange(
         DELETE("/${path}").header("Authorization", "Bearer $cynergiAccessToken"),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()
   }

   Object parseResponse(Object responseBody) {
      return jsonSlurper.parseText(responseBody as String)
   }
}
