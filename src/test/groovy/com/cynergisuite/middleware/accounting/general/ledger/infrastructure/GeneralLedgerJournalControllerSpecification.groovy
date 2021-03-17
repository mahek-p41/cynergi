package com.cynergisuite.middleware.accounting.general.ledger.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerJournalDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import javax.inject.Inject
import java.time.LocalDate

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NO_CONTENT
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class GeneralLedgerJournalControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/general-ledger/journal"

   @Inject GeneralLedgerJournalDataLoaderService dataLoaderService
   @Inject AccountDataLoaderService accountDataLoaderService
   @Inject GeneralLedgerSourceCodeDataLoaderService generalLedgerSourceCodeDataLoaderService

   void "fetch one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glJournal = dataLoaderService.single(company, acct, store, LocalDate.now(), glSourceCode)

      when:
      def result = get("$path/${glJournal.id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == glJournal.id
         account.id == glJournal.account.id
         profitCenter.id == glJournal.profitCenter.myId()
         date == glJournal.date.toString()

         with(source) {
            id == glJournal.source.id
            value == glJournal.source.value
            description == glJournal.source.description
         }

         amount == glJournal.amount
         message == glJournal.message
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
      final company2 = companyFactoryService.forDatasetCode('tstds2')
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glJournals = dataLoaderService.stream(12, company, acct, store, LocalDate.now(), glSourceCode).toList()
      dataLoaderService.stream(1, company2, acct, store, LocalDate.now(), glSourceCode).toList()
      def pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      def pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      def pageLast = new StandardPageRequest(3, 5, "id", "ASC")
      def pageFour = new StandardPageRequest(4, 5, "id", "ASC")
      def firstPageAccount = glJournals[0..4]
      def secondPageAccount = glJournals[5..9]
      def lastPageAccount = glJournals[10,11]

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
            id == firstPageAccount[index].id
            account.id == firstPageAccount[index].account.id
            profitCenter.id == firstPageAccount[index].profitCenter.myId()
            date == firstPageAccount[index].date.toString()

            with(source) {
               id == firstPageAccount[index].source.id
               value == firstPageAccount[index].source.value
               description == firstPageAccount[index].source.description
            }

            amount == firstPageAccount[index].amount
            message == firstPageAccount[index].message
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
            id == secondPageAccount[index].id
            account.id == secondPageAccount[index].account.id
            profitCenter.id == secondPageAccount[index].profitCenter.myId()
            date == secondPageAccount[index].date.toString()

            with(source) {
               id == secondPageAccount[index].source.id
               value == secondPageAccount[index].source.value
               description == secondPageAccount[index].source.description
            }

            amount == secondPageAccount[index].amount
            message == secondPageAccount[index].message
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
            id == lastPageAccount[index].id
            account.id == lastPageAccount[index].account.id
            profitCenter.id == lastPageAccount[index].profitCenter.myId()
            date == lastPageAccount[index].date.toString()

            with(source) {
               id == lastPageAccount[index].source.id
               value == lastPageAccount[index].source.value
               description == lastPageAccount[index].source.description
            }

            amount == lastPageAccount[index].amount
            message == lastPageAccount[index].message
         }
      }

      when:
      get("$path/${pageFour}")

      then:
      final def notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final def glJournalDTO = dataLoaderService.singleDTO(
         new SimpleIdentifiableDTO(acct.myId()),
         new SimpleIdentifiableDTO(store.myId()),
         LocalDate.now(),
         new GeneralLedgerSourceCodeDTO(glSourceCode)
      )

      when:
      def result = post("$path/", glJournalDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id > 0
         account.id == glJournalDTO.account.id
         profitCenter.id == glJournalDTO.profitCenter.myId()
         date == glJournalDTO.date.toString()

         with(source) {
            id == glJournalDTO.source.id
            value == glJournalDTO.source.value
            description == glJournalDTO.source.description
         }

         amount == glJournalDTO.amount
         message == glJournalDTO.message
      }
   }

   void "create valid general ledger journal with null message" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final def glJournalDTO = dataLoaderService.singleDTO(
         new SimpleIdentifiableDTO(acct.myId()),
         new SimpleIdentifiableDTO(store.myId()),
         LocalDate.now(),
         new GeneralLedgerSourceCodeDTO(glSourceCode)
      )
      glJournalDTO.message = null

      when:
      def result = post("$path/", glJournalDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id > 0
         account.id == glJournalDTO.account.id
         profitCenter.id == glJournalDTO.profitCenter.myId()
         date == glJournalDTO.date.toString()

         with(source) {
            id == glJournalDTO.source.id
            value == glJournalDTO.source.value
            description == glJournalDTO.source.description
         }

         amount == glJournalDTO.amount
         message == glJournalDTO.message
      }
   }

   @Unroll
   void "create invalid general ledger journal without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final def glJournalDTO = dataLoaderService.singleDTO(
         new SimpleIdentifiableDTO(acct.myId()),
         new SimpleIdentifiableDTO(store.myId()),
         LocalDate.now(),
         new GeneralLedgerSourceCodeDTO(glSourceCode)
      )
      glJournalDTO["$nonNullableProp"] = null

      when:
      post("$path/", glJournalDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp     || errorResponsePath
      'account'           || 'account'
      'amount'            || 'amount'
      'date'              || 'date'
      'profitCenter'      || 'profitCenter'
      'source'            || 'source'
   }

   void "update one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final def existingGLJournal = dataLoaderService.single(company, acct, store, LocalDate.now(), glSourceCode)
      final def updatedGLJournal = dataLoaderService.singleDTO(
         new SimpleIdentifiableDTO(acct.myId()),
         new SimpleIdentifiableDTO(store.myId()),
         LocalDate.now(),
         new GeneralLedgerSourceCodeDTO(glSourceCode)
      )
      updatedGLJournal.id = existingGLJournal.id

      when:
      def result = put("$path/${existingGLJournal.id}", updatedGLJournal)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == existingGLJournal.id
         account.id == updatedGLJournal.account.id
         profitCenter.id == updatedGLJournal.profitCenter.myId()
         date == updatedGLJournal.date.toString()

         with(source) {
            id == updatedGLJournal.source.id
            value == updatedGLJournal.source.value
            description == updatedGLJournal.source.description
         }

         amount == updatedGLJournal.amount
         message == updatedGLJournal.message
      }
   }

   void "update valid general ledger journal with null message" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final def existingGLJournal = dataLoaderService.single(company, acct, store, LocalDate.now(), glSourceCode)
      final def updatedGLJournal = dataLoaderService.singleDTO(
         new SimpleIdentifiableDTO(acct.myId()),
         new SimpleIdentifiableDTO(store.myId()),
         LocalDate.now(),
         new GeneralLedgerSourceCodeDTO(glSourceCode)
      )
      updatedGLJournal.id = existingGLJournal.id
      updatedGLJournal.message = null

      when:
      def result = put("$path/${existingGLJournal.id}", updatedGLJournal)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == existingGLJournal.id
         account.id == updatedGLJournal.account.id
         profitCenter.id == updatedGLJournal.profitCenter.myId()
         date == updatedGLJournal.date.toString()

         with(source) {
            id == updatedGLJournal.source.id
            value == updatedGLJournal.source.value
            description == updatedGLJournal.source.description
         }

         amount == updatedGLJournal.amount
         message == updatedGLJournal.message
      }
   }

   @Unroll
   void "update invalid general ledger journal without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final def existingGLJournal = dataLoaderService.single(company, acct, store, LocalDate.now(), glSourceCode)
      final def updatedGLJournal = dataLoaderService.singleDTO(
         new SimpleIdentifiableDTO(acct.myId()),
         new SimpleIdentifiableDTO(store.myId()),
         LocalDate.now(),
         new GeneralLedgerSourceCodeDTO(glSourceCode)
      )
      updatedGLJournal.id = existingGLJournal.id
      updatedGLJournal["$nonNullableProp"] = null

      when:
      put("$path/${existingGLJournal.id}", updatedGLJournal)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp     || errorResponsePath
      'account'           || 'account'
      'amount'            || 'amount'
      'date'              || 'date'
      'profitCenter'      || 'profitCenter'
      'source'            || 'source'
   }
}
