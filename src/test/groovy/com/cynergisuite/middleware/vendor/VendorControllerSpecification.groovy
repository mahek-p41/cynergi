package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.SearchPageRequest
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.address.AddressRepository
import com.cynergisuite.middleware.address.AddressTestDataLoader
import com.cynergisuite.middleware.address.AddressValueObject
import com.cynergisuite.middleware.error.ErrorDataTransferObject
import com.cynergisuite.middleware.shipping.freight.calc.method.FreightCalcMethodTypeDTO
import com.cynergisuite.middleware.shipping.freight.calc.method.infrastructure.FreightCalcMethodTypeRepository
import com.cynergisuite.middleware.shipping.freight.onboard.FreightOnboardTypeDTO
import com.cynergisuite.middleware.shipping.freight.onboard.infrastructure.FreightOnboardTypeRepository
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.shipping.shipvia.ShipViaValueObject
import com.cynergisuite.middleware.vendor.group.VendorGroupEntity
import com.cynergisuite.middleware.vendor.group.infrastructure.VendorGroupRepository
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleEntity
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import javax.inject.Inject


import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class VendorControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/vendor"

   @Inject AddressRepository addressRepository
   @Inject FreightOnboardTypeRepository freightOnboardTypeRepository
   @Inject FreightCalcMethodTypeRepository freightCalcMethodTypeRepository
   @Inject ShipViaTestDataLoaderService shipViaFactoryService
   @Inject VendorRepository vendorRepository
   @Inject VendorGroupRepository vendorGroupRepository
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
         new AddressValueObject(address) == new AddressValueObject(vendor.address)
         ourAccountNumber == vendor.ourAccountNumber
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
         new AddressValueObject(address) == new AddressValueObject(vendor.address)
         ourAccountNumber == vendor.ourAccountNumber
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
      response.size() == 18
      response.collect { new ErrorDataTransferObject(it.message, it.path) }.sort { o1, o2 -> o1 <=> o2 } == [
         new ErrorDataTransferObject("Cannot be blank", "name"),
         new ErrorDataTransferObject("Is required", "address"),
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
         new ErrorDataTransferObject("Is required", "ourAccountNumber"),
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
      response.collect { new ErrorDataTransferObject(it.message, it.path) }.toSet() == [
         new ErrorDataTransferObject("${String.format('%d', shipVia.id)} was unable to be found", "shipVia.id"),
         new ErrorDataTransferObject("${String.format('%d', vendorPaymentTerm.id)} was unable to be found", "paymentTerm.id"),
      ].toSet()
   }

   void "update one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final newVendorAddress = AddressTestDataLoader.single().with { new AddressValueObject(it) }
      final vendorUpdate = VendorTestDataLoader.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }

      when: "All properties are updated including address"
      vendorUpdate.id = vendor.id
      vendorUpdate.address = newVendorAddress
      def result = put("$path/${vendor.id}", vendorUpdate)
      vendorUpdate.address.id = result.address?.id

      then: "Everything should be different except the vendor.id"
      notThrown(Exception)
      result != null
      result.id != null
      result.id == vendor.id
      result.address.id != null
      result.address.id > 0
      result.address.id != vendor.address.id
      new VendorDTO(result) == vendorUpdate
   }

   void "update only address" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia).with { new VendorDTO(it) }
      final newVendorAddress = AddressTestDataLoader.single().with { new AddressValueObject(it) }

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

   void "search vendors" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')

      final addressVO = new AddressValueObject(1, "Test Address", "123 Test St", "Suite 1100", "Corpus Christi", "TX", "78418", 11.01, 42.07, "USA", "Nueces", "361777777", "3612222222")
      final addressEntity = new AddressEntity(addressVO)

      final schedules = [new VendorPaymentTermScheduleEntity(null, null, 90, 1.0, 1)]
      final VPT = new VendorPaymentTermEntity(null, company, "test1", null, null, null, schedules)
      final vendorPaymentTerm = vendorPaymentTermRepository.insert(VPT)

      final shipVia = shipViaFactoryService.single(nineNineEightEmployee.company)

      final groupEntity = new VendorGroupEntity(null, company, "Test Group", "Group used for testing!")
      final vendorGroup = vendorGroupRepository.insert(groupEntity)

      final onboard = freightOnboardTypeRepository.findOne(1)
      final method = freightCalcMethodTypeRepository.findOne(1)

      final vendorEntity1 = new VendorEntity(null, company, "Vendor 1", addressEntity, 1234, null, onboard, vendorPaymentTerm, 0, 0, false, shipVia, vendorGroup, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com", null, false, false)
      vendorRepository.insert(vendorEntity1).with { new VendorDTO(it) }
      final vendorEntity2 = new VendorEntity(null, company, "Vendor 2", addressEntity, 1234, null, onboard, vendorPaymentTerm, 0, 0, false, shipVia, vendorGroup, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com", null, false, false)
      vendorRepository.insert(vendorEntity2).with { new VendorDTO(it) }
      final vendorEntity3 = new VendorEntity(null, company, "Vendor 3", addressEntity, 1234, null, onboard, vendorPaymentTerm, 0, 0, false, shipVia, vendorGroup, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com", null, false, false)
      vendorRepository.insert(vendorEntity3).with { new VendorDTO(it) }
      final vendorEntity4 = new VendorEntity(null, company, "Out of search result 1", addressEntity, 1234, null, onboard, vendorPaymentTerm, 0, 0, false, shipVia, vendorGroup, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com", null, false, false)
      vendorRepository.insert(vendorEntity4).with { new VendorDTO(it) }
      final vendorEntity5 = new VendorEntity(null, company, "Out of search result 2", addressEntity, 1234, null, onboard, vendorPaymentTerm, 0, 0, false, shipVia, vendorGroup, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com", null, false, false)
      vendorRepository.insert(vendorEntity5).with { new VendorDTO(it) }
      def searchOne = new SearchPageRequest([query:"Vandor%202"])
      def searchTwoPageOne = new SearchPageRequest([page:1, size:2, query:"Vandor%202"])
      def searchTwoPageTwo = new SearchPageRequest([page:2, size:2, query:"Vandor%202"])
      def searchTwoPageThree = new SearchPageRequest([page:3, size:2, query:"Vandor%202"])
      def searchSqlInjection = new SearchPageRequest([query:"%20or%201=1;drop%20table%20account;--"])

      when:
      def searchOneResult = get("$path/search${searchOne}")

      then:
      searchOneResult.totalElements == 3
      searchOneResult.totalPages == 1
      searchOneResult.first == true
      searchOneResult.last == true
      searchOneResult.elements.size() == 3
      searchOneResult.elements[0].name == 'Vendor 2'
      searchOneResult.elements[1].name == 'Vendor 3'
      searchOneResult.elements[2].name == 'Vendor 1'

      when:
      def searchTwoPageOneResult = get("$path/search${searchTwoPageOne}")

      then:
      searchTwoPageOneResult.requested.with { new SearchPageRequest(it) } == searchTwoPageOne
      searchTwoPageOneResult.totalElements == 3
      searchTwoPageOneResult.totalPages == 2
      searchTwoPageOneResult.first == true
      searchTwoPageOneResult.last == false
      searchTwoPageOneResult.elements.size() == 2
      searchTwoPageOneResult.elements[0].name == 'Vendor 2'
      searchTwoPageOneResult.elements[1].name == 'Vendor 3'

      when:
      def searchTwoPageTwoResult = get("$path/search${searchTwoPageTwo}")

      then:
      searchTwoPageTwoResult.requested.with { new SearchPageRequest(it) } == searchTwoPageTwo
      searchTwoPageTwoResult.totalElements == 3
      searchTwoPageTwoResult.totalPages == 2
      searchTwoPageTwoResult.first == false
      searchTwoPageTwoResult.last == true
      searchTwoPageTwoResult.elements.size() == 1
      searchTwoPageTwoResult.elements[0].name == 'Vendor 1'

      when:
      get("$path/search${searchTwoPageThree}")

      then:
      def notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT

      when:
      def searchSqlInjectionResult = get("$path/search${searchSqlInjection}")

      then:
      searchSqlInjectionResult.requested.query == ' or 1=1'
      searchSqlInjectionResult.totalElements < 5
   }

   void "Try creating vendor with a negative ourAccountNumber" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')

      final addressVO = new AddressValueObject(1, "Test Address", "123 Test St", "Suite 1100", "Corpus Christi", "TX", "78418", 11.01, 42.07, "USA", "Nueces", "361777777", "3612222222")
      final addressEntity = new AddressEntity(addressVO)

      final schedules = [new VendorPaymentTermScheduleEntity(null, null, 90, 1.0, 1)]
      final VPT = new VendorPaymentTermEntity(null, company, "test1", null, null, null, schedules)
      final vendorPaymentTerm = vendorPaymentTermRepository.insert(VPT)

      final shipVia = shipViaFactoryService.single(nineNineEightEmployee.company)

      final VGRP = new VendorGroupEntity(null, company, "Test Group", "Group used for testing!")
      final vendorGroup = vendorGroupRepository.insert(VGRP)

      final onboard = freightOnboardTypeRepository.findOne(1)
      final method = freightCalcMethodTypeRepository.findOne(1)

      final vendorEntity = new VendorEntity(null, company, "test1", addressEntity, 1234, null, onboard, vendorPaymentTerm, 0, 0, false, shipVia, vendorGroup, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com", null, false, false)
      final vendor = vendorRepository.insert(vendorEntity).with { new VendorDTO(it) }

      when:
      vendor.ourAccountNumber = -1
      post(path, vendor)

      then:
      final ex = thrown(HttpClientResponseException)
      ex.status == BAD_REQUEST
      final response = ex.response.bodyAsJson()
      response.size() == 1
      response.collect { new ErrorDataTransferObject(it.message, it.path) } == [
         new ErrorDataTransferObject("ourAccountNumber must be greater than zero", "ourAccountNumber")
      ]
   }

   void "Create vendor with bumpPercent that has 2 integral and 8 fractional" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')

      final addressVO = new AddressValueObject(1, "Test Address", "123 Test St", "Suite 1100", "Corpus Christi", "TX", "78418", 11.01, 42.07, "USA", "Nueces", "361777777", "3612222222")
      final addressEntity = new AddressEntity(addressVO)

      final schedules = [new VendorPaymentTermScheduleEntity(null, null, 90, 1.0, 1)]
      final VPT = new VendorPaymentTermEntity(null, company, "test1", null, null, null, schedules)
      final vendorPaymentTerm = vendorPaymentTermRepository.insert(VPT)

      final shipVia = shipViaFactoryService.single(nineNineEightEmployee.company)

      final VGRP = new VendorGroupEntity(null, company, "Test Group", "Group used for testing!")
      final vendorGroup = vendorGroupRepository.insert(VGRP)

      final onboard = freightOnboardTypeRepository.findOne(1)
      final method = freightCalcMethodTypeRepository.findOne(1)

      final vendorEntity = new VendorEntity(null, company, "test1", addressEntity, 1234, null, onboard, vendorPaymentTerm, 0, 0, false, shipVia, vendorGroup, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com", null, false, false)
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
