package com.cynergisuite.middleware.accounting.bank.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDataLoaderService
import com.cynergisuite.middleware.accounting.bank.BankFactoryService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class BankControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/accounting/bank'
   private JsonOutput jsonOutput = new JsonOutput()
   private JsonSlurper jsonSlurper = new JsonSlurper()
   @Inject BankFactoryService bankFactoryService
   @Inject AccountDataLoaderService accountFactoryService

   void "fetch one bank by id" () {
      given:
      final def account = accountFactoryService.single(nineNineEightEmployee.company)
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      bankFactoryService.single(nineNineEightEmployee.company, store, account)
      final def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      when:
      def result = get("$path/${bank.id}")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == bank.id
         name == bank.name
         accountNumber == bank.accountNumber
         generalLedgerProfitCenter.id == bank.generalLedgerProfitCenter.id
         generalLedgerAccount.id == bank.generalLedgerAccount.id
         with(address) {
            id > 0
            name == bank.address.name
            address1 == bank.address.address1
            address2 == bank.address.address2
            city == bank.address.city
            country == bank.address.country
            county == bank.address.county
            latitude == bank.address.latitude
            longitude == bank.address.longitude
            postalCode == bank.address.postalCode
            state == bank.address.state
            phone == bank.address.phone
            fax == bank.address.fax
         }
         with(currency) {
            description == bank.currency.description
            value == bank.currency.value
         }
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
      final def account = accountFactoryService.single(nineNineEightEmployee.company)
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      bankFactoryService.stream(5, companyFactoryService.forDatasetCode('tstds2'), store, account)
      final def banks = bankFactoryService.stream(12, nineNineEightEmployee.company, store, account).toList()
      def pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      def pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      def pageLast = new StandardPageRequest(3, 5, "id", "ASC")
      def pageFour = new StandardPageRequest(4, 5, "id", "ASC")
      def firstPageBank = banks[0..4]
      def secondPageBank = banks[5..9]
      def lastPageBank = banks[10,11]

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
            accountNumber == firstPageBank[index].accountNumber
            generalLedgerProfitCenter.id == firstPageBank[index].generalLedgerProfitCenter.id
            generalLedgerAccount.id == firstPageBank[index].generalLedgerAccount.id
            with (address) {
               id > 0
               name == firstPageBank[index].address.name
               address1 == firstPageBank[index].address.address1
               address2 == firstPageBank[index].address.address2
               city == firstPageBank[index].address.city
               country == firstPageBank[index].address.country
               county == firstPageBank[index].address.county
               latitude == firstPageBank[index].address.latitude
               longitude == firstPageBank[index].address.longitude
               postalCode == firstPageBank[index].address.postalCode
               state == firstPageBank[index].address.state
               phone == firstPageBank[index].address.phone
               fax == firstPageBank[index].address.fax
            }
            with (currency) {
               description == firstPageBank[index].currency.description
               value == firstPageBank[index].currency.value
            }
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
            accountNumber == secondPageBank[index].accountNumber
            generalLedgerProfitCenter.id == secondPageBank[index].generalLedgerProfitCenter.id
            generalLedgerAccount.id == secondPageBank[index].generalLedgerAccount.id
            with(address) {
               id > 0
               name == secondPageBank[index].address.name
               address1 == secondPageBank[index].address.address1
               address2 == secondPageBank[index].address.address2
               city == secondPageBank[index].address.city
               country == secondPageBank[index].address.country
               county == secondPageBank[index].address.county
               latitude == secondPageBank[index].address.latitude
               longitude == secondPageBank[index].address.longitude
               postalCode == secondPageBank[index].address.postalCode
               state == secondPageBank[index].address.state
               phone == secondPageBank[index].address.phone
               fax == secondPageBank[index].address.fax
            }
            with(currency) {
               description == secondPageBank[index].currency.description
               value == secondPageBank[index].currency.value
            }
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
            accountNumber == lastPageBank[index].accountNumber
            generalLedgerProfitCenter.id == lastPageBank[index].generalLedgerProfitCenter.id
            generalLedgerAccount.id == lastPageBank[index].generalLedgerAccount.id
            with(address) {
               id > 0
               name == lastPageBank[index].address.name
               address1 == lastPageBank[index].address.address1
               address2 == lastPageBank[index].address.address2
               city == lastPageBank[index].address.city
               country == lastPageBank[index].address.country
               county == lastPageBank[index].address.county
               latitude == lastPageBank[index].address.latitude
               longitude == lastPageBank[index].address.longitude
               postalCode == lastPageBank[index].address.postalCode
               state == lastPageBank[index].address.state
               phone == lastPageBank[index].address.phone
               fax == lastPageBank[index].address.fax
            }
            with(currency) {
               description == lastPageBank[index].currency.description
               value == lastPageBank[index].currency.value
            }
         }
      }

      when:
      get("$path/${pageFour}")

      then:
      final def notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create a valid bank"() {
      given:
      final def account = accountFactoryService.single(nineNineEightEmployee.company)
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final def bankDTO = bankFactoryService.singleDTO(store, account)
      final def jsonBank = jsonOutput.toJson(bankDTO)

      when:
      def result = post("$path/", jsonBank)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
         name == bankDTO.name
         accountNumber == bankDTO.accountNumber
         generalLedgerProfitCenter.id == bankDTO.generalLedgerProfitCenter.id
         generalLedgerAccount.id == bankDTO.generalLedgerAccount.id
         with(address) {
            id > 0
            name == bankDTO.address.name
            address1 == bankDTO.address.address1
            address2 == bankDTO.address.address2
            city == bankDTO.address.city
            country == bankDTO.address.country
            county == bankDTO.address.county
            latitude == bankDTO.address.latitude
            longitude == bankDTO.address.longitude
            postalCode == bankDTO.address.postalCode
            state == bankDTO.address.state
            phone == bankDTO.address.phone
            fax == bankDTO.address.fax
         }
         with(currency) {
            description == bankDTO.currency.description
            value == bankDTO.currency.value
         }
      }
   }

   void "create an invalid bank without account number"() {
      given: 'get json bank object and make it invalid'
      final def account = accountFactoryService.single(nineNineEightEmployee.company)
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final def bankDTO = bankFactoryService.singleDTO(store, account)
      // Make invalid json
      def jsonBank = jsonSlurper.parseText(jsonOutput.toJson(bankDTO))
      jsonBank.remove('accountNumber')

      when:
      def result = post("$path/", jsonBank)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].message == 'Is required'
      response[0].path == 'bankDTO.accountNumber'
   }

   void "create an invalid bank without address"() {
      given: 'get json bank object and make it invalid'
      final def account = accountFactoryService.single(nineNineEightEmployee.company)
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final def bankDTO = bankFactoryService.singleDTO(store, account)
      // Make invalid json
      def jsonBank = jsonSlurper.parseText(jsonOutput.toJson(bankDTO))
      jsonBank.remove('address')

      when:
      post("$path/", jsonBank)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'bankDTO.address'
      response[0].message == 'Is required'
   }

   void "create an invalid bank with non exist currency value"() {
      given: 'get json bank object and make it invalid'
      final def account = accountFactoryService.single(nineNineEightEmployee.company)
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final def bankDTO = bankFactoryService.singleDTO(store, account)
      // Make invalid json
      def jsonBank = jsonSlurper.parseText(jsonOutput.toJson(bankDTO))
      jsonBank.currency.value = 'USDT'

      when:
      post("$path/", jsonBank)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'currency.value'
      response[0].message == 'USDT was unable to be found'
   }

   void "update a valid bank"() {
      given: 'Update existingBank in DB with all new data in jsonBank'
      final def account = accountFactoryService.single(nineNineEightEmployee.company)
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final def existingBank = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      final def updatedBankDTO = bankFactoryService.singleDTO(store, account)
      def jsonBank = jsonSlurper.parseText(jsonOutput.toJson(updatedBankDTO))
      jsonBank.with {
         id = existingBank.id
         address.id = existingBank.address.id
      }

      when:
      def result = put("$path/$existingBank.id", jsonBank)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
         name == updatedBankDTO.name
         accountNumber == updatedBankDTO.accountNumber
         generalLedgerProfitCenter.id == updatedBankDTO.generalLedgerProfitCenter.id
         generalLedgerAccount.id == updatedBankDTO.generalLedgerAccount.id
         with(address) {
            id > 0
            name == updatedBankDTO.address.name
            address1 == updatedBankDTO.address.address1
            address2 == updatedBankDTO.address.address2
            city == updatedBankDTO.address.city
            country == updatedBankDTO.address.country
            county == updatedBankDTO.address.county
            latitude == updatedBankDTO.address.latitude
            longitude == updatedBankDTO.address.longitude
            postalCode == updatedBankDTO.address.postalCode
            state == updatedBankDTO.address.state
            phone == updatedBankDTO.address.phone
            fax == updatedBankDTO.address.fax
         }
         with(currency) {
            description == updatedBankDTO.currency.description
            value == updatedBankDTO.currency.value
         }
      }
   }

   void "update a invalid bank without bank name"() {
      given:
      final def account = accountFactoryService.single(nineNineEightEmployee.company)
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final def bankDTO = bankFactoryService.single( nineNineEightEmployee.company, store, account)
      def jsonBank = jsonSlurper.parseText(jsonOutput.toJson(bankDTO))
      jsonBank.address.name = ''

      when:
      put("$path/$bankDTO.id", jsonBank)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1

      response[0].path == 'bankDTO.address.name'
      response[0].message == 'Is required'
   }

   void "update a invalid bank with non exist address id"() {
      given:
      final def account = accountFactoryService.single(nineNineEightEmployee.company)
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final def bankDTO = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      def jsonBank = jsonSlurper.parseText(jsonOutput.toJson(bankDTO))
      jsonBank.address.id = 1000

      when:
      put("$path/$bankDTO.id", jsonBank)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].message == '1,000 was unable to be found'
      response[0].path == 'address.id'
   }

   void "update a invalid bank without currency"() {
      given:
      final def account = accountFactoryService.single(nineNineEightEmployee.company)
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final def bankDTO = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      def jsonBank = jsonSlurper.parseText(jsonOutput.toJson(bankDTO))
      jsonBank.remove('currency')

      when:
      put("$path/$bankDTO.id", jsonBank)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'bankDTO.currency'
      response[0].message == 'Is required'
   }
}
