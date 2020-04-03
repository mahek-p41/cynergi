package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class VendorPaymentTermControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/vendor/payment/term"

   @Inject VendorPaymentTermDataLoaderService vendorPaymentTermDataLoaderService

   void "fetch one vendor payment term by id"() {
      given:
      final def vendorPaymentTerm = vendorPaymentTermDataLoaderService.single(nineNineEightAuthenticatedEmployee.myCompany())

      when:
      def result = get("$path/${vendorPaymentTerm.id}")

      then:
      notThrown(HttpClientResponseException)
      result.id == vendorPaymentTerm.id
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
      final company = companyFactoryService.forDatasetCode('tstds1')
      final vendorPaymentTerm = VendorPaymentTermDataLoader.single(company).with { new VendorPaymentTermValueObject(it) }

      when:
      def created = post(path, vendorPaymentTerm)

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
      final toCreate = new VendorPaymentTermValueObject([description: 'Test Description', numberOfPayments: 1, dueMonth1: 1, dueDays1: 1])

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
      created.duePercent1 == null
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
      final existing = vendorPaymentTermDataLoaderService.single(company).with { new VendorPaymentTermValueObject(it) }
      final updateTo = VendorPaymentTermDataLoader.single(company).with { new VendorPaymentTermValueObject(it) }

      when:
      def updated = put("$path/${existing.id}", updateTo)

      then:
      notThrown(Exception)
      updated.id == existing.id
      updated.description == updateTo.description
      updated.number == updated.id
      updated.numberOfPayments == updateTo.numberOfPayments
      updated.dueMonth1 == updateTo.dueMonth1
      updated.dueMonth2 == updateTo.dueMonth2
      updated.dueMonth3 == updateTo.dueMonth3
      updated.dueMonth4 == updateTo.dueMonth4
      updated.dueMonth5 == updateTo.dueMonth5
      updated.dueMonth6 == updateTo.dueMonth6
      updated.dueDays1 == updateTo.dueDays1
      updated.dueDays2 == updateTo.dueDays2
      updated.dueDays3 == updateTo.dueDays3
      updated.dueDays4 == updateTo.dueDays4
      updated.dueDays5 == updateTo.dueDays5
      updated.dueDays6 == updateTo.dueDays6
      updated.duePercent1 == updateTo.duePercent1
      updated.duePercent2 == updateTo.duePercent2
      updated.duePercent3 == updateTo.duePercent3
      updated.duePercent4 == updateTo.duePercent4
      updated.duePercent5 == updateTo.duePercent5
      updated.duePercent6 == updateTo.duePercent6
      updated.discountMonth == updateTo.discountMonth
      updated.discountDays == updateTo.discountDays
      updated.discountPercent == updateTo.discountPercent
   }
}
