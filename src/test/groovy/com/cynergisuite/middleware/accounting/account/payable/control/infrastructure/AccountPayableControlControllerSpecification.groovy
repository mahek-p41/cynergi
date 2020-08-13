package com.cynergisuite.middleware.accounting.account.payable.control.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.control.AccountPayableControlDataLoader.AccountPayableControlDataLoaderService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class AccountPayableControlControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/accounting/account/payable/control'
   private JsonOutput jsonOutput = new JsonOutput()
   private JsonSlurper jsonSlurper = new JsonSlurper()
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
      final def jsonAPControl = jsonOutput.toJson(accountPayableControl)

      when:
      def result = post("$path/", jsonAPControl)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
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

      def jsonAPControl = jsonSlurper.parseText(jsonOutput.toJson(accountPayableControl))

      when:
      post("$path/", jsonAPControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "company"
      response[0].message == "Account payable control for user's company " + company.myDataset() + " already exists"
   }

   void "create invalid account payable control without pay after discount date" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def accountPayableControl = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      //Make invalid json
      def jsonAPControl = jsonSlurper.parseText(jsonOutput.toJson(accountPayableControl))
      jsonAPControl.remove("payAfterDiscountDate")

      when:
      post("$path/", jsonAPControl)

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
      //Make invalid json
      def jsonAPControl = jsonSlurper.parseText(jsonOutput.toJson(accountPayableControl))
      jsonAPControl.remove("resetExpense")

      when:
      post("$path/", jsonAPControl)

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
      //Make invalid json
      def jsonAPControl = jsonSlurper.parseText(jsonOutput.toJson(accountPayableControl))
      jsonAPControl.remove("useRebatesIndicator")

      when:
      post("$path/", jsonAPControl)

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
      //Make invalid json
      def jsonAPControl = jsonSlurper.parseText(jsonOutput.toJson(accountPayableControl))
      jsonAPControl.remove("tradeCompanyIndicator")

      when:
      post("$path/", jsonAPControl)

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
      //Make invalid json
      def jsonAPControl = jsonSlurper.parseText(jsonOutput.toJson(accountPayableControl))
      jsonAPControl.remove("printCurrencyIndicatorType")

      when:
      post("$path/", jsonAPControl)

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
      //Make invalid json
      def jsonAPControl = jsonSlurper.parseText(jsonOutput.toJson(accountPayableControl))
      jsonAPControl.remove("lockInventoryIndicator")

      when:
      post("$path/", jsonAPControl)

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
      //Make invalid json
      def jsonAPControl = jsonSlurper.parseText(jsonOutput.toJson(accountPayableControl))
      jsonAPControl.remove("purchaseOrderNumberRequiredIndicatorType")

      when:
      post("$path/", jsonAPControl)

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
      //Make invalid json
      def jsonAPControl = jsonSlurper.parseText(jsonOutput.toJson(accountPayableControl))
      jsonAPControl.remove("generalLedgerInventoryClearingAccount")

      when:
      post("$path/", jsonAPControl)

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
      //Make invalid json
      def jsonAPControl = jsonSlurper.parseText(jsonOutput.toJson(accountPayableControl))
      jsonAPControl.generalLedgerInventoryClearingAccount.id = '0'

      when:
      post("$path/", jsonAPControl)

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
      //Make invalid json
      def jsonAPControl = jsonSlurper.parseText(jsonOutput.toJson(accountPayableControl))
      jsonAPControl.remove("generalLedgerInventoryAccount")

      when:
      post("$path/", jsonAPControl)

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
      //Make invalid json
      def jsonAPControl = jsonSlurper.parseText(jsonOutput.toJson(accountPayableControl))
      jsonAPControl.generalLedgerInventoryAccount.id = '0'

      when:
      post("$path/", jsonAPControl)

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
      final def jsonAPControl = jsonSlurper.parseText(jsonOutput.toJson(updatedAPControlDTO))
      jsonAPControl.id = existingAPControl.id

      when:
      def result = put("$path/$existingAPControl.id", jsonAPControl)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == existingAPControl.id
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
      final def jsonAPControl = jsonSlurper.parseText(jsonOutput.toJson(updatedAPControlDTO))
      jsonAPControl.id = '0'

      when:
      put("$path/$existingAPControl.id", jsonAPControl)

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
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      accountPayableControlDataLoaderService.single(company, glInvCleAcct, glInvAcct)
      final def updatedAPControlDTO = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      final def jsonAPControl = jsonSlurper.parseText(jsonOutput.toJson(updatedAPControlDTO))

      when:
      put("$path/99", jsonAPControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "id"
      response[0].message == "99 was unable to be found"
   }

   void "update invalid account payable control by removing print currency indicator type" () {
      given:
      final company = nineNineEightEmployee.company
      final glInvCleAcct = accountDataLoaderService.single(company)
      final glInvAcct = accountDataLoaderService.single(company)
      final def existingAPControl = accountPayableControlDataLoaderService.single(company, glInvCleAcct, glInvAcct)
      final def updatedAPControlDTO = accountPayableControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(glInvCleAcct.myId()), new SimpleIdentifiableDTO(glInvAcct.myId()))
      final def jsonAPControl = jsonSlurper.parseText(jsonOutput.toJson(updatedAPControlDTO))
      jsonAPControl.remove("printCurrencyIndicatorType")

      when:
      put("$path/$existingAPControl.id", jsonAPControl)

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
      final def jsonAPControl = jsonSlurper.parseText(jsonOutput.toJson(updatedAPControlDTO))
      jsonAPControl.remove("purchaseOrderNumberRequiredIndicatorType")

      when:
      put("$path/$existingAPControl.id", jsonAPControl)

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
      final def jsonAPControl = jsonSlurper.parseText(jsonOutput.toJson(updatedAPControlDTO))
      jsonAPControl.remove("generalLedgerInventoryClearingAccount")

      when:
      put("$path/$existingAPControl.id", jsonAPControl)

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
      final def jsonAPControl = jsonSlurper.parseText(jsonOutput.toJson(updatedAPControlDTO))
      jsonAPControl.remove("generalLedgerInventoryAccount")

      when:
      put("$path/$existingAPControl.id", jsonAPControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "generalLedgerInventoryAccount"
      response[0].message == "Is required"
   }
}
