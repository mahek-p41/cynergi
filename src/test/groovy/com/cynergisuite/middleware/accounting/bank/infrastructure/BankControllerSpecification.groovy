package com.cynergisuite.middleware.accounting.bank.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.bank.BankFactoryService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.INTERNAL_SERVER_ERROR
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class BankControllerSpecification extends ControllerSpecificationBase {
   private static String path = "/accounting/bank"
   private JsonOutput jsonOutput = new JsonOutput()
   private JsonSlurper jsonSlurper = new JsonSlurper()
   @Inject BankFactoryService bankFactoryService

   void "fetch one bank by id" () {
      given:
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      bankFactoryService.single(nineNineEightEmployee.company, store)
      final def bank = bankFactoryService.single(nineNineEightEmployee.company, store)

      when:
      def result = get("$path/${bank.id}")

      then:
      notThrown(HttpClientResponseException)

      // TODO find a better way for comparison
      with(result) {
         id == bank.id
         company.id == bank.company.id
         number == bank.number
         name == bank.name
         accountNumber == bank.accountNumber
         with(generalLedgerProfitCenter) {
            id == bank.generalLedgerProfitCenter.id
            number == bank.generalLedgerProfitCenter.number
            name == bank.generalLedgerProfitCenter.name
            company.id == bank.generalLedgerProfitCenter.company.id
         }
         with(address) {
            id > 0
            name == bank.address.name
            address1 == bank.address.address1
            address2 == bank.address.address2
            number == bank.address.number
            city == bank.address.city
            country == bank.address.country
            county == bank.address.county
            latitude == bank.address.latitude
            longitude == bank.address.longitude
            postalCode == bank.address.postalCode
            state == bank.address.state
         }
         with(currency) {
            description == bank.currency.description
            id == bank.currency.id
            localizationCode == bank.currency.localizationCode
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
      response.message == "0 was unable to be found"
   }

   void "fetch all" () {
      given:
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      bankFactoryService.stream(5, companyFactoryService.forDatasetCode('tstds2'), store)
      final def banks = bankFactoryService.stream(12, nineNineEightEmployee.company, store).toList()
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
            company.id == firstPageBank[index].company.id
            number == firstPageBank[index].number
            name == firstPageBank[index].name
            accountNumber == firstPageBank[index].accountNumber
            with (generalLedgerProfitCenter) {
               id == firstPageBank[index].generalLedgerProfitCenter.id
               number == firstPageBank[index].generalLedgerProfitCenter.number
               name == firstPageBank[index].generalLedgerProfitCenter.name
               company.id == firstPageBank[index].generalLedgerProfitCenter.company.id
            }
            with (address) {
               id > 0
               name == firstPageBank[index].address.name
               address1 == firstPageBank[index].address.address1
               address2 == firstPageBank[index].address.address2
               number == firstPageBank[index].address.number
               city == firstPageBank[index].address.city
               country == firstPageBank[index].address.country
               county == firstPageBank[index].address.county
               latitude == firstPageBank[index].address.latitude
               longitude == firstPageBank[index].address.longitude
               postalCode == firstPageBank[index].address.postalCode
               state == firstPageBank[index].address.state
            }
            with (currency) {
               description == firstPageBank[index].currency.description
               id == firstPageBank[index].currency.id
               localizationCode == firstPageBank[index].currency.localizationCode
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
            company.id == secondPageBank[index].company.id
            number == secondPageBank[index].number
            name == secondPageBank[index].name
            accountNumber == secondPageBank[index].accountNumber
            with(generalLedgerProfitCenter) {
               id == secondPageBank[index].generalLedgerProfitCenter.id
               number == secondPageBank[index].generalLedgerProfitCenter.number
               name == secondPageBank[index].generalLedgerProfitCenter.name
               company.id == secondPageBank[index].generalLedgerProfitCenter.company.id
            }
            with(address) {
               id > 0
               name == secondPageBank[index].address.name
               address1 == secondPageBank[index].address.address1
               address2 == secondPageBank[index].address.address2
               number == secondPageBank[index].address.number
               city == secondPageBank[index].address.city
               country == secondPageBank[index].address.country
               county == secondPageBank[index].address.county
               latitude == secondPageBank[index].address.latitude
               longitude == secondPageBank[index].address.longitude
               postalCode == secondPageBank[index].address.postalCode
               state == secondPageBank[index].address.state
            }
            with(currency) {
               description == secondPageBank[index].currency.description
               id == secondPageBank[index].currency.id
               localizationCode == secondPageBank[index].currency.localizationCode
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
            company.id == lastPageBank[index].company.id
            number == lastPageBank[index].number
            name == lastPageBank[index].name
            accountNumber == lastPageBank[index].accountNumber
            with(generalLedgerProfitCenter) {
               id == lastPageBank[index].generalLedgerProfitCenter.id
               number == lastPageBank[index].generalLedgerProfitCenter.number
               name == lastPageBank[index].generalLedgerProfitCenter.name
               company.id == lastPageBank[index].generalLedgerProfitCenter.company.id
            }
            with(address) {
               id > 0
               name == lastPageBank[index].address.name
               address1 == lastPageBank[index].address.address1
               address2 == lastPageBank[index].address.address2
               number == lastPageBank[index].address.number
               city == lastPageBank[index].address.city
               country == lastPageBank[index].address.country
               county == lastPageBank[index].address.county
               latitude == lastPageBank[index].address.latitude
               longitude == lastPageBank[index].address.longitude
               postalCode == lastPageBank[index].address.postalCode
               state == lastPageBank[index].address.state
            }
            with(currency) {
               description == lastPageBank[index].currency.description
               id == lastPageBank[index].currency.id
               localizationCode == lastPageBank[index].currency.localizationCode
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
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final def bankDTO = bankFactoryService.singleDTO( nineNineEightEmployee.company, store)
      final def jsonBank = jsonOutput.toJson(bankDTO)

      when:
      def result = post("$path/", jsonBank)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
         company.id == bankDTO.company.id
         number == bankDTO.number
         name == bankDTO.name
         accountNumber == bankDTO.accountNumber
         with(generalLedgerProfitCenter) {
            id == bankDTO.generalLedgerProfitCenter.id
            number == bankDTO.generalLedgerProfitCenter.number
            name == bankDTO.generalLedgerProfitCenter.name
            company.id == bankDTO.generalLedgerProfitCenter.company.id
         }
         with(address) {
            id > 0
            name == bankDTO.address.name
            address1 == bankDTO.address.address1
            address2 == bankDTO.address.address2
            number == bankDTO.address.number
            city == bankDTO.address.city
            country == bankDTO.address.country
            county == bankDTO.address.county
            latitude == bankDTO.address.latitude
            longitude == bankDTO.address.longitude
            postalCode == bankDTO.address.postalCode
            state == bankDTO.address.state
         }
         with(currency) {
            description == bankDTO.currency.description
            id == bankDTO.currency.id
            localizationCode == bankDTO.currency.localizationCode
            value == bankDTO.currency.value
         }
      }
   }

   void "create an invalid bank without number"() {
      given: 'get json bank object and make it invalid'
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final def bankDTO = bankFactoryService.singleDTO( nineNineEightEmployee.company, store)
      // Make invalid json
      def jsonBank = jsonSlurper.parseText(jsonOutput.toJson(bankDTO))
      jsonBank.remove("number")

      when:
      def result = post("$path/", jsonBank)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "Data integrity violation exception"
   }

   void "create an invalid bank without address"() {
      given: 'get json bank object and make it invalid'
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final def bankDTO = bankFactoryService.singleDTO( nineNineEightEmployee.company, store)
      // Make invalid json
      def jsonBank = jsonSlurper.parseText(jsonOutput.toJson(bankDTO))
      jsonBank.remove("address")

      when:
      def result = post("$path/", jsonBank)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 2
      response.path == "address"
      response.message == "Failed to convert argument [bankDTO] for value [null]"
   }

   void "create an invalid bank with non exist currency id"() {
      given: 'get json bank object and make it invalid'
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final def bankDTO = bankFactoryService.singleDTO( nineNineEightEmployee.company, store)
      // Make invalid json
      def jsonBank = jsonSlurper.parseText(jsonOutput.toJson(bankDTO))
      jsonBank.currency.id = 1000

      when:
      def result = post("$path/", jsonBank)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "Data integrity violation exception" // Maybe a more general message would be better
   }

   void "update a valid bank"() {
      given: 'Update existingBank in DB with all new data in jsonBank'
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final def existingBank = bankFactoryService.single( nineNineEightEmployee.company, store)
      final def updatedBankDTO = bankFactoryService.singleDTO( nineNineEightEmployee.company, store)
      def jsonBank = jsonSlurper.parseText(jsonOutput.toJson(updatedBankDTO))
      jsonBank.id = existingBank.id
      jsonBank.address.id = existingBank.address.id

      when:
      def result = put("$path/$existingBank.id", jsonBank)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
         company.id == updatedBankDTO.company.id
         number == updatedBankDTO.number
         name == updatedBankDTO.name
         accountNumber == updatedBankDTO.accountNumber
         with(generalLedgerProfitCenter) {
            id == updatedBankDTO.generalLedgerProfitCenter.id
            number == updatedBankDTO.generalLedgerProfitCenter.number
            name == updatedBankDTO.generalLedgerProfitCenter.name
            company.id == updatedBankDTO.generalLedgerProfitCenter.company.id
         }
         with(address) {
            id > 0
            name == updatedBankDTO.address.name
            address1 == updatedBankDTO.address.address1
            address2 == updatedBankDTO.address.address2
            number == updatedBankDTO.address.number
            city == updatedBankDTO.address.city
            country == updatedBankDTO.address.country
            county == updatedBankDTO.address.county
            latitude == updatedBankDTO.address.latitude
            longitude == updatedBankDTO.address.longitude
            postalCode == updatedBankDTO.address.postalCode
            state == updatedBankDTO.address.state
         }
         with(currency) {
            description == updatedBankDTO.currency.description
            id == updatedBankDTO.currency.id
            localizationCode == updatedBankDTO.currency.localizationCode
            value == updatedBankDTO.currency.value
         }
      }
   }

   void "update a invalid bank without bank name"() {
      given:
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final def bankDTO = bankFactoryService.single( nineNineEightEmployee.company, store)
      def jsonBank = jsonSlurper.parseText(jsonOutput.toJson(bankDTO))
      jsonBank.address.name = ''

      when:
      put("$path/$bankDTO.id", jsonBank)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 2
      response.path == "address.name"
      response.message == "Failed to convert argument [bankDTO] for value [null]" // Maybe a more general message would be better
   }

   void "update a invalid bank with non exist address id"() {
      given:
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final def bankDTO = bankFactoryService.single( nineNineEightEmployee.company, store)
      def jsonBank = jsonSlurper.parseText(jsonOutput.toJson(bankDTO))
      jsonBank.address.id = 1000

      when:
      put("$path/$bankDTO.id", jsonBank)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == INTERNAL_SERVER_ERROR
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "Data access exception"
   }

   void "update a invalid bank without currency"() {
      given:
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final def bankDTO = bankFactoryService.single( nineNineEightEmployee.company, store)
      def jsonBank = jsonSlurper.parseText(jsonOutput.toJson(bankDTO))
      jsonBank.remove("currency")

      when:
      put("$path/$bankDTO.id", jsonBank)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 2
      response.path == "currency"
      response.message == "Failed to convert argument [bankDTO] for value [null]" // Maybe a more general message would be better
   }
}
