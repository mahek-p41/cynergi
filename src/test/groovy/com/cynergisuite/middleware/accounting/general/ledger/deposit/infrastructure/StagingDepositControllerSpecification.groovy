package com.cynergisuite.middleware.accounting.general.ledger.deposit.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarCompleteDTO
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarDateRangeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.deposit.StagingDepositPageRequest
import com.cynergisuite.middleware.inload.InloadSUMGLINTAService
import com.cynergisuite.middleware.inload.InloadSUMGLINTVService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser

import java.time.LocalDate

import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class StagingDepositControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/general-ledger/deposit"

   @Inject AccountTestDataLoaderService accountDataLoaderService
   @Inject GeneralLedgerSourceCodeDataLoaderService generalLedgerSourceCodeDataLoaderService
   @Inject InloadSUMGLINTVService inloadSUMGLINTVService
   @Inject InloadSUMGLINTAService inloadSUMGLINTAService

   void "create pending journal entries from staging accounting entries by selection" () {
      given:
      def tstds1 = companies.find { it.datasetCode == "coravt"}
      accountDataLoaderService.single(tstds1, 1)
      accountDataLoaderService.single(tstds1, 2)
      generalLedgerSourceCodeDataLoaderService.stream(1, tstds1, "MEC").toList()
      final financialCalendarDTO = new FinancialCalendarCompleteDTO([periodFrom: new LocalDate(2018,01, 01)])
      final finCalDateRange = new FinancialCalendarDateRangeDTO(new LocalDate(2020, 01, 01), new LocalDate(2020, 03, 31))
      post("/accounting/financial-calendar/complete", financialCalendarDTO)

      put("/accounting/financial-calendar/open-gl", finCalDateRange)
      def format = CSVFormat.DEFAULT
         .builder()
         .setHeader(*['Data_Set_ID', 'Store_Number', 'Date', 'Verify_Successful', 'Error_Amount',
                      'Dep_Cash_Amt', 'Dep_For_Oth_Str_Amt', 'Dep_From_Oth_Str_Amt', 'Dep_CC_In_Str_Amt',
                      'Dep_ACH_OLP_Amt', 'Dep_CC_OLP_Amt', 'Dep_Debit_Card_Amt'])
         .build()
      def csvData = 'coravt,1,2020-03-31,true,000000000.00,000001050.28,000000000.00,000000151.35,000003058.63,000000000.00,000000548.74,-000000151.35,'
      def parser = CSVParser.parse(csvData, format)
      def record = parser.getRecords().get(0)

      def format2 = CSVFormat.DEFAULT
         .builder()
         .setHeader(*['Data_Set_ID', 'Store', 'Account_Number', 'Profit_Center_Number', 'JE_Date', 'Source_Code', 'JE_Amount', 'Message'])
         .build()
      def csvData2 = 'coravt,1,2,1,2020-03-31,MEC,-5697.59,'
      def parser2 = CSVParser.parse(csvData2, format2)
      def record2 = parser2.getRecords().get(0)
      inloadSUMGLINTVService.inloadCsv(record, UUID.randomUUID())
      inloadSUMGLINTAService.inloadCsv(record2, UUID.randomUUID())
      def filter = new StagingDepositPageRequest([verifiedSuccessful: true])
      when:
      def stagingDTO = get("$path$filter")
      def idList = stagingDTO.elements.collect { it.id }
      def result = post("$path/day", idList)
      get("$path$filter")
      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT

   }

   void "create pending journal entries from staging accounting entries by criteria" () {
      given:
      def tstds1 = companies.find { it.datasetCode == "coravt"}
      accountDataLoaderService.single(tstds1, 1)
      accountDataLoaderService.single(tstds1, 2)
      generalLedgerSourceCodeDataLoaderService.stream(1, tstds1, "MEC").toList()
      final financialCalendarDTO = new FinancialCalendarCompleteDTO([periodFrom: new LocalDate(2018,01, 01)])
      final finCalDateRange = new FinancialCalendarDateRangeDTO(new LocalDate(2020, 01, 01), new LocalDate(2020, 03, 31))
      post("/accounting/financial-calendar/complete", financialCalendarDTO)

      put("/accounting/financial-calendar/open-gl", finCalDateRange)
      def format = CSVFormat.DEFAULT
         .builder()
         .setHeader(*['Data_Set_ID', 'Store_Number', 'Date', 'Verify_Successful', 'Error_Amount',
                      'Dep_Cash_Amt', 'Dep_For_Oth_Str_Amt', 'Dep_From_Oth_Str_Amt', 'Dep_CC_In_Str_Amt',
                      'Dep_ACH_OLP_Amt', 'Dep_CC_OLP_Amt', 'Dep_Debit_Card_Amt'])
         .build()
      def csvData = 'coravt,1,2020-03-31,true,000000000.00,000001050.28,000000000.00,000000151.35,000003058.63,000000000.00,000000548.74,-000000151.35,'
      def parser = CSVParser.parse(csvData, format)
      def record = parser.getRecords().get(0)

      def format2 = CSVFormat.DEFAULT
         .builder()
         .setHeader(*['Data_Set_ID', 'Store', 'Account_Number', 'Profit_Center_Number', 'JE_Date', 'Source_Code', 'JE_Amount', 'Message'])
         .build()
      def csvData2 = 'coravt,1,2,1,2020-03-31,MEC,-5697.59,'
      def parser2 = CSVParser.parse(csvData2, format2)
      def record2 = parser2.getRecords().get(0)
      inloadSUMGLINTVService.inloadCsv(record, UUID.randomUUID())
      inloadSUMGLINTAService.inloadCsv(record2, UUID.randomUUID())
      def postFilter = new StagingDepositPageRequest([beginStore: 1, endStore: 1, verifiedSuccessful: true])
      def filter = new StagingDepositPageRequest([verifiedSuccessful: true])
      def filter2 = "&lastDayOfMonth=2020-03-31"
      when:
      post("$path/month-criteria$postFilter&lastDayOfMonth=2020-03-31", null)
      get("$path$filter")
      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT

   }
}
