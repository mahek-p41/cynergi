package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleEntity
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleValueObject
import com.cynergisuite.middleware.vendor.payment.term.schedule.infrastructure.VendorPaymentTermScheduleRepository
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import javax.inject.Inject
import static io.micronaut.http.HttpStatus.BAD_REQUEST

@MicronautTest(transactional = false)
class VendorPaymentTermControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/vendor/payment/term"

   @Inject VendorPaymentTermDataLoaderService vendorPaymentTermDataLoaderService
   @Inject VendorPaymentTermService vendorPaymentTermService
   @Inject VendorPaymentTermRepository vendorPaymentTermRepository
   @Inject VendorPaymentTermScheduleRepository vendorPaymentTermScheduleRepository

   void "fetch one vendor payment term by id"() {
      given:
      final def vendorPaymentTerm = vendorPaymentTermDataLoaderService.single(nineNineEightAuthenticatedEmployee.myCompany())

      when:
      def result = get("$path/${vendorPaymentTerm.id}")

      then:
      notThrown(HttpClientResponseException)
      result.id == vendorPaymentTerm.id
      result.description == vendorPaymentTerm.description
      result.number == vendorPaymentTerm.id
      result.numberOfPayments == vendorPaymentTerm.numberOfPayments
      result.discountMonth == vendorPaymentTerm.discountMonth
      result.discountDays == vendorPaymentTerm.discountDays
      result.discountPercent == vendorPaymentTerm.discountPercent
   }

    void "Term with 1 payments and one schedule record direct insert" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final schedules = [new VendorPaymentTermScheduleEntity(null, null, 90, 1, 1)]
      final VPT = new VendorPaymentTermEntity(null, company, "test1", null, 1, null, null, null, schedules)
      final vendorPaymentTerm = vendorPaymentTermRepository.insert(VPT)

      when:
      def result = get("$path/${vendorPaymentTerm.id}")

      then:
      notThrown(HttpClientResponseException)
      result.id == vendorPaymentTerm.id
      result.description == vendorPaymentTerm.description
      result.number == vendorPaymentTerm.id
      result.numberOfPayments == vendorPaymentTerm.numberOfPayments
      result.discountMonth == vendorPaymentTerm.discountMonth
      result.discountDays == vendorPaymentTerm.discountDays
      result.discountPercent == vendorPaymentTerm.discountPercent
   }

   void "Term with 1 payments and 1 schedule record using post" () {
      given:
      final schedules = [new VendorPaymentTermScheduleValueObject(null, null, 90, 1, 1)]
      final vendorPaymentTerm = new VendorPaymentTermValueObject(null, "test2", null, 1, null, null, null, schedules)

      when:
      def result = post("$path", vendorPaymentTerm)

      then:
      notThrown(HttpClientResponseException)
      result.description == vendorPaymentTerm.description
      result.numberOfPayments == vendorPaymentTerm.numberOfPayments
      result.discountMonth == vendorPaymentTerm.discountMonth
      result.discountDays == vendorPaymentTerm.discountDays
      result.discountPercent == vendorPaymentTerm.discountPercent
   }

   void "Term with 2 payments and 2 schedule records using post" () {
      given:
      final schedules = [new VendorPaymentTermScheduleValueObject(null, null, 30, 0.50, 1), new VendorPaymentTermScheduleValueObject(null, null, 60, 0.50, 2)]
      final vendorPaymentTerm = new VendorPaymentTermValueObject(null, "test3", null, 2, null, null, null, schedules)

      when:
      def result = post("$path", vendorPaymentTerm)

      then:
      notThrown(HttpClientResponseException)
      result.description == vendorPaymentTerm.description
      result.numberOfPayments == vendorPaymentTerm.numberOfPayments
      result.discountMonth == vendorPaymentTerm.discountMonth
      result.discountDays == vendorPaymentTerm.discountDays
      result.discountPercent == vendorPaymentTerm.discountPercent
      result.scheduleRecords[0].dueDays == schedules[0].dueDays
   }

   void "insert vendor payment term without a schedule" () {
      given:
      final existingVPT = new VendorPaymentTermValueObject([description: "test4", numberOfPayments:  1])

      when:
      post("$path", existingVPT)

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
      final schedules = [new VendorPaymentTermScheduleValueObject(null, null, 30, 0.75, 1), new VendorPaymentTermScheduleValueObject(null, null, 60, 0.25, 2)]
      final VPT = new VendorPaymentTermValueObject(null, "test5", null, 2, null, null, null, schedules)
      def existing = post("$path", VPT)
      existing.scheduleRecords[0].dueDays = 45

      when:
      def updated = put("$path/${existing.id}", existing)

      then:
      notThrown(Exception)
      updated.id == existing.id
      updated.description == existing.description
      updated.number == updated.id
      updated.numberOfPayments == existing.numberOfPayments
      updated.discountMonth == existing.discountMonth
      updated.discountDays == existing.discountDays
      updated.discountPercent == existing.discountPercent
   }

   void "update schedule record percentages to sum to less than 100" () {
      given:
      final schedules = [new VendorPaymentTermScheduleValueObject(null, null, 30, 0.75, 1), new VendorPaymentTermScheduleValueObject(null, null, 60, 0.25, 2)]
      final VPT = new VendorPaymentTermValueObject(null, "test6", null, 2, null, null, null, schedules)
      def existing = post("$path", VPT)
      existing.scheduleRecords[0].duePercent = 0.70
      existing.scheduleRecords[1].duePercent = 0.20

      when:
      def updated = put("$path/${existing.id}", existing)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
   }

   void "numberOfPayments does not equal the number of scheduleRecords" () {
      given:
      final schedules = [new VendorPaymentTermScheduleValueObject(null, null, 30, 0.75, 1), new VendorPaymentTermScheduleValueObject(null, null, 60, 0.15, 2), new VendorPaymentTermScheduleValueObject(null, null, 90, 0.10, 3)]
      final VPT = new VendorPaymentTermValueObject(null, "test7", null, 2, null, null, null, schedules)

      when:
      def existing = post("$path", VPT)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response.message[0] == "Is required"
      response.path[0] == "numberOfPayments"
   }

   void "Null discount percent" () {
      given:
      final schedules = [new VendorPaymentTermScheduleValueObject(null, null, 30, 1, 1)]
      final newVPT = new VendorPaymentTermValueObject([description: "test8", numberOfPayments:  1, discountMonth: 3, scheduleRecords: schedules])

      when:
      post("$path/", newVPT)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
   }

   void "delete the first of two schedule records" () {
      given:
      final schedules = [new VendorPaymentTermScheduleValueObject(null, null, 30, 0.75, 1), new VendorPaymentTermScheduleValueObject(null, null, 60, 0.25, 2)]
      final VPT = new VendorPaymentTermValueObject(null, "test6", null, 2, null, null, null, schedules)
      def existing = post("$path", VPT)
      existing.scheduleRecords.remove(0)
      existing.numberOfPayments = 1
      existing.scheduleRecords[0].duePercent = 1
      existing.scheduleRecords[0].scheduleOrderNumber = 1

      when:
      def updated = put("$path/${existing.id}", existing)

      then:
      notThrown(Exception)
      updated.id == existing.id
      updated.description == existing.description
      updated.number == updated.id
      updated.numberOfPayments == existing.numberOfPayments
      updated.discountMonth == existing.discountMonth
      updated.discountDays == existing.discountDays
      updated.discountPercent == existing.discountPercent
   }

   void "delete all schedule records tied to a vendor payment term" () {
      given:
      final schedules = [new VendorPaymentTermScheduleValueObject(null, null, 30, 0.75, 1), new VendorPaymentTermScheduleValueObject(null, null, 60, 0.25, 2)]
      final VPT = new VendorPaymentTermValueObject(null, "test7", null, 2, null, null, null, schedules)
      def existing = post("$path", VPT)
      existing.scheduleRecords.remove(1)
      existing.scheduleRecords.remove(0)
      existing.numberOfPayments = 0

      when:
      def updated = put("$path/${existing.id}", existing)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 3
      response.message[0] == "Size of provided value [] is invalid" || "numberOfPayments must be greater than zero" || "0 is below the allowed minimum"
      response.path[0] == "scheduleRecords" || "numberOfPayments"
      response.message[1] == "Size of provided value [] is invalid" || "numberOfPayments must be greater than zero" || "0 is below the allowed minimum"
      response.path[1] == "scheduleRecords" || "numberOfPayments"
      response.message[2] == "Size of provided value [] is invalid" || "numberOfPayments must be greater than zero" || "0 is below the allowed minimum"
      response.path[2] == "scheduleRecords" || "numberOfPayments"
   }

   void "fetch all vendor payment term records when more than vendor payment term exists" () {
      given:
      final schedules1 = [new VendorPaymentTermScheduleValueObject(null, null, 30, 0.50, 1), new VendorPaymentTermScheduleValueObject(null, null, 60, 0.50, 2)]
      final VPT1 = new VendorPaymentTermValueObject(null, "test8a", null, 2, null, null, null, schedules1)
      def result1 = post("$path", VPT1)
      final schedules2 = [new VendorPaymentTermScheduleValueObject(null, null, 60, 0.75, 1), new VendorPaymentTermScheduleValueObject(null, null, 90, 0.25, 2)]
      final VPT2 = new VendorPaymentTermValueObject(null, "test8b", null, 2, null, null, null, schedules2)
      def result2 = post("$path", VPT2)
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

   void "post vendor payment term with 51 numberOfPayments" () {
      given:
      final schedules = [new VendorPaymentTermScheduleValueObject(null, null, 90, 1, 1)]
      final vendorPaymentTerm = new VendorPaymentTermValueObject(null, "test2", null, 51, null, null, null, schedules)

      when:
      post("$path", vendorPaymentTerm)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].message == "51 exceeds the allowed maximum"
      response[0].path == "numberOfPayments"
   }

   void "post vendor payment term discountPercent that has 2 integral and 8 fractional" () {
      given:
      final schedules = [new VendorPaymentTermScheduleValueObject(null, null, 90, 1, 1)]
      final vendorPaymentTerm = new VendorPaymentTermValueObject(null, "test2", null, 1, null, null, 20.00000008, schedules)

      when:
      post("$path", vendorPaymentTerm)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].message == "20.00000008 is out of range for discountPercent"
      response[0].path == "discountPercent"
   }

}
