package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class VendorPaymentTermControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/vendor/payment/term"

   @Inject VendorPaymentTermDataLoaderService vendorPaymentTermDataLoaderService
   @Inject VendorPaymentTermService vendorPaymentTermService
   @Inject VendorPaymentTermRepository vendorPaymentTermRepository

   void "fetch one vendor payment term by id"() {
      given:
      final def vendorPaymentTerm = vendorPaymentTermDataLoaderService.single(nineNineEightAuthenticatedEmployee.myCompany())

      when:
      def result = get("$path/${vendorPaymentTerm.id}")

      then:
      notThrown(HttpClientResponseException)
   }

   void "Term with 2 payments with dueDays1 and dueMonth1 being null" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final newVPT = new VendorPaymentTermValueObject([description: "test1", numberOfPayments:  2, dueMonth2:  6, duePercent1: 50, duePercent2: 50])

      when:
      post("$path/", newVPT)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
   }

   void "Term with 3 payments with dueDays2 and dueMonth2 being null" () {
      given:
      final newVPT = new VendorPaymentTermValueObject([description: "test2", numberOfPayments:  3, dueMonth1:  3, dueMonth3:  9, duePercent1: 20, duePercent2: 20, duePercent3: 60])

      when:
      post("$path/", newVPT)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      //can add in other response. items to check
   }

   void "Term with 3 payments with dueMonth1-3 populated" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final newVPT = new VendorPaymentTermValueObject([description: "test3", numberOfPayments:  3, dueMonth1:  3, dueMonth2:  6, dueMonth3:  9, duePercent1: 40, duePercent2: 30, duePercent3: 30])
      final completeEntry = vendorPaymentTermService.create(newVPT,company)

      when:
      def result = get("$path/${completeEntry.id}")

      then:
      notThrown(Exception)
      result.id == completeEntry.id
   }

   void "Term with 3 payments where the percentages do not sum to 100" () {
      given:
      final newVPT = new VendorPaymentTermValueObject([description: "test4", numberOfPayments:  3, dueMonth1:  3, dueMonth2:  6, dueMonth3:  9, duePercent1: 40, duePercent2: 30, duePercent3: 25])

      when:
      post("$path/", newVPT)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
   }

   void "Null discount percent" () {
      given:
      final newVPT = new VendorPaymentTermValueObject([description: "test5", numberOfPayments:  1, dueMonth1:  3, duePercent1: 100, discountMonth: 3])

      when:
      post("$path/", newVPT)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
   }

   void "fetch one vendor payment term by id not found"() {
      when:
      get("$path/0")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "0 was unable to be found"
   }

   void "fetch all"() {
      given:
      def twentyVendorPaymentTerm = vendorPaymentTermDataLoaderService.stream(20, nineNineEightAuthenticatedEmployee.myCompany()).map {
         new VendorPaymentTermValueObject(it)
      }.sorted { o1, o2 -> o1.id <=> o2.id }.toList()
      def pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      def pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      def pageLast = new StandardPageRequest(4, 5, "id", "ASC")
      def pageFive = new StandardPageRequest(5, 5, "id", "ASC")
      def firstPageVendorPaymentTerm = twentyVendorPaymentTerm[0..4]
      def secondPageVendorPaymentTerm = twentyVendorPaymentTerm[5..9]
      def lastPageVendorPaymentTerm = twentyVendorPaymentTerm[15..19]

      when:
      def pageOneResult = get("$path${pageOne}")

      then:
      pageOneResult.requested.with { new StandardPageRequest(it) } == pageOne
      pageOneResult.totalElements == 20
      pageOneResult.totalPages == 4
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements.size() == 5
      pageOneResult.elements.collect { new VendorPaymentTermValueObject(it) } == firstPageVendorPaymentTerm

      when:
      def pageTwoResult = get("$path${pageTwo}")

      then:
      pageTwoResult.requested.with { new StandardPageRequest(it) } == pageTwo
      pageTwoResult.totalElements == 20
      pageTwoResult.totalPages == 4
      pageTwoResult.first == false
      pageTwoResult.last == false
      pageTwoResult.elements.size() == 5
      pageTwoResult.elements.collect { new VendorPaymentTermValueObject(it) } == secondPageVendorPaymentTerm

      when:
      def pageLastResult = get("$path${pageLast}")

      then:
      pageLastResult.requested.with { new StandardPageRequest(it) } == pageLast
      pageLastResult.totalElements == 20
      pageLastResult.totalPages == 4
      pageLastResult.first == false
      pageLastResult.last == true
      pageLastResult.elements.size() == 5
      pageLastResult.elements.collect { new VendorPaymentTermValueObject(it) } == lastPageVendorPaymentTerm

      when:
      get("$path/${pageFive}")

      then:
      final def notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create vendor payment term" () {
      given:
      final vendorPaymentTerm = new VendorPaymentTermValueObject([description: "test6", numberOfPayments:  1, dueMonth1:  3, duePercent1: 100])

      when:
      def created = post("$path/", vendorPaymentTerm)

      then:
      notThrown(Exception)
      created.id > 0
      created.description == vendorPaymentTerm.description
      created.number == created.id
      created.numberOfPayments == vendorPaymentTerm.numberOfPayments
      created.dueMonth1 == vendorPaymentTerm.dueMonth1
      created.dueMonth2 == vendorPaymentTerm.dueMonth2
      created.dueMonth3 == vendorPaymentTerm.dueMonth3
      created.dueMonth4 == vendorPaymentTerm.dueMonth4
      created.dueMonth5 == vendorPaymentTerm.dueMonth5
      created.dueMonth6 == vendorPaymentTerm.dueMonth6
      created.dueDays1 == vendorPaymentTerm.dueDays1
      created.dueDays2 == vendorPaymentTerm.dueDays2
      created.dueDays3 == vendorPaymentTerm.dueDays3
      created.dueDays4 == vendorPaymentTerm.dueDays4
      created.dueDays5 == vendorPaymentTerm.dueDays5
      created.dueDays6 == vendorPaymentTerm.dueDays6
      created.duePercent1 == vendorPaymentTerm.duePercent1
      created.duePercent2 == vendorPaymentTerm.duePercent2
      created.duePercent3 == vendorPaymentTerm.duePercent3
      created.duePercent4 == vendorPaymentTerm.duePercent4
      created.duePercent5 == vendorPaymentTerm.duePercent5
      created.duePercent6 == vendorPaymentTerm.duePercent6
      created.discountMonth == vendorPaymentTerm.discountMonth
      created.discountDays == vendorPaymentTerm.discountDays
      created.discountPercent == vendorPaymentTerm.discountPercent
   }

   void "create vendor payment term with 1 payment" () {
      given: 'A single vendor payment term with a single month for payment'
      final toCreate = new VendorPaymentTermValueObject([description: 'Test Description', numberOfPayments: 1, dueMonth1: 1, dueDays1: 1, duePercent1: 100])

      when:
      def created = post(path, toCreate)

      then:
      notThrown(Exception)
      created.id > 0
      created.description == 'Test Description'
      created.number == created.id
      created.numberOfPayments == 1
      created.dueMonth1 == 1
      created.dueMonth2 == null
      created.dueMonth3 == null
      created.dueMonth4 == null
      created.dueMonth5 == null
      created.dueMonth6 == null
      created.dueDays1 == 1
      created.dueDays2 == null
      created.dueDays3 == null
      created.dueDays4 == null
      created.dueDays5 == null
      created.dueDays6 == null
      created.duePercent1 == 100
      created.duePercent2 == null
      created.duePercent3 == null
      created.duePercent4 == null
      created.duePercent5 == null
      created.duePercent6 == null
      created.discountMonth == null
      created.discountDays == null
      created.discountPercent == null
   }

   void "update vendor payment term" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final existingVPT = new VendorPaymentTermValueObject([description: "test6", numberOfPayments:  1, dueMonth1:  3, duePercent1: 100])
      final existing = vendorPaymentTermService.create(existingVPT,company)
      existing.dueMonth1 = 4

      when:
      def updated = put("$path/${existing.id}", existing)

      then:
      notThrown(Exception)
      updated.id == existing.id
      updated.description == existing.description
      updated.number == updated.id
      updated.numberOfPayments == existing.numberOfPayments
      updated.dueMonth1 == existing.dueMonth1
      updated.dueMonth2 == existing.dueMonth2
      updated.dueMonth3 == existing.dueMonth3
      updated.dueMonth4 == existing.dueMonth4
      updated.dueMonth5 == existing.dueMonth5
      updated.dueMonth6 == existing.dueMonth6
      updated.dueDays1 == existing.dueDays1
      updated.dueDays2 == existing.dueDays2
      updated.dueDays3 == existing.dueDays3
      updated.dueDays4 == existing.dueDays4
      updated.dueDays5 == existing.dueDays5
      updated.dueDays6 == existing.dueDays6
      updated.duePercent1 == existing.duePercent1
      updated.duePercent2 == existing.duePercent2
      updated.duePercent3 == existing.duePercent3
      updated.duePercent4 == existing.duePercent4
      updated.duePercent5 == existing.duePercent5
      updated.duePercent6 == existing.duePercent6
      updated.discountMonth == existing.discountMonth
      updated.discountDays == existing.discountDays
      updated.discountPercent == existing.discountPercent
   }
}
