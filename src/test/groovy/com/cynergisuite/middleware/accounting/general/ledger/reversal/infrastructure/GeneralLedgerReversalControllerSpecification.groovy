package com.cynergisuite.middleware.accounting.general.ledger.reversal.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class GeneralLedgerReversalControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/general-ledger/reversal'

   @Inject AccountDataLoaderService accountDataLoaderService
   @Inject GeneralLedgerSourceCodeDataLoaderService sourceCodeDataLoaderService
   @Inject GeneralLedgerDetailDataLoaderService detailDataLoaderService
   @Inject GeneralLedgerReversalDataLoaderService generalLedgerReversalDataLoaderService

   void "fetch one" () {
      given:
      final company = nineNineEightEmployee.company
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final sourceCodes = sourceCodeDataLoaderService.stream(2, company).toList()
      final glDetailSource = sourceCodes[0]
      final glDetail = detailDataLoaderService.single(company, account, profitCenter, glDetailSource)
      final glReversalSource = sourceCodes[1]
      final def generalLedgerReversal = generalLedgerReversalDataLoaderService.single(company, glReversalSource, glDetail)

      when:
      def result = get("$path/$generalLedgerReversal.id")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == generalLedgerReversal.id
         source.id == generalLedgerReversal.source.id
         date == generalLedgerReversal.date.toString()
         reversalDate == generalLedgerReversal.reversalDate.toString()
         generalLedgerDetail.id == generalLedgerReversal.generalLedgerDetail.id
         comment == generalLedgerReversal.comment
         entryMonth == generalLedgerReversal.entryMonth
         entryNumber == generalLedgerReversal.entryNumber
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

   void "create one" () {
      given:
      final company = nineNineEightEmployee.company
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final sourceCodes = sourceCodeDataLoaderService.stream(2, company).toList()
      final glDetailSource = sourceCodes[0]
      final glDetail = detailDataLoaderService.single(company, account, profitCenter, glDetailSource)
      final glReversalSource = sourceCodes[1]
      final def generalLedgerReversal = generalLedgerReversalDataLoaderService.single(company, glReversalSource, glDetail)

      when:
      def result = post("$path/", generalLedgerReversal)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
         source.id == generalLedgerReversal.source.id
         date == generalLedgerReversal.date.toString()
         reversalDate == generalLedgerReversal.reversalDate.toString()
         generalLedgerDetail.id == generalLedgerReversal.generalLedgerDetail.id
         comment == generalLedgerReversal.comment
         entryMonth == generalLedgerReversal.entryMonth
         entryNumber == generalLedgerReversal.entryNumber
      }
   }

   void "create valid general ledger reversal with null comment" () {
      given:
      final company = nineNineEightEmployee.company
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final sourceCodes = sourceCodeDataLoaderService.stream(2, company).toList()
      final glDetailSource = sourceCodes[0]
      final glDetail = detailDataLoaderService.single(company, account, profitCenter, glDetailSource)
      final glReversalSource = sourceCodes[1]
      def generalLedgerReversalDTO = generalLedgerReversalDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glReversalSource), new GeneralLedgerDetailDTO(glDetail))
      generalLedgerReversalDTO.comment = null

      when:
      def result = post("$path/", generalLedgerReversalDTO)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
         source.id == generalLedgerReversalDTO.source.id
         date == generalLedgerReversalDTO.date.toString()
         reversalDate == generalLedgerReversalDTO.reversalDate.toString()
         generalLedgerDetail.id == generalLedgerReversalDTO.generalLedgerDetail.id
         comment == generalLedgerReversalDTO.comment
         entryMonth == generalLedgerReversalDTO.entryMonth
         entryNumber == generalLedgerReversalDTO.entryNumber
      }
   }

   @Unroll
   void "create invalid general ledger reversal without #nonNullableProp" () {
      given:
      final company = nineNineEightEmployee.company
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final sourceCodes = sourceCodeDataLoaderService.stream(2, company).toList()
      final glDetailSource = sourceCodes[0]
      final glDetail = detailDataLoaderService.single(company, account, profitCenter, glDetailSource)
      final glReversalSource = sourceCodes[1]
      def generalLedgerReversalDTO = generalLedgerReversalDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glReversalSource), new GeneralLedgerDetailDTO(glDetail))
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
      'generalLedgerDetail'      || 'generalLedgerDetail'
      'reversalDate'             || 'reversalDate'
      'source'                   || 'source'
   }

   void "create invalid general ledger reversal with non-existing source id and general ledger detail id" () {
      given:
      final company = nineNineEightEmployee.company
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final sourceCodes = sourceCodeDataLoaderService.stream(2, company).toList()
      final glDetailSource = sourceCodes[0]
      final glDetail = detailDataLoaderService.single(company, account, profitCenter, glDetailSource)
      final glReversalSource = sourceCodes[1]
      def generalLedgerReversalDTO = generalLedgerReversalDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glReversalSource), new GeneralLedgerDetailDTO(glDetail))
      generalLedgerReversalDTO.source.id = 0
      generalLedgerReversalDTO.generalLedgerDetail.id = 0

      when:
      post("$path/", generalLedgerReversalDTO)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 2
      response[0].path == "source.id"
      response[0].message == "0 was unable to be found"
      response[1].path == "generalLedgerDetail.id"
      response[1].message == "0 was unable to be found"
   }

   void "update one" () {
      given:
      final company = nineNineEightEmployee.company
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final sourceCodes = sourceCodeDataLoaderService.stream(2, company).toList()
      final glDetailSource = sourceCodes[0]
      final glDetail = detailDataLoaderService.single(company, account, profitCenter, glDetailSource)
      final glReversalSource = sourceCodes[1]
      final def existingGLReversal = generalLedgerReversalDataLoaderService.single(company, glReversalSource, glDetail)
      def updatedGLReversal = generalLedgerReversalDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glReversalSource), new GeneralLedgerDetailDTO(glDetail))
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
         generalLedgerDetail.id == updatedGLReversal.generalLedgerDetail.id
         comment == updatedGLReversal.comment
         entryMonth == updatedGLReversal.entryMonth
         entryNumber == updatedGLReversal.entryNumber
      }
   }

   void "update valid general ledger reversal with null comment" () {
      given:
      final company = nineNineEightEmployee.company
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final sourceCodes = sourceCodeDataLoaderService.stream(2, company).toList()
      final glDetailSource = sourceCodes[0]
      final glDetail = detailDataLoaderService.single(company, account, profitCenter, glDetailSource)
      final glReversalSource = sourceCodes[1]
      final def existingGLReversal = generalLedgerReversalDataLoaderService.single(company, glReversalSource, glDetail)
      def updatedGLReversal = generalLedgerReversalDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glReversalSource), new GeneralLedgerDetailDTO(glDetail))
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
         generalLedgerDetail.id == updatedGLReversal.generalLedgerDetail.id
         comment == updatedGLReversal.comment
         entryMonth == updatedGLReversal.entryMonth
         entryNumber == updatedGLReversal.entryNumber
      }
   }

   @Unroll
   void "update invalid general ledger reversal without #nonNullableProp" () {
      given:
      final company = nineNineEightEmployee.company
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final sourceCodes = sourceCodeDataLoaderService.stream(2, company).toList()
      final glDetailSource = sourceCodes[0]
      final glDetail = detailDataLoaderService.single(company, account, profitCenter, glDetailSource)
      final glReversalSource = sourceCodes[1]
      final def existingGLReversal = generalLedgerReversalDataLoaderService.single(company, glReversalSource, glDetail)
      def updatedGLReversal = generalLedgerReversalDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glReversalSource), new GeneralLedgerDetailDTO(glDetail))
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
      'generalLedgerDetail'      || 'generalLedgerDetail'
      'reversalDate'             || 'reversalDate'
      'source'                   || 'source'
   }

   void "update invalid general ledger reversal with non-existing source id and general ledger detail id" () {
      given:
      final company = nineNineEightEmployee.company
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final sourceCodes = sourceCodeDataLoaderService.stream(2, company).toList()
      final glDetailSource = sourceCodes[0]
      final glDetail = detailDataLoaderService.single(company, account, profitCenter, glDetailSource)
      final glReversalSource = sourceCodes[1]
      final def existingGLReversal = generalLedgerReversalDataLoaderService.single(company, glReversalSource, glDetail)
      def updatedGLReversal = generalLedgerReversalDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glReversalSource), new GeneralLedgerDetailDTO(glDetail))
      updatedGLReversal.id = existingGLReversal.id
      updatedGLReversal.source.id = 0
      updatedGLReversal.generalLedgerDetail.id = 0

      when:
      put("$path/${existingGLReversal.id}", updatedGLReversal)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 2
      response[0].path == "source.id"
      response[0].message == "0 was unable to be found"
      response[1].path == "generalLedgerDetail.id"
      response[1].message == "0 was unable to be found"
   }
}
