package com.cynergisuite.middleware.accounting.general.ledger.control.infrastructure

import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.control.GeneralLedgerControlDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import jakarta.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class GeneralLedgerControlControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/general-ledger/control'

   @Inject AccountTestDataLoaderService accountDataLoaderService
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
      final generalLedgerControl = generalLedgerControlDataLoaderService.single(
         company,
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
         defaultProfitCenter.id == generalLedgerControl.defaultProfitCenter.id
         defaultAccountPayableAccount.id == generalLedgerControl.defaultAccountPayableAccount.id
         defaultAccountPayableAccount.name == generalLedgerControl.defaultAccountPayableAccount.name
         defaultAccountPayableDiscountAccount.id == generalLedgerControl.defaultAccountPayableDiscountAccount.id
         defaultAccountPayableDiscountAccount.name== generalLedgerControl.defaultAccountPayableDiscountAccount.name
         defaultAccountReceivableAccount.id == generalLedgerControl.defaultAccountReceivableAccount.id
         defaultAccountReceivableAccount.name == generalLedgerControl.defaultAccountReceivableAccount.name
         defaultAccountReceivableDiscountAccount.id == generalLedgerControl.defaultAccountReceivableDiscountAccount.id
         defaultAccountReceivableDiscountAccount.name == generalLedgerControl.defaultAccountReceivableDiscountAccount.name
         defaultAccountMiscInventoryAccount.id == generalLedgerControl.defaultAccountMiscInventoryAccount.id
         defaultAccountMiscInventoryAccount.name == generalLedgerControl.defaultAccountMiscInventoryAccount.name
         defaultAccountSerializedInventoryAccount.id == generalLedgerControl.defaultAccountSerializedInventoryAccount.id
         defaultAccountSerializedInventoryAccount.name == generalLedgerControl.defaultAccountSerializedInventoryAccount.name
         defaultAccountUnbilledInventoryAccount.id == generalLedgerControl.defaultAccountUnbilledInventoryAccount.id
         defaultAccountUnbilledInventoryAccount.name == generalLedgerControl.defaultAccountUnbilledInventoryAccount.name
         defaultAccountFreightAccount.id == generalLedgerControl.defaultAccountFreightAccount.id
         defaultAccountFreightAccount.name == generalLedgerControl.defaultAccountFreightAccount.name
      }
   }

   void "fetch one general ledger control by company not found" () {
      when:
      get("$path")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "General ledger control record of the company was unable to be found"
      response.code == 'system.not.found'
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
      final generalLedgerControl = generalLedgerControlDataLoaderService.singleDTO(
         new SimpleLegacyIdentifiableDTO(defProfitCenter.myId()),
         new AccountDTO(defAPAcct),
         new AccountDTO(defAPDiscAcct),
         new AccountDTO(defARAcct),
         new AccountDTO(defARDiscAcct),
         new AccountDTO(defAcctMiscInvAcct),
         new AccountDTO(defAcctSerializedInvAcct),
         new AccountDTO(defAcctUnbilledInvAcct),
         new AccountDTO(defAcctFreightAcct)
      )

      when:
      def result = post("$path", generalLedgerControl)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id != null
         defaultProfitCenter.id == generalLedgerControl.defaultProfitCenter.id
         defaultAccountPayableAccount.id == generalLedgerControl.defaultAccountPayableAccount.id
         defaultAccountPayableAccount.name == generalLedgerControl.defaultAccountPayableAccount.name
         defaultAccountPayableDiscountAccount.id == generalLedgerControl.defaultAccountPayableDiscountAccount.id
         defaultAccountPayableDiscountAccount.name == generalLedgerControl.defaultAccountPayableDiscountAccount.name
         defaultAccountReceivableAccount.id == generalLedgerControl.defaultAccountReceivableAccount.id
         defaultAccountReceivableAccount.name == generalLedgerControl.defaultAccountReceivableAccount.name
         defaultAccountReceivableDiscountAccount.id == generalLedgerControl.defaultAccountReceivableDiscountAccount.id
         defaultAccountReceivableDiscountAccount.name == generalLedgerControl.defaultAccountReceivableDiscountAccount.name
         defaultAccountMiscInventoryAccount.id == generalLedgerControl.defaultAccountMiscInventoryAccount.id
         defaultAccountMiscInventoryAccount.name == generalLedgerControl.defaultAccountMiscInventoryAccount.name
         defaultAccountSerializedInventoryAccount.id == generalLedgerControl.defaultAccountSerializedInventoryAccount.id
         defaultAccountSerializedInventoryAccount.name == generalLedgerControl.defaultAccountSerializedInventoryAccount.name
         defaultAccountUnbilledInventoryAccount.id == generalLedgerControl.defaultAccountUnbilledInventoryAccount.id
         defaultAccountUnbilledInventoryAccount.name == generalLedgerControl.defaultAccountUnbilledInventoryAccount.name
         defaultAccountFreightAccount.id == generalLedgerControl.defaultAccountFreightAccount.id
         defaultAccountFreightAccount.name == generalLedgerControl.defaultAccountFreightAccount.name

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
      final generalLedgerControl = generalLedgerControlDataLoaderService.singleDTO(
         new SimpleLegacyIdentifiableDTO(defProfitCenter.myId()),
         new AccountDTO(defAPAcct),
         new AccountDTO(defAPDiscAcct),
         new AccountDTO(defARAcct),
         new AccountDTO(defARDiscAcct),
         new AccountDTO(defAcctMiscInvAcct),
         new AccountDTO(defAcctSerializedInvAcct),
         new AccountDTO(defAcctUnbilledInvAcct),
         new AccountDTO(defAcctFreightAcct)
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
         id != null
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
      final generalLedgerControl = generalLedgerControlDataLoaderService.single(
         company,
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
      response[0].message == "${company.datasetCode} already exists"
      response[0].code == 'cynergi.validation.config.exists'
   }

   @Unroll
   void "create invalid general ledger control without #nonNullableProp" () {
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
      def generalLedgerControl = generalLedgerControlDataLoaderService.singleDTO(
         new SimpleLegacyIdentifiableDTO(defProfitCenter.myId()),
         new AccountDTO(defAPAcct),
         new AccountDTO(defAPDiscAcct),
         new AccountDTO(defARAcct),
         new AccountDTO(defARDiscAcct),
         new AccountDTO(defAcctMiscInvAcct),
         new AccountDTO(defAcctSerializedInvAcct),
         new AccountDTO(defAcctUnbilledInvAcct),
         new AccountDTO(defAcctFreightAcct)
      )
      generalLedgerControl["$nonNullableProp"] = null

      when:
      post("$path", generalLedgerControl)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp                     || errorResponsePath
      'defaultProfitCenter'               || 'defaultProfitCenter'
   }

   void "create invalid general ledger control with non-existing account" () {
      given:
      final nonExistent = accountDataLoaderService.singleDTO(nineNineEightEmployee.company)
      nonExistent.id = UUID.randomUUID()
      final defProfitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final generalLedgerControl = generalLedgerControlDataLoaderService.singleDTO(
         new SimpleLegacyIdentifiableDTO(defProfitCenter.myId()),
         nonExistent,
         nonExistent,
         nonExistent,
         nonExistent,
         nonExistent,
         nonExistent,
         nonExistent,
         nonExistent
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
      response.collect { it.message } as Set == ["$nonExistent.id was unable to be found"] as Set
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
      final existingGLControl = generalLedgerControlDataLoaderService.single(
         company,
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
      final updatedGLControlDTO = generalLedgerControlDataLoaderService.singleDTO(
         new SimpleLegacyIdentifiableDTO(defProfitCenter.myId()),
         new AccountDTO(defAPAcct),
         new AccountDTO(defAPDiscAcct),
         new AccountDTO(defARAcct),
         new AccountDTO(defARDiscAcct),
         new AccountDTO(defAcctMiscInvAcct),
         new AccountDTO(defAcctSerializedInvAcct),
         new AccountDTO(defAcctUnbilledInvAcct),
         new AccountDTO(defAcctFreightAcct)
      )
      updatedGLControlDTO.id = existingGLControl.id

      when:
      def result = put("$path", updatedGLControlDTO)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id != null
         defaultProfitCenter.id == updatedGLControlDTO.defaultProfitCenter.id
         defaultAccountPayableAccount.id == updatedGLControlDTO.defaultAccountPayableAccount.id
         defaultAccountPayableAccount.name == updatedGLControlDTO.defaultAccountPayableAccount.name
         defaultAccountPayableDiscountAccount.id == updatedGLControlDTO.defaultAccountPayableDiscountAccount.id
         defaultAccountPayableDiscountAccount.name == updatedGLControlDTO.defaultAccountPayableDiscountAccount.name
         defaultAccountReceivableAccount.id == updatedGLControlDTO.defaultAccountReceivableAccount.id
         defaultAccountReceivableAccount.name == updatedGLControlDTO.defaultAccountReceivableAccount.name
         defaultAccountReceivableDiscountAccount.id == updatedGLControlDTO.defaultAccountReceivableDiscountAccount.id
         defaultAccountReceivableDiscountAccount.name == updatedGLControlDTO.defaultAccountReceivableDiscountAccount.name
         defaultAccountMiscInventoryAccount.id == updatedGLControlDTO.defaultAccountMiscInventoryAccount.id
         defaultAccountMiscInventoryAccount.name == updatedGLControlDTO.defaultAccountMiscInventoryAccount.name
         defaultAccountSerializedInventoryAccount.id == updatedGLControlDTO.defaultAccountSerializedInventoryAccount.id
         defaultAccountSerializedInventoryAccount.name == updatedGLControlDTO.defaultAccountSerializedInventoryAccount.name
         defaultAccountUnbilledInventoryAccount.id == updatedGLControlDTO.defaultAccountUnbilledInventoryAccount.id
         defaultAccountUnbilledInventoryAccount.name == updatedGLControlDTO.defaultAccountUnbilledInventoryAccount.name
         defaultAccountFreightAccount.id == updatedGLControlDTO.defaultAccountFreightAccount.id
         defaultAccountFreightAccount.name == updatedGLControlDTO.defaultAccountFreightAccount.name
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
      final existingGLControl = generalLedgerControlDataLoaderService.single(
         company,
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
      final updatedGLControlDTO = generalLedgerControlDataLoaderService.singleDTO(
         new SimpleLegacyIdentifiableDTO(defProfitCenter.myId()),
         new AccountDTO(defAPAcct),
         new AccountDTO(defAPDiscAcct),
         new AccountDTO(defARAcct),
         new AccountDTO(defARDiscAcct),
         new AccountDTO(defAcctMiscInvAcct),
         new AccountDTO(defAcctSerializedInvAcct),
         new AccountDTO(defAcctUnbilledInvAcct),
         new AccountDTO(defAcctFreightAcct)
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
         id != null
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

   void "update invalid general ledger control for a company that hasn't defined one yet" () {
      given:
      final nonExistentId = UUID.randomUUID()
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
      final updatedGLControlDTO = generalLedgerControlDataLoaderService.singleDTO(
         new SimpleLegacyIdentifiableDTO(defProfitCenter.myId()),
         new AccountDTO(defAPAcct),
         new AccountDTO(defAPDiscAcct),
         new AccountDTO(defARAcct),
         new AccountDTO(defARDiscAcct),
         new AccountDTO(defAcctMiscInvAcct),
         new AccountDTO(defAcctSerializedInvAcct),
         new AccountDTO(defAcctUnbilledInvAcct),
         new AccountDTO(defAcctFreightAcct)
      )
      updatedGLControlDTO.id = nonExistentId

      when:
      put("$path", updatedGLControlDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${company.id} was unable to be found"
   }

   @Unroll
   void "update invalid general ledger control without #nonNullableProp" () {
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
      final updatedGLControlDTO = generalLedgerControlDataLoaderService.singleDTO(
         new SimpleLegacyIdentifiableDTO(defProfitCenter.myId()),
         new AccountDTO(defAPAcct),
         new AccountDTO(defAPDiscAcct),
         new AccountDTO(defARAcct),
         new AccountDTO(defARDiscAcct),
         new AccountDTO(defAcctMiscInvAcct),
         new AccountDTO(defAcctSerializedInvAcct),
         new AccountDTO(defAcctUnbilledInvAcct),
         new AccountDTO(defAcctFreightAcct)
      )
      updatedGLControlDTO["$nonNullableProp"] = null

      when:
      put("$path", updatedGLControlDTO)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp                     || errorResponsePath
      'defaultProfitCenter'               || 'defaultProfitCenter'
   }

   void "update invalid general ledger control with non-existing account ids" () {
      given:
      final nonExistent = accountDataLoaderService.singleDTO(nineNineEightEmployee.company)
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
      final existingGLControl = generalLedgerControlDataLoaderService.single(
         company,
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
      nonExistent.id = UUID.randomUUID()
      final updatedGLControlDTO = generalLedgerControlDataLoaderService.singleDTO(
         new SimpleLegacyIdentifiableDTO(0),
         nonExistent,
         nonExistent,
         nonExistent,
         nonExistent,
         nonExistent,
         nonExistent,
         nonExistent,
         nonExistent,

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
      response.collect { it.message } as Set == ["$nonExistent.id was unable to be found", "0 was unable to be found"] as Set
   }
}
