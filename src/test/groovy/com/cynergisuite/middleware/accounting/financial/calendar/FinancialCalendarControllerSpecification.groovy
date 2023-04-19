package com.cynergisuite.middleware.accounting.financial.calendar

import com.cynergisuite.domain.FinancialCalendarValidateDatesFilterRequest
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDTO
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDataLoader
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Unroll

import java.time.LocalDate

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class FinancialCalendarControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/accounting/financial-calendar"

   @Inject FinancialCalendarDataLoaderService financialCalendarDataLoaderService

   void "fetch one" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final financialCalendarEntity = financialCalendarDataLoaderService.single(tstds1)

      when:
      def result = get("$path/${financialCalendarEntity.id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == financialCalendarEntity.id
         period == financialCalendarEntity.period
         periodFrom == financialCalendarEntity.periodFrom.toString()
         periodTo == financialCalendarEntity.periodTo.toString()
         fiscalYear == financialCalendarEntity.fiscalYear
         generalLedgerOpen == financialCalendarEntity.generalLedgerOpen
         accountPayableOpen == financialCalendarEntity.accountPayableOpen

         with(overallPeriod) {
            value == financialCalendarEntity.overallPeriod.value
            abbreviation == financialCalendarEntity.overallPeriod.abbreviation
            description == financialCalendarEntity.overallPeriod.description
         }
      }
   }

   void "fetch all" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final tstds2 = companyFactoryService.forDatasetCode('corrto')
      final financialCalendar = financialCalendarDataLoaderService.stream(3, tstds1).toList()
      financialCalendarDataLoaderService.stream(5, tstds2).toList()
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
               id == financialCalendar[index].id
               period == financialCalendar[index].period
               periodFrom == financialCalendar[index].periodFrom.toString()
               periodTo == financialCalendar[index].periodTo.toString()
               fiscalYear == financialCalendar[index].fiscalYear
               generalLedgerOpen == financialCalendar[index].generalLedgerOpen
               accountPayableOpen == financialCalendar[index].accountPayableOpen

               with(overallPeriod) {
                  value == financialCalendar[index].overallPeriod.value
                  abbreviation == financialCalendar[index].overallPeriod.abbreviation
                  description == financialCalendar[index].overallPeriod.description
               }
            }
         }
      }

      when:
      get("$path$pageTwo")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create one" () {
      given:
      final financialCalendarDTO = financialCalendarDataLoaderService.singleDTO()

      when:
      def result = post(path, financialCalendarDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id != null
         period == financialCalendarDTO.period
         periodFrom == financialCalendarDTO.periodFrom.toString()
         periodTo == financialCalendarDTO.periodTo.toString()
         fiscalYear == financialCalendarDTO.fiscalYear
         generalLedgerOpen == financialCalendarDTO.generalLedgerOpen
         accountPayableOpen == financialCalendarDTO.accountPayableOpen

         with(overallPeriod) {
            value == financialCalendarDTO.overallPeriod.value
            abbreviation == financialCalendarDTO.overallPeriod.abbreviation
            description == financialCalendarDTO.overallPeriod.description
         }
      }
   }

   void "create invalid financial calendar without #nonNullableProp" () {
      given:
      final financialCalendarDTO = financialCalendarDataLoaderService.singleDTO()
      financialCalendarDTO["$nonNullableProp"] = null

      when:
      post(path, financialCalendarDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp                  || errorResponsePath
      'overallPeriod'                  || 'overallPeriod'
      'period'                         || 'period'
      'periodFrom'                     || 'periodFrom'
      'periodTo'                       || 'periodTo'
      'fiscalYear'                     || 'fiscalYear'
      'generalLedgerOpen'              || 'generalLedgerOpen'
      'accountPayableOpen'             || 'accountPayableOpen'
   }

   @Unroll
   void "create invalid financial calendar with non-existing overallPeriod" () {
      given:
      final financialCalendarDTO = financialCalendarDataLoaderService.singleDTO()
      financialCalendarDTO.overallPeriod = new OverallPeriodTypeDTO ('invalid', 'Z', 'Invalid DTO')

      when:
      post(path, financialCalendarDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == 'invalid was unable to be found'
      response.code == 'system.not.found'
   }

   void "update one" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final financialCalendarEntity = financialCalendarDataLoaderService.single(tstds1)
      final financialCalendarDTO = financialCalendarDataLoaderService.singleDTO()
      financialCalendarDTO.id = financialCalendarEntity.id

      when:
      def result = put("$path/${financialCalendarEntity.id}", financialCalendarDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id != null
         period == financialCalendarDTO.period
         periodFrom == financialCalendarDTO.periodFrom.toString()
         periodTo == financialCalendarDTO.periodTo.toString()
         fiscalYear == financialCalendarDTO.fiscalYear
         generalLedgerOpen == financialCalendarDTO.generalLedgerOpen
         accountPayableOpen == financialCalendarDTO.accountPayableOpen

         with(overallPeriod) {
            value == financialCalendarDTO.overallPeriod.value
            abbreviation == financialCalendarDTO.overallPeriod.abbreviation
            description == financialCalendarDTO.overallPeriod.description
         }
      }
   }

   @Unroll
   void "update invalid financial calendar with non-existing overallPeriod" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final financialCalendarEntity = financialCalendarDataLoaderService.single(tstds1)
      final financialCalendarDTO = financialCalendarDataLoaderService.singleDTO()
      financialCalendarDTO.overallPeriod = new OverallPeriodTypeDTO ('invalid', 'Z', 'Invalid DTO')

      when:
      put("$path/${financialCalendarEntity.id}", financialCalendarDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == 'invalid was unable to be found'
      response.code == 'system.not.found'
   }

   void "modify the open ap and gl ranges" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      financialCalendarDataLoaderService.streamFiscalYear(tstds1, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now(), true, true).collect()
      final dateRangeGL = new FinancialCalendarDateRangeDTO(LocalDate.now(), LocalDate.now().plusDays(120))
      final dateRangeAP = new FinancialCalendarDateRangeDTO(LocalDate.now().plusDays(7), LocalDate.now().plusDays(80))

      when:
      put("$path/open-ap", dateRangeAP)

      then:
      notThrown(Exception)

      when:
      put("$path/open-gl", dateRangeGL)

      then:
      notThrown(Exception)
   }

   void "modify the open ap range" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      financialCalendarDataLoaderService.streamFiscalYear(tstds1, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now(), true, true).collect()
      final dateRangeDTO = new FinancialCalendarDateRangeDTO(LocalDate.now(), LocalDate.now().plusDays(80))

      when:
      put("$path/open-ap", dateRangeDTO)

      then:
      notThrown(Exception)
   }

   void "fetch gl open date range" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      financialCalendarDataLoaderService.streamFiscalYear(tstds1, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now(), true, true).collect()
      final dateRangeGL = new FinancialCalendarDateRangeDTO(LocalDate.now(), LocalDate.now().plusMonths(9))
      final dateRangeAP = new FinancialCalendarDateRangeDTO(LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(7))

      when:
      put("$path/open-ap", dateRangeAP)

      then:
      notThrown(Exception)

      when: 'open GL for a date range'
      put("$path/open-gl", dateRangeGL)

      then:
      notThrown(Exception)

      when: 'fetch date range'
      def result = get("$path/gl-dates-open")

      then:
      notThrown(Exception)
      result != null
      result.first == dateRangeGL.periodFrom.toString()
      result.second == dateRangeGL.periodTo.plusMonths(1).minusDays(1).toString()
   }

   void "fetch ap open date range" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      financialCalendarDataLoaderService.streamFiscalYear(tstds1, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now(), true, true).collect()
      final dateRangeGL = new FinancialCalendarDateRangeDTO(LocalDate.now(), LocalDate.now().plusMonths(9))
      final dateRangeAP = new FinancialCalendarDateRangeDTO(LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(7))

      when:
      put("$path/open-ap", dateRangeAP)

      then:
      notThrown(Exception)

      when: 'open GL for a date range'
      put("$path/open-gl", dateRangeGL)

      then:
      notThrown(Exception)

      when: 'fetch date range'
      def result = get("$path/ap-dates-open")

      then:
      notThrown(Exception)
      result != null
      result.first == dateRangeAP.periodFrom.toString()
      result.second == dateRangeAP.periodTo.plusMonths(1).minusDays(1).toString()
   }

   void "try to open gl account starting after the open ap range" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      financialCalendarDataLoaderService.streamFiscalYear(tstds1, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now(), true, true).collect()
      final dateRangeGL = new FinancialCalendarDateRangeDTO(LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(7))
      final dateRangeAP = new FinancialCalendarDateRangeDTO(LocalDate.now(), LocalDate.now().plusMonths(7))

      when:
      put("$path/open-ap", dateRangeAP)

      then:
      notThrown(Exception)

      when:
      put("$path/open-gl", dateRangeGL)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.code[0] == 'cynergi.validation.gl.not.encompassing.ap.window'
      response.message[0] == 'The GL open range chosen must encompass the whole open AP range'
   }

   void "try to open gl account ending before the open ap range" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      financialCalendarDataLoaderService.streamFiscalYear(tstds1, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now(), true, true).collect()
      final dateRangeGL = new FinancialCalendarDateRangeDTO(LocalDate.now(), LocalDate.now().plusMonths(6))
      final dateRangeAP = new FinancialCalendarDateRangeDTO(LocalDate.now(), LocalDate.now().plusMonths(7))

      when:
      put("$path/open-ap", dateRangeAP)

      then:
      notThrown(Exception)

      when:
      put("$path/open-gl", dateRangeGL)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.code[0] == 'cynergi.validation.gl.not.encompassing.ap.window'
      response.message[0] == 'The GL open range chosen must encompass the whole open AP range'
   }

   void "try to open ap starting before the open gl range" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      financialCalendarDataLoaderService.streamFiscalYear(tstds1, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now(), true, true).collect()
      final dateRangeGL = new FinancialCalendarDateRangeDTO(LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(7))
      final dateRangeAP1 = new FinancialCalendarDateRangeDTO(LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(7))
      final dateRangeAP2 = new FinancialCalendarDateRangeDTO(LocalDate.now(), LocalDate.now().plusMonths(6))

      when:
      put("$path/open-ap", dateRangeAP1)

      then:
      notThrown(Exception)

      when:
      put("$path/open-gl", dateRangeGL)

      then:
      notThrown(Exception)

      when:
      put("$path/open-ap", dateRangeAP2)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.code[0] == 'cynergi.validation.ap.outside.of.gl.window'
      response.message[0] == 'The AP open range chosen must be within the open GL range'
   }

   void "try to set the open gl window when no ap window is open" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      financialCalendarDataLoaderService.streamFiscalYear(tstds1, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now().plusMonths(1), false, false).collect()
      final dateRangeGL = new FinancialCalendarDateRangeDTO(LocalDate.now(), LocalDate.now().plusMonths(7))

      when:
      put("$path/open-gl", dateRangeGL)

      then:
      notThrown(Exception)
   }

   void "try to set the open ap window when no gl window is open" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      financialCalendarDataLoaderService.streamFiscalYear(tstds1, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now().plusMonths(1), false, false).collect()
      final dateRangeAP = new FinancialCalendarDateRangeDTO(LocalDate.now(), LocalDate.now().plusMonths(7))

      when:
      put("$path/open-ap", dateRangeAP)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.code[0] == 'cynergi.validation.gl.must.be.open.to.open.ap.range'
      response.message[0] == 'Must set an open GL range before setting an open AP range'
   }

   void "create fiscal calendar year" () {
      given:
      final fiscalYr = FinancialCalendarDataLoader.streamFiscalYearDTO(1).collect()

      when:
      def result = post("$path/year", fiscalYr)

      then:
      notThrown(Exception)
      result != null
      result.eachWithIndex { financialCalendarPeriod, index ->
         with(financialCalendarPeriod) {
            id != null
            period == fiscalYr[index].period
            periodFrom == fiscalYr[index].periodFrom.toString()
            periodTo == fiscalYr[index].periodTo.toString()
            fiscalYear == fiscalYr[index].fiscalYear
            generalLedgerOpen == fiscalYr[index].generalLedgerOpen
            accountPayableOpen == fiscalYr[index].accountPayableOpen

            with(overallPeriod) {
               value == fiscalYr[index].overallPeriod.value
               abbreviation == fiscalYr[index].overallPeriod.abbreviation
               description == fiscalYr[index].overallPeriod.description
            }
         }
      }
   }

   void "create complete financial calendar and fetch fiscal years" () {
      given:
      final beginDate = LocalDate.parse("2021-11-09")
      final financialCalendarDTO = new FinancialCalendarCompleteDTO([year: 2022, periodFrom: beginDate])

      when:
      def result = post("$path/complete", financialCalendarDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         result.eachWithIndex { dto, index ->
            with(dto) {
               id == dto.id
               period == dto.period
               periodFrom == dto.periodFrom.toString()
               periodTo == dto.periodTo.toString()
               fiscalYear == dto.fiscalYear
               generalLedgerOpen == dto.generalLedgerOpen
               accountPayableOpen == dto.accountPayableOpen

               with(overallPeriod) {
                  value == dto.overallPeriod.value
                  abbreviation == dto.overallPeriod.abbreviation
                  description == dto.overallPeriod.description
               }
            }
         }
      }

      when:
      def fiscalYears = get("$path/fiscal-year")

      then:
      notThrown(Exception)
      fiscalYears != null
      fiscalYears.size() == 4
      with(fiscalYears[0]) {
         begin == '2019-11-09'
         end == '2020-11-08'
         fiscalYear == 2020
         with(overallPeriod) {
            value == 'R'
            abbreviation == 'Prior to Prev'
            description == 'Prior to Previous Financial Period'
         }
      }
      with(fiscalYears[2]) {
         begin == '2021-11-09'
         end == '2022-11-08'
         fiscalYear == 2022
         with(overallPeriod) {
            value == 'C'
            abbreviation == 'Curr'
            description == 'Current Financial Period'
         }
      }
   }

   void "starting date not in financial calendar" () {
      given:
      final beginDate = LocalDate.parse("2021-11-09")
      final financialCalendarDTO = new FinancialCalendarCompleteDTO([year: 2022, periodFrom: beginDate])

      final testDate = LocalDate.parse("2000-01-01")
      def filterRequest = new FinancialCalendarValidateDatesFilterRequest([fromDate: testDate])

      when: 'create financial calendar'
      def result = post("$path/complete", financialCalendarDTO)

      then:
      notThrown(Exception)
      result != null

      when: 'test a bad date'
      get("$path/validate-dates$filterRequest")

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$testDate was unable to be found"
      response.code == 'system.not.found'
   }

   void "ending date not in financial calendar" () {
      given:
      final beginDate = LocalDate.parse("2021-11-09")
      final financialCalendarDTO = new FinancialCalendarCompleteDTO([year: 2022, periodFrom: beginDate])

      final testDate = LocalDate.parse("2030-01-01")
      def filterRequest = new FinancialCalendarValidateDatesFilterRequest([fromDate: testDate])

      when: 'create financial calendar'
      def result = post("$path/complete", financialCalendarDTO)

      then:
      notThrown(Exception)
      result != null

      when: 'test a bad date'
      get("$path/validate-dates$filterRequest")

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$testDate was unable to be found"
      response.code == 'system.not.found'
   }

   void "starting and ending dates not in same fiscal year" () {
      given:
      final beginDate = LocalDate.parse("2021-11-09")
      final financialCalendarDTO = new FinancialCalendarCompleteDTO([year: 2022, periodFrom: beginDate])

      final testStartingDate = LocalDate.parse("2021-12-01")
      final testEndingDate = LocalDate.parse("2023-01-01")
      def filterRequest = new FinancialCalendarValidateDatesFilterRequest([fromDate: testStartingDate, thruDate: testEndingDate])

      when: 'create financial calendar'
      def result = post("$path/complete", financialCalendarDTO)

      then:
      notThrown(Exception)
      result != null

      when: 'test bad dates'
      get("$path/validate-dates$filterRequest")

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.path[0] == 'startingDate'
      response.message[0] == "Dates $testStartingDate and $testEndingDate must be in same fiscal year"
      response.code[0] == 'cynergi.validation.dates.must.be.in.same.fiscal.year'
   }

   void "modify the open ap and gl ranges at only 1 company when 2 exist" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final tstds2 = companyFactoryService.forDatasetCode('corrto')
      financialCalendarDataLoaderService.streamFiscalYear(tstds1, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now(), true, true).collect()
      financialCalendarDataLoaderService.streamFiscalYear(tstds2, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now(), true, true).collect()
      final dateRangeGL = new FinancialCalendarDateRangeDTO(LocalDate.now(), LocalDate.now().plusDays(120))
      final dateRangeAP = new FinancialCalendarDateRangeDTO(LocalDate.now().plusDays(7), LocalDate.now().plusDays(80))

      when:
      put("$path/open-ap", dateRangeAP)

      then:
      notThrown(Exception)

      when:
      put("$path/open-gl", dateRangeGL)

      then:
      notThrown(Exception)
   }
}
