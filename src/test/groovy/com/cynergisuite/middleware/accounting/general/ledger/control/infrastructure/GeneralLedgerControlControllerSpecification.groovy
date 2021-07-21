package com.cynergisuite.middleware.accounting.general.ledger.control.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.control.GeneralLedgerControlDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

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
         new SimpleLegacyIdentifiableDTO(defProfitCenter.myId()),
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
         id != null
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
         new SimpleLegacyIdentifiableDTO(defProfitCenter.myId()),
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
      final def generalLedgerControl = generalLedgerControlDataLoaderService.single(
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
      response[0].message == "${company.myDataset()} already exists"
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
         new SimpleIdentifiableDTO(defAPAcct.myId()),
         new SimpleIdentifiableDTO(defAPDiscAcct.myId()),
         new SimpleIdentifiableDTO(defARAcct.myId()),
         new SimpleIdentifiableDTO(defARDiscAcct.myId()),
         new SimpleIdentifiableDTO(defAcctMiscInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctSerializedInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctUnbilledInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctFreightAcct.myId())
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

   void "create invalid general ledger control with non-existing account ids" () {
      given:
      final nonExistentId = UUID.randomUUID()
      final defProfitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final def generalLedgerControl = generalLedgerControlDataLoaderService.singleDTO(
         new SimpleLegacyIdentifiableDTO(defProfitCenter.myId()),
         new SimpleIdentifiableDTO(nonExistentId),
         new SimpleIdentifiableDTO(nonExistentId),
         new SimpleIdentifiableDTO(nonExistentId),
         new SimpleIdentifiableDTO(nonExistentId),
         new SimpleIdentifiableDTO(nonExistentId),
         new SimpleIdentifiableDTO(nonExistentId),
         new SimpleIdentifiableDTO(nonExistentId),
         new SimpleIdentifiableDTO(nonExistentId)
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
      response.collect { it.message } as Set == ["$nonExistentId was unable to be found"] as Set
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
         new SimpleLegacyIdentifiableDTO(defProfitCenter.myId()),
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
         id != null
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
         new SimpleLegacyIdentifiableDTO(defProfitCenter.myId()),
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
      final def updatedGLControlDTO = generalLedgerControlDataLoaderService.singleDTO(
         new SimpleLegacyIdentifiableDTO(defProfitCenter.myId()),
         new SimpleIdentifiableDTO(defAPAcct.myId()),
         new SimpleIdentifiableDTO(defAPDiscAcct.myId()),
         new SimpleIdentifiableDTO(defARAcct.myId()),
         new SimpleIdentifiableDTO(defARDiscAcct.myId()),
         new SimpleIdentifiableDTO(defAcctMiscInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctSerializedInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctUnbilledInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctFreightAcct.myId())
      )
      updatedGLControlDTO.id = nonExistentId

      when:
      put("$path", updatedGLControlDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${company.myId()} was unable to be found"
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
      final def updatedGLControlDTO = generalLedgerControlDataLoaderService.singleDTO(
         new SimpleLegacyIdentifiableDTO(defProfitCenter.myId()),
         new SimpleIdentifiableDTO(defAPAcct.myId()),
         new SimpleIdentifiableDTO(defAPDiscAcct.myId()),
         new SimpleIdentifiableDTO(defARAcct.myId()),
         new SimpleIdentifiableDTO(defARDiscAcct.myId()),
         new SimpleIdentifiableDTO(defAcctMiscInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctSerializedInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctUnbilledInvAcct.myId()),
         new SimpleIdentifiableDTO(defAcctFreightAcct.myId())
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
      final def existingGLControl = generalLedgerControlDataLoaderService.single(
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
      final def updatedGLControlDTO = generalLedgerControlDataLoaderService.singleDTO(
         new SimpleLegacyIdentifiableDTO(0),
         new SimpleIdentifiableDTO(nonExistentId),
         new SimpleIdentifiableDTO(nonExistentId),
         new SimpleIdentifiableDTO(nonExistentId),
         new SimpleIdentifiableDTO(nonExistentId),
         new SimpleIdentifiableDTO(nonExistentId),
         new SimpleIdentifiableDTO(nonExistentId),
         new SimpleIdentifiableDTO(nonExistentId),
         new SimpleIdentifiableDTO(nonExistentId)
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
      response.collect { it.message } as Set == ["$nonExistentId was unable to be found", "0 was unable to be found"] as Set
   }
}
