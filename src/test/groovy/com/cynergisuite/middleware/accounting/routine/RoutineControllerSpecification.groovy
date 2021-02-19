package com.cynergisuite.middleware.accounting.routine

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.routine.type.OverallPeriodTypeDTO
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import javax.inject.Inject
import java.time.LocalDate

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class RoutineControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/accounting/routine"

   @Inject RoutineDataLoaderService routineDataLoaderService

   void "fetch one" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final routine = routineDataLoaderService.single(tstds1)

      when:
      def result = get("$path/${routine.id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == routine.id
         period == routine.period
         periodFrom == routine.periodFrom.toString()
         periodTo == routine.periodTo.toString()
         fiscalYear == routine.fiscalYear
         generalLedgerOpen == routine.generalLedgerOpen
         accountPayableOpen == routine.accountPayableOpen

         with(overallPeriod) {
            value == routine.overallPeriod.value
            abbreviation == routine.overallPeriod.abbreviation
            description == routine.overallPeriod.description
         }
      }
   }

   void "fetch all" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      final routines = routineDataLoaderService.stream(3, tstds1).toList()
      routineDataLoaderService.stream(5, tstds2).toList()
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
               id == routines[index].id
               period == routines[index].period
               periodFrom == routines[index].periodFrom.toString()
               periodTo == routines[index].periodTo.toString()
               fiscalYear == routines[index].fiscalYear
               generalLedgerOpen == routines[index].generalLedgerOpen
               accountPayableOpen == routines[index].accountPayableOpen

               with(overallPeriod) {
                  value == routines[index].overallPeriod.value
                  abbreviation == routines[index].overallPeriod.abbreviation
                  description == routines[index].overallPeriod.description
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
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final routine = routineDataLoaderService.singleDTO(tstds1)

      when:
      def result = post(path, routine)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id > 0
         period == routine.period
         periodFrom == routine.periodFrom.toString()
         periodTo == routine.periodTo.toString()
         fiscalYear == routine.fiscalYear
         generalLedgerOpen == routine.generalLedgerOpen
         accountPayableOpen == routine.accountPayableOpen

         with(overallPeriod) {
            value == routine.overallPeriod.value
            abbreviation == routine.overallPeriod.abbreviation
            description == routine.overallPeriod.description
         }
      }
   }

   void "create invalid Routine without #nonNullableProp" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final routineDTO = routineDataLoaderService.singleDTO(tstds1)
      routineDTO["$nonNullableProp"] = null

      when:
      post(path, routineDTO)

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
   void "create invalid Routine with non-existing overallPeriod" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final routineDTO = routineDataLoaderService.singleDTO(tstds1)
      routineDTO.overallPeriod = new OverallPeriodTypeDTO ('invalid', 'Z', 'Invalid DTO')

      when:
      post(path, routineDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == 'invalid was unable to be found'
   }

   void "update one" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final routineEntity = routineDataLoaderService.single(tstds1)
      final routine = routineDataLoaderService.singleDTO(tstds1)
      routine.id = routineEntity.id

      when:
      def result = put("$path/${routineEntity.id}", routine)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id > 0
         period == routine.period
         periodFrom == routine.periodFrom.toString()
         periodTo == routine.periodTo.toString()
         fiscalYear == routine.fiscalYear
         generalLedgerOpen == routine.generalLedgerOpen
         accountPayableOpen == routine.accountPayableOpen

         with(overallPeriod) {
            value == routine.overallPeriod.value
            abbreviation == routine.overallPeriod.abbreviation
            description == routine.overallPeriod.description
         }
      }
   }

   @Unroll
   void "update invalid Routine with non-existing overallPeriod" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final routineEntity = routineDataLoaderService.single(tstds1)
      final routineDTO = routineDataLoaderService.singleDTO(tstds1)
      routineDTO.overallPeriod = new OverallPeriodTypeDTO ('invalid', 'Z', 'Invalid DTO')

      when:
      put("$path/${routineEntity.id}", routineDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == 'invalid was unable to be found'
   }

   void "open gl account" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final routines = RoutineDataLoader.streamDTO(8).toList()
      final firstRoutine = routines[0]
      //final dateRangeDTO = new RoutineDateRangeDTO(firstRoutine.periodFrom, firstRoutine.periodTo)
      final dateRangeDTO = new RoutineDateRangeDTO(LocalDate.now(), LocalDate.of(2021, 4, 18))

      when:
      put("$path/open-gl", dateRangeDTO)

      then:
      notThrown(Exception)

      when:
      def result = get("$path/${firstRoutine.id}")

      then:
      result != firstRoutine
   }

   void "create fiscal calendar" () {

   }
}
