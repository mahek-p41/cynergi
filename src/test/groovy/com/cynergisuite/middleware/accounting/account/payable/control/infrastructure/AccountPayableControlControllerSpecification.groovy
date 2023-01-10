package com.cynergisuite.middleware.accounting.account.payable.control.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableCheckFormTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableCheckFormTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.control.AccountPayableControlTestDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import jakarta.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class AccountPayableControlControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/accounting/account-payable/control'

   @Inject AccountTestDataLoaderService accountDataLoaderService
   @Inject AccountPayableControlTestDataLoaderService accountPayableControlDataLoaderService

   void "fetch one account payable control by company" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final accountPayableControl = accountPayableControlDataLoaderService.single(company, glInvCleAcct, glInvAcct)

      when:
      def result = get("$path/")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == accountPayableControl.id

         with(checkFormType) {
            value == accountPayableControl.checkFormType.value
            description == accountPayableControl.checkFormType.description
         }

         payAfterDiscountDate == accountPayableControl.payAfterDiscountDate
         resetExpense == accountPayableControl.resetExpense
         useRebatesIndicator == accountPayableControl.useRebatesIndicator
         tradeCompanyIndicator == accountPayableControl.tradeCompanyIndicator

         with(printCurrencyIndicatorType) {
            value == accountPayableControl.printCurrencyIndicatorType.value
            description == accountPayableControl.printCurrencyIndicatorType.description
         }

         lockInventoryIndicator == accountPayableControl.lockInventoryIndicator

         with(purchaseOrderNumberRequiredIndicatorType) {
            value == accountPayableControl.purchaseOrderNumberRequiredIndicatorType.value
            description == accountPayableControl.purchaseOrderNumberRequiredIndicatorType.description
         }

         generalLedgerInventoryClearingAccount.id == accountPayableControl.generalLedgerInventoryClearingAccount.id
         generalLedgerInventoryClearingAccount.name == accountPayableControl.generalLedgerInventoryClearingAccount.name
         generalLedgerInventoryAccount.id == accountPayableControl.generalLedgerInventoryAccount.id
         generalLedgerInventoryAccount.name == accountPayableControl.generalLedgerInventoryAccount.name
      }
   }

   void "fetch one account payable control by company not found" () {
      when:
      get("$path/")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "Account payable control record of the company was unable to be found"
      response.code == 'system.not.found'

   }

   void "create valid account payable control" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new AccountDTO(glInvCleAcct), new AccountDTO(glInvAcct))

      when:
      def result = post("$path/", accountPayableControl)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id != null

         with(checkFormType) {
            value == accountPayableControl.checkFormType.value
            description == accountPayableControl.checkFormType.description
         }

         payAfterDiscountDate == accountPayableControl.payAfterDiscountDate
         resetExpense == accountPayableControl.resetExpense
         useRebatesIndicator == accountPayableControl.useRebatesIndicator
         tradeCompanyIndicator == accountPayableControl.tradeCompanyIndicator

         with(printCurrencyIndicatorType) {
            value == accountPayableControl.printCurrencyIndicatorType.value
            description == accountPayableControl.printCurrencyIndicatorType.description
         }

         lockInventoryIndicator == accountPayableControl.lockInventoryIndicator

         with(purchaseOrderNumberRequiredIndicatorType) {
            value == accountPayableControl.purchaseOrderNumberRequiredIndicatorType.value
            description == accountPayableControl.purchaseOrderNumberRequiredIndicatorType.description
         }

         generalLedgerInventoryClearingAccount.id == accountPayableControl.generalLedgerInventoryClearingAccount.id
         generalLedgerInventoryClearingAccount.name == accountPayableControl.generalLedgerInventoryClearingAccount.name
         generalLedgerInventoryAccount.id == accountPayableControl.generalLedgerInventoryAccount.id
         generalLedgerInventoryAccount.name == accountPayableControl.generalLedgerInventoryAccount.name
      }
   }

   void "create invalid account payable control for company with existing record" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final accountPayableControl = accountPayableControlDataLoaderService.single(company, glInvCleAcct, glInvAcct)

      when:
      post("$path/", accountPayableControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "company"
      response[0].message == "${company.datasetCode} already exists"
      response[0].code == "cynergi.validation.config.exists"
   }

   @Unroll
   void "create invalid account payable control without #nonNullableProp" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new AccountDTO(glInvCleAcct), new AccountDTO(glInvAcct))
      accountPayableControl["$nonNullableProp"] = null

      when:
      post("$path/", accountPayableControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'
      response[0].code == 'javax.validation.constraints.NotNull.message'

      where:
      nonNullableProp                              || errorResponsePath
      'checkFormType'                              || 'checkFormType'
      'generalLedgerInventoryAccount'              || 'generalLedgerInventoryAccount'
      'generalLedgerInventoryClearingAccount'      || 'generalLedgerInventoryClearingAccount'
      'lockInventoryIndicator'                     || 'lockInventoryIndicator'
      'payAfterDiscountDate'                       || 'payAfterDiscountDate'
      'printCurrencyIndicatorType'                 || 'printCurrencyIndicatorType'
      'purchaseOrderNumberRequiredIndicatorType'   || 'purchaseOrderNumberRequiredIndicatorType'
      'resetExpense'                               || 'resetExpense'
      'tradeCompanyIndicator'                      || 'tradeCompanyIndicator'
      'useRebatesIndicator'                        || 'useRebatesIndicator'
   }

   void "create invalid account payable control with non-existing check form type value" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new AccountDTO(glInvCleAcct), new AccountDTO(glInvAcct))
      accountPayableControl.checkFormType = new AccountPayableCheckFormTypeDTO(AccountPayableCheckFormTypeDataLoader.random())
      accountPayableControl.checkFormType.value = "nonexisting"

      when:
      post("$path/", accountPayableControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "checkFormType.value"
      response[0].message == "nonexisting was unable to be found"
      response[0].code == 'system.not.found'
   }

   void "create invalid account payable control with non-existing general ledger inventory clearing account id" () {
      given:
      final nonExistentGeneralLedgerInventoryClearingAccountId = UUID.randomUUID()
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new AccountDTO(glInvCleAcct), new AccountDTO(glInvAcct))
      accountPayableControl.generalLedgerInventoryClearingAccount.id = nonExistentGeneralLedgerInventoryClearingAccountId

      when:
      post("$path/", accountPayableControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "generalLedgerInventoryClearingAccount.id"
      response[0].message == "$nonExistentGeneralLedgerInventoryClearingAccountId was unable to be found"
      response[0].code == 'system.not.found'
   }

   void "create invalid account payable control with non-existing general ledger inventory account id" () {
      given:
      final nonExistentGeneralLedgerInventoryAccountId = UUID.randomUUID()
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new AccountDTO(glInvCleAcct), new AccountDTO(glInvAcct))
      accountPayableControl.generalLedgerInventoryAccount.id = nonExistentGeneralLedgerInventoryAccountId

      when:
      post("$path/", accountPayableControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "generalLedgerInventoryAccount.id"
      response[0].message == "$nonExistentGeneralLedgerInventoryAccountId was unable to be found"
      response[0].code == 'system.not.found'
   }

   void "update valid account payable control by id" () {
      given: "update existingAPControl in db with all new data in jsonAPControl"
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final existingAPControl = accountPayableControlDataLoaderService.single(company, glInvCleAcct, glInvAcct)
      final updatedAPControlDTO = accountPayableControlDataLoaderService.singleDTO(new AccountDTO(glInvCleAcct), new AccountDTO(glInvAcct))
      updatedAPControlDTO.id = existingAPControl.id

      when:
      def result = put("$path", updatedAPControlDTO)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == existingAPControl.id

         with(checkFormType) {
            value == updatedAPControlDTO.checkFormType.value
            description == updatedAPControlDTO.checkFormType.description
         }

         payAfterDiscountDate == updatedAPControlDTO.payAfterDiscountDate
         resetExpense == updatedAPControlDTO.resetExpense
         useRebatesIndicator == updatedAPControlDTO.useRebatesIndicator
         tradeCompanyIndicator == updatedAPControlDTO.tradeCompanyIndicator

         with(printCurrencyIndicatorType) {
            value == updatedAPControlDTO.printCurrencyIndicatorType.value
            description == updatedAPControlDTO.printCurrencyIndicatorType.description
         }

         lockInventoryIndicator == updatedAPControlDTO.lockInventoryIndicator

         with(purchaseOrderNumberRequiredIndicatorType) {
            value == updatedAPControlDTO.purchaseOrderNumberRequiredIndicatorType.value
            description == updatedAPControlDTO.purchaseOrderNumberRequiredIndicatorType.description
         }

         generalLedgerInventoryClearingAccount.id == updatedAPControlDTO.generalLedgerInventoryClearingAccount.id
         generalLedgerInventoryClearingAccount.name == updatedAPControlDTO.generalLedgerInventoryClearingAccount.name
         generalLedgerInventoryAccount.id == updatedAPControlDTO.generalLedgerInventoryAccount.id
         generalLedgerInventoryAccount.name == updatedAPControlDTO.generalLedgerInventoryAccount.name
      }
   }

   void "update account payable control with invalid id in payload" () {
      given:
      final nonExistentAPControlId = UUID.randomUUID()
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final existingAPControl = accountPayableControlDataLoaderService.single(company, glInvCleAcct, glInvAcct)
      final updatedAPControlDTO = accountPayableControlDataLoaderService.singleDTO(new AccountDTO(glInvCleAcct), new AccountDTO(glInvAcct))
      updatedAPControlDTO.id = nonExistentAPControlId

      when: "sending a payload with an invalid id"
      def result = put("$path", updatedAPControlDTO)

      then: "the id should be ignored and the entity associated with the company of the user should be used"
      notThrown(HttpClientResponseException)
      with(result) {
         id == existingAPControl.id

         with(checkFormType) {
            value == updatedAPControlDTO.checkFormType.value
            description == updatedAPControlDTO.checkFormType.description
         }

         payAfterDiscountDate == updatedAPControlDTO.payAfterDiscountDate
         resetExpense == updatedAPControlDTO.resetExpense
         useRebatesIndicator == updatedAPControlDTO.useRebatesIndicator
         tradeCompanyIndicator == updatedAPControlDTO.tradeCompanyIndicator

         with(printCurrencyIndicatorType) {
            value == updatedAPControlDTO.printCurrencyIndicatorType.value
            description == updatedAPControlDTO.printCurrencyIndicatorType.description
         }

         lockInventoryIndicator == updatedAPControlDTO.lockInventoryIndicator

         with(purchaseOrderNumberRequiredIndicatorType) {
            value == updatedAPControlDTO.purchaseOrderNumberRequiredIndicatorType.value
            description == updatedAPControlDTO.purchaseOrderNumberRequiredIndicatorType.description
         }

         generalLedgerInventoryClearingAccount.id == updatedAPControlDTO.generalLedgerInventoryClearingAccount.id
         generalLedgerInventoryClearingAccount.name == updatedAPControlDTO.generalLedgerInventoryClearingAccount.name
         generalLedgerInventoryAccount.id == updatedAPControlDTO.generalLedgerInventoryAccount.id
         generalLedgerInventoryAccount.name == updatedAPControlDTO.generalLedgerInventoryAccount.name
      }
   }

   void "update invalid account payable control with non-existing id" () {
      given:
      final company2 = companyFactoryService.forDatasetCode('corrto') // load the company that isn't logged in to create the control record
      final glInvCleAcct = accountDataLoaderService.single(company2)
      final glInvAcct = accountDataLoaderService.single(company2)
      accountPayableControlDataLoaderService.single(company2, glInvCleAcct, glInvAcct)
      final updatedAPControlDTO = accountPayableControlDataLoaderService.singleDTO(new AccountDTO(glInvCleAcct), new AccountDTO(glInvAcct))

      when:
      put("$path", updatedAPControlDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${nineNineEightEmployee.company.id} was unable to be found"
      response.code == 'system.not.found'
   }

   void "update invalid account payable control without non-nullable properties" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      accountPayableControlDataLoaderService.single(company, glInvCleAcct, glInvAcct)
      def updatedAPControlDTO = accountPayableControlDataLoaderService.singleDTO(new AccountDTO(glInvCleAcct), new AccountDTO(glInvAcct))
      updatedAPControlDTO.checkFormType = null
      updatedAPControlDTO.generalLedgerInventoryAccount = null
      updatedAPControlDTO.generalLedgerInventoryClearingAccount = null
      updatedAPControlDTO.lockInventoryIndicator = null
      updatedAPControlDTO.payAfterDiscountDate = null

      updatedAPControlDTO.resetExpense = null
      updatedAPControlDTO.printCurrencyIndicatorType = null
      updatedAPControlDTO.purchaseOrderNumberRequiredIndicatorType = null
      updatedAPControlDTO.tradeCompanyIndicator = null
      updatedAPControlDTO.useRebatesIndicator = null


      when:
      put("$path", updatedAPControlDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson().collect().sort { a,b -> a.path <=> b.path }
      response.size() == 10
      response[0].path == 'checkFormType'
      response[1].path == 'generalLedgerInventoryAccount'
      response[2].path == 'generalLedgerInventoryClearingAccount'
      response[3].path == 'lockInventoryIndicator'
      response[4].path == 'payAfterDiscountDate'
      response[5].path == 'printCurrencyIndicatorType'
      response[6].path == 'purchaseOrderNumberRequiredIndicatorType'
      response[7].path == 'resetExpense'
      response[8].path == 'tradeCompanyIndicator'
      response[9].path == 'useRebatesIndicator'
      response.collect { it.message } as Set == ['Is required'] as Set
   }
}
