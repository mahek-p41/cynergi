package com.cynergisuite.middleware.sign.here.token.infrastructure

import com.cynergisuite.domain.SimpleLegacyNumberDTO
import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.company.CompanyDTO
import com.cynergisuite.middleware.sign.here.token.SignHereTokenDTO
import com.cynergisuite.middleware.sign.here.token.SignHereTokenTestDataLoaderService
import com.cynergisuite.middleware.store.StoreTestDataLoaderService
import io.micronaut.core.type.Argument
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpRequest.PUT
import static io.micronaut.http.HttpStatus.BAD_REQUEST

@MicronautTest(transactional = false)
class SignHereTokenControllerSpecification extends ServiceSpecificationBase {
   @Client("/sign/here/token") @Inject HttpClient tokenClient

   @Inject SignHereTokenTestDataLoaderService signHereTokenTestDataLoaderService
   @Inject StoreTestDataLoaderService storeFactoryService

   void "fetch one AWS token by store number" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.random(company)
      final dataset = 'tstds1'
      final signHereToken = signHereTokenTestDataLoaderService.single(company, store, "wrXnbCWTUrxojPBGMMSakODI9BilnWVlvovUneTOCTybz5nG8678211k")

      when:
      def result = tokenClient.toBlocking().exchange(GET("/store/${store.number}/dataset/${dataset}"), // login without authorization
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      result.token == signHereToken.token
      result.store.number == store.number
   }

   void "fetch one AWS token by invalid store number" () {
      given: 'store number 1 is assigned to a region of company tstds1'
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.random(company)
      final dataset = 'tstds1'
      final signHereToken = signHereTokenTestDataLoaderService.single(company, store, "wrXnbCWTUrxojPBGMMSakODI9BilnWVlvovUneTOCTybz5nG8678211k")

      when:
      def result = tokenClient.toBlocking().exchange(GET("/store/55/dataset/${dataset}"), // login without authorization
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      final exception = thrown(HttpClientResponseException)
      final body = exception.response.bodyAsJson()
      body.message == "tstds1 -> 55 was unable to be found"
   }

   void "fetch one token by id not found" () {
      when:
      def result = tokenClient.toBlocking().exchange(GET("/0"), // login without authorization
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 3
      response.message == "Failed to convert argument [id] for value [0]"
   }

   void "update a token" () {
      given: 'store number 1 is assigned to a region of company tstds1'
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.random(company)
      final signHereToken = signHereTokenTestDataLoaderService.single(company, store, "wrXnbCWTUrxojPBGMMSakODI9BilnWVlvovUneTOCTybz5nG8678211k")
      final SLN = new SimpleLegacyNumberDTO(store.number)
      final newDTO = new SignHereTokenDTO(signHereToken.id.toString(), new CompanyDTO(company), SLN, "111111111111111111111111111111111")

      when:
      def result = tokenClient.toBlocking().exchange(PUT("/${signHereToken.id}", newDTO),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      final extraDTO = new SignHereTokenDTO(signHereToken.id.toString(), new CompanyDTO(company), SLN, "111111111111111111111111111111111")
      result.company.id.toString() == extraDTO.company.id
      result.token == extraDTO.token
   }

   void "try an update using too short a token" () {
      given: 'store number 1 is assigned to a region of company tstds1'
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.random(company)
      final signHereToken = signHereTokenTestDataLoaderService.single(company, store, "wrXnbCWTUrxojPBGMMSakODI9BilnWVlvovUneTOCTybz5nG8678211k")
      final SLN = new SimpleLegacyNumberDTO(store.number)
      final messageArray = []
      messageArray[0] = "Size of provided value 12345678901234567890123456789 is invalid"
      final newDTO = new SignHereTokenDTO(signHereToken.id.toString(), new CompanyDTO(company), SLN, "12345678901234567890123456789")

      when:
      def result = tokenClient.toBlocking().exchange(PUT("/${signHereToken.id}", newDTO),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      final exception = thrown(HttpClientResponseException)
      final body = exception.response.bodyAsJson()
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == messageArray
   }

   void "try an update using too long a token" () {
      given: 'store number 1 is assigned to a region of company tstds1'
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.random(company)
      final signHereToken = signHereTokenTestDataLoaderService.single(company, store, "wrXnbCWTUrxojPBGMMSakODI9BilnWVlvovUneTOCTybz5nG8678211k")
      final SLN = new SimpleLegacyNumberDTO(store.number)
      final messageArray = []
      messageArray[0] = "Size of provided value 123456789012345678901234567890123456789012345678901234567890X is invalid"
      final newDTO = new SignHereTokenDTO(signHereToken.id.toString(), new CompanyDTO(company), SLN, "123456789012345678901234567890123456789012345678901234567890X")

      when:
      def result = tokenClient.toBlocking().exchange(PUT("/${signHereToken.id}", newDTO),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      final exception = thrown(Exception)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == messageArray
   }
}
