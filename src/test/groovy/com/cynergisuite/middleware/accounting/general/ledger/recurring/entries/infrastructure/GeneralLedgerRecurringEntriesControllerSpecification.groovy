package com.cynergisuite.middleware.accounting.general.ledger.recurring.entries.infrastructure

import com.cynergisuite.domain.GeneralLedgerRecurringEntriesFilterRequest
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringTypeDTO
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringTypeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.GeneralLedgerRecurringDistributionDataLoader
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.GeneralLedgerRecurringDistributionDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.recurring.entries.GeneralLedgerRecurringEntriesDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import javax.inject.Inject
import java.time.LocalDate
import java.time.ZoneId

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class GeneralLedgerRecurringEntriesControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/accounting/general-ledger/recurring/entries"

   @Inject AccountTestDataLoaderService accountDataLoaderService
   @Inject GeneralLedgerRecurringDataLoaderService generalLedgerRecurringDataLoaderService
   @Inject GeneralLedgerRecurringDistributionDataLoaderService generalLedgerRecurringDistributionDataLoaderService
   @Inject GeneralLedgerRecurringEntriesDataLoaderService dataLoaderService
   @Inject GeneralLedgerRecurringTypeDataLoaderService generalLedgerRecurringTypeDataLoaderService
   @Inject GeneralLedgerSourceCodeDataLoaderService generalLedgerSourceCodeDataLoaderService

   void "fetch one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glRecurringDistributions = generalLedgerRecurringDistributionDataLoaderService.stream(1, glRecurring, account, profitCenter).toList()

      when:
      def result = get("$path/${glRecurring.id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         with(generalLedgerRecurring) {
            id == glRecurring.id

            with(type) {
               value == glRecurring.type.value
               description == glRecurring.type.description
            }

            with(source) {
               value == glRecurring.source.value
               description == glRecurring.source.description
            }

            reverseIndicator == glRecurring.reverseIndicator
            message == glRecurring.message
            beginDate == glRecurring.beginDate.toString()
            endDate == glRecurring.endDate.toString()
         }

         generalLedgerRecurringDistributions.eachWithIndex{ distribution, index ->
            distribution.id == glRecurringDistributions[index].id
            distribution.generalLedgerDistributionAccount.id == glRecurringDistributions[index].generalLedgerDistributionAccount.id
            distribution.generalLedgerDistributionProfitCenter.id == glRecurringDistributions[index].generalLedgerDistributionProfitCenter.myId()
            distribution.generalLedgerDistributionAmount == glRecurringDistributions[index].generalLedgerDistributionAmount
         }

         balance == BigDecimal.ZERO
      }
   }

   void "fetch one not found" () {
      given:
      final nonExistentId = UUID.randomUUID()

      when: // non-existent gl recurring id
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
      final glRecurrings = generalLedgerRecurringDataLoaderService.stream(3, company, glSourceCode).toList()
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)

      def glRecurringDistributions = []
      glRecurrings.each { it ->
         glRecurringDistributions = generalLedgerRecurringDistributionDataLoaderService.stream(2, it, account, profitCenter).toList()
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
               with(generalLedgerRecurring) {
                  id == glRecurrings[index].id

                  with(type) {
                     value == glRecurrings[index].type.value
                     description == glRecurrings[index].type.description
                  }

                  with(source) {
                     value == glRecurrings[index].source.value
                     description == glRecurrings[index].source.description
                  }

                  reverseIndicator == glRecurrings[index].reverseIndicator
                  message == glRecurrings[index].message
                  beginDate == glRecurrings[index].beginDate.toString()
                  endDate == glRecurrings[index].endDate.toString()
               }

               generalLedgerRecurringDistributions.eachWithIndex{ distribution, int i ->
                  glRecurringDistributions.find{ element -> element == distribution }
               }

               balance == BigDecimal.ZERO
            }
         }
      }
   }

   void "filter for report #criteria" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')

      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      def glRecurringDTO1 = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      def glRecurringDTO2 = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      glRecurringDTO1.endDate = LocalDate.now()
      glRecurringDTO2.endDate = LocalDate.now()
      while (glRecurringDTO1.type == glRecurringDTO2.type) {
         glRecurringDTO1.type = new GeneralLedgerRecurringTypeDTO(generalLedgerRecurringTypeDataLoaderService.random())
      }

      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      def glRecurringDistributionDTOs1 = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         2,
         glRecurringDTO1,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      ).toList()
      def glRecurringDistributionDTOs2 = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         2,
         glRecurringDTO2,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      ).toList()

      def glRecurringEntriesDTO1 = dataLoaderService.singleDTO(glRecurringDTO1, glRecurringDistributionDTOs1)
      def glRecurringEntriesDTO2 = dataLoaderService.singleDTO(glRecurringDTO2, glRecurringDistributionDTOs2)

      def filterRequest = new GeneralLedgerRecurringEntriesFilterRequest([sortBy: "id", sortDirection: "ASC"])
      switch (criteria) {
         case 'Entry1':
            filterRequest['entryType'] = glRecurringDTO1.type.value
            filterRequest['sourceCode'] = glRecurringDTO1.source.value
            break
         case 'Entry2':
            filterRequest['entryType'] = glRecurringDTO2.type.value
            filterRequest['sourceCode'] = glRecurringDTO2.source.value
            break
         case 'AllEntries':
            filterRequest['sourceCode'] = glRecurringDTO1.source.value
            break
      }

      when:
      post(path, glRecurringEntriesDTO1)
      post(path, glRecurringEntriesDTO2)
      def response = get("$path/report-$filterRequest")

      then:
      notThrown(Exception)
      response != null
      response.size() == entriesCount
      where:
      criteria       || entriesCount
      'Entry1'       || 1
      'Entry2'       || 1
      'AllEntries'   || 2
   }

   void "create one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      ).toList()
      def glRecurringEntriesDTO = dataLoaderService.singleDTO(glRecurringDTO, glRecurringDistributionDTOs)

      when:
      def result = post(path, glRecurringEntriesDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         with(generalLedgerRecurring) {
            id != null

            with(type) {
               value == glRecurringEntriesDTO.generalLedgerRecurring.type.value
               description == glRecurringEntriesDTO.generalLedgerRecurring.type.description
            }

            with(source) {
               value == glRecurringEntriesDTO.generalLedgerRecurring.source.value
               description == glRecurringEntriesDTO.generalLedgerRecurring.source.description
            }

            reverseIndicator == glRecurringEntriesDTO.generalLedgerRecurring.reverseIndicator
            message == glRecurringEntriesDTO.generalLedgerRecurring.message
            beginDate == glRecurringEntriesDTO.generalLedgerRecurring.beginDate.toString()
            endDate == glRecurringEntriesDTO.generalLedgerRecurring.endDate.toString()
         }

         generalLedgerRecurringDistributions.eachWithIndex{ distribution, index ->
            distribution.id != null
            distribution.generalLedgerRecurring.id == glRecurringEntriesDTO.generalLedgerRecurringDistributions[index].generalLedgerRecurring.id
            distribution.generalLedgerDistributionAccount.id == glRecurringEntriesDTO.generalLedgerRecurringDistributions[index].generalLedgerDistributionAccount.id
            distribution.generalLedgerDistributionProfitCenter.id == glRecurringEntriesDTO.generalLedgerRecurringDistributions[index].generalLedgerDistributionProfitCenter.myId()
            distribution.generalLedgerDistributionAmount == glRecurringEntriesDTO.generalLedgerRecurringDistributions[index].generalLedgerDistributionAmount
         }

         balance == BigDecimal.ZERO
      }
   }

   @Unroll
   void "create invalid GL recurring entry without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      ).toList()
      def glRecurringEntriesDTO = dataLoaderService.singleDTO(glRecurringDTO, glRecurringDistributionDTOs)
      glRecurringEntriesDTO["$nonNullableProp"] = null

      when:
      post(path, glRecurringEntriesDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp                        || errorResponsePath
      'balance'                              || 'balance'
      'generalLedgerRecurring'               || 'generalLedgerRecurring'
   }

   @Unroll
   void "create invalid GL recurring entry with non-existing GL recurring #testProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      ).toList()
      def glRecurringEntriesDTO = dataLoaderService.singleDTO(glRecurringDTO, glRecurringDistributionDTOs)
      glRecurringEntriesDTO.generalLedgerRecurring."$testProp" = invalidValue

      when:
      post(path, glRecurringEntriesDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage

      where:
      testProp | invalidValue                                                                                                || errorResponsePath                   | errorMessage
      'source' | new GeneralLedgerSourceCodeDTO(UUID.fromString('ee2359b6-c88c-11eb-8098-02420a4d0702'), 'Z', 'Invalid DTO') || 'generalLedgerRecurring.source.id'  | 'ee2359b6-c88c-11eb-8098-02420a4d0702 was unable to be found'
      'type'   | new GeneralLedgerRecurringTypeDTO('Z', 'Invalid DTO')                                                       || 'generalLedgerRecurring.type.value' | 'Z was unable to be found'
   }

   @Unroll
   void "create invalid GL recurring entry with non-existing GL recurring distribution #testProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      ).toList()
      def glRecurringEntriesDTO = dataLoaderService.singleDTO(glRecurringDTO, glRecurringDistributionDTOs)
      glRecurringEntriesDTO.generalLedgerRecurringDistributions.forEach {
         it."$testProp" = invalidValue
      }

      when:
      post(path, glRecurringEntriesDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage

      where:
      testProp                                | invalidValue                                                                       || errorResponsePath | errorMessage
      'generalLedgerDistributionAccount'      | new SimpleIdentifiableDTO(UUID.fromString('0fd98cc1-0870-4a98-958b-e62bf5c389e8')) || 'generalLedgerRecurringDistributions[index].generalLedgerDistributionAccount.id'      | "0fd98cc1-0870-4a98-958b-e62bf5c389e8 was unable to be found"
      'generalLedgerDistributionProfitCenter' | new SimpleLegacyIdentifiableDTO(999_999)                                           || 'generalLedgerRecurringDistributions[index].generalLedgerDistributionProfitCenter.id' | '999999 was unable to be found'
   }

   void "create invalid GL recurring entry with begin date after end date" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      ).toList()
      def glRecurringEntriesDTO = dataLoaderService.singleDTO(glRecurringDTO, glRecurringDistributionDTOs)
      glRecurringEntriesDTO.generalLedgerRecurring.beginDate = LocalDate.now()
      glRecurringEntriesDTO.generalLedgerRecurring.endDate = LocalDate.now().minusDays(2)

      when:
      post(path, glRecurringEntriesDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'generalLedgerRecurring.beginDate'
      response[0].message == "\"End date of ${glRecurringEntriesDTO.generalLedgerRecurring.endDate} was before start date of ${glRecurringEntriesDTO.generalLedgerRecurring.beginDate}\""
   }

   void "create invalid GL recurring entry with non-zero balance" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      ).toList()
      def glRecurringEntriesDTO = dataLoaderService.singleDTO(glRecurringDTO, glRecurringDistributionDTOs)
      glRecurringEntriesDTO.balance = 1500

      when:
      post(path, glRecurringEntriesDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'balance'
      response[0].message == 'Balance must total zero'
   }

   void "update one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)

      final glRecurringEntity = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      glRecurringDTO.id = glRecurringEntity.id

      final glRecurringDistributions = generalLedgerRecurringDistributionDataLoaderService.stream(1, glRecurringEntity, account, profitCenter).toList()
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      ).toList()
      glRecurringDistributionDTOs.eachWithIndex { it, index ->
         it.id = glRecurringDistributions[index].id
      }

      def glRecurringEntriesDTO = dataLoaderService.singleDTO(glRecurringDTO, glRecurringDistributionDTOs)

      when:
      def result = put("$path/${glRecurringEntity.id}", glRecurringEntriesDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         with(generalLedgerRecurring) {
            id != null

            with(type) {
               value == glRecurringEntriesDTO.generalLedgerRecurring.type.value
               description == glRecurringEntriesDTO.generalLedgerRecurring.type.description
            }

            with(source) {
               value == glRecurringEntriesDTO.generalLedgerRecurring.source.value
               description == glRecurringEntriesDTO.generalLedgerRecurring.source.description
            }

            reverseIndicator == glRecurringEntriesDTO.generalLedgerRecurring.reverseIndicator
            message == glRecurringEntriesDTO.generalLedgerRecurring.message
            beginDate == glRecurringEntriesDTO.generalLedgerRecurring.beginDate.toString()
            endDate == glRecurringEntriesDTO.generalLedgerRecurring.endDate.toString()
         }

         generalLedgerRecurringDistributions.eachWithIndex{ distribution, index ->
            distribution.id != null
            distribution.generalLedgerDistributionAccount.id == glRecurringEntriesDTO.generalLedgerRecurringDistributions[index].generalLedgerDistributionAccount.id
            distribution.generalLedgerDistributionProfitCenter.id == glRecurringEntriesDTO.generalLedgerRecurringDistributions[index].generalLedgerDistributionProfitCenter.myId()
            distribution.generalLedgerDistributionAmount == glRecurringEntriesDTO.generalLedgerRecurringDistributions[index].generalLedgerDistributionAmount
         }

         balance == BigDecimal.ZERO
      }
   }

   @Unroll
   void "update invalid GL recurring entry without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)

      final glRecurringEntity = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      glRecurringDTO.id = glRecurringEntity.id

      final glRecurringDistributions = generalLedgerRecurringDistributionDataLoaderService.stream(1, glRecurringEntity, account, profitCenter).toList()
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      ).toList()
      glRecurringDistributionDTOs.eachWithIndex { it, index ->
         it.id = glRecurringDistributions[index].id
      }

      def glRecurringEntriesDTO = dataLoaderService.singleDTO(glRecurringDTO, glRecurringDistributionDTOs)
      glRecurringEntriesDTO["$nonNullableProp"] = null

      when:
      put("$path/${glRecurringEntity.id}", glRecurringEntriesDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp                        || errorResponsePath
      'balance'                              || 'balance'
      'generalLedgerRecurring'               || 'generalLedgerRecurring'
   }

   @Unroll
   void "update invalid GL recurring entry with non-existing GL recurring #testProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)

      final glRecurringEntity = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      glRecurringDTO.id = glRecurringEntity.id

      final glRecurringDistributions = generalLedgerRecurringDistributionDataLoaderService.stream(1, glRecurringEntity, account, profitCenter).toList()
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      ).toList()
      glRecurringDistributionDTOs.eachWithIndex { it, index ->
         it.id = glRecurringDistributions[index].id
      }

      def glRecurringEntriesDTO = dataLoaderService.singleDTO(glRecurringDTO, glRecurringDistributionDTOs)
      glRecurringEntriesDTO.generalLedgerRecurring."$testProp" = invalidValue

      when:
      put("$path/${glRecurringEntity.id}", glRecurringEntriesDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage

      where:
      testProp | invalidValue                                                                                                || errorResponsePath                   | errorMessage
      'source' | new GeneralLedgerSourceCodeDTO(UUID.fromString('ee2359b6-c88c-11eb-8098-02420a4d0702'), 'Z', 'Invalid DTO') || 'generalLedgerRecurring.source.id'  | 'ee2359b6-c88c-11eb-8098-02420a4d0702 was unable to be found'
      'type'   | new GeneralLedgerRecurringTypeDTO('Z', 'Invalid DTO')                                                       || 'generalLedgerRecurring.type.value' | 'Z was unable to be found'
   }

   @Unroll
   void "update invalid GL recurring entry with non-existing GL recurring distribution #testProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)

      final glRecurringEntity = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      glRecurringDTO.id = glRecurringEntity.id

      final glRecurringDistributions = generalLedgerRecurringDistributionDataLoaderService.stream(1, glRecurringEntity, account, profitCenter).toList()
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      ).toList()
      glRecurringDistributionDTOs.eachWithIndex { it, index ->
         it.id = glRecurringDistributions[index].id
      }

      def glRecurringEntriesDTO = dataLoaderService.singleDTO(glRecurringDTO, glRecurringDistributionDTOs)
      glRecurringEntriesDTO.generalLedgerRecurringDistributions.forEach {
         it."$testProp" = invalidValue
      }

      when:
      put("$path/${glRecurringEntity.id}", glRecurringEntriesDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage

      where:
      testProp                                | invalidValue                                                                       || errorResponsePath | errorMessage
      'generalLedgerDistributionAccount'      | new SimpleIdentifiableDTO(UUID.fromString('0fd98cc1-0870-4a98-958b-e62bf5c389e8')) || 'generalLedgerRecurringDistributions[index].generalLedgerDistributionAccount.id'      | "0fd98cc1-0870-4a98-958b-e62bf5c389e8 was unable to be found"
      'generalLedgerDistributionProfitCenter' | new SimpleLegacyIdentifiableDTO(999_999)                                           || 'generalLedgerRecurringDistributions[index].generalLedgerDistributionProfitCenter.id' | '999999 was unable to be found'
   }

   void "update invalid GL recurring entry with begin date after end date" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)

      final glRecurringEntity = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      glRecurringDTO.id = glRecurringEntity.id

      final glRecurringDistributions = generalLedgerRecurringDistributionDataLoaderService.stream(1, glRecurringEntity, account, profitCenter).toList()
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      ).toList()
      glRecurringDistributionDTOs.eachWithIndex { it, index ->
         it.id = glRecurringDistributions[index].id
      }

      def glRecurringEntriesDTO = dataLoaderService.singleDTO(glRecurringDTO, glRecurringDistributionDTOs)
      glRecurringEntriesDTO.generalLedgerRecurring.beginDate = LocalDate.now()
      glRecurringEntriesDTO.generalLedgerRecurring.endDate = LocalDate.now().minusDays(2)

      when:
      put("$path/${glRecurringEntity.id}", glRecurringEntriesDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'generalLedgerRecurring.beginDate'
      response[0].message == "\"End date of ${glRecurringEntriesDTO.generalLedgerRecurring.endDate} was before start date of ${glRecurringEntriesDTO.generalLedgerRecurring.beginDate}\""
   }

   void "update invalid GL recurring entry with non-zero balance" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)

      final glRecurringEntity = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      glRecurringDTO.id = glRecurringEntity.id

      final glRecurringDistributions = generalLedgerRecurringDistributionDataLoaderService.stream(1, glRecurringEntity, account, profitCenter).toList()
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      ).toList()
      glRecurringDistributionDTOs.eachWithIndex { it, index ->
         it.id = glRecurringDistributions[index].id
      }

      def glRecurringEntriesDTO = dataLoaderService.singleDTO(glRecurringDTO, glRecurringDistributionDTOs)
      glRecurringEntriesDTO.balance = 1500

      when:
      put("$path/${glRecurringEntity.id}", glRecurringEntriesDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'balance'
      response[0].message == 'Balance must total zero'
   }

   void "delete one GL recurring entry" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      generalLedgerRecurringDistributionDataLoaderService.stream(1, glRecurring, account, profitCenter).toList()

      when:
      get("$path/${glRecurring.id}")
      delete("$path/${glRecurring.id}")

      then:
      notThrown(Exception)

      when:
      get("$path/${glRecurring.id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${glRecurring.id} was unable to be found"
      response.code == 'system.not.found'
   }

   void "delete GL recurring entry from other company is not allowed" () {
      given:
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds2)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(tstds2, glSourceCode)
      final account = accountDataLoaderService.single(tstds2)
      final profitCenter = storeFactoryService.store(3, tstds2)
      generalLedgerRecurringDistributionDataLoaderService.stream(1, glRecurring, account, profitCenter).toList()

      when:
      get("$path/${glRecurring.id}")
      delete("$path/${glRecurring.id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${glRecurring.id} was unable to be found"
      response.code == 'system.not.found'
   }

   void "recreate deleted GL recurring entry" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      ).toList()
      def glRecurringEntriesDTO = dataLoaderService.singleDTO(glRecurringDTO, glRecurringDistributionDTOs)

      when: // create a GL recurring entry
      def response1 = post(path, glRecurringEntriesDTO)

      then:
      notThrown(Exception)
      response1 != null
      with(response1) {
         with(generalLedgerRecurring) {
            id != null

            with(type) {
               value == glRecurringEntriesDTO.generalLedgerRecurring.type.value
               description == glRecurringEntriesDTO.generalLedgerRecurring.type.description
            }

            with(source) {
               value == glRecurringEntriesDTO.generalLedgerRecurring.source.value
               description == glRecurringEntriesDTO.generalLedgerRecurring.source.description
            }

            reverseIndicator == glRecurringEntriesDTO.generalLedgerRecurring.reverseIndicator
            message == glRecurringEntriesDTO.generalLedgerRecurring.message
            beginDate == glRecurringEntriesDTO.generalLedgerRecurring.beginDate.toString()
            endDate == glRecurringEntriesDTO.generalLedgerRecurring.endDate.toString()
         }

         generalLedgerRecurringDistributions.eachWithIndex{ distribution, index ->
            distribution.id != null
            distribution.generalLedgerRecurring.id == glRecurringEntriesDTO.generalLedgerRecurringDistributions[index].generalLedgerRecurring.id
            distribution.generalLedgerDistributionAccount.id == glRecurringEntriesDTO.generalLedgerRecurringDistributions[index].generalLedgerDistributionAccount.id
            distribution.generalLedgerDistributionProfitCenter.id == glRecurringEntriesDTO.generalLedgerRecurringDistributions[index].generalLedgerDistributionProfitCenter.myId()
            distribution.generalLedgerDistributionAmount == glRecurringEntriesDTO.generalLedgerRecurringDistributions[index].generalLedgerDistributionAmount
         }

         balance == BigDecimal.ZERO
      }

      when: // delete GL recurring entry
      delete("$path/${response1.generalLedgerRecurring.id}")

      then: "GL recurring entry of user's company is deleted"
      notThrown(HttpClientResponseException)

      when: // recreate GL recurring entry
      def response2 = post(path, glRecurringEntriesDTO)

      then:
      notThrown(Exception)
      response2 != null
      with(response2) {
         with(generalLedgerRecurring) {
            id != null

            with(type) {
               value == glRecurringEntriesDTO.generalLedgerRecurring.type.value
               description == glRecurringEntriesDTO.generalLedgerRecurring.type.description
            }

            with(source) {
               value == glRecurringEntriesDTO.generalLedgerRecurring.source.value
               description == glRecurringEntriesDTO.generalLedgerRecurring.source.description
            }

            reverseIndicator == glRecurringEntriesDTO.generalLedgerRecurring.reverseIndicator
            message == glRecurringEntriesDTO.generalLedgerRecurring.message
            beginDate == glRecurringEntriesDTO.generalLedgerRecurring.beginDate.toString()
            endDate == glRecurringEntriesDTO.generalLedgerRecurring.endDate.toString()
         }

         generalLedgerRecurringDistributions.eachWithIndex{ distribution, index ->
            distribution.id != null
            distribution.generalLedgerRecurring.id == glRecurringEntriesDTO.generalLedgerRecurringDistributions[index].generalLedgerRecurring.id
            distribution.generalLedgerDistributionAccount.id == glRecurringEntriesDTO.generalLedgerRecurringDistributions[index].generalLedgerDistributionAccount.id
            distribution.generalLedgerDistributionProfitCenter.id == glRecurringEntriesDTO.generalLedgerRecurringDistributions[index].generalLedgerDistributionProfitCenter.myId()
            distribution.generalLedgerDistributionAmount == glRecurringEntriesDTO.generalLedgerRecurringDistributions[index].generalLedgerDistributionAmount
         }

         balance == BigDecimal.ZERO
      }

      when: // delete GL recurring entry again
      delete("$path/${response2.generalLedgerRecurring.id}")

      then: "GL recurring entry of user's company is deleted"
      notThrown(HttpClientResponseException)
   }

   void "transfer GL recurring entry to GL details" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      glRecurringDTO.endDate = LocalDate.now()
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final employee = employeeFactoryService.single(store)
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(acct),
         new SimpleLegacyIdentifiableDTO(store.myId())
      ).toList()
      def glRecurringEntriesDTO = dataLoaderService.singleDTO(glRecurringDTO, glRecurringDistributionDTOs)

      def filterRequest = new GeneralLedgerRecurringEntriesFilterRequest([sortBy: "id", sortDirection: "ASC"])
      filterRequest['entryType'] = glRecurringDTO.type.value
      filterRequest['sourceCode'] = glRecurringDTO.source.value
      filterRequest['entryDate'] = glRecurringDTO.lastTransferDate.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime()
      filterRequest['employeeNumber'] = employee.number

      final glDetailPage = new StandardPageRequest(1, 5, "id", "ASC")

      when: // GL recurring and GL recurring distributions are posted
      def postResult = post(path, glRecurringEntriesDTO)

      then:
      notThrown(Exception)
      postResult != null
      with(postResult) {
         with(generalLedgerRecurring) {
            id != null

            with(type) {
               value == glRecurringEntriesDTO.generalLedgerRecurring.type.value
               description == glRecurringEntriesDTO.generalLedgerRecurring.type.description
            }

            with(source) {
               value == glRecurringEntriesDTO.generalLedgerRecurring.source.value
               description == glRecurringEntriesDTO.generalLedgerRecurring.source.description
            }

            reverseIndicator == glRecurringEntriesDTO.generalLedgerRecurring.reverseIndicator
            message == glRecurringEntriesDTO.generalLedgerRecurring.message
            beginDate == glRecurringEntriesDTO.generalLedgerRecurring.beginDate.toString()
            endDate == glRecurringEntriesDTO.generalLedgerRecurring.endDate.toString()
         }

         generalLedgerRecurringDistributions.eachWithIndex{ distribution, index ->
            distribution.id != null
            distribution.generalLedgerRecurring.id == glRecurringEntriesDTO.generalLedgerRecurringDistributions[index].generalLedgerRecurring.id
            distribution.generalLedgerDistributionAccount.id == glRecurringEntriesDTO.generalLedgerRecurringDistributions[index].generalLedgerDistributionAccount.id
            distribution.generalLedgerDistributionProfitCenter.id == glRecurringEntriesDTO.generalLedgerRecurringDistributions[index].generalLedgerDistributionProfitCenter.myId()
            distribution.generalLedgerDistributionAmount == glRecurringEntriesDTO.generalLedgerRecurringDistributions[index].generalLedgerDistributionAmount
         }

         balance == BigDecimal.ZERO
      }

      when: // GL recurring and GL recurring distributions are fetched and GL details are created
      get("$path/transfer-$filterRequest")

      then:
      notThrown(Exception)

      when: // new GL detail records are fetched
      def fetchResult = get("/general-ledger/detail$glDetailPage")

      then:
      notThrown(Exception)
      with(fetchResult) {
         requested.with { new StandardPageRequest(it) } == glDetailPage
         totalElements == 1
         totalPages == 1
         first == true
         last == true
         elements.eachWithIndex { pageOneResult, index ->
            with(pageOneResult) {
               id != null
               account.id == glRecurringDistributionDTOs[index].generalLedgerDistributionAccount.id
               date == glRecurringDTO.lastTransferDate.toString()
               profitCenter.id == glRecurringDistributionDTOs[index].generalLedgerDistributionProfitCenter.id
               source.id == glRecurringDTO.source.id
               amount == glRecurringDistributionDTOs[index].generalLedgerDistributionAmount
               message == glRecurringDTO.message
               employeeNumberId == employee.number
            }
         }
      }
   }
}
