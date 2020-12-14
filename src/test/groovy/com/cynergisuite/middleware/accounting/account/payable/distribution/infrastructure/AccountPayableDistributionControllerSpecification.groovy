package com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NO_CONTENT
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class AccountPayableDistributionControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/accounting/account-payable/distribution'

   @Inject AccountPayableDistributionDataLoaderService dataLoaderService
   @Inject AccountDataLoaderService accountDataLoaderService

   void "fetch one account payable distribution by id" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final def apDistribution = dataLoaderService.single(store, acct, company)

      when:
      def result = get("$path/${apDistribution.id}")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == apDistribution.id
         name == apDistribution.name
         profitCenter.id == apDistribution.profitCenter.id
         account.id == apDistribution.account.id
         percent == apDistribution.percent
      }
   }

   void "fetch one account payable distribution not found" () {
      when:
      get("$path/0")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == 'Account payable distribution was unable to be found'
   }

   void "fetch all" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      dataLoaderService.stream(5, store, acct, companyFactoryService.forDatasetCode('tstds2')).toList()
      final apDistributions = dataLoaderService.stream(12, store, acct, company).toList()
      def pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      def pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      def pageLast = new StandardPageRequest(3, 5, "id", "ASC")
      def pageFour = new StandardPageRequest(4, 5, "id", "ASC")
      def firstPageAccount = apDistributions[0..4]
      def secondPageAccount = apDistributions[5..9]
      def lastPageAccount = apDistributions[10,11]

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
            id == firstPageAccount[index].id
            name == firstPageAccount[index].name
            profitCenter.id == firstPageAccount[index].profitCenter.id
            account.id == firstPageAccount[index].account.id
            percent == firstPageAccount[index].percent
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
            id == secondPageAccount[index].id
            name == secondPageAccount[index].name
            profitCenter.id == secondPageAccount[index].profitCenter.id
            account.id == secondPageAccount[index].account.id
            percent == secondPageAccount[index].percent
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
            id == lastPageAccount[index].id
            name == lastPageAccount[index].name
            profitCenter.id == lastPageAccount[index].profitCenter.id
            account.id == lastPageAccount[index].account.id
            percent == lastPageAccount[index].percent
         }
      }

      when:
      get("$path/${pageFour}")

      then:
      final def notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create valid account payable distribution"() {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final def apDistribution = dataLoaderService.singleDTO(new SimpleIdentifiableDTO(store.myId()), new SimpleIdentifiableDTO(acct.myId()))

      when:
      def result = post("$path/", apDistribution)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
         name == apDistribution.name
         profitCenter.id == apDistribution.profitCenter.id
         account.id == apDistribution.account.id
         percent == apDistribution.percent
      }
   }

   @Unroll
   void "create invalid account payable distribution without #nonNullableProp"() {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final def apDistributionDTO = dataLoaderService.singleDTO(new SimpleIdentifiableDTO(store.myId()), new SimpleIdentifiableDTO(acct.myId()))
      apDistributionDTO["$nonNullableProp"] = null

      when:
      post("$path/", apDistributionDTO)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp                              || errorResponsePath
      'account'                                    || 'account'
      'name'                                       || 'name'
      'percent'                                    || 'percent'
      'profitCenter'                               || 'profitCenter'
   }

   @Unroll
   void "create invalid account payable distribution with non-existing #testProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final def apDistributionDTO = dataLoaderService.singleDTO(new SimpleIdentifiableDTO(store.myId()), new SimpleIdentifiableDTO(acct.myId()))
      apDistributionDTO["$testProp"] = invalidValue

      when:
      post("$path", apDistributionDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage
      where:
      testProp          | invalidValue                         || errorResponsePath    | errorMessage
      'account'         | new SimpleIdentifiableDTO(999999)    || 'account.id'         | '999,999 was unable to be found'
      'profitCenter'    | new SimpleIdentifiableDTO(999999)    || 'profitCenter.id'    | '999,999 was unable to be found'
   }

   @Unroll
   void "create invalid account payable distribution with percent as #invalidPercent" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final def apDistributionDTO = dataLoaderService.singleDTO(new SimpleIdentifiableDTO(store.myId()), new SimpleIdentifiableDTO(acct.myId()))
      apDistributionDTO["$percent"] = invalidPercent

      when:
      post("$path", apDistributionDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage
      where:
      percent      | invalidPercent    || errorResponsePath    | errorMessage
      'percent'    | 0                 || 'percent'            | 'percent must be greater than zero'
      'percent'    | 1.2               || 'percent'            | 'Must be in range of (0, 1]'
   }

   void "update valid account payable distribution" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final def existingAPDistribution = dataLoaderService.single(store, acct, company)
      final def updatedAPDistributionDTO = dataLoaderService.singleDTO(new SimpleIdentifiableDTO(store.myId()), new SimpleIdentifiableDTO(acct.myId()))
      updatedAPDistributionDTO.id = existingAPDistribution.id

      when:
      def result = put("$path/${existingAPDistribution.id}", updatedAPDistributionDTO)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == existingAPDistribution.id
         name == updatedAPDistributionDTO.name
         profitCenter.id == updatedAPDistributionDTO.profitCenter.id
         account.id == updatedAPDistributionDTO.account.id
         percent == updatedAPDistributionDTO.percent
      }
   }

   void "update invalid account payable distribution without non-nullable properties" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final def existingAPDistribution = dataLoaderService.single(store, acct, company)
      def updatedAPDistributionDTO = dataLoaderService.singleDTO(new SimpleIdentifiableDTO(store.myId()), new SimpleIdentifiableDTO(acct.myId()))
      updatedAPDistributionDTO.account = null
      updatedAPDistributionDTO.name = null
      updatedAPDistributionDTO.percent = null
      updatedAPDistributionDTO.profitCenter = null

      when:
      put("$path/${existingAPDistribution.id}", updatedAPDistributionDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson().collect().sort { a,b -> a.path <=> b.path }
      response.size() == 4
      response[0].path == 'account'
      response[1].path == 'name'
      response[2].path == 'percent'
      response[3].path == 'profitCenter'
      response.collect { it.message } as Set == ['Is required'] as Set
   }

   void "update invalid account payable distribution with non-existing ids" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final def existingAPDistribution = dataLoaderService.single(store, acct, company)
      def updatedAPDistributionDTO = dataLoaderService.singleDTO(new SimpleIdentifiableDTO(store.myId()), new SimpleIdentifiableDTO(acct.myId()))
      updatedAPDistributionDTO.account.id = 999999
      updatedAPDistributionDTO.profitCenter.id = 999999

      when:
      put("$path/${existingAPDistribution.id}", updatedAPDistributionDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson().collect().sort { a,b -> a.path <=> b.path }
      response.size() == 2
      response[0].path == 'account.id'
      response[1].path == 'profitCenter.id'
      response[0].message == '999,999 was unable to be found'
      response[1].message == '999,999 was unable to be found'
   }

   @Unroll
   void "update invalid account payable distribution with percent as #invalidPercent" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final def existingAPDistribution = dataLoaderService.single(store, acct, company)
      def updatedAPDistributionDTO = dataLoaderService.singleDTO(new SimpleIdentifiableDTO(store.myId()), new SimpleIdentifiableDTO(acct.myId()))
      updatedAPDistributionDTO["$percent"] = invalidPercent

      when:
      put("$path/${existingAPDistribution.id}", updatedAPDistributionDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage
      where:
      percent      | invalidPercent    || errorResponsePath    | errorMessage
      'percent'    | 0                 || 'percent'            | 'percent must be greater than zero'
      'percent'    | 1.2               || 'percent'            | 'Must be in range of (0, 1]'
   }
}
