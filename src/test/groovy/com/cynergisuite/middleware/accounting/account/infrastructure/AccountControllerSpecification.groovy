package com.cynergisuite.middleware.accounting.account.infrastructure

import com.cynergisuite.domain.SearchPageRequest
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.control.AccountPayableControlTestDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.CONFLICT
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class AccountControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/accounting/account'

   @Inject AccountTestDataLoaderService accountDataLoaderService
   @Inject AccountPayableControlTestDataLoaderService accountPayableControlDataLoaderService

   void "fetch one account by id" () {
      given:
      accountDataLoaderService.single(nineNineEightEmployee.company)
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)

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
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      accountDataLoaderService.stream(5, companyFactoryService.forDatasetCode('tstds2'))
      final accounts = accountDataLoaderService.stream(12, nineNineEightEmployee.company).toList()
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
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "search accounts by number" () {
      given: "A company and a random collection of 50 accounts with a specific account"
      final company = companyFactoryService.forDatasetCode('tstds1')
      final accounts = accountDataLoaderService.stream(50, company).collect()
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
      final account1 = accountDataLoaderService.single(company, "East Hill Bank")
      final account2 = accountDataLoaderService.single(company, "7 Hills Bank and Trust")
      final account3 = accountDataLoaderService.single(company, "Bob's Credit Union")

      when: "fuzzy querying for a name with bank"
      def result = get("$path/search?query=bank")

      then:
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

      when: "fuzzy searching for band"
      result = get("$path/search?query=band")

      then:
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

      when: "strict searching for bank"
      result = get("$path/search?query=bank&fuzzy=false")

      then:
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

      when: "strict searching for band"
      get("$path/search?query=band&fuzzy=false")

      then:
      def ex = thrown(HttpClientResponseException)
      ex.response.status == NO_CONTENT
   }

   void "search accounts by number and name" () {
      given: "A company and 3 accounts 2 of them with bank in the name"
      final company = companyFactoryService.forDatasetCode('tstds1')
      final account1 = accountDataLoaderService.single(company, "East Hill Bank")
      final account2 = accountDataLoaderService.single(company, "7 Hills Bank and Trust")
      final account3 = accountDataLoaderService.single(company, "Bob's Credit Union")

      when: "fuzzy querying for number and 'bank'"
      def result = get("$path/search?query=${account1.number}%20bank")

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
      result = get("$path/search?query=${account1.number}%20bank&fuzzy=false")

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
      result = get("$path/search?query=${account3.number}%20Credit")

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
      result = get("$path/search?query=${account3.number}%20Bob's%20Credit%20Union&fuzzy=false")

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
      final account = accountDataLoaderService.singleDTO(nineNineEightEmployee.company)

      when:
      def result = post("$path/", account)

      then:
      notThrown(HttpClientResponseException)

      result.number != null
      result.number > 0

      with(result) {
         id != null
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

   void "create a valid account with null form 1099 field"() {
      given:
      final account = accountDataLoaderService.singleDTO(nineNineEightEmployee.company)
      account.form1099Field = null

      when:
      def result = post("$path/", account)

      then:
      notThrown(HttpClientResponseException)

      result.number != null
      result.number > 0

      with(result) {
         id != null
         name == account.name
         form1099Field == null
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

   void "create an invalid account without #nonNullableProp"() {
      given: 'get json account object and make it invalid'
      final accountDTO = accountDataLoaderService.singleDTO(nineNineEightEmployee.company)
      accountDTO["$nonNullableProp"] = null

      when:
      post("$path/", accountDTO)

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
      'type'                                       || 'type'
      'normalAccountBalance'                       || 'normalAccountBalance'
      'status'                                     || 'status'
      'corporateAccountIndicator'                  || 'corporateAccountIndicator'
   }

   void "create an invalid account with non exist type value"() {
      given: 'get json account object and make it invalid'
      final accountDTO = accountDataLoaderService.singleDTO(nineNineEightEmployee.company)
      accountDTO.type.value = 'Invalid'

      when:
      post("$path/", accountDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'type.value'
      response[0].message == 'Invalid was unable to be found'
      response[0].code == 'system.not.found'
   }

   void "create an invalid account with duplicate account number"() {
      given:
      final existingAccount = accountDataLoaderService.single(nineNineEightEmployee.company)
      final accountDTO = accountDataLoaderService.singleDTO(nineNineEightEmployee.company)
      accountDTO.number = existingAccount.number

      when:
      post("$path/", accountDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'number'
      response[0].message == "$existingAccount.number already exists"
      response[0].code == 'cynergi.validation.duplicate'
   }

   void "update a valid account"() {
      given: 'Update existingAccount in DB with all new data'
      final existingAccount = accountDataLoaderService.single(nineNineEightEmployee.company)
      final updatedAccountDTO = accountDataLoaderService.singleDTO(nineNineEightEmployee.company)
      updatedAccountDTO.id = existingAccount.id
      updatedAccountDTO.number = existingAccount.number

      when:
      def result = put("$path/$existingAccount.id", updatedAccountDTO)

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

   void "update a invalid account without non-nullable properties"() {
      given:
      final existingAccount = accountDataLoaderService.single(nineNineEightEmployee.company)
      def accountDTO = accountDataLoaderService.singleDTO(nineNineEightEmployee.company)
      accountDTO.name = null
      accountDTO.type = null
      accountDTO.normalAccountBalance = null
      accountDTO.status = null
      accountDTO.corporateAccountIndicator = null

      when:
      put("$path/$existingAccount.id", accountDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson().collect().sort { a,b -> a.path <=> b.path }
      response.size() == 5
      response[0].path == 'corporateAccountIndicator'
      response[1].path == 'name'
      response[2].path == 'normalAccountBalance'
      response[3].path == 'status'
      response[4].path == 'type'
      response.collect { it.message } as Set == ['Is required'] as Set
   }

   void "update a invalid account with non exist status id"() {
      given:
      final existingAccount = accountDataLoaderService.single(nineNineEightEmployee.company)
      def accountDTO = accountDataLoaderService.singleDTO(nineNineEightEmployee.company)
      accountDTO.status.value = 'Z'

      when:
      put("$path/$existingAccount.id", accountDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'status.value'
      response[0].message == 'Z was unable to be found'
      response[0].code == 'system.not.found'
   }

   void "update a invalid account with duplicate account number"() {
      given:
      final existingAccounts = accountDataLoaderService.stream(2, nineNineEightEmployee.company).collect()
      def accountDTO = accountDataLoaderService.singleDTO(nineNineEightEmployee.company)
      accountDTO.id = existingAccounts[0].id
      accountDTO.number = existingAccounts[1].number

      when:
      put("$path/$accountDTO.id", accountDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'number'
      response[0].message == "$accountDTO.number already exists"
      response[0].code == 'cynergi.validation.duplicate'
   }

   void "delete account" () {
      given:
      accountDataLoaderService.single(nineNineEightEmployee.company)
      def account = accountDataLoaderService.single(nineNineEightEmployee.company)

      when:
      delete("$path/$account.id", )

      then: "account of for user's company is deleted"
      notThrown(HttpClientResponseException)

      when:
      get("$path/$account.id")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$account.id was unable to be found"
      response.code == 'system.not.found'
   }

   void "delete account still has references" () {
      given:
      accountDataLoaderService.single(nineNineEightEmployee.company)
      final glInvCleAcct = accountDataLoaderService.single(nineNineEightEmployee.company)
      final glInvAcct = accountDataLoaderService.single(nineNineEightEmployee.company)
      accountPayableControlDataLoaderService.single(nineNineEightEmployee.company, glInvCleAcct, glInvAcct)

      when:
      delete("$path/$glInvCleAcct.id", )

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == CONFLICT
      def response = exception.response.bodyAsJson()
      response.message == "Requested operation violates data integrity"
      response.code == "cynergi.data.constraint.violated"

      when:
      delete("$path/$glInvAcct.id", )

      then:
      def exception2 = thrown(HttpClientResponseException)
      exception2.response.status == CONFLICT
      def response2 = exception2.response.bodyAsJson()
      response2.message == "Requested operation violates data integrity"
      response2.code == "cynergi.data.constraint.violated"
   }

   void "delete account from other company is not allowed" () {
      given:
      def tstds2 = companies.find { it.datasetCode == "tstds2" }
      accountDataLoaderService.single(nineNineEightEmployee.company)
      def account = accountDataLoaderService.single(tstds2)

      when:
      delete("$path/$account.id")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$account.id was unable to be found"
      response.code == 'system.not.found'
   }

   void "recreate deleted account" () {
      given:
      final account = accountDataLoaderService.singleDTO(nineNineEightEmployee.company)

      when: // create an account
      def response1 = post("$path/", account)

      then:
      notThrown(HttpClientResponseException)

      response1.number != null
      response1.number > 0

      with(response1) {
         id != null
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

      when: // delete account
      delete("$path/$response1.id")

      then: "account of user's company is deleted"
      notThrown(HttpClientResponseException)

      when: // recreate account
      def response2 = post("$path/", account)

      then:
      notThrown(HttpClientResponseException)

      response2.number != null
      response2.number > 0

      with(response2) {
         id != null
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

      when: // delete account again
      delete("$path/$response2.id")

      then: "account of user's company is deleted"
      notThrown(HttpClientResponseException)
   }
}
