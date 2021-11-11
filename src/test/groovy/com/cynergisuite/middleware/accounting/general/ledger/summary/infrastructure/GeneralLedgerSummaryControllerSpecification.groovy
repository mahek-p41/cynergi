package com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.general.ledger.summary.GeneralLedgerSummaryDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class GeneralLedgerSummaryControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/general-ledger/summary"

   @Inject GeneralLedgerSummaryDataLoaderService dataLoaderService
   @Inject AccountTestDataLoaderService accountDataLoaderService

   void "fetch one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final glSummary = dataLoaderService.single(company, acct, store)

      when:
      def result = get("$path/${glSummary.id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == glSummary.id
         account.id == glSummary.account.id
         profitCenter.id == glSummary.profitCenter.myId()

         with(overallPeriod) {
            value == glSummary.overallPeriod.value
            description == glSummary.overallPeriod.description
         }

         netActivityPeriod1 == glSummary.netActivityPeriod1
         netActivityPeriod2 == glSummary.netActivityPeriod2
         netActivityPeriod3 == glSummary.netActivityPeriod3
         netActivityPeriod4 == glSummary.netActivityPeriod4
         netActivityPeriod5 == glSummary.netActivityPeriod5
         netActivityPeriod6 == glSummary.netActivityPeriod6
         netActivityPeriod7 == glSummary.netActivityPeriod7
         netActivityPeriod8 == glSummary.netActivityPeriod8
         netActivityPeriod9 == glSummary.netActivityPeriod9
         netActivityPeriod10 == glSummary.netActivityPeriod10
         netActivityPeriod11 == glSummary.netActivityPeriod11
         netActivityPeriod12 == glSummary.netActivityPeriod12
         beginningBalance == glSummary.beginningBalance
         closingBalance == glSummary.closingBalance
      }
   }

   void "fetch one not found" () {
      given:
      final nonExistentId = UUID.randomUUID()

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
      final company = companyFactoryService.forDatasetCode('tstds1')
      final accountList = accountDataLoaderService.stream(12, company).toList()
      final store = storeFactoryService.store(3, company)
      final glSummary1 = dataLoaderService.single(company, accountList[0] as AccountEntity, store)
      final glSummary2 = dataLoaderService.single(company, accountList[1] as AccountEntity, store)
      final glSummary3 = dataLoaderService.single(company, accountList[2] as AccountEntity, store)
      final glSummary4 = dataLoaderService.single(company, accountList[3] as AccountEntity, store)
      final glSummary5 = dataLoaderService.single(company, accountList[4] as AccountEntity, store)
      final glSummary6 = dataLoaderService.single(company, accountList[5] as AccountEntity, store)
      final glSummary7 = dataLoaderService.single(company, accountList[6] as AccountEntity, store)
      final glSummary8 = dataLoaderService.single(company, accountList[7] as AccountEntity, store)
      final glSummary9 = dataLoaderService.single(company, accountList[8] as AccountEntity, store)
      final glSummary10 = dataLoaderService.single(company, accountList[9] as AccountEntity, store)
      final glSummary11 = dataLoaderService.single(company, accountList[10] as AccountEntity, store)
      final glSummary12 = dataLoaderService.single(company, accountList[11] as AccountEntity, store)
      def pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      def pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      def pageLast = new StandardPageRequest(3, 5, "id", "ASC")
      def pageFour = new StandardPageRequest(4, 5, "id", "ASC")
      def firstPage = [glSummary1, glSummary2, glSummary3, glSummary4, glSummary5]
      def secondPage = [glSummary6, glSummary7, glSummary8, glSummary9, glSummary10]
      def lastPage = [glSummary11, glSummary12]

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
            id == firstPage[index].id
            account.id == firstPage[index].account.id
            profitCenter.id == firstPage[index].profitCenter.myId()

            with(overallPeriod) {
               value == firstPage[index].overallPeriod.value
               description == firstPage[index].overallPeriod.description
            }

            netActivityPeriod1 == firstPage[index].netActivityPeriod1
            netActivityPeriod2 == firstPage[index].netActivityPeriod2
            netActivityPeriod3 == firstPage[index].netActivityPeriod3
            netActivityPeriod4 == firstPage[index].netActivityPeriod4
            netActivityPeriod5 == firstPage[index].netActivityPeriod5
            netActivityPeriod6 == firstPage[index].netActivityPeriod6
            netActivityPeriod7 == firstPage[index].netActivityPeriod7
            netActivityPeriod8 == firstPage[index].netActivityPeriod8
            netActivityPeriod9 == firstPage[index].netActivityPeriod9
            netActivityPeriod10 == firstPage[index].netActivityPeriod10
            netActivityPeriod11 == firstPage[index].netActivityPeriod11
            netActivityPeriod12 == firstPage[index].netActivityPeriod12
            beginningBalance == firstPage[index].beginningBalance
            closingBalance == firstPage[index].closingBalance
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
            id == secondPage[index].id
            account.id == secondPage[index].account.id
            profitCenter.id == secondPage[index].profitCenter.myId()

            with(overallPeriod) {
               value == secondPage[index].overallPeriod.value
               description == secondPage[index].overallPeriod.description
            }

            netActivityPeriod1 == secondPage[index].netActivityPeriod1
            netActivityPeriod2 == secondPage[index].netActivityPeriod2
            netActivityPeriod3 == secondPage[index].netActivityPeriod3
            netActivityPeriod4 == secondPage[index].netActivityPeriod4
            netActivityPeriod5 == secondPage[index].netActivityPeriod5
            netActivityPeriod6 == secondPage[index].netActivityPeriod6
            netActivityPeriod7 == secondPage[index].netActivityPeriod7
            netActivityPeriod8 == secondPage[index].netActivityPeriod8
            netActivityPeriod9 == secondPage[index].netActivityPeriod9
            netActivityPeriod10 == secondPage[index].netActivityPeriod10
            netActivityPeriod11 == secondPage[index].netActivityPeriod11
            netActivityPeriod12 == secondPage[index].netActivityPeriod12
            beginningBalance == secondPage[index].beginningBalance
            closingBalance == secondPage[index].closingBalance
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
            id == lastPage[index].id
            account.id == lastPage[index].account.id
            profitCenter.id == lastPage[index].profitCenter.myId()

            with(overallPeriod) {
               value == lastPage[index].overallPeriod.value
               description == lastPage[index].overallPeriod.description
            }

            netActivityPeriod1 == lastPage[index].netActivityPeriod1
            netActivityPeriod2 == lastPage[index].netActivityPeriod2
            netActivityPeriod3 == lastPage[index].netActivityPeriod3
            netActivityPeriod4 == lastPage[index].netActivityPeriod4
            netActivityPeriod5 == lastPage[index].netActivityPeriod5
            netActivityPeriod6 == lastPage[index].netActivityPeriod6
            netActivityPeriod7 == lastPage[index].netActivityPeriod7
            netActivityPeriod8 == lastPage[index].netActivityPeriod8
            netActivityPeriod9 == lastPage[index].netActivityPeriod9
            netActivityPeriod10 == lastPage[index].netActivityPeriod10
            netActivityPeriod11 == lastPage[index].netActivityPeriod11
            netActivityPeriod12 == lastPage[index].netActivityPeriod12
            beginningBalance == lastPage[index].beginningBalance
            closingBalance == lastPage[index].closingBalance
         }
      }

      when:
      get("$path/${pageFour}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final glSummaryDTO = dataLoaderService.singleDTO(new SimpleIdentifiableDTO(acct), new SimpleLegacyIdentifiableDTO(store.myId()))

      when:
      def result = post("$path/", glSummaryDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id != null
         account.id == glSummaryDTO.account.id
         profitCenter.id == glSummaryDTO.profitCenter.myId()

         with(overallPeriod) {
            value == glSummaryDTO.overallPeriod.value
            description == glSummaryDTO.overallPeriod.description
         }

         netActivityPeriod1 == glSummaryDTO.netActivityPeriod1
         netActivityPeriod2 == glSummaryDTO.netActivityPeriod2
         netActivityPeriod3 == glSummaryDTO.netActivityPeriod3
         netActivityPeriod4 == glSummaryDTO.netActivityPeriod4
         netActivityPeriod5 == glSummaryDTO.netActivityPeriod5
         netActivityPeriod6 == glSummaryDTO.netActivityPeriod6
         netActivityPeriod7 == glSummaryDTO.netActivityPeriod7
         netActivityPeriod8 == glSummaryDTO.netActivityPeriod8
         netActivityPeriod9 == glSummaryDTO.netActivityPeriod9
         netActivityPeriod10 == glSummaryDTO.netActivityPeriod10
         netActivityPeriod11 == glSummaryDTO.netActivityPeriod11
         netActivityPeriod12 == glSummaryDTO.netActivityPeriod12
         beginningBalance == glSummaryDTO.beginningBalance
         closingBalance == glSummaryDTO.closingBalance
      }
   }

   void "create valid general ledger summary without nullable properties" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final glSummaryDTO = dataLoaderService.singleDTO(new SimpleIdentifiableDTO(acct), new SimpleLegacyIdentifiableDTO(store.myId()))
      glSummaryDTO.netActivityPeriod1 = null
      glSummaryDTO.netActivityPeriod2 = null
      glSummaryDTO.netActivityPeriod3 = null
      glSummaryDTO.netActivityPeriod4 = null
      glSummaryDTO.netActivityPeriod5 = null
      glSummaryDTO.netActivityPeriod6 = null
      glSummaryDTO.netActivityPeriod7 = null
      glSummaryDTO.netActivityPeriod8 = null
      glSummaryDTO.netActivityPeriod9 = null
      glSummaryDTO.netActivityPeriod10 = null
      glSummaryDTO.netActivityPeriod11 = null
      glSummaryDTO.netActivityPeriod12 = null
      glSummaryDTO.beginningBalance = null
      glSummaryDTO.closingBalance = null

      when:
      def result = post("$path/", glSummaryDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id != null
         account.id == glSummaryDTO.account.id
         profitCenter.id == glSummaryDTO.profitCenter.myId()

         with(overallPeriod) {
            value == glSummaryDTO.overallPeriod.value
            description == glSummaryDTO.overallPeriod.description
         }

         netActivityPeriod1 == glSummaryDTO.netActivityPeriod1
         netActivityPeriod2 == glSummaryDTO.netActivityPeriod2
         netActivityPeriod3 == glSummaryDTO.netActivityPeriod3
         netActivityPeriod4 == glSummaryDTO.netActivityPeriod4
         netActivityPeriod5 == glSummaryDTO.netActivityPeriod5
         netActivityPeriod6 == glSummaryDTO.netActivityPeriod6
         netActivityPeriod7 == glSummaryDTO.netActivityPeriod7
         netActivityPeriod8 == glSummaryDTO.netActivityPeriod8
         netActivityPeriod9 == glSummaryDTO.netActivityPeriod9
         netActivityPeriod10 == glSummaryDTO.netActivityPeriod10
         netActivityPeriod11 == glSummaryDTO.netActivityPeriod11
         netActivityPeriod12 == glSummaryDTO.netActivityPeriod12
         beginningBalance == glSummaryDTO.beginningBalance
         closingBalance == glSummaryDTO.closingBalance
      }
   }

   @Unroll
   void "create invalid general ledger summary without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final glSummaryDTO = dataLoaderService.singleDTO(new SimpleIdentifiableDTO(acct), new SimpleLegacyIdentifiableDTO(store.myId()))
      glSummaryDTO["$nonNullableProp"] = null

      when:
      post("$path/", glSummaryDTO)

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
      'overallPeriod'     || 'overallPeriod'
      'profitCenter'      || 'profitCenter'
   }

   void "create invalid general ledger summary with duplicate entry" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final acct1 = accountDataLoaderService.single(company)
      final acct2 = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(1, company)
      final existingGLSummary = dataLoaderService.single(company, acct1, store)
      final newGLSummary = dataLoaderService.singleDTO(new SimpleIdentifiableDTO(acct2), new SimpleLegacyIdentifiableDTO(store.myId()))
      newGLSummary.account.id = existingGLSummary.account.id
      newGLSummary.overallPeriod.value = existingGLSummary.overallPeriod.value

      when:
      post("$path/", newGLSummary)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'id'
      response[0].message == " already exists"
      response[0].code == 'cynergi.validation.duplicate'
   }

   void "update one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final existingGLSummary = dataLoaderService.single(company, acct, store)
      final updatedGLSummary = dataLoaderService.singleDTO(new SimpleIdentifiableDTO(acct), new SimpleLegacyIdentifiableDTO(store.myId()))
      updatedGLSummary.id = existingGLSummary.id

      when:
      def result = put("$path/${existingGLSummary.id}", updatedGLSummary)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == updatedGLSummary.id
         account.id == updatedGLSummary.account.id
         profitCenter.id == updatedGLSummary.profitCenter.myId()

         with(overallPeriod) {
            value == updatedGLSummary.overallPeriod.value
            description == updatedGLSummary.overallPeriod.description
         }

         netActivityPeriod1 == updatedGLSummary.netActivityPeriod1
         netActivityPeriod2 == updatedGLSummary.netActivityPeriod2
         netActivityPeriod3 == updatedGLSummary.netActivityPeriod3
         netActivityPeriod4 == updatedGLSummary.netActivityPeriod4
         netActivityPeriod5 == updatedGLSummary.netActivityPeriod5
         netActivityPeriod6 == updatedGLSummary.netActivityPeriod6
         netActivityPeriod7 == updatedGLSummary.netActivityPeriod7
         netActivityPeriod8 == updatedGLSummary.netActivityPeriod8
         netActivityPeriod9 == updatedGLSummary.netActivityPeriod9
         netActivityPeriod10 == updatedGLSummary.netActivityPeriod10
         netActivityPeriod11 == updatedGLSummary.netActivityPeriod11
         netActivityPeriod12 == updatedGLSummary.netActivityPeriod12
         beginningBalance == updatedGLSummary.beginningBalance
         closingBalance == updatedGLSummary.closingBalance
      }
   }

   void "update valid general ledger summary without nullable properties" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final existingGLSummary = dataLoaderService.single(company, acct, store)
      final updatedGLSummary = dataLoaderService.singleDTO(new SimpleIdentifiableDTO(acct), new SimpleLegacyIdentifiableDTO(store.myId()))
      updatedGLSummary.id = existingGLSummary.id
      updatedGLSummary.netActivityPeriod1 = null
      updatedGLSummary.netActivityPeriod2 = null
      updatedGLSummary.netActivityPeriod3 = null
      updatedGLSummary.netActivityPeriod4 = null
      updatedGLSummary.netActivityPeriod5 = null
      updatedGLSummary.netActivityPeriod6 = null
      updatedGLSummary.netActivityPeriod7 = null
      updatedGLSummary.netActivityPeriod8 = null
      updatedGLSummary.netActivityPeriod9 = null
      updatedGLSummary.netActivityPeriod10 = null
      updatedGLSummary.netActivityPeriod11 = null
      updatedGLSummary.netActivityPeriod12 = null
      updatedGLSummary.beginningBalance = null
      updatedGLSummary.closingBalance = null

      when:
      def result = put("$path/${existingGLSummary.id}", updatedGLSummary)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == updatedGLSummary.id
         account.id == updatedGLSummary.account.id
         profitCenter.id == updatedGLSummary.profitCenter.myId()

         with(overallPeriod) {
            value == updatedGLSummary.overallPeriod.value
            description == updatedGLSummary.overallPeriod.description
         }

         netActivityPeriod1 == updatedGLSummary.netActivityPeriod1
         netActivityPeriod2 == updatedGLSummary.netActivityPeriod2
         netActivityPeriod3 == updatedGLSummary.netActivityPeriod3
         netActivityPeriod4 == updatedGLSummary.netActivityPeriod4
         netActivityPeriod5 == updatedGLSummary.netActivityPeriod5
         netActivityPeriod6 == updatedGLSummary.netActivityPeriod6
         netActivityPeriod7 == updatedGLSummary.netActivityPeriod7
         netActivityPeriod8 == updatedGLSummary.netActivityPeriod8
         netActivityPeriod9 == updatedGLSummary.netActivityPeriod9
         netActivityPeriod10 == updatedGLSummary.netActivityPeriod10
         netActivityPeriod11 == updatedGLSummary.netActivityPeriod11
         netActivityPeriod12 == updatedGLSummary.netActivityPeriod12
         beginningBalance == updatedGLSummary.beginningBalance
         closingBalance == updatedGLSummary.closingBalance
      }
   }

   void "update valid general ledger summary with duplicate entry" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(1, company)
      final glSummaryToUpdate = dataLoaderService.single(company, acct, store)
      final updatedGLSummary = dataLoaderService.singleDTO(new SimpleIdentifiableDTO(acct), new SimpleLegacyIdentifiableDTO(store.myId()))
      updatedGLSummary.id = glSummaryToUpdate.id
      updatedGLSummary.overallPeriod.value = glSummaryToUpdate.overallPeriod.value
      updatedGLSummary.overallPeriod.description = glSummaryToUpdate.overallPeriod.description

      when:
      def result = put("$path/${glSummaryToUpdate.id}", updatedGLSummary)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == updatedGLSummary.id
         account.id == updatedGLSummary.account.id
         profitCenter.id == updatedGLSummary.profitCenter.myId()

         with(overallPeriod) {
            value == updatedGLSummary.overallPeriod.value
            description == updatedGLSummary.overallPeriod.description
         }

         netActivityPeriod1 == updatedGLSummary.netActivityPeriod1
         netActivityPeriod2 == updatedGLSummary.netActivityPeriod2
         netActivityPeriod3 == updatedGLSummary.netActivityPeriod3
         netActivityPeriod4 == updatedGLSummary.netActivityPeriod4
         netActivityPeriod5 == updatedGLSummary.netActivityPeriod5
         netActivityPeriod6 == updatedGLSummary.netActivityPeriod6
         netActivityPeriod7 == updatedGLSummary.netActivityPeriod7
         netActivityPeriod8 == updatedGLSummary.netActivityPeriod8
         netActivityPeriod9 == updatedGLSummary.netActivityPeriod9
         netActivityPeriod10 == updatedGLSummary.netActivityPeriod10
         netActivityPeriod11 == updatedGLSummary.netActivityPeriod11
         netActivityPeriod12 == updatedGLSummary.netActivityPeriod12
         beginningBalance == updatedGLSummary.beginningBalance
         closingBalance == updatedGLSummary.closingBalance
      }
   }

   @Unroll
   void "update invalid general ledger summary without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final existingGLSummary = dataLoaderService.single(company, acct, store)
      final updatedGLSummary = dataLoaderService.singleDTO(new SimpleIdentifiableDTO(acct), new SimpleLegacyIdentifiableDTO(store.myId()))
      updatedGLSummary.id = existingGLSummary.id
      updatedGLSummary["$nonNullableProp"] = null

      when:
      put("$path/${existingGLSummary.id}", updatedGLSummary)

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
      'overallPeriod'     || 'overallPeriod'
      'profitCenter'      || 'profitCenter'
   }

   void "update invalid general ledger summary with duplicate entry" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final acct = accountDataLoaderService.single(company)
      final store1 = storeFactoryService.store(1, company)
      final store3 = storeFactoryService.store(3, company)
      final glSummaryToUpdate = dataLoaderService.single(company, acct, store1)
      final duplicateGlSummary = dataLoaderService.single(company, acct, store3)
      final updatedGLSummary = dataLoaderService.singleDTO(new SimpleIdentifiableDTO(acct), new SimpleLegacyIdentifiableDTO(store3.myId()))
      updatedGLSummary.id = glSummaryToUpdate.id
      updatedGLSummary.overallPeriod.value = duplicateGlSummary.overallPeriod.value

      when:
      put("$path/${glSummaryToUpdate.id}", updatedGLSummary)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'id'
      response[0].message == "${updatedGLSummary.id} already exists"
      response[0].code == 'cynergi.validation.duplicate'
   }
}
