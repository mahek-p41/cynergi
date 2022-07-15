package com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionDetailDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionTemplateDataLoaderService
import com.cynergisuite.middleware.store.StoreDTO
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import jakarta.inject.Inject

import static io.micronaut.http.HttpStatus.NO_CONTENT
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class AccountPayableDistributionTemplateControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/accounting/account-payable/distribution/template'

   @Inject AccountPayableDistributionTemplateDataLoaderService dataLoaderService
   @Inject AccountPayableDistributionDetailDataLoaderService detailDataLoaderService
   @Inject AccountTestDataLoaderService accountDataLoaderService

   void "fetch one account payable distribution template by id" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final apDistribution = dataLoaderService.single(company)

      when:
      def result = get("$path/${apDistribution.id}")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == apDistribution.id
         name == apDistribution.name
      }
   }

   void "fetch one account payable distribution not found" () {
      given:
      final nonExistentAccountPayableDistribution = UUID.randomUUID()

      when:
      get("$path/$nonExistentAccountPayableDistribution")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$nonExistentAccountPayableDistribution was unable to be found"
      response.code == 'system.not.found'
   }

   void "fetch all" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final apDistributions = dataLoaderService.stream(12,company).toList()
      def pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      def pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      def pageLast = new StandardPageRequest(3, 5, "id", "ASC")
      def pageFour = new StandardPageRequest(4, 5, "id", "ASC")
      def firstPageAPDist = apDistributions[0..4]
      def secondPageAPDist = apDistributions[5..9]
      def lastPageAPDist = apDistributions[10,11]

      when:
      def pageOneResult = get("$path${pageOne}")

      then:
      pageOneResult.requested.with { new StandardPageRequest(it) } == pageOne
      pageOneResult.totalElements == 12
      pageOneResult.totalPages == 3
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements.size() == 5
      pageOneResult.elements.eachWithIndex { result, index ->
         with(result) {
            id == firstPageAPDist[index].id
            name == firstPageAPDist[index].name
         }
      }

      when:
      def pageTwoResult = get("$path${pageTwo}")

      then:
      pageTwoResult.requested.with { new StandardPageRequest(it) } == pageTwo
      pageTwoResult.totalElements == 12
      pageTwoResult.totalPages == 3
      pageTwoResult.first == false
      pageTwoResult.last == false
      pageTwoResult.elements.size() == 5
      pageTwoResult.elements.eachWithIndex { result, index ->
         with(result) {
            id == secondPageAPDist[index].id
            name == secondPageAPDist[index].name
         }
      }

      when:
      def pageLastResult = get("$path${pageLast}")

      then:
      pageLastResult.requested.with { new StandardPageRequest(it) } == pageLast
      pageLastResult.totalElements == 12
      pageLastResult.totalPages == 3
      pageLastResult.first == false
      pageLastResult.last == true
      pageLastResult.elements.size() == 2
      pageLastResult.elements.eachWithIndex { result, index ->
         with(result) {
            id == lastPageAPDist[index].id
            name == lastPageAPDist[index].name
         }
      }

      when:
      get("$path/${pageFour}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create valid account payable distribution"() {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final apDistribution = dataLoaderService.singleDTO()
      final apDistributionDetail = apDistribution.name = "test"

      when:
      def result = post("$path/", apDistribution)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id != null
         name == apDistribution.name
      }
   }

   void "update valid account payable distribution template" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final existingAPDistribution = dataLoaderService.single(company)
      final updatedAPDistributionDTO = dataLoaderService.singleDTO()
      updatedAPDistributionDTO.id = existingAPDistribution.id
      updatedAPDistributionDTO.name = "new name"

      when:
      def result = put("$path/${existingAPDistribution.id}", updatedAPDistributionDTO)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == existingAPDistribution.id
         name == updatedAPDistributionDTO.name
      }
   }

   void "delete one account payable distribution" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final apDistribution = dataLoaderService.single(company)
      final detail = detailDataLoaderService.single(store, acct, company, apDistribution)

      when:
      delete("$path/${apDistribution.id}")

      then:
      notThrown(Exception)

      when:
      get("$path/${apDistribution.id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${apDistribution.id} was unable to be found"
      response.code == 'system.not.found'
   }

   void "delete account payable distribution from other company is not allowed" () {
      given:
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      final store = storeFactoryService.store(3, tstds2)
      final acct = accountDataLoaderService.single(tstds2)
      final apDistribution = dataLoaderService.single(tstds2)
      final detail = detailDataLoaderService.single(store, acct, tstds2, apDistribution)

      when:
      delete("$path/${apDistribution.id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${apDistribution.id} was unable to be found"
      response.code == 'system.not.found'
   }

   void "recreate deleted account payable distribution" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final apDistribution = dataLoaderService.singleDTO()
      final apDistDetail = detailDataLoaderService.singleDTO(new StoreDTO(store), new AccountDTO(acct), apDistribution)


      when: // create a account payable distribution
      def response1 = post("$path/", apDistribution)
      apDistDetail.distributionTemplate.id = response1.id
      post("/accounting/account-payable/distribution/detail", apDistDetail)

      then:
      notThrown(HttpClientResponseException)

      with(response1) {
         id != null
         name == apDistribution.name
      }

      when: // delete account payable distribution
      delete("$path/$response1.id")

      then: "account payable distribution of user's company is deleted"
      notThrown(HttpClientResponseException)

      when: // recreate account payable distribution
      def response2 = post("$path/", apDistribution)
      apDistDetail.distributionTemplate.id = response2.id
      post("/accounting/account-payable/distribution/detail", apDistDetail)

      then:
      notThrown(HttpClientResponseException)

      with(response2) {
         id != null
         name == apDistribution.name
      }

      when: // delete account payable distribution again
      delete("$path/$response2.id")

      then: "account payable distribution of user's company is deleted"
      notThrown(HttpClientResponseException)
   }
}
