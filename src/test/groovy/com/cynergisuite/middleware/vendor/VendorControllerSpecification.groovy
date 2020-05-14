package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.address.AddressRepository
import com.cynergisuite.middleware.address.AddressValueObject
import com.cynergisuite.middleware.shipvia.ShipViaFactoryService
import com.cynergisuite.middleware.vendor.freight.infrastructure.FreightMethodTypeRepository
import com.cynergisuite.middleware.vendor.freight.infrastructure.FreightOnboardTypeRepository
import com.cynergisuite.middleware.vendor.group.VendorGroupEntity
import com.cynergisuite.middleware.vendor.group.infrastructure.VendorGroupRepository
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermService
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleEntity
import com.cynergisuite.middleware.vendor.payment.term.schedule.infrastructure.VendorPaymentTermScheduleRepository
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import org.codehaus.groovy.runtime.typehandling.GroovyCastException

import javax.inject.Inject
import java.time.LocalDate

import static io.micronaut.http.HttpStatus.BAD_REQUEST

@MicronautTest(transactional = false)
class VendorControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/vendor"

   @Inject AddressRepository addressRepository
   @Inject FreightOnboardTypeRepository freightOnboardTypeRepository
   @Inject FreightMethodTypeRepository freightMethodTypeRepository
   @Inject ShipViaFactoryService shipViaFactoryService
   @Inject VendorService vendorService
   @Inject VendorRepository vendorRepository
   @Inject VendorGroupRepository vendorGroupRepository
   @Inject VendorPaymentTermScheduleRepository vendorPaymentTermScheduleRepository
   @Inject VendorPaymentTermDataLoaderService vendorPaymentTermDataLoaderService
   @Inject VendorPaymentTermService vendorPaymentTermService
   @Inject VendorPaymentTermRepository vendorPaymentTermRepository

   void "Create and fetch one vendor" () {
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
      final method = freightMethodTypeRepository.findOne(1)

      final dateOne = LocalDate.now()
      final dateTwo = LocalDate.now()
      final vendorEntity = new VendorEntity(null, company, 1, "test1", addressEntity, 1234, null, onboard, vendorPaymentTerm, 0, 0, false, shipVia, vendorGroup, dateOne, dateTwo, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com")
      final vendor = vendorRepository.insert(vendorEntity).with { new VendorValueObject(it) }

      when:
      def result = get("$path/${vendor.id}")

      then:
      notThrown(HttpClientResponseException)
      result.id == vendor.id
      result.vendorNumber == vendor.vendorNumber
      result.ourAccountNumber == vendor.ourAccountNumber
      result.federalIdNumber == vendor.federalIdNumber
   }

   void "Create and update one vendor" () {
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
      final method = freightMethodTypeRepository.findOne(1)

      final dateOne = LocalDate.now()
      final dateTwo = LocalDate.now()
      final vendorEntity = new VendorEntity(null, company, 2, "test1", addressEntity, 1234, null, onboard, vendorPaymentTerm, 0, 0, false, shipVia, vendorGroup, dateOne, dateTwo, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com")
      final vendor = vendorRepository.insert(vendorEntity).with { new VendorValueObject(it) }

      when:
      vendor.nameKey = "test2"
      def updated = put("$path/${vendor.id}", vendor)

      then:
      notThrown(HttpClientResponseException)
      updated.id == vendor.id
      updated.nameKey == vendor.nameKey
      updated.vendorNumber == vendor.vendorNumber
      updated.ourAccountNumber == vendor.ourAccountNumber
      updated.federalIdNumber == vendor.federalIdNumber
   }

   void "fetch all vendors" () {
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
      final method = freightMethodTypeRepository.findOne(1)

      final dateOne = LocalDate.now()
      final dateTwo = LocalDate.now()
      final vendorEntity = new VendorEntity(null, company, 3, "test1", addressEntity, 1234, null, onboard, vendorPaymentTerm, 0, 0, false, shipVia, vendorGroup, dateOne, dateTwo, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com")
      final vendor = vendorRepository.insert(vendorEntity).with { new VendorValueObject(it) }
      final vendorEntity2 = new VendorEntity(null, company, 4, "test1", addressEntity, 1234, null, onboard, vendorPaymentTerm, 0, 0, false, shipVia, vendorGroup, dateOne, dateTwo, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com")
      final vendor2 = vendorRepository.insert(vendorEntity2).with { new VendorValueObject(it) }
      def pageOne = new StandardPageRequest(1, 5, "id", "ASC")

      when:
      def pageOneResult = get("$path${pageOne}")

      then:
      pageOneResult.requested.with { new StandardPageRequest(it) } == pageOne
      pageOneResult.totalElements == 2
      pageOneResult.totalPages == 1
      pageOneResult.first == true
      pageOneResult.last == true
      pageOneResult.elements.size() == 2
   }

   void "Create vendor then modify resulting VO to insert with a post" () {
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
      final method = freightMethodTypeRepository.findOne(1)

      final dateOne = LocalDate.now()
      final dateTwo = LocalDate.now()
      final vendorEntity = new VendorEntity(null, company, 5, "test1", addressEntity, 1234, null, onboard, vendorPaymentTerm, 0, 0, false, shipVia, vendorGroup, dateOne, dateTwo, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com")
      final vendor = vendorRepository.insert(vendorEntity).with { new VendorValueObject(it) }

      when:
      vendor.vendorNumber = 6
      vendor.nameKey = "test2"
      def result = post(path, vendor)

      then:
      notThrown(HttpClientResponseException)
      result.id != vendor.id
      result.vendorNumber == vendor.vendorNumber
      result.ourAccountNumber == vendor.ourAccountNumber
      result.federalIdNumber == vendor.federalIdNumber
   }

   void "Try creating vendor with null vendor number" () {
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
      final method = freightMethodTypeRepository.findOne(1)

      final dateOne = LocalDate.now()
      final dateTwo = LocalDate.now()
      final vendorEntity = new VendorEntity(null, company, 6, "test1", addressEntity, 1234, null, onboard, vendorPaymentTerm, 0, 0, false, shipVia, vendorGroup, dateOne, dateTwo, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com")
      final vendor = vendorRepository.insert(vendorEntity).with { new VendorValueObject(it) }

      when:
      vendor.vendorNumber = null
      post(path, vendor)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response.message[0] == "Is required"
      response.path[0] == "vendorNumber"
   }

   void "Try creating vendor with a string in ourAccountNumber" () {
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
      final method = freightMethodTypeRepository.findOne(1)

      final dateOne = LocalDate.now()
      final dateTwo = LocalDate.now()
      final vendorEntity = new VendorEntity(null, company, 7, "test1", addressEntity, 1234, null, onboard, vendorPaymentTerm, 0, 0, false, shipVia, vendorGroup, dateOne, dateTwo, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com")
      final vendor = vendorRepository.insert(vendorEntity).with { new VendorValueObject(it) }

      when:
      vendor.vendorNumber = 6
      vendor.ourAccountNumber = "NaN"
      post(path, vendor)

      then:
      final exception = thrown(GroovyCastException)
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
      final method = freightMethodTypeRepository.findOne(1)

      final dateOne = LocalDate.now()
      final dateTwo = LocalDate.now()
      final vendorEntity = new VendorEntity(null, company, 8, "test1", addressEntity, 1234, null, onboard, vendorPaymentTerm, 0, 0, false, shipVia, vendorGroup, dateOne, dateTwo, 5, 2500.00, 5, 5000.00, false, "ABC123DEF456", "John Doe", null, false, null, method, null, null, false, false, false, false, false, "patricks@hightouchinc.com")
      final vendor = vendorRepository.insert(vendorEntity).with { new VendorValueObject(it) }

      when:
      vendor.vendorNumber = 9
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
