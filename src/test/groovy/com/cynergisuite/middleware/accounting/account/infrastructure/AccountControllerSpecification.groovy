package com.cynergisuite.middleware.accounting.account.infrastructure

import com.cynergisuite.domain.SearchPageRequest
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountDataLoaderService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class AccountControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/accounting/account'
   private JsonOutput jsonOutput = new JsonOutput()
   private JsonSlurper jsonSlurper = new JsonSlurper()
   @Inject AccountDataLoaderService accountFactoryService

   void "fetch one account by id" () {
      given:
      accountFactoryService.single(nineNineEightEmployee.company)
      final def account = accountFactoryService.single(nineNineEightEmployee.company)

      when:
      def result = get("$path/${account.id}")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == account.id
         number == account.number
         name == account.name
         form1099Field == account.form1099Field
         corporateAccountIndicator == account.corporateAccountIndicator
         with(type) {
            description == account.type.description
            value == account.type.value
         }
         with(normalAccountBalance) {
            description == account.normalAccountBalance.description
            value == account.normalAccountBalance.value
         }
         with(status) {
            description == account.status.description
            value == account.status.value
         }
         with(type) {
            description == account.type.description
            value == account.type.value
         }
      }
   }

   void "fetch one account by id not found" () {
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
      final def store = storeFactoryService.store(3, nineNineEightEmployee.company)
      accountFactoryService.stream(5, companyFactoryService.forDatasetCode('tstds2'))
      final def accounts = accountFactoryService.stream(12, nineNineEightEmployee.company).toList()
      def pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      def pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      def pageLast = new StandardPageRequest(3, 5, "id", "ASC")
      def pageFour = new StandardPageRequest(4, 5, "id", "ASC")
      def firstPageAccount = accounts[0..4]
      def secondPageAccount = accounts[5..9]
      def lastPageAccount = accounts[10,11]

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
            id == firstPageAccount[index].id
            number == firstPageAccount[index].number
            name == firstPageAccount[index].name
            form1099Field == firstPageAccount[index].form1099Field
            corporateAccountIndicator == firstPageAccount[index].corporateAccountIndicator
            with(type) {
               description == firstPageAccount[index].type.description
               value == firstPageAccount[index].type.value
            }
            with(normalAccountBalance) {
               description == firstPageAccount[index].normalAccountBalance.description
               value == firstPageAccount[index].normalAccountBalance.value
            }
            with(status) {
               description == firstPageAccount[index].status.description
               value == firstPageAccount[index].status.value
            }
            with(type) {
               description == firstPageAccount[index].type.description
               value == firstPageAccount[index].type.value
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
      pageTwoResult.elements.eachWithIndex { result, index ->
         with(result) {
            id == secondPageAccount[index].id
            name == secondPageAccount[index].name
            form1099Field == secondPageAccount[index].form1099Field
            corporateAccountIndicator == secondPageAccount[index].corporateAccountIndicator
            with(type) {
               description == secondPageAccount[index].type.description
               value == secondPageAccount[index].type.value
            }
            with(normalAccountBalance) {
               description == secondPageAccount[index].normalAccountBalance.description
               value == secondPageAccount[index].normalAccountBalance.value
            }
            with(status) {
               description == secondPageAccount[index].status.description
               value == secondPageAccount[index].status.value
            }
            with(type) {
               description == secondPageAccount[index].type.description
               value == secondPageAccount[index].type.value
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
      pageLastResult.elements.eachWithIndex { result, index ->
         with(result) {
            id == lastPageAccount[index].id
            name == lastPageAccount[index].name
            form1099Field == lastPageAccount[index].form1099Field
            corporateAccountIndicator == lastPageAccount[index].corporateAccountIndicator
            with(type) {
               description == lastPageAccount[index].type.description
               value == lastPageAccount[index].type.value
            }
            with(normalAccountBalance) {
               description == lastPageAccount[index].normalAccountBalance.description
               value == lastPageAccount[index].normalAccountBalance.value
            }
            with(status) {
               description == lastPageAccount[index].status.description
               value == lastPageAccount[index].status.value
            }
            with(type) {
               description == lastPageAccount[index].type.description
               value == lastPageAccount[index].type.value
            }
         }

      }

      when:
      get("$path/${pageFour}")

      then:
      final def notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "search accounts by number" () {
      given: "A company and a random collection of 50 accounts with a specific account"
      final company = companyFactoryService.forDatasetCode('tstds1')
      final accounts = accountFactoryService.stream(50, company).collect()
      final queryString = accounts[20].number
      def pageOne = new SearchPageRequest([page:1, size:5, query:"${ queryString }"])

      when: "strict querying for a number"
      def result = get("$path/search?query=${accounts[19].number}&fuzzy=false")

      then: "return single account"
      notThrown(HttpClientException)
      result != null
      with(result) {
         totalElements == 1
         totalPages == 1
         elements.size() == 1
         new AccountDTO(elements[0]) == new AccountDTO(accounts[19])
      }

      when: "fuzzy querying for a number"
      def pageOneResult = get("$path/search?query=${accounts[19].number}")

      then: 'the result for 20 should be [20, 29, 28, 27...21] according to Postgres algorithm for fuzzy search'
      notThrown(HttpClientException)
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements.size() == 10

      with(pageOneResult.elements[0]) {
         id == accounts[19].id
         name == accounts[19].name
         number == accounts[19].number
      }

      when: "Throw SQL Injection at it"
      get("$path/search?query=%20or%201=1;drop%20table%20account;--")

      then:
      def ex = thrown(HttpClientResponseException)
      ex.response.status == NO_CONTENT
   }

   void "search accounts by name" () {
      given: "A company and 3 accounts 2 of them with bank in the name"
      final company = companyFactoryService.forDatasetCode('tstds1')
      final account1 = accountFactoryService.single(company, "East Hill Bank")
      final account2 = accountFactoryService.single(company, "7 Hills Bank and Trust")
      final account3 = accountFactoryService.single(company, "Bob's Credit Union")

      when: "fuzzy querying for a name with bank"
      def result = get("$path/search?query=bank")

      then:
      notThrown(HttpClientException)
      with(result) {
         totalElements == 2
         totalPages == 1
         elements.size() == 2
         elements.collect { new AccountDTO(it) }.sort{ o1, o2 -> o1.id <=> o2.id } == [
            new AccountDTO(account1),
            new AccountDTO(account2)
         ]
      }

      when: "fuzzy searching for band"
      result = get("$path/search?query=band")

      then:
      notThrown(HttpClientException)
      with(result) {
         totalElements == 2
         totalPages == 1
         elements.size() == 2
         elements.collect { new AccountDTO(it) }.sort{ o1, o2 -> o1.id <=> o2.id } == [
            new AccountDTO(account1),
            new AccountDTO(account2)
         ]
      }

      when: "strict searching for bank"
      result = get("$path/search?query=bank&fuzzy=false")

      then:
      notThrown(HttpClientException)
      with(result) {
         totalElements == 2
         totalPages == 1
         elements.size() == 2
         elements.collect { new AccountDTO(it) }.sort{ o1, o2 -> o1.id <=> o2.id } == [
            new AccountDTO(account1),
            new AccountDTO(account2)
         ]
      }

      when: "strict searching for band"
      get("$path/search?query=band&fuzzy=false")

      then:
      def ex = thrown(HttpClientResponseException)
      ex.response.status == NO_CONTENT
   }

   void "search accounts by number and name" () {
      given: "A company and 3 accounts 2 of them with bank in the name"
      final company = companyFactoryService.forDatasetCode('tstds1')
      final account1 = accountFactoryService.single(company, "East Hill Bank")
      final account2 = accountFactoryService.single(company, "7 Hills Bank and Trust")
      final account3 = accountFactoryService.single(company, "Bob's Credit Union")

      when: "fuzzy querying for number and 'bank'"
      def result = get("$path/search?query=${account1.id}%20bank")

      then: "both accounts with 'bank' are returned"
      notThrown(HttpClientException)
      with(result) {
         totalElements == 2
         totalPages == 1
         elements.size() == 2
         elements.collect { new AccountDTO(it) }.sort { o1, o2 -> o1.id <=> o2.id } == [
            new AccountDTO(account1),
            new AccountDTO(account2)
         ]
      }

      when: "strict querying for number and 'bank'"
      result = get("$path/search?query=${account1.id}_bank&fuzzy=false")

      then: "only one account is returned"
      notThrown(HttpClientException)
      with(result) {
         totalElements == 1
         totalPages == 1
         elements.size() == 1
         elements.collect { new AccountDTO(it) }.sort { o1, o2 -> o1.id <=> o2.id } == [
            new AccountDTO(account1)
         ]
      }

      when: "fuzzy querying for number and 'credit'"
      result = get("$path/search?query=${account3.id}%20Credit")

      then: "only one account is returned"
      notThrown(HttpClientException)
      with(result) {
         totalElements == 1
         totalPages == 1
         elements.size() == 1
         elements.collect { new AccountDTO(it) }.sort { o1, o2 -> o1.id <=> o2.id } == [
            new AccountDTO(account3)
         ]
      }

      when: "strict querying for number and full account name"
      result = get("$path/search?query=${account3.id}_Bob's_Credit_Union&fuzzy=false")

      then: "only one account is returned"
      notThrown(HttpClientException)
      with(result) {
         totalElements == 1
         totalPages == 1
         elements.size() == 1
         elements.collect { new AccountDTO(it) }.sort { o1, o2 -> o1.id <=> o2.id } == [
            new AccountDTO(account3)
         ]
      }
   }

   void "create a valid account"() {
      given:
      final def account = accountFactoryService.singleValueObject(nineNineEightEmployee.company)
      final def jsonAccount = jsonOutput.toJson(account)

      when:
      def result = post("$path/", jsonAccount)

      then:
      notThrown(HttpClientResponseException)

      result.number != null
      result.number > 0

      with(result) {
         id > 0
         name == account.name
         form1099Field == account.form1099Field
         corporateAccountIndicator == account.corporateAccountIndicator
         with(type) {
            description == account.type.description
            value == account.type.value
         }
         with(normalAccountBalance) {
            description == account.normalAccountBalance.description
            value == account.normalAccountBalance.value
         }
         with(status) {
            description == account.status.description
            value == account.status.value
         }
         with(type) {
            description == account.type.description
            value == account.type.value
         }
      }
   }

   void "create an invalid account without name"() {
      given: 'get json account object and make it invalid'
      final accountDTO = accountFactoryService.singleValueObject(nineNineEightEmployee.company)
      // Make invalid json
      final jsonAccount = jsonSlurper.parseText(jsonOutput.toJson(accountDTO))
      jsonAccount.remove('name')

      when:
      post("$path/", jsonAccount)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response.path[0] == 'name'
      response.message[0] == 'Is required'
   }

   void "create an invalid account without type"() {
      given: 'get json account object and make it invalid'
      final def accountDTO = accountFactoryService.singleValueObject(nineNineEightEmployee.company)
      // Make invalid json
      def jsonAccount = jsonSlurper.parseText(jsonOutput.toJson(accountDTO))
      jsonAccount.remove('type')

      when:
      post("$path/", jsonAccount)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.path[0] == 'type'
      response.message[0] == 'Is required'
   }

   void "create an invalid account with non exist type value"() {
      given: 'get json account object and make it invalid'
      final def accountDTO = accountFactoryService.singleValueObject(nineNineEightEmployee.company)
      // Make invalid json
      def jsonAccount = jsonSlurper.parseText(jsonOutput.toJson(accountDTO))
      jsonAccount.type.value = 'Invalid'

      when:
      post("$path/", jsonAccount)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'vo.type.value'
      response[0].message == 'Invalid was unable to be found'
   }

   void "update a valid account"() {
      given: 'Update existingAccount in DB with all new data in jsonAccount'
      final def existingAccount = accountFactoryService.single( nineNineEightEmployee.company)
      final def updatedAccountDTO = accountFactoryService.singleValueObject(nineNineEightEmployee.company)
      def jsonAccount = jsonSlurper.parseText(jsonOutput.toJson(updatedAccountDTO))
      jsonAccount.id = existingAccount.id
      jsonAccount.number = existingAccount.number

      when:
      def result = put("$path/$existingAccount.id", jsonAccount)

      then:
      notThrown(HttpClientResponseException)

      result.number != null
      result.number > 0

      with(result) {
         id == existingAccount.id
         name == updatedAccountDTO.name
         form1099Field == updatedAccountDTO.form1099Field
         corporateAccountIndicator == updatedAccountDTO.corporateAccountIndicator
         with(type) {
            description == updatedAccountDTO.type.description
            value == updatedAccountDTO.type.value
         }
         with(normalAccountBalance) {
            description == updatedAccountDTO.normalAccountBalance.description
            value == updatedAccountDTO.normalAccountBalance.value
         }
         with(status) {
            description == updatedAccountDTO.status.description
            value == updatedAccountDTO.status.value
         }
         with(type) {
            description == updatedAccountDTO.type.description
            value == updatedAccountDTO.type.value
         }
      }
   }

   void "update a invalid account without account description"() {
      given:
      final def accountDTO = accountFactoryService.single( nineNineEightEmployee.company)
      def jsonAccount = jsonSlurper.parseText(jsonOutput.toJson(accountDTO))
      jsonAccount.name = ''

      when:
      put("$path/$accountDTO.id", jsonAccount)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.path[0] == 'name'
      response.message[0] == 'Is required' // Maybe a more general message would be better
   }

   void "update a invalid account with non exist status id"() {
      given:
      final accountDTO = accountFactoryService.single( nineNineEightEmployee.company)
      final jsonAccount = jsonSlurper.parseText(jsonOutput.toJson(accountDTO))
      jsonAccount.status.value = 'Z'

      when:
      put("$path/$accountDTO.id", jsonAccount)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'vo.status.value'
      response[0].message == 'Z was unable to be found'
   }


   void "delete account" () {
      given:
      accountFactoryService.single(nineNineEightEmployee.company)
      def account = accountFactoryService.single(nineNineEightEmployee.company)

      when:
      delete( "$path/$account.id", )

      then: "account of for user's company is delete"
      notThrown(HttpClientResponseException)

      when:
      get("$path/$account.id")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "$account.id was unable to be found"
   }

   void "delete account from other company is not allowed" () {
      given:
      def tstds2 = companies.find { it.datasetCode == "tstds2" }
      accountFactoryService.single(nineNineEightEmployee.company)
      def account = accountFactoryService.single(tstds2)

      when:
      delete( "$path/$account.id")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "$account.id was unable to be found"
   }
}
