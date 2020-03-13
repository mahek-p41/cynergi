package com.cynergisuite.domain.infrastructure

import com.cynergisuite.middleware.authentication.LoginCredentials
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeFactoryService
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
   private @Inject EmployeeFactoryService userSetupEmployeeFactoryService
   @Client("/api") @Inject RxHttpClient httpClient
   @Inject UserService userService

   BlockingHttpClient client
   Company tstds1
   EmployeeEntity nineNineEightEmployee
   AuthenticatedEmployee authenticatedEmployee
   String cynergiAccessToken

   void setup() {
      this.client = httpClient.toBlocking()
      this.tstds1 = companyFactoryService.forDatasetCode('tstds1')
      this.nineNineEightEmployee = userSetupEmployeeFactoryService.single(998, tstds1, 'man', 'super', 'pass', true, "A", 0)
      this.authenticatedEmployee = userService.fetchUserByAuthentication(nineNineEightEmployee.number, nineNineEightEmployee.passCode, tstds1.datasetCode, null).blockingGet().with { new AuthenticatedEmployee(it, 'pass') }
      this.cynergiAccessToken = loginEmployee(authenticatedEmployee)
   }

   String loginEmployee(AuthenticatedEmployee employee) {
      return client.exchange(POST("/login", new LoginCredentials(employee.number.toString(), employee.passCode, employee.location?.myNumber(), employee.company.myDataset())), BearerAccessRefreshToken).body().accessToken
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
