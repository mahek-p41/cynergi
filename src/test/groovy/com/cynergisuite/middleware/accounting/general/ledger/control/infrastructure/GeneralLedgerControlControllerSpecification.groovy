package com.cynergisuite.middleware.accounting.general.ledger.control.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.control.GeneralLedgerControlDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject
import java.time.LocalDate

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static java.time.Month.FEBRUARY
import static java.time.Month.JANUARY

@MicronautTest(transactional = false)
class GeneralLedgerControlControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/general-ledger/control'

   @Inject AccountDataLoaderService accountDataLoaderService
   @Inject GeneralLedgerControlDataLoaderService generalLedgerControlDataLoaderService

   void "fetch one general ledger control by company" () {
      given:
      final company = nineNineEightEmployee.company
      final defProfitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final defAPAcct = accountDataLoaderService.single(company)
      final defAPDiscAcct = accountDataLoaderService.single(company)
      final defARAcct = accountDataLoaderService.single(company)
      final defARDiscAcct = accountDataLoaderService.single(company)
      final defAcctMiscInvAcct = accountDataLoaderService.single(company)
      final defAcctSerializedInvAcct = accountDataLoaderService.single(company)
      final defAcctUnbilledInvAcct = accountDataLoaderService.single(company)
      final defAcctFreightAcct = accountDataLoaderService.single(company)
      final def generalLedgerControl = generalLedgerControlDataLoaderService.single(
         company,
         null,
         null,
         defProfitCenter,
         defAPAcct,
         defAPDiscAcct,
         defARAcct,
         defARDiscAcct,
         defAcctMiscInvAcct,
         defAcctSerializedInvAcct,
         defAcctUnbilledInvAcct,
         defAcctFreightAcct
      )

      when:
      def result = get("$path")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == generalLedgerControl.id
         periodFrom == generalLedgerControl.periodFrom.toString()
         periodTo == generalLedgerControl.periodTo.toString()
         defaultProfitCenter.id == generalLedgerControl.defaultProfitCenter.id
         defaultAccountPayableAccount.id == generalLedgerControl.defaultAccountPayableAccount.id
         defaultAccountPayableDiscountAccount.id == generalLedgerControl.defaultAccountPayableDiscountAccount.id
         defaultAccountReceivableAccount.id == generalLedgerControl.defaultAccountReceivableAccount.id
         defaultAccountReceivableDiscountAccount.id == generalLedgerControl.defaultAccountReceivableDiscountAccount.id
         defaultAccountMiscInventoryAccount.id == generalLedgerControl.defaultAccountMiscInventoryAccount.id
         defaultAccountSerializedInventoryAccount.id == generalLedgerControl.defaultAccountSerializedInventoryAccount.id
         defaultAccountUnbilledInventoryAccount.id == generalLedgerControl.defaultAccountUnbilledInventoryAccount.id
         defaultAccountFreightAccount.id == generalLedgerControl.defaultAccountFreightAccount.id
      }
   }

   void "fetch one general ledger control by company not found" () {
      when:
      get("$path")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "General ledger control record of the company was unable to be found"
   }

   void "create valid general ledger control" () {
      given:
      final company = nineNineEightEmployee.company
      final defProfitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final defAPAcct = accountDataLoaderService.single(company)
      final defAPDiscAcct = accountDataLoaderService.single(company)
      final defARAcct = accountDataLoaderService.single(company)
      final defARDiscAcct = accountDataLoaderService.single(company)
      final defAcctMiscInvAcct = accountDataLoaderService.single(company)
      final defAcctSerializedInvAcct = accountDataLoaderService.single(company)
      final defAcctUnbilledInvAcct = accountDataLoaderService.single(company)
      final defAcctFreightAcct = accountDataLoaderService.single(company)
      final def generalLedgerControl = generalLedgerControlDataLoaderService.singleDTO(
         null,
         null,
         new SimpleIdentifiableDTO(defProfitCenter.myId()),
         new SimpleIdentifiableDTO(defAPAcct.myId()),
         new SimpleIdentifiableDTO(defAPDiscAcct.myId()),
         new SimpleIdentifiableDTO(defARAcct.myId()),
         new SimpleIdentifiableDTO(defARDiscAcct.myId()),
         new SimpleIdentifiableDTO(defAcctMiscInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctSerializedInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctUnbilledInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctFreightAcct.myId())
      )

      when:
      def result = post("$path", generalLedgerControl)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
         periodFrom == generalLedgerControl.periodFrom.toString()
         periodTo == generalLedgerControl.periodTo.toString()
         defaultProfitCenter.id == generalLedgerControl.defaultProfitCenter.id
         defaultAccountPayableAccount.id == generalLedgerControl.defaultAccountPayableAccount.id
         defaultAccountPayableDiscountAccount.id == generalLedgerControl.defaultAccountPayableDiscountAccount.id
         defaultAccountReceivableAccount.id == generalLedgerControl.defaultAccountReceivableAccount.id
         defaultAccountReceivableDiscountAccount.id == generalLedgerControl.defaultAccountReceivableDiscountAccount.id
         defaultAccountMiscInventoryAccount.id == generalLedgerControl.defaultAccountMiscInventoryAccount.id
         defaultAccountSerializedInventoryAccount.id == generalLedgerControl.defaultAccountSerializedInventoryAccount.id
         defaultAccountUnbilledInventoryAccount.id == generalLedgerControl.defaultAccountUnbilledInventoryAccount.id
         defaultAccountFreightAccount.id == generalLedgerControl.defaultAccountFreightAccount.id
      }
   }

   void "create valid general ledger control with null account ids" () {
      given:
      final company = nineNineEightEmployee.company
      final defProfitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final defAPAcct = accountDataLoaderService.single(company)
      final defAPDiscAcct = accountDataLoaderService.single(company)
      final defARAcct = accountDataLoaderService.single(company)
      final defARDiscAcct = accountDataLoaderService.single(company)
      final defAcctMiscInvAcct = accountDataLoaderService.single(company)
      final defAcctSerializedInvAcct = accountDataLoaderService.single(company)
      final defAcctUnbilledInvAcct = accountDataLoaderService.single(company)
      final defAcctFreightAcct = accountDataLoaderService.single(company)
      final def generalLedgerControl = generalLedgerControlDataLoaderService.singleDTO(
         null,
         null,
         new SimpleIdentifiableDTO(defProfitCenter.myId()),
         new SimpleIdentifiableDTO(defAPAcct.myId()),
         new SimpleIdentifiableDTO(defAPDiscAcct.myId()),
         new SimpleIdentifiableDTO(defARAcct.myId()),
         new SimpleIdentifiableDTO(defARDiscAcct.myId()),
         new SimpleIdentifiableDTO(defAcctMiscInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctSerializedInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctUnbilledInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctFreightAcct.myId())
      )
      generalLedgerControl.defaultAccountPayableAccount = null
      generalLedgerControl.defaultAccountPayableDiscountAccount = null
      generalLedgerControl.defaultAccountReceivableAccount = null
      generalLedgerControl.defaultAccountReceivableDiscountAccount = null
      generalLedgerControl.defaultAccountMiscInventoryAccount = null
      generalLedgerControl.defaultAccountSerializedInventoryAccount = null
      generalLedgerControl.defaultAccountUnbilledInventoryAccount = null
      generalLedgerControl.defaultAccountFreightAccount = null

      when:
      def result = post("$path", generalLedgerControl)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
         periodFrom == generalLedgerControl.periodFrom.toString()
         periodTo == generalLedgerControl.periodTo.toString()
         defaultProfitCenter.id == generalLedgerControl.defaultProfitCenter.id
         defaultAccountPayableAccount == null
         defaultAccountPayableDiscountAccount == null
         defaultAccountReceivableAccount == null
         defaultAccountReceivableDiscountAccount == null
         defaultAccountMiscInventoryAccount == null
         defaultAccountSerializedInventoryAccount == null
         defaultAccountUnbilledInventoryAccount == null
         defaultAccountFreightAccount == null
      }

      when:
      def result2 = get("$path")

      then:
      notThrown(HttpClientResponseException)
      result2 != null
   }

   void "create invalid general ledger control for company with existing record" () {
      given:
      final company = nineNineEightEmployee.company
      final defProfitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final defAPAcct = accountDataLoaderService.single(company)
      final defAPDiscAcct = accountDataLoaderService.single(company)
      final defARAcct = accountDataLoaderService.single(company)
      final defARDiscAcct = accountDataLoaderService.single(company)
      final defAcctMiscInvAcct = accountDataLoaderService.single(company)
      final defAcctSerializedInvAcct = accountDataLoaderService.single(company)
      final defAcctUnbilledInvAcct = accountDataLoaderService.single(company)
      final defAcctFreightAcct = accountDataLoaderService.single(company)
      final def generalLedgerControl = generalLedgerControlDataLoaderService.single(
         company,
         null,
         null,
         defProfitCenter,
         defAPAcct,
         defAPDiscAcct,
         defARAcct,
         defARDiscAcct,
         defAcctMiscInvAcct,
         defAcctSerializedInvAcct,
         defAcctUnbilledInvAcct,
         defAcctFreightAcct
      )

      when:
      post("$path", generalLedgerControl)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "company"
      response[0].message == "${company.myDataset()} already exists"
   }

   void "create invalid general ledger control where periodTo is before periodFrom" () {
      given:
      final company = nineNineEightEmployee.company
      final defProfitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final defAPAcct = accountDataLoaderService.single(company)
      final defAPDiscAcct = accountDataLoaderService.single(company)
      final defARAcct = accountDataLoaderService.single(company)
      final defARDiscAcct = accountDataLoaderService.single(company)
      final defAcctMiscInvAcct = accountDataLoaderService.single(company)
      final defAcctSerializedInvAcct = accountDataLoaderService.single(company)
      final defAcctUnbilledInvAcct = accountDataLoaderService.single(company)
      final defAcctFreightAcct = accountDataLoaderService.single(company)
      final def generalLedgerControl = generalLedgerControlDataLoaderService.singleDTO(
         LocalDate.of(2000, FEBRUARY, 2),
         LocalDate.of(2000, JANUARY, 2),
         new SimpleIdentifiableDTO(defProfitCenter.myId()),
         new SimpleIdentifiableDTO(defAPAcct.myId()),
         new SimpleIdentifiableDTO(defAPDiscAcct.myId()),
         new SimpleIdentifiableDTO(defARAcct.myId()),
         new SimpleIdentifiableDTO(defARDiscAcct.myId()),
         new SimpleIdentifiableDTO(defAcctMiscInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctSerializedInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctUnbilledInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctFreightAcct.myId())
      )

      when:
      post("$path", generalLedgerControl)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "periodTo"
      response[0].message == "To date of ${generalLedgerControl.periodTo} is before from date of ${generalLedgerControl.periodFrom}"
   }

   void "create invalid general ledger control without periodFrom" () {
      given:
      final company = nineNineEightEmployee.company
      final defProfitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final defAPAcct = accountDataLoaderService.single(company)
      final defAPDiscAcct = accountDataLoaderService.single(company)
      final defARAcct = accountDataLoaderService.single(company)
      final defARDiscAcct = accountDataLoaderService.single(company)
      final defAcctMiscInvAcct = accountDataLoaderService.single(company)
      final defAcctSerializedInvAcct = accountDataLoaderService.single(company)
      final defAcctUnbilledInvAcct = accountDataLoaderService.single(company)
      final defAcctFreightAcct = accountDataLoaderService.single(company)
      final def generalLedgerControl = generalLedgerControlDataLoaderService.singleDTO(
         null,
         null,
         new SimpleIdentifiableDTO(defProfitCenter.myId()),
         new SimpleIdentifiableDTO(defAPAcct.myId()),
         new SimpleIdentifiableDTO(defAPDiscAcct.myId()),
         new SimpleIdentifiableDTO(defARAcct.myId()),
         new SimpleIdentifiableDTO(defARDiscAcct.myId()),
         new SimpleIdentifiableDTO(defAcctMiscInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctSerializedInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctUnbilledInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctFreightAcct.myId())
      )
      generalLedgerControl.periodFrom = null

      when:
      post("$path", generalLedgerControl)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "periodFrom"
      response[0].message == "Is required"
   }

   void "create invalid general ledger control without periodTo" () {
      given:
      final company = nineNineEightEmployee.company
      final defProfitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final defAPAcct = accountDataLoaderService.single(company)
      final defAPDiscAcct = accountDataLoaderService.single(company)
      final defARAcct = accountDataLoaderService.single(company)
      final defARDiscAcct = accountDataLoaderService.single(company)
      final defAcctMiscInvAcct = accountDataLoaderService.single(company)
      final defAcctSerializedInvAcct = accountDataLoaderService.single(company)
      final defAcctUnbilledInvAcct = accountDataLoaderService.single(company)
      final defAcctFreightAcct = accountDataLoaderService.single(company)
      final def generalLedgerControl = generalLedgerControlDataLoaderService.singleDTO(
         null,
         null,
         new SimpleIdentifiableDTO(defProfitCenter.myId()),
         new SimpleIdentifiableDTO(defAPAcct.myId()),
         new SimpleIdentifiableDTO(defAPDiscAcct.myId()),
         new SimpleIdentifiableDTO(defARAcct.myId()),
         new SimpleIdentifiableDTO(defARDiscAcct.myId()),
         new SimpleIdentifiableDTO(defAcctMiscInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctSerializedInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctUnbilledInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctFreightAcct.myId())
      )
      generalLedgerControl.periodTo = null

      when:
      post("$path", generalLedgerControl)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "periodTo"
      response[0].message == "Is required"
   }

   void "create invalid general ledger control without default profit center" () {
      given:
      final company = nineNineEightEmployee.company
      final defProfitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final defAPAcct = accountDataLoaderService.single(company)
      final defAPDiscAcct = accountDataLoaderService.single(company)
      final defARAcct = accountDataLoaderService.single(company)
      final defARDiscAcct = accountDataLoaderService.single(company)
      final defAcctMiscInvAcct = accountDataLoaderService.single(company)
      final defAcctSerializedInvAcct = accountDataLoaderService.single(company)
      final defAcctUnbilledInvAcct = accountDataLoaderService.single(company)
      final defAcctFreightAcct = accountDataLoaderService.single(company)
      final def generalLedgerControl = generalLedgerControlDataLoaderService.singleDTO(
         null,
         null,
         new SimpleIdentifiableDTO(defProfitCenter.myId()),
         new SimpleIdentifiableDTO(defAPAcct.myId()),
         new SimpleIdentifiableDTO(defAPDiscAcct.myId()),
         new SimpleIdentifiableDTO(defARAcct.myId()),
         new SimpleIdentifiableDTO(defARDiscAcct.myId()),
         new SimpleIdentifiableDTO(defAcctMiscInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctSerializedInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctUnbilledInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctFreightAcct.myId())
      )
      generalLedgerControl.defaultProfitCenter = null

      when:
      post("$path", generalLedgerControl)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "defaultProfitCenter"
      response[0].message == "Is required"
   }

   void "create invalid general ledger control with non-existing account ids" () {
      given:
      final defProfitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final def generalLedgerControl = generalLedgerControlDataLoaderService.singleDTO(
         null,
         null,
         new SimpleIdentifiableDTO(defProfitCenter.myId()),
         new SimpleIdentifiableDTO(0),
         new SimpleIdentifiableDTO(0),
         new SimpleIdentifiableDTO(0),
         new SimpleIdentifiableDTO(0),
         new SimpleIdentifiableDTO(0),
         new SimpleIdentifiableDTO(0),
         new SimpleIdentifiableDTO(0),
         new SimpleIdentifiableDTO(0)
      )

      when:
      post("$path", generalLedgerControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 8
      response[0].path == "defaultAccountPayableAccount.id"
      response[1].path == "defaultAccountPayableDiscountAccount.id"
      response[2].path == "defaultAccountReceivableAccount.id"
      response[3].path == "defaultAccountReceivableDiscountAccount.id"
      response[4].path == "defaultAccountMiscInventoryAccount.id"
      response[5].path == "defaultAccountSerializedInventoryAccount.id"
      response[6].path == "defaultAccountUnbilledInventoryAccount.id"
      response[7].path == "defaultAccountFreightAccount.id"
      response[0].message == "0 was unable to be found"
      response[1].message == "0 was unable to be found"
      response[2].message == "0 was unable to be found"
      response[3].message == "0 was unable to be found"
      response[4].message == "0 was unable to be found"
      response[5].message == "0 was unable to be found"
      response[6].message == "0 was unable to be found"
      response[7].message == "0 was unable to be found"
   }

   void "update valid general ledger control by id" () {
      given:
      final company = nineNineEightEmployee.company
      final defProfitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final defAPAcct = accountDataLoaderService.single(company)
      final defAPDiscAcct = accountDataLoaderService.single(company)
      final defARAcct = accountDataLoaderService.single(company)
      final defARDiscAcct = accountDataLoaderService.single(company)
      final defAcctMiscInvAcct = accountDataLoaderService.single(company)
      final defAcctSerializedInvAcct = accountDataLoaderService.single(company)
      final defAcctUnbilledInvAcct = accountDataLoaderService.single(company)
      final defAcctFreightAcct = accountDataLoaderService.single(company)
      final def existingGLControl = generalLedgerControlDataLoaderService.single(
         company,
         null,
         null,
         defProfitCenter,
         defAPAcct,
         defAPDiscAcct,
         defARAcct,
         defARDiscAcct,
         defAcctMiscInvAcct,
         defAcctSerializedInvAcct,
         defAcctUnbilledInvAcct,
         defAcctFreightAcct
      )
      final def updatedGLControlDTO = generalLedgerControlDataLoaderService.singleDTO(
         null,
         null,
         new SimpleIdentifiableDTO(defProfitCenter.myId()),
         new SimpleIdentifiableDTO(defAPAcct.myId()),
         new SimpleIdentifiableDTO(defAPDiscAcct.myId()),
         new SimpleIdentifiableDTO(defARAcct.myId()),
         new SimpleIdentifiableDTO(defARDiscAcct.myId()),
         new SimpleIdentifiableDTO(defAcctMiscInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctSerializedInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctUnbilledInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctFreightAcct.myId())
      )
      updatedGLControlDTO.id = existingGLControl.id

      when:
      def result = put("$path", updatedGLControlDTO)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
         periodFrom == updatedGLControlDTO.periodFrom.toString()
         periodTo == updatedGLControlDTO.periodTo.toString()
         defaultProfitCenter.id == updatedGLControlDTO.defaultProfitCenter.id
         defaultAccountPayableAccount.id == updatedGLControlDTO.defaultAccountPayableAccount.id
         defaultAccountPayableDiscountAccount.id == updatedGLControlDTO.defaultAccountPayableDiscountAccount.id
         defaultAccountReceivableAccount.id == updatedGLControlDTO.defaultAccountReceivableAccount.id
         defaultAccountReceivableDiscountAccount.id == updatedGLControlDTO.defaultAccountReceivableDiscountAccount.id
         defaultAccountMiscInventoryAccount.id == updatedGLControlDTO.defaultAccountMiscInventoryAccount.id
         defaultAccountSerializedInventoryAccount.id == updatedGLControlDTO.defaultAccountSerializedInventoryAccount.id
         defaultAccountUnbilledInventoryAccount.id == updatedGLControlDTO.defaultAccountUnbilledInventoryAccount.id
         defaultAccountFreightAccount.id == updatedGLControlDTO.defaultAccountFreightAccount.id
      }
   }

   void "update valid general ledger control by id with null account ids" () {
      given:
      final company = nineNineEightEmployee.company
      final defProfitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final defAPAcct = accountDataLoaderService.single(company)
      final defAPDiscAcct = accountDataLoaderService.single(company)
      final defARAcct = accountDataLoaderService.single(company)
      final defARDiscAcct = accountDataLoaderService.single(company)
      final defAcctMiscInvAcct = accountDataLoaderService.single(company)
      final defAcctSerializedInvAcct = accountDataLoaderService.single(company)
      final defAcctUnbilledInvAcct = accountDataLoaderService.single(company)
      final defAcctFreightAcct = accountDataLoaderService.single(company)
      final def existingGLControl = generalLedgerControlDataLoaderService.single(
         company,
         null,
         null,
         defProfitCenter,
         defAPAcct,
         defAPDiscAcct,
         defARAcct,
         defARDiscAcct,
         defAcctMiscInvAcct,
         defAcctSerializedInvAcct,
         defAcctUnbilledInvAcct,
         defAcctFreightAcct
      )
      final def updatedGLControlDTO = generalLedgerControlDataLoaderService.singleDTO(
         null,
         null,
         new SimpleIdentifiableDTO(defProfitCenter.myId()),
         new SimpleIdentifiableDTO(defAPAcct.myId()),
         new SimpleIdentifiableDTO(defAPDiscAcct.myId()),
         new SimpleIdentifiableDTO(defARAcct.myId()),
         new SimpleIdentifiableDTO(defARDiscAcct.myId()),
         new SimpleIdentifiableDTO(defAcctMiscInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctSerializedInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctUnbilledInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctFreightAcct.myId())
      )
      updatedGLControlDTO.id = existingGLControl.id
      updatedGLControlDTO.defaultAccountPayableAccount = null
      updatedGLControlDTO.defaultAccountPayableDiscountAccount = null
      updatedGLControlDTO.defaultAccountReceivableAccount = null
      updatedGLControlDTO.defaultAccountReceivableDiscountAccount = null
      updatedGLControlDTO.defaultAccountMiscInventoryAccount = null
      updatedGLControlDTO.defaultAccountSerializedInventoryAccount = null
      updatedGLControlDTO.defaultAccountUnbilledInventoryAccount = null
      updatedGLControlDTO.defaultAccountFreightAccount = null

      when:
      def result = put("$path", updatedGLControlDTO)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
         periodFrom == updatedGLControlDTO.periodFrom.toString()
         periodTo == updatedGLControlDTO.periodTo.toString()
         defaultProfitCenter.id == updatedGLControlDTO.defaultProfitCenter.id
         defaultAccountPayableAccount == null
         defaultAccountPayableDiscountAccount == null
         defaultAccountReceivableAccount == null
         defaultAccountReceivableDiscountAccount == null
         defaultAccountMiscInventoryAccount == null
         defaultAccountSerializedInventoryAccount == null
         defaultAccountUnbilledInventoryAccount == null
         defaultAccountFreightAccount == null
      }

      when:
      def result2 = get("$path")

      then:
      notThrown(HttpClientResponseException)
      result2 != null
   }

   void "update invalid general ledger control with id 0" () {
      given:
      final company = nineNineEightEmployee.company
      final defProfitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final defAPAcct = accountDataLoaderService.single(company)
      final defAPDiscAcct = accountDataLoaderService.single(company)
      final defARAcct = accountDataLoaderService.single(company)
      final defARDiscAcct = accountDataLoaderService.single(company)
      final defAcctMiscInvAcct = accountDataLoaderService.single(company)
      final defAcctSerializedInvAcct = accountDataLoaderService.single(company)
      final defAcctUnbilledInvAcct = accountDataLoaderService.single(company)
      final defAcctFreightAcct = accountDataLoaderService.single(company)
      final def updatedGLControlDTO = generalLedgerControlDataLoaderService.singleDTO(
         null,
         null,
         new SimpleIdentifiableDTO(defProfitCenter.myId()),
         new SimpleIdentifiableDTO(defAPAcct.myId()),
         new SimpleIdentifiableDTO(defAPDiscAcct.myId()),
         new SimpleIdentifiableDTO(defARAcct.myId()),
         new SimpleIdentifiableDTO(defARDiscAcct.myId()),
         new SimpleIdentifiableDTO(defAcctMiscInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctSerializedInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctUnbilledInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctFreightAcct.myId())
      )
      updatedGLControlDTO.id = 0

      when:
      put("$path", updatedGLControlDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "id"
      response[0].message == "id must be greater than zero"
   }

   void "update invalid general ledger control with non-existing company" () {
      given:
      final tstds2 = companyFactoryService.forDatasetCode('tstds2') // load the company that isn't logged in to create the control record
      final defProfitCenter = storeFactoryService.store(3, tstds2)
      final defAPAcct = accountDataLoaderService.single(tstds2)
      final defAPDiscAcct = accountDataLoaderService.single(tstds2)
      final defARAcct = accountDataLoaderService.single(tstds2)
      final defARDiscAcct = accountDataLoaderService.single(tstds2)
      final defAcctMiscInvAcct = accountDataLoaderService.single(tstds2)
      final defAcctSerializedInvAcct = accountDataLoaderService.single(tstds2)
      final defAcctUnbilledInvAcct = accountDataLoaderService.single(tstds2)
      final defAcctFreightAcct = accountDataLoaderService.single(tstds2)
      final def existingGLControl = generalLedgerControlDataLoaderService.single(
         tstds2,
         null,
         null,
         defProfitCenter,
         defAPAcct,
         defAPDiscAcct,
         defARAcct,
         defARDiscAcct,
         defAcctMiscInvAcct,
         defAcctSerializedInvAcct,
         defAcctUnbilledInvAcct,
         defAcctFreightAcct
      )
      final def updatedGLControlDTO = generalLedgerControlDataLoaderService.singleDTO(
         null,
         null,
         new SimpleIdentifiableDTO(defProfitCenter.myId()),
         new SimpleIdentifiableDTO(defAPAcct.myId()),
         new SimpleIdentifiableDTO(defAPDiscAcct.myId()),
         new SimpleIdentifiableDTO(defARAcct.myId()),
         new SimpleIdentifiableDTO(defARDiscAcct.myId()),
         new SimpleIdentifiableDTO(defAcctMiscInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctSerializedInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctUnbilledInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctFreightAcct.myId())
      )
      updatedGLControlDTO.id = existingGLControl.id

      when:
      put("$path", updatedGLControlDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.message[0] == "${defProfitCenter.myId()} was unable to be found"
      response.message[1] == "${defAPAcct.myId()} was unable to be found"
      response.message[2] == "${defAPDiscAcct.myId()} was unable to be found"
      response.message[3] == "${defARAcct.myId()} was unable to be found"
      response.message[4] == "${defARDiscAcct.myId()} was unable to be found"
      response.message[5] == "${defAcctMiscInvAcct.myId()} was unable to be found"
      response.message[6] == "${defAcctSerializedInvAcct.myId()} was unable to be found"
      response.message[7] == "${defAcctUnbilledInvAcct.myId()} was unable to be found"
      response.message[8] == "${defAcctFreightAcct.myId()} was unable to be found"
   }

   void "update invalid general ledger control with periodTo before periodFrom" () {
      given:
      final company = nineNineEightEmployee.company
      final defProfitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final defAPAcct = accountDataLoaderService.single(company)
      final defAPDiscAcct = accountDataLoaderService.single(company)
      final defARAcct = accountDataLoaderService.single(company)
      final defARDiscAcct = accountDataLoaderService.single(company)
      final defAcctMiscInvAcct = accountDataLoaderService.single(company)
      final defAcctSerializedInvAcct = accountDataLoaderService.single(company)
      final defAcctUnbilledInvAcct = accountDataLoaderService.single(company)
      final defAcctFreightAcct = accountDataLoaderService.single(company)
      final def existingGLControl = generalLedgerControlDataLoaderService.single(
         company,
         null,
         null,
         defProfitCenter,
         defAPAcct,
         defAPDiscAcct,
         defARAcct,
         defARDiscAcct,
         defAcctMiscInvAcct,
         defAcctSerializedInvAcct,
         defAcctUnbilledInvAcct,
         defAcctFreightAcct
      )
      final def updatedGLControlDTO = generalLedgerControlDataLoaderService.singleDTO(
         null,
         null,
         new SimpleIdentifiableDTO(defProfitCenter.myId()),
         new SimpleIdentifiableDTO(defAPAcct.myId()),
         new SimpleIdentifiableDTO(defAPDiscAcct.myId()),
         new SimpleIdentifiableDTO(defARAcct.myId()),
         new SimpleIdentifiableDTO(defARDiscAcct.myId()),
         new SimpleIdentifiableDTO(defAcctMiscInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctSerializedInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctUnbilledInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctFreightAcct.myId())
      )
      updatedGLControlDTO.id = existingGLControl.id
      updatedGLControlDTO.periodFrom = LocalDate.of(2000, FEBRUARY, 2)
      updatedGLControlDTO.periodTo = LocalDate.of(2000, JANUARY, 2)

      when:
      put("$path", updatedGLControlDTO)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "periodTo"
      response[0].message == "To date of ${updatedGLControlDTO.periodTo} is before from date of ${updatedGLControlDTO.periodFrom}"
   }

   void "update invalid general ledger control without periodFrom" () {
      given:
      final company = nineNineEightEmployee.company
      final defProfitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final defAPAcct = accountDataLoaderService.single(company)
      final defAPDiscAcct = accountDataLoaderService.single(company)
      final defARAcct = accountDataLoaderService.single(company)
      final defARDiscAcct = accountDataLoaderService.single(company)
      final defAcctMiscInvAcct = accountDataLoaderService.single(company)
      final defAcctSerializedInvAcct = accountDataLoaderService.single(company)
      final defAcctUnbilledInvAcct = accountDataLoaderService.single(company)
      final defAcctFreightAcct = accountDataLoaderService.single(company)
      final def updatedGLControlDTO = generalLedgerControlDataLoaderService.singleDTO(
         null,
         null,
         new SimpleIdentifiableDTO(defProfitCenter.myId()),
         new SimpleIdentifiableDTO(defAPAcct.myId()),
         new SimpleIdentifiableDTO(defAPDiscAcct.myId()),
         new SimpleIdentifiableDTO(defARAcct.myId()),
         new SimpleIdentifiableDTO(defARDiscAcct.myId()),
         new SimpleIdentifiableDTO(defAcctMiscInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctSerializedInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctUnbilledInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctFreightAcct.myId())
      )
      updatedGLControlDTO.periodFrom = null

      when:
      put("$path", updatedGLControlDTO)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "periodFrom"
      response[0].message == "Is required"
   }

   void "update invalid general ledger control without periodTo" () {
      given:
      final company = nineNineEightEmployee.company
      final defProfitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final defAPAcct = accountDataLoaderService.single(company)
      final defAPDiscAcct = accountDataLoaderService.single(company)
      final defARAcct = accountDataLoaderService.single(company)
      final defARDiscAcct = accountDataLoaderService.single(company)
      final defAcctMiscInvAcct = accountDataLoaderService.single(company)
      final defAcctSerializedInvAcct = accountDataLoaderService.single(company)
      final defAcctUnbilledInvAcct = accountDataLoaderService.single(company)
      final defAcctFreightAcct = accountDataLoaderService.single(company)
      final def updatedGLControlDTO = generalLedgerControlDataLoaderService.singleDTO(
         null,
         null,
         new SimpleIdentifiableDTO(defProfitCenter.myId()),
         new SimpleIdentifiableDTO(defAPAcct.myId()),
         new SimpleIdentifiableDTO(defAPDiscAcct.myId()),
         new SimpleIdentifiableDTO(defARAcct.myId()),
         new SimpleIdentifiableDTO(defARDiscAcct.myId()),
         new SimpleIdentifiableDTO(defAcctMiscInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctSerializedInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctUnbilledInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctFreightAcct.myId())
      )
      updatedGLControlDTO.periodTo = null

      when:
      put("$path", updatedGLControlDTO)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "periodTo"
      response[0].message == "Is required"
   }

   void "update invalid general ledger control without default profit center" () {
      given:
      final company = nineNineEightEmployee.company
      final defProfitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final defAPAcct = accountDataLoaderService.single(company)
      final defAPDiscAcct = accountDataLoaderService.single(company)
      final defARAcct = accountDataLoaderService.single(company)
      final defARDiscAcct = accountDataLoaderService.single(company)
      final defAcctMiscInvAcct = accountDataLoaderService.single(company)
      final defAcctSerializedInvAcct = accountDataLoaderService.single(company)
      final defAcctUnbilledInvAcct = accountDataLoaderService.single(company)
      final defAcctFreightAcct = accountDataLoaderService.single(company)
      final def updatedGLControlDTO = generalLedgerControlDataLoaderService.singleDTO(
         null,
         null,
         new SimpleIdentifiableDTO(defProfitCenter.myId()),
         new SimpleIdentifiableDTO(defAPAcct.myId()),
         new SimpleIdentifiableDTO(defAPDiscAcct.myId()),
         new SimpleIdentifiableDTO(defARAcct.myId()),
         new SimpleIdentifiableDTO(defARDiscAcct.myId()),
         new SimpleIdentifiableDTO(defAcctMiscInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctSerializedInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctUnbilledInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctFreightAcct.myId())
      )
      updatedGLControlDTO.defaultProfitCenter = null

      when:
      put("$path", updatedGLControlDTO)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "defaultProfitCenter"
      response[0].message == "Is required"
   }

   void "update invalid general ledger control with non-existing account ids" () {
      given:
      final company = nineNineEightEmployee.company
      final defProfitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final defAPAcct = accountDataLoaderService.single(company)
      final defAPDiscAcct = accountDataLoaderService.single(company)
      final defARAcct = accountDataLoaderService.single(company)
      final defARDiscAcct = accountDataLoaderService.single(company)
      final defAcctMiscInvAcct = accountDataLoaderService.single(company)
      final defAcctSerializedInvAcct = accountDataLoaderService.single(company)
      final defAcctUnbilledInvAcct = accountDataLoaderService.single(company)
      final defAcctFreightAcct = accountDataLoaderService.single(company)
      final def existingGLControl = generalLedgerControlDataLoaderService.single(
         company,
         null,
         null,
         defProfitCenter,
         defAPAcct,
         defAPDiscAcct,
         defARAcct,
         defARDiscAcct,
         defAcctMiscInvAcct,
         defAcctSerializedInvAcct,
         defAcctUnbilledInvAcct,
         defAcctFreightAcct
      )
      final def updatedGLControlDTO = generalLedgerControlDataLoaderService.singleDTO(
         null,
         null,
         new SimpleIdentifiableDTO(0),
         new SimpleIdentifiableDTO(0),
         new SimpleIdentifiableDTO(0),
         new SimpleIdentifiableDTO(0),
         new SimpleIdentifiableDTO(0),
         new SimpleIdentifiableDTO(0),
         new SimpleIdentifiableDTO(0),
         new SimpleIdentifiableDTO(0),
         new SimpleIdentifiableDTO(0)
      )
      updatedGLControlDTO.id = existingGLControl.id

      when:
      put("$path", updatedGLControlDTO)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 9
      response[0].path == "defaultProfitCenter.id"
      response[1].path == "defaultAccountPayableAccount.id"
      response[2].path == "defaultAccountPayableDiscountAccount.id"
      response[3].path == "defaultAccountReceivableAccount.id"
      response[4].path == "defaultAccountReceivableDiscountAccount.id"
      response[5].path == "defaultAccountMiscInventoryAccount.id"
      response[6].path == "defaultAccountSerializedInventoryAccount.id"
      response[7].path == "defaultAccountUnbilledInventoryAccount.id"
      response[8].path == "defaultAccountFreightAccount.id"
      response[0].message == "0 was unable to be found"
      response[1].message == "0 was unable to be found"
      response[2].message == "0 was unable to be found"
      response[3].message == "0 was unable to be found"
      response[4].message == "0 was unable to be found"
      response[5].message == "0 was unable to be found"
      response[6].message == "0 was unable to be found"
      response[7].message == "0 was unable to be found"
      response[8].message == "0 was unable to be found"
   }

}
