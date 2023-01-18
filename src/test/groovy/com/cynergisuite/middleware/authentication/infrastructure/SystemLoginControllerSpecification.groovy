package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.address.AddressTestDataLoaderService
import com.cynergisuite.middleware.authentication.LoginCredentials
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import io.micronaut.core.type.Argument
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject


import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpRequest.HEAD
import static io.micronaut.http.HttpRequest.POST
import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.METHOD_NOT_ALLOWED
import static io.micronaut.http.HttpStatus.UNAUTHORIZED

@MicronautTest(transactional = false)
class SystemLoginControllerSpecification extends ServiceSpecificationBase {
   @Inject @Client("/api") HttpClient httpClient
   @Inject AddressTestDataLoaderService addressTestDataLoaderService
   @Inject CompanyRepository companyRepository

   void "login successful with user who doesn't have department" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final employee = employeeFactoryService.single(store)

      when:
      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login", new LoginCredentials(employee.number.toString(), employee.passCode, employee.store.myNumber(), employee.company.datasetCode)),
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
      response.employeeNumber == employee.number.toString()
      response.storeNumber == 3
      response.company.with {
         clientCode = company.clientCode
         clientId = company.clientId
         datasetCode = company.datasetCode
         id = company.id
         name = company.name
         federalTaxNumber = null
      }
   }

   void "login with user who has department assigned" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(company)
      final employee = employeeFactoryService.single(store, department)

      when:
      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login", new LoginCredentials(employee.number.toString(), employee.passCode, employee.store.myNumber(), employee.company.datasetCode)),
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
      response.employeeNumber == "${employee.number}"
      response.storeNumber == 3
      response.company.with {
         clientCode = company.clientCode
         clientId = company.clientId
         datasetCode = company.datasetCode
         id = company.id
         name = company.name
         federalTaxNumber = null
      }
   }

   void "login failure due to invalid store" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(store.myCompany())
      final validEmployee = employeeFactoryService.single(store, department)

      when:
      httpClient.toBlocking()
         .exchange(
            POST("/login",new LoginCredentials(validEmployee.number.toString(), validEmployee.passCode, 75, validEmployee.company.datasetCode)),
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
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(store.myCompany())
      final validEmployee = employeeFactoryService.single(store, department)

      when:
      httpClient.toBlocking()
         .exchange(
            POST("/login",new LoginCredentials(validEmployee.number.toString(), validEmployee.passCode, validEmployee.store.myNumber(), null)),
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
      final company = companyFactoryService.forDatasetCode('coravt')
      final storeOneTstds1 = storeFactoryService.store(3, company)
      final user = employeeFactoryService.single(storeOneTstds1)

      when:
      httpClient.toBlocking()
         .exchange(
            POST("/login", new LoginCredentials(user.number.toString(), user.passCode, user.store.myNumber(), 'corrto')),
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
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final tstds2 = companyFactoryService.forDatasetCode('corrto')
      final htUberUserTstds1 = employeeFactoryService.singleSuperUser(998, tstds1, 'admin', null, 'word')
      final htUberUserTstds2 = employeeFactoryService.singleSuperUser(998, tstds2, 'admin', null, 'word')

      when:
      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login",new LoginCredentials('998', 'word', null, 'coravt')),
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
      response.storeNumber == 9000
      response.company.with {
         clientCode = tstds1.clientCode
         clientId = tstds1.clientId
         datasetCode = tstds1.datasetCode
         id = tstds1.id
         name = tstds1.name
         federalTaxNumber = null
      }
   }

   void "login with superfluous URL parameters" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final department = departmentFactoryService.random(store.myCompany())
      final employee = employeeFactoryService.single(store, department)

      when:
      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login?extraOne=1&extraTwo=two", new LoginCredentials(employee.number.toString(), employee.passCode, employee.store.myNumber(), employee.company.datasetCode)),
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
      response.employeeNumber == "${employee.number}"
      response.storeNumber == 3
      response.company.with {
         clientCode = company.clientCode
         clientId = company.clientId
         datasetCode = company.datasetCode
         id = company.id
         name = company.name
         federalTaxNumber = null
      }
   }

   void "login with user that has an assigned store, but doesn't provide one"() {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final department = departmentFactoryService.department('SL', company)
      final store = storeFactoryService.store(1, company)
      final employee = employeeFactoryService.single(store, department)

      when:
      httpClient.toBlocking()
         .exchange(
            POST("/login", new LoginCredentials(employee.number.toString(), employee.passCode, null, employee.company.datasetCode)),
            Argument.of(String),
            Argument.of(String)
         ).bodyAsJson()

      then:
      final error = thrown(HttpClientResponseException)
      error.status == UNAUTHORIZED
      final json = error.response.bodyAsJson()
      json.message == "Store is required for ${employee.number} to access"
   }

   void "login with user doesn't have a store assigned, but chooses one" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final department = departmentFactoryService.department('HO', company)
      final store = storeFactoryService.store(3, company)
      final employee = employeeFactoryService.single(department)

      when:
      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login", new LoginCredentials(employee.number.toString(), employee.passCode, store.myNumber(), employee.company.datasetCode)),
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
      response.employeeNumber == "${employee.number}"
      response.storeNumber == 3
      response.company.with {
         clientCode = company.clientCode
         clientId = company.clientId
         datasetCode = company.datasetCode
         id = company.id
         name = company.name
         federalTaxNumber = null
      }
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
            Argument.of(String),
         )

      then:
      final e = thrown(HttpClientResponseException)
      e.response.bodyAsJson().message == 'Method Not Allowed'
      e.status == METHOD_NOT_ALLOWED
   }

   void "login with user who has a different store than they chose, but also has the alternative store indicator set to A" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final userAssignedStore = storeFactoryService.store(3, company)
      final otherStore = storeFactoryService.store(1, company)
      final employee = employeeFactoryService.single(userAssignedStore, "A")

      when:
      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login", new LoginCredentials(employee.number.toString(), employee.passCode, otherStore.myNumber(), employee.company.datasetCode)),
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
      response.employeeNumber == employee.number.toString()
      response.storeNumber == 1
      response.company.with {
         clientCode = company.clientCode
         clientId = company.clientId
         datasetCode = company.datasetCode
         id = company.id
         name = company.name
         federalTaxNumber = null
      }
   }

   void "login with sysz user who provides pass_code with 8 characters that is 2 over the max length" () {
      when:
      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login", new LoginCredentials("103", "12312345", 1, "corrto")),
            Argument.of(String),
            Argument.of(String)
         ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      authResponse.access_token != null
   }

   void "login with sysz user who provides pass_code with exact pass_code of 6" () {
      when:
      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login", new LoginCredentials("103", "123123", 1, "corrto")),
            Argument.of(String),
            Argument.of(String)
         ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      authResponse.access_token != null
   }

   void "login with sysz user who provides pass_code with exact pass_code of 4" () {
      when:
      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login", new LoginCredentials("111", "0808", 1, "corrto")),
            Argument.of(String),
            Argument.of(String)
         ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      authResponse.access_token != null
   }

   void "login with sysz user who provides pass_code with exact pass_code of 4 and an address" () {
      given:
      final address = addressTestDataLoaderService.single()
      final company = companyFactoryService.forDatasetCode('coravt')

      when:
      companyRepository.update(company, company.copyMeWithNewAddress(address))
      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login", new LoginCredentials("111", "0808", 1, "corrto")),
            Argument.of(String),
            Argument.of(String)
         ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      authResponse.access_token != null
   }

   void "login with sysz user who provides pass_code with 8 characters and uses the wrong passcode that is only 4 characters" () {
      when:
      httpClient.toBlocking()
         .exchange(
            POST("/login", new LoginCredentials("106", "pass", 3, "coravt")),
            Argument.of(String),
            Argument.of(String)
         ).bodyAsJson()

      then:
      final e = thrown(HttpClientResponseException)
      e.response.bodyAsJson().message == 'Access denied for 106 credentials do not match'
      e.status == UNAUTHORIZED
   }

   void "login with high touch uber user to a company with an address" () {
      given:
      final address = addressTestDataLoaderService.single()
      final company = companyFactoryService.forDatasetCode('coravt')
      final htUberUser = employeeFactoryService.singleSuperUser(998, company, 'admin', null, 'word')

      when:
      companyRepository.update(company, company.copyMeWithNewAddress(address))

      def authResponse = httpClient.toBlocking()
         .exchange(
            POST("/login",new LoginCredentials('998', 'word', null, company.datasetCode)),
            Argument.of(String),
            Argument.of(String)
         ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      authResponse.access_token != null
   }
}
