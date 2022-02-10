package com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalDTO
import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.GeneralLedgerReversalDistributionDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class GeneralLedgerReversalDistributionControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/accounting/general-ledger/reversal/distribution"

   @Inject AccountTestDataLoaderService accountDataLoaderService
   @Inject GeneralLedgerReversalDataLoaderService generalLedgerReversalDataLoaderService
   @Inject GeneralLedgerReversalDistributionDataLoaderService dataLoaderService
   @Inject GeneralLedgerSourceCodeDataLoaderService generalLedgerSourceCodeDataLoaderService

   void "fetch one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glReversal = generalLedgerReversalDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glReversalDistribution = dataLoaderService.single(glReversal, account, profitCenter)

      when:
      def result = get("$path/${glReversalDistribution.id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == glReversalDistribution.id
         generalLedgerReversal.id == glReversalDistribution.generalLedgerReversal.id
         generalLedgerReversalDistributionAccount.id == glReversalDistribution.generalLedgerReversalDistributionAccount.myId()
         generalLedgerReversalDistributionProfitCenter.id == glReversalDistribution.generalLedgerReversalDistributionProfitCenter.myId()
         generalLedgerReversalDistributionAmount == glReversalDistribution.generalLedgerReversalDistributionAmount
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
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glReversal = generalLedgerReversalDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glReversalDistributions = dataLoaderService.stream(3, glReversal, account, profitCenter).toList()
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
               id == glReversalDistributions[index].id
               generalLedgerReversal.id == glReversalDistributions[index].generalLedgerReversal.id
               generalLedgerReversalDistributionAccount.id == glReversalDistributions[index].generalLedgerReversalDistributionAccount.myId()
               generalLedgerReversalDistributionProfitCenter.id == glReversalDistributions[index].generalLedgerReversalDistributionProfitCenter.myId()
               generalLedgerReversalDistributionAmount == glReversalDistributions[index].generalLedgerReversalDistributionAmount
            }
         }
      }

      when:
      get("$path$pageTwo")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "fetch all by gl reversal id" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glReversal1 = generalLedgerReversalDataLoaderService.single(company, glSourceCode)
      final glReversal2 = generalLedgerReversalDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glReversalDistributions = dataLoaderService.stream(3, glReversal1, account, profitCenter).toList()
      glReversalDistributions.add(dataLoaderService.single(glReversal2, account, profitCenter))
      glReversalDistributions.add(dataLoaderService.single(glReversal2, account, profitCenter))
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")

      when: // fetch records with first gl reversal id
      def result = get("$path/reversal-id/${glReversal1.id}$pageOne")

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
               id == glReversalDistributions[index].id
               generalLedgerReversal.id == glReversalDistributions[index].generalLedgerReversal.id
               generalLedgerReversalDistributionAccount.id == glReversalDistributions[index].generalLedgerReversalDistributionAccount.myId()
               generalLedgerReversalDistributionProfitCenter.id == glReversalDistributions[index].generalLedgerReversalDistributionProfitCenter.myId()
               generalLedgerReversalDistributionAmount == glReversalDistributions[index].generalLedgerReversalDistributionAmount
            }
         }
      }

      when: // fetch records with second gl reversal id
      result = get("$path/reversal-id/${glReversal2.id}$pageOne")

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
               id == glReversalDistributions[index + 3].id
               generalLedgerReversal.id == glReversalDistributions[index + 3].generalLedgerReversal.id
               generalLedgerReversalDistributionAccount.id == glReversalDistributions[index + 3].generalLedgerReversalDistributionAccount.myId()
               generalLedgerReversalDistributionProfitCenter.id == glReversalDistributions[index + 3].generalLedgerReversalDistributionProfitCenter.myId()
               generalLedgerReversalDistributionAmount == glReversalDistributions[index + 3].generalLedgerReversalDistributionAmount
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
      final glReversal = generalLedgerReversalDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glReversalDistributionDTO = dataLoaderService.singleDTO(
         new GeneralLedgerReversalDTO(glReversal),
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      )

      when:
      def result = post(path, glReversalDistributionDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id != null
         generalLedgerReversal.id == glReversalDistributionDTO.generalLedgerReversal.id
         generalLedgerReversalDistributionAccount.id == glReversalDistributionDTO.generalLedgerReversalDistributionAccount.myId()
         generalLedgerReversalDistributionProfitCenter.id == glReversalDistributionDTO.generalLedgerReversalDistributionProfitCenter.myId()
         generalLedgerReversalDistributionAmount == glReversalDistributionDTO.generalLedgerReversalDistributionAmount
      }
   }

   @Unroll
   void "create invalid GL reversal distribution without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glReversal = generalLedgerReversalDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glReversalDistributionDTO = dataLoaderService.singleDTO(
         new GeneralLedgerReversalDTO(glReversal),
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      )
      glReversalDistributionDTO["$nonNullableProp"] = null

      when:
      post(path, glReversalDistributionDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp                                   || errorResponsePath
      'generalLedgerReversal'                           || 'generalLedgerReversal'
      'generalLedgerReversalDistributionAccount'        || 'generalLedgerReversalDistributionAccount'
      'generalLedgerReversalDistributionProfitCenter'   || 'generalLedgerReversalDistributionProfitCenter'
      'generalLedgerReversalDistributionAmount'         || 'generalLedgerReversalDistributionAmount'
   }

   @Unroll
   void "create invalid GL reversal distribution with non-existing #testProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glReversal = generalLedgerReversalDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glReversalDistributionDTO = dataLoaderService.singleDTO(
         new GeneralLedgerReversalDTO(glReversal),
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      )

      when:
      glReversalDistributionDTO["$testProp"] = invalidValue
      post(path, glReversalDistributionDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage
      where:
      testProp                                        | invalidValue                                                                       || errorResponsePath                                  | errorMessage
      'generalLedgerReversalDistributionAccount'      | new SimpleIdentifiableDTO(UUID.fromString('0fd98cc1-0870-4a98-958b-e62bf5c389e8')) || 'generalLedgerReversalDistributionAccount.id'      | "0fd98cc1-0870-4a98-958b-e62bf5c389e8 was unable to be found"
      'generalLedgerReversalDistributionProfitCenter' | new SimpleLegacyIdentifiableDTO(999_999)                                           || 'generalLedgerReversalDistributionProfitCenter.id' | '999999 was unable to be found'
   }

   void "update one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glReversal = generalLedgerReversalDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glReversalDistributionEntity = dataLoaderService.single(glReversal, account, profitCenter)
      final glReversalDistributionDTO = dataLoaderService.singleDTO(
         new GeneralLedgerReversalDTO(glReversal),
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      )
      glReversalDistributionDTO.id = glReversalDistributionEntity.id

      when:
      def result = put("$path/${glReversalDistributionEntity.id}", glReversalDistributionDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == glReversalDistributionDTO.id
         generalLedgerReversal.id == glReversalDistributionDTO.generalLedgerReversal.id
         generalLedgerReversalDistributionAccount.id == glReversalDistributionDTO.generalLedgerReversalDistributionAccount.myId()
         generalLedgerReversalDistributionProfitCenter.id == glReversalDistributionDTO.generalLedgerReversalDistributionProfitCenter.myId()
         generalLedgerReversalDistributionAmount == glReversalDistributionDTO.generalLedgerReversalDistributionAmount
      }
   }

   @Unroll
   void "update invalid GL reversal distribution without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glReversal = generalLedgerReversalDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glReversalDistributionEntity = dataLoaderService.single(glReversal, account, profitCenter)
      final glReversalDistributionDTO = dataLoaderService.singleDTO(
         new GeneralLedgerReversalDTO(glReversal),
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      )
      glReversalDistributionDTO.id = glReversalDistributionEntity.id
      glReversalDistributionDTO["$nonNullableProp"] = null

      when:
      put("$path/${glReversalDistributionEntity.id}", glReversalDistributionDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp                                   || errorResponsePath
      'generalLedgerReversal'                           || 'generalLedgerReversal'
      'generalLedgerReversalDistributionAccount'        || 'generalLedgerReversalDistributionAccount'
      'generalLedgerReversalDistributionProfitCenter'   || 'generalLedgerReversalDistributionProfitCenter'
      'generalLedgerReversalDistributionAmount'         || 'generalLedgerReversalDistributionAmount'
   }

   @Unroll
   void "update invalid GL reversal distribution with non-existing #testProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glReversal = generalLedgerReversalDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glReversalDistributionEntity = dataLoaderService.single(glReversal, account, profitCenter)
      final glReversalDistributionDTO = dataLoaderService.singleDTO(
         new GeneralLedgerReversalDTO(glReversal),
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      )
      glReversalDistributionDTO.id = glReversalDistributionEntity.id

      when:
      glReversalDistributionDTO["$testProp"] = invalidValue
      put("$path/${glReversalDistributionEntity.id}", glReversalDistributionDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage
      where:
      testProp                                        | invalidValue                                                                       || errorResponsePath                                  | errorMessage
      'generalLedgerReversalDistributionAccount'      | new SimpleIdentifiableDTO(UUID.fromString('0fd98cc1-0870-4a98-958b-e62bf5c389e8')) || 'generalLedgerReversalDistributionAccount.id'      | "0fd98cc1-0870-4a98-958b-e62bf5c389e8 was unable to be found"
      'generalLedgerReversalDistributionProfitCenter' | new SimpleLegacyIdentifiableDTO(999_999)                                           || 'generalLedgerReversalDistributionProfitCenter.id' | '999999 was unable to be found'
   }

   void "delete one GL reversal distribution" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glReversal = generalLedgerReversalDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glReversalDistribution = dataLoaderService.single(glReversal, account, profitCenter)

      when:
      delete("$path/${glReversalDistribution.id}")

      then:
      notThrown(Exception)

      when:
      get("$path/${glReversalDistribution.id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${glReversalDistribution.id} was unable to be found"
      response.code == 'system.not.found'
   }

   void "recreate deleted GL reversal distribution" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glReversal = generalLedgerReversalDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glReversalDistributionDTO = dataLoaderService.singleDTO(
         new GeneralLedgerReversalDTO(glReversal),
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      )

      when: // create a GL reversal distribution
      def response1 = post(path, glReversalDistributionDTO)

      then:
      notThrown(Exception)
      response1 != null
      with(response1) {
         id != null
         generalLedgerReversal.id == glReversalDistributionDTO.generalLedgerReversal.id
         generalLedgerReversalDistributionAccount.id == glReversalDistributionDTO.generalLedgerReversalDistributionAccount.myId()
         generalLedgerReversalDistributionProfitCenter.id == glReversalDistributionDTO.generalLedgerReversalDistributionProfitCenter.myId()
         generalLedgerReversalDistributionAmount == glReversalDistributionDTO.generalLedgerReversalDistributionAmount
      }

      when: // delete GL reversal distribution
      delete("$path/$response1.id")

      then: "GL reversal distribution of user's company is deleted"
      notThrown(HttpClientResponseException)

      when: // recreate GL reversal distribution
      def response2 = post(path, glReversalDistributionDTO)

      then:
      notThrown(Exception)
      response2 != null
      with(response2) {
         id != null
         generalLedgerReversal.id == glReversalDistributionDTO.generalLedgerReversal.id
         generalLedgerReversalDistributionAccount.id == glReversalDistributionDTO.generalLedgerReversalDistributionAccount.myId()
         generalLedgerReversalDistributionProfitCenter.id == glReversalDistributionDTO.generalLedgerReversalDistributionProfitCenter.myId()
         generalLedgerReversalDistributionAmount == glReversalDistributionDTO.generalLedgerReversalDistributionAmount
      }

      when: // delete GL reversal distribution again
      delete("$path/$response2.id")

      then: "GL reversal distribution of user's company is deleted"
      notThrown(HttpClientResponseException)
   }
}
