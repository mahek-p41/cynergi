package com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionDetailDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionTemplateDTO
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionTemplateDataLoaderService
import com.cynergisuite.middleware.store.StoreDTO
import com.cynergisuite.middleware.store.StoreTestDataLoader
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Unroll

import static io.micronaut.http.HttpStatus.*

@MicronautTest(transactional = false)
class AccountPayableDistributionDetailControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/accounting/account-payable/distribution/detail'

   @Inject AccountPayableDistributionDetailDataLoaderService dataLoaderService
   @Inject AccountPayableDistributionTemplateDataLoaderService templateDataLoaderService

   @Inject AccountTestDataLoaderService accountDataLoaderService

   void "fetch one account payable distribution detail by id" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final template = templateDataLoaderService.single(company)
      final apDistribution = dataLoaderService.single(store, acct, company, template)

      when:
      def result = get("$path/${apDistribution.id}")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == apDistribution.id
         distributionTemplate.id == apDistribution.distributionTemplate.id
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
      final template = templateDataLoaderService.single(company)
      final apDistributions = dataLoaderService.stream(12,store, acct, company, template).toList()
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
            distributionTemplate.id == firstPageAPDist[index].distributionTemplate.id
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
            distributionTemplate.id == secondPageAPDist[index].distributionTemplate.id
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
            distributionTemplate.id == lastPageAPDist[index].distributionTemplate.id
         }
      }

      when:
      get("$path/${pageFour}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "fetch all account payable distributions by template id" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final acct2 = accountDataLoaderService.single(company)
      final template1 = templateDataLoaderService.single(company)
      final template2 = templateDataLoaderService.single(company)
      def accountingGroup = []
      def insuranceGroup = []

      2.times {
         accountingGroup.add(dataLoaderService.single(StoreTestDataLoader.stores(company)[it], acct, company, template1))
      }
      2.times {
         insuranceGroup.add(dataLoaderService.single(StoreTestDataLoader.stores(company)[it], acct2, company, template2))
      }

      when:
      def result = get("$path/template/${accountingGroup[0].distributionTemplate.id}")

      then:
      result.totalElements == 2
      result.totalPages == 1
      result.first == true
      result.last == true
      result.elements.size() == 2
      result.elements.every { it.distributionTemplate.name == template1.name }

   }

   void "create valid account payable distribution"() {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final template = templateDataLoaderService.single(company)
      final apDistribution = dataLoaderService.singleDTO(new StoreDTO(store), new AccountDTO(acct), new AccountPayableDistributionTemplateDTO(template))

      when:
      def result = post("$path/", apDistribution)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id != null
         distributionTemplate.name == apDistribution.distributionTemplate.name
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
      final template = templateDataLoaderService.single(company)
      final apDistributionDTO = dataLoaderService.singleDTO(new StoreDTO(store), new AccountDTO(acct), new AccountPayableDistributionTemplateDTO(template))
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
      'percent'                                    || 'percent'
      'profitCenter'                               || 'profitCenter'
   }

   void "create invalid account payable distribution with non-existing profit center" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final template = templateDataLoaderService.single(company)
      final apDistributionDTO = dataLoaderService.singleDTO(new StoreDTO(store), new AccountDTO(acct), new AccountPayableDistributionTemplateDTO(template))
      final invalidValue = new StoreDTO(store)
      invalidValue.id = 999999
      apDistributionDTO["profitCenter"] = invalidValue

      when:
      post("$path", apDistributionDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'profitCenter.id'
      response[0].message == "999999 was unable to be found"
      response[0].code == 'system.not.found'
   }

   void "create invalid account payable distribution with non-existing Account" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final template = templateDataLoaderService.single(company)
      final apDistributionDTO = dataLoaderService.singleDTO(new StoreDTO(store), new AccountDTO(acct), new AccountPayableDistributionTemplateDTO(template))
      final badDTO = new AccountDTO(acct)
      badDTO.id = UUID.fromString("ee2359b6-c88c-11eb-8098-02420a4d0702")
      final invalidValue =  badDTO
      apDistributionDTO["account"] = invalidValue

      when:
      post("$path", apDistributionDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'account.id'
      response[0].message == "ee2359b6-c88c-11eb-8098-02420a4d0702 was unable to be found"
      response[0].code == 'system.not.found'
   }

   @Unroll
   void "create invalid account payable distribution with percent = #invalidPercent" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final template = templateDataLoaderService.single(company)
      final apDistributionDTO = dataLoaderService.singleDTO(new StoreDTO(store), new AccountDTO(acct), new AccountPayableDistributionTemplateDTO(template))
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
      final template = templateDataLoaderService.single(company)
      def apDistributions = []
      accounts.eachWithIndex { account, index ->
         apDistributions.add(dataLoaderService.singleDTO(new StoreDTO(store), new AccountDTO(account), new AccountPayableDistributionTemplateDTO(template)))
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
      final template = templateDataLoaderService.single(company)
      final existingAPDistribution = dataLoaderService.single(store, acct, company, template)
      final updatedAPDistributionDTO = dataLoaderService.singleDTO(new StoreDTO(store), new AccountDTO(acct), new AccountPayableDistributionTemplateDTO(template))
      updatedAPDistributionDTO.id = existingAPDistribution.id

      when:
      def result = put("$path/${existingAPDistribution.id}", updatedAPDistributionDTO)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == existingAPDistribution.id
         distributionTemplate.name == updatedAPDistributionDTO.distributionTemplate.name
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
      final template = templateDataLoaderService.single(company)
      final existingAPDistribution = dataLoaderService.single(store, acct, company, template)
      def updatedAPDistributionDTO = dataLoaderService.singleDTO(new StoreDTO(store), new AccountDTO(acct), new AccountPayableDistributionTemplateDTO(template))
      updatedAPDistributionDTO.account = null
      updatedAPDistributionDTO.distributionTemplate = null
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
      response[1].path == 'distributionTemplate'
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
      final template = templateDataLoaderService.single(company)
      final existingAPDistribution = dataLoaderService.single(store, acct, company, template)
      final updatedAPDistributionDTO = dataLoaderService.singleDTO(new StoreDTO(store), new AccountDTO(acct), new AccountPayableDistributionTemplateDTO(template))
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
      final template = templateDataLoaderService.single(company)
      final existingAPDistribution = dataLoaderService.single(store, acct, company, template)
      def updatedAPDistributionDTO = dataLoaderService.singleDTO(new StoreDTO(store), new AccountDTO(acct), new AccountPayableDistributionTemplateDTO(template))
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
      final template = templateDataLoaderService.single(company)
      def existingAPDistributions = []
      def updatedAPDistDTOs = []
      accounts.eachWithIndex { account, index ->
         existingAPDistributions.add(dataLoaderService.single(store, account, company, template))
         updatedAPDistDTOs.add(dataLoaderService.singleDTO(new StoreDTO(store), new AccountDTO(account), new AccountPayableDistributionTemplateDTO(template)))
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
      final template = templateDataLoaderService.single(company)
      final apDistribution = dataLoaderService.single(store, acct, company, template)

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
      final template = templateDataLoaderService.single(tstds2)
      final apDistribution = dataLoaderService.single(store, acct, tstds2, template)

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
      final template = templateDataLoaderService.single(company)
      final apDistribution = dataLoaderService.singleDTO(new StoreDTO(store), new AccountDTO(acct), new AccountPayableDistributionTemplateDTO(template))

      when: // create a account payable distribution
      def response1 = post("$path/", apDistribution)

      then:
      notThrown(HttpClientResponseException)

      with(response1) {
         id != null
         distributionTemplate.name == apDistribution.distributionTemplate.name
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
         distributionTemplate.name == apDistribution.distributionTemplate.name
         profitCenter.id == apDistribution.profitCenter.id
         account.id == apDistribution.account.id
         percent == apDistribution.percent
      }

      when: // delete account payable distribution again
      delete("$path/$response2.id")

      then: "account payable distribution of user's company is deleted"
      notThrown(HttpClientResponseException)
   }

   void "update valid list of account payable distribution" () {
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final accounts = accountDataLoaderService.stream(5, company).toList()
      final template = templateDataLoaderService.single(company)
      def existingAPDistributions = []
      def updatedAPDistDTOs = []
      accounts.eachWithIndex { account, index ->
         existingAPDistributions.add(dataLoaderService.single(store, account, company, template))
         updatedAPDistDTOs.add(dataLoaderService.singleDTO(new StoreDTO(store), new AccountDTO(account), new AccountPayableDistributionTemplateDTO(template)))
      }
      updatedAPDistDTOs.eachWithIndex{ dto, index ->
         dto.id = existingAPDistributions[index].id
         dto.percent = 0.2
      }
      when:
      def result = put("$path", updatedAPDistDTOs)

      then:
      notThrown(HttpClientResponseException)
      with(result) {
         result.eachWithIndex { dto, index ->
            with(dto) {
               id == dto.id
               distributionTemplate.name == dto.distributionTemplate.name
               profitCenter.id == dto.profitCenter.id
               account.id == dto.account.id
               percent == dto.percent
            }
         }
      }
   }
}
