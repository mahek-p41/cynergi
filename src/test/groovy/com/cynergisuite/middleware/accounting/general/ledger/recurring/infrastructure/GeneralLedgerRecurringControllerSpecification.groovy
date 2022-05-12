package com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure

import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringTypeDTO
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.GeneralLedgerRecurringDistributionDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import jakarta.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.CONFLICT
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class GeneralLedgerRecurringControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/accounting/general-ledger/recurring"

   @Inject GeneralLedgerRecurringDataLoaderService generalLedgerRecurringDataLoaderService
   @Inject GeneralLedgerRecurringDistributionDataLoaderService generalLedgerRecurringDistributionDataLoaderService
   @Inject GeneralLedgerSourceCodeDataLoaderService generalLedgerSourceCodeDataLoaderService
   @Inject AccountTestDataLoaderService accountDataLoaderService

   void "fetch one" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds1)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(tstds1, glSourceCode)

      when:
      def result = get("$path/${glRecurring.id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == glRecurring.id
         reverseIndicator == glRecurring.reverseIndicator
         message == glRecurring.message
         beginDate == glRecurring.beginDate.toString()
         endDate == glRecurring.endDate.toString()

         with(type) {
            value == glRecurring.type.value
            description == glRecurring.type.description
         }

         with(source) {
            value == glRecurring.source.value
            description == glRecurring.source.description
         }
      }
   }

   void "fetch all" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds1)
      final glSourceCodeTstds2 = generalLedgerSourceCodeDataLoaderService.single(tstds2)
      final glRecurrings = generalLedgerRecurringDataLoaderService.stream(3, tstds1, glSourceCode).toList()
      generalLedgerRecurringDataLoaderService.stream(5, tstds2, glSourceCodeTstds2).toList()
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
               id == glRecurrings[index].id
               reverseIndicator == glRecurrings[index].reverseIndicator
               message == glRecurrings[index].message
               beginDate == glRecurrings[index].beginDate.toString()
               endDate == glRecurrings[index].endDate.toString()

               with(type) {
                  value == glRecurrings[index].type.value
                  description == glRecurrings[index].type.description
               }

               with(source) {
                  value == glRecurrings[index].source.value
                  description == glRecurrings[index].source.description
               }
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
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds1)
      final glRecurring = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)

      when:
      def result = post(path, glRecurring)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id != null
         reverseIndicator == glRecurring.reverseIndicator
         message == glRecurring.message
         beginDate == glRecurring.beginDate.toString()
         endDate == glRecurring.endDate.toString()

         with(type) {
            value == glRecurring.type.value
            description == glRecurring.type.description
         }

         with(source) {
            value == glRecurring.source.value
            description == glRecurring.source.description
         }
      }
   }

   void "create valid GL recurring without nullable properties" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds1)
      final glRecurring = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      glRecurring.message = null
      glRecurring.endDate = null
      glRecurring.lastTransferDate = null

      when:
      def result = post(path, glRecurring)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id != null
         reverseIndicator == glRecurring.reverseIndicator
         message == null
         beginDate == glRecurring.beginDate.toString()
         endDate == null
         lastTransferDate == null

         with(type) {
            value == glRecurring.type.value
            description == glRecurring.type.description
         }

         with(source) {
            value == glRecurring.source.value
            description == glRecurring.source.description
         }
      }
   }

   @Unroll
   void "create invalid GL recurring without #nonNullableProp" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds1)
      final recurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      recurringDTO["$nonNullableProp"] = null

      when:
      post(path, recurringDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp                  || errorResponsePath
      'beginDate'                      || 'beginDate'
      'reverseIndicator'               || 'reverseIndicator'
      'source'                         || 'source'
      'type'                           || 'type'
   }

   @Unroll
   void "create invalid GL recurring with non-existing #testProp" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds1)
      final recurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      recurringDTO["$testProp"] = invalidValue

      when:
      post(path, recurringDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage

      where:
      testProp  | invalidValue                                              || errorResponsePath    | errorMessage
      'source' | new GeneralLedgerSourceCodeDTO(UUID.fromString('ee2359b6-c88c-11eb-8098-02420a4d0702'), 'Z', 'Invalid DTO') || 'source.id'  | 'ee2359b6-c88c-11eb-8098-02420a4d0702 was unable to be found'
      'type'   | new GeneralLedgerRecurringTypeDTO('Z', 'Invalid DTO')                                                       || 'type.value' | 'Z was unable to be found'
   }

   void "update one" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds1)
      final glRecurringEntity = generalLedgerRecurringDataLoaderService.single(tstds1, glSourceCode)
      final glRecurring = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      glRecurring.id = glRecurringEntity.id

      when:
      def result = put("$path/${glRecurringEntity.id}", glRecurring)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id != null
         reverseIndicator == glRecurring.reverseIndicator
         message == glRecurring.message
         beginDate == glRecurring.beginDate.toString()
         endDate == glRecurring.endDate.toString()

         with(type) {
            value == glRecurring.type.value
            description == glRecurring.type.description
         }

         with(source) {
            value == glRecurring.source.value
            description == glRecurring.source.description
         }
      }
   }

   void "update valid GL recurring without nullable properties" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds1)
      final glRecurringEntity = generalLedgerRecurringDataLoaderService.single(tstds1, glSourceCode)
      final glRecurring = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      glRecurring.id = glRecurringEntity.id
      glRecurring.message = null
      glRecurring.endDate = null
      glRecurring.lastTransferDate = null

      when:
      def result = put("$path/${glRecurringEntity.id}", glRecurring)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id != null
         reverseIndicator == glRecurring.reverseIndicator
         message == null
         beginDate == glRecurring.beginDate.toString()
         endDate == null
         lastTransferDate == null

         with(type) {
            value == glRecurring.type.value
            description == glRecurring.type.description
         }

         with(source) {
            value == glRecurring.source.value
            description == glRecurring.source.description
         }
      }
   }

   @Unroll
   void "update invalid GL recurring without #nonNullableProp" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds1)
      final glRecurringEntity = generalLedgerRecurringDataLoaderService.single(tstds1, glSourceCode)
      final glRecurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      glRecurringDTO.id = glRecurringEntity.id
      glRecurringDTO["$nonNullableProp"] = null

      when:
      post(path, glRecurringDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp                  || errorResponsePath
      'beginDate'                      || 'beginDate'
      'reverseIndicator'               || 'reverseIndicator'
      'source'                         || 'source'
      'type'                           || 'type'
   }

   @Unroll
   void "update invalid GL recurring with non-existing #testProp" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds1)
      final recurringEntity = generalLedgerRecurringDataLoaderService.single(tstds1, glSourceCode)
      final recurringDTO = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      recurringDTO["$testProp"] = invalidValue

      when:
      put("$path/${recurringEntity.id}", recurringDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage

      where:
      testProp  | invalidValue                                              || errorResponsePath    | errorMessage
      'source'  | new GeneralLedgerSourceCodeDTO (UUID.fromString('ee2359b6-c88c-11eb-8098-02420a4d0702'), 'Z', 'Invalid DTO')  || 'source.id'          | 'ee2359b6-c88c-11eb-8098-02420a4d0702 was unable to be found'
      'type'    | new GeneralLedgerRecurringTypeDTO ('Z', 'Invalid DTO')    || 'type.value'         | 'Z was unable to be found'
   }

   void "delete one GL recurring" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final acct1 = accountDataLoaderService.single(tstds1)
      final profitCenter = storeFactoryService.store(3, tstds1)
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds1)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(tstds1, glSourceCode)
      generalLedgerRecurringDistributionDataLoaderService.single(glRecurring, acct1, profitCenter)

      when:
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

   void "delete GL recurring from other company is not allowed" () {
      given:
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds2)
      final glRecurring = generalLedgerRecurringDataLoaderService.single(tstds2, glSourceCode)

      when:
      delete("$path/${glRecurring.id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${glRecurring.id} was unable to be found"
      response.code == 'system.not.found'
   }

   void "recreate deleted GL recurring" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds1)
      final glRecurring = generalLedgerRecurringDataLoaderService.singleDTO(glSourceCode)
      final account = accountDataLoaderService.single(tstds1)
      final profitCenter = storeFactoryService.store(3, tstds1)
      final glRecurringDistributionDTO = generalLedgerRecurringDistributionDataLoaderService.singleDTO(
         glRecurring,
         new AccountDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId())
      )

      when: // create a GL recurring
      def response1 = post(path, glRecurring)
      glRecurringDistributionDTO.generalLedgerRecurring.id = response1.id
      post("/accounting/general-ledger/recurring/distribution", glRecurringDistributionDTO)

      then:
      notThrown(Exception)
      response1 != null
      with(response1) {
         id != null
         reverseIndicator == glRecurring.reverseIndicator
         message == glRecurring.message
         beginDate == glRecurring.beginDate.toString()
         endDate == glRecurring.endDate.toString()

         with(type) {
            value == glRecurring.type.value
            description == glRecurring.type.description
         }

         with(source) {
            value == glRecurring.source.value
            description == glRecurring.source.description
         }
      }

      when: // delete GL recurring

      delete("$path/$response1.id")

      then: "GL recurring of user's company is deleted"
      notThrown(HttpClientResponseException)

      when: // recreate GL recurring

      def response2 = post(path, glRecurring)
      glRecurringDistributionDTO.generalLedgerRecurring.id = response2.id
      post("/accounting/general-ledger/recurring/distribution", glRecurringDistributionDTO)

      then:
      notThrown(Exception)
      response2 != null
      with(response2) {
         id != null
         reverseIndicator == glRecurring.reverseIndicator
         message == glRecurring.message
         beginDate == glRecurring.beginDate.toString()
         endDate == glRecurring.endDate.toString()

         with(type) {
            value == glRecurring.type.value
            description == glRecurring.type.description
         }

         with(source) {
            value == glRecurring.source.value
            description == glRecurring.source.description
         }
      }

      when: // delete GL recurring again
      delete("$path/$response2.id")

      then: "GL recurring of user's company is deleted"
      notThrown(HttpClientResponseException)
   }
}
