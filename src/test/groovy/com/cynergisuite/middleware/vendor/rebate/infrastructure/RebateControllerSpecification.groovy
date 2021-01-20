package com.cynergisuite.middleware.vendor.rebate.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDataLoaderService
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.vendor.VendorDTO
import com.cynergisuite.middleware.vendor.VendorEntity
import com.cynergisuite.middleware.vendor.VendorTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import com.cynergisuite.middleware.vendor.rebate.RebateDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

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
      final vendorList = vendorTestDataLoaderService.stream(4, company, vendorPaymentTerm, shipVia).toList()
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      def rebateEntity = rebateDataLoaderService.single(company, vendorList, glDebitAcct, glCreditAcct)
      rebateDataLoaderService.assignVendorsToRebate(rebateEntity, vendorList)

      when:
      def result = get("$path/${rebateEntity.id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == rebateEntity.id

         vendors.eachWithIndex{ vendor, index ->
            vendor == rebateEntity.vendors[index]
         }

         with(status) {
            value == rebateEntity.status.value
            description == rebateEntity.status.description
         }

         description == rebateEntity.description

         with(type) {
            value == rebateEntity.rebate.value
            description == rebateEntity.rebate.description
         }

         percent == rebateEntity.percent
         amountPerUnit == rebateEntity.amountPerUnit
         accrualIndicator == rebateEntity.accrualIndicator
         if (generalLedgerDebitAccount != null) {
            generalLedgerDebitAccount.id == rebateEntity.generalLedgerDebitAccount.id
         }
         generalLedgerCreditAccount.id == rebateEntity.generalLedgerCreditAccount.id
      }
   }

   void "fetch one not found" () {
      when:
      get("$path/0")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "0 was unable to be found"
   }

   void "fetch all" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorList = vendorTestDataLoaderService.stream(4, company, vendorPaymentTerm, shipVia).toList()
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebates = rebateDataLoaderService.stream(7, company, vendorList, glDebitAcct, glCreditAcct).toList()
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      final firstPageRebate = rebates[0..4]
      final lastPageRebate = rebates[5,6]
      rebates.eachWithIndex{ rebate, _ ->
         rebateDataLoaderService.assignVendorsToRebate(rebate, vendorList)
      }

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

               vendors.eachWithIndex{ vendor, i ->
                  vendor == firstPageRebate[index].vendors[i]
               }

               with(status) {
                  value == firstPageRebate[index].status.value
                  description == firstPageRebate[index].status.description
               }

               description == firstPageRebate[index].description

               with(type) {
                  value == firstPageRebate[index].rebate.value
                  description == firstPageRebate[index].rebate.description
               }

               percent == firstPageRebate[index].percent
               amountPerUnit == firstPageRebate[index].amountPerUnit
               accrualIndicator == firstPageRebate[index].accrualIndicator
               if (generalLedgerDebitAccount != null) {
                  generalLedgerDebitAccount.id == firstPageRebate[index].generalLedgerDebitAccount.id
               }
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

               vendors.eachWithIndex{ vendor, i ->
                  vendor == lastPageRebate[index].vendors[i]
               }

               with(status) {
                  value == lastPageRebate[index].status.value
                  description == lastPageRebate[index].status.description
               }

               description == lastPageRebate[index].description

               with(type) {
                  value == lastPageRebate[index].rebate.value
                  description == lastPageRebate[index].rebate.description
               }

               percent == lastPageRebate[index].percent
               amountPerUnit == lastPageRebate[index].amountPerUnit
               accrualIndicator == lastPageRebate[index].accrualIndicator
               if (generalLedgerDebitAccount != null) {
                  generalLedgerDebitAccount.id == lastPageRebate[index].generalLedgerDebitAccount.id
               }
               generalLedgerCreditAccount.id == lastPageRebate[index].generalLedgerCreditAccount.id
            }
         }
      }
   }

   void "create one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebateDTO = rebateDataLoaderService.singleDTO(null, new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))

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
         vendors == []

         with(status) {
            value == rebateDTO.status.value
            description == rebateDTO.status.description
         }

         description == rebateDTO.description

         with(type) {
            value == rebateDTO.type.value
            description == rebateDTO.type.description
         }

         percent == rebateDTO.percent
         amountPerUnit == rebateDTO.amountPerUnit
         accrualIndicator == rebateDTO.accrualIndicator
         if (generalLedgerDebitAccount != null) {
            generalLedgerDebitAccount.id == rebateDTO.generalLedgerDebitAccount.id
         }
         generalLedgerCreditAccount.id == rebateDTO.generalLedgerCreditAccount.id
      }
   }

   void "create valid rebate with null general ledger debit account" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebateDTO = rebateDataLoaderService.singleDTO(null, new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
      rebateDTO.accrualIndicator = false
      rebateDTO.generalLedgerDebitAccount = null

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
         vendors == []

         with(status) {
            value == rebateDTO.status.value
            description == rebateDTO.status.description
         }

         description == rebateDTO.description

         with(type) {
            value == rebateDTO.type.value
            description == rebateDTO.type.description
         }

         percent == rebateDTO.percent
         amountPerUnit == rebateDTO.amountPerUnit
         accrualIndicator == rebateDTO.accrualIndicator

         generalLedgerDebitAccount == rebateDTO.generalLedgerDebitAccount
         generalLedgerCreditAccount.id == rebateDTO.generalLedgerCreditAccount.id
      }
   }

   void "create invalid rebate with null general ledger debit account" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebateDTO = rebateDataLoaderService.singleDTO(null, new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
      rebateDTO.accrualIndicator = true
      rebateDTO.generalLedgerDebitAccount = null

      when:
      post(path, rebateDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'generalLedgerDebitAccount'
      response[0].message == 'Account is required'
   }

   @Unroll
   void "create invalid rebate without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebate = rebateDataLoaderService.singleDTO(null, new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
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
      nonNullableProp              || errorResponsePath
      'accrualIndicator'           || 'accrualIndicator'
      'description'                || 'description'
      'generalLedgerCreditAccount' || 'generalLedgerCreditAccount'
      'type'                       || 'type'
      'status'                     || 'status'
   }

   void "create invalid rebate with non-existing vendor" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorList = vendorTestDataLoaderService.stream(1, company, vendorPaymentTerm, shipVia).toList()
      List<SimpleIdentifiableDTO> vendorListDTO=[]
      vendorList.eachWithIndex { vendor, index ->
         vendorListDTO << new SimpleIdentifiableDTO(vendor)
      }
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebate = rebateDataLoaderService.singleDTO(vendorListDTO, new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
      rebate.vendors.add(new SimpleIdentifiableDTO(999999))

      when:
      post("$path/", rebate)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'vendors[1].id'
      response[0].message == '999,999 was unable to be found'
   }

   void "create invalid rebate with percent greater than one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebate = rebateDataLoaderService.singleDTO(null, new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
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
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebate = rebateDataLoaderService.singleDTO(null, new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
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
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebate = rebateDataLoaderService.singleDTO(null, new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
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
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebate = rebateDataLoaderService.singleDTO(null, new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
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
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      def existingRebate = rebateDataLoaderService.single(company, null, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateDataLoaderService.singleDTO(null, new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))

      when:
      updatedRebateDTO.id = existingRebate.id
      def result = put("$path/${existingRebate.id}", updatedRebateDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == updatedRebateDTO.id
         vendors == []

         with(status) {
            value == updatedRebateDTO.status.value
            description == updatedRebateDTO.status.description
         }

         description == updatedRebateDTO.description

         with(type) {
            value == updatedRebateDTO.type.value
            description == updatedRebateDTO.type.description
         }

         percent == updatedRebateDTO.percent
         amountPerUnit == updatedRebateDTO.amountPerUnit
         accrualIndicator == updatedRebateDTO.accrualIndicator
         if (generalLedgerDebitAccount != null) {
            generalLedgerDebitAccount.id == updatedRebateDTO.generalLedgerDebitAccount.id
         }
         generalLedgerCreditAccount.id == updatedRebateDTO.generalLedgerCreditAccount.id
      }
   }

   void "update valid rebate with null general ledger debit account" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      def existingRebate = rebateDataLoaderService.single(company, null, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateDataLoaderService.singleDTO(null, new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
      updatedRebateDTO.accrualIndicator = false
      updatedRebateDTO.generalLedgerDebitAccount = null

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

         vendors == []

         with(status) {
            value == updatedRebateDTO.status.value
            description == updatedRebateDTO.status.description
         }

         description == updatedRebateDTO.description

         with(type) {
            value == updatedRebateDTO.type.value
            description == updatedRebateDTO.type.description
         }

         percent == updatedRebateDTO.percent
         amountPerUnit == updatedRebateDTO.amountPerUnit
         accrualIndicator == updatedRebateDTO.accrualIndicator

         generalLedgerDebitAccount == updatedRebateDTO.generalLedgerDebitAccount
         generalLedgerCreditAccount.id == updatedRebateDTO.generalLedgerCreditAccount.id
      }
   }

   void "update invalid rebate with null general ledger debit account" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      def existingRebate = rebateDataLoaderService.single(company, null, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateDataLoaderService.singleDTO(null, new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
      updatedRebateDTO.accrualIndicator = true
      updatedRebateDTO.generalLedgerDebitAccount = null

      when:
      updatedRebateDTO.id = existingRebate.id
      put("$path/${existingRebate.id}", updatedRebateDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'generalLedgerDebitAccount'
      response[0].message == 'Account is required'
   }

   @Unroll
   void "update invalid rebate without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      def existingRebate = rebateDataLoaderService.single(company, null, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateDataLoaderService.singleDTO(null, new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
      updatedRebateDTO["$nonNullableProp"] = null

      when:
      updatedRebateDTO.id = existingRebate.id
      put("$path/${existingRebate.id}", updatedRebateDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp              || errorResponsePath
      'accrualIndicator'           || 'accrualIndicator'
      'description'                || 'description'
      'generalLedgerCreditAccount' || 'generalLedgerCreditAccount'
      'type'                       || 'type'
      'status'                     || 'status'
   }

   void "update invalid rebate with non-existing vendor" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorList = vendorTestDataLoaderService.stream(1, company, vendorPaymentTerm, shipVia).toList()
      List<SimpleIdentifiableDTO> vendorListDTO=[]
      vendorList.eachWithIndex { vendor, index ->
         vendorListDTO << new SimpleIdentifiableDTO(vendor)
      }
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      def existingRebate = rebateDataLoaderService.single(company, vendorList, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateDataLoaderService.singleDTO(vendorListDTO, new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
      updatedRebateDTO.vendors.add(new SimpleIdentifiableDTO(999999))

      when:
      updatedRebateDTO.id = existingRebate.id
      put("$path/${existingRebate.id}", updatedRebateDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'vendors[1].id'
      response[0].message == '999,999 was unable to be found'
   }

   void "update invalid rebate with percent greater than one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      def existingRebate = rebateDataLoaderService.single(company, null, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateDataLoaderService.singleDTO(null, new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
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
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      def existingRebate = rebateDataLoaderService.single(company, null, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateDataLoaderService.singleDTO(null, new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
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
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      def existingRebate = rebateDataLoaderService.single(company, null, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateDataLoaderService.singleDTO(null, new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
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
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      def existingRebate = rebateDataLoaderService.single(company, null, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateDataLoaderService.singleDTO(null, new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))
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

   void "assign vendors to rebate" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorList = vendorTestDataLoaderService.stream(4, company, vendorPaymentTerm, shipVia).toList()
      List<SimpleIdentifiableDTO> vendorListDTO=[]
      vendorList.eachWithIndex { vendor, index ->
         vendorListDTO << new SimpleIdentifiableDTO(vendor)
      }
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebateDTO = rebateDataLoaderService.singleDTO(vendorListDTO, new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))

      when: // create rebate
      def result = post(path, rebateDTO)
      rebateDTO.id = result.id

      then:
      notThrown(Exception)
      result != null
      result.id != null
      result.id > 0

      when: // assign vendors to rebate
      vendorListDTO.eachWithIndex{ vendorDTO, index ->
         result = post("$path/${rebateDTO.id}/vendor", vendorDTO)
      }

      then:
      notThrown(HttpClientResponseException)
      result == null

      when: // update rebate's vendor list
      vendorListDTO.eachWithIndex{ vendorDTO, index ->
         rebateDTO.vendors[index] = vendorDTO
      }
      result = put("$path/${rebateDTO.id}", rebateDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == rebateDTO.id

         vendors.eachWithIndex{ vendor, index ->
            vendor == rebateDTO.vendors[index]
         }

         with(status) {
            value == rebateDTO.status.value
            description == rebateDTO.status.description
         }

         description == rebateDTO.description

         with(type) {
            value == rebateDTO.type.value
            description == rebateDTO.type.description
         }

         percent == rebateDTO.percent
         amountPerUnit == rebateDTO.amountPerUnit
         accrualIndicator == rebateDTO.accrualIndicator
         if (generalLedgerDebitAccount != null) {
            generalLedgerDebitAccount.id == rebateDTO.generalLedgerDebitAccount.id
         }
         generalLedgerCreditAccount.id == rebateDTO.generalLedgerCreditAccount.id
      }
   }

   void "disassociate vendor from rebate" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorList = vendorTestDataLoaderService.stream(4, company, vendorPaymentTerm, shipVia).toList()
      List<SimpleIdentifiableDTO> vendorListDTO=[]
      vendorList.eachWithIndex { vendor, index ->
         vendorListDTO << new SimpleIdentifiableDTO(vendor)
      }
      final glDebitAcct = accountDataLoaderService.single(company)
      final glCreditAcct = accountDataLoaderService.single(company)
      final rebateDTO = rebateDataLoaderService.singleDTO(vendorListDTO, new SimpleIdentifiableDTO(glDebitAcct), new SimpleIdentifiableDTO(glCreditAcct))

      when: // create rebate
      def result = post(path, rebateDTO)
      rebateDTO.id = result.id

      then:
      notThrown(Exception)
      result != null
      result.id != null
      result.id > 0

      when: // assign vendors to rebate
      vendorListDTO.eachWithIndex{ vendorDTO, index ->
         result = post("$path/${rebateDTO.id}/vendor", vendorDTO)
      }

      then:
      notThrown(HttpClientResponseException)
      result == null

      when: // update rebate's vendor list
      vendorListDTO.eachWithIndex{ vendorDTO, index ->
         rebateDTO.vendors[index] = vendorDTO
      }
      result = put("$path/${rebateDTO.id}", rebateDTO)

      then:
      notThrown(Exception)
      result != null
      result.id == rebateDTO.id
      result.vendors.eachWithIndex{ vendor, index ->
         vendor == rebateDTO.vendors[index]
      }

      when: // disassociate a vendor from rebate
      result = delete("$path/${rebateDTO.id}/vendor/${vendorList[0].myId()}")

      then:
      notThrown(HttpClientResponseException)
      result == null

      when: // update rebate's vendor list again
      vendorListDTO.eachWithIndex{ vendorDTO, index ->
         rebateDTO.vendors[index] = vendorDTO
      }
      result = put("$path/${rebateDTO.id}", rebateDTO)

      then:
      notThrown(Exception)
      result != null
      result.id == rebateDTO.id
      result.vendors.eachWithIndex{ vendor, index ->
         vendor == rebateDTO.vendors[index]
      }
   }
}
