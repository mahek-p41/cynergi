package com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionDataLoaderService
import com.cynergisuite.middleware.store.StoreTestDataLoader
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
   @Inject AccountTestDataLoaderService accountDataLoaderService

   void "fetch one account payable distribution by id" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final apDistribution = dataLoaderService.single(store, acct, company)

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
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final apDistributions = dataLoaderService.stream(12, store, acct, company).toList()
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
            profitCenter.id == firstPageAPDist[index].profitCenter.id
            account.id == firstPageAPDist[index].account.id
            percent == firstPageAPDist[index].percent
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
            profitCenter.id == secondPageAPDist[index].profitCenter.id
            account.id == secondPageAPDist[index].account.id
            percent == secondPageAPDist[index].percent
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
            profitCenter.id == lastPageAPDist[index].profitCenter.id
            account.id == lastPageAPDist[index].account.id
            percent == lastPageAPDist[index].percent
         }
      }

      when:
      get("$path/${pageFour}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "fetch a list of account payable distribution groups" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final apDistributions = dataLoaderService.stream(12, store, acct, company).sorted { o1, o2 ->
         o1.name <=> o2.name
      }.toList()
      final pageRequest = new StandardPageRequest(1, 20, "name", "ASC")

      when:
      def result = get("$path/groups${pageRequest}")

      then:
      result.requested.with { new StandardPageRequest(it) } == pageRequest
      result.totalElements == 12
      result.totalPages == 1
      result.first == true
      result.last == true
      result.elements.size() == 12
      result.elements == apDistributions.collect { it.name }.toList()
   }

   void "fetch all account payable distributions by group" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final acct = accountDataLoaderService.single(company)
      final acct2 = accountDataLoaderService.single(company)
      def accountingGroup = []
      def insuranceGroup = []

      2.times {
         accountingGroup.add(dataLoaderService.single(StoreTestDataLoader.stores(company)[it], acct, company, 'Accounting'))
      }
      2.times {
         insuranceGroup.add(dataLoaderService.single(StoreTestDataLoader.stores(company)[it], acct2, company, 'Insurance'))
      }

      when:
      def result = get("$path/name/Accounting")

      then:
      result.totalElements == 2
      result.totalPages == 1
      result.first == true
      result.last == true
      result.elements.size() == 2
      result.elements.every { it.name == 'Accounting' }

   }

   void "create valid account payable distribution"() {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final apDistribution = dataLoaderService.singleDTO(new SimpleLegacyIdentifiableDTO(store.myId()), new SimpleIdentifiableDTO(acct.myId()), null)

      when:
      def result = post("$path/", apDistribution)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id != null
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
      final apDistributionDTO = dataLoaderService.singleDTO(new SimpleLegacyIdentifiableDTO(store.myId()), new SimpleIdentifiableDTO(acct.myId()), null)
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
      response[0].code == 'javax.validation.constraints.NotNull.message'

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
      final apDistributionDTO = dataLoaderService.singleDTO(new SimpleLegacyIdentifiableDTO(store.myId()), new SimpleIdentifiableDTO(acct.myId()), null)
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
      response[0].code == 'system.not.found'
      where:
      testProp       | invalidValue                                                                       || errorResponsePath | errorMessage
      'account'      | new SimpleIdentifiableDTO(UUID.fromString('ee2359b6-c88c-11eb-8098-02420a4d0702')) || 'account.id'      | "ee2359b6-c88c-11eb-8098-02420a4d0702 was unable to be found"
      'profitCenter' | new SimpleLegacyIdentifiableDTO(999999)                                            || 'profitCenter.id' | "999999 was unable to be found"
   }

   @Unroll
   void "create invalid account payable distribution with percent = #invalidPercent" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final apDistributionDTO = dataLoaderService.singleDTO(new SimpleLegacyIdentifiableDTO(store.myId()), new SimpleIdentifiableDTO(acct.myId()), null)
      apDistributionDTO["$percent"] = invalidPercent

      when:
      post("$path", apDistributionDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorPath
      response[0].code == errorCode
      response[0].message == errorMessage

      where:
      percent      | invalidPercent    || errorPath   | errorCode                                         | errorMessage
      'percent'    | (-0.1)            || 'percent'   | 'javax.validation.constraints.DecimalMin.message' | 'must be greater than or equal to value'
      'percent'    | 1.2               || 'percent'   | 'javax.validation.constraints.DecimalMax.message' | 'must be less than or equal to value'
   }

   void "create invalid account payable distribution group with percent total over 100%" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final accounts = accountDataLoaderService.stream(5, company).toList()
      def apDistributions = []
      accounts.eachWithIndex { account, index ->
         apDistributions.add(dataLoaderService.singleDTO(new SimpleLegacyIdentifiableDTO(store.myId()), new SimpleIdentifiableDTO(account.myId()), "test"))
      }
      apDistributions[4].percent = 1

      when:
      def result
      apDistributions.each {
         result = post("$path/", it)
      }

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "percent"
      response[0].message == "Percent total cannot exceed 100%"
      response[0].code == "cynergi.validation.percent.total.greater.than.100"
   }

   void "update valid account payable distribution" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final existingAPDistribution = dataLoaderService.single(store, acct, company)
      final updatedAPDistributionDTO = dataLoaderService.singleDTO(new SimpleLegacyIdentifiableDTO(store.myId()), new SimpleIdentifiableDTO(acct.myId()), null)
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
      final existingAPDistribution = dataLoaderService.single(store, acct, company)
      def updatedAPDistributionDTO = dataLoaderService.singleDTO(new SimpleLegacyIdentifiableDTO(store.myId()), new SimpleIdentifiableDTO(acct.myId()), null)
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
      final nonExistentAccountId = UUID.randomUUID()
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final existingAPDistribution = dataLoaderService.single(store, acct, company)
      final updatedAPDistributionDTO = dataLoaderService.singleDTO(new SimpleLegacyIdentifiableDTO(store.myId()), new SimpleIdentifiableDTO(acct.myId()), null)
      updatedAPDistributionDTO.account.id = nonExistentAccountId
      updatedAPDistributionDTO.profitCenter.id = 999999

      when:
      put("$path/${existingAPDistribution.id}", updatedAPDistributionDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson().collect().sort { a,b -> a.path <=> b.path }
      response.size() == 2
      response[0].path == 'account.id'
      response[0].message == "$nonExistentAccountId was unable to be found"
      response[0].code == 'system.not.found'
      response[1].path == 'profitCenter.id'
      response[1].message == '999999 was unable to be found'
      response[1].code == 'system.not.found'
   }

   @Unroll
   void "update invalid account payable distribution with percent = #invalidPercent" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final existingAPDistribution = dataLoaderService.single(store, acct, company)
      def updatedAPDistributionDTO = dataLoaderService.singleDTO(new SimpleLegacyIdentifiableDTO(store.myId()), new SimpleIdentifiableDTO(acct.myId()), null)
      updatedAPDistributionDTO["$percent"] = invalidPercent

      when:
      put("$path/${existingAPDistribution.id}", updatedAPDistributionDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorPath
      response[0].code == errorCode
      response[0].message == errorMessage

      where:
      percent      | invalidPercent    || errorPath   | errorCode                                         | errorMessage
      'percent'    | (-0.1)            || 'percent'   | 'javax.validation.constraints.DecimalMin.message' | 'must be greater than or equal to value'
      'percent'    | 1.2               || 'percent'   | 'javax.validation.constraints.DecimalMax.message' | 'must be less than or equal to value'
   }

   void "update invalid account payable distribution group with percent total over 100%" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final accounts = accountDataLoaderService.stream(5, company).toList()
      def existingAPDistributions = []
      def updatedAPDistDTOs = []
      accounts.eachWithIndex { account, index ->
         existingAPDistributions.add(dataLoaderService.single(store, account, company))
         updatedAPDistDTOs.add(dataLoaderService.singleDTO(new SimpleLegacyIdentifiableDTO(store.myId()), new SimpleIdentifiableDTO(account.myId()), "test"))
      }
      updatedAPDistDTOs.eachWithIndex{ dto, index ->
         dto.id = existingAPDistributions[index].id
      }
      updatedAPDistDTOs[4].percent = 1

      when:
      def result
      updatedAPDistDTOs.each {
         result = put("$path/${it.id}", it)
      }

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "percent"
      response[0].message == "Percent total cannot exceed 100%"
      response[0].code == "cynergi.validation.percent.total.greater.than.100"
   }

   void "delete one account payable distribution" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final apDistribution = dataLoaderService.single(store, acct, company)

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
      final apDistribution = dataLoaderService.single(store, acct, tstds2)

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
      final apDistribution = dataLoaderService.singleDTO(new SimpleLegacyIdentifiableDTO(store.myId()), new SimpleIdentifiableDTO(acct.myId()), null)

      when: // create a account payable distribution
      def response1 = post("$path/", apDistribution)

      then:
      notThrown(HttpClientResponseException)

      with(response1) {
         id != null
         name == apDistribution.name
         profitCenter.id == apDistribution.profitCenter.id
         account.id == apDistribution.account.id
         percent == apDistribution.percent
      }

      when: // delete account payable distribution
      delete("$path/$response1.id")

      then: "account payable distribution of user's company is deleted"
      notThrown(HttpClientResponseException)

      when: // recreate account payable distribution
      def response2 = post("$path/", apDistribution)

      then:
      notThrown(HttpClientResponseException)

      with(response2) {
         id != null
         name == apDistribution.name
         profitCenter.id == apDistribution.profitCenter.id
         account.id == apDistribution.account.id
         percent == apDistribution.percent
      }

      when: // delete account payable distribution again
      delete("$path/$response2.id")

      then: "account payable distribution of user's company is deleted"
      notThrown(HttpClientResponseException)
   }
}
