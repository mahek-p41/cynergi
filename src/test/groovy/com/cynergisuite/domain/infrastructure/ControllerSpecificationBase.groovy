package com.cynergisuite.domain.infrastructure

import com.cynergisuite.middleware.authentication.LoginCredentials
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeTestDataLoaderService
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken

import jakarta.inject.Inject

import static io.micronaut.http.HttpRequest.DELETE
import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpRequest.POST
import static io.micronaut.http.HttpRequest.PUT

abstract class ControllerSpecificationBase extends ServiceSpecificationBase {
   private @Inject EmployeeTestDataLoaderService userSetupEmployeeFactoryService
   private @Inject StoreRepository userStoreRepository
   @Client("/api") @Inject HttpClient httpClient
   @Inject UserService userService

   BlockingHttpClient client
   CompanyEntity tstds1
   EmployeeEntity nineNineEightEmployee
   AuthenticatedEmployee nineNineEightAuthenticatedEmployee
   String nineNineEightAccessToken

   StoreEntity store1Tstds1
   StoreEntity store3Tstds1

   void setup() {
      this.client = httpClient.toBlocking()
      this.tstds1 = companyFactoryService.forDatasetCode('tstds1')
      this.store1Tstds1 = userStoreRepository.findOne(1, tstds1)
      this.store3Tstds1 = userStoreRepository.findOne(3, tstds1)

      this.nineNineEightEmployee = userSetupEmployeeFactoryService.singleSuperUser(998, tstds1, 'man', 'super', 'pass')
      this.nineNineEightAuthenticatedEmployee = userService.fetchUserByAuthentication(nineNineEightEmployee.number, nineNineEightEmployee.passCode, tstds1.datasetCode, null).with { new AuthenticatedEmployee(it, 'pass') }
      this.nineNineEightAccessToken = loginEmployee(nineNineEightAuthenticatedEmployee)
   }

   String loginEmployee(AuthenticatedEmployee employee) {
      return client.exchange(POST("/login", new LoginCredentials(employee.number.toString(), employee.passCode, employee.assignedLocation?.myNumber(), employee.company.datasetCode)), BearerAccessRefreshToken).body().accessToken
   }

   Object get(String path, String accessToken = nineNineEightAccessToken) throws HttpClientResponseException {
      return client.exchange(
         GET("/${path}").header("Authorization", "Bearer $accessToken"),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()
   }

   Object post(String path, Object body, String accessToken = nineNineEightAccessToken) throws HttpClientResponseException {
      return postForResponse(path, body, accessToken).bodyAsJson()
   }

   HttpResponse postForResponse(String path, Object body, String accessToken = nineNineEightAccessToken) throws HttpClientResponseException {
      return client.exchange(
         POST("/${path}", body).header("Authorization", "Bearer $accessToken"),
         Argument.of(String),
         Argument.of(String)
      )
   }

   Object put(String path, Object body, String accessToken = nineNineEightAccessToken) throws HttpClientResponseException {
      return client.exchange(
         PUT("/${path}", body).header("Authorization", "Bearer $accessToken"),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()
   }

   Object delete(String path, String accessToken = nineNineEightAccessToken) throws HttpClientResponseException {
      return client.exchange(
         DELETE("/${path}").header("Authorization", "Bearer $accessToken"),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()
   }

   Object delete(String path, Object body, String accessToken = nineNineEightAccessToken) throws HttpClientResponseException {
      return client.exchange(
         DELETE("/${path}", body).header("Authorization", "Bearer $accessToken"),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()
   }
}
