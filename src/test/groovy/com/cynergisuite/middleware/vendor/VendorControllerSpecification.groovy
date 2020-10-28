package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.SearchPageRequest
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.address.AddressTestDataLoader
import com.cynergisuite.middleware.address.AddressDTO
import com.cynergisuite.middleware.address.AddressTestDataLoaderService
import com.cynergisuite.middleware.error.ErrorDataTransferObject
import com.cynergisuite.middleware.shipping.freight.calc.method.FreightCalcMethodTypeDTO
import com.cynergisuite.middleware.shipping.freight.calc.method.infrastructure.FreightCalcMethodTypeRepository
import com.cynergisuite.middleware.shipping.freight.onboard.FreightOnboardTypeDTO
import com.cynergisuite.middleware.shipping.freight.onboard.infrastructure.FreightOnboardTypeRepository
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.shipping.shipvia.ShipViaValueObject
import com.cynergisuite.middleware.vendor.group.VendorGroupEntity
import com.cynergisuite.middleware.vendor.group.VendorGroupTestDataLoaderService
import com.cynergisuite.middleware.vendor.group.infrastructure.VendorGroupRepository
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleEntity
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class VendorControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/vendor"

   @Inject AddressTestDataLoaderService addressTestDataLoaderService
   @Inject FreightOnboardTypeRepository freightOnboardTypeRepository
   @Inject FreightCalcMethodTypeRepository freightCalcMethodTypeRepository
   @Inject ShipViaTestDataLoaderService shipViaFactoryService
   @Inject VendorRepository vendorRepository
   @Inject VendorGroupRepository vendorGroupRepository
   @Inject VendorGroupTestDataLoaderService vendorGroupTestDataLoaderService
   @Inject VendorTestDataLoaderService vendorTestDataLoaderService
   @Inject VendorPaymentTermRepository vendorPaymentTermRepository
   @Inject VendorPaymentTermTestDataLoaderService vendorPaymentTermTestDataLoaderService

   void "fetch one with a single vendor payment term schedule" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
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
         new ShipViaValueObject(shipVia) == new ShipViaValueObject(vendor.shipVia)
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
      final shipVia = shipViaFactoryService.single(company)
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
         new ShipViaValueObject(shipVia) == new ShipViaValueObject(vendor.shipVia)
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
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendors = vendorTestDataLoaderService.stream(7, company, vendorPaymentTerm, shipVia).toList()
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")

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
         new VendorDTO(elements[0]) == new VendorDTO(vendors[0])
         new VendorDTO(elements[1]) == new VendorDTO(vendors[1])
         new VendorDTO(elements[2]) == new VendorDTO(vendors[2])
         new VendorDTO(elements[3]) == new VendorDTO(vendors[3])
         new VendorDTO(elements[4]) == new VendorDTO(vendors[4])
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
         new VendorDTO(elements[0]) == new VendorDTO(vendors[5])
         new VendorDTO(elements[1]) == new VendorDTO(vendors[6])
      }
   }

   void "create one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
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
      result.id > 0
      result.address.id != null
      result.address.id > 0
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
      response.collect { new ErrorDataTransferObject(it.message, it.path) }.sort { o1, o2 -> o1 <=> o2 } == [
         new ErrorDataTransferObject("Cannot be blank", "name"),
         new ErrorDataTransferObject("Is required", "allowDropShipToCustomer"),
         new ErrorDataTransferObject("Is required", "autoSubmitPurchaseOrder"),
         new ErrorDataTransferObject("Is required", "chargeInventoryTax1"),
         new ErrorDataTransferObject("Is required", "chargeInventoryTax2"),
         new ErrorDataTransferObject("Is required", "chargeInventoryTax3"),
         new ErrorDataTransferObject("Is required", "chargeInventoryTax4"),
         new ErrorDataTransferObject("Is required", "federalIdNumberVerification"),
         new ErrorDataTransferObject("Is required", "freightCalcMethodType"),
         new ErrorDataTransferObject("Is required", "freightOnboardType"),
         new ErrorDataTransferObject("Is required", "name"),
         new ErrorDataTransferObject("Is required", "paymentTerm"),
         new ErrorDataTransferObject("Is required", "returnPolicy"),
         new ErrorDataTransferObject("Is required", "separateCheck"),
         new ErrorDataTransferObject("Is required", "shipVia"),
         new ErrorDataTransferObject("Is required", "vendor1099"),
      ]
   }

   void "create with payTo" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
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
      result.id > 0
      result.address.id != null
      result.address.id > 0
      result.payTo.id == payToVendor.id
      new VendorDTO(result) == vendor
   }

   void "create with another company's shipVia, vendorPaymentTerm" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final company2 = companyFactoryService.forDatasetCode('tstds2')
      final shipVia = shipViaFactoryService.single(company2)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company2)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }

      when:
      post(path, vendor)

      then:
      final ex = thrown(HttpClientResponseException)
      ex.status == BAD_REQUEST
      final response = ex.response.bodyAsJson()
      response.size() == 2
      response.collect { new ErrorDataTransferObject(it.message, it.path) }.sort { o1, o2 -> o1 <=> o2 } == [
         new ErrorDataTransferObject("${String.format('%d', vendorPaymentTerm.id)} was unable to be found", "paymentTerm.id"),
         new ErrorDataTransferObject("${String.format('%d', shipVia.id)} was unable to be found", "shipVia.id"),
      ].sort { o1, o2 -> o1 <=> o2 }
   }

   void "create valid vendor with vendor group" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
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
      result.id > 0
      result.address.id != null
      result.address.id > 0
      result.vendorGroup != null
      new VendorDTO(result) == vendor
   }

   void "create valid vendor without vendor group" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
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
      result.id > 0
      result.address.id != null
      result.address.id > 0
      result.vendorGroup == null
      new VendorDTO(result) == vendor
   }

   void "create valid vendor without address and our account number" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
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
      result.id > 0
      result.address == null
      result.accountNumber == null

      new VendorDTO(result) == vendor
   }

   void "create valid vendor with allow drop ship to customer and auto submit purchase order set to true" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
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
      result.id > 0
      result.address.id != null
      result.address.id > 0
      result.vendorGroup != null
      result.allowDropShipToCustomer == true
      result.autoSubmitPurchaseOrder == true
      new VendorDTO(result) == vendor
   }

   void "create valid vendor with allow drop ship to customer and auto submit purchase order set to false" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
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
      result.id > 0
      result.address.id != null
      result.address.id > 0
      result.vendorGroup != null
      result.allowDropShipToCustomer == false
      result.autoSubmitPurchaseOrder == false
      new VendorDTO(result) == vendor
   }

   void "create invalid vendor with non-existing vendor group" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
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

   void "create invalid vendor with null email address" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      vendor.autoSubmitPurchaseOrder = true
      vendor.emailAddress = null

      when:
      post(path, vendor)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "emailAddress"
      response[0].message == "Is required"
   }

   void "create invalid vendor with invalid email address" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      vendor.autoSubmitPurchaseOrder = true
      vendor.emailAddress = "invalidEmail"

      when:
      post(path, vendor)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "emailAddress"
      response[0].message == "invalidEmail is an invalid email address"
   }

   void "create invalid vendor with null po submit email address" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      vendor.autoSubmitPurchaseOrder = true
      vendor.purchaseOrderSubmitEmailAddress = null

      when:
      post(path, vendor)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "purchaseOrderSubmitEmailAddress"
      response[0].message == "Is required"
   }

   void "create invalid vendor with invalid po submit email address" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      vendor.autoSubmitPurchaseOrder = true
      vendor.purchaseOrderSubmitEmailAddress = "invalidEmail"

      when:
      post(path, vendor)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "purchaseOrderSubmitEmailAddress"
      response[0].message == "invalidEmail is an invalid email address"
   }

   void "create invalid vendor with null allow drop ship to customer" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      vendor.allowDropShipToCustomer = null

      when:
      post(path, vendor)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "allowDropShipToCustomer"
      response[0].message == "Is required"
   }

   void "create invalid vendor with null auto submit purchase order" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      vendor.autoSubmitPurchaseOrder = null

      when:
      post(path, vendor)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "autoSubmitPurchaseOrder"
      response[0].message == "Is required"
   }

   void "update one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final address = addressTestDataLoaderService.single()
      final shipVia = shipViaFactoryService.single(company)
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
      final shipVia = shipViaFactoryService.single(company)
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
      result.address.id > 0
      result.address.id == vendor.address.id
      new VendorDTO(result) == vendor
   }

   void "update invalid vendor that assigns itself as pay to vendor" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
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
      final shipVia = shipViaFactoryService.single(company)
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
      final shipVia = shipViaFactoryService.single(company)
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

   void "update vendor with null po submit email address" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final vendorUpdate = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      vendorUpdate.autoSubmitPurchaseOrder = true
      vendorUpdate.purchaseOrderSubmitEmailAddress = null

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
      response[0].message == "Is required"
   }

   void "update vendor with invalid po submit email address" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
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
      final shipVia = shipViaFactoryService.single(company)
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

   void "update invalid vendor with null allow drop ship to customer field" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final vendorUpdate = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      vendorUpdate.allowDropShipToCustomer = null

      when:
      vendorUpdate.id = vendor.id
      vendorUpdate.id = vendor.getAddress().myId()
      put("$path/${vendor.id}", vendorUpdate)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "allowDropShipToCustomer"
      response[0].message == "Is required"
   }

   void "update invalid vendor with null auto submit purchase order field" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final vendorUpdate = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      vendorUpdate.autoSubmitPurchaseOrder = null

      when:
      vendorUpdate.id = vendor.id
      vendorUpdate.id = vendor.getAddress().myId()
      put("$path/${vendor.id}", vendorUpdate)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "autoSubmitPurchaseOrder"
      response[0].message == "Is required"
   }

   void "search vendors" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')

      final addressVO = new AddressDTO(1, "Test Address", "123 Test St", "Suite 1100", "Corpus Christi", "TX", "78418", 11.01, 42.07, "USA", "Nueces", "361777777", "3612222222")
      final addressEntity = new AddressEntity(addressVO)

      final schedules = [new VendorPaymentTermScheduleEntity(null, null, 90, 1.0, 1)]
      final VPT = new VendorPaymentTermEntity(null, company, "test1", null, null, null, schedules)
      final vendorPaymentTerm = vendorPaymentTermRepository.insert(VPT)

      final shipVia = shipViaFactoryService.single(nineNineEightEmployee.company)

      final groupEntity = new VendorGroupEntity(null, company, "Test Group", "Group used for testing!")
      final vendorGroup = vendorGroupRepository.insert(groupEntity, company)

      final onboard = freightOnboardTypeRepository.findOne(1)
      final method = freightCalcMethodTypeRepository.findOne(1)

      final vendorEntity1 = new VendorEntity(null, company, "Vendor 1", addressEntity, '12345678910', null, onboard, vendorPaymentTerm, 0, false, shipVia, vendorGroup, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com", null, false, false, null, null, null)
      vendorRepository.insert(vendorEntity1).with { new VendorDTO(it) }
      final vendorEntity2 = new VendorEntity(null, company, "Vendor 2", addressEntity, '12345678910', null, onboard, vendorPaymentTerm, 0, false, shipVia, vendorGroup, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com", null, false, false, null, "Note something", null)
      vendorRepository.insert(vendorEntity2).with { new VendorDTO(it) }
      final vendorEntity3 = new VendorEntity(null, company, "Vendor 3", addressEntity, '12345678910', null, onboard, vendorPaymentTerm, 0, false, shipVia, vendorGroup, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com", null, false, false, null, null, "316317318")
      vendorRepository.insert(vendorEntity3).with { new VendorDTO(it) }
      final vendorEntity4 = new VendorEntity(null, company, "Out of search result 1", addressEntity, '12345678910', null, onboard, vendorPaymentTerm, 0, false, shipVia, vendorGroup, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com", null, false, false, null, "Other note", "316123456")
      vendorRepository.insert(vendorEntity4).with { new VendorDTO(it) }
      final vendorEntity5 = new VendorEntity(null, company, "Out of search result 2", addressEntity, '12345678910', null, onboard, vendorPaymentTerm, 0, false, shipVia, vendorGroup, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com", null, false, false, null, null, null)
      vendorRepository.insert(vendorEntity5).with { new VendorDTO(it) }
      def searchOne = new SearchPageRequest([query:"Vandor%202"])
      def searchFivePageOne = new SearchPageRequest([page:1, size:5, query:"Vandor%202"])
      def searchFivePageTwo = new SearchPageRequest([page:2, size:5, query:"Vandor%202"])
      def searchSqlInjection = new SearchPageRequest([query:"%20or%201=1;drop%20table%20account;--"])

      when:
      def searchOneResult = get("$path/search${searchOne}")

      then:
      notThrown(HttpClientException)
      searchOneResult.totalElements == 3
      searchOneResult.totalPages == 1
      searchOneResult.first == true
      searchOneResult.last == true
      searchOneResult.elements.size() == 3
      searchOneResult.elements[0].name == 'Vendor 2'
      searchOneResult.elements[1].name == 'Vendor 3'
      searchOneResult.elements[2].name == 'Vendor 1'

      when:
      def searchTwoPageOneResult = get("$path/search${searchFivePageOne}")

      then:
      notThrown(HttpClientException)
      searchTwoPageOneResult.requested.with { new SearchPageRequest(it) } == searchFivePageOne
      searchTwoPageOneResult.totalElements == 3
      searchTwoPageOneResult.totalPages == 1
      searchTwoPageOneResult.first == true
      searchTwoPageOneResult.last == true
      searchTwoPageOneResult.elements.size() == 3
      searchTwoPageOneResult.elements[0].name == 'Vendor 2'
      searchTwoPageOneResult.elements[1].name == 'Vendor 3'

      when:
      get("$path/search${searchFivePageTwo}")

      then:
      def notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT

      when:
      def searchSqlInjectionResult = get("$path/search${searchSqlInjection}")

      then:
      notThrown(HttpClientException)
      searchSqlInjectionResult.requested.query == ' or 1=1'
      searchSqlInjectionResult.totalElements < 5
   }

   void "search vendors by name not fuzzy" () {
      given: "Three vendors 2 with similar names"
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
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
         new ShipViaValueObject(shipVia) == new ShipViaValueObject(vendorOne.shipVia)
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
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final randomVendors = vendorTestDataLoaderService.stream(20, company, vendorPaymentTerm, shipVia)
      final targetVendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia, "Super Awesome Company")

      when: "A specific vendor is searched for by their 'number'"
      def result = get("$path/search?query=${targetVendor.id}&fuzzy=false")

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
         new ShipViaValueObject(shipVia) == new ShipViaValueObject(targetVendor.shipVia)
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

   void "Try creating vendor with a negative accountNumber" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')

      final addressVO = new AddressDTO(1, "Test Address", "123 Test St", "Suite 1100", "Corpus Christi", "TX", "78418", 11.01, 42.07, "USA", "Nueces", "361777777", "3612222222")
      final addressEntity = new AddressEntity(addressVO)

      final schedules = [new VendorPaymentTermScheduleEntity(null, null, 90, 1.0, 1)]
      final VPT = new VendorPaymentTermEntity(null, company, "test1", null, null, null, schedules)
      final vendorPaymentTerm = vendorPaymentTermRepository.insert(VPT)

      final shipVia = shipViaFactoryService.single(nineNineEightEmployee.company)

      final VGRP = new VendorGroupEntity(null, company, "Test Group", "Group used for testing!")
      final vendorGroup = vendorGroupRepository.insert(VGRP, company)

      final onboard = freightOnboardTypeRepository.findOne(1)
      final method = freightCalcMethodTypeRepository.findOne(1)

      final vendorEntity = new VendorEntity(null, company, "test1", addressEntity, '12345678910', null, onboard, vendorPaymentTerm, 0, false, shipVia, vendorGroup, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com", null, false, false, null, null, null)
      final vendor = vendorRepository.insert(vendorEntity).with { new VendorDTO(it) }

      when:
      vendor.accountNumber = -1
      post(path, vendor)

      then:
      final ex = thrown(HttpClientResponseException)
      ex.status == BAD_REQUEST
      final response = ex.response.bodyAsJson()
      response.size() == 1
      response.collect { new ErrorDataTransferObject(it.message, it.path) } == [
         new ErrorDataTransferObject("accountNumber must be greater than zero", "accountNumber")
      ]
   }

   void "Create vendor with bumpPercent that has 2 integral and 8 fractional" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')

      final addressVO = new AddressDTO(1, "Test Address", "123 Test St", "Suite 1100", "Corpus Christi", "TX", "78418", 11.01, 42.07, "USA", "Nueces", "361777777", "3612222222")
      final addressEntity = new AddressEntity(addressVO)

      final schedules = [new VendorPaymentTermScheduleEntity(null, null, 90, 1.0, 1)]
      final VPT = new VendorPaymentTermEntity(null, company, "test1", null, null, null, schedules)
      final vendorPaymentTerm = vendorPaymentTermRepository.insert(VPT)

      final shipVia = shipViaFactoryService.single(nineNineEightEmployee.company)

      final VGRP = new VendorGroupEntity(null, company, "Test Group", "Group used for testing!")
      final vendorGroup = vendorGroupRepository.insert(VGRP, company)

      final onboard = freightOnboardTypeRepository.findOne(1)
      final method = freightCalcMethodTypeRepository.findOne(1)

      final vendorEntity = new VendorEntity(null, company, "test1", addressEntity, '12345678910', null, onboard, vendorPaymentTerm, 0, false, shipVia, vendorGroup, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com", null, false, false, null, "Note something", null)
      final vendor = vendorRepository.insert(vendorEntity).with { new VendorDTO(it) }

      when:
      vendor.bumpPercent = 20.00000008
      post(path, vendor)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].message == "20.00000008 is out of range for bumpPercent"
      response[0].path == "bumpPercent"
   }
}
