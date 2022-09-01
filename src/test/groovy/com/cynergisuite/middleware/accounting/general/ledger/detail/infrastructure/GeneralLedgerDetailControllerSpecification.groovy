package com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure

import com.cynergisuite.domain.GeneralLedgerSearchReportFilterRequest
import com.cynergisuite.domain.GeneralLedgerSourceReportFilterRequest
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.bank.BankFactoryService
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDataLoaderService
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarDataLoaderService
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarEntity
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodType
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDTO
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDataLoader
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerAccountPostingDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDataLoader
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.accounting.general.ledger.journal.entry.GeneralLedgerJournalEntryDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.summary.GeneralLedgerSummaryDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.summary.GeneralLedgerSummaryEntity
import com.cynergisuite.middleware.accounting.general.ledger.summary.GeneralLedgerSummaryService
import com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure.GeneralLedgerSummaryRepository
import com.cynergisuite.middleware.store.StoreDTO
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import jakarta.inject.Inject

import java.time.OffsetDateTime

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class GeneralLedgerDetailControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/general-ledger/detail'

   @Inject AccountTestDataLoaderService accountDataLoaderService
   @Inject GeneralLedgerSourceCodeDataLoaderService sourceCodeDataLoaderService
   @Inject GeneralLedgerDetailDataLoaderService generalLedgerDetailDataLoaderService
   @Inject BankReconciliationTypeDataLoaderService bankReconciliationTypeDataLoaderService
   @Inject FinancialCalendarDataLoaderService financialCalendarDataLoaderService
   @Inject GeneralLedgerSummaryDataLoaderService generalLedgerSummaryDataLoaderService
   @Inject FinancialCalendarRepository financialCalendarRepository
   @Inject GeneralLedgerSummaryRepository generalLedgerSummaryRepository
   @Inject GeneralLedgerSourceCodeRepository generalLedgerSourceCodeRepository
   @Inject GeneralLedgerSummaryService generalLedgerSummaryService
   @Inject BankFactoryService bankFactoryService

   void "fetch one general ledger detail by company" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      final generalLedgerDetail = generalLedgerDetailDataLoaderService.single(company, glAccount, profitCenter, glSource)

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
      final nonExistentId = UUID.randomUUID()
      final company = companies.find {it.datasetCode == 'tstds2' }
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      generalLedgerDetailDataLoaderService.single(company, glAccount, profitCenter, glSource)

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
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      final generalLedgerDetails = generalLedgerDetailDataLoaderService.stream(3, company, glAccount, profitCenter, glSource).toList()
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
               id == generalLedgerDetails[index].id
               account.id == generalLedgerDetails[index].account.id
               date == generalLedgerDetails[index].date.toString()
               profitCenter.id == generalLedgerDetails[index].profitCenter.id
               source.id == generalLedgerDetails[index].source.id
               amount == generalLedgerDetails[index].amount
               message == generalLedgerDetails[index].message
               employeeNumberId == generalLedgerDetails[index].employeeNumberId
               journalEntryNumber == generalLedgerDetails[index].journalEntryNumber
            }
         }
      }

      when:
      get("$path$pageTwo")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create valid general ledger detail" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      final generalLedgerDetail = GeneralLedgerDetailDataLoader.streamDTO(1, glAccount, profitCenter, glSource).findFirst().get()

      when:
      def result = post("$path/", generalLedgerDetail)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id != null
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
         id != null
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
      final nonExistentId = UUID.randomUUID()
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      def generalLedgerDetail = GeneralLedgerDetailDataLoader.streamDTO(1, glAccount, profitCenter, glSource).findFirst().get()
      generalLedgerDetail.account = new SimpleIdentifiableDTO(nonExistentId)
      generalLedgerDetail.profitCenter = new SimpleLegacyIdentifiableDTO(0)
      generalLedgerDetail.source = new SimpleIdentifiableDTO(nonExistentId)

      when:
      post("$path", generalLedgerDetail)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 3
      response[0].path == "account.id"
      response[0].message == "$nonExistentId was unable to be found"
      response[0].code == 'system.not.found'
      response[1].path == "profitCenter.id"
      response[1].message == "0 was unable to be found"
      response[1].code == 'system.not.found'
      response[2].path == "source.id"
      response[2].message == "$nonExistentId was unable to be found"
      response[2].code == 'system.not.found'
   }

   void "update valid general ledger detail by id" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      final existingGLDetail = generalLedgerDetailDataLoaderService.single(company, glAccount, profitCenter, glSource)
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
      final existingGLDetail = generalLedgerDetailDataLoaderService.single(company, glAccount, profitCenter, glSource)

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
      final existingGLDetail = generalLedgerDetailDataLoaderService.single(company, glAccount, profitCenter, glSource)
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
      final existingGLDetail = generalLedgerDetailDataLoaderService.single(company, glAccount, profitCenter, glSource)
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
      final nonExistentId = UUID.randomUUID()
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      final existingGLDetail = generalLedgerDetailDataLoaderService.single(company, glAccount, profitCenter, glSource)
      final updatedGLDetail = GeneralLedgerDetailDataLoader.streamDTO(1, glAccount, profitCenter, glSource).findFirst().get()
      updatedGLDetail.id = existingGLDetail.id
      updatedGLDetail.account = new SimpleIdentifiableDTO(nonExistentId)
      updatedGLDetail.profitCenter = new SimpleLegacyIdentifiableDTO(0)
      updatedGLDetail.source = new SimpleIdentifiableDTO(nonExistentId)

      when:
      put("$path/$existingGLDetail.id", updatedGLDetail)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 3
      def sortedResponse = response.collect().sort { a,b -> a.path <=> b.path }
      sortedResponse[0].path == "account.id"
      sortedResponse[0].message == "$nonExistentId was unable to be found"
      sortedResponse[0].code == 'system.not.found'
      sortedResponse[1].path == "profitCenter.id"
      sortedResponse[1].message == "0 was unable to be found"
      sortedResponse[1].code == 'system.not.found'
      sortedResponse[2].path == "source.id"
      sortedResponse[2].message == "$nonExistentId was unable to be found"
      sortedResponse[2].code == 'system.not.found'
   }

   void "filter for report #criteria" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      final glDetailsDTO = generalLedgerDetailDataLoaderService.streamDTO(6, glAccount, profitCenter, glSource).toList()

      glDetailsDTO[0].amount = 1000
      glDetailsDTO[0].message = "test"
      glDetailsDTO[0].date = OffsetDateTime.now().minusDays(30).toLocalDate()

      glDetailsDTO[1].amount = 5000
      glDetailsDTO[1].date = OffsetDateTime.now().minusDays(20).toLocalDate()

      glDetailsDTO[2].amount = 3000
      glDetailsDTO[2].date = OffsetDateTime.now().minusDays(15).toLocalDate()

      glDetailsDTO[3].amount = -1000
      glDetailsDTO[3].date = OffsetDateTime.now().minusDays(15).toLocalDate()

      glDetailsDTO[4].amount = -3000
      glDetailsDTO[4].date = OffsetDateTime.now().minusDays(20).toLocalDate()

      glDetailsDTO[5].amount = -5000
      glDetailsDTO[5].date = OffsetDateTime.now().minusDays(10).toLocalDate()

      glDetailsDTO.eachWithIndex { glDetail, index ->
         post("$path", glDetail)
      }

      def frmPmtDt = OffsetDateTime.now().minusDays(30)
      def thruPmtDt = OffsetDateTime.now().minusDays(10)

      def filterRequest = new GeneralLedgerSearchReportFilterRequest([sortBy: "id", sortDirection: "ASC"])
      switch (criteria) {
         case 'StartingAccount':
            filterRequest['startingAccount'] = glAccount.number
            filterRequest['endingAccount'] = glAccount.number
            break
         case 'ProfitCenter':
            filterRequest['profitCenter'] = glDetailsDTO[0].profitCenter.id
            break
         case 'SourceCode':
            filterRequest['sourceCode'] = glSource.value
            break
         case 'TypeEntry':
            filterRequest['typeEntry'] = null
            break
         case 'TypeEntryCredit':
            filterRequest['typeEntry'] = 'C'
            break
         case 'TypeEntryDebit':
            filterRequest['typeEntry'] = 'D'
            break
         case 'Amount':
            filterRequest['lowAmount'] = -1000
            filterRequest['highAmount'] =  1000
            break
         case 'description':
            filterRequest['description'] = glDetailsDTO[0].message
            break
         case 'jeNumber':
            filterRequest["jeNumber"] = glDetailsDTO[0].journalEntryNumber
            break
         case 'PmtDateCase':
            filterRequest["frmPmtDt"] = frmPmtDt
            filterRequest["thruPmtDt"] = thruPmtDt
            break

      }

      when:
      def response = get("$path/search-report/$filterRequest")

      then:
      notThrown(Exception)
      response != null
      response.payments.size() == paymentCount

      where:
      criteria           || paymentCount
      'StartingAccount'  || 6
      'ProfitCenter'     || 6
      'SourceCode'       || 6
      'TypeEntry'        || 6
      'TypeEntryCredit'  || 3
      'TypeEntryDebit'   || 3
      'Amount'           || 2
      'description'      || 1
      'jeNumber'         || 1
      'PmtDateCase'      || 6
   }

   void "filter for source report #criteria" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount1 = accountDataLoaderService.single(company)
      final glAccount2 = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource1 = sourceCodeDataLoaderService.single(company)
      final glSource2 = sourceCodeDataLoaderService.single(company)
      final glDetailsDTO = generalLedgerDetailDataLoaderService.streamDTO(1, glAccount1, profitCenter, glSource1).toList()
      glDetailsDTO.add(generalLedgerDetailDataLoaderService.singleDTO(glAccount2, profitCenter, glSource1))
      glDetailsDTO.add(generalLedgerDetailDataLoaderService.singleDTO(glAccount1, profitCenter, glSource1))
      glDetailsDTO.add(generalLedgerDetailDataLoaderService.singleDTO(glAccount2, profitCenter, glSource1))
      glDetailsDTO.add(generalLedgerDetailDataLoaderService.singleDTO(glAccount1, profitCenter, glSource2))
      glDetailsDTO.add(generalLedgerDetailDataLoaderService.singleDTO(glAccount2, profitCenter, glSource2))

      glDetailsDTO[0].amount = 100
      glDetailsDTO[0].message = "test"
      glDetailsDTO[0].date = OffsetDateTime.now().minusDays(30).toLocalDate()
      glDetailsDTO[0].journalEntryNumber = 1

      glDetailsDTO[1].amount = -100
      glDetailsDTO[1].message = "test"
      glDetailsDTO[1].date = OffsetDateTime.now().minusDays(20).toLocalDate()
      glDetailsDTO[1].journalEntryNumber = 1

      glDetailsDTO[2].amount = 200
      glDetailsDTO[2].message = "test description"
      glDetailsDTO[2].date = OffsetDateTime.now().minusDays(15).toLocalDate()
      glDetailsDTO[2].journalEntryNumber = 2

      glDetailsDTO[3].amount = -200
      glDetailsDTO[3].message = "test description"
      glDetailsDTO[3].date = OffsetDateTime.now().minusDays(15).toLocalDate()
      glDetailsDTO[3].journalEntryNumber = 2

      glDetailsDTO[4].amount = 300
      glDetailsDTO[4].message = "test"
      glDetailsDTO[4].date = OffsetDateTime.now().minusDays(20).toLocalDate()
      glDetailsDTO[4].journalEntryNumber = 3

      glDetailsDTO[5].amount = -300
      glDetailsDTO[5].message = "test"
      glDetailsDTO[5].date = OffsetDateTime.now().minusDays(10).toLocalDate()
      glDetailsDTO[5].journalEntryNumber = 3

      glDetailsDTO.eachWithIndex { glDetail, index ->
         post("$path", glDetail)
      }

      def filterRequest = new GeneralLedgerSourceReportFilterRequest()
      switch (criteria) {
         case 'Sort by account':
            filterRequest['sortBy'] = "account_id"
            break
         case 'Sort by journal entry number':
            filterRequest['sortBy'] = "journal_entry_number"
            break
         case 'Sort by description':
            filterRequest['sortBy'] = "message"
            break
      }

      when:
      def response = get("$path/source-report/$filterRequest")

      then:
      notThrown(Exception)
      response != null
      response.sourceCodes.size() == sourceCodeCount
      response.reportTotalDebit == totalDebit
      response.reportTotalCredit == totalCredit

      where:
      criteria                            || sourceCodeCount | totalDebit | totalCredit
      'Sort by account'                   || 2               | 600        | 600
      'Sort by journal entry number'      || 2               | 600        | 600
      'Sort by description'               || 2               | 600        | 600
   }

   void "filter for source report sort by description" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      final glDetailsDTO = generalLedgerDetailDataLoaderService.streamDTO(10, glAccount, profitCenter, glSource).toList()

      glDetailsDTO[0].amount = 57
      glDetailsDTO[0].message = null
      glDetailsDTO[0].date = OffsetDateTime.now().minusDays(30).toLocalDate()
      glDetailsDTO[0].journalEntryNumber = 1

      glDetailsDTO[1].amount = -57
      glDetailsDTO[1].message = null
      glDetailsDTO[1].date = OffsetDateTime.now().minusDays(20).toLocalDate()
      glDetailsDTO[1].journalEntryNumber = 1

      glDetailsDTO[2].amount = 21
      glDetailsDTO[2].message = "second test message"
      glDetailsDTO[2].date = OffsetDateTime.now().minusDays(15).toLocalDate()
      glDetailsDTO[2].journalEntryNumber = 2

      glDetailsDTO[3].amount = 21
      glDetailsDTO[3].message = "second test message"
      glDetailsDTO[3].date = OffsetDateTime.now().minusDays(15).toLocalDate()
      glDetailsDTO[3].journalEntryNumber = 2

      glDetailsDTO[4].amount = -42
      glDetailsDTO[4].message = "second test message"
      glDetailsDTO[4].date = OffsetDateTime.now().minusDays(20).toLocalDate()
      glDetailsDTO[4].journalEntryNumber = 2

      glDetailsDTO[5].amount = 62
      glDetailsDTO[5].message = null
      glDetailsDTO[5].date = OffsetDateTime.now().minusDays(10).toLocalDate()
      glDetailsDTO[5].journalEntryNumber = 3

      glDetailsDTO[6].amount = -31
      glDetailsDTO[6].message = null
      glDetailsDTO[6].date = OffsetDateTime.now().minusDays(10).toLocalDate()
      glDetailsDTO[6].journalEntryNumber = 3

      glDetailsDTO[7].amount = -31
      glDetailsDTO[7].message = null
      glDetailsDTO[7].date = OffsetDateTime.now().minusDays(10).toLocalDate()
      glDetailsDTO[7].journalEntryNumber = 3

      glDetailsDTO[8].amount = 124
      glDetailsDTO[8].message = "first test message"
      glDetailsDTO[8].date = OffsetDateTime.now().minusDays(10).toLocalDate()
      glDetailsDTO[8].journalEntryNumber = 4

      glDetailsDTO[9].amount = -124
      glDetailsDTO[9].message = "first test message"
      glDetailsDTO[9].date = OffsetDateTime.now().minusDays(10).toLocalDate()
      glDetailsDTO[9].journalEntryNumber = 4

      glDetailsDTO.eachWithIndex { glDetail, index ->
         post("$path", glDetail)
      }

      def filterRequest = new GeneralLedgerSourceReportFilterRequest([sortBy: "message"])

      when:
      def response = get("$path/source-report/$filterRequest")

      then:
      notThrown(Exception)
      response != null
      with(response) {
         sourceCodes.size() == 1
         reportTotalDebit == 285
         reportTotalCredit == 285
         sourceCodes[0].details[1].runningDescTotalDebit == 124
         sourceCodes[0].details[1].runningDescTotalCredit == 124
         sourceCodes[0].details[4].runningDescTotalDebit == 42
         sourceCodes[0].details[4].runningDescTotalCredit == 42
         sourceCodes[0].details[9].runningDescTotalDebit == 119
         sourceCodes[0].details[9].runningDescTotalCredit == 119
      }
   }

   void "post account entries where gl source code is NOT BAL and gl Account is bank account" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      bankFactoryService.single(company, profitCenter, glAccount)
      final glSrcCode = sourceCodeDataLoaderService.singleDTO()
      glSrcCode.value = "TST"
      final glSource = new GeneralLedgerSourceCodeEntity(glSrcCode)
      final sourceEnt = generalLedgerSourceCodeRepository.insert(glSource, company)
      final bankType = bankReconciliationTypeDataLoaderService.random()
      final glDetailsDTO = new GeneralLedgerDetailDTO(generalLedgerDetailDataLoaderService.single(company, glAccount, profitCenter, sourceEnt))
      final calendar = financialCalendarDataLoaderService.singleDTO()
      calendar.generalLedgerOpen = true
      final calEnt = new FinancialCalendarEntity(calendar, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" })
      financialCalendarRepository.insert(calEnt, company)
      glDetailsDTO.date = calendar.periodFrom
      final summary = generalLedgerSummaryDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glAccount.myId()), new SimpleLegacyIdentifiableDTO(profitCenter.myId()))
      summary.overallPeriod = new OverallPeriodTypeDTO(OverallPeriodTypeDataLoader.predefined().find { it.value == "C" })
      final sumEnt = new GeneralLedgerSummaryEntity(summary, glAccount, profitCenter, new OverallPeriodType( calEnt.overallPeriod.myId(), summary.overallPeriod.description, summary.overallPeriod.value, summary.overallPeriod.abbreviation, calEnt.overallPeriod.localizationCode))
      generalLedgerSummaryRepository.insert(sumEnt, company)
      new GeneralLedgerJournalEntryDetailDTO(new AccountDTO(glAccount), new BankReconciliationTypeDTO(bankType), new StoreDTO(profitCenter), 315.00)
      final postingDTO = new GeneralLedgerAccountPostingDTO([glDetail: glDetailsDTO, bankType: null])
      when:
      def response = post("$path/subroutine/", postingDTO)

      then:
      notThrown(Exception)
      //bankRecon service created a new bank reconciliation
      response.bankRecon != null
   }


   void "post account entries where gl source code not BAL and gl Account is not bank account" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSrcCode = sourceCodeDataLoaderService.singleDTO()
      glSrcCode.value = "TST"
      final glSource = new GeneralLedgerSourceCodeEntity(glSrcCode)
      final sourceEnt = generalLedgerSourceCodeRepository.insert(glSource, company)
      final bankType = bankReconciliationTypeDataLoaderService.random()
      final glDetailsDTO = new GeneralLedgerDetailDTO(generalLedgerDetailDataLoaderService.single(company, glAccount, profitCenter, sourceEnt))
      final calendar = financialCalendarDataLoaderService.singleDTO()
      calendar.generalLedgerOpen = true
      final calEnt = new FinancialCalendarEntity(calendar, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" })
      financialCalendarRepository.insert(calEnt, company)
      glDetailsDTO.date = calendar.periodFrom
      final summary = generalLedgerSummaryDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glAccount.myId()), new SimpleLegacyIdentifiableDTO(profitCenter.myId()))
      summary.overallPeriod = new OverallPeriodTypeDTO(OverallPeriodTypeDataLoader.predefined().find { it.value == "C" })
      final sumEnt = new GeneralLedgerSummaryEntity(summary, glAccount, profitCenter, new OverallPeriodType( calEnt.overallPeriod.myId(), summary.overallPeriod.description, summary.overallPeriod.value, summary.overallPeriod.abbreviation, calEnt.overallPeriod.localizationCode))
      generalLedgerSummaryRepository.insert(sumEnt, company)
      new GeneralLedgerJournalEntryDetailDTO(new AccountDTO(glAccount), new BankReconciliationTypeDTO(bankType), new StoreDTO(profitCenter), 315.00)
      final postingDTO = new GeneralLedgerAccountPostingDTO([glDetail: glDetailsDTO, bankType: null])
      when:
      post("$path/subroutine/", postingDTO)

      then:
      notThrown(Exception)
      //glSummary updated netActivityPeriodX
      def test = generalLedgerSummaryService.fetchOneByBusinessKey(company, glDetailsDTO.account.id, glDetailsDTO.profitCenter.myId(), calEnt.overallPeriod.value )
      sumEnt.netActivityPeriod1 != test.netActivityPeriod1
      sumEnt.netActivityPeriod1 == test.netActivityPeriod1 - glDetailsDTO.amount
   }

   void "post account entries where summary doesnt exist" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSrcCode = sourceCodeDataLoaderService.singleDTO()
      glSrcCode.value = "TST"
      final glSource = new GeneralLedgerSourceCodeEntity(glSrcCode)
      final sourceEnt = generalLedgerSourceCodeRepository.insert(glSource, company)
      final bankType = bankReconciliationTypeDataLoaderService.random()
      final glDetailsDTO = new GeneralLedgerDetailDTO(generalLedgerDetailDataLoaderService.single(company, glAccount, profitCenter, sourceEnt))
      final calendar = financialCalendarDataLoaderService.singleDTO()
      calendar.generalLedgerOpen = true
      final calEnt = new FinancialCalendarEntity(calendar, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" })
      financialCalendarRepository.insert(calEnt, company)
      glDetailsDTO.date = calendar.periodFrom
      new GeneralLedgerJournalEntryDetailDTO(new AccountDTO(glAccount), new BankReconciliationTypeDTO(bankType), new StoreDTO(profitCenter), 315.00)
      final postingDTO = new GeneralLedgerAccountPostingDTO([glDetail: glDetailsDTO, bankType: null])
      when:
      def summary = generalLedgerSummaryService.fetchOneByBusinessKey(company, glDetailsDTO.account.id, glDetailsDTO.profitCenter.myId(), calEnt.overallPeriod.value)

      then:
      notThrown(Exception)
      summary == null

      when:
      post("$path/subroutine/", postingDTO)

      then:
      notThrown(Exception)
      def test = generalLedgerSummaryService.fetchOneByBusinessKey(company, glDetailsDTO.account.id, glDetailsDTO.profitCenter.myId(), calEnt.overallPeriod.value )
      test != null

   }
}
