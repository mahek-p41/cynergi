package com.cynergisuite.middleware.agreement.signing.infrastructure

import com.cynergisuite.domain.SimpleLegacyNumberDTO
import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.agreement.signing.AgreementSigningDTO
import com.cynergisuite.middleware.agreement.signing.AgreementSigningTestDataLoaderService
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
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class AgreementSigningControllerSpecification extends ServiceSpecificationBase {
   @Client("/agreement/signing") @Inject HttpClient signingClient

   @Inject AgreementSigningTestDataLoaderService agreementSigningService
   @Inject StoreTestDataLoaderService storeFactoryService

   void "fetch one agreement signing record by id" () {
      given: 'store number 1 is assigned to a region of company tstds1'
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.random(company)
      final dataset = 'tstds1'
      final agreementSigning = agreementSigningService.single(company, store, 123456, 111111, 654321, "R", 1, "ABC123")

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

   void "fetch three agreement_signing records for customer# 123456" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final dataset = 'tstds1'
      final store = storeFactoryService.random(company)
      final agreementSigning1 = agreementSigningService.single(company, store, 123456, 111111, 654321, "R", 1, "ABC123")
      final agreementSigning2 = agreementSigningService.single(company, store, 123456, 111111, 876543, "R", 1, "ABC789")
      final agreementSigning3 = agreementSigningService.single(company, store, 123456, 111111, 987654, "R", 1, "ABC456")
      final agreementSigning4 = agreementSigningService.single(company, store, 222222, 333333, 444444, "R", 1, "ABC101")
      final agreementSigning5 = agreementSigningService.single(company, store, 222222, 333333, 555555, "R", 1, "ABC112")
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
            externalSignatureId == agreementSigningArray[index].externalSignatureId
         }
      }
      result.elements != null
      result.elements.size() == 3
      result.totalElements == 3
      result.totalPages == 1
      result.first == true
      result.last == true
   }

   void "fetch agreement_signing records for made up/missing customer# 999999" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final dataset = 'tstds1'
      final store = storeFactoryService.random(company)
      final agreementSigning1 = agreementSigningService.single(company, store, 123456, 111111, 654321, "R", 1, "ABC123")
      final agreementSigning2 = agreementSigningService.single(company, store, 123456, 111111, 876543, "R", 1, "ABC789")
      final agreementSigning3 = agreementSigningService.single(company, store, 123456, 111111, 987654, "R", 1, "ABC456")
      final agreementSigning4 = agreementSigningService.single(company, store, 222222, 333333, 444444, "R", 1, "ABC101")
      final agreementSigning5 = agreementSigningService.single(company, store, 222222, 333333, 555555, "R", 1, "ABC112")
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
      final company = companyFactoryService.forDatasetCode('tstds1')
      final dataset = 'tstds1'
      final store1 = storeFactoryService.store(1, company)
      final store3 = storeFactoryService.store(3, company)
      final agreementSigning1 = agreementSigningService.single(company, store1 as StoreEntity, 123456, 111111, 654321, "R", 1, "ABC654")
      final agreementSigning2 = agreementSigningService.single(company, store1 as StoreEntity, 123456, 111111,876543, "R", 1, "ABC876")
      final agreementSigning3 = agreementSigningService.single(company, store1 as StoreEntity, 123456, 111111,987654, "R", 1, "ABC987")
      final agreementSigning4 = agreementSigningService.single(company, store1 as StoreEntity, 222222, 333333,990000, "R", 1, "ABC990")
      final agreementSigning5 = agreementSigningService.single(company, store3 as StoreEntity, 222222, 333333,999999, "R", 1, "ABC999")
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
            externalSignatureId == agreementSigningArray[index].externalSignatureId
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
      final company = companyFactoryService.forDatasetCode('tstds1')
      final dataset = 'tstds1'
      final store = storeFactoryService.random(company)
      final SLN = new SimpleLegacyNumberDTO(store.number)
      final agreementSigning = agreementSigningService.single(company, store, 123456, 111111, 654321, "R", 1, "ABC123")
      final newDTO = new AgreementSigningDTO(agreementSigning.id, company, SLN, 123456, 111111, 654321, "R", 1, "XYZ456")

      when:
      def result = signingClient.toBlocking().exchange(PUT("/${agreementSigning.id}/dataset/${dataset}", newDTO),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      notThrown(HttpClientResponseException)
      final extraDTO = new AgreementSigningDTO(agreementSigning.id, company, SLN, 123456, 111111, 654321, "R", 1, "XYZ456")
      result.agreementType == extraDTO.agreementType
      result.externalSignatureId == extraDTO.externalSignatureId
   }
}