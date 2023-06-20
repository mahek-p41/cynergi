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
}
