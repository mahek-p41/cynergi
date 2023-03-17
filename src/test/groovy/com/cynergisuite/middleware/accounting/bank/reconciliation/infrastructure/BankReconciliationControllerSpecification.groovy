package com.cynergisuite.middleware.accounting.bank.reconciliation.infrastructure

import com.cynergisuite.domain.BankReconClearingFilterRequest
import com.cynergisuite.domain.BankReconciliationTransactionsFilterRequest
import com.cynergisuite.domain.ReconcileBankAccountFilterRequest
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.bank.BankFactoryService
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationDataLoaderService
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationType
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDataLoader
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Unroll

import java.time.LocalDate

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class BankReconciliationControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/accounting/bank-recon'
   @Inject AccountTestDataLoaderService accountDataLoaderService
   @Inject BankFactoryService bankFactoryService
   @Inject BankReconciliationDataLoaderService dataLoaderService


   void "fetch one bank reconciliation by id" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      final bankRecon = dataLoaderService.single(tstds1, bankIn, LocalDate.now(), null)

      when:
      def result = get("$path/${bankRecon.id}")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == bankRecon.id
         bank.id == bankRecon.bank.id

         with(type) {
            value == bankRecon.type.value
            description == bankRecon.type.description
         }

         date == bankRecon.date.toString()
         clearedDate == bankRecon.clearedDate
         amount == bankRecon.amount
         description == bankRecon.description
         document == bankRecon.document
      }
   }

   void "fetch one bank reconciliation by id not found" () {
      given:
      final nonExistentId = UUID.randomUUID()

      when:
      get("$path/$nonExistentId")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$nonExistentId was unable to be found"
      response.code == 'system.not.found'
   }

   void "fetch all" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      dataLoaderService.stream(5, companyFactoryService.forDatasetCode('corrto'), bankIn, LocalDate.now(), null)
      final bankRecons = dataLoaderService.stream(12, tstds1, bankIn, LocalDate.now(), null).toList()
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      final pageLast = new StandardPageRequest(3, 5, "id", "ASC")
      final pageFour = new StandardPageRequest(4, 5, "id", "ASC")
      final firstPageBankRecon = bankRecons[0..4]
      final secondPageBankRecon = bankRecons[5..9]
      final lastPageBankRecon = bankRecons[10,11]

      when:
      def pageOneResult = get("$path${pageOne}")

      then:
      pageOneResult.requested.with { new StandardPageRequest(it) } == pageOne
      pageOneResult.totalElements == 12
      pageOneResult.totalPages == 3
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements.size() == 5
      pageOneResult.elements.eachWithIndex { it, index ->
         with (it) {
            id == firstPageBankRecon[index].id
            bank.id == firstPageBankRecon[index].bank.id

            with(type) {
               value == firstPageBankRecon[index].type.value
               description == firstPageBankRecon[index].type.description
            }

            date == firstPageBankRecon[index].date.toString()
            clearedDate == firstPageBankRecon[index].clearedDate
            amount == firstPageBankRecon[index].amount
            description == firstPageBankRecon[index].description
            document == firstPageBankRecon[index].document
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
      pageTwoResult.elements.eachWithIndex { it, index ->
         with(it) {
            id == secondPageBankRecon[index].id
            bank.id == secondPageBankRecon[index].bank.id

            with(type) {
               value == secondPageBankRecon[index].type.value
               description == secondPageBankRecon[index].type.description
            }

            date == secondPageBankRecon[index].date.toString()
            clearedDate == secondPageBankRecon[index].clearedDate
            amount == secondPageBankRecon[index].amount
            description == secondPageBankRecon[index].description
            document == secondPageBankRecon[index].document
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
      pageLastResult.elements.eachWithIndex { it, index ->
         with(it) {
            id == lastPageBankRecon[index].id
            bank.id == lastPageBankRecon[index].bank.id

            with(type) {
               value == lastPageBankRecon[index].type.value
               description == lastPageBankRecon[index].type.description
            }

            date == lastPageBankRecon[index].date.toString()
            clearedDate == lastPageBankRecon[index].clearedDate
            amount == lastPageBankRecon[index].amount
            description == lastPageBankRecon[index].description
            document == lastPageBankRecon[index].document
         }
      }

      when:
      get("$path/${pageFour}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create valid bank reconciliation"() {
      given:
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      final bankRecon = dataLoaderService.singleDTO(bankIn, LocalDate.now(), LocalDate.now())

      when:
      def result = post("$path/", bankRecon)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id != null
         bank.id == bankRecon.bank.id

         with(type) {
            value == bankRecon.type.value
            description == bankRecon.type.description
         }

         date == bankRecon.date.toString()
         clearedDate == bankRecon.clearedDate.toString()
         amount == bankRecon.amount
         description == bankRecon.description
         document == bankRecon.document
      }
   }

   void "create valid bank reconciliation with null clearedDate and document" () {
      given:
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      final bankRecon = dataLoaderService.singleDTO(bankIn, LocalDate.now(), LocalDate.now())
      bankRecon.type = new BankReconciliationTypeDTO(BankReconciliationTypeDataLoader.predefined().findAll {it.value != "V" }.random() as BankReconciliationType)
      bankRecon.clearedDate = null
      bankRecon.document = null

      when:
      def result = post("$path/", bankRecon)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id != null
         bank.id == bankRecon.bank.id

         with(type) {
            value == bankRecon.type.value
            description == bankRecon.type.description
         }

         date == bankRecon.date.toString()
         clearedDate == bankRecon.clearedDate
         amount == bankRecon.amount
         description == bankRecon.description
         document == bankRecon.document
      }
   }

   @Unroll
   void "create invalid bank reconciliation without #nonNullableProp" () {
      given:
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      final bankRecon = dataLoaderService.singleDTO(bankIn, LocalDate.now(), null)
      bankRecon.type = new BankReconciliationTypeDTO(BankReconciliationTypeDataLoader.predefined().findAll {it.value != "V" }.random() as BankReconciliationType)
      bankRecon["$nonNullableProp"] = null

      when:
      post("$path/", bankRecon)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'
      response[0].code == 'javax.validation.constraints.NotNull.message'

      where:
      nonNullableProp || errorResponsePath
      'amount'        || 'amount'
      'bank'          || 'bank'
      'date'          || 'date'
      'description'   || 'description'
      'type'          || 'type'
   }

   void "create invalid bank reconciliation with non-existing bank id" () {
      given:
      final nonExistentBankId = UUID.randomUUID()
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      final bankRecon = dataLoaderService.singleDTO(bankIn, LocalDate.now(), null)
      bankRecon.type = new BankReconciliationTypeDTO(BankReconciliationTypeDataLoader.predefined().findAll {it.value != "V" }.random() as BankReconciliationType)
      bankRecon.bank.id = nonExistentBankId

      when:
      post("$path/", bankRecon)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "bank.id"
      response[0].message == "$nonExistentBankId was unable to be found"
      response[0].code == 'system.not.found'
   }

   void "update valid bank reconciliation"() {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      final existingBankRecon = dataLoaderService.single(tstds1, bankIn, LocalDate.now(), LocalDate.now())
      final updatedBankReconDTO = dataLoaderService.singleDTO(bankIn, LocalDate.now(), LocalDate.now())

      when:
      def result = put("$path/$existingBankRecon.id", updatedBankReconDTO)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id != null
         bank.id == updatedBankReconDTO.bank.id

         with(type) {
            value == updatedBankReconDTO.type.value
            description == updatedBankReconDTO.type.description
         }

         date == updatedBankReconDTO.date.toString()
         clearedDate == updatedBankReconDTO.clearedDate.toString()
         amount == updatedBankReconDTO.amount
         description == updatedBankReconDTO.description
         document == updatedBankReconDTO.document
      }
   }

   void "update valid bank reconciliation with null clearedDate and document"() {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      final existingBankRecon = dataLoaderService.single(tstds1, bankIn, LocalDate.now(), LocalDate.now())
      final updatedBankReconDTO = dataLoaderService.singleDTO(bankIn, LocalDate.now(), LocalDate.now())
      updatedBankReconDTO.type = new BankReconciliationTypeDTO(BankReconciliationTypeDataLoader.predefined().findAll {it.value != "V" }.random() as BankReconciliationType)
      updatedBankReconDTO.clearedDate = null
      updatedBankReconDTO.document = null

      when:
      def result = put("$path/$existingBankRecon.id", updatedBankReconDTO)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == existingBankRecon.id
         bank.id == updatedBankReconDTO.bank.id

         with(type) {
            value == updatedBankReconDTO.type.value
            description == updatedBankReconDTO.type.description
         }

         date == updatedBankReconDTO.date.toString()
         clearedDate == updatedBankReconDTO.clearedDate
         amount == updatedBankReconDTO.amount
         description == updatedBankReconDTO.description
         document == updatedBankReconDTO.document
      }
   }

   void "update invalid bank reconciliation without non-nullable properties" () {
      given:
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      final existingBankRecon = dataLoaderService.single(tstds1, bankIn, LocalDate.now(), LocalDate.now())
      final updatedBankReconDTO = dataLoaderService.singleDTO(bankIn, LocalDate.now(), LocalDate.now())
      updatedBankReconDTO.amount = null
      updatedBankReconDTO.bank = null
      updatedBankReconDTO.date = null
      updatedBankReconDTO.description = null
      updatedBankReconDTO.type = null

      when:
      put("$path/$existingBankRecon.id", updatedBankReconDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson().collect().sort { a,b -> a.path <=> b.path }
      response.size() == 5
      response[0].path == 'amount'
      response[1].path == 'bank'
      response[2].path == 'date'
      response[3].path == 'description'
      response[4].path == 'type'
      response.collect { it.message } as Set == ['Is required'] as Set
   }

   void "update invalid bank reconciliation with non-existing bank id" () {
      given:
      final nonExistentBankId = UUID.randomUUID()
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      final existingBankRecon = dataLoaderService.single(tstds1, bankIn, LocalDate.now(), LocalDate.now())
      final updatedBankReconDTO = dataLoaderService.singleDTO(bankIn, LocalDate.now(), LocalDate.now())
      updatedBankReconDTO.bank.id = nonExistentBankId

      when:
      put("$path/$existingBankRecon.id", updatedBankReconDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "bank.id"
      response[0].message == "$nonExistentBankId was unable to be found"
      response[0].code == 'system.not.found'
   }

   void "fetch list of bank reconciliation clearing status" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      dataLoaderService.stream(5, companyFactoryService.forDatasetCode('corrto'), bankIn, LocalDate.now(), null)
      final bankRecons = dataLoaderService.stream(12, tstds1, bankIn, LocalDate.now(), null).toList()

      def filterRequest = new BankReconClearingFilterRequest()

      filterRequest['bank'] = bankIn.number
      filterRequest['fromTransactionDate'] = LocalDate.now().minusDays(5)
      filterRequest['thruTransactionDate'] = LocalDate.now().plusDays(5)


      when:
      def result = get("$path/clearing${filterRequest}")

      then:
      notThrown(Exception)
      result.size() == 12
   }

   void "update list of bank reconciliation cleared status" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      dataLoaderService.stream(5, companyFactoryService.forDatasetCode('corrto'), bankIn, LocalDate.now(), null)
      final bankRecons = dataLoaderService.stream(12, tstds1, bankIn, LocalDate.now(), null).toList()

      def filterRequest = new BankReconClearingFilterRequest()

      filterRequest['bank'] = bankIn.number
      filterRequest['fromTransactionDate'] = LocalDate.now().minusDays(5)
      filterRequest['thruTransactionDate'] = LocalDate.now().plusDays(5)

      def toUpdate = get("$path/clearing${filterRequest}")

      toUpdate.get(0).clearedDate = LocalDate.now()

      when:

      def result = put("$path/bulk-update", toUpdate)

      then:
      notThrown(HttpClientResponseException)

      with(result.get(0)) {
         id != null
         bank.id == toUpdate.get(0).bank.id

         with(type) {
            value == toUpdate.get(0).type.value
            description == toUpdate.get(0).type.description
         }

         date == toUpdate.get(0).date.toString()
         clearedDate == toUpdate.get(0).clearedDate.toString()
         amount == toUpdate.get(0).amount
         description == toUpdate.get(0).description
         document == toUpdate.get(0).document
      }
      with(result.get(1)) {
         clearedDate == null
      }
   }

   void "update list of bank reconciliation cleared status to uncleared" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      dataLoaderService.stream(5, companyFactoryService.forDatasetCode('corrto'), bankIn, LocalDate.now(), null)
      final bankRecons = dataLoaderService.stream(12, tstds1, bankIn, LocalDate.now(), LocalDate.now().plusMonths(1)).toList()

      def filterRequest = new BankReconClearingFilterRequest()

      filterRequest['bank'] = bankIn.number
      filterRequest['fromTransactionDate'] = LocalDate.now().minusDays(5)
      filterRequest['thruTransactionDate'] = LocalDate.now().plusDays(5)

      def toUpdate = get("$path/clearing${filterRequest}")

      toUpdate.get(0).clearedDate = null

      when:
      def result = put("$path/bulk-update", toUpdate)

      then:
      notThrown(HttpClientResponseException)

      with(result.get(0)) {
         id != null
         bank.id == toUpdate.get(0).bank.id

         with(type) {
            value == toUpdate.get(0).type.value
            description == toUpdate.get(0).type.description
         }

         date == toUpdate.get(0).date.toString()
         clearedDate == null
         amount == toUpdate.get(0).amount
         description == toUpdate.get(0).description
         document == toUpdate.get(0).document
      }
      with(result.get(1)) {
         clearedDate == toUpdate.get(1).clearedDate
      }
   }

   void "fetch all reconciliation transactions for one bank" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      dataLoaderService.stream(5, companyFactoryService.forDatasetCode('corrto'), bankIn, LocalDate.now(), null)
      final bankRecons = dataLoaderService.stream(12, tstds1, bankIn, LocalDate.now(), null).toList()
      final pageOne = new BankReconciliationTransactionsFilterRequest(1, 5, "id", "ASC", bankIn.number, null, null, null, null, null, null, null, null, null, null)
      final pageTwo = new BankReconciliationTransactionsFilterRequest(2, 5, "id", "ASC", bankIn.number, null, null, null, null, null, null, null, null, null, null)
      final pageLast = new BankReconciliationTransactionsFilterRequest(3, 5, "id", "ASC", bankIn.number, null, null, null, null, null, null, null, null, null, null)
      final pageFour = new BankReconciliationTransactionsFilterRequest(4, 5, "id", "ASC", bankIn.number, null, null, null, null, null, null, null, null, null, null)
      final firstPageBankRecon = bankRecons[0..4]
      final secondPageBankRecon = bankRecons[5..9]
      final lastPageBankRecon = bankRecons[10,11]

      when:
      def pageOneResult = get("$path/transactions${pageOne}")

      then:
      pageOneResult.requested.with { new BankReconciliationTransactionsFilterRequest(it) } == pageOne
      pageOneResult.totalElements == 12
      pageOneResult.totalPages == 3
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements.size() == 5
      pageOneResult.elements.eachWithIndex { it, index ->
         with (it) {
            id == firstPageBankRecon[index].id
            bank.id == firstPageBankRecon[index].bank.id

            with(type) {
               value == firstPageBankRecon[index].type.value
               description == firstPageBankRecon[index].type.description
            }

            date == firstPageBankRecon[index].date.toString()
            clearedDate == firstPageBankRecon[index].clearedDate
            amount == firstPageBankRecon[index].amount
            description == firstPageBankRecon[index].description
            document == firstPageBankRecon[index].document
         }
      }

      when:
      def pageTwoResult = get("$path/transactions${pageTwo}")

      then:
      pageTwoResult.requested.with { new BankReconciliationTransactionsFilterRequest(it) } == pageTwo
      pageTwoResult.totalElements == 12
      pageTwoResult.totalPages == 3
      pageTwoResult.first == false
      pageTwoResult.last == false
      pageTwoResult.elements.size() == 5
      pageTwoResult.elements.eachWithIndex { it, index ->
         with(it) {
            id == secondPageBankRecon[index].id
            bank.id == secondPageBankRecon[index].bank.id

            with(type) {
               value == secondPageBankRecon[index].type.value
               description == secondPageBankRecon[index].type.description
            }

            date == secondPageBankRecon[index].date.toString()
            clearedDate == secondPageBankRecon[index].clearedDate
            amount == secondPageBankRecon[index].amount
            description == secondPageBankRecon[index].description
            document == secondPageBankRecon[index].document
         }
      }
      when:
      def pageLastResult = get("$path/transactions${pageLast}")

      then:
      pageLastResult.requested.with { new BankReconciliationTransactionsFilterRequest(it) } == pageLast
      pageLastResult.totalElements == 12
      pageLastResult.totalPages == 3
      pageLastResult.first == false
      pageLastResult.last == true
      pageLastResult.elements.size() == 2
      pageLastResult.elements.eachWithIndex { it, index ->
         with(it) {
            id == lastPageBankRecon[index].id
            bank.id == lastPageBankRecon[index].bank.id

            with(type) {
               value == lastPageBankRecon[index].type.value
               description == lastPageBankRecon[index].type.description
            }

            date == lastPageBankRecon[index].date.toString()
            clearedDate == lastPageBankRecon[index].clearedDate
            amount == lastPageBankRecon[index].amount
            description == lastPageBankRecon[index].description
            document == lastPageBankRecon[index].document
         }
      }

      when:
      get("$path/transactions${pageFour}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "fetch all reconciliation transactions for one bank and one recon type" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      dataLoaderService.stream(5, companyFactoryService.forDatasetCode('corrto'), bankIn, LocalDate.now(), null)
      final bankRecons = dataLoaderService.stream(12, tstds1, bankIn, LocalDate.now(), null, 'A').toList()
      final pageOne = new BankReconciliationTransactionsFilterRequest(1, 5, "id", "ASC", bankIn.number, 'A', null, null, null, null, null, null, null, null, null)
      final pageTwo = new BankReconciliationTransactionsFilterRequest(2, 5, "id", "ASC", bankIn.number, 'A', null, null, null, null, null, null, null, null, null)
      final pageLast = new BankReconciliationTransactionsFilterRequest(3, 5, "id", "ASC", bankIn.number, 'A', null, null, null, null, null, null, null, null, null)
      final pageFour = new BankReconciliationTransactionsFilterRequest(4, 5, "id", "ASC", bankIn.number, 'A', null, null, null, null, null, null, null, null, null)
      final firstPageBankRecon = bankRecons[0..4]
      final secondPageBankRecon = bankRecons[5..9]
      final lastPageBankRecon = bankRecons[10,11]

      when:
      def pageOneResult = get("$path/transactions${pageOne}")

      then:
      pageOneResult.requested.with { new BankReconciliationTransactionsFilterRequest(it) } == pageOne
      pageOneResult.totalElements == 12
      pageOneResult.totalPages == 3
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements.size() == 5
      pageOneResult.elements.eachWithIndex { it, index ->
         with (it) {
            id == firstPageBankRecon[index].id
            bank.id == firstPageBankRecon[index].bank.id

            with(type) {
               value == firstPageBankRecon[index].type.value
               description == firstPageBankRecon[index].type.description
            }

            date == firstPageBankRecon[index].date.toString()
            clearedDate == firstPageBankRecon[index].clearedDate
            amount == firstPageBankRecon[index].amount
            description == firstPageBankRecon[index].description
            document == firstPageBankRecon[index].document
         }
      }

      when:
      def pageTwoResult = get("$path/transactions${pageTwo}")

      then:
      pageTwoResult.requested.with { new BankReconciliationTransactionsFilterRequest(it) } == pageTwo
      pageTwoResult.totalElements == 12
      pageTwoResult.totalPages == 3
      pageTwoResult.first == false
      pageTwoResult.last == false
      pageTwoResult.elements.size() == 5
      pageTwoResult.elements.eachWithIndex { it, index ->
         with(it) {
            id == secondPageBankRecon[index].id
            bank.id == secondPageBankRecon[index].bank.id

            with(type) {
               value == secondPageBankRecon[index].type.value
               description == secondPageBankRecon[index].type.description
            }

            date == secondPageBankRecon[index].date.toString()
            clearedDate == secondPageBankRecon[index].clearedDate
            amount == secondPageBankRecon[index].amount
            description == secondPageBankRecon[index].description
            document == secondPageBankRecon[index].document
         }
      }
      when:
      def pageLastResult = get("$path/transactions${pageLast}")

      then:
      pageLastResult.requested.with { new BankReconciliationTransactionsFilterRequest(it) } == pageLast
      pageLastResult.totalElements == 12
      pageLastResult.totalPages == 3
      pageLastResult.first == false
      pageLastResult.last == true
      pageLastResult.elements.size() == 2
      pageLastResult.elements.eachWithIndex { it, index ->
         with(it) {
            id == lastPageBankRecon[index].id
            bank.id == lastPageBankRecon[index].bank.id

            with(type) {
               value == lastPageBankRecon[index].type.value
               description == lastPageBankRecon[index].type.description
            }

            date == lastPageBankRecon[index].date.toString()
            clearedDate == lastPageBankRecon[index].clearedDate
            amount == lastPageBankRecon[index].amount
            description == lastPageBankRecon[index].description
            document == lastPageBankRecon[index].document
         }
      }

      when:
      get("$path/transactions${pageFour}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "fetch all reconciliation transactions with all filter options (minus cleared date range) set" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      dataLoaderService.stream(5, companyFactoryService.forDatasetCode('corrto'), bankIn, LocalDate.now(), null)
      final bankRecons = dataLoaderService.stream(12, tstds1, bankIn, LocalDate.now().minusDays(1), null, 'A', 111.11, 'testdesc', '20230216').toList()
      final bankRecons2 = dataLoaderService.stream(12, tstds1, bankIn, LocalDate.now().minusDays(1), null, 'C', 111.11, 'testdesc', '20230216').toList()
      final pageOne = new BankReconciliationTransactionsFilterRequest(1, 5, "id", "ASC", bankIn.number, 'A', LocalDate.now().minusDays(1), LocalDate.now(), '20230201', '20230228', 'testdesc', 'O', null, null, 111.11)
      final pageTwo = new BankReconciliationTransactionsFilterRequest(2, 5, "id", "ASC", bankIn.number, 'A', LocalDate.now().minusDays(1), LocalDate.now(), '20230201', '20230228', 'testdesc', 'O', null, null, 111.11)
      final pageLast = new BankReconciliationTransactionsFilterRequest(3, 5, "id", "ASC", bankIn.number, 'A', LocalDate.now().minusDays(1), LocalDate.now(), '20230201', '20230228', 'testdesc', 'O', null, null, 111.11)
      final pageFour = new BankReconciliationTransactionsFilterRequest(4, 5, "id", "ASC", bankIn.number, 'A', LocalDate.now().minusDays(1), LocalDate.now(), '20230201', '20230228', 'testdesc', 'O', null, null, 111.11)
      final firstPageBankRecon = bankRecons[0..4]
      final secondPageBankRecon = bankRecons[5..9]
      final lastPageBankRecon = bankRecons[10,11]

      when:
      def pageOneResult = get("$path/transactions${pageOne}")

      then:
      pageOneResult.totalElements == 12
      pageOneResult.totalPages == 3
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements.size() == 5
      pageOneResult.elements.eachWithIndex { it, index ->
         with (it) {
            id == firstPageBankRecon[index].id
            bank.id == firstPageBankRecon[index].bank.id

            with(type) {
               value == firstPageBankRecon[index].type.value
               description == firstPageBankRecon[index].type.description
            }

            date == firstPageBankRecon[index].date.toString()
            clearedDate == firstPageBankRecon[index].clearedDate
            amount == firstPageBankRecon[index].amount
            description == firstPageBankRecon[index].description
            document == firstPageBankRecon[index].document
         }
      }

      when:
      def pageTwoResult = get("$path/transactions${pageTwo}")

      then:
      pageTwoResult.totalElements == 12
      pageTwoResult.totalPages == 3
      pageTwoResult.first == false
      pageTwoResult.last == false
      pageTwoResult.elements.size() == 5
      pageTwoResult.elements.eachWithIndex { it, index ->
         with(it) {
            id == secondPageBankRecon[index].id
            bank.id == secondPageBankRecon[index].bank.id

            with(type) {
               value == secondPageBankRecon[index].type.value
               description == secondPageBankRecon[index].type.description
            }

            date == secondPageBankRecon[index].date.toString()
            clearedDate == secondPageBankRecon[index].clearedDate
            amount == secondPageBankRecon[index].amount
            description == secondPageBankRecon[index].description
            document == secondPageBankRecon[index].document
         }
      }
      when:
      def pageLastResult = get("$path/transactions${pageLast}")

      then:
      pageLastResult.totalElements == 12
      pageLastResult.totalPages == 3
      pageLastResult.first == false
      pageLastResult.last == true
      pageLastResult.elements.size() == 2
      pageLastResult.elements.eachWithIndex { it, index ->
         with(it) {
            id == lastPageBankRecon[index].id
            bank.id == lastPageBankRecon[index].bank.id

            with(type) {
               value == lastPageBankRecon[index].type.value
               description == lastPageBankRecon[index].type.description
            }

            date == lastPageBankRecon[index].date.toString()
            clearedDate == lastPageBankRecon[index].clearedDate
            amount == lastPageBankRecon[index].amount
            description == lastPageBankRecon[index].description
            document == lastPageBankRecon[index].document
         }
      }

      when:
      get("$path/transactions${pageFour}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "fetch reconciliation transactions within cleared date range" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      dataLoaderService.stream(5, companyFactoryService.forDatasetCode('corrto'), bankIn, LocalDate.now(), null)
      final bankRecons = dataLoaderService.stream(12, tstds1, bankIn, LocalDate.now().minusDays(1), LocalDate.now().minusDays(1), 'A', 111.11, 'testdesc', '20230216').toList()
      final bankRecons2 = dataLoaderService.stream(12, tstds1, bankIn, LocalDate.now().minusDays(1), LocalDate.now().minusDays(3), 'D', 111.11, 'testdesc', '20230216').toList()
      final pageOne = new BankReconciliationTransactionsFilterRequest(1, 5, "id", "ASC", bankIn.number, 'A', LocalDate.now().minusDays(2), LocalDate.now(), '20230201', '20230228', 'testdesc', 'C', LocalDate.now().minusDays(2), LocalDate.now(), 111.11)
      final pageTwo = new BankReconciliationTransactionsFilterRequest(2, 5, "id", "ASC", bankIn.number, 'A', LocalDate.now().minusDays(2), LocalDate.now(), '20230201', '20230228', 'testdesc', 'C', LocalDate.now().minusDays(2), LocalDate.now(), 111.11)
      final pageLast = new BankReconciliationTransactionsFilterRequest(3, 5, "id", "ASC", bankIn.number, 'A', LocalDate.now().minusDays(2), LocalDate.now(), '20230201', '20230228', 'testdesc', 'C', LocalDate.now().minusDays(2), LocalDate.now(), 111.11)
      final pageFour = new BankReconciliationTransactionsFilterRequest(4, 5, "id", "ASC", bankIn.number, 'A', LocalDate.now().minusDays(2), LocalDate.now(), '20230201', '20230228', 'testdesc', 'C', LocalDate.now().minusDays(2), LocalDate.now(), 111.11)
      final firstPageBankRecon = bankRecons[0..4]
      final secondPageBankRecon = bankRecons[5..9]
      final lastPageBankRecon = bankRecons[10,11]

      when:
      def pageOneResult = get("$path/transactions${pageOne}")

      then:
      pageOneResult.totalElements == 12
      pageOneResult.totalPages == 3
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements.size() == 5
      pageOneResult.elements.eachWithIndex { it, index ->
         with (it) {
            id == firstPageBankRecon[index].id
            bank.id == firstPageBankRecon[index].bank.id

            with(type) {
               value == firstPageBankRecon[index].type.value
               description == firstPageBankRecon[index].type.description
            }

            date == firstPageBankRecon[index].date.toString()
            clearedDate == firstPageBankRecon[index].clearedDate.toString()
            amount == firstPageBankRecon[index].amount
            description == firstPageBankRecon[index].description
            document == firstPageBankRecon[index].document
         }
      }

      when:
      def pageTwoResult = get("$path/transactions${pageTwo}")

      then:
      pageTwoResult.totalElements == 12
      pageTwoResult.totalPages == 3
      pageTwoResult.first == false
      pageTwoResult.last == false
      pageTwoResult.elements.size() == 5
      pageTwoResult.elements.eachWithIndex { it, index ->
         with(it) {
            id == secondPageBankRecon[index].id
            bank.id == secondPageBankRecon[index].bank.id

            with(type) {
               value == secondPageBankRecon[index].type.value
               description == secondPageBankRecon[index].type.description
            }

            date == secondPageBankRecon[index].date.toString()
            clearedDate == secondPageBankRecon[index].clearedDate.toString()
            amount == secondPageBankRecon[index].amount
            description == secondPageBankRecon[index].description
            document == secondPageBankRecon[index].document
         }
      }
      when:
      def pageLastResult = get("$path/transactions${pageLast}")

      then:
      pageLastResult.totalElements == 12
      pageLastResult.totalPages == 3
      pageLastResult.first == false
      pageLastResult.last == true
      pageLastResult.elements.size() == 2
      pageLastResult.elements.eachWithIndex { it, index ->
         with(it) {
            id == lastPageBankRecon[index].id
            bank.id == lastPageBankRecon[index].bank.id

            with(type) {
               value == lastPageBankRecon[index].type.value
               description == lastPageBankRecon[index].type.description
            }

            date == lastPageBankRecon[index].date.toString()
            clearedDate == lastPageBankRecon[index].clearedDate.toString()
            amount == lastPageBankRecon[index].amount
            description == lastPageBankRecon[index].description
            document == lastPageBankRecon[index].document
         }
      }

      when:
      get("$path/transactions${pageFour}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "delete a bank reconciliation"() {
      given:
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      final bankRecon = dataLoaderService.singleDTO(bankIn, LocalDate.now(), LocalDate.now())

      def toDelete = post("$path/", bankRecon)

      when:
      def result = delete("$path/bulk-delete", toDelete)
      then: "bank for user's company is deleted"
      notThrown(HttpClientResponseException)

      when:
      get("$path/${toDelete.id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$toDelete.id was unable to be found"
      response.code == 'system.not.found'
   }

   void "delete a list of bank reconciliation"() {
      given:
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      final bankRecons = dataLoaderService.stream(12, tstds1, bankIn, LocalDate.now(), null).toList()

      def br1 = get("$path/${bankRecons[0].id}")
      def br2 = get("$path/${bankRecons[1].id}")
      def br3 = get("$path/${bankRecons[2].id}")

      def toDelete = []
      toDelete.add(br1)
      toDelete.add(br2)

      when:
      delete("$path/bulk-delete/", toDelete)
      then: "bank for user's company is deleted"
      notThrown(HttpClientResponseException)

      when:
      get("$path/${br1.id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$br1.id was unable to be found"
      response.code == 'system.not.found'

      when:
      get("$path/${br3.id}")

      then:
      notThrown(HttpClientResponseException)
   }

   void "fetch reconcile bank account report" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      dataLoaderService.stream(5, companyFactoryService.forDatasetCode('corrto'), bankIn, LocalDate.now(), null)
      final feeBankRecons = dataLoaderService.stream(5, tstds1, bankIn, LocalDate.now(), null, 'F').toList()
      final voidedBankRecons = dataLoaderService.stream(3, tstds1, bankIn, LocalDate.now(), null, 'V').toList()
      final filterRequest = new ReconcileBankAccountFilterRequest(bankIn.number, LocalDate.now())
      final givenSumFee = feeBankRecons.amount.sum()
      final givenSumVoided = voidedBankRecons.amount.sum()

      when:
      def result = get("$path/reconcile${filterRequest}")

      then:
      with(result) { it ->
         with(groupedReconciliations) { groupedRecon ->
            with(groupedRecon[0]) {
               type.value == 'F'
               sumAmount == givenSumFee
               details.size() == 5
            }
            with(groupedRecon[1]) {
               type.value == 'V'
               sumAmount == givenSumVoided
               details.size() == 3
            }
         }
         totalOutstandingItems == givenSumFee + givenSumVoided
         computedBankStmtBalance == glBalance - givenSumFee
      }
   }

   void "fetch reconcile bank account summary without details" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankIn = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      dataLoaderService.stream(5, companyFactoryService.forDatasetCode('corrto'), bankIn, LocalDate.now(), null)
      final feeBankRecons = dataLoaderService.stream(5, tstds1, bankIn, LocalDate.now(), null, 'F').toList()
      final voidedBankRecons = dataLoaderService.stream(3, tstds1, bankIn, LocalDate.now(), null, 'V').toList()
      final filterRequest = new ReconcileBankAccountFilterRequest(bankIn.number, LocalDate.now())
      final givenSumFee = feeBankRecons.amount.sum()
      final givenSumVoided = voidedBankRecons.amount.sum()

      when:
      def result = get("$path/reconcile/summary${filterRequest}")

      then:
      with(result) { it ->
         with(groupedReconciliations) { groupedRecon ->
            with(groupedRecon[0]) {
               type.value == 'F'
               sumAmount == givenSumFee
               details == null
            }
            with(groupedRecon[1]) {
               type.value == 'V'
               sumAmount == givenSumVoided
               details == null
            }
         }
         totalOutstandingItems == givenSumFee + givenSumVoided
         computedBankStmtBalance == glBalance - givenSumFee
      }
   }
}
