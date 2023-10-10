package com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringDTO
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.GeneralLedgerRecurringDistributionDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.GeneralLedgerRecurringDistributionEntity
import com.cynergisuite.middleware.store.StoreDTO
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Unroll

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class GeneralLedgerRecurringDistributionControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/accounting/general-ledger/recurring/distribution"

   @Inject AccountTestDataLoaderService accountDataLoaderService
   @Inject GeneralLedgerRecurringDataLoaderService generalLedgerRecurringDataLoaderService
   @Inject GeneralLedgerRecurringDistributionDataLoaderService dataLoaderService
   @Inject GeneralLedgerSourceCodeDataLoaderService generalLedgerSourceCodeDataLoaderService

   void "fetch one" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glRecurringDistribution = dataLoaderService.single(glRecurring, account, profitCenter)

      when:
      def result = get("$path/${glRecurringDistribution.id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == glRecurringDistribution.id
         generalLedgerRecurring.id == glRecurringDistribution.generalLedgerRecurringId
         generalLedgerDistributionAccount.id == glRecurringDistribution.generalLedgerDistributionAccount.myId()
         generalLedgerDistributionProfitCenter.id == glRecurringDistribution.generalLedgerDistributionProfitCenter.myId()
         generalLedgerDistributionAmount == glRecurringDistribution.generalLedgerDistributionAmount
      }
   }

   void "fetch one not found" () {
      given:
      final nonExistentId = UUID.randomUUID()

      when:
      get("$path/$nonExistentId")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$nonExistentId was unable to be found"
      response.code == 'system.not.found'
   }

   void "fetch all" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glRecurringDistributions = dataLoaderService.stream(3, glRecurring, account, profitCenter).toList()
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")

      when:
      def result = get("$path$pageOne")

      then:
      notThrown(Exception)
      with(result) {
         requested.with { new StandardPageRequest(it) } == pageOne
         totalElements == 3
         totalPages == 1
         first == true
         last == true
         elements.eachWithIndex { pageOneResult, index ->
            with(pageOneResult) {
               id == glRecurringDistributions[index].id
               generalLedgerRecurring.id == glRecurringDistributions[index].generalLedgerRecurringId
               generalLedgerDistributionAccount.id == glRecurringDistributions[index].generalLedgerDistributionAccount.myId()
               generalLedgerDistributionProfitCenter.id == glRecurringDistributions[index].generalLedgerDistributionProfitCenter.myId()
               generalLedgerDistributionAmount == glRecurringDistributions[index].generalLedgerDistributionAmount
            }
         }
      }

      when:
      get("$path$pageTwo")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "fetch all by gl recurring id" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring1 = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final glRecurring2 = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glRecurringDistributions = dataLoaderService.stream(3, glRecurring1, account, profitCenter).toList()
      glRecurringDistributions.add(dataLoaderService.single(glRecurring2, account, profitCenter))
      glRecurringDistributions.add(dataLoaderService.single(glRecurring2, account, profitCenter))
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")

      when: // fetch records with first gl recurring id
      def result = get("$path/recurring-id/${glRecurring1.id}$pageOne")

      then: // first three records are found
      notThrown(Exception)
      with(result) {
         requested.with { new StandardPageRequest(it) } == pageOne
         totalElements == 3
         totalPages == 1
         first == true
         last == true
         elements.eachWithIndex { pageOneResult, index ->
            with(pageOneResult) {
               id == glRecurringDistributions[index].id
               generalLedgerRecurring.id == glRecurringDistributions[index].generalLedgerRecurringId
               generalLedgerDistributionAccount.id == glRecurringDistributions[index].generalLedgerDistributionAccount.myId()
               generalLedgerDistributionProfitCenter.id == glRecurringDistributions[index].generalLedgerDistributionProfitCenter.myId()
               generalLedgerDistributionAmount == glRecurringDistributions[index].generalLedgerDistributionAmount
            }
         }
      }

      when: // fetch records with second gl recurring id
      result = get("$path/recurring-id/${glRecurring2.id}$pageOne")

      then: // last two records are found
      notThrown(Exception)
      with(result) {
         requested.with { new StandardPageRequest(it) } == pageOne
         totalElements == 2
         totalPages == 1
         first == true
         last == true
         elements.eachWithIndex { pageOneResult, index ->
            with(pageOneResult) {
               id == glRecurringDistributions[index + 3].id
               generalLedgerRecurring.id == glRecurringDistributions[index + 3].generalLedgerRecurringId
               generalLedgerDistributionAccount.id == glRecurringDistributions[index + 3].generalLedgerDistributionAccount.myId()
               generalLedgerDistributionProfitCenter.id == glRecurringDistributions[index + 3].generalLedgerDistributionProfitCenter.myId()
               generalLedgerDistributionAmount == glRecurringDistributions[index + 3].generalLedgerDistributionAmount
            }
         }
      }

      when:
      get("$path$pageTwo")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create one" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      def glRecurringDistributionDTO = dataLoaderService.singleDTO(
         new GeneralLedgerRecurringDTO(glRecurring),
         new AccountDTO(account),
         new StoreDTO(profitCenter)
      )
      glRecurringDistributionDTO.generalLedgerDistributionAmount = 99999999999.99

      when:
      def result = post(path, glRecurringDistributionDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id != null
         generalLedgerRecurring.id == glRecurringDistributionDTO.generalLedgerRecurring.id
         generalLedgerDistributionAccount.id == glRecurringDistributionDTO.generalLedgerDistributionAccount.myId()
         generalLedgerDistributionProfitCenter.id == glRecurringDistributionDTO.generalLedgerDistributionProfitCenter.myId()
         generalLedgerDistributionAmount == glRecurringDistributionDTO.generalLedgerDistributionAmount
      }
   }

   @Unroll
   void "create invalid GL recurring distribution without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glRecurringDistributionDTO = dataLoaderService.singleDTO(
         new GeneralLedgerRecurringDTO(glRecurring),
         new AccountDTO(account),
         new StoreDTO(profitCenter)
      )
      glRecurringDistributionDTO["$nonNullableProp"] = null

      when:
      post(path, glRecurringDistributionDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp                           || errorResponsePath
      'generalLedgerRecurring'                  || 'generalLedgerRecurring'
      'generalLedgerDistributionAccount'        || 'generalLedgerDistributionAccount'
      'generalLedgerDistributionProfitCenter'   || 'generalLedgerDistributionProfitCenter'
      'generalLedgerDistributionAmount'         || 'generalLedgerDistributionAmount'
   }

   void "create invalid GL recurring distribution with non-existing generalLedgerDistributionAccount" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final invalidAccount = new AccountDTO(accountDataLoaderService.single(company))
      invalidAccount.id = UUID.fromString('0fd98cc1-0870-4a98-958b-e62bf5c389e8')
      final profitCenter = storeFactoryService.store(3, company)
      final glRecurringDistributionDTO = dataLoaderService.singleDTO(
         new GeneralLedgerRecurringDTO(glRecurring),
         invalidAccount,
         new StoreDTO(profitCenter)
      )

      when:
      post(path, glRecurringDistributionDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'generalLedgerDistributionAccount.id'
      response[0].message == '0fd98cc1-0870-4a98-958b-e62bf5c389e8 was unable to be found'
   }

   void "update one" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glRecurringDistributionEntity = dataLoaderService.single(glRecurring, account, profitCenter)
      final glRecurringDistributionDTO = dataLoaderService.singleDTO(
         new GeneralLedgerRecurringDTO(glRecurring),
         new AccountDTO(account),
         new StoreDTO(profitCenter)
      )
      glRecurringDistributionDTO.id = glRecurringDistributionEntity.id

      when:
      def result = put("$path/${glRecurringDistributionEntity.id}", glRecurringDistributionDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == glRecurringDistributionDTO.id
         generalLedgerRecurring.id == glRecurringDistributionDTO.generalLedgerRecurring.id
         generalLedgerDistributionAccount.id == glRecurringDistributionDTO.generalLedgerDistributionAccount.myId()
         generalLedgerDistributionProfitCenter.id == glRecurringDistributionDTO.generalLedgerDistributionProfitCenter.myId()
         generalLedgerDistributionAmount == glRecurringDistributionDTO.generalLedgerDistributionAmount
      }
   }

   @Unroll
   void "update invalid GL recurring distribution without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glRecurringDistributionEntity = dataLoaderService.single(glRecurring, account, profitCenter)
      final glRecurringDistributionDTO = dataLoaderService.singleDTO(
         new GeneralLedgerRecurringDTO(glRecurring),
         new AccountDTO(account),
         new StoreDTO(profitCenter)
      )
      glRecurringDistributionDTO["$nonNullableProp"] = null

      when:
      put("$path/${glRecurringDistributionEntity.id}", glRecurringDistributionDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp                           || errorResponsePath
      'generalLedgerRecurring'                  || 'generalLedgerRecurring'
      'generalLedgerDistributionAccount'        || 'generalLedgerDistributionAccount'
      'generalLedgerDistributionProfitCenter'   || 'generalLedgerDistributionProfitCenter'
      'generalLedgerDistributionAmount'         || 'generalLedgerDistributionAmount'
   }

   void "update invalid GL recurring distribution with non-existing generalLedgerDistributionAccount" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glRecurringDistributionEntity = dataLoaderService.single(glRecurring, account, profitCenter)
      final invalidAccount =  new AccountDTO(accountDataLoaderService.single(company))
      invalidAccount.id = UUID.fromString('0fd98cc1-0870-4a98-958b-e62bf5c389e8')
      final glRecurringDistributionDTO = dataLoaderService.singleDTO(
         new GeneralLedgerRecurringDTO(glRecurring),
         invalidAccount,
         new StoreDTO(profitCenter)
      )

      when:
      put("$path/${glRecurringDistributionEntity.id}", glRecurringDistributionDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'generalLedgerDistributionAccount.id'
      response[0].message == '0fd98cc1-0870-4a98-958b-e62bf5c389e8 was unable to be found'
   }

   void "delete all GL recurring distribution by GL Recurring ID" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glRecurringDistribution = dataLoaderService.single(glRecurring, account, profitCenter)

      when:
      delete("$path/recurring-id/${glRecurring.id}")

      then:
      notThrown(Exception)

      when:
      get("$path/${glRecurringDistribution.id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${glRecurringDistribution.id} was unable to be found"
      response.code == 'system.not.found'
   }


   void "delete one GL recurring distribution" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glRecurringDistribution = dataLoaderService.single(glRecurring, account, profitCenter)

      when:
      delete("$path/${glRecurringDistribution.id}")

      then:
      notThrown(Exception)

      when:
      get("$path/${glRecurringDistribution.id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${glRecurringDistribution.id} was unable to be found"
      response.code == 'system.not.found'
   }


   void "recreate deleted GL recurring distribution" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glRecurringDistributionDTO = dataLoaderService.singleDTO(
         new GeneralLedgerRecurringDTO(glRecurring),
         new AccountDTO(account),
         new StoreDTO(profitCenter)
      )

      when: // create a GL recurring distribution
      def response1 = post(path, glRecurringDistributionDTO)

      then:
      notThrown(Exception)
      response1 != null
      with(response1) {
         id != null
         generalLedgerRecurring.id == glRecurringDistributionDTO.generalLedgerRecurring.id
         generalLedgerDistributionAccount.id == glRecurringDistributionDTO.generalLedgerDistributionAccount.myId()
         generalLedgerDistributionProfitCenter.id == glRecurringDistributionDTO.generalLedgerDistributionProfitCenter.myId()
         generalLedgerDistributionAmount == glRecurringDistributionDTO.generalLedgerDistributionAmount
      }

      when: // delete GL recurring distribution
      delete("$path/$response1.id")

      then: "GL recurring distribution of user's company is deleted"
      notThrown(HttpClientResponseException)

      when: // recreate GL recurring distribution
      def response2 = post(path, glRecurringDistributionDTO)

      then:
      notThrown(Exception)
      response2 != null
      with(response2) {
         id != null
         generalLedgerRecurring.id == glRecurringDistributionDTO.generalLedgerRecurring.id
         generalLedgerDistributionAccount.id == glRecurringDistributionDTO.generalLedgerDistributionAccount.myId()
         generalLedgerDistributionProfitCenter.id == glRecurringDistributionDTO.generalLedgerDistributionProfitCenter.myId()
         generalLedgerDistributionAmount == glRecurringDistributionDTO.generalLedgerDistributionAmount
      }

      when: // delete GL recurring distribution again
      delete("$path/$response2.id")

      then: "GL recurring distribution of user's company is deleted"
      notThrown(HttpClientResponseException)
   }

   void "fetch totals" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring1 = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final glRecurring2 = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final List<GeneralLedgerRecurringDistributionEntity> glRecurringDistributions  = dataLoaderService.stream(3, glRecurring1, account, profitCenter).toList()
      final recurring1CreditSum = glRecurringDistributions.findAll {it.generalLedgerDistributionAmount < BigDecimal.ZERO }.sum {-it.generalLedgerDistributionAmount }
      final recurring1DebitSum = glRecurringDistributions.findAll {it.generalLedgerDistributionAmount >= BigDecimal.ZERO }.sum {it.generalLedgerDistributionAmount }
      final recurring1Total = glRecurringDistributions.sum {it.generalLedgerDistributionAmount }
      glRecurringDistributions.add(dataLoaderService.single(glRecurring2, account, profitCenter))
      glRecurringDistributions.add(dataLoaderService.single(glRecurring2, account, profitCenter))

      when:
      def result = get("$path/calculate-total/${glRecurring1.id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         result.credit == recurring1CreditSum
         result.debit == recurring1DebitSum
         result.total == recurring1Total
      }
   }
}