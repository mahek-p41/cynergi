package com.cynergisuite.middleware.accounting.account.payable.control.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableCheckFormTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableCheckFormTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.control.AccountPayableControlDataLoaderService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class AccountPayableControlControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/accounting/account-payable/control'
   private jsonOutput = new JsonOutput()
   private jsonSlurper = new JsonSlurper()

   @Inject AccountDataLoaderService accountDataLoaderService
   @Inject AccountPayableControlDataLoaderService accountPayableControlDataLoaderService

   void "fetch one account payable control by company" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def accountPayableControl = accountPayableControlDataLoaderService.single(company, glInvCleAcct, glInvAcct)

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
         generalLedgerInventoryAccount.id == accountPayableControl.generalLedgerInventoryAccount.id
      }
   }

   void "fetch one account payable control by company not found" () {
      when:
      get("$path/")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "Account payable control record of the company was unable to be found"

   }

   void "create valid account payable control" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))

      when:
      def result = post("$path/", accountPayableControl)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0

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
         generalLedgerInventoryAccount.id == accountPayableControl.generalLedgerInventoryAccount.id
      }
   }

   void "create invalid account payable control for company with existing record" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def accountPayableControl = accountPayableControlDataLoaderService.single(company, glInvCleAcct, glInvAcct)

      when:
      post("$path/", accountPayableControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "company"
      response[0].message == "${company.myDataset()} already exists"
   }

   void "create invalid account payable control without check form type" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      accountPayableControl.checkFormType = null

      when:
      post("$path/", accountPayableControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "checkFormType"
      response[0].message == "Is required"
   }

   void "create invalid account payable control with non-existing check form type value" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
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
   }

   void "create invalid account payable control with null value of check form type" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      final accountPayableControlJson = jsonSlurper.parseText(jsonOutput.toJson(accountPayableControl))
      accountPayableControlJson.checkFormType.value = null

      when:
      post("$path/", accountPayableControlJson)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 2
      response.path == "checkFormType.value"
      response.message == "Failed to convert argument [dto] for value [null]"
   }

   void "create invalid account payable control without pay after discount date" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      accountPayableControl.payAfterDiscountDate = null

      when:
      post("$path/", accountPayableControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "payAfterDiscountDate"
      response[0].message == "Is required"
   }

   void "create invalid account payable control without reset expense" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      accountPayableControl.resetExpense = null

      when:
      post("$path/", accountPayableControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "resetExpense"
      response[0].message == "Is required"
   }

   void "create invalid account payable control without use rebates indicator" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      accountPayableControl.useRebatesIndicator = null

      when:
      post("$path/", accountPayableControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "useRebatesIndicator"
      response[0].message == "Is required"
   }

   void "create invalid account payable control without trade company indicator" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      accountPayableControl.tradeCompanyIndicator = null

      when:
      post("$path/", accountPayableControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "tradeCompanyIndicator"
      response[0].message == "Is required"
   }

   void "create invalid account payable control without print currency indicator type" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      accountPayableControl.printCurrencyIndicatorType = null

      when:
      post("$path/", accountPayableControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "printCurrencyIndicatorType"
      response[0].message == "Is required"
   }

   void "create invalid account payable control without lock inventory indicator" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      accountPayableControl.lockInventoryIndicator = null

      when:
      post("$path/", accountPayableControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "lockInventoryIndicator"
      response[0].message == "Is required"
   }

   void "create invalid account payable control without purchase order number required indicator type" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      accountPayableControl.purchaseOrderNumberRequiredIndicatorType = null

      when:
      post("$path/", accountPayableControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "purchaseOrderNumberRequiredIndicatorType"
      response[0].message == "Is required"
   }

   void "create invalid account payable control without general ledger inventory clearing account" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      accountPayableControl.generalLedgerInventoryClearingAccount = null

      when:
      post("$path/", accountPayableControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "generalLedgerInventoryClearingAccount"
      response[0].message == "Is required"
   }

   void "create invalid account payable control with non-existing general ledger inventory clearing account id" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      accountPayableControl.generalLedgerInventoryClearingAccount.id = 0

      when:
      post("$path/", accountPayableControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "generalLedgerInventoryClearingAccount.id"
      response[0].message == "0 was unable to be found"
   }

   void "create invalid account payable control without general ledger inventory account" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      accountPayableControl.generalLedgerInventoryAccount = null

      when:
      post("$path/", accountPayableControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "generalLedgerInventoryAccount"
      response[0].message == "Is required"
   }

   void "create invalid account payable control with non-existing general ledger inventory account id" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      accountPayableControl.generalLedgerInventoryAccount.id = 0

      when:
      post("$path/", accountPayableControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "generalLedgerInventoryAccount.id"
      response[0].message == "0 was unable to be found"
   }

   void "update valid account payable control by id" () {
      given: "update existingAPControl in db with all new data in jsonAPControl"
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def existingAPControl = accountPayableControlDataLoaderService.single(company, glInvCleAcct, glInvAcct)
      final def updatedAPControlDTO = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
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
         generalLedgerInventoryAccount.id == updatedAPControlDTO.generalLedgerInventoryAccount.id
      }
   }

   void "update invalid account payable control with id 0" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def existingAPControl = accountPayableControlDataLoaderService.single(company, glInvCleAcct, glInvAcct)
      final def updatedAPControlDTO = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      updatedAPControlDTO.id = 0

      when:
      put("$path", updatedAPControlDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "id"
      response[0].message == "id must be greater than zero"
   }

   void "update invalid account payable control with non-existing id" () {
      given:
      final company2 = companyFactoryService.forDatasetCode('tstds2') // load the company that isn't logged in to create the control record
      final glInvCleAcct = accountDataLoaderService.single(company2)
      final glInvAcct = accountDataLoaderService.single(company2)
      accountPayableControlDataLoaderService.single(company2, glInvCleAcct, glInvAcct)
      final updatedAPControlDTO = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))

      when:
      put("$path", updatedAPControlDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${nineNineEightEmployee.company.myId()} was unable to be found"
   }

   void "update invalid account payable control by removing check form type" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def existingAPControl = accountPayableControlDataLoaderService.single(company, glInvCleAcct, glInvAcct)
      final def updatedAPControlDTO = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      updatedAPControlDTO.checkFormType = null

      when:
      put("$path", updatedAPControlDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "checkFormType"
      response[0].message == "Is required"
   }

   void "update invalid account payable control by removing print currency indicator type" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def existingAPControl = accountPayableControlDataLoaderService.single(company, glInvCleAcct, glInvAcct)
      final def updatedAPControlDTO = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      updatedAPControlDTO.printCurrencyIndicatorType = null

      when:
      put("$path", updatedAPControlDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "printCurrencyIndicatorType"
      response[0].message == "Is required"
   }

   void "update invalid account payable control by removing purchase order number required indicator type"() {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def existingAPControl = accountPayableControlDataLoaderService.single(company, glInvCleAcct, glInvAcct)
      final def updatedAPControlDTO = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      updatedAPControlDTO.purchaseOrderNumberRequiredIndicatorType = null

      when:
      put("$path", updatedAPControlDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "purchaseOrderNumberRequiredIndicatorType"
      response[0].message == "Is required"
   }

   void "update invalid account payable control by removing general ledger inventory clearing account" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def existingAPControl = accountPayableControlDataLoaderService.single(company, glInvCleAcct, glInvAcct)
      final def updatedAPControlDTO = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      updatedAPControlDTO.generalLedgerInventoryClearingAccount = null

      when:
      put("$path", updatedAPControlDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "generalLedgerInventoryClearingAccount"
      response[0].message == "Is required"
   }

   void "update invalid account payable control by removing general ledger inventory account" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def existingAPControl = accountPayableControlDataLoaderService.single(company, glInvCleAcct, glInvAcct)
      final def updatedAPControlDTO = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      updatedAPControlDTO.generalLedgerInventoryAccount = null

      when:
      put("$path", updatedAPControlDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "generalLedgerInventoryAccount"
      response[0].message == "Is required"
   }
}
