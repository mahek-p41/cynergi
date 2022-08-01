package com.cynergisuite.middleware.accounting.bank.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.bank.BankDTO
import com.cynergisuite.middleware.accounting.bank.BankFactoryService
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationDataLoaderService
import com.cynergisuite.middleware.error.ErrorDTO
import groovy.json.JsonOutput
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import jakarta.inject.Inject
import java.time.LocalDate

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.CONFLICT
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class BankControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/accounting/bank'
   private JsonOutput jsonOutput = new JsonOutput()
   @Inject BankFactoryService bankFactoryService
   @Inject AccountTestDataLoaderService accountFactoryService
   @Inject BankReconciliationDataLoaderService bankReconciliationDataLoaderService

   void "fetch one bank by id" () {
      given:
      final account = accountFactoryService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      when:
      def result = get("$path/${bank.id}")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == bank.id
         name == bank.name
         generalLedgerProfitCenter.id == bank.generalLedgerProfitCenter.myId()
         generalLedgerAccount.id == bank.generalLedgerAccount.id
      }
   }

   void "fetch one bank by id not found" () {
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
      final account = accountFactoryService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      bankFactoryService.stream(5, store, account)
      final banks = bankFactoryService.stream(12, store, account).toList()
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
         id != null
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
      response.collect { new ErrorDTO(it.message, it.code, it.path) } == [
         new ErrorDTO("${account.id} was unable to be found", 'system.not.found', "generalLedgerAccount.id")
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
      response.collect { new ErrorDTO(it.message, it.code, it.path) } == [
         new ErrorDTO("${String.format('%,d', store.id)} was unable to be found", 'system.not.found', "generalLedgerProfitCenter.id")
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
      response[0].code == 'cynergi.validation.duplicate'
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
      response[0].code == 'javax.validation.constraints.NotNull.message'

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
         id == updatedBankDTO.id
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
         id == updatedBankDTO.id
         name == updatedBankDTO.name
         generalLedgerProfitCenter.id == store.id
         generalLedgerAccount.id == updateAccount.id
      }
   }

   void "update invalid bank without non-nullable properties"() {
      given:
      final account = accountFactoryService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final existingBank = bankFactoryService.single(nineNineEightEmployee.company, store, account)
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
      final company = nineNineEightEmployee.company
      final account1 = accountFactoryService.single(company)
      final account2 = accountFactoryService.single(company)
      final store = storeFactoryService.store(3, company)
      final existingBank1 = bankFactoryService.single(company, store, account1)
      final existingBank2 = bankFactoryService.single(company, store, account2)
      final bankDTO = bankFactoryService.singleDTO(store, account1)
      bankDTO.id = existingBank1.id
      bankDTO.number = existingBank2.number

      when:
      put("$path/$bankDTO.id", bankDTO)

      then:
      final ex = thrown(HttpClientResponseException)
      ex.status == BAD_REQUEST
      final response = ex.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'number'
      response[0].message == "$bankDTO.number already exists"
      response[0].code == 'cynergi.validation.duplicate'
   }

   void "update a valid bank with no id"() {
      given: 'Update existingBank in DB with all new data'
      final account = accountFactoryService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final existingBank = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      final updatedBankDTO = new BankDTO(existingBank)
      updatedBankDTO.id = existingBank.id

      when:
      def result = put("$path/$updatedBankDTO.id", updatedBankDTO)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == updatedBankDTO.id
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

      then: "bank for user's company is deleted"
      notThrown(HttpClientResponseException)

      when:
      get("$path/$bank.id")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$bank.id was unable to be found"
      response.code == 'system.not.found'
   }

   void "delete bank from other company is not allowed" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "tstds1" }
      final tstds2 = companies.find { it.datasetCode == "tstds2" }
      final accountTstds1 = accountFactoryService.single(tstds1)
      final accountTstds2 = accountFactoryService.single(tstds2)
      final store3Tstds1 = storeFactoryService.store(3, tstds1)
      final store4Tstds2 = storeFactoryService.store(4, tstds2)
      final bankStore3Tstds1 = bankFactoryService.single(tstds1, store3Tstds1, accountTstds1)
      final bankStore4Tstds2 = bankFactoryService.single(tstds2, store4Tstds2, accountTstds2)

      when:
      delete("$path/$bankStore4Tstds2.id")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$bankStore4Tstds2.id was unable to be found"
      response.code == 'system.not.found'
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
      response.message == "Requested operation violates data integrity"
      response.code == 'cynergi.data.constraint.violated'
   }

   void "recreate deleted bank" () {
      given:
      final account = accountFactoryService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bankDTO = bankFactoryService.singleDTO(store, account)

      when: // create a bank
      def response1 = post("$path/", bankDTO)

      then:
      notThrown(HttpClientResponseException)

      with(response1) {
         id != null
         name == bankDTO.name
         generalLedgerProfitCenter.id == bankDTO.generalLedgerProfitCenter.id
         generalLedgerAccount.id == bankDTO.generalLedgerAccount.id
      }

      when: // delete bank
      delete("$path/$response1.id")

      then: "bank of user's company is deleted"
      notThrown(HttpClientResponseException)

      when: // recreate bank
      def response2 = post("$path/", bankDTO)

      then:
      notThrown(HttpClientResponseException)

      with(response2) {
         id != null
         name == bankDTO.name
         generalLedgerProfitCenter.id == bankDTO.generalLedgerProfitCenter.id
         generalLedgerAccount.id == bankDTO.generalLedgerAccount.id
      }

      when: // delete bank again
      delete("$path/$response2.id")

      then: "bank of user's company is deleted"
      notThrown(HttpClientResponseException)
   }
}
