package com.cynergisuite.middleware.vendor.rebate.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDataLoaderService
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.vendor.VendorTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import com.cynergisuite.middleware.vendor.rebate.RebateDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST

@MicronautTest(transactional = false)
class RebateControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/vendor/rebate"

   @Inject AccountDataLoaderService accountDataLoaderService
   @Inject RebateDataLoaderService rebateDataLoaderService
   @Inject ShipViaTestDataLoaderService shipViaFactoryService
   @Inject VendorPaymentTermTestDataLoaderService vendorPaymentTermTestDataLoaderService
   @Inject VendorTestDataLoaderService vendorTestDataLoaderService

   void "fetch one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor1 = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebateEntity = rebateDataLoaderService.single(company, vendor1, glDebitAcct, glCreditAcct)

      when:
      def result = get("$path/${rebateEntity.id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == rebateEntity.id
         vendor.id == rebateEntity.vendor.myId()

         with(status) {
            value == rebateEntity.status.value
            description == rebateEntity.status.description
         }

         description == rebateEntity.description

         with(rebate) {
            value == rebateEntity.rebate.value
            description == rebateEntity.rebate.description
         }

         percent == rebateEntity.percent
         amountPerUnit == rebateEntity.amountPerUnit
         accrualIndicator == rebateEntity.accrualIndicator
         generalLedgerDebitAccount.id == rebateEntity.generalLedgerDebitAccount.id
         generalLedgerCreditAccount.id == rebateEntity.generalLedgerCreditAccount.id
      }
   }

   void "fetch all" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor1 = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebates = rebateDataLoaderService.stream(7, company, vendor1, glDebitAcct, glCreditAcct).toList()
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      final firstPageRebate = rebates[0..4]
      final lastPageRebate = rebates[5,6]

      when:
      def result = get("$path$pageOne")

      then:
      notThrown(Exception)
      with(result) {
         requested.with { new StandardPageRequest(it) } == pageOne
         totalElements == 7
         totalPages == 2
         first == true
         last == false
         elements.size() == 5
         elements.eachWithIndex { pageOneResult, index ->
            with(pageOneResult) {
               id == firstPageRebate[index].id
               vendor.id == firstPageRebate[index].vendor.myId()

               with(status) {
                  value == firstPageRebate[index].status.value
                  description == firstPageRebate[index].status.description
               }

               description == firstPageRebate[index].description

               with(rebate) {
                  value == firstPageRebate[index].rebate.value
                  description == firstPageRebate[index].rebate.description
               }

               percent == firstPageRebate[index].percent
               amountPerUnit == firstPageRebate[index].amountPerUnit
               accrualIndicator == firstPageRebate[index].accrualIndicator
               generalLedgerDebitAccount.id == firstPageRebate[index].generalLedgerDebitAccount.id
               generalLedgerCreditAccount.id == firstPageRebate[index].generalLedgerCreditAccount.id
            }
         }
      }

      when:
      result = get("$path$pageTwo")

      then:
      notThrown(Exception)
      with(result) {
         requested.with { new StandardPageRequest(it) } == pageTwo
         totalElements == 7
         totalPages == 2
         first == false
         last == true
         elements.size() == 2
         elements.eachWithIndex { pageLastResult, index ->
            with(pageLastResult) {
               id == lastPageRebate[index].id
               vendor.id == lastPageRebate[index].vendor.myId()

               with(status) {
                  value == lastPageRebate[index].status.value
                  description == lastPageRebate[index].status.description
               }

               description == lastPageRebate[index].description

               with(rebate) {
                  value == lastPageRebate[index].rebate.value
                  description == lastPageRebate[index].rebate.description
               }

               percent == lastPageRebate[index].percent
               amountPerUnit == lastPageRebate[index].amountPerUnit
               accrualIndicator == lastPageRebate[index].accrualIndicator
               generalLedgerDebitAccount.id == lastPageRebate[index].generalLedgerDebitAccount.id
               generalLedgerCreditAccount.id == lastPageRebate[index].generalLedgerCreditAccount.id
            }
         }
      }
   }

   void "create one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor1 = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebateDTO = rebateDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor1), new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))

      when:
      def result = post(path, rebateDTO)
      rebateDTO.id = result.id

      then:
      notThrown(Exception)
      result != null
      result.id != null
      result.id > 0
      with(result) {
         id == rebateDTO.id
         vendor.id == rebateDTO.vendor.myId()

         with(status) {
            value == rebateDTO.status.value
            description == rebateDTO.status.description
         }

         description == rebateDTO.description

         with(rebate) {
            value == rebateDTO.rebate.value
            description == rebateDTO.rebate.description
         }

         percent == rebateDTO.percent
         amountPerUnit == rebateDTO.amountPerUnit
         accrualIndicator == rebateDTO.accrualIndicator

         generalLedgerDebitAccount.id == rebateDTO.generalLedgerDebitAccount.id
         generalLedgerCreditAccount.id == rebateDTO.generalLedgerCreditAccount.id
      }
   }

   void "create valid rebate with null general ledger debit account and general ledger credit account" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor1 = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebateDTO = rebateDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor1), new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
      rebateDTO.generalLedgerDebitAccount = null
      rebateDTO.generalLedgerCreditAccount = null

      when:
      def result = post(path, rebateDTO)
      rebateDTO.id = result.id

      then:
      notThrown(Exception)
      result != null
      result.id != null
      result.id > 0
      with(result) {
         id == rebateDTO.id
         vendor.id == rebateDTO.vendor.myId()

         with(status) {
            value == rebateDTO.status.value
            description == rebateDTO.status.description
         }

         description == rebateDTO.description

         with(rebate) {
            value == rebateDTO.rebate.value
            description == rebateDTO.rebate.description
         }

         percent == rebateDTO.percent
         amountPerUnit == rebateDTO.amountPerUnit
         accrualIndicator == rebateDTO.accrualIndicator

         generalLedgerDebitAccount == null
         generalLedgerCreditAccount == null
      }
   }

   @Unroll
   void "create invalid rebate without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor1 = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebate = rebateDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor1), new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
      rebate["$nonNullableProp"] = null

      when:
      post("$path/", rebate)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp            || errorResponsePath
      'accrualIndicator'         || 'accrualIndicator'
      'description'              || 'description'
      'rebate'                   || 'rebate'
      'status'                   || 'status'
      'vendor'                   || 'vendor'
   }

   void "create invalid rebate with percent greater than one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor1 = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebate = rebateDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor1), new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
      rebate.percent = 1.2
      rebate.amountPerUnit = null

      when:
      post("$path/", rebate)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'percent'
      response[0].message == 'Must be in range of (0, 1]'
   }

   void "create invalid rebate with percent and amountPerUnit both null" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor1 = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebate = rebateDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor1), new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
      rebate.percent = null
      rebate.amountPerUnit = null

      when:
      post("$path/", rebate)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'percent, amountPerUnit'
      response[0].message == 'One method must be selected (percent or per unit)'
   }

   void "create invalid rebate with percent and amountPerUnit both not null" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor1 = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebate = rebateDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor1), new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
      rebate.percent = 0.5
      rebate.amountPerUnit = 10

      when:
      post("$path/", rebate)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'percent, amountPerUnit'
      response[0].message == 'One method must be selected (percent or per unit)'
   }

   void "create invalid rebate with non-existing general ledger debit account and general ledger credit account ids" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor1 = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebate = rebateDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor1), new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
      rebate.generalLedgerDebitAccount.id = 0
      rebate.generalLedgerCreditAccount.id = 0

      when:
      post("$path/", rebate)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson().collect().sort { a,b -> a.path <=> b.path }
      response.size() == 2
      response[0].path == "generalLedgerCreditAccount.id"
      response[0].message == "generalLedgerCreditAccount.id must be greater than zero"
      response[1].path == "generalLedgerDebitAccount.id"
      response[1].message == "generalLedgerDebitAccount.id must be greater than zero"
   }

   void "update one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor1 = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      def existingRebate = rebateDataLoaderService.single(company, vendor1, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor1), new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))

      when:
      updatedRebateDTO.id = existingRebate.id
      def result = put("$path/${existingRebate.id}", updatedRebateDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == updatedRebateDTO.id
         vendor.id == updatedRebateDTO.vendor.myId()

         with(status) {
            value == updatedRebateDTO.status.value
            description == updatedRebateDTO.status.description
         }

         description == updatedRebateDTO.description

         with(rebate) {
            value == updatedRebateDTO.rebate.value
            description == updatedRebateDTO.rebate.description
         }

         percent == updatedRebateDTO.percent
         amountPerUnit == updatedRebateDTO.amountPerUnit
         accrualIndicator == updatedRebateDTO.accrualIndicator
         generalLedgerDebitAccount.id == updatedRebateDTO.generalLedgerDebitAccount.id
         generalLedgerCreditAccount.id == updatedRebateDTO.generalLedgerCreditAccount.id
      }
   }

   void "update valid rebate with null general ledger debit account and general ledger credit account" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor1 = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      def existingRebate = rebateDataLoaderService.single(company, vendor1, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor1), new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
      updatedRebateDTO.generalLedgerDebitAccount = null
      updatedRebateDTO.generalLedgerCreditAccount = null

      when:
      updatedRebateDTO.id = existingRebate.id
      def result = put("$path/${existingRebate.id}", updatedRebateDTO)

      then:
      notThrown(Exception)
      result != null
      result.id != null
      result.id > 0
      with(result) {
         id == updatedRebateDTO.id
         vendor.id == updatedRebateDTO.vendor.myId()

         with(status) {
            value == updatedRebateDTO.status.value
            description == updatedRebateDTO.status.description
         }

         description == updatedRebateDTO.description

         with(rebate) {
            value == updatedRebateDTO.rebate.value
            description == updatedRebateDTO.rebate.description
         }

         percent == updatedRebateDTO.percent
         amountPerUnit == updatedRebateDTO.amountPerUnit
         accrualIndicator == updatedRebateDTO.accrualIndicator

         generalLedgerDebitAccount == null
         generalLedgerCreditAccount == null
      }
   }

   void "update invalid rebate without non-nullable properties" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor1 = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      def existingRebate = rebateDataLoaderService.single(company, vendor1, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor1), new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
      updatedRebateDTO.vendor = null
      updatedRebateDTO.status = null
      updatedRebateDTO.description = null
      updatedRebateDTO.rebate = null
      updatedRebateDTO.accrualIndicator = null

      when:
      updatedRebateDTO.id = existingRebate.id
      put("$path/${existingRebate.id}", updatedRebateDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson().collect().sort { a,b -> a.path <=> b.path }
      response.size() == 5
      response[0].path == 'accrualIndicator'
      response[1].path == 'description'
      response[2].path == 'rebate'
      response[3].path == 'status'
      response[4].path == 'vendor'
      response.collect { it.message } as Set == ['Is required'] as Set
   }

   void "update invalid rebate with percent greater than one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor1 = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      def existingRebate = rebateDataLoaderService.single(company, vendor1, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor1), new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
      updatedRebateDTO.percent = 1.2
      updatedRebateDTO.amountPerUnit = null

      when:
      updatedRebateDTO.id = existingRebate.id
      put("$path/${existingRebate.id}", updatedRebateDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'percent'
      response[0].message == 'Must be in range of (0, 1]'
   }

   void "update invalid rebate with percent and amountPerUnit both null" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor1 = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      def existingRebate = rebateDataLoaderService.single(company, vendor1, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor1), new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
      updatedRebateDTO.percent = null
      updatedRebateDTO.amountPerUnit = null

      when:
      updatedRebateDTO.id = existingRebate.id
      put("$path/${existingRebate.id}", updatedRebateDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'percent, amountPerUnit'
      response[0].message == 'One method must be selected (percent or per unit)'
   }

   void "update invalid rebate with percent and amountPerUnit both not null" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor1 = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      def existingRebate = rebateDataLoaderService.single(company, vendor1, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor1), new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
      updatedRebateDTO.percent = 0.5
      updatedRebateDTO.amountPerUnit = 10

      when:
      updatedRebateDTO.id = existingRebate.id
      put("$path/${existingRebate.id}", updatedRebateDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'percent, amountPerUnit'
      response[0].message == 'One method must be selected (percent or per unit)'
   }

   void "update invalid rebate with non-existing general ledger debit account and general ledger credit account ids" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor1 = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      def existingRebate = rebateDataLoaderService.single(company, vendor1, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor1), new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
      updatedRebateDTO.generalLedgerDebitAccount.id = 0
      updatedRebateDTO.generalLedgerCreditAccount.id = 0

      when:
      updatedRebateDTO.id = existingRebate.id
      put("$path/${existingRebate.id}", updatedRebateDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson().collect().sort { a,b -> a.path <=> b.path }
      response.size() == 2
      response[0].path == "generalLedgerCreditAccount.id"
      response[0].message == "generalLedgerCreditAccount.id must be greater than zero"
      response[1].path == "generalLedgerDebitAccount.id"
      response[1].message == "generalLedgerDebitAccount.id must be greater than zero"
   }
}
