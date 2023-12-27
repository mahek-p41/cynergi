package com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure

import com.cynergisuite.domain.GeneralLedgerSearchReportFilterRequest
import com.cynergisuite.domain.GeneralLedgerSourceReportFilterRequest
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.bank.BankFactoryService
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDataLoaderService
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarCompleteDTO
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
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailFilterRequest
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailPageRequest
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailPostPurgeDTO
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

import java.time.LocalDate
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
      generalLedgerSummaryDataLoaderService.single(company, glAccount, profitCenter, OverallPeriodTypeDataLoader.predefined().get(1))
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
      final company = companies.find {it.datasetCode == 'corrto' }
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      generalLedgerSummaryDataLoaderService.single(company, glAccount, profitCenter, OverallPeriodTypeDataLoader.predefined().get(1))
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
      final glAccount2 = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      final beginDate = LocalDate.parse("2021-11-09")
      final financialCalendarDTO = new FinancialCalendarCompleteDTO([periodFrom: beginDate])
      final filterOne = new GeneralLedgerDetailPageRequest([account: glAccount.number, profitCenter: profitCenter.myNumber(), fiscalYear: 2022, from: OffsetDateTime.now().minusDays(90).toLocalDate(), thru: OffsetDateTime.now().plusDays(10).toLocalDate()])
      final filterTwo = new GeneralLedgerDetailPageRequest([account: glAccount2.number, profitCenter: profitCenter.myNumber(), fiscalYear: 202, from: OffsetDateTime.now().minusDays(90).toLocalDate(), thru: OffsetDateTime.now().plusDays(10).toLocalDate()])
      final filterThree = new GeneralLedgerDetailPageRequest([account: glAccount.number, fiscalYear: 2022, from: OffsetDateTime.now().minusDays(90).toLocalDate(), thru: OffsetDateTime.now().plusDays(10).toLocalDate()])

      when:
      def result1 = post("/accounting/financial-calendar/complete", financialCalendarDTO)

      then:
      notThrown(Exception)
      result1 != null

      when:
      final glSummary = generalLedgerSummaryDataLoaderService.single(company, glAccount, profitCenter, OverallPeriodTypeDataLoader.predefined().get(1))
      def generalLedgerDetails = generalLedgerDetailDataLoaderService.stream(3, company, glAccount, profitCenter, glSource).toList()

      def result = get("$path$filterOne")

      then:
      notThrown(Exception)
      with(result) {
         requested.with {
            page = filterOne.page
            size = filterOne.size
            sortBy = filterOne.sortBy
            sortDirection = filterOne.sortDirection
            from = filterOne.from
            thru = filterOne.thru
            account = filterOne.account
         }
         totalElements == 3
         totalPages == 1
         first == true
         last == true
         elements.eachWithIndex { pageOneResult, index ->
            with(pageOneResult) {
               id == generalLedgerDetails[index].id
               account.id == generalLedgerDetails[index].account.id
               date == generalLedgerDetails[index].date.toString()
               profitCenter.number == generalLedgerDetails[index].profitCenter.number
               source.id == generalLedgerDetails[index].source.id
               amount == generalLedgerDetails[index].amount
               message == generalLedgerDetails[index].message
               employeeNumberId == generalLedgerDetails[index].employeeNumberId
               journalEntryNumber == generalLedgerDetails[index].journalEntryNumber
            }
         }
      }

      when:
      get("$path$filterTwo")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT

      when:
      get("$path$filterThree")

      then:
      notThrown(Exception)
      with(result) {
         requested.with {
            page = filterThree.page
            size = filterThree.size
            sortBy = filterThree.sortBy
            sortDirection = filterThree.sortDirection
            from = filterThree.from
            thru = filterThree.thru
            account = filterThree.account
         }
         totalElements == 3
         totalPages == 1
         first == true
         last == true
      }
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
      generalLedgerDetail.account.id = nonExistentId
      generalLedgerDetail.profitCenter.id = 999999
      generalLedgerDetail.source.id = nonExistentId

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
      response[1].message == "999999 was unable to be found"
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
      generalLedgerSummaryDataLoaderService.single(company, glAccount, profitCenter, OverallPeriodTypeDataLoader.predefined().get(1))
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
      generalLedgerSummaryDataLoaderService.single(company, glAccount, profitCenter, OverallPeriodTypeDataLoader.predefined().get(1))
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
      generalLedgerSummaryDataLoaderService.single(company, glAccount, profitCenter, OverallPeriodTypeDataLoader.predefined().get(1))
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
      generalLedgerSummaryDataLoaderService.single(company, glAccount, profitCenter, OverallPeriodTypeDataLoader.predefined().get(1))
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
      generalLedgerSummaryDataLoaderService.single(company, glAccount, profitCenter, OverallPeriodTypeDataLoader.predefined().get(1))
      final existingGLDetail = generalLedgerDetailDataLoaderService.single(company, glAccount, profitCenter, glSource)
      final updatedGLDetail = GeneralLedgerDetailDataLoader.streamDTO(1, glAccount, profitCenter, glSource).findFirst().get()
      updatedGLDetail.id = existingGLDetail.id
      updatedGLDetail.account.id = nonExistentId
      updatedGLDetail.profitCenter.id = 999999
      updatedGLDetail.source.id = nonExistentId

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
      sortedResponse[1].message == "999999 was unable to be found"
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
      generalLedgerSummaryDataLoaderService.single(company, glAccount, profitCenter, OverallPeriodTypeDataLoader.predefined().get(1))
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
            filterRequest['profitCenter'] = glDetailsDTO[0].profitCenter.storeNumber
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
      final profitCenter1 = storeFactoryService.store(1, nineNineEightEmployee.company)
      final profitCenter2 = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource1 = sourceCodeDataLoaderService.single(company)
      final glSource2 = sourceCodeDataLoaderService.single(company)
      final finCalDTO = financialCalendarDataLoaderService.singleDTO()
      finCalDTO.fiscalYear = LocalDate.now().year
      finCalDTO.period = 1
      finCalDTO.generalLedgerOpen = true
      finCalDTO.accountPayableOpen = true
      finCalDTO.periodFrom = LocalDate.now().minusMonths(1)
      finCalDTO.periodTo = LocalDate.now()
      financialCalendarRepository.insert(new FinancialCalendarEntity(finCalDTO, OverallPeriodTypeDataLoader.predefined().get(1)), company)
      generalLedgerSummaryDataLoaderService.single(company, glAccount1, profitCenter1, OverallPeriodTypeDataLoader.predefined().get(1))
      generalLedgerSummaryDataLoaderService.single(company, glAccount2, profitCenter1, OverallPeriodTypeDataLoader.predefined().get(1))
      generalLedgerSummaryDataLoaderService.single(company, glAccount1, profitCenter2, OverallPeriodTypeDataLoader.predefined().get(1))
      generalLedgerSummaryDataLoaderService.single(company, glAccount2, profitCenter2, OverallPeriodTypeDataLoader.predefined().get(1))
      final glDetailsDTO = generalLedgerDetailDataLoaderService.streamDTO(1, glAccount1, profitCenter1, glSource1).toList()
      glDetailsDTO.add(generalLedgerDetailDataLoaderService.singleDTO(glAccount2, profitCenter1, glSource1))
      glDetailsDTO.add(generalLedgerDetailDataLoaderService.singleDTO(glAccount1, profitCenter1, glSource1))
      glDetailsDTO.add(generalLedgerDetailDataLoaderService.singleDTO(glAccount2, profitCenter2, glSource1))
      glDetailsDTO.add(generalLedgerDetailDataLoaderService.singleDTO(glAccount1, profitCenter2, glSource2))
      glDetailsDTO.add(generalLedgerDetailDataLoaderService.singleDTO(glAccount2, profitCenter2, glSource2))

      glDetailsDTO[0].amount = 100
      glDetailsDTO[0].message = "test"
      glDetailsDTO[0].date = LocalDate.now().minusDays(30)
      glDetailsDTO[0].journalEntryNumber = 1

      glDetailsDTO[1].amount = -100
      glDetailsDTO[1].message = "test"
      glDetailsDTO[1].date = LocalDate.now().minusDays(20)
      glDetailsDTO[1].journalEntryNumber = 1

      glDetailsDTO[2].amount = 200
      glDetailsDTO[2].message = "test description"
      glDetailsDTO[2].date = LocalDate.now().minusDays(15)
      glDetailsDTO[2].journalEntryNumber = 2

      glDetailsDTO[3].amount = -200
      glDetailsDTO[3].message = "test description"
      glDetailsDTO[3].date = LocalDate.now().minusDays(15)
      glDetailsDTO[3].journalEntryNumber = 2

      glDetailsDTO[4].amount = 300
      glDetailsDTO[4].message = "test"
      glDetailsDTO[4].date = LocalDate.now().minusDays(20)
      glDetailsDTO[4].journalEntryNumber = 3

      glDetailsDTO[5].amount = -300
      glDetailsDTO[5].message = "test"
      glDetailsDTO[5].date = LocalDate.now().minusDays(10)
      glDetailsDTO[5].journalEntryNumber = 3

      glDetailsDTO.eachWithIndex { glDetail, index ->
         post("$path", glDetail)
      }

      def filterRequest = new GeneralLedgerSourceReportFilterRequest()
      switch (criteria) {
         case 'Sort by account':
            filterRequest['sortBy'] = "account_number"

            break
         case 'Sort by journal entry number':
            filterRequest['sortBy'] = "journal_entry_number"

            break
         case 'Sort by description':
            filterRequest['sortBy'] = "message"

            break
         case 'Select one source code':
            filterRequest['sortBy'] = "account_number"
            filterRequest['startSource'] = glSource1.value
            filterRequest['endSource'] = glSource1.value

            break
         case 'Select one profit center':
            filterRequest['sortBy'] = "account_number"
            filterRequest['profitCenter'] = 1

            break
         case 'Select by dates':
            filterRequest['sortBy'] = "account_number"
            filterRequest['startDate'] = LocalDate.now().minusDays(15)

            break
         case 'Select one journal entry number':
            filterRequest['sortBy'] = "account_number"
            filterRequest['jeNumber'] = 2
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
      'Select one source code'            || 1               | 300        | 300
      'Select one profit center'          || 1               | 300        | 100
      'Select by dates'                   || 2               | 200        | 500
      'Select one journal entry number'   || 1               | 200        | 200
   }

   void "filter for source report sort by description" () {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource = sourceCodeDataLoaderService.single(company)
      final finCalDTO = financialCalendarDataLoaderService.singleDTO()
      finCalDTO.fiscalYear = LocalDate.now().year
      finCalDTO.period = 1
      finCalDTO.generalLedgerOpen = true
      finCalDTO.accountPayableOpen = true
      finCalDTO.periodFrom = LocalDate.now().minusMonths(1)
      finCalDTO.periodTo = LocalDate.now()
      financialCalendarRepository.insert(new FinancialCalendarEntity(finCalDTO, OverallPeriodTypeDataLoader.predefined().get(1)), company)

      final glSum = generalLedgerSummaryDataLoaderService.single(company, glAccount, profitCenter, OverallPeriodTypeDataLoader.predefined().get(1))
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
      final summary = generalLedgerSummaryDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glAccount.myId()), new SimpleLegacyIdentifiableDTO(profitCenter.myNumber()))
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
      def test = generalLedgerSummaryService.fetchOneByBusinessKey(company, glDetailsDTO.account.id, glDetailsDTO.profitCenter.storeNumber, calEnt.overallPeriod.value )
      sumEnt.netActivityPeriod1 != test.netActivityPeriod1
      sumEnt.netActivityPeriod1 == test.netActivityPeriod1 - glDetailsDTO.amount
   }

   void "post account entries where summary doesn't exist" () {
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
      def summary = generalLedgerSummaryService.fetchOneByBusinessKey(company, glDetailsDTO.account.id, glDetailsDTO.profitCenter.storeNumber, calEnt.overallPeriod.value)

      then:
      notThrown(Exception)
      summary == null

      when:
      post("$path/subroutine/", postingDTO)

      then:
      notThrown(Exception)
      def test = generalLedgerSummaryService.fetchOneByBusinessKey(company, glDetailsDTO.account.id, glDetailsDTO.profitCenter.storeNumber, calEnt.overallPeriod.value )
      test != null

   }

   void "fetch net change" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final glSrcCode = sourceCodeDataLoaderService.single(company)
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final beginDate = LocalDate.now().minusYears(3).plusMonths(1)
      final financialCalendarDTO = new FinancialCalendarCompleteDTO([periodFrom: beginDate])
      final filterRequest1 = new GeneralLedgerDetailFilterRequest([from: OffsetDateTime.now().toLocalDate(), thru: OffsetDateTime.now().plusDays(10).toLocalDate(), account: acct.number, profitCenter: store.myNumber(), fiscalYear: LocalDate.now().minusYears(1).year])
      final filterRequest2 = new GeneralLedgerDetailFilterRequest([from: OffsetDateTime.now().toLocalDate(), thru: OffsetDateTime.now().plusDays(10).toLocalDate(), account: 9999, profitCenter: store.myNumber(), fiscalYear: LocalDate.now().minusYears(1).year])

      when:
      def result1 = post("/accounting/financial-calendar/complete", financialCalendarDTO)

      then:
      notThrown(Exception)
      result1 != null

      when:
      def glSummary1 = generalLedgerSummaryDataLoaderService.single(company, acct, store, OverallPeriodTypeDataLoader.predefined().get(1))
      def glSummary2 = generalLedgerSummaryDataLoaderService.single(company, acct, store, OverallPeriodTypeDataLoader.predefined().get(2))
      def glDetails = generalLedgerDetailDataLoaderService.single(company, acct, store, glSrcCode)
      def creditAmount = (glDetails.amount < 0) ? glDetails.amount : 0
      def debitAmount = (glDetails.amount > 0) ? glDetails.amount : 0
      def result2 = get("$path/netchange$filterRequest1")

      then:
      notThrown(Exception)
      result2 != null
      with(result2) {
         beginBalance == glSummary1.beginningBalance
         credit == creditAmount
         debit == debitAmount
         netChange == creditAmount + debitAmount
         endBalance == beginBalance + glDetails.amount
      }

      when:
      get("$path/netchange$filterRequest2")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == NO_CONTENT
   }

   void "delete a list of general ledger detail"() {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource1 = sourceCodeDataLoaderService.single(company)
      final glSource2 = sourceCodeDataLoaderService.single(company)
      final glSummary = generalLedgerSummaryDataLoaderService.single(company, glAccount, profitCenter, OverallPeriodTypeDataLoader.predefined().get(1))
      final generalLedgerDetails1a = generalLedgerDetailDataLoaderService.stream(1, company, glAccount, profitCenter, glSource1, 50000.00).toList()
      final generalLedgerDetails1b = generalLedgerDetailDataLoaderService.stream(1, company, glAccount, profitCenter, glSource1, -50000.00).toList()
      final generalLedgerDetails1c = generalLedgerDetailDataLoaderService.stream(1, company, glAccount, profitCenter, glSource1, 25000.00).toList()
      final generalLedgerDetails2 = generalLedgerDetailDataLoaderService.stream(3, company, glAccount, profitCenter, glSource2).toList()

      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now().minusMonths(9), true, false).collect()

      def gld1 = get("$path/${generalLedgerDetails1a[0].id}")
      def gld2 = get("$path/${generalLedgerDetails1b[0].id}")
      def gld3 = get("$path/${generalLedgerDetails1c[0].id}")

      def gld4 = get("$path/${generalLedgerDetails2[0].id}")
      def gld5 = get("$path/${generalLedgerDetails2[1].id}")
      def gld6 = get("$path/${generalLedgerDetails2[2].id}")

      final toBeDeleted = new GeneralLedgerDetailPostPurgeDTO(LocalDate.now(), LocalDate.now().plusDays(5), glSource1.value)

      when:
      def toBeDeletedCount = get("$path/purgeCount$toBeDeleted")
      then:
      notThrown(HttpClientResponseException)
      with(toBeDeletedCount) {
         gldCount == 3
         balance == 25000.00
         debitTotal == 75000.00
         creditTotal == -50000.00
      }

      when:
      def deletedCount = delete("$path/purge$toBeDeleted")
      then:
      notThrown(HttpClientResponseException)
      deletedCount == 3

      when:
      def result = get("$path/${gld1.id}")

      then:
      final ex1 = thrown(HttpClientResponseException)
      ex1.status == NOT_FOUND

      when:
      def result2 = get("$path/${gld2.id}")

      then:
      final ex2 = thrown(HttpClientResponseException)
      ex2.status == NOT_FOUND

      when:
      def result3 = get("$path/${gld3.id}")

      then:
      final ex3 = thrown(HttpClientResponseException)
      ex3.status == NOT_FOUND

      when:
      def result4 = get("$path/${gld4.id}")

      then:
      result4.source.id == glSource2.id

      when:
      def result5 = get("$path/${gld5.id}")

      then:
      result5.source.id == glSource2.id

      when:
      def result6 = get("$path/${gld6.id}")

      then:
      result6.source.id == glSource2.id
   }

   void "Try to delete non-existing general ledger detail"() {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource1 = sourceCodeDataLoaderService.single(company)
      final glSource2 = sourceCodeDataLoaderService.single(company)
      final glSummary = generalLedgerSummaryDataLoaderService.single(company, glAccount, profitCenter, OverallPeriodTypeDataLoader.predefined().get(1))
      final generalLedgerDetails1 = generalLedgerDetailDataLoaderService.stream(3, company, glAccount, profitCenter, glSource1).toList()

      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now().minusMonths(9), true, false).collect()

      final toBeDeleted = new GeneralLedgerDetailPostPurgeDTO(LocalDate.now(), LocalDate.now().plusDays(5), glSource2.value)

      when:
      def toBeDeletedCount = get("$path/purgeCount$toBeDeleted")
      then:
      notThrown(HttpClientResponseException)
      with(toBeDeletedCount) {
         gldCount == 0
         balance == 0
         debitTotal == 0
         creditTotal == 0
      }

      when:
      delete("$path/purge$toBeDeleted")
      then:
      final ex = thrown(HttpClientResponseException)
      def response = ex.response.bodyAsJson()
      ex.status == NOT_FOUND
      response.message == 'A List of Matching General Ledger Entries was unable to be found'
   }

   void "Try to delete general ledger detail with dates outside the range of the financial calendar"() {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource1 = sourceCodeDataLoaderService.single(company)
      final glSource2 = sourceCodeDataLoaderService.single(company)
      final glSummary = generalLedgerSummaryDataLoaderService.single(company, glAccount, profitCenter, OverallPeriodTypeDataLoader.predefined().get(1))
      final generalLedgerDetails1 = generalLedgerDetailDataLoaderService.stream(3, company, glAccount, profitCenter, glSource1).toList()

      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.parse("2021-01-01"), true, false).collect()

      final toBeDeleted = new GeneralLedgerDetailPostPurgeDTO(LocalDate.now(), LocalDate.now().plusDays(5), glSource2.value)

      when:
      delete("$path/purge$toBeDeleted")
      then:
      final ex = thrown(HttpClientResponseException)
      def response = ex.response.bodyAsJson()
      ex.status == NOT_FOUND
      response.message == "${ LocalDate.now() } was unable to be found"
   }

   void "Try to get count of general ledger detail to be purged with dates outside the range of the financial calendar"() {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource1 = sourceCodeDataLoaderService.single(company)
      final glSource2 = sourceCodeDataLoaderService.single(company)
      final glSummary = generalLedgerSummaryDataLoaderService.single(company, glAccount, profitCenter, OverallPeriodTypeDataLoader.predefined().get(1))
      final generalLedgerDetails1 = generalLedgerDetailDataLoaderService.stream(3, company, glAccount, profitCenter, glSource1).toList()

      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.parse("2021-01-01"), true, false).collect()

      final toBeDeleted = new GeneralLedgerDetailPostPurgeDTO(LocalDate.now(), LocalDate.now().plusDays(5), glSource2.value)

      when:
      get("$path/purgeCount$toBeDeleted")

      then:
      final ex = thrown(HttpClientResponseException)
      def response = ex.response.bodyAsJson()
      ex.status == NOT_FOUND
      response.message == "${ LocalDate.now() } was unable to be found"
   }

   void "Try to delete general ledger detail with dates not in the same fiscal year"() {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource1 = sourceCodeDataLoaderService.single(company)
      final glSource2 = sourceCodeDataLoaderService.single(company)
      final glSummary = generalLedgerSummaryDataLoaderService.single(company, glAccount, profitCenter, OverallPeriodTypeDataLoader.predefined().get(1))
      final generalLedgerDetails1 = generalLedgerDetailDataLoaderService.stream(3, company, glAccount, profitCenter, glSource1).toList()

      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now().minusMonths(9), true, false).collect()
      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "N" }, LocalDate.parse("2024-01-01"), true, false).collect()

      final toBeDeleted = new GeneralLedgerDetailPostPurgeDTO(LocalDate.parse("2023-03-28"), LocalDate.parse("2024-04-02"), glSource2.value)

      when:
      delete("$path/purge$toBeDeleted")
      then:
      final ex = thrown(HttpClientResponseException)
      def response = ex.response.bodyAsJson()
      ex.status == BAD_REQUEST
      response.code[0] == 'cynergi.validation.dates.must.be.in.same.fiscal.year'
      response.message[0] == 'Dates 2023-03-28 and 2024-04-02 must be in same fiscal year'
   }

   void "Try to count general ledger detail to be purged with dates not in the same fiscal year"() {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource1 = sourceCodeDataLoaderService.single(company)
      final glSource2 = sourceCodeDataLoaderService.single(company)
      final glSummary = generalLedgerSummaryDataLoaderService.single(company, glAccount, profitCenter, OverallPeriodTypeDataLoader.predefined().get(1))
      final generalLedgerDetails1 = generalLedgerDetailDataLoaderService.stream(3, company, glAccount, profitCenter, glSource1).toList()

      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now().minusMonths(9), true, false).collect()
      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "N" }, LocalDate.parse("2024-01-01"), true, false).collect()

      final toBeDeleted = new GeneralLedgerDetailPostPurgeDTO(LocalDate.parse("2023-03-28"), LocalDate.parse("2024-04-02"), glSource2.value)

      when:
      get("$path/purgeCount$toBeDeleted")

      then:
      final ex = thrown(HttpClientResponseException)
      def response = ex.response.bodyAsJson()
      ex.status == BAD_REQUEST
      response.path[0] == 'fromDate'
      response.code[0] == 'cynergi.validation.dates.must.be.in.same.fiscal.year'
      response.message[0] == 'Dates 2023-03-28 and 2024-04-02 must be in same fiscal year'
   }

   void "Try to delete general ledger detail with dates not in the Current or Next fiscal years"() {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource1 = sourceCodeDataLoaderService.single(company)
      final glSource2 = sourceCodeDataLoaderService.single(company)
      final glSummary = generalLedgerSummaryDataLoaderService.single(company, glAccount, profitCenter, OverallPeriodTypeDataLoader.predefined().get(1))
      final generalLedgerDetails1 = generalLedgerDetailDataLoaderService.stream(3, company, glAccount, profitCenter, glSource1).toList()

      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "P" }, LocalDate.now().minusMonths(9), true, false).collect()

      final toBeDeleted = new GeneralLedgerDetailPostPurgeDTO(LocalDate.now(), LocalDate.now().plusDays(5), glSource2.value)

      when:
      delete("$path/purge$toBeDeleted")

      then:
      final ex = thrown(HttpClientResponseException)
      def response = ex.response.bodyAsJson()
      ex.status == BAD_REQUEST
      response.path[0] == 'fromDate'
      response.code[0] == 'cynergi.validation.dates.not.in.current.or.next.fiscal.year'
      response.message[0] == 'The selected dates must both be within the Current or Next fiscal year'
   }

   void "Try to count general ledger detail to be purged with dates not in the Current or Next fiscal years"() {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource1 = sourceCodeDataLoaderService.single(company)
      final glSource2 = sourceCodeDataLoaderService.single(company)
      final glSummary = generalLedgerSummaryDataLoaderService.single(company, glAccount, profitCenter, OverallPeriodTypeDataLoader.predefined().get(1))
      final generalLedgerDetails1 = generalLedgerDetailDataLoaderService.stream(3, company, glAccount, profitCenter, glSource1).toList()

      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "P" }, LocalDate.now().minusMonths(9), true, false).collect()

      final toBeDeleted = new GeneralLedgerDetailPostPurgeDTO(LocalDate.now(), LocalDate.now().plusDays(5), glSource2.value)

      when:
      get("$path/purgeCount$toBeDeleted")

      then:
      final ex = thrown(HttpClientResponseException)
      def response = ex.response.bodyAsJson()
      ex.status == BAD_REQUEST
      response.path[0] == 'fromDate'
      response.code[0] == 'cynergi.validation.dates.not.in.current.or.next.fiscal.year'
      response.message[0] == 'The selected dates must both be within the Current or Next fiscal year'
   }

   void "Try to delete general ledger detail with a non-existing source code value"() {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource1 = sourceCodeDataLoaderService.single(company)
      final glSummary = generalLedgerSummaryDataLoaderService.single(company, glAccount, profitCenter, OverallPeriodTypeDataLoader.predefined().get(1))
      final generalLedgerDetails1 = generalLedgerDetailDataLoaderService.stream(3, company, glAccount, profitCenter, glSource1).toList()

      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now().minusMonths(9), true, false).collect()

      final toBeDeleted = new GeneralLedgerDetailPostPurgeDTO(LocalDate.now(), LocalDate.now().plusDays(5), 'XYZ')

      when:
      delete("$path/purge$toBeDeleted")
      then:
      final ex = thrown(HttpClientResponseException)
      def response = ex.response.bodyAsJson()
      ex.status == BAD_REQUEST
      response.path[0] == 'sourceCode'
      response.code[0] == 'cynergi.validation.source.code.does.not.exist'
      response.message[0] == 'The general ledger source value entered does not exist'
   }

   void "Try to count general ledger detail to be purged with a non-existing source code value"() {
      given:
      final company = nineNineEightEmployee.company
      final glAccount = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final glSource1 = sourceCodeDataLoaderService.single(company)
      final glSummary = generalLedgerSummaryDataLoaderService.single(company, glAccount, profitCenter, OverallPeriodTypeDataLoader.predefined().get(1))
      final generalLedgerDetails1 = generalLedgerDetailDataLoaderService.stream(3, company, glAccount, profitCenter, glSource1).toList()

      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now().minusMonths(9), true, false).collect()

      final toBeDeleted = new GeneralLedgerDetailPostPurgeDTO(LocalDate.now(), LocalDate.now().plusDays(5), 'XYZ')

      when:
      get("$path/purgeCount$toBeDeleted")

      then:
      final ex = thrown(HttpClientResponseException)
      def response = ex.response.bodyAsJson()
      ex.status == BAD_REQUEST
      response.path[0] == 'sourceCode'
      response.code[0] == 'cynergi.validation.source.code.does.not.exist'
      response.message[0] == 'The general ledger source value entered does not exist'
   }
}
