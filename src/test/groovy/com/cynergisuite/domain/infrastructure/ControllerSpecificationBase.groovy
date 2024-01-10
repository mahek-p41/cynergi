package com.cynergisuite.domain.infrastructure

import com.cynergisuite.middleware.authentication.LoginCredentials
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeTestDataLoaderService
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import groovy.sql.Sql
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken

import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.transaction.Transactional

import static io.micronaut.http.HttpRequest.DELETE
import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpRequest.POST
import static io.micronaut.http.HttpRequest.PUT

abstract class ControllerSpecificationBase extends ServiceSpecificationBase {
   private @Inject EmployeeTestDataLoaderService userSetupEmployeeFactoryService
   private @Inject StoreRepository userStoreRepository
   @Client("/api") @Inject HttpClient httpClient
   //@Inject TruncateDatabaseService truncateDatabaseService //Results in: No detected/configured IoServiceFactoryFactory using Nio2ServiceFactoryFactory
   @Inject UserService userService

   private static final Logger logger = LoggerFactory.getLogger("ControllerSpecificationBase")
   /*
   private final Sql sql

   @Inject
   ControllerSpecificationBase(Sql sql) {
      this.sql = sql
   }
   */

   BlockingHttpClient client
   CompanyEntity tstds1
   EmployeeEntity nineNineEightEmployee
   AuthenticatedEmployee nineNineEightAuthenticatedEmployee
   String nineNineEightAccessToken

   StoreEntity store1Tstds1
   StoreEntity store3Tstds1

   void setup() {
      this.client = httpClient.toBlocking()
      this.tstds1 = companyFactoryService.forDatasetCode('coravt')
      this.store1Tstds1 = userStoreRepository.findOne(1, tstds1)
      this.store3Tstds1 = userStoreRepository.findOne(3, tstds1)

      this.nineNineEightEmployee = userSetupEmployeeFactoryService.singleSuperUser(998, tstds1, 'man', 'super', 'pass')

      //Load inventory here?
      //loadInventory()
      //truncateDatabaseService.loadInventory() //Results in: java.lang.NullPointerException: Cannot invoke method truncate() on null object
      logger.debug("In ControllerSpecificationBase setup")

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

   HttpResponse getForResponse(String path, String accessToken = nineNineEightAccessToken) throws HttpClientResponseException {
      return client.exchange(
         GET("/${path}").header("Authorization", "Bearer $accessToken"),
         Argument.of(String),
         Argument.of(String)
      )
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

   @Transactional
   void loadInventory() {
      logger.debug("Copying inventory_vw data to inventory")
      sql.execute("""
          INSERT INTO inventory (dataset, serial_number, lookup_key, lookup_key_type, barcode, alternate_id, brand,model_number, product_code, description, received_date, original_cost, actual_cost, model_category, times_rented,total_revenue, remaining_value, sell_price, assigned_value, idle_days, condition, invoice_number, inv_invoice_expensed_date, inv_purchase_order_number, returned_date, location, status, primary_location, location_type, status_id, model_id, store_id, received_location, invoice_id, inventory_changed_sw, changes_sent_to_current_state_sw)
           SELECT dataset, serial_number, lookup_key, lookup_key_type, barcode, COALESCE(alt_id, '123') AS alternate_id, COALESCE(brand, 'default_brand') AS brand, model_number, product_code, description, received_date, original_cost, actual_cost, model_category, times_rented, total_revenue, remaining_value, sell_price, assigned_value, idle_days, condition, invoice_number, inv_invoice_expensed_date, inv_purchase_order_number, returned_date, location, status, primary_location, location_type, 1 AS status_id, null AS model_id, 1 AS store_id, 1 AS received_location, null AS invoice_id, false AS inventory_changed_sw, false AS changes_sent_to_current_state_sw
            FROM fastinfo_prod_import.inventory_vw"""
      )
   }
}
