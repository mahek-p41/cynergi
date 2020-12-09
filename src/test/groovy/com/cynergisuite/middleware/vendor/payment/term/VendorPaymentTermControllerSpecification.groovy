package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleDTO
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleEntity
import com.cynergisuite.middleware.vendor.payment.term.schedule.infrastructure.VendorPaymentTermScheduleRepository
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST

@MicronautTest(transactional = false)
class VendorPaymentTermControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/vendor/payment/term"

   @Inject VendorPaymentTermTestDataLoaderService vendorPaymentTermTestDataLoaderService
   @Inject VendorPaymentTermRepository vendorPaymentTermRepository

   void "fetch one vendor payment term by id"() {
      given:
      final def vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(nineNineEightAuthenticatedEmployee.myCompany())

      when:
      def result = get("$path/${vendorPaymentTerm.id}")

      then:
      notThrown(HttpClientResponseException)
      result.id == vendorPaymentTerm.id
      result.description == vendorPaymentTerm.description
      result.discountMonth == vendorPaymentTerm.discountMonth
      result.discountDays == vendorPaymentTerm.discountDays
      result.discountPercent == vendorPaymentTerm.discountPercent
   }

    void "Term with 1 payments and one schedule record direct insert" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final schedules = [new VendorPaymentTermScheduleEntity(null, null, 90, 1.0, 1)]
      final VPT = new VendorPaymentTermEntity(null, company, "test1", null, null, null, schedules)
      final vendorPaymentTerm = vendorPaymentTermRepository.insert(VPT)

      when:
      def result = get("$path/${vendorPaymentTerm.id}")

      then:
      notThrown(HttpClientResponseException)
      result.id == vendorPaymentTerm.id
      result.description == vendorPaymentTerm.description
      result.discountMonth == vendorPaymentTerm.discountMonth
      result.discountDays == vendorPaymentTerm.discountDays
      result.discountPercent == vendorPaymentTerm.discountPercent
   }

   void "Term with 1 payments and 1 schedule record using post" () {
      given:
      final schedules = [new VendorPaymentTermScheduleDTO(null, null, 90, 1.0, 1)]
      final vendorPaymentTerm = new VendorPaymentTermDTO(null, "test2", null, null, null, schedules)

      when:
      def result = post(path, vendorPaymentTerm)

      then:
      notThrown(HttpClientResponseException)
      result.description == vendorPaymentTerm.description
      result.discountMonth == vendorPaymentTerm.discountMonth
      result.discountDays == vendorPaymentTerm.discountDays
      result.discountPercent == vendorPaymentTerm.discountPercent
   }

   void "Term with 2 payments and 2 schedule records using post" () {
      given:
      final schedules = [new VendorPaymentTermScheduleDTO(null, null, 30, 0.50, 1), new VendorPaymentTermScheduleDTO(null, null, 60, 0.50, 2)]
      final vendorPaymentTerm = new VendorPaymentTermDTO(null, "test3", null, null, null, schedules)

      when:
      def result = post(path, vendorPaymentTerm)

      then:
      notThrown(HttpClientResponseException)
      result.description == vendorPaymentTerm.description
      result.discountMonth == vendorPaymentTerm.discountMonth
      result.discountDays == vendorPaymentTerm.discountDays
      result.discountPercent == vendorPaymentTerm.discountPercent
      result.scheduleRecords[0].dueDays == schedules[0].dueDays
   }

   void "insert vendor payment term without a schedule" () {
      given:
      final existingVPT = new VendorPaymentTermDTO([description: "test4"])

      when:
      post(path, existingVPT)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response.message[0] == "Size of provided value [] is invalid"
      response.path[0] == "scheduleRecords"
   }

   void "update vendor payment term schedule record" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final schedules = [new VendorPaymentTermScheduleEntity(null, null, 30, 0.75, 1), new VendorPaymentTermScheduleEntity(null, null, 60, 0.25, 2)]
      final paymentTerm = new VendorPaymentTermEntity(null, company, "test5", null, null, null, schedules)
      final existing = vendorPaymentTermRepository.insert(paymentTerm).with { new VendorPaymentTermDTO(it) }

      when:
      existing.scheduleRecords[0].dueDays = 45
      def updated = put("$path/${existing.id}", existing)

      then:
      notThrown(Exception)
      updated.id == existing.id
      updated.description == existing.description
      updated.discountMonth == existing.discountMonth
      updated.discountDays == existing.discountDays
      updated.discountPercent == existing.discountPercent
   }

   void "update schedule record percentages to sum to less than 100" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final schedules = [new VendorPaymentTermScheduleEntity(null, null, 30, 0.75, 1), new VendorPaymentTermScheduleEntity(null, null, 60, 0.25, 2)]
      final paymentTerm = new VendorPaymentTermEntity(null, company, "test6", null, null, null, schedules)
      final existing = vendorPaymentTermRepository.insert(paymentTerm).with { new VendorPaymentTermDTO(it) }

      when:
      existing.scheduleRecords[0].duePercent = 0.70
      existing.scheduleRecords[1].duePercent = 0.20
      put("$path/${existing.id}", existing)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
   }

   void "Null discount percent" () {
      given:
      final schedules = [new VendorPaymentTermScheduleDTO(null, null, 30, 1.0, 1)]
      final newVPT = new VendorPaymentTermDTO([description: "test8", discountMonth: 3, scheduleRecords: schedules])

      when:
      post(path, newVPT)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].message == "Is required"
      response[0].path == "discountPercent"
   }

   void "Null discount month and discount days" () {
      given:
      final schedules = [new VendorPaymentTermScheduleDTO(null, null, 30, 1.0, 1)]
      final newVPT = new VendorPaymentTermDTO([description: "test8", discountMonth: null, discountDays: null, discountPercent: 1, scheduleRecords: schedules])

      when:
      post(path, newVPT)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].message == "Cannot be updated to 1"
      response[0].path == "discountPercent"
   }

   void "Discount percent is 0" () {
      given:
      final schedules = [new VendorPaymentTermScheduleDTO(null, null, 30, 1.0, 1)]
      final newVPT = new VendorPaymentTermDTO([description: "test8", discountMonth: 3, discountPercent: 0, scheduleRecords: schedules])

      when:
      post(path, newVPT)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].message == "discountPercent must be greater than zero"
      response[0].path == "discountPercent"
   }

   void "Discount percent greater than 1" () {
      given:
      final schedules = [new VendorPaymentTermScheduleDTO(null, null, 30, 1.0, 1)]
      final newVPT = new VendorPaymentTermDTO([description: "test8", discountMonth: 3, discountPercent: 1.5, scheduleRecords: schedules])

      when:
      post(path, newVPT)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].message == "Must be in range of (0, 1]"
      response[0].path == "discountPercent"
   }

   void "delete the first of two schedule records" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final schedules = [new VendorPaymentTermScheduleEntity(null, null, 30, 0.75, 1), new VendorPaymentTermScheduleEntity(null, null, 60, 0.25, 2)]
      final paymentTerm = new VendorPaymentTermEntity(null, company, "test6", null, null, null, schedules)
      final existing = vendorPaymentTermRepository.insert(paymentTerm).with { new VendorPaymentTermDTO(it) }

      when:
      existing.scheduleRecords.remove(0)
      existing.scheduleRecords[0].duePercent = 1
      existing.scheduleRecords[0].scheduleOrderNumber = 1
      def updated = put("$path/${existing.id}", existing)

      then:
      notThrown(Exception)
      updated.id == existing.id
      updated.description == existing.description
      updated.discountMonth == existing.discountMonth
      updated.discountDays == existing.discountDays
      updated.discountPercent == existing.discountPercent
   }

   void "delete all schedule records tied to a vendor payment term" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final schedules = [new VendorPaymentTermScheduleEntity(null, null, 30, 0.75, 1), new VendorPaymentTermScheduleEntity(null, null, 60, 0.25, 2)]
      final paymentTerm = new VendorPaymentTermEntity(null, company, "test7", null, null, null, schedules)
      final existing = vendorPaymentTermRepository.insert(paymentTerm).with { new VendorPaymentTermDTO(it) }

      when:
      existing.scheduleRecords.remove(1)
      existing.scheduleRecords.remove(0)
      put("$path/${existing.id}", existing)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response.message[0] == "Size of provided value [] is invalid"
      response.path[0] == "scheduleRecords"
   }

   void "fetch all vendor payment term records when more than vendor payment term exists" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final schedules1 = [new VendorPaymentTermScheduleEntity(null, null, 30, 0.50, 1), new VendorPaymentTermScheduleEntity(null, null, 60, 0.50, 2)]
      final paymentTerm1 = new VendorPaymentTermEntity(null, company, "test8a", null, null, null, schedules1)
      final result1 = vendorPaymentTermRepository.insert(paymentTerm1)
      final schedules2 = [new VendorPaymentTermScheduleEntity(null, null, 60, 0.75, 1), new VendorPaymentTermScheduleEntity(null, null, 90, 0.25, 2)]
      final paymentTerm2 = new VendorPaymentTermEntity(null, company, "test8b", null, null, null, schedules2)
      def result2 = vendorPaymentTermRepository.insert(paymentTerm2)
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

   void "post vendor payment term discountPercent that has 2 integral and 8 fractional" () {
      given:
      final schedules = [new VendorPaymentTermScheduleDTO(null, null, 90, 1.0, 1)]
      final vendorPaymentTerm = new VendorPaymentTermDTO(null, "test2", null, null, 20.00000008, schedules)

      when:
      post(path, vendorPaymentTerm)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].message == "20.00000008 is out of range for discountPercent"
      response[0].path == "discountPercent"
   }

   void "post vendor payment term with 0 percent properties" () {
      given:
      final schedules = [new VendorPaymentTermScheduleDTO(null, null, 30, 0.0, 1), new VendorPaymentTermScheduleDTO(null, null, 60, 0.50, 2)]
      final vendorPaymentTerm = new VendorPaymentTermDTO(null, "test3", null, null, 0.0, schedules)

      when:
      post(path, vendorPaymentTerm)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson().sort { o1, o2 -> o1.message <=> o2.message }
      response.size() == 2
      response[0].message == "discountPercent must be greater than zero"
      response[0].path == "discountPercent"
      response[1].message == "scheduleRecords[0].duePercent must be greater than zero"
      response[1].path == "scheduleRecords[0].duePercent"
   }

   void "post vendor payment term with two 49 duePercents" () {
      given:
      final schedules = [new VendorPaymentTermScheduleDTO(null, null, 30, 0.49, 1), new VendorPaymentTermScheduleDTO(null, null, 60, 0.49, 2)]
      final vendorPaymentTerm = new VendorPaymentTermDTO(null, "test3", null, null, null, schedules)

      when:
      post(path, vendorPaymentTerm)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].message == "Payment terms percent due does not equal 100, was 98.00000"
      response[0].path == "scheduleRecords.duePercent[*]"
   }
}
