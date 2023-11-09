package com.cynergisuite.middleware.inload

import com.cynergisuite.domain.GeneralLedgerJournalFilterRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser

@MicronautTest(transactional = false)
class InloadServiceSpecification extends ControllerSpecificationBase {

   @Inject AccountTestDataLoaderService accountTestDataLoaderService
   @Inject GeneralLedgerSourceCodeDataLoaderService generalLedgerSourceCodeDataLoaderService
   @Inject InloadMEINVService inloadMEINVService
   @Inject InloadSUMGLDETService inloadSUMGLDETService
   @Inject InloadSUMGLINTVService inloadSUMGLINTVService
   @Inject InloadSUMGLINTAService inloadSUMGLINTAService


   void "test InloadMEINVService"() {
      given:
      def tstds1 = companies.find { it.datasetCode == "coravt"}
      accountTestDataLoaderService.single(tstds1, 1)
      accountTestDataLoaderService.single(tstds1, 2)
      generalLedgerSourceCodeDataLoaderService.stream(1, tstds1, "MEC").toList()
      def format = CSVFormat.DEFAULT
         .builder()
         .setHeader(*['Data_Set_ID', 'Account_Number', 'Profit_Center_Number', 'JE_Date', 'Source_Code', 'JE_Amount'])
         .build()
      def csvData = 'coravt,1,1,2020-03-31,MEC,-5697.59'
      def parser = CSVParser.parse(csvData, format)
      def record = parser.getRecords().get(0)
      def pageOne = new GeneralLedgerJournalFilterRequest(1, 5, "id", "ASC", null, null, null, null, null, null)

      when:
      inloadMEINVService.inloadCsv(record, UUID.randomUUID())

      then:
      notThrown(Exception)

      when:
      def result = get("/general-ledger/journal${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      result.totalElements == 1
      with(result.elements[0]) {
         id != null
         account.number == 1
         profitCenter.storeNumber == 1
         date == '2020-03-31'
         source.value == 'MEC'
         amount == -5697.59
         message == null
      }
   }

   void "test InloadSUMGLDETService"() {
      given:
      def tstds1 = companies.find { it.datasetCode == "coravt"}
      accountTestDataLoaderService.single(tstds1, 1)
      accountTestDataLoaderService.single(tstds1, 2)
      generalLedgerSourceCodeDataLoaderService.stream(1, tstds1, "MEC").toList()
      def format = CSVFormat.DEFAULT
         .builder()
         .setHeader(*['Data_Set_ID', 'Account_Number', 'Profit_Center_Number', 'JE_Date', 'Source_Code', 'JE_Amount'])
         .build()
      def csvData = 'coravt,2,1,2020-03-31,MEC,-5697.59'
      def parser = CSVParser.parse(csvData, format)
      def record = parser.getRecords().get(0)
      def pageOne = new GeneralLedgerJournalFilterRequest(1, 5, "id", "ASC", null, null, null, null, null, null)

      when:
      inloadSUMGLDETService.inloadCsv(record, UUID.randomUUID())

      then:
      notThrown(Exception)

      when:
      def result = get("/general-ledger/journal${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      result.totalElements == 1
      with(result.elements[0]) {
         id != null
         account.number == 2
         profitCenter.storeNumber == 1
         date == '2020-03-31'
         source.value == 'MEC'
         amount == -5697.59
         message == null
      }
   }


   void "test InloadSUMGLINTVService & InloadSUMGLINTAService"() {
      given: "test InloadSUMGLINTVService"
      def tstds1 = companies.find { it.datasetCode == "coravt"}
      accountTestDataLoaderService.single(tstds1, 1)
      accountTestDataLoaderService.single(tstds1, 2)
      generalLedgerSourceCodeDataLoaderService.stream(1, tstds1, "MEC").toList()

      def format = CSVFormat.DEFAULT
         .builder()
         .setHeader(*['Data_Set_ID', 'Store_Number', 'Date', 'Verify_Successful', 'Error_Amount',
                      'Dep_Cash_Amt', 'Dep_Cash_GL_Acct_Nbr', 'Dep_For_Oth_Str_Amt', 'Dep_For_Oth_Str_GL_Acct_Nbr',
                      'Dep_From_Oth_Str_Amt', 'Dep_From_Oth_Str_GL_Acct_Nbr', 'Dep_CC_In_Str_Amt', 'Dep_CC_In_Str_GL_Acct_Nbr',
                      'Dep_ACH_OLP_Amt', 'Dep_ACH_OLP_GL_Acct_Nbr', 'Dep_CC_OLP_Amt', 'Dep_CC_OLP_GL_Acct_Nbr',
                      'Dep_Debit_Card_Amt', 'Dep_Debit_Card_GL_Acct_Nbr', 'NSF_Return_Check_Amt', 'NSF_Return_Check_GL_Acct_Nbr',
                      'AR_Bad_Check_Amt', 'AR_Bad_Check_GL_Acct_Nbr', 'ICC_Chargeback_Amt', 'ICC_Chargeback_GL_Acct_Nbr',
                      'ACH_Chargeback_Amt', 'ACH_Chargeback_GL_Acct_Nbr'])
         .build()
      def csvData = 'coravt,1,2020-03-31,true,000000000.00,' +
         '000001050.28,1,001016,2,' +
         '151.35,1,3058.63,2,' +
         '0.00,1,548.74,2,' +
         '-151.35,1,4657.65,2,' +
         '0.00,1,3.00,2,' +
         '4.00,1'
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
      def csvData2 = 'coravt,1,2,1,2020-03-31,MEC,-5697.59,DEP_1,'
      def parser2 = CSVParser.parse(csvData2, format2)
      def record2 = parser2.getRecords().get(0)

      when:
      inloadSUMGLINTAService.inloadCsv(record2, UUID.randomUUID())

      then:
      notThrown(Exception)

      when:
      def result = get("general-ledger/deposit?verifiedSuccessful=true")

      then:
      notThrown(HttpClientResponseException)
      with(result) {
         totalElements == 1
         with(elements[0]) {
            verifySuccessful == true
            businessDate == "2020-03-31"
            movedToPendingJournalEntries == false
            store == 1
            storeName == "HOUMA"
            errorAmount == 0
            deposit1Cash == 1050.28
            deposit2PmtForOtherStores == 1016
            deposit3PmtFromOtherStores == 151.35
            deposit4CCInStr == 3058.63
            deposit5ACHOLP == 0
            deposit6CCOLP == 548.74
            deposit7DebitCard == -151.35
            depositTotal == 5673.65
         }
      }
   }

}
