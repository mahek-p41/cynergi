package com.cynergisuite.middleware.accounting.general.ledger.reversal.entry.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.GeneralLedgerReversalDistributionDataLoader
import com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.GeneralLedgerReversalDistributionDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.reversal.entry.GeneralLedgerReversalEntryDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import jakarta.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class GeneralLedgerReversalEntryControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/accounting/general-ledger/reversal/entry"

   @Inject AccountTestDataLoaderService accountDataLoaderService
   @Inject GeneralLedgerReversalDataLoaderService generalLedgerReversalDataLoaderService
   @Inject GeneralLedgerReversalDistributionDataLoaderService generalLedgerReversalDistributionDataLoaderService
   @Inject GeneralLedgerReversalEntryDataLoaderService dataLoaderService
   @Inject GeneralLedgerSourceCodeDataLoaderService generalLedgerSourceCodeDataLoaderService

   void "fetch one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glReversal = generalLedgerReversalDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glReversalDistributions = generalLedgerReversalDistributionDataLoaderService.stream(1, glReversal, account, profitCenter).toList()

      when:
      def result = get("$path/${glReversal.id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         with(generalLedgerReversal) {
            id == glReversal.id

            with(source) {
               value == glReversal.source.value
               description == glReversal.source.description
            }

            date == glReversal.date.toString()
            reversalDate == glReversal.reversalDate.toString()
            comment == glReversal.comment
            entryMonth == glReversal.entryMonth
            entryNumber == glReversal.entryNumber
         }

         generalLedgerReversalDistributions.eachWithIndex{ distribution, index ->
            distribution.id == glReversalDistributions[index].id
            distribution.generalLedgerReversal.id == glReversalDistributions[index].generalLedgerReversal.id
            distribution.generalLedgerReversalDistributionAccount.id == glReversalDistributions[index].generalLedgerReversalDistributionAccount.myId()
            distribution.generalLedgerReversalDistributionProfitCenter.id == glReversalDistributions[index].generalLedgerReversalDistributionProfitCenter.myId()
            distribution.generalLedgerReversalDistributionAmount == glReversalDistributions[index].generalLedgerReversalDistributionAmount
         }

         balance == BigDecimal.ZERO
      }
   }

   void "fetch one not found" () {
      given:
      final nonExistentId = UUID.randomUUID()

      when: // non-existent gl reversal id
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
      final glReversals = generalLedgerReversalDataLoaderService.stream(3, company, glSourceCode).toList()
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)

      def glReversalDistributions = []
      glReversals.each { it ->
         glReversalDistributions = generalLedgerReversalDistributionDataLoaderService.stream(2, it, account, profitCenter).toList()
      }

      def pageOne = new StandardPageRequest(1, 5, "id", "ASC")

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
               with(generalLedgerReversal) {
                  id == glReversals[index].id
                  source.id == glReversals[index].source.id
                  date == glReversals[index].date.toString()
                  reversalDate == glReversals[index].reversalDate.toString()
                  comment == glReversals[index].comment
                  entryMonth == glReversals[index].entryMonth
                  entryNumber == glReversals[index].entryNumber
               }

               generalLedgerReversalDistributions.eachWithIndex{ distribution, int i ->
                  glReversalDistributions.find{ element -> element == distribution }
               }

               balance == BigDecimal.ZERO
            }
         }
      }
   }

   void "update one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glReversalEntity = generalLedgerReversalDataLoaderService.single(company, glSourceCode)
      final glReversalDTO = generalLedgerReversalDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glSourceCode))
      glReversalDTO.id = glReversalEntity.id

      final glReversalDistributions = generalLedgerReversalDistributionDataLoaderService.stream(1, glReversalEntity, account, profitCenter).toList()
      def glReversalDistributionDTOs = GeneralLedgerReversalDistributionDataLoader.streamDTO(
         1,
         glReversalDTO,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      ).toList()
      glReversalDistributionDTOs.eachWithIndex { it, index ->
         it.id = glReversalDistributions[index].id
      }

      def glReversalEntryDTO = dataLoaderService.singleDTO(glReversalDTO, glReversalDistributionDTOs)

      when:
      def result = put("$path/${glReversalEntity.id}", glReversalEntryDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         with(generalLedgerReversal) {
            id == glReversalDTO.id

            with(source) {
               value == glReversalEntryDTO.generalLedgerReversal.source.value
               description == glReversalEntryDTO.generalLedgerReversal.source.description
            }

            date == glReversalEntryDTO.generalLedgerReversal.date.toString()
            reversalDate == glReversalEntryDTO.generalLedgerReversal.reversalDate.toString()
            comment == glReversalEntryDTO.generalLedgerReversal.comment
            entryMonth == glReversalEntryDTO.generalLedgerReversal.entryMonth
            entryNumber == glReversalEntryDTO.generalLedgerReversal.entryNumber
         }

         generalLedgerReversalDistributions.eachWithIndex{ distribution, index ->
            distribution.id == glReversalEntryDTO.generalLedgerReversalDistributions[index].id
            distribution.generalLedgerReversal.id == glReversalEntryDTO.generalLedgerReversalDistributions[index].generalLedgerReversal.id
            distribution.generalLedgerReversalDistributionAccount.id == glReversalEntryDTO.generalLedgerReversalDistributions[index].generalLedgerReversalDistributionAccount.myId()
            distribution.generalLedgerReversalDistributionProfitCenter.id == glReversalEntryDTO.generalLedgerReversalDistributions[index].generalLedgerReversalDistributionProfitCenter.myId()
            distribution.generalLedgerReversalDistributionAmount == glReversalEntryDTO.generalLedgerReversalDistributions[index].generalLedgerReversalDistributionAmount
         }

         balance == BigDecimal.ZERO
      }
   }

   @Unroll
   void "update invalid GL reversal entry without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glReversalEntity = generalLedgerReversalDataLoaderService.single(company, glSourceCode)
      final glReversalDTO = generalLedgerReversalDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glSourceCode))
      glReversalDTO.id = glReversalEntity.id

      final glReversalDistributions = generalLedgerReversalDistributionDataLoaderService.stream(1, glReversalEntity, account, profitCenter).toList()
      def glReversalDistributionDTOs = GeneralLedgerReversalDistributionDataLoader.streamDTO(
         1,
         glReversalDTO,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      ).toList()
      glReversalDistributionDTOs.eachWithIndex { it, index ->
         it.id = glReversalDistributions[index].id
      }

      def glReversalEntryDTO = dataLoaderService.singleDTO(glReversalDTO, glReversalDistributionDTOs)
      glReversalEntryDTO["$nonNullableProp"] = null

      when:
      put("$path/${glReversalEntity.id}", glReversalEntryDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp                       || errorResponsePath
      'balance'                             || 'balance'
      'generalLedgerReversal'               || 'generalLedgerReversal'
   }

   @Unroll
   void "update invalid GL reversal entry with non-existing GL reversal source id" () {
      given:
      final nonExistentId = UUID.randomUUID()
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glReversalEntity = generalLedgerReversalDataLoaderService.single(company, glSourceCode)
      final glReversalDTO = generalLedgerReversalDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glSourceCode))
      glReversalDTO.id = glReversalEntity.id

      final glReversalDistributions = generalLedgerReversalDistributionDataLoaderService.stream(1, glReversalEntity, account, profitCenter).toList()
      def glReversalDistributionDTOs = GeneralLedgerReversalDistributionDataLoader.streamDTO(
         1,
         glReversalDTO,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      ).toList()
      glReversalDistributionDTOs.eachWithIndex { it, index ->
         it.id = glReversalDistributions[index].id
      }

      def glReversalEntryDTO = dataLoaderService.singleDTO(glReversalDTO, glReversalDistributionDTOs)
      glReversalEntryDTO.generalLedgerReversal.source.id = nonExistentId

      when:
      put("$path/${glReversalEntity.id}", glReversalEntryDTO)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "generalLedgerReversal.source.id"
      response[0].message == "$nonExistentId was unable to be found"
   }

   @Unroll
   void "update invalid GL reversal entry with non-existing GL reversal distribution #testProp" () {
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glReversalEntity = generalLedgerReversalDataLoaderService.single(company, glSourceCode)
      final glReversalDTO = generalLedgerReversalDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glSourceCode))
      glReversalDTO.id = glReversalEntity.id

      final glReversalDistributions = generalLedgerReversalDistributionDataLoaderService.stream(1, glReversalEntity, account, profitCenter).toList()
      def glReversalDistributionDTOs = GeneralLedgerReversalDistributionDataLoader.streamDTO(
         1,
         glReversalDTO,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      ).toList()
      glReversalDistributionDTOs.eachWithIndex { it, index ->
         it.id = glReversalDistributions[index].id
      }

      def glReversalEntryDTO = dataLoaderService.singleDTO(glReversalDTO, glReversalDistributionDTOs)
      glReversalEntryDTO.generalLedgerReversalDistributions.forEach {
         it."$testProp" = invalidValue
      }

      when:
      put("$path/${glReversalEntity.id}", glReversalEntryDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage

      where:
      testProp                                        | invalidValue                                                                       || errorResponsePath | errorMessage
      'generalLedgerReversalDistributionAccount'      | new SimpleIdentifiableDTO(UUID.fromString('0fd98cc1-0870-4a98-958b-e62bf5c389e8')) || 'generalLedgerReversalDistributions[index].generalLedgerReversalDistributionAccount.id' | "0fd98cc1-0870-4a98-958b-e62bf5c389e8 was unable to be found"
      'generalLedgerReversalDistributionProfitCenter' | new SimpleLegacyIdentifiableDTO(999_999)                                           || 'generalLedgerReversalDistributions[index].generalLedgerReversalDistributionProfitCenter.id' | '999999 was unable to be found'
   }

   void "update invalid GL reversal entry with non-zero balance" () {
      given:
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glReversalEntity = generalLedgerReversalDataLoaderService.single(company, glSourceCode)
      final glReversalDTO = generalLedgerReversalDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glSourceCode))
      glReversalDTO.id = glReversalEntity.id

      final glReversalDistributions = generalLedgerReversalDistributionDataLoaderService.stream(1, glReversalEntity, account, profitCenter).toList()
      def glReversalDistributionDTOs = GeneralLedgerReversalDistributionDataLoader.streamDTO(
         1,
         glReversalDTO,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      ).toList()
      glReversalDistributionDTOs.eachWithIndex { it, index ->
         it.id = glReversalDistributions[index].id
      }

      def glReversalEntryDTO = dataLoaderService.singleDTO(glReversalDTO, glReversalDistributionDTOs)
      glReversalEntryDTO.balance = 1500

      when:
      put("$path/${glReversalEntity.id}", glReversalEntryDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'balance'
      response[0].message == 'Balance must total zero'
   }

   void "delete one GL reversal entry" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glReversal = generalLedgerReversalDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      generalLedgerReversalDistributionDataLoaderService.stream(1, glReversal, account, profitCenter).toList()

      when:
      get("$path/${glReversal.id}")
      delete("$path/${glReversal.id}")

      then:
      notThrown(Exception)

      when:
      get("$path/${glReversal.id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${glReversal.id} was unable to be found"
      response.code == 'system.not.found'
   }

   void "delete GL reversal entry from other company is not allowed" () {
      given:
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds2)
      final glReversal = generalLedgerReversalDataLoaderService.single(tstds2, glSourceCode)
      final account = accountDataLoaderService.single(tstds2)
      final profitCenter = storeFactoryService.store(3, tstds2)
      generalLedgerReversalDistributionDataLoaderService.stream(1, glReversal, account, profitCenter).toList()

      when:
      get("$path/${glReversal.id}")
      delete("$path/${glReversal.id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${glReversal.id} was unable to be found"
      response.code == 'system.not.found'
   }
}
