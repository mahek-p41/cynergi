package com.cynergisuite.domain.infrastructure

import com.cynergisuite.middleware.authentication.LoginCredentials
import com.cynergisuite.middleware.authentication.user.EmployeeUser
import com.cynergisuite.middleware.authentication.user.UserService
import io.micronaut.core.type.Argument
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken

import javax.inject.Inject

import static io.micronaut.http.HttpRequest.DELETE
import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpRequest.POST
import static io.micronaut.http.HttpRequest.PUT

abstract class ControllerSpecificationBase extends ServiceSpecificationBase {
   @Client("/api") @Inject RxHttpClient httpClient
   @Inject UserService userService

   protected BlockingHttpClient client
   protected String cynergiAccessToken
   protected EmployeeUser authenticatedEmployee

   void setup() {
      client = httpClient.toBlocking()
      authenticatedEmployee = userService.fetchUserByAuthentication(111, 'pass', 'tstds1', null).blockingGet().with { new EmployeeUser(it, 'pass') }
      cynergiAccessToken = loginEmployee(authenticatedEmployee)
   }

   String loginEmployee(EmployeeUser employee) {
      return client.exchange(POST("/login", new LoginCredentials(employee.number.toString(), employee.passCode, employee.location.myNumber(), employee.company.myDataset())), BearerAccessRefreshToken).body().accessToken
   }

   Object get(String path, String accessToken = cynergiAccessToken) throws HttpClientResponseException {
      return client.exchange(
         GET("/${path}").header("Authorization", "Bearer $accessToken"),
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

   Object put(String path, Object body, String accessToken = cynergiAccessToken) throws HttpClientResponseException {
      return client.exchange(
         PUT("/${path}", body).header("Authorization", "Bearer $cynergiAccessToken"),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()
   }

   Object delete(String path, String accessToken = cynergiAccessToken) throws HttpClientResponseException {
      return client.exchange(
         DELETE("/${path}").header("Authorization", "Bearer $accessToken"),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()
   }
}
