package com.cynergisuite.middleware.accounting.bank.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDataLoaderService
import com.cynergisuite.middleware.accounting.bank.BankDTO
import com.cynergisuite.middleware.accounting.bank.BankFactoryService
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationDataLoaderService
import com.cynergisuite.middleware.error.ErrorDTO
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject
import java.time.LocalDate

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.CONFLICT
import static io.micronaut.http.HttpStatus.INTERNAL_SERVER_ERROR
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class BankControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/accounting/bank'
   private JsonOutput jsonOutput = new JsonOutput()
   @Inject BankFactoryService bankFactoryService
   @Inject AccountDataLoaderService accountFactoryService
   @Inject BankReconciliationDataLoaderService bankReconciliationDataLoaderService

   void "fetch one bank by id" () {
      given:
      final account = accountFactoryService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      bankFactoryService.single(nineNineEightEmployee.company, store, account)
      final bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      when:
      def result = get("$path/${bank.id}")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == bank.id
         name == bank.name
         generalLedgerProfitCenter.id == bank.generalLedgerProfitCenter.id
         generalLedgerAccount.id == bank.generalLedgerAccount.id
      }
   }

   void "fetch one bank by id not found" () {
      when:
      get("$path/0")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == '0 was unable to be found'
   }

   void "fetch all" () {
      given:
      final account = accountFactoryService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      bankFactoryService.stream(5, companyFactoryService.forDatasetCode('tstds2'), store, account)
      final banks = bankFactoryService.stream(12, nineNineEightEmployee.company, store, account).toList()
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      final pageLast = new StandardPageRequest(3, 5, "id", "ASC")
      final pageFour = new StandardPageRequest(4, 5, "id", "ASC")
      final firstPageBank = banks[0..4]
      final secondPageBank = banks[5..9]
      final lastPageBank = banks[10,11]

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
            id == firstPageBank[index].id
            name == firstPageBank[index].name
            generalLedgerProfitCenter.id == firstPageBank[index].generalLedgerProfitCenter.id
            generalLedgerAccount.id == firstPageBank[index].generalLedgerAccount.id
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
            id == secondPageBank[index].id
            name == secondPageBank[index].name
            generalLedgerProfitCenter.id == secondPageBank[index].generalLedgerProfitCenter.id
            generalLedgerAccount.id == secondPageBank[index].generalLedgerAccount.id
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
            id == lastPageBank[index].id
            name == lastPageBank[index].name
            generalLedgerProfitCenter.id == lastPageBank[index].generalLedgerProfitCenter.id
            generalLedgerAccount.id == lastPageBank[index].generalLedgerAccount.id
         }
      }

      when:
      get("$path/${pageFour}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create a valid bank"() {
      given:
      final account = accountFactoryService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankDTO = bankFactoryService.singleDTO(store, account)
      final jsonBank = jsonOutput.toJson(bankDTO)

      when:
      def result = post("$path/", jsonBank)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
         name == bankDTO.name
         generalLedgerProfitCenter.id == bankDTO.generalLedgerProfitCenter.id
         generalLedgerAccount.id == bankDTO.generalLedgerAccount.id
      }
   }

   void "create invalid bank with other company's account" () {
      given:
      final company2 = companyFactoryService.forDatasetCode('tstds2')
      final account = accountFactoryService.single(company2)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankDTO = bankFactoryService.singleDTO(store, account)

      when:
      post("$path/", bankDTO)

      then:
      final ex = thrown(HttpClientResponseException)
      ex.status == BAD_REQUEST
      final response = ex.response.bodyAsJson()
      response.size() == 1
      response.collect { new ErrorDTO(it.message, it.path) } == [
         new ErrorDTO("${String.format('%,d', account.id)} was unable to be found", "generalLedgerAccount.id")
      ]
   }

   void "create invalid bank with other company's store" () {
      given:
      final company2 = companyFactoryService.forDatasetCode('tstds2')
      final account = accountFactoryService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, company2)
      final bankDTO = bankFactoryService.singleDTO(store, account)

      when:
      post("$path/", bankDTO)

      then:
      final ex = thrown(HttpClientResponseException)
      ex.status == BAD_REQUEST
      final response = ex.response.bodyAsJson()
      response.size() == 1
      response.collect { new ErrorDTO(it.message, it.path) } == [
         new ErrorDTO("${String.format('%,d', store.id)} was unable to be found", "generalLedgerProfitCenter.id")
      ]
   }

   void "create invalid bank with duplicate bank number" () {
      given:
      final account = accountFactoryService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final existingBank = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      final bankDTO = bankFactoryService.singleDTO(store, account)
      bankDTO.number = existingBank.number

      when:
      post("$path/", bankDTO)

      then:
      final ex = thrown(HttpClientResponseException)
      ex.status == BAD_REQUEST
      final response = ex.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'number'
      response[0].message == "$existingBank.number already exists"
   }

   void "create an invalid account without #nonNullableProp"() {
      given:
      final company2 = companyFactoryService.forDatasetCode('tstds2')
      final account = accountFactoryService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, company2)

      final bankDTO = bankFactoryService.singleDTO(store, account)
      bankDTO["$nonNullableProp"] = null

      when:
      post("$path/", bankDTO)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp                              || errorResponsePath
      'number'                                     || 'number'
      'name'                                       || 'name'
      'generalLedgerProfitCenter'                  || 'generalLedgerProfitCenter'
      'generalLedgerAccount'                       || 'generalLedgerAccount'
   }

   void "update a valid bank's profit center"() {
      given: 'Update existingBank in DB with all new data in jsonBank'
      final account = accountFactoryService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final updateStore = storeFactoryService.store(1, nineNineEightEmployee.company)
      final existingBank = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      def updatedBankDTO = bankFactoryService.singleDTO(store, account)
      updatedBankDTO.id = existingBank.id
      updatedBankDTO.generalLedgerProfitCenter.id = updateStore.id

      when:
      def result = put("$path/$updatedBankDTO.id", updatedBankDTO)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
         name == updatedBankDTO.name
         generalLedgerProfitCenter.id == updateStore.id
         generalLedgerAccount.id == updatedBankDTO.generalLedgerAccount.id
      }
   }

   void "update a valid bank's profit gl account"() {
      given: 'Update existingBank in DB with all new data in jsonBank'
      final account = accountFactoryService.single(nineNineEightEmployee.company)
      final updateAccount = accountFactoryService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final existingBank = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      final updatedBankDTO = bankFactoryService.singleDTO(store, account)
      updatedBankDTO.id = existingBank.id
      updatedBankDTO.generalLedgerAccount.id = updateAccount.id

      when:
      def result = put("$path/$updatedBankDTO.id", updatedBankDTO)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
         name == updatedBankDTO.name
         generalLedgerProfitCenter.id == store.id
         generalLedgerAccount.id == updateAccount.id
      }
   }

   void "update invalid bank without non-nullable properties"() {
      given:
      final def account = accountFactoryService.single(nineNineEightEmployee.company)
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final def existingBank = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      def updatedBankDTO = bankFactoryService.singleDTO(store, account)
      updatedBankDTO.id = existingBank.id
      updatedBankDTO.number = null
      updatedBankDTO.name = null
      updatedBankDTO.generalLedgerProfitCenter = null
      updatedBankDTO.generalLedgerAccount = null


      when:
      put("$path/$updatedBankDTO.id", updatedBankDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson().collect().sort { a,b -> a.path <=> b.path }
      response.size() == 4

      response[0].path == 'generalLedgerAccount'
      response[1].path == 'generalLedgerProfitCenter'
      response[2].path == 'name'
      response[3].path == 'number'
      response.collect { it.message } as Set == ['Is required'] as Set
   }

   void "update invalid bank with duplicate bank number" () {
      given:
      final account = accountFactoryService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final existingBanks = bankFactoryService.stream(2, nineNineEightEmployee.company, store, account).collect()
      final bankDTO = bankFactoryService.singleDTO(store, account)
      bankDTO.id = existingBanks[0].id
      bankDTO.number = existingBanks[1].number

      when:
      put("$path/$bankDTO.id", bankDTO)

      then:
      final ex = thrown(HttpClientResponseException)
      ex.status == BAD_REQUEST
      final response = ex.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'number'
      response[0].message == "$bankDTO.number already exists"
   }

   void "update a valid bank with no id"() {
      given: 'Update existingBank in DB with all new data'
      final def account = accountFactoryService.single(nineNineEightEmployee.company)
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final def existingBank = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      final def updatedBankDTO = new BankDTO(existingBank)
      updatedBankDTO.id = existingBank.id

      when:
      def result = put("$path/$updatedBankDTO.id", updatedBankDTO)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
         name == updatedBankDTO.name
         generalLedgerProfitCenter.id == store.id
         generalLedgerAccount.id == updatedBankDTO.generalLedgerAccount.id
      }
   }

   void "delete bank" () {
      given:
      final account = accountFactoryService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      bankFactoryService.single(nineNineEightEmployee.company, store, account)
      final bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      when:
      delete("$path/$bank.id", )

      then: "bank for user's company is delete"
      notThrown(HttpClientResponseException)

      when:
      get("$path/$bank.id")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "$bank.id was unable to be found"
   }

   void "delete bank from other company is not allowed" () {
      given:
      def tstds2 = companies.find { it.datasetCode == "tstds2" }
      final account = accountFactoryService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      bankFactoryService.single(nineNineEightEmployee.company, store, account)
      def bank = bankFactoryService.single(tstds2, store, account)

      when:
      delete("$path/$bank.id")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "$bank.id was unable to be found"
   }

   void "delete bank still has reference" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final account = accountFactoryService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      bankReconciliationDataLoaderService.single(tstds1, bank, LocalDate.now(), null)

      when:
      delete("$path/$bank.id")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == CONFLICT
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "Key (id)=($bank.id) is still referenced from table \"bank_reconciliation\"."
   }
}
