package com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDataLoader
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class GeneralLedgerDetailControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/general-ledger/detail'

   @Inject AccountDataLoaderService accountDataLoaderService
   @Inject GeneralLedgerSourceCodeDataLoaderService sourceCodeDataLoaderService
   @Inject GeneralLedgerDetailDataLoaderService generalLedgerDetailDataLoaderService

   void "fetch one general ledger detail by company" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      final def generalLedgerDetail = generalLedgerDetailDataLoaderService.single(company, glAccount, profitCenter, glSource)

      when:
      def result = get("$path/$generalLedgerDetail.id")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == generalLedgerDetail.id
         account.id == generalLedgerDetail.account.id
         date == generalLedgerDetail.date.toString()
         profitCenter.id == generalLedgerDetail.profitCenter.id
         source.id == generalLedgerDetail.source.id
         amount == generalLedgerDetail.amount
         message == generalLedgerDetail.message
         employeeNumberId == generalLedgerDetail.employeeNumberId
         journalEntryNumber == generalLedgerDetail.journalEntryNumber
      }
   }

   void "fetch one general ledger detail by company not found" () {
      given: 'an GL detail of other company'
      final company = companies.find {it.datasetCode == 'tstds2' }
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      generalLedgerDetailDataLoaderService.single(company, glAccount, profitCenter, glSource)

      when:
      get("$path/999")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "999 was unable to be found"
   }

   void "create valid general ledger detail" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      final def generalLedgerDetail = GeneralLedgerDetailDataLoader.streamDTO(1, glAccount, profitCenter, glSource).findFirst().get()

      when:
      def result = post("$path/", generalLedgerDetail)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
         account.id == generalLedgerDetail.account.id
         date == generalLedgerDetail.date.toString()
         profitCenter.id == generalLedgerDetail.profitCenter.id
         source.id == generalLedgerDetail.source.id
         amount == generalLedgerDetail.amount
         message == generalLedgerDetail.message
         employeeNumberId == generalLedgerDetail.employeeNumberId
         journalEntryNumber == generalLedgerDetail.journalEntryNumber
      }
   }

   void "create valid general ledger detail with null message, employeeNumberId, journalEntryNumber" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      def generalLedgerDetail = GeneralLedgerDetailDataLoader.streamDTO(1, glAccount, profitCenter, glSource).findFirst().get()
      generalLedgerDetail.message = null
      generalLedgerDetail.employeeNumberId = null
      generalLedgerDetail.journalEntryNumber = null

      when:
      def result = post("$path", generalLedgerDetail)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
         account.id == generalLedgerDetail.account.id
         date == generalLedgerDetail.date.toString()
         profitCenter.id == generalLedgerDetail.profitCenter.id
         source.id == generalLedgerDetail.source.id
         amount == generalLedgerDetail.amount
         message == null
         employeeNumberId == null
         journalEntryNumber == null
      }
   }

   void "create invalid general ledger detail without account" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      def generalLedgerDetail = GeneralLedgerDetailDataLoader.streamDTO(1, glAccount, profitCenter, glSource).findFirst().get()
      generalLedgerDetail.account = null

      when:
      post("$path", generalLedgerDetail)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "account"
      response[0].message == "Is required"
   }

   void "create invalid general ledger detail without profit center" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      def generalLedgerDetail = GeneralLedgerDetailDataLoader.streamDTO(1, glAccount, profitCenter, glSource).findFirst().get()
      generalLedgerDetail.profitCenter = null

      when:
      post("$path", generalLedgerDetail)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "profitCenter"
      response[0].message == "Is required"
   }

   void "create invalid general ledger detail without source" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      def generalLedgerDetail = GeneralLedgerDetailDataLoader.streamDTO(1, glAccount, profitCenter, glSource).findFirst().get()
      generalLedgerDetail.source = null

      when:
      post("$path", generalLedgerDetail)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "source"
      response[0].message == "Is required"
   }

   void "create invalid general ledger detail with non-existing account, profit center, source" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      def generalLedgerDetail = GeneralLedgerDetailDataLoader.streamDTO(1, glAccount, profitCenter, glSource).findFirst().get()
      generalLedgerDetail.account = new SimpleIdentifiableDTO(0)
      generalLedgerDetail.profitCenter = new SimpleIdentifiableDTO(0)
      generalLedgerDetail.source = new SimpleIdentifiableDTO(0)

      when:
      post("$path", generalLedgerDetail)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 3
      response[0].path == "account.id"
      response[0].message == "0 was unable to be found"
      response[1].path == "profitCenter.id"
      response[1].message == "0 was unable to be found"
      response[2].path == "source.id"
      response[2].message == "0 was unable to be found"
   }

   void "update valid general ledger detail by id" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      final def existingGLDetail = generalLedgerDetailDataLoaderService.single(company, glAccount, profitCenter, glSource)
      def updatedGLDetail = GeneralLedgerDetailDataLoader.streamDTO(1, glAccount, profitCenter, glSource).findFirst().get()
      updatedGLDetail.id = existingGLDetail.id

      when:
      def result = put("$path/$existingGLDetail.id", updatedGLDetail)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == existingGLDetail.id
         account.id == updatedGLDetail.account.id
         date == updatedGLDetail.date.toString()
         profitCenter.id == updatedGLDetail.profitCenter.id
         source.id == updatedGLDetail.source.id
         amount == updatedGLDetail.amount
         message == updatedGLDetail.message
         employeeNumberId == updatedGLDetail.employeeNumberId
         journalEntryNumber == updatedGLDetail.journalEntryNumber
      }
   }

   void "update valid general ledger detail with no change" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      final def existingGLDetail = generalLedgerDetailDataLoaderService.single(company, glAccount, profitCenter, glSource)

      when:
      def result = put("$path/$existingGLDetail.id", existingGLDetail)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == existingGLDetail.id
         account.id == existingGLDetail.account.id
         date == existingGLDetail.date.toString()
         profitCenter.id == existingGLDetail.profitCenter.id
         source.id == existingGLDetail.source.id
         amount == existingGLDetail.amount
         message == existingGLDetail.message
         employeeNumberId == existingGLDetail.employeeNumberId
         journalEntryNumber == existingGLDetail.journalEntryNumber
      }
   }

   void "update valid general ledger detail null message, employeeNumberId, journalEntryNumber" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      final def existingGLDetail = generalLedgerDetailDataLoaderService.single(company, glAccount, profitCenter, glSource)
      def updatedGLDetail = GeneralLedgerDetailDataLoader.streamDTO(1, glAccount, profitCenter, glSource).findFirst().get()
      updatedGLDetail.id = existingGLDetail.id
      updatedGLDetail.message = null
      updatedGLDetail.employeeNumberId = null
      updatedGLDetail.journalEntryNumber = null

      when:
      def result = put("$path/$existingGLDetail.id", updatedGLDetail)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == existingGLDetail.id
         account.id == updatedGLDetail.account.id
         date == updatedGLDetail.date.toString()
         profitCenter.id == updatedGLDetail.profitCenter.id
         source.id == updatedGLDetail.source.id
         amount == updatedGLDetail.amount
         message == null
         employeeNumberId == null
         journalEntryNumber == null
      }
   }

   void "update valid general ledger detail with null account, profit center, source" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      final def existingGLDetail = generalLedgerDetailDataLoaderService.single(company, glAccount, profitCenter, glSource)
      def updatedGLDetail = GeneralLedgerDetailDataLoader.streamDTO(1, glAccount, profitCenter, glSource).findFirst().get()
      updatedGLDetail.id = existingGLDetail.id
      updatedGLDetail.account = null
      updatedGLDetail.profitCenter = null
      updatedGLDetail.source = null

      when:
      put("$path/$existingGLDetail.id", updatedGLDetail)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 3
      def sortedResponse = response.collect().sort { a,b -> a.path <=> b.path }
      sortedResponse[0].path == "account"
      sortedResponse[0].message == "Is required"
      sortedResponse[1].path == "profitCenter"
      sortedResponse[1].message == "Is required"
      sortedResponse[2].path == "source"
      sortedResponse[2].message == "Is required"
   }

   void "update valid general ledger detail with non-existing account, profit center, source" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      final def existingGLDetail = generalLedgerDetailDataLoaderService.single(company, glAccount, profitCenter, glSource)
      def updatedGLDetail = GeneralLedgerDetailDataLoader.streamDTO(1, glAccount, profitCenter, glSource).findFirst().get()
      updatedGLDetail.id = existingGLDetail.id
      updatedGLDetail.account = new SimpleIdentifiableDTO(0)
      updatedGLDetail.profitCenter = new SimpleIdentifiableDTO(0)
      updatedGLDetail.source = new SimpleIdentifiableDTO(0)

      when:
      put("$path/$existingGLDetail.id", updatedGLDetail)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 3
      def sortedResponse = response.collect().sort { a,b -> a.path <=> b.path }
      sortedResponse[0].path == "account.id"
      sortedResponse[0].message == "0 was unable to be found"
      sortedResponse[1].path == "profitCenter.id"
      sortedResponse[1].message == "0 was unable to be found"
      sortedResponse[2].path == "source.id"
      sortedResponse[2].message == "0 was unable to be found"
   }
}
