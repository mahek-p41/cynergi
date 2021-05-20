package com.cynergisuite.middleware.accounting.general.ledger.reversal.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class GeneralLedgerReversalControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/general-ledger/reversal'

   @Inject GeneralLedgerSourceCodeDataLoaderService sourceCodeDataLoaderService
   @Inject GeneralLedgerReversalDataLoaderService generalLedgerReversalDataLoaderService

   void "fetch one" () {
      given:
      final company = nineNineEightEmployee.company
      final sourceCode = sourceCodeDataLoaderService.single(company)
      final generalLedgerReversal = generalLedgerReversalDataLoaderService.single(company, sourceCode)

      when:
      def result = get("$path/$generalLedgerReversal.id")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == generalLedgerReversal.id
         source.id == generalLedgerReversal.source.id
         date == generalLedgerReversal.date.toString()
         reversalDate == generalLedgerReversal.reversalDate.toString()
         comment == generalLedgerReversal.comment
         entryMonth == generalLedgerReversal.entryMonth
         entryNumber == generalLedgerReversal.entryNumber
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
      response.code == "system.not.found"
   }

   void "fetch all" () {
      given:
      final company = nineNineEightEmployee.company
      final sourceCode = sourceCodeDataLoaderService.single(company)
      final glReversals = generalLedgerReversalDataLoaderService.stream(12, company, sourceCode).toList()
      def pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      def pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      def pageLast = new StandardPageRequest(3, 5, "id", "ASC")
      def pageFour = new StandardPageRequest(4, 5, "id", "ASC")
      def firstPage = glReversals[0..4]
      def secondPage = glReversals[5..9]
      def lastPage = glReversals[10,11]

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
            id == firstPage[index].id
            source.id == firstPage[index].source.id
            date == firstPage[index].date.toString()
            reversalDate == firstPage[index].reversalDate.toString()
            comment == firstPage[index].comment
            entryMonth == firstPage[index].entryMonth
            entryNumber == firstPage[index].entryNumber
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
            id == secondPage[index].id
            source.id == secondPage[index].source.id
            date == secondPage[index].date.toString()
            reversalDate == secondPage[index].reversalDate.toString()
            comment == secondPage[index].comment
            entryMonth == secondPage[index].entryMonth
            entryNumber == secondPage[index].entryNumber
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
            id == lastPage[index].id
            source.id == lastPage[index].source.id
            date == lastPage[index].date.toString()
            reversalDate == lastPage[index].reversalDate.toString()
            comment == lastPage[index].comment
            entryMonth == lastPage[index].entryMonth
            entryNumber == lastPage[index].entryNumber
         }
      }

      when:
      get("$path/${pageFour}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create one" () {
      given:
      final company = nineNineEightEmployee.company
      final sourceCode = sourceCodeDataLoaderService.single(company)
      final generalLedgerReversal = generalLedgerReversalDataLoaderService.single(company, sourceCode)

      when:
      def result = post("$path/", generalLedgerReversal)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id != null
         source.id == generalLedgerReversal.source.id
         date == generalLedgerReversal.date.toString()
         reversalDate == generalLedgerReversal.reversalDate.toString()
         comment == generalLedgerReversal.comment
         entryMonth == generalLedgerReversal.entryMonth
         entryNumber == generalLedgerReversal.entryNumber
      }
   }

   void "create valid general ledger reversal with null comment" () {
      given:
      final company = nineNineEightEmployee.company
      final sourceCode = sourceCodeDataLoaderService.single(company)
      def generalLedgerReversalDTO = generalLedgerReversalDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(sourceCode))
      generalLedgerReversalDTO.comment = null

      when:
      def result = post("$path/", generalLedgerReversalDTO)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id != null
         source.id == generalLedgerReversalDTO.source.id
         date == generalLedgerReversalDTO.date.toString()
         reversalDate == generalLedgerReversalDTO.reversalDate.toString()
         comment == generalLedgerReversalDTO.comment
         entryMonth == generalLedgerReversalDTO.entryMonth
         entryNumber == generalLedgerReversalDTO.entryNumber
      }
   }

   @Unroll
   void "create invalid general ledger reversal without #nonNullableProp" () {
      given:
      final company = nineNineEightEmployee.company
      final sourceCode = sourceCodeDataLoaderService.single(company)
      def generalLedgerReversalDTO = generalLedgerReversalDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(sourceCode))
      generalLedgerReversalDTO["$nonNullableProp"] = null

      when:
      post("$path/", generalLedgerReversalDTO)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp            || errorResponsePath
      'date'                     || 'date'
      'entryMonth'               || 'entryMonth'
      'entryNumber'              || 'entryNumber'
      'reversalDate'             || 'reversalDate'
      'source'                   || 'source'
   }

   void "create invalid general ledger reversal with non-existing source id" () {
      given:
      final nonExistentId = UUID.randomUUID()
      final company = nineNineEightEmployee.company
      final sourceCode = sourceCodeDataLoaderService.single(company)
      def generalLedgerReversalDTO = generalLedgerReversalDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(sourceCode))
      generalLedgerReversalDTO.source.id = nonExistentId

      when:
      post("$path/", generalLedgerReversalDTO)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "source.id"
      response[0].message == "$nonExistentId was unable to be found"
   }

   void "update one" () {
      given:
      final company = nineNineEightEmployee.company
      final sourceCode = sourceCodeDataLoaderService.single(company)
      final existingGLReversal = generalLedgerReversalDataLoaderService.single(company, sourceCode)
      def updatedGLReversal = generalLedgerReversalDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(sourceCode))
      updatedGLReversal.id = existingGLReversal.id

      when:
      def result = put("$path/${existingGLReversal.id}", updatedGLReversal)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == updatedGLReversal.id
         source.id == updatedGLReversal.source.id
         date == updatedGLReversal.date.toString()
         reversalDate == updatedGLReversal.reversalDate.toString()
         comment == updatedGLReversal.comment
         entryMonth == updatedGLReversal.entryMonth
         entryNumber == updatedGLReversal.entryNumber
      }
   }

   void "update valid general ledger reversal with null comment" () {
      given:
      final company = nineNineEightEmployee.company
      final sourceCode = sourceCodeDataLoaderService.single(company)
      final existingGLReversal = generalLedgerReversalDataLoaderService.single(company, sourceCode)
      def updatedGLReversal = generalLedgerReversalDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(sourceCode))
      updatedGLReversal.id = existingGLReversal.id
      updatedGLReversal.comment = null

      when:
      def result = put("$path/${existingGLReversal.id}", updatedGLReversal)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == updatedGLReversal.id
         source.id == updatedGLReversal.source.id
         date == updatedGLReversal.date.toString()
         reversalDate == updatedGLReversal.reversalDate.toString()
         comment == updatedGLReversal.comment
         entryMonth == updatedGLReversal.entryMonth
         entryNumber == updatedGLReversal.entryNumber
      }
   }

   @Unroll
   void "update invalid general ledger reversal without #nonNullableProp" () {
      given:
      final company = nineNineEightEmployee.company
      final sourceCode = sourceCodeDataLoaderService.single(company)
      final existingGLReversal = generalLedgerReversalDataLoaderService.single(company, sourceCode)
      def updatedGLReversal = generalLedgerReversalDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(sourceCode))
      updatedGLReversal.id = existingGLReversal.id
      updatedGLReversal["$nonNullableProp"] = null

      when:
      put("$path/${existingGLReversal.id}", updatedGLReversal)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp            || errorResponsePath
      'date'                     || 'date'
      'entryMonth'               || 'entryMonth'
      'entryNumber'              || 'entryNumber'
      'reversalDate'             || 'reversalDate'
      'source'                   || 'source'
   }

   void "update invalid general ledger reversal with non-existing source id" () {
      given:
      final nonExistentId = UUID.randomUUID()
      final company = nineNineEightEmployee.company
      final sourceCode = sourceCodeDataLoaderService.single(company)
      final existingGLReversal = generalLedgerReversalDataLoaderService.single(company, sourceCode)
      def updatedGLReversal = generalLedgerReversalDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(sourceCode))
      updatedGLReversal.id = existingGLReversal.id
      updatedGLReversal.source.id = nonExistentId

      when:
      put("$path/${existingGLReversal.id}", updatedGLReversal)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "source.id"
      response[0].message == "$nonExistentId was unable to be found"
   }
}
