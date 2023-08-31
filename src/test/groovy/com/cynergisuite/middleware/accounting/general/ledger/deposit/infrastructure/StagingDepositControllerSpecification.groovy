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
         .setHeader(*['Data_Set_ID', 'Store', 'Account_Number', 'Profit_Center_Number', 'JE_Date', 'Source_Code', 'JE_Amount', 'Deposit_Type' , 'Message'])
         .build()
      def csvData2 = 'coravt,1,2,1,2020-03-31,MEC,-5697.59,DEP_1,'
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
         .setHeader(*['Data_Set_ID', 'Store', 'Account_Number', 'Profit_Center_Number', 'JE_Date', 'Source_Code', 'JE_Amount', 'Deposit_Type' , 'Message'])
         .build()
      def csvData2 = 'coravt,1,2,1,2020-03-31,MEC,-5697.59,DEP_2,'
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

   void "test staging status"() {
      given: "test InloadSUMGLINTVService"
      def tstds1 = companies.find { it.datasetCode == "coravt"}
      accountDataLoaderService.single(tstds1, 1)
      accountDataLoaderService.single(tstds1, 2)
      generalLedgerSourceCodeDataLoaderService.stream(1, tstds1, "MEC").toList()

      def format = CSVFormat.DEFAULT
         .builder()
         .setHeader(*['Data_Set_ID', 'Store_Number', 'Date', 'Verify_Successful', 'Error_Amount',
                      'Dep_Cash_Amt', 'Dep_For_Oth_Str_Amt', 'Dep_From_Oth_Str_Amt', 'Dep_CC_In_Str_Amt',
                      'Dep_ACH_OLP_Amt', 'Dep_CC_OLP_Amt', 'Dep_Debit_Card_Amt'])
         .build()
      def csvData = """
            coravt,1,2020-03-31,true,000000000.00,000001050.28,000000000.00,000000150.35,000003050.63,000000000.00,000000540.74,-000000150.35,
            coravt,1,2020-03-01,true,0000000001.00,000001051.28,000000001.00,000000151.35,000003051.63,000000000.00,000000541.74,-000000151.35,
            coravt,2,2020-03-01,false,0000000002.00,000001052.28,000000002.00,000000152.35,000003052.63,000000000.00,000000542.74,-000000152.35,
         """
      def parser = CSVParser.parse(csvData, format)
      def records = parser.getRecords()

      when:
      inloadSUMGLINTVService.inloadCsv(records[0], UUID.randomUUID())
      inloadSUMGLINTVService.inloadCsv(records[1], UUID.randomUUID())
      inloadSUMGLINTVService.inloadCsv(records[2], UUID.randomUUID())

      then:
      notThrown(Exception)

      when:
      def result = get("general-ledger/deposit/status?yearMonth=2020-03")

      then:
      notThrown(HttpClientResponseException)
      with(result[0]) {
         verifySuccessful == true
         businessDate =="2020-03-01"
         movedToPendingJournalEntries == false
         store == 1
         storeName == "HOUMA"
      }
      with(result[1]) {
         verifySuccessful == false
         businessDate =="2020-03-01"
         movedToPendingJournalEntries == false
         store == 2
         storeName == "LAFAYETTE"
      }
      with(result[2]) {
         verifySuccessful == true
         businessDate =="2020-03-31"
         movedToPendingJournalEntries == false
         store == 1
         storeName == "HOUMA"
      }
   }

   void "test accounting entry details"() {
      given:
      def tstds1 = companies.find { it.datasetCode == "coravt"}
      accountDataLoaderService.single(tstds1, 1)
      accountDataLoaderService.single(tstds1, 2)
      generalLedgerSourceCodeDataLoaderService.stream(1, tstds1, "MEC").toList()

      def format = CSVFormat.DEFAULT
         .builder()
         .setHeader(*['Data_Set_ID', 'Store_Number', 'Date', 'Verify_Successful', 'Error_Amount',
                      'Dep_Cash_Amt', 'Dep_For_Oth_Str_Amt', 'Dep_From_Oth_Str_Amt', 'Dep_CC_In_Str_Amt',
                      'Dep_ACH_OLP_Amt', 'Dep_CC_OLP_Amt', 'Dep_Debit_Card_Amt'])
         .build()
      def csvData = 'coravt,1,2020-03-31,true,000000000.00,000001050.28,000000000.00,000000151.35,000003058.63,000000000.00,000000548.74,-000000151.35,'
      def parser = CSVParser.parse(csvData, format)
      def record = parser.getRecords().get(0)

      when:
      inloadSUMGLINTVService.inloadCsv(record, UUID.randomUUID())

      then: "test InloadSUMGLINTAService"
      notThrown(Exception)
      def format2 = CSVFormat.DEFAULT
         .builder()
         .setHeader(*['Data_Set_ID', 'Store', 'Account_Number', 'Profit_Center_Number', 'JE_Date', 'Source_Code', 'JE_Amount', 'Deposit_Type', 'Message'])
         .build()
      def csvData2 = """
         coravt,1,2,1,2020-03-31,MEC,-5697.59,DEP_1,
         coravt,1,2,2,2020-03-31,MEC,-9999.66,,
         """
      def parser2 = CSVParser.parse(csvData2, format2)
      def records2 = parser2.getRecords()

      when:
      inloadSUMGLINTAService.inloadCsv(records2[0], UUID.randomUUID())
      inloadSUMGLINTAService.inloadCsv(records2[1], UUID.randomUUID())

      then:
      notThrown(Exception)

      when:
      def result = get("general-ledger/deposit/status?yearMonth=2020-03")

      then:
      notThrown(HttpClientResponseException)
      with(result[0]) {
         verifySuccessful == true
         businessDate =="2020-03-31"
         movedToPendingJournalEntries == false
         store == 1
         storeName == "HOUMA"
      }
      def verifyID = result[0].id

      when:
      def result2 = get("general-ledger/deposit/detail/$verifyID")

      then:
      notThrown(HttpClientResponseException)
      with(result2) {
         with(accountingDetails[0]) {
            verifyId == verifyID.toString()
            accountNumber == 2
            profitCenterNumber == 1
            sourceValue == "MEC"
            debit == 0
            credit == -5697.59
            date == "2020-03-31"
         }
         with(accountingDetails[1]) {
            verifyId == verifyID.toString()
            accountNumber == 2
            profitCenterNumber == 2
            sourceValue == "MEC"
            debit == 0
            credit == -9999.66
            date == "2020-03-31"
         }
         debitTotal == 0.00
         creditTotal == -15697.25
      }
   }
}
