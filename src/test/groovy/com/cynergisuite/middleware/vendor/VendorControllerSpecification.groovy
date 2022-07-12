package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.SearchPageRequest
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDataLoaderService
import com.cynergisuite.middleware.accounting.bank.BankFactoryService
import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.address.AddressTestDataLoader
import com.cynergisuite.middleware.address.AddressDTO
import com.cynergisuite.middleware.address.AddressTestDataLoaderService
import com.cynergisuite.middleware.error.ErrorDTO
import com.cynergisuite.middleware.shipping.freight.calc.method.FreightCalcMethodTypeDTO
import com.cynergisuite.middleware.shipping.freight.calc.method.infrastructure.FreightCalcMethodTypeRepository
import com.cynergisuite.middleware.shipping.freight.onboard.FreightOnboardTypeDTO
import com.cynergisuite.middleware.shipping.freight.onboard.infrastructure.FreightOnboardTypeRepository
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.shipping.shipvia.ShipViaDTO
import com.cynergisuite.middleware.vendor.group.VendorGroupEntity
import com.cynergisuite.middleware.vendor.group.VendorGroupTestDataLoaderService
import com.cynergisuite.middleware.vendor.group.infrastructure.VendorGroupRepository
import com.cynergisuite.middleware.vendor.infrastructure.VendorPageRequest
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import com.cynergisuite.middleware.vendor.infrastructure.VendorSearchPageRequest
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleEntity
import com.cynergisuite.middleware.vendor.rebate.RebateTestDataLoader
import com.cynergisuite.middleware.vendor.rebate.RebateTestDataLoaderService
import com.google.common.net.UrlEscapers
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import jakarta.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.CONFLICT
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class VendorControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/vendor"

   @Inject AccountPayablePaymentDataLoaderService accountPayablePaymentDataLoaderService
   @Inject AccountTestDataLoaderService accountTestDataLoaderService
   @Inject AddressTestDataLoaderService addressTestDataLoaderService
   @Inject BankFactoryService bankFactoryService
   @Inject FreightOnboardTypeRepository freightOnboardTypeRepository
   @Inject FreightCalcMethodTypeRepository freightCalcMethodTypeRepository
   @Inject RebateTestDataLoaderService rebateTestDataLoaderService
   @Inject ShipViaTestDataLoaderService shipViaTestDataLoaderService
   @Inject VendorRepository vendorRepository
   @Inject VendorGroupRepository vendorGroupRepository
   @Inject VendorGroupTestDataLoaderService vendorGroupTestDataLoaderService
   @Inject VendorTestDataLoaderService vendorTestDataLoaderService
   @Inject VendorPaymentTermRepository vendorPaymentTermRepository
   @Inject VendorPaymentTermTestDataLoaderService vendorPaymentTermTestDataLoaderService

   void "fetch one with a single vendor payment term schedule" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)

      when:
      def result = get("$path/${vendor.id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == vendor.id
         name == vendor.name
         new AddressDTO(address) == new AddressDTO(vendor.address)
         accountNumber == vendor.accountNumber
         new FreightOnboardTypeDTO(freightOnboardType) == new FreightOnboardTypeDTO(vendor.freightOnboardType)
         paymentTerm.id == vendor.paymentTerm.id
         returnPolicy == vendor.returnPolicy
         new ShipViaDTO(shipVia) == new ShipViaDTO(vendor.shipVia)
         minimumQuantity == vendor.minimumQuantity
         minimumAmount?.toString() == vendor.minimumAmount?.toString()
         freeShipQuantity == vendor.freeShipQuantity
         freeShipAmount?.toString() == vendor.freeShipAmount?.toString()
         vendor1099 == vendor.vendor1099
         federalIdNumber == vendor.federalIdNumber
         salesRepresentativeName == vendor.salesRepresentativeName
         salesRepresentativeFax == vendor.salesRepresentativeFax
         separateCheck == vendor.separateCheck
         new FreightCalcMethodTypeDTO(freightCalcMethodType) == new FreightCalcMethodTypeDTO(vendor.freightCalcMethodType)
         freightPercent?.toString() == vendor.freightPercent?.toString()
         chargeInventoryTax1 == vendor.chargeInventoryTax1
         chargeInventoryTax2 == vendor.chargeInventoryTax2
         chargeInventoryTax3 == vendor.chargeInventoryTax3
         chargeInventoryTax4 == vendor.chargeInventoryTax4
         federalIdNumberVerification == vendor.federalIdNumberVerification
         emailAddress == vendor.emailAddress
         purchaseOrderSubmitEmailAddress == vendor.purchaseOrderSubmitEmailAddress
         allowDropShipToCustomer == vendor.allowDropShipToCustomer
         autoSubmitPurchaseOrder == vendor.autoSubmitPurchaseOrder
         number == vendor.number
         note == vendor.note
         phone == vendor.phone
      }
   }

   void "fetch one with a two vendor payment term schedule" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)

      when:
      def result = get("$path/${vendor.id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == vendor.id
         name == vendor.name
         new AddressDTO(address) == new AddressDTO(vendor.address)
         accountNumber == vendor.accountNumber
         new FreightOnboardTypeDTO(freightOnboardType) == new FreightOnboardTypeDTO(vendor.freightOnboardType)
         paymentTerm.id == vendor.paymentTerm.id
         returnPolicy == vendor.returnPolicy
         new ShipViaDTO(shipVia) == new ShipViaDTO(vendor.shipVia)
         minimumQuantity == vendor.minimumQuantity
         minimumAmount?.toString() == vendor.minimumAmount?.toString()
         freeShipQuantity == vendor.freeShipQuantity
         freeShipAmount?.toString() == vendor.freeShipAmount?.toString()
         vendor1099 == vendor.vendor1099
         federalIdNumber == vendor.federalIdNumber
         salesRepresentativeName == vendor.salesRepresentativeName
         salesRepresentativeFax == vendor.salesRepresentativeFax
         separateCheck == vendor.separateCheck
         new FreightCalcMethodTypeDTO(freightCalcMethodType) == new FreightCalcMethodTypeDTO(vendor.freightCalcMethodType)
         freightPercent?.toString() == vendor.freightPercent?.toString()
         chargeInventoryTax1 == vendor.chargeInventoryTax1
         chargeInventoryTax2 == vendor.chargeInventoryTax2
         chargeInventoryTax3 == vendor.chargeInventoryTax3
         chargeInventoryTax4 == vendor.chargeInventoryTax4
         federalIdNumberVerification == vendor.federalIdNumberVerification
         emailAddress == vendor.emailAddress
         purchaseOrderSubmitEmailAddress == vendor.purchaseOrderSubmitEmailAddress
         allowDropShipToCustomer == vendor.allowDropShipToCustomer
         autoSubmitPurchaseOrder == vendor.autoSubmitPurchaseOrder
         number == vendor.number
         note == vendor.note
         phone == vendor.phone
      }
   }

   void "fetch all" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendors = vendorTestDataLoaderService.stream(7, company, vendorPaymentTerm, shipVia).toList().sort { o1, o2 -> o1.id <=> o2.id }
      final pageOne = new VendorPageRequest([page: 1, size: 5, sortBy: "id", sortDirection: "ASC"])
      final pageTwo = new VendorPageRequest([page: 2, size: 5, sortBy: "id", sortDirection: "ASC", active: false])

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
         elements.collect { new VendorDTO(it) }
            .sort {o1, o2 -> o1.id <=> o2.id} == vendors[0..4].collect { new VendorDTO(it) }
      }

      when:
      result = get("$path$pageTwo")

      then:
      final ex = thrown(HttpClientResponseException)
      ex.status == NO_CONTENT
   }

   void "create one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }

      when:
      def result = post(path, vendor)
      vendor.id = result.id
      vendor.address.id = result.address?.id
      vendor.number = result.number

      then:
      notThrown(Exception)
      result != null
      result.id != null
      result.address.id != null
      new VendorDTO(result) == vendor
   }

   void "create with all nulls" () {
      given: "An empty Vendor"
      final vendor = new VendorDTO()

      when: "The empty Vendor is posted"
      post(path, vendor)

      then: "The server responds with Bad Request"
      final ex = thrown(HttpClientResponseException)
      ex.status == BAD_REQUEST
      final response = ex.response.bodyAsJson()
      response.size() == 16
      response.collect { new ErrorDTO(it.message, it.code, it.path) }.sort { o1, o2 -> o1 <=> o2 } == [
         new ErrorDTO("Cannot be blank", "javax.validation.constraints.NotNull.message", "name"),
         new ErrorDTO("Is required", "javax.validation.constraints.NotNull.message", "allowDropShipToCustomer"),
         new ErrorDTO("Is required", "javax.validation.constraints.NotNull.message", "autoSubmitPurchaseOrder"),
         new ErrorDTO("Is required", "javax.validation.constraints.NotNull.message", "chargeInventoryTax1"),
         new ErrorDTO("Is required", "javax.validation.constraints.NotNull.message", "chargeInventoryTax2"),
         new ErrorDTO("Is required", "javax.validation.constraints.NotNull.message", "chargeInventoryTax3"),
         new ErrorDTO("Is required", "javax.validation.constraints.NotNull.message", "chargeInventoryTax4"),
         new ErrorDTO("Is required", "javax.validation.constraints.NotNull.message", "federalIdNumberVerification"),
         new ErrorDTO("Is required", "javax.validation.constraints.NotNull.message", "freightCalcMethodType"),
         new ErrorDTO("Is required", "javax.validation.constraints.NotNull.message", "freightOnboardType"),
         new ErrorDTO("Is required", "javax.validation.constraints.NotNull.message", "name"),
         new ErrorDTO("Is required", "javax.validation.constraints.NotNull.message", "paymentTerm"),
         new ErrorDTO("Is required", "javax.validation.constraints.NotNull.message", "returnPolicy"),
         new ErrorDTO("Is required", "javax.validation.constraints.NotNull.message", "separateCheck"),
         new ErrorDTO("Is required", "javax.validation.constraints.NotNull.message", "shipVia"),
         new ErrorDTO("Is required", "javax.validation.constraints.NotNull.message", "vendor1099"),
      ].sort { o1, o2 -> o1 <=> o2 }
   }

   void "create with payTo" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final payToVendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }

      when:
      vendor.payTo = new SimpleIdentifiableDTO(payToVendor)
      def result = post(path, vendor)
      vendor.id = result.id
      vendor.address.id = result.address?.id
      vendor.number = result.number

      then:
      notThrown(Exception)
      result != null
      result.id != null
      result.address.id != null
      result.payTo.id == payToVendor.id
      new VendorDTO(result) == vendor
   }

   void "create with another company's shipVia, vendorPaymentTerm" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final company2 = companyFactoryService.forDatasetCode('tstds2')
      final shipVia = shipViaTestDataLoaderService.single(company2)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company2)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }

      when:
      post(path, vendor)

      then:
      final ex = thrown(HttpClientResponseException)
      ex.status == BAD_REQUEST
      final response = ex.response.bodyAsJson()
      response.size() == 2
      response.collect { new ErrorDTO(it.message, it.code, it.path) }.sort { o1, o2 -> o1 <=> o2 } == [
         new ErrorDTO("${vendorPaymentTerm.id} was unable to be found", 'system.not.found', "paymentTerm.id"),
         new ErrorDTO("${shipVia.id} was unable to be found", 'system.not.found', "shipVia.id"),
      ].sort { o1, o2 -> o1 <=> o2 }
   }

   void "create valid vendor with vendor group" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      List<VendorGroupEntity> vendorGroups = new ArrayList<VendorGroupEntity>()
      companies.each {
         vendorGroups.addAll(vendorGroupTestDataLoaderService.stream(it).collect())
      }
      vendor.vendorGroup = new SimpleIdentifiableDTO(vendorGroups[0].id)

      when:
      def result = post(path, vendor)
      vendor.id = result.id
      vendor.address.id = result.address?.id
      vendor.vendorGroup = result.vendorGroup
      vendor.number = result.number

      then:
      notThrown(Exception)
      result != null
      result.id != null
      result.address.id != null
      result.vendorGroup != null
      new VendorDTO(result) == vendor
   }

   void "create valid vendor without vendor group" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      List<VendorGroupEntity> vendorGroups = new ArrayList<VendorGroupEntity>()
      companies.each {
         vendorGroups.addAll(vendorGroupTestDataLoaderService.stream(it).collect())
      }
      vendor.vendorGroup = null

      when:
      def result = post(path, vendor)
      vendor.id = result.id
      vendor.address.id = result.address?.id
      //vendor.vendorGroup = result.vendorGroup
      vendor.number = result.number

      then:
      notThrown(Exception)
      result != null
      result.id != null
      result.address.id != null
      result.vendorGroup == null
      new VendorDTO(result) == vendor
   }

   void "create valid vendor without address and account number" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      vendor.address = null
      vendor.accountNumber = null

      when:
      def result = post(path, vendor)
      vendor.id = result.id
      vendor.number = result.number

      then:
      notThrown(Exception)
      result.address == null
      result.accountNumber == null

      new VendorDTO(result) == vendor
   }

   void "create valid vendor with allow drop ship to customer and auto submit purchase order set to true" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      List<VendorGroupEntity> vendorGroups = new ArrayList<VendorGroupEntity>()
      companies.each {
         vendorGroups.addAll(vendorGroupTestDataLoaderService.stream(it).collect())
      }
      vendor.vendorGroup = new SimpleIdentifiableDTO(vendorGroups[0].id)

      vendor.allowDropShipToCustomer = true
      vendor.autoSubmitPurchaseOrder = true

      when:
      def result = post(path, vendor)
      vendor.id = result.id
      vendor.address.id = result.address?.id
      vendor.vendorGroup = result.vendorGroup
      vendor.number = result.number

      then:
      notThrown(Exception)
      result != null
      result.id != null
      result.address.id != null
      result.vendorGroup != null
      result.allowDropShipToCustomer == true
      result.autoSubmitPurchaseOrder == true
      new VendorDTO(result) == vendor
   }

   void "create valid vendor with allow drop ship to customer and auto submit purchase order set to false" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      List<VendorGroupEntity> vendorGroups = new ArrayList<VendorGroupEntity>()
      companies.each {
         vendorGroups.addAll(vendorGroupTestDataLoaderService.stream(it).collect())
      }
      vendor.vendorGroup = new SimpleIdentifiableDTO(vendorGroups[0].id)

      vendor.allowDropShipToCustomer = false
      vendor.autoSubmitPurchaseOrder = false

      when:
      def result = post(path, vendor)
      vendor.id = result.id
      vendor.address.id = result.address?.id
      vendor.vendorGroup = result.vendorGroup
      vendor.number = result.number

      then:
      notThrown(Exception)
      result != null
      result.id != null
      result.address.id != null
      result.vendorGroup != null
      result.allowDropShipToCustomer == false
      result.autoSubmitPurchaseOrder == false
      new VendorDTO(result) == vendor
   }

   void "create invalid vendor with non-existing vendor group" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      List<VendorGroupEntity> vendorGroups = new ArrayList<VendorGroupEntity>()
      companies.each {
         vendorGroups.addAll(vendorGroupTestDataLoaderService.stream(it).collect())
      }
      vendor.vendorGroup = new SimpleIdentifiableDTO(vendorGroups[3].id)

      when:
      post(path, vendor)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "vendorGroup.id"
      response[0].message == "${vendorGroups[3].id} was unable to be found"
   }

   @Unroll
   void "create invalid vendor without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      vendor.autoSubmitPurchaseOrder = true
      vendor["$nonNullableProp"] = null

      when:
      post(path, vendor)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == "Is required"

      where:
      nonNullableProp                        || errorResponsePath
      'emailAddress'                         || 'emailAddress'
      'purchaseOrderSubmitEmailAddress'      || 'purchaseOrderSubmitEmailAddress'
      'allowDropShipToCustomer'              || 'allowDropShipToCustomer'
      'autoSubmitPurchaseOrder'              || 'autoSubmitPurchaseOrder'
   }

   @Unroll
   void "create invalid vendor with invalid #testProp = #invalidValue" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      vendor["$testProp"] = invalidValue

      when:
      post(path, vendor)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == testProp
      response[0].code == errorCode
      response[0].message == errorMessage

      where:
      testProp                          | invalidValue      || errorCode                                          | errorMessage
      'bumpPercent'                     | -0.1212345        || 'javax.validation.constraints.DecimalMin.message'  | 'must be greater than or equal to value'
      'bumpPercent'                     | 0.123123456       || 'javax.validation.constraints.Digits.message'      | '0.123123456 is out of range for bumpPercent'
      'bumpPercent'                     | 10                || 'javax.validation.constraints.Digits.message'      | '10.0 is out of range for bumpPercent'
      'bumpPercent'                     | 0                 || 'javax.validation.constraints.DecimalMin.message'  | 'must be greater than or equal to value'
      'freightPercent'                  | -0.1212345        || 'javax.validation.constraints.DecimalMin.message'  | 'must be greater than or equal to value'
      'freightPercent'                  | 0.123123456       || 'javax.validation.constraints.Digits.message'      | '0.123123456 is out of range for freightPercent'
      'freightPercent'                  | 10                || 'javax.validation.constraints.Digits.message'      | '10.0 is out of range for freightPercent'
      'freightPercent'                  | 0                 || 'javax.validation.constraints.DecimalMin.message'  | 'must be greater than or equal to value'
      'freightAmount'                   | -0.1234567        || 'javax.validation.constraints.DecimalMin.message'  | 'must be greater than or equal to value'
      'freightAmount'                   | 0                 || 'javax.validation.constraints.DecimalMin.message'  | 'must be greater than or equal to value'
      'emailAddress'                    | 'invalidEmail'    || 'javax.validation.constraints.Email.message'       | 'invalidEmail is an invalid email address'
      'purchaseOrderSubmitEmailAddress' | 'invalidEmail'    || 'javax.validation.constraints.Email.message'       | 'invalidEmail is an invalid email address'
   }

   @Unroll
   void "create valid vendor with valid #testProp=#testValue" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      vendor["$testProp"] = testValue

      when:
      post(path, vendor)

      then:
      notThrown(Exception)

      where:
      testProp          | testValue
      'bumpPercent'     | 0.12345678
      'bumpPercent'     | 1
      'freightPercent'  | 0.12345678
      'freightPercent'  | 1
      'freightAmount'   | 123456.78
      'freightAmount'   | 0.99
   }

   void "update one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final address = addressTestDataLoaderService.single()
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = vendorTestDataLoaderService.single(company, address, vendorPaymentTerm, shipVia)
      final newVendorAddress = AddressTestDataLoader.single()
      final vendorUpdate = vendorTestDataLoaderService.single(company, newVendorAddress, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }

      when: "All properties are updated including address"
      vendorUpdate.id = vendor.id
      vendorUpdate.address.id = vendor.address.myId()
      vendorUpdate.number = vendor.number
      def result = put("$path/${vendor.id}", vendorUpdate)

      then: "Everything should be different except the vendor.id"
      notThrown(Exception)
      result != null
      result.id != null
      result.id == vendor.id
      result.address.id == vendor.address.id
      new VendorDTO(result) == vendorUpdate
   }

   void "update only address" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      final newVendorAddress = AddressTestDataLoader.single().with { new AddressDTO(it) }

      when: "None of the properties except those on vendor.address are changed"
      newVendorAddress.id = vendor.address.id
      vendor.address = newVendorAddress
      def result = put("$path/${vendor.id}", vendor)
      vendor.address.id = result.address?.id

      then: "Nothing should be different except for the properties on vendor.address excluding vendor.address.id"
      notThrown(Exception)
      result != null
      result.id != null
      result.id == vendor.id
      result.address.id != null
      result.address.id == vendor.address.id
      new VendorDTO(result) == vendor
   }

   void "update invalid vendor that assigns itself as pay to vendor" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final vendorUpdate = new VendorDTO(VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia))
      vendorUpdate.payTo = new SimpleIdentifiableDTO(vendor.id)

      when:
      vendorUpdate.id = vendor.id
      vendorUpdate.address.id = vendor.getAddress().myId()
      put("$path/${vendor.id}", vendorUpdate)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "payTo.id"
      response[0].message == "Invalid Pay To Vendor ${vendor.id}"
   }

   void "update vendor with null email address" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final vendorUpdate = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      vendorUpdate.emailAddress = null
      vendorUpdate.autoSubmitPurchaseOrder = true

      when:
      vendorUpdate.id = vendor.id
      vendorUpdate.address.id = vendor.getAddress().myId()
      put("$path/${vendor.id}", vendorUpdate)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "emailAddress"
      response[0].message == "Is required"
   }

   void "update vendor with invalid email address" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final vendorUpdate = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      vendorUpdate.autoSubmitPurchaseOrder = true
      vendorUpdate.emailAddress = "invalidEmail"

      when:
      vendorUpdate.id = vendor.id
      vendorUpdate.address.id = vendor.address.myId()
      put("$path/${vendor.id}", vendorUpdate)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "emailAddress"
      response[0].message == "invalidEmail is an invalid email address"
   }

   @Unroll
   void "update invalid vendor without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final vendorUpdate = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      vendorUpdate.id = vendor.id
      vendorUpdate.address.id = vendor.address.myId()
      vendorUpdate.autoSubmitPurchaseOrder = true
      vendorUpdate["$nonNullableProp"] = null

      when:
      put("$path/${vendor.id}", vendorUpdate)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == "Is required"

      where:
      nonNullableProp                        || errorResponsePath
      'emailAddress'                         || 'emailAddress'
      'purchaseOrderSubmitEmailAddress'      || 'purchaseOrderSubmitEmailAddress'
      'allowDropShipToCustomer'              || 'allowDropShipToCustomer'
      'autoSubmitPurchaseOrder'              || 'autoSubmitPurchaseOrder'
   }

   void "update vendor with invalid po submit email address" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final vendorUpdate = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      vendorUpdate.autoSubmitPurchaseOrder = true
      vendorUpdate.purchaseOrderSubmitEmailAddress = "invalidEmail"

      when:
      vendorUpdate.id = vendor.id
      vendorUpdate.address.id = vendor.address.myId()
      put("$path/${vendor.id}", vendorUpdate)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "purchaseOrderSubmitEmailAddress"
      response[0].message == "invalidEmail is an invalid email address"
   }

   void "update vendor and toggle allow drop ship to customer and auto submit purchase order fields" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final newVendorAddress = AddressTestDataLoader.single().with { new AddressDTO(it) }
      final vendorUpdate = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }

      final existingAllowDropShipToCustomer = vendorUpdate.allowDropShipToCustomer
      final existingAutoSubmitPurchaseOrder = vendorUpdate.autoSubmitPurchaseOrder
      vendorUpdate.allowDropShipToCustomer = !existingAllowDropShipToCustomer
      vendorUpdate.autoSubmitPurchaseOrder = !existingAutoSubmitPurchaseOrder

      when:
      vendorUpdate.id = vendor.id
      newVendorAddress.id = vendor.getAddress().myId()
      vendorUpdate.address = newVendorAddress
      vendorUpdate.number = vendor.number
      def result = put("$path/${vendor.id}", vendorUpdate)

      then:
      notThrown(Exception)
      result != null
      result.id != null
      result.id == vendor.id
      result.address.id == vendor.address.id
      result.allowDropShipToCustomer == !existingAllowDropShipToCustomer
      result.autoSubmitPurchaseOrder == !existingAutoSubmitPurchaseOrder
      new VendorDTO(result) == vendorUpdate
   }

   void "search vendors" () {
      given:
      final urlFragmentEscaper = UrlEscapers.urlFragmentEscaper()
      final addressId = UUID.randomUUID()
      final company = companyFactoryService.forDatasetCode('tstds1')

      final addressVO = new AddressDTO(addressId, "Test Address", "123 Test St", "Suite 1100", "Corpus Christi", "TX", "78418", 11.01, 42.07, "USA", "Nueces", "361777777", "3612222222")
      final addressEntity = new AddressEntity(addressVO)

      final schedules = [new VendorPaymentTermScheduleEntity(null, null, 90, 1.0, 1)]
      final VPT = new VendorPaymentTermEntity(null, company, "test1", null, null, null, schedules)
      final vendorPaymentTerm = vendorPaymentTermRepository.insert(VPT)

      final shipVia = shipViaTestDataLoaderService.single(nineNineEightEmployee.company)

      final groupEntity = new VendorGroupEntity(null, company, "Test Group", "Group used for testing!")
      final vendorGroup = vendorGroupRepository.insert(groupEntity, company)

      final onboard = freightOnboardTypeRepository.findOne(1)
      final method = freightCalcMethodTypeRepository.findOne(1)

      final vendorEntity1 = new VendorEntity(null, company, "Vendor 1", addressEntity, '12345678910', null, onboard, vendorPaymentTerm, 0, false, shipVia, vendorGroup, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com", null, false, false, null, null, null, true)
      vendorRepository.insert(vendorEntity1).with { new VendorDTO(it) }
      final vendorEntity2 = new VendorEntity(null, company, "Vendor 2", addressEntity, '12345678910', null, onboard, vendorPaymentTerm, 0, false, shipVia, vendorGroup, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com", null, false, false, null, "Note something", null, true)
      vendorRepository.insert(vendorEntity2).with { new VendorDTO(it) }
      final vendorEntity3 = new VendorEntity(null, company, "Vendor 3", addressEntity, '12345678910', null, onboard, vendorPaymentTerm, 0, false, shipVia, vendorGroup, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com", null, false, false, null, null, "316317318", false)
      vendorRepository.insert(vendorEntity3).with { new VendorDTO(it) }
      final vendorEntity4 = new VendorEntity(null, company, "Out of search result 1", addressEntity, '12345678910', null, onboard, vendorPaymentTerm, 0, false, shipVia, vendorGroup, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com", null, false, false, null, "Other note", "316123456", true)
      vendorRepository.insert(vendorEntity4).with { new VendorDTO(it) }
      final vendorEntity5 = new VendorEntity(null, company, "Out of search result 2", addressEntity, '12345678910', null, onboard, vendorPaymentTerm, 0, false, shipVia, vendorGroup, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com", null, false, false, null, null, null, true)
      vendorRepository.insert(vendorEntity5).with { new VendorDTO(it) }
      def searchOne = new VendorSearchPageRequest([query:"Vandor%202", active:true])
      def searchFivePageOne = new VendorSearchPageRequest([page:1, size:5, query:"Vandor%202", active:true])
      def searchFivePageTwo = new VendorSearchPageRequest([page:2, size:5, query:"Vandor%202"])
      def searchSqlInjection = new VendorSearchPageRequest([query: urlFragmentEscaper.escape("or 1=1;drop table account;--")])

      when:
      def searchOneResult = get("$path/search${searchOne}")

      then:
      notThrown(HttpClientException)
      searchOneResult.totalElements == 2
      searchOneResult.totalPages == 1
      searchOneResult.first == true
      searchOneResult.last == true
      searchOneResult.elements.size() == 2
      searchOneResult.elements[0].name == 'Vendor 2'

      when:
      def searchTwoPageOneResult = get("$path/search${searchFivePageOne}")

      then:
      notThrown(HttpClientException)
      searchTwoPageOneResult.requested.with { new VendorSearchPageRequest(it) } == searchFivePageOne
      searchTwoPageOneResult.totalElements == 2
      searchTwoPageOneResult.totalPages == 1
      searchTwoPageOneResult.first == true
      searchTwoPageOneResult.last == true
      searchTwoPageOneResult.elements.size() == 2
      searchTwoPageOneResult.elements[0].name == 'Vendor 2'

      when:
      get("$path/search${searchFivePageTwo}")

      then:
      def notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT

      when:
      def searchSqlInjectionResult = get("$path/search${searchSqlInjection}")

      then:
      notThrown(HttpClientException)
      searchSqlInjectionResult.requested.query == 'or 1=1' //;drop table account;--' the server is stripping this off for some reason
      searchSqlInjectionResult.totalElements < 5
   }

   void "search vendors by name not fuzzy" () {
      given: "Three vendors 2 with similar names"
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendorOne = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia, "Vendor One")
      final vendorTwo = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia, "Vendor Two")
      final vendorThree = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia, "Couch Maker")

      when: "A first page is requested searching for Vendor One"
      def result = get("$path/search?query=vendor%20one&fuzzy=false")

      then: "1 element should come back"
      notThrown(HttpClientResponseException)
      result.totalElements == 1
      result.first == true
      result.last == true
      with(result.elements[0]) {
         id == vendorOne.id
         name == vendorOne.name
         new AddressDTO(address) == new AddressDTO(vendorOne.address)
         accountNumber == vendorOne.accountNumber
         new FreightOnboardTypeDTO(freightOnboardType) == new FreightOnboardTypeDTO(vendorOne.freightOnboardType)
         paymentTerm.id == vendorOne.paymentTerm.id
         returnPolicy == vendorOne.returnPolicy
         new ShipViaDTO(shipVia) == new ShipViaDTO(vendorOne.shipVia)
         minimumQuantity == vendorOne.minimumQuantity
         minimumAmount?.toString() == vendorOne.minimumAmount?.toString()
         freeShipQuantity == vendorOne.freeShipQuantity
         freeShipAmount?.toString() == vendorOne.freeShipAmount?.toString()
         vendor1099 == vendorOne.vendor1099
         federalIdNumber == vendorOne.federalIdNumber
         salesRepresentativeName == vendorOne.salesRepresentativeName
         salesRepresentativeFax == vendorOne.salesRepresentativeFax
         separateCheck == vendorOne.separateCheck
         new FreightCalcMethodTypeDTO(freightCalcMethodType) == new FreightCalcMethodTypeDTO(vendorOne.freightCalcMethodType)
         freightPercent?.toString() == vendorOne.freightPercent?.toString()
         chargeInventoryTax1 == vendorOne.chargeInventoryTax1
         chargeInventoryTax2 == vendorOne.chargeInventoryTax2
         chargeInventoryTax3 == vendorOne.chargeInventoryTax3
         chargeInventoryTax4 == vendorOne.chargeInventoryTax4
         federalIdNumberVerification == vendorOne.federalIdNumberVerification
         emailAddress == vendorOne.emailAddress
         purchaseOrderSubmitEmailAddress == vendorOne.purchaseOrderSubmitEmailAddress
         allowDropShipToCustomer == vendorOne.allowDropShipToCustomer
         autoSubmitPurchaseOrder == vendorOne.autoSubmitPurchaseOrder
         number == vendorOne.number
         note == vendorOne.note
         phone == vendorOne.phone
      }
   }

   void "search vendors by number" () {
      given: "A random collection of 20 vendors with a target vendor"
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final randomVendors = vendorTestDataLoaderService.stream(20, company, vendorPaymentTerm, shipVia)
      final targetVendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia, "Super Awesome Company")

      when: "A specific vendor is searched for by their 'number'"
      def result = get("$path/search?query=${targetVendor.number}&fuzzy=false")

      then: "That vendor is returned"
      notThrown(HttpClientResponseException)
      result.totalElements == 1
      result.first == true
      result.last == true
      with(result.elements[0]) {
         id == targetVendor.id
         name == targetVendor.name
         new AddressDTO(address) == new AddressDTO(targetVendor.address)
         accountNumber == targetVendor.accountNumber
         new FreightOnboardTypeDTO(freightOnboardType) == new FreightOnboardTypeDTO(targetVendor.freightOnboardType)
         paymentTerm.id == targetVendor.paymentTerm.id
         returnPolicy == targetVendor.returnPolicy
         new ShipViaDTO(shipVia) == new ShipViaDTO(targetVendor.shipVia)
         minimumQuantity == targetVendor.minimumQuantity
         minimumAmount?.toString() == targetVendor.minimumAmount?.toString()
         freeShipQuantity == targetVendor.freeShipQuantity
         freeShipAmount?.toString() == targetVendor.freeShipAmount?.toString()
         vendor1099 == targetVendor.vendor1099
         federalIdNumber == targetVendor.federalIdNumber
         salesRepresentativeName == targetVendor.salesRepresentativeName
         salesRepresentativeFax == targetVendor.salesRepresentativeFax
         separateCheck == targetVendor.separateCheck
         new FreightCalcMethodTypeDTO(freightCalcMethodType) == new FreightCalcMethodTypeDTO(targetVendor.freightCalcMethodType)
         freightPercent?.toString() == targetVendor.freightPercent?.toString()
         chargeInventoryTax1 == targetVendor.chargeInventoryTax1
         chargeInventoryTax2 == targetVendor.chargeInventoryTax2
         chargeInventoryTax3 == targetVendor.chargeInventoryTax3
         chargeInventoryTax4 == targetVendor.chargeInventoryTax4
         federalIdNumberVerification == targetVendor.federalIdNumberVerification
         emailAddress == targetVendor.emailAddress
         purchaseOrderSubmitEmailAddress == targetVendor.purchaseOrderSubmitEmailAddress
         allowDropShipToCustomer == targetVendor.allowDropShipToCustomer
         autoSubmitPurchaseOrder == targetVendor.autoSubmitPurchaseOrder
         number == targetVendor.number
         note == targetVendor.note
         phone == targetVendor.phone
      }

      when: "Throw SQL Injection at it"
      result = get("$path/search?query=%20or%201=1;drop%20table%20account;--")

      then:
      def ex = thrown(HttpClientResponseException)
      ex.response.status == NO_CONTENT
   }

   void "assign rebates to vendor" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }

      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      def rebateList = rebateTestDataLoaderService.stream(5, company, [], glDebitAcct, glCreditAcct).toList()
      def rebateDTOList = RebateTestDataLoader.streamDTO(5, [], new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct)).toList()

      when: // create vendor
      def result = post(path, vendor)
      vendor.id = result.id

      then:
      notThrown(Exception)
      result != null
      result.id != null

      when: // assign rebates to vendor
      rebateDTOList.eachWithIndex { rebateDTO, index ->
         rebateDTO.id = rebateList[index].id
         result = post("/vendor/rebate/${rebateDTO.id}/vendor", vendor)
      }

      then:
      notThrown(HttpClientResponseException)
      result == null

      when: // update rebates' vendor lists
      def vendorDTO = new SimpleIdentifiableDTO(vendor.id)
      def resultList = []
      rebateDTOList.eachWithIndex { rebateDTO, index ->
         rebateDTO.vendors.add(vendorDTO)
         resultList.add(put("/vendor/rebate/${rebateDTO.id}", rebateDTO))
      }

      then:
      notThrown(Exception)
      resultList.eachWithIndex { rebateResult, index ->
         rebateResult != null
         with(rebateResult) {
            id == rebateDTOList[index].id

            vendors.eachWithIndex { v, i ->
               v == rebateDTOList[index].vendors[i]
            }

            with(status) {
               value == rebateDTOList[index].status.value
               description == rebateDTOList[index].status.description
            }

            description == rebateDTOList[index].description

            with(type) {
               value == rebateDTOList[index].type.value
               description == rebateDTOList[index].type.description
            }

            percent == rebateDTOList[index].percent
            amountPerUnit == rebateDTOList[index].amountPerUnit
            accrualIndicator == rebateDTOList[index].accrualIndicator
            if (generalLedgerDebitAccount != null) {
               generalLedgerDebitAccount.id == rebateDTOList[index].generalLedgerDebitAccount.id
            }
            generalLedgerCreditAccount.id == rebateDTOList[index].generalLedgerCreditAccount.id
         }
      }
   }

   void "disassociate rebate from vendor" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }

      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      def rebateList = rebateTestDataLoaderService.stream(5, company, [], glDebitAcct, glCreditAcct).toList()
      def rebateDTOList = RebateTestDataLoader.streamDTO(5, [], new AccountDTO(glDebitAcct), new AccountDTO(glCreditAcct)).toList()

      when: // create vendor
      def result = post(path, vendor)
      vendor.id = result.id

      then:
      notThrown(Exception)
      result != null
      result.id != null

      when: // assign rebates to vendor
      rebateDTOList.eachWithIndex { rebateDTO, index ->
         rebateDTO.id = rebateList[index].id
         result = post("/vendor/rebate/${rebateDTO.id}/vendor", vendor)
      }

      then:
      notThrown(HttpClientResponseException)
      result == null

      when: // update rebates' vendor lists
      def vendorDTO = new SimpleIdentifiableDTO(vendor.id)
      def resultList = []
      rebateDTOList.eachWithIndex { rebateDTO, index ->
         rebateDTO.vendors.add(vendorDTO)
         resultList.add(put("/vendor/rebate/${rebateDTO.id}", rebateDTO))
      }

      then:
      notThrown(Exception)
      resultList.eachWithIndex { rebateResult, index ->
         rebateResult != null
         rebateResult.id == rebateDTOList[index].id
         rebateResult.vendors.eachWithIndex { v, i ->
            v == rebateDTOList[index].vendors[i]
         }
      }

      when: // disassociate a rebate from the vendor
      result = delete("/vendor/rebate/${rebateDTOList[0].id}/vendor/${vendor.id}")

      then:
      notThrown(HttpClientResponseException)
      result == null

      when: // update rebates' vendor lists again
      def newResultList = []
      rebateDTOList.eachWithIndex { rebateDTO, index ->
         rebateDTO.vendors.add(vendorDTO)
         newResultList.add(put("/vendor/rebate/${rebateDTO.id}", rebateDTO))
      }

      then:
      notThrown(Exception)
      newResultList.eachWithIndex { rebateResult, index ->
         rebateResult != null
         rebateResult.id == rebateDTOList[index].id
         rebateResult.vendors.eachWithIndex { v, i ->
            v == rebateDTOList[index].vendors[i]
         }
      }
   }

   void "delete vendor" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)

      when:
      delete("$path/$vendor.id", )

      then: "vendor of user's company is deleted"
      notThrown(HttpClientResponseException)

      when:
      get("$path/$vendor.id")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$vendor.id was unable to be found"
      response.code == 'system.not.found'
   }

   void "delete vendor still has references" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final apPaymentVendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)

      final store = storeFactoryService.store(3, company)
      final account = accountTestDataLoaderService.single(company)
      final bank = bankFactoryService.single(company, store, account)
      accountPayablePaymentDataLoaderService.single(company, bank, apPaymentVendor)

      when:
      delete("$path/$apPaymentVendor.id", )

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == CONFLICT
      def response = exception.response.bodyAsJson()
      response.message == "Requested operation violates data integrity"
      response.code == "cynergi.data.constraint.violated"
   }

   void "delete vendor from other company is not allowed" () {
      given:
      def tstds2 = companies.find { it.datasetCode == "tstds2" }
      final shipVia = shipViaTestDataLoaderService.single(tstds2)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(tstds2)
      final vendor = vendorTestDataLoaderService.single(tstds2, vendorPaymentTerm, shipVia)

      when:
      delete("$path/$vendor.id")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$vendor.id was unable to be found"
      response.code == 'system.not.found'
   }

   void "recreate deleted vendor" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }

      when: // create a vendor
      def response1 = post("$path/", vendor)
      vendor.id = response1.id
      vendor.address.id = response1.address?.id
      vendor.number = response1.number

      then:
      notThrown(Exception)
      response1 != null
      response1.id != null
      response1.address.id != null
      response1.number != null
      response1.number > 0
      new VendorDTO(response1) == vendor

      when: // delete vendor
      delete("$path/$response1.id")

      then: "vendor of user's company is deleted"
      notThrown(HttpClientResponseException)

      when: // recreate vendor
      def response2 = post("$path/", vendor)
      vendor.id = response2.id
      vendor.address.id = response2.address?.id
      vendor.number = response2.number

      then:
      notThrown(Exception)
      response2 != null
      response2.id != null
      response2.address.id != null
      response2.number != null
      response2.number > 0
      new VendorDTO(response2) == vendor

      when: // delete vendor again
      delete("$path/$response2.id")

      then: "vendor of user's company is deleted"
      notThrown(HttpClientResponseException)
   }
}
