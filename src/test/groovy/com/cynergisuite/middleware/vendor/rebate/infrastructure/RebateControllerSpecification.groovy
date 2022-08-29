package com.cynergisuite.middleware.vendor.rebate.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountStatusFactory
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.vendor.VendorTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import com.cynergisuite.middleware.vendor.rebate.RebateEntity
import com.cynergisuite.middleware.vendor.rebate.RebateTestDataLoaderService
import com.cynergisuite.middleware.vendor.rebate.RebateTypeDataLoader
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import jakarta.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class RebateControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/vendor/rebate"

   @Inject AccountTestDataLoaderService accountTestDataLoaderService
   @Inject RebateTestDataLoaderService rebateTestDataLoaderService
   @Inject ShipViaTestDataLoaderService shipViaTestDataLoaderService
   @Inject VendorPaymentTermTestDataLoaderService vendorPaymentTermTestDataLoaderService
   @Inject VendorTestDataLoaderService vendorTestDataLoaderService
   @Inject RebateRepository rebateRepository

   void "fetch one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorList = vendorTestDataLoaderService.stream(4, company, vendorPaymentTerm, shipVia).toList()
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      final rebateEntity = rebateTestDataLoaderService.single(company, vendorList, glDebitAcct, glCreditAcct)
      rebateTestDataLoaderService.assignVendorsToRebate(rebateEntity, vendorList)

      when:
      def result = get("$path/${rebateEntity.id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == rebateEntity.id

         vendors.size() == 4
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
      given:
      final nonExistentId = UUID.randomUUID()

      when:
      get("$path/$nonExistentId")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$nonExistentId was unable to be found"
      response.code == 'system.not.found'
   }

   void "fetch all" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorList = vendorTestDataLoaderService.stream(4, company, vendorPaymentTerm, shipVia).toList()
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      final rebates = rebateTestDataLoaderService.stream(7, company, vendorList, glDebitAcct, glCreditAcct).toList()
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      final firstPageRebate = rebates[0..4]
      final lastPageRebate = rebates[5,6]
      rebates.eachWithIndex{ rebate, _ ->
         rebateTestDataLoaderService.assignVendorsToRebate(rebate, vendorList)
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

   void "fetch by vendors" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorList = vendorTestDataLoaderService.stream(4, company, vendorPaymentTerm, shipVia).toList()
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      final rebates = rebateTestDataLoaderService.stream(7, company, vendorList, glDebitAcct, glCreditAcct).toList()
      final filterRequest = new RebatePageRequest(['page': 1, 'size': 10, 'sortBy': "id", 'sortDirection': "ASC", 'vendorIds': [vendorList.get(1).myId()]])
      rebates.eachWithIndex{ rebate, _ ->
         rebateTestDataLoaderService.assignVendorsToRebate(rebate, vendorList)
      }

      when:
      def result = get("$path$filterRequest")

      then:
      // result should returns all rebases
      notThrown(Exception)
      with(result) {
         totalElements == 7
         totalPages == 1
         first == true
         last == true
         elements.size() == 7
         elements.eachWithIndex { pageOneResult, index ->
            with(pageOneResult) {
               id == rebates[index].id

               vendors.eachWithIndex{ vendor, i ->
                  vendor == rebates[index].vendors[i]
               }

               with(status) {
                  value == rebates[index].status.value
                  description == rebates[index].status.description
               }

               description == rebates[index].description

               with(type) {
                  value == rebates[index].rebate.value
                  description == rebates[index].rebate.description
               }

               percent == rebates[index].percent
               amountPerUnit == rebates[index].amountPerUnit
               accrualIndicator == rebates[index].accrualIndicator
               if (generalLedgerDebitAccount != null) {
                  generalLedgerDebitAccount.id == rebates[index].generalLedgerDebitAccount.id
               }
               generalLedgerCreditAccount.id == rebates[index].generalLedgerCreditAccount.id
            }
         }
      }

//      when:
//      // disassociate 2 last rebates from the first vendor
//      rebates[5..6].eachWithIndex{ rebate, _ ->
//         rebateTestDataLoaderService.disassociateVendorFromRebate(rebate, [vendorList.get(1)])
//      }
//      result = get("$path$filterRequest")
//
//      then:
//      // result should returns 5 out of 7 rebases
//      notThrown(Exception)
//      with(result) {
//         totalElements == 5
//         totalPages == 1
//         first == true
//         last == true
//         elements.size() == 5
//         elements.eachWithIndex { element, index ->
//            with(element) {
//               id == rebates[index].id
//
//               vendors.eachWithIndex{ vendor, i ->
//                  vendor == rebates[index].vendors[i]
//               }
//
//               with(status) {
//                  value == rebates[index].status.value
//                  description == rebates[index].status.description
//               }
//
//               description == rebates[index].description
//
//               with(type) {
//                  value == rebates[index].rebate.value
//                  description == rebates[index].rebate.description
//               }
//
//               percent == rebates[index].percent
//               amountPerUnit == rebates[index].amountPerUnit
//               accrualIndicator == rebates[index].accrualIndicator
//               if (generalLedgerDebitAccount != null) {
//                  generalLedgerDebitAccount.id == rebates[index].generalLedgerDebitAccount.id
//               }
//               generalLedgerCreditAccount.id == rebates[index].generalLedgerCreditAccount.id
//            }
//         }
//      }
//
//      when:
//      // disassociate all rebates from the first vendor
//      rebates[0..4].eachWithIndex{ rebate, _ ->
//         rebateTestDataLoaderService.disassociateVendorFromRebate(rebate, [vendorList.get(1)])
//      }
//      result = get("$path$filterRequest")
//
//      then:
//      // should throws an HttpClientResponseException
//      final exception = thrown(HttpClientResponseException)
//      exception.response.status == NO_CONTENT
   }

   void "create one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      final rebateDTO = rebateTestDataLoaderService.singleDTO(null, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))

      when:
      def result = post(path, rebateDTO)
      rebateDTO.id = result.id

      then:
      notThrown(Exception)
      result != null
      result.id != null
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
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      final rebateDTO = rebateTestDataLoaderService.singleDTO(null, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))
      rebateDTO.accrualIndicator = false
      rebateDTO.generalLedgerDebitAccount = null

      when:
      def result = post(path, rebateDTO)
      rebateDTO.id = result.id

      then:
      notThrown(Exception)
      result != null
      result.id != null
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
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      final rebateDTO = rebateTestDataLoaderService.singleDTO(null, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))
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
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      final rebate = rebateTestDataLoaderService.singleDTO(null, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))
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
      final invalidVendorId = UUID.randomUUID()
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorList = vendorTestDataLoaderService.stream(1, company, vendorPaymentTerm, shipVia).toList()
      List<SimpleIdentifiableDTO> vendorListDTO=[]
      vendorList.eachWithIndex { vendor, index ->
         vendorListDTO << new SimpleIdentifiableDTO(vendor)
      }
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      final rebate = rebateTestDataLoaderService.singleDTO(vendorListDTO, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))
      rebate.vendors.add(new SimpleIdentifiableDTO(invalidVendorId))

      when:
      post("$path/", rebate)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'vendors[1].id'
      response[0].message == "${invalidVendorId} was unable to be found"
      response[0].code == 'system.not.found'
   }

   void "create invalid rebate with percent greater than one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      final rebate = rebateTestDataLoaderService.singleDTO(null, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))
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
      response[0].code == 'javax.validation.constraints.DecimalMax.message'
      response[0].message == 'must be less than or equal to value'
   }

   void "create invalid rebate with percent and amountPerUnit both null" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      final rebate = rebateTestDataLoaderService.singleDTO(null, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))
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
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      final rebate = rebateTestDataLoaderService.singleDTO(null, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))
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
      final generalLedgerDebitAccountNonExistentId = UUID.fromString('72bab362-2399-4884-8614-de144273e16e')
      final generalLedgerCreditAccountNonExistentId = UUID.fromString('42686b2e-5379-4f13-9889-5871261f724c')
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      final rebate = rebateTestDataLoaderService.singleDTO(null, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))
      rebate.accrualIndicator = false
      rebate.generalLedgerDebitAccount.id = generalLedgerDebitAccountNonExistentId
      rebate.generalLedgerCreditAccount.id = generalLedgerCreditAccountNonExistentId

      when:
      post("$path/", rebate)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson().collect().sort { a,b -> a.path <=> b.path }
      response.size() == 2
      response[0].path == "generalLedgerCreditAccount.id"
      response[0].message == "$generalLedgerCreditAccountNonExistentId was unable to be found"
      response[0].code == 'system.not.found'
      response[1].path == "generalLedgerDebitAccount.id"
      response[1].message == "$generalLedgerDebitAccountNonExistentId was unable to be found"
      response[1].code == 'system.not.found'
   }

   void "update one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      def existingRebate = rebateTestDataLoaderService.single(company, null, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateTestDataLoaderService.singleDTO(null, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))

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
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      def existingRebate = rebateTestDataLoaderService.single(company, null, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateTestDataLoaderService.singleDTO(null, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))
      updatedRebateDTO.accrualIndicator = false
      updatedRebateDTO.generalLedgerDebitAccount = null

      when:
      updatedRebateDTO.id = existingRebate.id
      def result = put("$path/${existingRebate.id}", updatedRebateDTO)

      then:
      notThrown(Exception)
      result != null
      result.id != null
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
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      def existingRebate = rebateTestDataLoaderService.single(company, null, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateTestDataLoaderService.singleDTO(null, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))
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
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      def existingRebate = rebateTestDataLoaderService.single(company, null, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateTestDataLoaderService.singleDTO(null, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))
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
      final nonExistentVendorId = UUID.randomUUID()
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorList = vendorTestDataLoaderService.stream(1, company, vendorPaymentTerm, shipVia).toList()
      List<SimpleIdentifiableDTO> vendorListDTO=[]
      vendorList.eachWithIndex { vendor, index ->
         vendorListDTO << new SimpleIdentifiableDTO(vendor)
      }
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      def existingRebate = rebateTestDataLoaderService.single(company, vendorList, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateTestDataLoaderService.singleDTO(vendorListDTO, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))
      updatedRebateDTO.vendors.add(new SimpleIdentifiableDTO(nonExistentVendorId))

      when:
      updatedRebateDTO.id = existingRebate.id
      put("$path/${existingRebate.id}", updatedRebateDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'vendors[1].id'
      response[0].message == "${nonExistentVendorId} was unable to be found"
      response[0].code == 'system.not.found'
   }

   void "update invalid rebate with percent greater than one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      def existingRebate = rebateTestDataLoaderService.single(company, null, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateTestDataLoaderService.singleDTO(null, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))
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
      response[0].code == 'javax.validation.constraints.DecimalMax.message'
      response[0].message == 'must be less than or equal to value'
   }

   void "update invalid rebate with percent and amountPerUnit both null" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      def existingRebate = rebateTestDataLoaderService.single(company, null, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateTestDataLoaderService.singleDTO(null, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))
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
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      def existingRebate = rebateTestDataLoaderService.single(company, null, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateTestDataLoaderService.singleDTO(null, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))
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
      final generalLedgerDebitAccountNonExistentId = UUID.fromString('72bab362-2399-4884-8614-de144273e16e')
      final generalLedgerCreditAccountNonExistentId = UUID.fromString('42686b2e-5379-4f13-9889-5871261f724c')
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      def existingRebate = rebateTestDataLoaderService.single(company, null, glDebitAcct, glCreditAcct)
      def updatedRebateDTO = rebateTestDataLoaderService.singleDTO(null, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))
      updatedRebateDTO.accrualIndicator = false
      updatedRebateDTO.generalLedgerDebitAccount.id = generalLedgerDebitAccountNonExistentId
      updatedRebateDTO.generalLedgerCreditAccount.id = generalLedgerCreditAccountNonExistentId

      when:
      updatedRebateDTO.id = existingRebate.id
      put("$path/${existingRebate.id}", updatedRebateDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson().collect().sort { a,b -> a.path <=> b.path }
      response.size() == 2
      response[0].path == "generalLedgerCreditAccount.id"
      response[0].message == "$generalLedgerCreditAccountNonExistentId was unable to be found"
      response[0].code == 'system.not.found'
      response[1].path == "generalLedgerDebitAccount.id"
      response[1].message == "$generalLedgerDebitAccountNonExistentId was unable to be found"
      response[1].code == 'system.not.found'
   }

   void "assign vendors to rebate" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorList = vendorTestDataLoaderService.stream(4, company, vendorPaymentTerm, shipVia).toList()
      List<SimpleIdentifiableDTO> vendorListDTO=[]
      vendorList.eachWithIndex { vendor, index ->
         vendorListDTO << new SimpleIdentifiableDTO(vendor)
      }
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      final rebateDTO = rebateTestDataLoaderService.singleDTO(vendorListDTO, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))

      when: // create rebate
      def result = post(path, rebateDTO)
      rebateDTO.id = result.id

      then:
      notThrown(Exception)
      result != null
      result.id != null

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
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorList = vendorTestDataLoaderService.stream(4, company, vendorPaymentTerm, shipVia).toList()
      List<SimpleIdentifiableDTO> vendorListDTO=[]
      vendorList.eachWithIndex { vendor, index ->
         vendorListDTO << new SimpleIdentifiableDTO(vendor)
      }
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      final rebateDTO = rebateTestDataLoaderService.singleDTO(vendorListDTO, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))

      when: // create rebate
      def result = post(path, rebateDTO)
      rebateDTO.id = result.id

      then:
      notThrown(Exception)
      result != null
      result.id != null

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

   void "create one with duplicate description" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      def updatedRebateDTO = rebateTestDataLoaderService.singleDTO(null, new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct))
      updatedRebateDTO.description = 'test 1'
      final rebateEnt = new RebateEntity(UUID.randomUUID(), updatedRebateDTO, null, AccountStatusFactory.random(),  RebateTypeDataLoader.random(), glDebitAcct, glCreditAcct, )
      rebateRepository.insert(rebateEnt, company)
      when:

      def result = post(path, updatedRebateDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson().collect().sort { a,b -> a.path <=> b.path }
      response.size() == 1
      response[0].path == "description"
      response[0].message == "test 1 already exists"
      response[0].code == 'cynergi.validation.duplicate'
   }

}
