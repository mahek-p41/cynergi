package com.cynergisuite.domain.infrastructure

import com.cynergisuite.domain.IdentifiableValueObject
import com.cynergisuite.middleware.employee.Employee
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
   protected Employee authenticatedEmployee

   void setup() {
      client = httpClient.toBlocking()
      authenticatedEmployee = employeeService.findUserByAuthentication(123, 'pass').blockingGet()
      cynergiAccessToken = client.exchange(POST("/login", new UsernamePasswordCredentials('123', 'pass')), BearerAccessRefreshToken).body().accessToken
      jsonSlurper = new JsonSlurper()
   }

   Object get(String path) throws HttpClientResponseException {
      final def json = client.exchange(
         GET("/${path}").header("Authorization", "Bearer $cynergiAccessToken"),
         Argument.of(String) as Argument<String>,
         Argument.of(String) as Argument<String>
      ).body()

      return jsonSlurper.parseText(json)
   }

   def <BODY extends IdentifiableValueObject> Object post(String path, BODY body) throws HttpClientResponseException {
      final def json = client.exchange(
         POST("/${path}", body).header("Authorization", "Bearer $cynergiAccessToken"),
         Argument.of(String),
         Argument.of(String)
      ).body()

      return jsonSlurper.parseText(json)
   }

   def <BODY extends IdentifiableValueObject> Object put(String path, BODY body) throws HttpClientResponseException {
      def json = client.exchange(
         PUT("/${path}", body).header("Authorization", "Bearer $cynergiAccessToken"),
         Argument.of(String),
         Argument.of(String)
      ).body()

      return jsonSlurper.parseText(json)
   }

   Object delete(String path) throws HttpClientResponseException {
      final def json = client.exchange(
         DELETE("/${path}").header("Authorization", "Bearer $cynergiAccessToken"),
         Argument.of(String),
         Argument.of(String)
      ).body()

      return jsonSlurper.parseText(json)
   }

   Object parseResponse(Object responseBody) {
      return jsonSlurper.parseText(responseBody as String)
   }
}
