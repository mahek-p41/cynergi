package com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringDTO
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.GeneralLedgerRecurringDistributionDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class GeneralLedgerRecurringDistributionControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/accounting/general-ledger/recurring/distribution"

   @Inject AccountDataLoaderService accountDataLoaderService
   @Inject GeneralLedgerRecurringDataLoaderService generalLedgerRecurringDataLoaderService
   @Inject GeneralLedgerRecurringDistributionDataLoaderService dataLoaderService
   @Inject GeneralLedgerSourceCodeDataLoaderService generalLedgerSourceCodeDataLoaderService

   void "fetch one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
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
         generalLedgerRecurring.id == glRecurringDistribution.generalLedgerRecurring.id
         generalLedgerDistributionAccount.id == glRecurringDistribution.generalLedgerDistributionAccount.myId()
         generalLedgerDistributionProfitCenter.id == glRecurringDistribution.generalLedgerDistributionProfitCenter.myId()
         generalLedgerDistributionAmount == glRecurringDistribution.generalLedgerDistributionAmount
      }
   }

   void "fetch one not found" () {
      when:
      get("$path/0")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "0 was unable to be found"
   }

   void "fetch all" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
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
               generalLedgerRecurring.id == glRecurringDistributions[index].generalLedgerRecurring.id
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
      final company = companyFactoryService.forDatasetCode('tstds1')
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
               generalLedgerRecurring.id == glRecurringDistributions[index].generalLedgerRecurring.id
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
               generalLedgerRecurring.id == glRecurringDistributions[index + 3].generalLedgerRecurring.id
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
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glRecurringDistributionDTO = dataLoaderService.singleDTO(
         new GeneralLedgerRecurringDTO(glRecurring),
         new SimpleIdentifiableDTO(account),
         new SimpleIdentifiableDTO(profitCenter.myId())
      )

      when:
      def result = post(path, glRecurringDistributionDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id > 0
         generalLedgerRecurring.id == glRecurringDistributionDTO.generalLedgerRecurring.id
         generalLedgerDistributionAccount.id == glRecurringDistributionDTO.generalLedgerDistributionAccount.myId()
         generalLedgerDistributionProfitCenter.id == glRecurringDistributionDTO.generalLedgerDistributionProfitCenter.myId()
         generalLedgerDistributionAmount == glRecurringDistributionDTO.generalLedgerDistributionAmount
      }
   }

   @Unroll
   void "create invalid GL recurring distribution without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glRecurringDistributionDTO = dataLoaderService.singleDTO(
         new GeneralLedgerRecurringDTO(glRecurring),
         new SimpleIdentifiableDTO(account),
         new SimpleIdentifiableDTO(profitCenter.myId())
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

   @Unroll
   void "create invalid GL recurring distribution with non-existing #testProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glRecurringDistributionDTO = dataLoaderService.singleDTO(
         new GeneralLedgerRecurringDTO(glRecurring),
         new SimpleIdentifiableDTO(account),
         new SimpleIdentifiableDTO(profitCenter.myId())
      )
      glRecurringDistributionDTO["$testProp"] = invalidValue

      when:
      post(path, glRecurringDistributionDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage
      where:
      testProp                                   | invalidValue                         || errorResponsePath                             | errorMessage
      'generalLedgerDistributionAccount'         | new SimpleIdentifiableDTO(999999)    || 'generalLedgerDistributionAccount.id'         | '999,999 was unable to be found'
      'generalLedgerDistributionProfitCenter'    | new SimpleIdentifiableDTO(999999)    || 'generalLedgerDistributionProfitCenter.id'    | '999,999 was unable to be found'
   }

   void "update one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glRecurringDistributionEntity = dataLoaderService.single(glRecurring, account, profitCenter)
      final glRecurringDistributionDTO = dataLoaderService.singleDTO(
         new GeneralLedgerRecurringDTO(glRecurring),
         new SimpleIdentifiableDTO(account),
         new SimpleIdentifiableDTO(profitCenter.myId())
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
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glRecurringDistributionEntity = dataLoaderService.single(glRecurring, account, profitCenter)
      final glRecurringDistributionDTO = dataLoaderService.singleDTO(
         new GeneralLedgerRecurringDTO(glRecurring),
         new SimpleIdentifiableDTO(account),
         new SimpleIdentifiableDTO(profitCenter.myId())
      )
      glRecurringDistributionDTO.id = glRecurringDistributionEntity.id
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

   @Unroll
   void "update invalid GL recurring distribution with non-existing #testProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glRecurringDistributionEntity = dataLoaderService.single(glRecurring, account, profitCenter)
      final glRecurringDistributionDTO = dataLoaderService.singleDTO(
         new GeneralLedgerRecurringDTO(glRecurring),
         new SimpleIdentifiableDTO(account),
         new SimpleIdentifiableDTO(profitCenter.myId())
      )
      glRecurringDistributionDTO.id = glRecurringDistributionEntity.id
      glRecurringDistributionDTO["$testProp"] = invalidValue

      when:
      put("$path/${glRecurringDistributionEntity.id}", glRecurringDistributionDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage
      where:
      testProp                                   | invalidValue                         || errorResponsePath                             | errorMessage
      'generalLedgerDistributionAccount'         | new SimpleIdentifiableDTO(999999)    || 'generalLedgerDistributionAccount.id'         | '999,999 was unable to be found'
      'generalLedgerDistributionProfitCenter'    | new SimpleIdentifiableDTO(999999)    || 'generalLedgerDistributionProfitCenter.id'    | '999,999 was unable to be found'
   }

   void "delete one GL recurring distribution" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
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
      response.size() == 1
      response.message == "${glRecurringDistribution.id} was unable to be found"
   }
}
