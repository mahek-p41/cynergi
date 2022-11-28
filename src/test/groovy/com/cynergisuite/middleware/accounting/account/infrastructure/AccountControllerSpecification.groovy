package com.cynergisuite.middleware.accounting.account.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.control.AccountPayableControlTestDataLoaderService
import com.cynergisuite.middleware.accounting.bank.BankFactoryService
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
   @Inject BankFactoryService bankFactoryService

   void "fetch one account by id" () {
      given:
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)

      when:
      def result = get("$path/${account.id}")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == account.id
         number == account.number
         name == account.name
         corporateAccountIndicator == account.corporateAccountIndicator
         bankId == null
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
         with(form1099Field) {
            description == account.form1099Field.description
            value == account.form1099Field.value
         }
      }
   }

   void "fetch one account used by bank" () {
      given:
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      when:
      def result = get("$path/${account.id}")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == account.id
         number == account.number
         name == account.name
         corporateAccountIndicator == account.corporateAccountIndicator
         bankId == bank.id.toString()
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
         with(form1099Field) {
            description == account.form1099Field.description
            value == account.form1099Field.value
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
            corporateAccountIndicator == firstPageAccount[index].corporateAccountIndicator
            bankId == null
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
            with(form1099Field) {
               description == firstPageAccount[index].form1099Field.description
               value == firstPageAccount[index].form1099Field.value
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
            corporateAccountIndicator == secondPageAccount[index].corporateAccountIndicator
            bankId == null
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
            with(form1099Field) {
               description == secondPageAccount[index].form1099Field.description
               value == secondPageAccount[index].form1099Field.value
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
            corporateAccountIndicator == lastPageAccount[index].corporateAccountIndicator
            bankId == null
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
            with(form1099Field) {
               description == lastPageAccount[index].form1099Field.description
               value == lastPageAccount[index].form1099Field.value
            }
         }
      }

      when:
      get("$path/${pageFour}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "fetch all some accounts used by bank" () {
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
      def banks = []
      firstPageAccount.each {
         banks.add(bankFactoryService.single(nineNineEightEmployee.company, store, it))
      }

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
            corporateAccountIndicator == firstPageAccount[index].corporateAccountIndicator
            bankId == banks[index].id.toString()
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
            with(form1099Field) {
               description == firstPageAccount[index].form1099Field.description
               value == firstPageAccount[index].form1099Field.value
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
            corporateAccountIndicator == secondPageAccount[index].corporateAccountIndicator
            bankId == null
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
            with(form1099Field) {
               description == secondPageAccount[index].form1099Field.description
               value == secondPageAccount[index].form1099Field.value
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
            corporateAccountIndicator == lastPageAccount[index].corporateAccountIndicator
            bankId == null
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
            with(form1099Field) {
               description == lastPageAccount[index].form1099Field.description
               value == lastPageAccount[index].form1099Field.value
            }
         }
      }

      when:
      get("$path/${pageFour}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "search accounts with fuzzy searching" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final account1234 = accountDataLoaderService.single(company, "5670 Acct", 1234)
      final account1235 = accountDataLoaderService.single(company, "Bank Acct", 1235)
      final account1236 = accountDataLoaderService.single(company, "Bank of America", 1236)
      final account5678 = accountDataLoaderService.single(company, "Test Acct", 5678)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      if (account1234.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account1234)
      }
      if (account1235.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account1235)
      }
      if (account1236.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account1236)
      }
      if (account5678.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account5678)
      }

      def query = ""
      switch (criteria) {
         case ' 123':
            query = "%20123"
            break
         case '567':
            query = "567"
            break
         case 'bank ':
            query = "bank%20"
            break
         case '5670 Acct':
            query = "5670%20Acct"
            break
         case '5678':
            query = "5678"
            break
      }

      when:
      def result = get("$path/search?query=$query")

      then:
      notThrown(HttpClientException)
      result != null
      result.elements.size() == searchResultsCount
      where:
      criteria       || searchResultsCount
      ' 123'         || 3
      '567'          || 2
      'bank '        || 2
      '5670 Acct'    || 1
      '5678'         || 1
   }

   void "search accounts with fuzzy searching no results found" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final account1234 = accountDataLoaderService.single(company, "5670 Acct", 1234)
      final account1235 = accountDataLoaderService.single(company, "Bank Acct", 1235)
      final account1236 = accountDataLoaderService.single(company, "Bank of America", 1236)
      final account5678 = accountDataLoaderService.single(company, "Test Acct", 5678)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      if (account1234.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account1234)
      }
      if (account1235.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account1235)
      }
      if (account1236.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account1236)
      }
      if (account5678.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account5678)
      }

      def query = ""
      switch (criteria) {
         case 'Acct':
            query = "Acct"
            break
         case '23':
            query = "23"
            break
      }

      when:
      get("$path/search?fuzzy=false&query=$query")

      then:
      def ex = thrown(HttpClientResponseException)
      ex.response.status == response
      where:
      criteria       || response
      'Acct'         || NO_CONTENT
      '23'           || NO_CONTENT
   }

   void "search accounts with strict searching" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final account1234 = accountDataLoaderService.single(company, "5670 Acct", 1234)
      final account1235 = accountDataLoaderService.single(company, "Bank Acct", 1235)
      final account1236 = accountDataLoaderService.single(company, "Bank of America", 1236)
      final account5678 = accountDataLoaderService.single(company, "Test Acct", 5678)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      if (account1234.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account1234)
      }
      if (account1235.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account1235)
      }
      if (account1236.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account1236)
      }
      if (account5678.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account5678)
      }

      def query = ""
      switch (criteria) {
         case '   567  ':
            query = "%20%20%20567%20%20"
            break
         case 'bank':
            query = "bank"
            break
         case '5670 Acct':
            query = "5670%20Acct"
            break
         case '5678':
            query = "5678"
            break
      }

      when:
      def result = get("$path/search?fuzzy=false&query=$query")

      then:
      notThrown(HttpClientException)
      result != null
      result.elements.size() == searchResultsCount
      where:
      criteria       || searchResultsCount
      '   567  '     || 1
      'bank'         || 2
      '5670 Acct'    || 1
      '5678'         || 1
   }

   void "search accounts with strict searching no results found" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final account1234 = accountDataLoaderService.single(company, "5670 Acct", 1234)
      final account1235 = accountDataLoaderService.single(company, "Bank Acct", 1235)
      final account1236 = accountDataLoaderService.single(company, "Bank of America", 1236)
      final account5678 = accountDataLoaderService.single(company, "Test Acct", 5678)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      if (account1234.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account1234)
      }
      if (account1235.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account1235)
      }
      if (account1236.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account1236)
      }
      if (account5678.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account5678)
      }

      def query = ""
      switch (criteria) {
         case '123':
            query = "123"
            break
         case 'Acct':
            query = "Acct"
            break
         case '23':
            query = "23"
            break
      }

      when:
      get("$path/search?fuzzy=false&query=$query")

      then:
      def ex = thrown(HttpClientResponseException)
      ex.response.status == response
      where:
      criteria       || response
      '123'          || NO_CONTENT
      'Acct'         || NO_CONTENT
      '23'           || NO_CONTENT
   }

   void "search accounts by both number and name" () {
      final company = companyFactoryService.forDatasetCode('tstds1')
      final account1235 = accountDataLoaderService.single(company, "Bank Acct", 1235)
      final account1236 = accountDataLoaderService.single(company, "Bank of America", 1236)
      final account5678 = accountDataLoaderService.single(company, "1235-Bank Account", 5678)
      final account5679 = accountDataLoaderService.single(company, "Bank Account - Checking", 5679)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      if (account1235.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account1235)
      }
      if (account1236.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account1236)
      }
      if (account5678.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account5678)
      }
      if (account5679.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account5679)
      }

      def query = ""
      switch (criteria) {
         case '1235 - bank':
            query = "1235%20-%20bank"
            break
         case '1236 - Bank of america':
            query = "1236%20-%20Bank%20of%20america"
            break
         case '5679 - Bank account - Checking':
            query = "5679%20-%20Bank%20account%20-%20Checking"
            break
      }

      when:
      def result = get("$path/search?query=$query")

      then:
      notThrown(HttpClientException)
      result != null
      result.elements.size() == searchResultsCount
      where:
      criteria                               || searchResultsCount
      '1235 - bank'                          || 1
      '1236 - Bank of america'               || 1
      '5679 - Bank account - Checking'       || 1
   }

   void "sort search result for accounts by both number and name" () {
      final company = companyFactoryService.forDatasetCode('tstds1')
      final account1130 = accountDataLoaderService.single(company, "Inventory - TV", 1130)
      final account1105 = accountDataLoaderService.single(company, "Inventory - Audio", 1105)
      final account1118 = accountDataLoaderService.single(company, "Inventory - Computer", 1118)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      if (account1130.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account1130)
      }
      if (account1105.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account1105)
      }
      if (account1118.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account1118)
      }

      when:
      def result = get("$path/search?query=invent")

      then:
      notThrown(HttpClientException)
      result != null
      result.elements.size() == 3
      result.elements[0].name == account1105.name
      result.elements[1].name == account1118.name
      result.elements[2].name == account1130.name
   }

   void "search accounts by both number and name no results found" () {
      final company = companyFactoryService.forDatasetCode('tstds1')
      final account1235 = accountDataLoaderService.single(company, "Bank Acct", 1235)
      final account1236 = accountDataLoaderService.single(company, "Bank of America", 1236)
      final account5678 = accountDataLoaderService.single(company, "1235 - Bank Account", 5678)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      if (account1235.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account1235)
      }
      if (account1236.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account1236)
      }
      if (account5678.bankId != null) {
         bankFactoryService.single(nineNineEightEmployee.company, store, account5678)
      }

      def query = ""
      switch (criteria) {
         case '1234 - bank':
            query = "1234%20-%20bank"
            break
         case '1235 - Banks':
            query = "1235%20-%20Banks"
            break
         case '1235 -Bank':
            query = "1235%20-Bank"
            break
      }

      when:
      get("$path/search?query=$query")

      then:
      def ex = thrown(HttpClientResponseException)
      ex.response.status == response
      where:
      criteria       || response
      '1234 - bank'  || NO_CONTENT
      '1235 - Banks' || NO_CONTENT
      '1235 -Bank'   || NO_CONTENT
   }

   void "search accounts sql injection" () {
      when: "Throw SQL Injection at it"
      get("$path/search?query=%20or%201=1;drop%20table%20account;--")

      then:
      def ex = thrown(HttpClientResponseException)
      ex.response.status == NO_CONTENT
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
         with(form1099Field) {
            description == account.form1099Field.description
            value == account.form1099Field.value
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
         with(form1099Field) {
            description == updatedAccountDTO.form1099Field.description
            value == updatedAccountDTO.form1099Field.value
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

   void "update a invalid account with null 1099 field"() {
      given:
      final existingAccount = accountDataLoaderService.single(nineNineEightEmployee.company)
      final updatedAccountDTO = accountDataLoaderService.singleDTO(nineNineEightEmployee.company)
      updatedAccountDTO.id = existingAccount.id
      updatedAccountDTO.number = existingAccount.number
      updatedAccountDTO.form1099Field = null

      when:
      def result = put("$path/$existingAccount.id", updatedAccountDTO)

      then:
      notThrown(HttpClientResponseException)

      result.number != null
      result.number > 0

      with(result) {
         id == existingAccount.id
         name == updatedAccountDTO.name
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
         with(form1099Field) {
            description == account.form1099Field.description
            value == account.form1099Field.value
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
