package com.cynergisuite.middleware.accounting.general.ledger.recurring.entries.infrastructure

import com.cynergisuite.domain.GeneralLedgerRecurringEntriesFilterRequest
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarDataLoaderService
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarDateRangeDTO
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDataLoader
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailPageRequest
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringTypeDTO
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringTypeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.GeneralLedgerRecurringDistributionDataLoader
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.GeneralLedgerRecurringDistributionDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.recurring.entries.GeneralLedgerRecurringEntriesDataLoaderService
import com.cynergisuite.middleware.store.StoreDTO
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Unroll

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
   @Inject FinancialCalendarDataLoaderService financialCalendarDataLoaderService

   void "fetch one" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
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
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurrings = generalLedgerRecurringDataLoaderService.stream(3, company, glSourceCode).toList()
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)

      def glRecurringDistributions = []
      glRecurrings.each { it ->
         glRecurringDistributions = generalLedgerRecurringDistributionDataLoaderService.stream(2, it, account, profitCenter).toList()
      }

      def pageOne = new GeneralLedgerRecurringEntriesFilterRequest(1, 5, "id", "ASC", null, null, null, null)

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
         elements.size == 3
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
            }
         }
      }
   }

   void "filter for report #criteria" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')

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
         new StoreDTO(profitCenter)
      ).toList()
      def glRecurringDistributionDTOs2 = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         2,
         glRecurringDTO2,
         new AccountDTO(account),
         new StoreDTO(profitCenter)
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
      def response = get("$path/report$filterRequest")

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
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(account),
         new StoreDTO(profitCenter)
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
      }
   }

   @Unroll
   void "create invalid GL recurring entry without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(account),
         new StoreDTO(profitCenter)
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
      'generalLedgerRecurring'               || 'generalLedgerRecurring'
   }

   @Unroll
   void "create invalid GL recurring entry with non-existing GL recurring #testProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(account),
         new StoreDTO(profitCenter)
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

   void "create invalid GL recurring entry with non-existing GL recurring distribution account" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      final invalidAccount = new AccountDTO(accountDataLoaderService.single(company))
      invalidAccount.id = UUID.fromString('0fd98cc1-0870-4a98-958b-e62bf5c389e8')
      final profitCenter = storeFactoryService.store(3, company)
      final glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         invalidAccount,
         new StoreDTO(profitCenter)
      ).toList()
      def glRecurringEntriesDTO = dataLoaderService.singleDTO(glRecurringDTO, glRecurringDistributionDTOs)

      when:
      post(path, glRecurringEntriesDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'generalLedgerRecurringDistributions[index].generalLedgerDistributionAccount.id'
      response[0].message == '0fd98cc1-0870-4a98-958b-e62bf5c389e8 was unable to be found'
   }

   void "create invalid GL recurring entry with begin date after end date" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(account),
         new StoreDTO(profitCenter)
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

   void "update one" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
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
         new StoreDTO(profitCenter)
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
      }
   }

   @Unroll
   void "update invalid GL recurring entry without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
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
         new StoreDTO(profitCenter)
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
      'generalLedgerRecurring'               || 'generalLedgerRecurring'
   }

   @Unroll
   void "update invalid GL recurring entry with non-existing GL recurring #testProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
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
         new StoreDTO(profitCenter)
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

   void "update invalid GL recurring entry with non-existing GL recurring distribution account" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      final glRecurringEntity = generalLedgerRecurringDataLoaderService.single(company, glSourceCode)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      glRecurringDTO.id = glRecurringEntity.id
      generalLedgerRecurringDistributionDataLoaderService.stream(1, glRecurringEntity, account, profitCenter).toList()
      final invalidAccount = new AccountDTO(account)
      invalidAccount.id = UUID.fromString('0fd98cc1-0870-4a98-958b-e62bf5c389e8')
      final glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         invalidAccount,
         new StoreDTO(profitCenter)
      ).toList()
      final glRecurringEntriesDTO = dataLoaderService.singleDTO(glRecurringDTO, glRecurringDistributionDTOs)

      when:
      put("$path/${glRecurringEntity.id}", glRecurringEntriesDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'generalLedgerRecurringDistributions[index].generalLedgerDistributionAccount.id'
      response[0].message == '0fd98cc1-0870-4a98-958b-e62bf5c389e8 was unable to be found'
   }

   void "update invalid GL recurring entry with begin date after end date" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
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
         new StoreDTO(profitCenter)
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

   void "delete one GL recurring entry" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
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
      final tstds2 = companyFactoryService.forDatasetCode('corrto')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds2)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(tstds2, glSourceCode)
      final account = accountDataLoaderService.single(tstds2)
      final profitCenter = storeFactoryService.store(6, tstds2)
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
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(account),
         new StoreDTO(profitCenter)
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
      }

      when: // delete GL recurring entry again
      delete("$path/${response2.generalLedgerRecurring.id}")

      then: "GL recurring entry of user's company is deleted"
      notThrown(HttpClientResponseException)
   }

   void "transfer GL recurring entry to GL details without reversal" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      glRecurringDTO.endDate = LocalDate.now()
      glRecurringDTO.reverseIndicator = false
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final employee = employeeFactoryService.single(store)
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         2,
         glRecurringDTO,
         new AccountDTO(acct),
         new StoreDTO(store),
         1000 as BigDecimal
      ).toList()
      def glRecurringDistributionCreditDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         2,
         glRecurringDTO,
         new AccountDTO(acct),
         new StoreDTO(store),
         -1000 as BigDecimal
      ).toList()
      glRecurringDistributionDTOs.addAll(glRecurringDistributionCreditDTOs)
      def glRecurringEntriesDTO = dataLoaderService.singleDTO(glRecurringDTO, glRecurringDistributionDTOs)
      def entryDate = glRecurringDTO.lastTransferDate.atStartOfDay(ZoneId.of("-05:00")).toLocalDate()
      def filterRequest = new GeneralLedgerRecurringEntriesFilterRequest([sortBy: "id", sortDirection: "ASC"])
      filterRequest['entryType'] = glRecurringDTO.type.value
      filterRequest['sourceCode'] = glRecurringDTO.source.value
      filterRequest['entryDate'] = glRecurringDTO.lastTransferDate.atStartOfDay(ZoneId.of("-05:00")).toLocalDate()
      filterRequest['employeeNumber'] = employee.number

      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, entryDate).collect()
      final dateRangeDTO = new FinancialCalendarDateRangeDTO(entryDate, entryDate.plusDays(80))
      final glDetailPage = new GeneralLedgerDetailPageRequest([account: acct.number, profitCenter: store.myNumber(), fiscalYear: entryDate.getYear(), from: glRecurringDTO.beginDate.minusDays(20), thru: glRecurringDTO.endDate.plusDays(20)])

      final dateRangeAP = new FinancialCalendarDateRangeDTO(entryDate, entryDate.plusDays(30))

      when:
      put("/accounting/financial-calendar/open-ap", dateRangeAP)

      then:
      notThrown(Exception)

      when: 'open GL in financial calendar'
      put("/accounting/financial-calendar/open-gl", dateRangeDTO)

      then:
      notThrown(Exception)

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
      }

      when: // GL recurring and GL recurring distributions are fetched and GL details are created
      post("$path/transfer$filterRequest", null)

      then:
      notThrown(Exception)

      when: // new GL detail records are fetched
      def fetchResult = get("/general-ledger/detail$glDetailPage")

      then:
      notThrown(Exception)
      with(fetchResult) {
         fetchResult.with {
            page = glDetailPage.page
            size = glDetailPage.size
            sortBy = glDetailPage.sortBy
            sortDirection = glDetailPage.sortDirection
            from = glDetailPage.from
            thru = glDetailPage.thru
            account = glDetailPage.account
         }
         totalElements == 4
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
               message == glRecurringDTO.message
               employeeNumberId == employee.number
            }
         }
      }
   }

   void "transfer GL recurring entry to GL details with reversal" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      glRecurringDTO.endDate = LocalDate.now()
      glRecurringDTO.reverseIndicator = true
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final employee = employeeFactoryService.single(store)
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         2,
         glRecurringDTO,
         new AccountDTO(acct),
         new StoreDTO(store),
         1000 as BigDecimal
      ).toList()
      def glRecurringDistributionCreditDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         2,
         glRecurringDTO,
         new AccountDTO(acct),
         new StoreDTO(store),
         -1000 as BigDecimal
      ).toList()
      glRecurringDistributionDTOs.addAll(glRecurringDistributionCreditDTOs)
      def glRecurringEntriesDTO = dataLoaderService.singleDTO(glRecurringDTO, glRecurringDistributionDTOs)
      def entryDate = glRecurringDTO.lastTransferDate.atStartOfDay(ZoneId.of("-05:00")).toLocalDate()
      def filterRequest = new GeneralLedgerRecurringEntriesFilterRequest([sortBy: "id", sortDirection: "ASC"])
      filterRequest['entryType'] = glRecurringDTO.type.value
      filterRequest['sourceCode'] = glRecurringDTO.source.value
      filterRequest['entryDate'] = glRecurringDTO.lastTransferDate.atStartOfDay(ZoneId.of("-05:00")).toLocalDate()
      filterRequest['employeeNumber'] = employee.number

      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, entryDate).collect()
      final dateRangeDTO = new FinancialCalendarDateRangeDTO(entryDate, entryDate.plusDays(80))
      final glDetailPage = new GeneralLedgerDetailPageRequest([account: acct.number, profitCenter: store.myNumber(), fiscalYear: entryDate.getYear(), from: glRecurringDTO.beginDate.minusDays(20), thru: glRecurringDTO.endDate.plusDays(20)])

      final dateRangeAP = new FinancialCalendarDateRangeDTO(entryDate, entryDate.plusDays(60))

      when:
      put("/accounting/financial-calendar/open-ap", dateRangeAP)

      then:
      notThrown(Exception)

      when: 'open GL in financial calendar'
      put("/accounting/financial-calendar/open-gl", dateRangeDTO)

      then:
      notThrown(Exception)

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
      }

      when: // GL recurring and GL recurring distributions are fetched and GL details are created
      post("$path/transfer$filterRequest", null)

      then:
      notThrown(Exception)

      when: // new GL detail records are fetched
      def fetchResult = get("/general-ledger/detail$glDetailPage")

      then:
      notThrown(Exception)
      with(fetchResult) {
         fetchResult.with {
            page = glDetailPage.page
            size = glDetailPage.size
            sortBy = glDetailPage.sortBy
            sortDirection = glDetailPage.sortDirection
            from = glDetailPage.from
            thru = glDetailPage.thru
            account = glDetailPage.account
         }
         totalElements == 4
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
               message == glRecurringDTO.message
               employeeNumberId == employee.number
            }
         }
      }
   }

   void "transfer a single GL recurring entry to GL details without reversal" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      glRecurringDTO.endDate = LocalDate.now()
      glRecurringDTO.reverseIndicator = false
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final employee = employeeFactoryService.single(store)
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(acct),
         new StoreDTO(store)
      ).toList()
      def glRecurringEntriesDTO = dataLoaderService.singleDTO(glRecurringDTO, glRecurringDistributionDTOs)
      def entryDate = glRecurringDTO.lastTransferDate.atStartOfDay(ZoneId.of("-05:00")).toLocalDate()
      def filterRequest = new GeneralLedgerRecurringEntriesFilterRequest([sortBy: "id", sortDirection: "ASC"])
      filterRequest['entryType'] = glRecurringDTO.type.value
      filterRequest['sourceCode'] = glRecurringDTO.source.value
      filterRequest['entryDate'] = entryDate
      filterRequest['employeeNumber'] = employee.number

      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, entryDate).collect()
      final dateRangeDTO = new FinancialCalendarDateRangeDTO(entryDate, entryDate.plusDays(80))

      final dateRangeAP = new FinancialCalendarDateRangeDTO(entryDate, entryDate.plusDays(30))

      when:
      put("/accounting/financial-calendar/open-ap", dateRangeAP)

      then:
      notThrown(Exception)

      when: 'open GL in financial calendar'
      put("/accounting/financial-calendar/open-gl", dateRangeDTO)

      then:
      notThrown(Exception)

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
      }

      when:
      postResult.entryDate = glRecurringDTO.lastTransferDate.atStartOfDay(ZoneId.of("-05:00")).toLocalDate()
      post("$path/transfer/single", postResult)

      then:
      notThrown(Exception)

   }

   void "transfer a single GL recurring entry to GL details with reversal" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      def glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      glRecurringDTO.endDate = LocalDate.now()
      glRecurringDTO.reverseIndicator = true
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final employee = employeeFactoryService.single(store)
      def glRecurringDistributionDTOs = GeneralLedgerRecurringDistributionDataLoader.streamDTO(
         1,
         glRecurringDTO,
         new AccountDTO(acct),
         new StoreDTO(store)
      ).toList()
      def glRecurringEntriesDTO = dataLoaderService.singleDTO(glRecurringDTO, glRecurringDistributionDTOs)
      def entryDate = glRecurringDTO.lastTransferDate.atStartOfDay(ZoneId.of("-05:00")).toLocalDate()
      def filterRequest = new GeneralLedgerRecurringEntriesFilterRequest([sortBy: "id", sortDirection: "ASC"])
      filterRequest['entryType'] = glRecurringDTO.type.value
      filterRequest['sourceCode'] = glRecurringDTO.source.value
      filterRequest['entryDate'] = entryDate
      filterRequest['employeeNumber'] = employee.number

      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, entryDate).collect()
      final dateRangeDTO = new FinancialCalendarDateRangeDTO(entryDate, entryDate.plusDays(80))

      final dateRangeAP = new FinancialCalendarDateRangeDTO(entryDate, entryDate.plusDays(30))

      when:
      put("/accounting/financial-calendar/open-ap", dateRangeAP)

      then:
      notThrown(Exception)

      when: 'open GL in financial calendar'
      put("/accounting/financial-calendar/open-gl", dateRangeDTO)

      then:
      notThrown(Exception)

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
      }

      when:
      postResult.entryDate = glRecurringDTO.lastTransferDate.atStartOfDay(ZoneId.of("-05:00")).toLocalDate()
      post("$path/transfer/single", postResult)

      then:
      notThrown(Exception)

   }
}
