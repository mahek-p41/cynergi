package com.cynergisuite.middleware.agreement.signing.infrastructure

import com.cynergisuite.domain.SimpleLegacyNumberDTO
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.agreement.signing.AgreementSigningDTO
import com.cynergisuite.middleware.agreement.signing.AgreementSigningTestDataLoaderService
import com.cynergisuite.middleware.company.CompanyDTO
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.StoreTestDataLoaderService
import io.micronaut.core.type.Argument
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpRequest.PUT
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class AgreementSigningControllerSpecification extends ControllerSpecificationBase {
   @Client("/agreement/signing") @Inject HttpClient signingClient

   @Inject AgreementSigningTestDataLoaderService agreementSigningService
   @Inject StoreTestDataLoaderService storeFactoryService

   void "fetch one agreement signing record by id" () {
      given: 'store number 1 is assigned to a region of company tstds1'
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.random(company)
      final dataset = 'coravt'
      final agreementSigning = agreementSigningService.single(company, store, 123456, 111111, 654321, "R", 1, UUID.randomUUID())

      when:
      def result = signingClient.toBlocking().exchange(GET("/${agreementSigning.id}/dataset/${dataset}"),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      result.id == agreementSigning.id
      result.store.number == store.number
      result.primaryCustomerNumber == agreementSigning.primaryCustomerNumber
      result.agreementNumber == agreementSigning.agreementNumber
   }

   void "Upsert Prep with existing agreement record" () {
      given: 'store number 1 is assigned to a region of company tstds1'
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.random(company)
      final dataset = 'coravt'
      final agreementSigning = agreementSigningService.single(company, store, 123456, 111111, 654321, "R", 1, UUID.randomUUID())

      when:
      def result = signingClient.toBlocking().exchange(GET("/upsertPrep/${dataset}/123456/654321/R"),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      result.id == agreementSigning.id
      result.store.number == store.number
      result.primaryCustomerNumber == agreementSigning.primaryCustomerNumber
      result.agreementNumber == agreementSigning.agreementNumber
      result.agreementType == agreementSigning.agreementType
   }

   void "Upsert Prep without existing agreement record" () {
      given: 'store number 1 is assigned to a region of company tstds1'
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.random(company)
      final dataset = 'coravt'

      when:
      def result = signingClient.toBlocking().exchange(GET("/upsertPrep/${dataset}/123456/654321/R"),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == NOT_FOUND
      final body = exception.response.bodyAsJson()
      body.code == "system.not.found"
   }

   void "fetch three agreement_signing records for customer# 123456" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final dataset = 'coravt'
      final store = storeFactoryService.random(company)
      final agreementSigning1 = agreementSigningService.single(company, store, 123456, 111111, 654321, "R", 1, UUID.randomUUID())
      final agreementSigning2 = agreementSigningService.single(company, store, 123456, 111111, 876543, "R", 1, UUID.randomUUID())
      final agreementSigning3 = agreementSigningService.single(company, store, 123456, 111111, 987654, "R", 1, UUID.randomUUID())
      final agreementSigning4 = agreementSigningService.single(company, store, 222222, 333333, 444444, "R", 1, UUID.randomUUID())
      final agreementSigning5 = agreementSigningService.single(company, store, 222222, 333333, 555555, "R", 1, UUID.randomUUID())
      final agreementSigningArray = [agreementSigning1, agreementSigning2, agreementSigning3, agreementSigning4, agreementSigning5]
      final agreementSigningPage1 = new AgreementSigningPageRequest(1, 5, "asn_agreement_number", "ASC", store.myNumber(), 123456, null)

      when:
      def result = signingClient.toBlocking().exchange(GET("/paged/dataset/${dataset}${agreementSigningPage1}"),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      result.elements.eachWithIndex { result2, index ->
         with(result2) {
            primaryCustomerNumber == agreementSigningArray[index].primaryCustomerNumber
            agreementNumber == agreementSigningArray[index].agreementNumber
            agreementType == agreementSigningArray[index].agreementType
            externalSignatureId == agreementSigningArray[index].externalSignatureId.toString()
         }
      }
      result.elements != null
      result.elements.size() == 3
      result.totalElements == 3
      result.totalPages == 1
      result.first == true
      result.last == true
   }

   void "fetch three agreement_signing records for customer# 123456 authenticated" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.random(company)
      final agreementSigning1 = agreementSigningService.single(company, store, 123456, 111111, 654321, "R", 1, UUID.randomUUID())
      final agreementSigning2 = agreementSigningService.single(company, store, 123456, 111111, 876543, "R", 1, UUID.randomUUID())
      final agreementSigning3 = agreementSigningService.single(company, store, 123456, 111111, 987654, "R", 1, UUID.randomUUID())
      final agreementSigning4 = agreementSigningService.single(company, store, 222222, 333333, 444444, "R", 1, UUID.randomUUID())
      final agreementSigning5 = agreementSigningService.single(company, store, 222222, 333333, 555555, "R", 1, UUID.randomUUID())
      final agreementSigningArray = [agreementSigning1, agreementSigning2, agreementSigning3, agreementSigning4, agreementSigning5]

      when:
      def result = get("/agreement/signing/customerAgreements/123456")

      then:
      notThrown(HttpClientResponseException)
      result.size() == 3
      result.eachWithIndex { result2, index ->
         with(result2) {
            primaryCustomerNumber == agreementSigningArray[index].primaryCustomerNumber
            agreementNumber == agreementSigningArray[index].agreementNumber
            agreementType == agreementSigningArray[index].agreementType
            externalSignatureId == agreementSigningArray[index].externalSignatureId.toString()
         }
      }
   }

   void "fetch agreement_signing records for made up/missing customer# 999999" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final dataset = 'coravt'
      final store = storeFactoryService.random(company)
      final agreementSigning1 = agreementSigningService.single(company, store, 123456, 111111, 654321, "R", 1, UUID.randomUUID())
      final agreementSigning2 = agreementSigningService.single(company, store, 123456, 111111, 876543, "R", 1, UUID.randomUUID())
      final agreementSigning3 = agreementSigningService.single(company, store, 123456, 111111, 987654, "R", 1, UUID.randomUUID())
      final agreementSigning4 = agreementSigningService.single(company, store, 222222, 333333, 444444, "R", 1, UUID.randomUUID())
      final agreementSigning5 = agreementSigningService.single(company, store, 222222, 333333, 555555, "R", 1, UUID.randomUUID())
      final agreementSigningPage1 = new AgreementSigningPageRequest(1, 5, "asn_agreement_number", "ASC", null, 999999, null)

      when:
      def result = signingClient.toBlocking().exchange(GET("/paged/dataset/${dataset}${agreementSigningPage1}"),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == NO_CONTENT
   }

   void "fetch agreement_signing records for store# 1" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final dataset = 'coravt'
      final store1 = storeFactoryService.store(1, company)
      final store3 = storeFactoryService.store(3, company)
      final agreementSigning1 = agreementSigningService.single(company, store1 as StoreEntity, 123456, 111111, 654321, "R", 1, UUID.randomUUID())
      final agreementSigning2 = agreementSigningService.single(company, store1 as StoreEntity, 123456, 111111,876543, "R", 1, UUID.randomUUID())
      final agreementSigning3 = agreementSigningService.single(company, store1 as StoreEntity, 123456, 111111,987654, "R", 1, UUID.randomUUID())
      final agreementSigning4 = agreementSigningService.single(company, store1 as StoreEntity, 222222, 333333,990000, "R", 1, UUID.randomUUID())
      final agreementSigning5 = agreementSigningService.single(company, store3 as StoreEntity, 222222, 333333,999999, "R", 1, UUID.randomUUID())
      final agreementSigningArray = [agreementSigning1, agreementSigning2, agreementSigning3, agreementSigning4, agreementSigning5]
      final agreementSigningPage1 = new AgreementSigningPageRequest(1, 5, "asn_agreement_number", "ASC", store1.myNumber(), null, null)

      when:
      def result = signingClient.toBlocking().exchange(GET("/paged/dataset/${dataset}${agreementSigningPage1}"),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      result.elements.eachWithIndex { result2, index ->
         with(result2) {
            primaryCustomerNumber == agreementSigningArray[index].primaryCustomerNumber
            agreementNumber == agreementSigningArray[index].agreementNumber
            agreementType == agreementSigningArray[index].agreementType
            externalSignatureId == agreementSigningArray[index].externalSignatureId.toString()
         }
      }
      result.elements != null
      result.elements.size() == 4
      result.totalElements == 4
      result.totalPages == 1
      result.first == true
      result.last == true
   }

   void "update an agreement signing record" () {
      given: 'store number 1 is assigned to a region of company tstds1'
      final companyEntity = companyFactoryService.forDatasetCode('coravt')
      final dataset = 'coravt'
      final store = storeFactoryService.random(companyEntity)
      final company = new CompanyDTO(companyEntity)
      final SLN = new SimpleLegacyNumberDTO(store.number)
      final externalId = UUID.randomUUID()
      final agreementSigning = agreementSigningService.single(companyEntity, store, 123456, 111111, 654321, "R", 1, externalId)
      final newDTO = new AgreementSigningDTO(agreementSigning.id.toString(), company, SLN, 123456, 111111, 654321, "R", 1, externalId.toString())

      when:
      def result = signingClient.toBlocking().exchange(PUT("/${agreementSigning.id}/dataset/${dataset}", newDTO),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      final extraDTO = new AgreementSigningDTO(agreementSigning.id.toString(), company, SLN, 123456, 111111, 654321, "R", 1, externalId.toString())
      result.agreementType == extraDTO.agreementType
      result.externalSignatureId == extraDTO.externalSignatureId.toString()
   }

   void "fetch list of agreements for a single customer" () {
      given: 'store number 1 is assigned to a region of company tstds1'
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.random(company)
      final dataset = 'coravt'
      final customerNumber = 123456
      final agreement1 = agreementSigningService.single(company, store, 123456, 111111, 654322, "R", 1, UUID.randomUUID())
      final agreement2 = agreementSigningService.single(company, store, 123456, 111111, 654323, "R", 1, UUID.randomUUID())
      final agreement3 = agreementSigningService.single(company, store, 123456, 111111, 654324, "R", 1, UUID.randomUUID())
      final agreement4 = agreementSigningService.single(company, store, 123456, 111111, 654325, "R", 1, UUID.randomUUID())

      when:
      def result = signingClient.toBlocking().exchange(GET("/customerAgreements/${dataset}/${customerNumber}/"),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      result[0].agreementNumber == 654322
      result[1].agreementNumber == 654323
      result[2].agreementNumber == 654324
      result[3].agreementNumber == 654325
   }

   void "fetch list of agreements for a single customer that does not exist" () {
      given: 'store number 1 is assigned to a region of company tstds1'
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.random(company)
      final dataset = 'coravt'
      final customerNumber = 222222
      final agreement1 = agreementSigningService.single(company, store, 123456, 111111, 654322, "R", 1, UUID.randomUUID())
      final agreement2 = agreementSigningService.single(company, store, 123456, 111111, 654323, "R", 1, UUID.randomUUID())
      final agreement3 = agreementSigningService.single(company, store, 123456, 111111, 654324, "R", 1, UUID.randomUUID())
      final agreement4 = agreementSigningService.single(company, store, 123456, 111111, 654325, "R", 1, UUID.randomUUID())

      when:
      def result = signingClient.toBlocking().exchange(GET("/customerAgreements/${dataset}/${customerNumber}/"),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      result[0] == null
   }
}
