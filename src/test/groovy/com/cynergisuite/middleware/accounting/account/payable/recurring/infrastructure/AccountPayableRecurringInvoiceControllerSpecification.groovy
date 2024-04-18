package com.cynergisuite.middleware.accounting.account.payable.recurring.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableRecurringInvoiceStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.recurring.AccountPayableRecurringInvoiceDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.recurring.ExpenseMonthCreationTypeDTO
import com.cynergisuite.middleware.address.AddressDTO
import com.cynergisuite.middleware.shipping.freight.calc.method.FreightCalcMethodType
import com.cynergisuite.middleware.shipping.freight.calc.method.FreightCalcMethodTypeDTO
import com.cynergisuite.middleware.shipping.freight.onboard.FreightOnboardTypeDTO
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.vendor.VendorDTO
import com.cynergisuite.middleware.vendor.VendorTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Unroll

import static io.micronaut.http.HttpStatus.BAD_REQUEST

@MicronautTest(transactional = false)
class AccountPayableRecurringInvoiceControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/accounting/account-payable/recurring"

   @Inject AccountPayableRecurringInvoiceDataLoaderService accountPayableRecurringInvoiceDataLoaderService
   @Inject ShipViaTestDataLoaderService shipViaFactoryService
   @Inject VendorPaymentTermTestDataLoaderService vendorPaymentTermTestDataLoaderService
   @Inject VendorTestDataLoaderService vendorTestDataLoaderService

   void "fetch one" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final payTo = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final accountPayableRecurringInvoiceEntity = accountPayableRecurringInvoiceDataLoaderService.single(company, vendor, payTo)

      when:
      def result = get("$path/${accountPayableRecurringInvoiceEntity.id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == accountPayableRecurringInvoiceEntity.id
         vendor.id == accountPayableRecurringInvoiceEntity.vendor.id
         invoice == accountPayableRecurringInvoiceEntity.invoice
         invoiceAmount == accountPayableRecurringInvoiceEntity.invoiceAmount
         fixedAmountIndicator == accountPayableRecurringInvoiceEntity.fixedAmountIndicator
         employeeNumberId == accountPayableRecurringInvoiceEntity.employeeNumberId
         message == accountPayableRecurringInvoiceEntity.message
         codeIndicator == accountPayableRecurringInvoiceEntity.codeIndicator
         type == accountPayableRecurringInvoiceEntity.type
         invoice == accountPayableRecurringInvoiceEntity.invoice
         payTo.id == accountPayableRecurringInvoiceEntity.payTo.id
         lastTransferToCreateInvoiceDate == accountPayableRecurringInvoiceEntity.lastTransferToCreateInvoiceDate.toString()

         with(status) {
            value == accountPayableRecurringInvoiceEntity.status.value
            description == accountPayableRecurringInvoiceEntity.status.description
         }

         dueDays == accountPayableRecurringInvoiceEntity.dueDays
         automatedIndicator == accountPayableRecurringInvoiceEntity.automatedIndicator
         separateCheckIndicator == accountPayableRecurringInvoiceEntity.separateCheckIndicator

         with(expenseMonthCreationIndicator) {
            value == accountPayableRecurringInvoiceEntity.expenseMonthCreationIndicator.value
            description == accountPayableRecurringInvoiceEntity.expenseMonthCreationIndicator.description
         }

         invoiceDay == accountPayableRecurringInvoiceEntity.invoiceDay
         expenseDay == accountPayableRecurringInvoiceEntity.expenseDay
         schedule == accountPayableRecurringInvoiceEntity.schedule
         lastCreatedInPeriod == accountPayableRecurringInvoiceEntity.lastCreatedInPeriod.toString()
         nextCreationDate == accountPayableRecurringInvoiceEntity.nextCreationDate.toString()
         nextInvoiceDate == accountPayableRecurringInvoiceEntity.nextInvoiceDate.toString()
         nextExpenseDate == accountPayableRecurringInvoiceEntity.nextExpenseDate.toString()
         startDate == accountPayableRecurringInvoiceEntity.startDate.toString()
         endDate == accountPayableRecurringInvoiceEntity.endDate.toString()
      }
   }

   void "fetch all" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendors = vendorTestDataLoaderService.stream(7, company, vendorPaymentTerm, shipVia).toList()
      final payTo = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final accountPayableRecurringInvoiceEntity = vendors.collect { vendor ->
         accountPayableRecurringInvoiceDataLoaderService.single(company, vendor, payTo)
      }

      final pageOne = new StandardPageRequest(1, 5, "N", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "N", "ASC")
      accountPayableRecurringInvoiceEntity.sort{
         o1, o2 -> o1.vendor.number <=> o2.vendor.number
      }
      final firstPageAccountPayableRecurringInvoice = accountPayableRecurringInvoiceEntity[0..4]
      final lastPageAccountPayableRecurringInvoice = accountPayableRecurringInvoiceEntity[5,6]

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
               id == firstPageAccountPayableRecurringInvoice[index].id
               vendor.id == firstPageAccountPayableRecurringInvoice[index].vendor.id
               invoice == firstPageAccountPayableRecurringInvoice[index].invoice
               invoiceAmount == firstPageAccountPayableRecurringInvoice[index].invoiceAmount
               fixedAmountIndicator == firstPageAccountPayableRecurringInvoice[index].fixedAmountIndicator
               employeeNumberId == firstPageAccountPayableRecurringInvoice[index].employeeNumberId
               message == firstPageAccountPayableRecurringInvoice[index].message
               codeIndicator == firstPageAccountPayableRecurringInvoice[index].codeIndicator
               type == firstPageAccountPayableRecurringInvoice[index].type
               invoice == firstPageAccountPayableRecurringInvoice[index].invoice
               payTo.id == firstPageAccountPayableRecurringInvoice[index].payTo.id
               lastTransferToCreateInvoiceDate == firstPageAccountPayableRecurringInvoice[index].lastTransferToCreateInvoiceDate.toString()

               with(status) {
                  value == firstPageAccountPayableRecurringInvoice[index].status.value
                  description == firstPageAccountPayableRecurringInvoice[index].status.description
               }

               dueDays == firstPageAccountPayableRecurringInvoice[index].dueDays
               automatedIndicator == firstPageAccountPayableRecurringInvoice[index].automatedIndicator
               separateCheckIndicator == firstPageAccountPayableRecurringInvoice[index].separateCheckIndicator

               with(expenseMonthCreationIndicator) {
                  value == firstPageAccountPayableRecurringInvoice[index].expenseMonthCreationIndicator.value
                  description == firstPageAccountPayableRecurringInvoice[index].expenseMonthCreationIndicator.description
               }

               invoiceDay == firstPageAccountPayableRecurringInvoice[index].invoiceDay
               expenseDay == firstPageAccountPayableRecurringInvoice[index].expenseDay
               schedule == firstPageAccountPayableRecurringInvoice[index].schedule
               lastCreatedInPeriod == firstPageAccountPayableRecurringInvoice[index].lastCreatedInPeriod.toString()
               nextCreationDate == firstPageAccountPayableRecurringInvoice[index].nextCreationDate.toString()
               nextInvoiceDate == firstPageAccountPayableRecurringInvoice[index].nextInvoiceDate.toString()
               nextExpenseDate == firstPageAccountPayableRecurringInvoice[index].nextExpenseDate.toString()
               startDate == firstPageAccountPayableRecurringInvoice[index].startDate.toString()
               endDate == firstPageAccountPayableRecurringInvoice[index].endDate.toString()
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
               id == lastPageAccountPayableRecurringInvoice[index].id
               vendor.id == lastPageAccountPayableRecurringInvoice[index].vendor.id
               invoice == lastPageAccountPayableRecurringInvoice[index].invoice
               invoiceAmount == lastPageAccountPayableRecurringInvoice[index].invoiceAmount
               fixedAmountIndicator == lastPageAccountPayableRecurringInvoice[index].fixedAmountIndicator
               employeeNumberId == lastPageAccountPayableRecurringInvoice[index].employeeNumberId
               message == lastPageAccountPayableRecurringInvoice[index].message
               codeIndicator == lastPageAccountPayableRecurringInvoice[index].codeIndicator
               type == lastPageAccountPayableRecurringInvoice[index].type
               invoice == lastPageAccountPayableRecurringInvoice[index].invoice
               payTo.id == lastPageAccountPayableRecurringInvoice[index].payTo.id
               lastTransferToCreateInvoiceDate == lastPageAccountPayableRecurringInvoice[index].lastTransferToCreateInvoiceDate.toString()

               with(status) {
                  value == lastPageAccountPayableRecurringInvoice[index].status.value
                  description == lastPageAccountPayableRecurringInvoice[index].status.description
               }

               dueDays == lastPageAccountPayableRecurringInvoice[index].dueDays
               automatedIndicator == lastPageAccountPayableRecurringInvoice[index].automatedIndicator
               separateCheckIndicator == lastPageAccountPayableRecurringInvoice[index].separateCheckIndicator

               with(expenseMonthCreationIndicator) {
                  value == lastPageAccountPayableRecurringInvoice[index].expenseMonthCreationIndicator.value
                  description == lastPageAccountPayableRecurringInvoice[index].expenseMonthCreationIndicator.description
               }

               invoiceDay == lastPageAccountPayableRecurringInvoice[index].invoiceDay
               expenseDay == lastPageAccountPayableRecurringInvoice[index].expenseDay
               schedule == lastPageAccountPayableRecurringInvoice[index].schedule
               lastCreatedInPeriod == lastPageAccountPayableRecurringInvoice[index].lastCreatedInPeriod.toString()
               nextCreationDate == lastPageAccountPayableRecurringInvoice[index].nextCreationDate.toString()
               nextInvoiceDate == lastPageAccountPayableRecurringInvoice[index].nextInvoiceDate.toString()
               nextExpenseDate == lastPageAccountPayableRecurringInvoice[index].nextExpenseDate.toString()
               startDate == lastPageAccountPayableRecurringInvoice[index].startDate.toString()
               endDate == lastPageAccountPayableRecurringInvoice[index].endDate.toString()
            }
         }
      }
   }

   void "create one" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final payTo = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final accountPayableRecurringInvoiceDTO = accountPayableRecurringInvoiceDataLoaderService.singleDTO(vendor, payTo)

      when:
      def result = post(path, accountPayableRecurringInvoiceDTO)
      accountPayableRecurringInvoiceDTO.id = result.id

      then:
      notThrown(Exception)
      result != null
      result.id != null
      with(result) {
         id == accountPayableRecurringInvoiceDTO.id
         vendor.id == accountPayableRecurringInvoiceDTO.vendor.id
         invoice == accountPayableRecurringInvoiceDTO.invoice
         invoiceAmount == accountPayableRecurringInvoiceDTO.invoiceAmount
         fixedAmountIndicator == accountPayableRecurringInvoiceDTO.fixedAmountIndicator
         employeeNumberId == accountPayableRecurringInvoiceDTO.employeeNumberId
         message == accountPayableRecurringInvoiceDTO.message
         codeIndicator == accountPayableRecurringInvoiceDTO.codeIndicator
         type == accountPayableRecurringInvoiceDTO.type
         invoice == accountPayableRecurringInvoiceDTO.invoice
         payTo.id == accountPayableRecurringInvoiceDTO.payTo.id
         lastTransferToCreateInvoiceDate == accountPayableRecurringInvoiceDTO.lastTransferToCreateInvoiceDate.toString()

         with(status) {
            value == accountPayableRecurringInvoiceDTO.status.value
            description == accountPayableRecurringInvoiceDTO.status.description
         }

         dueDays == accountPayableRecurringInvoiceDTO.dueDays
         automatedIndicator == accountPayableRecurringInvoiceDTO.automatedIndicator
         separateCheckIndicator == accountPayableRecurringInvoiceDTO.separateCheckIndicator

         with(expenseMonthCreationIndicator) {
            value == accountPayableRecurringInvoiceDTO.expenseMonthCreationIndicator.value
            description == accountPayableRecurringInvoiceDTO.expenseMonthCreationIndicator.description
         }

         invoiceDay == accountPayableRecurringInvoiceDTO.invoiceDay
         expenseDay == accountPayableRecurringInvoiceDTO.expenseDay
         schedule == accountPayableRecurringInvoiceDTO.schedule
         lastCreatedInPeriod == accountPayableRecurringInvoiceDTO.lastCreatedInPeriod.toString()
         nextCreationDate == accountPayableRecurringInvoiceDTO.nextCreationDate.toString()
         nextInvoiceDate == accountPayableRecurringInvoiceDTO.nextInvoiceDate.toString()
         nextExpenseDate == accountPayableRecurringInvoiceDTO.nextExpenseDate.toString()
         startDate == accountPayableRecurringInvoiceDTO.startDate.toString()
         endDate == accountPayableRecurringInvoiceDTO.endDate.toString()
      }
   }

   void "create valid Account Payable Recurring Invoice without nullable properties" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final payTo = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final accountPayableRecurringInvoiceDTO = accountPayableRecurringInvoiceDataLoaderService.singleDTO(vendor, payTo)
      accountPayableRecurringInvoiceDTO.message = null
      accountPayableRecurringInvoiceDTO.codeIndicator = null
      accountPayableRecurringInvoiceDTO.lastTransferToCreateInvoiceDate = null
      accountPayableRecurringInvoiceDTO.schedule = null
      accountPayableRecurringInvoiceDTO.lastCreatedInPeriod = null
      accountPayableRecurringInvoiceDTO.nextCreationDate = null
      accountPayableRecurringInvoiceDTO.nextExpenseDate = null
      accountPayableRecurringInvoiceDTO.nextInvoiceDate = null


      when:
      def result = post(path, accountPayableRecurringInvoiceDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id != null
         vendor.id == accountPayableRecurringInvoiceDTO.vendor.id
         invoice == accountPayableRecurringInvoiceDTO.invoice
         invoiceAmount == accountPayableRecurringInvoiceDTO.invoiceAmount
         fixedAmountIndicator == accountPayableRecurringInvoiceDTO.fixedAmountIndicator
         employeeNumberId == accountPayableRecurringInvoiceDTO.employeeNumberId
         message == null
         codeIndicator == null
         type == accountPayableRecurringInvoiceDTO.type
         invoice == accountPayableRecurringInvoiceDTO.invoice
         payTo.id == accountPayableRecurringInvoiceDTO.payTo.id
         lastTransferToCreateInvoiceDate == null

         with(status) {
            value == accountPayableRecurringInvoiceDTO.status.value
            description == accountPayableRecurringInvoiceDTO.status.description
         }

         dueDays == accountPayableRecurringInvoiceDTO.dueDays
         automatedIndicator == accountPayableRecurringInvoiceDTO.automatedIndicator
         separateCheckIndicator == accountPayableRecurringInvoiceDTO.separateCheckIndicator

         with(expenseMonthCreationIndicator) {
            value == accountPayableRecurringInvoiceDTO.expenseMonthCreationIndicator.value
            description == accountPayableRecurringInvoiceDTO.expenseMonthCreationIndicator.description
         }

         invoiceDay == accountPayableRecurringInvoiceDTO.invoiceDay
         expenseDay == accountPayableRecurringInvoiceDTO.expenseDay
         schedule == null
         lastCreatedInPeriod == null
         nextCreationDate == null
         nextInvoiceDate == null
         nextExpenseDate == null
         startDate == accountPayableRecurringInvoiceDTO.startDate.toString()
         endDate == accountPayableRecurringInvoiceDTO.endDate.toString()
      }
   }

   @Unroll
   void "create invalid Account Payable Recurring Invoice without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final payTo = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final accountPayableRecurringInvoiceDTO = accountPayableRecurringInvoiceDataLoaderService.singleDTO(vendor, payTo)
      accountPayableRecurringInvoiceDTO["$nonNullableProp"] = null

      when:
      post("$path/", accountPayableRecurringInvoiceDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'
      response[0].code == 'javax.validation.constraints.NotNull.message'

      where:
      nonNullableProp                  || errorResponsePath
      'vendor'                         || 'vendor'
      'invoice'                        || 'invoice'
      'invoiceAmount'                  || 'invoiceAmount'
      'fixedAmountIndicator'           || 'fixedAmountIndicator'
      'employeeNumberId'               || 'employeeNumberId'
      'type'                           || 'type'
      'payTo'                          || 'payTo'
      'status'                         || 'status'
      'dueDays'                        || 'dueDays'
      'automatedIndicator'             || 'automatedIndicator'
      'separateCheckIndicator'         || 'separateCheckIndicator'
      'expenseMonthCreationIndicator'  || 'expenseMonthCreationIndicator'
      'invoiceDay'                     || 'invoiceDay'
      'expenseDay'                     || 'expenseDay'
      'startDate'                      || 'startDate'
      'endDate'                        || 'endDate'
   }

   @Unroll
   void "create invalid Account Payable Recurring Invoice with non-existing #testProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final payTo = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
//      final vendorDTO = new VendorDTO(vendor)
//      vendorDTO.id = UUID.fromString('ee2359b6-c88c-11eb-8098-02420a4d0702')
//      final payToDTO = new VendorDTO(payTo)
//      payToDTO.id = UUID.fromString('ee2359b6-c88c-11eb-8098-02420a4d0702')

      final accountPayableRecurringInvoiceDTO = accountPayableRecurringInvoiceDataLoaderService.singleDTO(vendor, payTo)
      accountPayableRecurringInvoiceDTO["$testProp"] = invalidValue

      when:
      post("$path", accountPayableRecurringInvoiceDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage
      response[0].code == 'system.not.found'


      where:
      testProp                        | invalidValue                                                                       || errorResponsePath                     | errorMessage
      'vendor'                        | new VendorDTO(
         id: UUID.fromString('ee2359b6-c88c-11eb-8098-02420a4d0702'),
         name: "Example Vendor",
         address: null,
         payTo: new SimpleIdentifiableDTO(UUID.fromString('ee2359b6-c88c-11eb-8098-02420a4d0702')),
         freightOnboardType: new FreightOnboardTypeDTO('P', 'Prepaid'),
         paymentTerm: new SimpleIdentifiableDTO(UUID.fromString('ee2359b6-c88c-11eb-8098-02420a4d0702')),
         returnPolicy: true,
         shipVia: new SimpleIdentifiableDTO(UUID.fromString('ee2359b6-c88c-11eb-8098-02420a4d0702')),
         separateCheck: true,
         freightCalcMethodType: new FreightCalcMethodTypeDTO('I', 'Invoice'),
         chargeInventoryTax1: true,
         chargeInventoryTax2: true,
         chargeInventoryTax3: true,
         chargeInventoryTax4: true,
         federalIdNumberVerification: true,
         allowDropShipToCustomer: true,
         autoSubmitPurchaseOrder: true,
         isActive: true,
         hasRebate: true
      ) || 'vendor.id' | 'ee2359b6-c88c-11eb-8098-02420a4d0702 was unable to be found'
      'payTo'                         | new VendorDTO(
         id: UUID.fromString('ee2359b6-c88c-11eb-8098-02420a4d0702'),
         name: "Example Vendor",
         address: null,
         payTo: new SimpleIdentifiableDTO(UUID.fromString('ee2359b6-c88c-11eb-8098-02420a4d0702')),
         freightOnboardType: new FreightOnboardTypeDTO('P', 'Prepaid'),
         paymentTerm: new SimpleIdentifiableDTO(UUID.fromString('ee2359b6-c88c-11eb-8098-02420a4d0702')),
         returnPolicy: true,
         shipVia: new SimpleIdentifiableDTO(UUID.fromString('ee2359b6-c88c-11eb-8098-02420a4d0702')),
         separateCheck: true,
         freightCalcMethodType: new FreightCalcMethodTypeDTO('I', 'Invoice'),
         chargeInventoryTax1: true,
         chargeInventoryTax2: true,
         chargeInventoryTax3: true,
         chargeInventoryTax4: true,
         federalIdNumberVerification: true,
         allowDropShipToCustomer: true,
         autoSubmitPurchaseOrder: true,
         isActive: true,
         hasRebate: true
      )      || 'payTo.id'                            | 'ee2359b6-c88c-11eb-8098-02420a4d0702 was unable to be found'
      'status'                        | new AccountPayableRecurringInvoiceStatusTypeDTO('Z', 'Invalid DTO')                || 'status.value'                        | 'Z was unable to be found'
      'expenseMonthCreationIndicator' | new ExpenseMonthCreationTypeDTO('Z', 'Invalid DTO')                                || 'expenseMonthCreationIndicator.value' | 'Z was unable to be found'


   }
   def createVendorDTO(vendor) {
      new VendorDTO(vendor)
   }
   void "update one" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final payTo = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final accountPayableRecurringInvoiceEntity = accountPayableRecurringInvoiceDataLoaderService.single(company, vendor, payTo)
      final accountPayableRecurringInvoiceDTO = accountPayableRecurringInvoiceDataLoaderService.singleDTO(vendor, payTo)
      accountPayableRecurringInvoiceDTO.id = accountPayableRecurringInvoiceEntity.id

      when:
      def result = put("$path/${accountPayableRecurringInvoiceEntity.id}", accountPayableRecurringInvoiceDTO)

      then:
      notThrown(Exception)
      result != null
      result.id != null
      with(result) {
         id == accountPayableRecurringInvoiceDTO.id
         vendor.id == accountPayableRecurringInvoiceDTO.vendor.id
         invoice == accountPayableRecurringInvoiceDTO.invoice
         invoiceAmount == accountPayableRecurringInvoiceDTO.invoiceAmount
         fixedAmountIndicator == accountPayableRecurringInvoiceDTO.fixedAmountIndicator
         employeeNumberId == accountPayableRecurringInvoiceDTO.employeeNumberId
         message == accountPayableRecurringInvoiceDTO.message
         codeIndicator == accountPayableRecurringInvoiceDTO.codeIndicator
         type == accountPayableRecurringInvoiceDTO.type
         invoice == accountPayableRecurringInvoiceDTO.invoice
         payTo.id == accountPayableRecurringInvoiceDTO.payTo.id
         lastTransferToCreateInvoiceDate == accountPayableRecurringInvoiceDTO.lastTransferToCreateInvoiceDate.toString()

         with(status) {
            value == accountPayableRecurringInvoiceDTO.status.value
            description == accountPayableRecurringInvoiceDTO.status.description
         }

         dueDays == accountPayableRecurringInvoiceDTO.dueDays
         automatedIndicator == accountPayableRecurringInvoiceDTO.automatedIndicator
         separateCheckIndicator == accountPayableRecurringInvoiceDTO.separateCheckIndicator

         with(expenseMonthCreationIndicator) {
            value == accountPayableRecurringInvoiceDTO.expenseMonthCreationIndicator.value
            description == accountPayableRecurringInvoiceDTO.expenseMonthCreationIndicator.description
         }

         invoiceDay == accountPayableRecurringInvoiceDTO.invoiceDay
         expenseDay == accountPayableRecurringInvoiceDTO.expenseDay
         schedule == accountPayableRecurringInvoiceDTO.schedule
         lastCreatedInPeriod == accountPayableRecurringInvoiceDTO.lastCreatedInPeriod.toString()
         nextCreationDate == accountPayableRecurringInvoiceDTO.nextCreationDate.toString()
         nextInvoiceDate == accountPayableRecurringInvoiceDTO.nextInvoiceDate.toString()
         nextExpenseDate == accountPayableRecurringInvoiceDTO.nextExpenseDate.toString()
         startDate == accountPayableRecurringInvoiceDTO.startDate.toString()
         endDate == accountPayableRecurringInvoiceDTO.endDate.toString()
      }
   }

   void "update valid Account Payable Recurring Invoice without nullable properties" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final payTo = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final accountPayableRecurringInvoiceEntity = accountPayableRecurringInvoiceDataLoaderService.single(company, vendor, payTo)
      final accountPayableRecurringInvoiceDTO = accountPayableRecurringInvoiceDataLoaderService.singleDTO(vendor, payTo)
      accountPayableRecurringInvoiceDTO.id = accountPayableRecurringInvoiceEntity.id
      accountPayableRecurringInvoiceDTO.message = null
      accountPayableRecurringInvoiceDTO.codeIndicator = null
      accountPayableRecurringInvoiceDTO.lastTransferToCreateInvoiceDate = null
      accountPayableRecurringInvoiceDTO.schedule = null
      accountPayableRecurringInvoiceDTO.lastCreatedInPeriod = null
      accountPayableRecurringInvoiceDTO.nextCreationDate = null
      accountPayableRecurringInvoiceDTO.nextExpenseDate = null
      accountPayableRecurringInvoiceDTO.nextInvoiceDate = null

      when:
      def result = put("$path/${accountPayableRecurringInvoiceEntity.id}", accountPayableRecurringInvoiceDTO)

      then:
      notThrown(Exception)
      result != null
      result.id != null
      with(result) {
         id == accountPayableRecurringInvoiceDTO.id
         vendor.id == accountPayableRecurringInvoiceDTO.vendor.id
         invoice == accountPayableRecurringInvoiceDTO.invoice
         invoiceAmount == accountPayableRecurringInvoiceDTO.invoiceAmount
         fixedAmountIndicator == accountPayableRecurringInvoiceDTO.fixedAmountIndicator
         employeeNumberId == accountPayableRecurringInvoiceDTO.employeeNumberId
         message == null
         codeIndicator == null
         type == accountPayableRecurringInvoiceDTO.type
         invoice == accountPayableRecurringInvoiceDTO.invoice
         payTo.id == accountPayableRecurringInvoiceDTO.payTo.id
         lastTransferToCreateInvoiceDate == null

         with(status) {
            value == accountPayableRecurringInvoiceDTO.status.value
            description == accountPayableRecurringInvoiceDTO.status.description
         }

         dueDays == accountPayableRecurringInvoiceDTO.dueDays
         automatedIndicator == accountPayableRecurringInvoiceDTO.automatedIndicator
         separateCheckIndicator == accountPayableRecurringInvoiceDTO.separateCheckIndicator

         with(expenseMonthCreationIndicator) {
            value == accountPayableRecurringInvoiceDTO.expenseMonthCreationIndicator.value
            description == accountPayableRecurringInvoiceDTO.expenseMonthCreationIndicator.description
         }

         invoiceDay == accountPayableRecurringInvoiceDTO.invoiceDay
         expenseDay == accountPayableRecurringInvoiceDTO.expenseDay
         schedule == null
         lastCreatedInPeriod == null
         nextCreationDate == null
         nextInvoiceDate == null
         nextExpenseDate == null
         startDate == accountPayableRecurringInvoiceDTO.startDate.toString()
         endDate == accountPayableRecurringInvoiceDTO.endDate.toString()
      }
   }

   void "update invalid Account Payable Recurring Invoice with non-existing values" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final shipVia = shipViaFactoryService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final payTo = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)
      final nonExistentVendorId = new VendorDTO(vendor)
      nonExistentVendorId.id = UUID.randomUUID()
      final nonExistentPayToId = new VendorDTO(payTo)
      nonExistentPayToId.id = UUID.randomUUID()
      final accountPayableRecurringInvoiceEntity = accountPayableRecurringInvoiceDataLoaderService.single(company, vendor, payTo)
      final accountPayableRecurringInvoiceDTO = accountPayableRecurringInvoiceDataLoaderService.singleDTO(vendor, payTo)
      accountPayableRecurringInvoiceDTO.id = accountPayableRecurringInvoiceEntity.id
      accountPayableRecurringInvoiceDTO.vendor = nonExistentVendorId
      accountPayableRecurringInvoiceDTO.payTo = nonExistentPayToId
      accountPayableRecurringInvoiceDTO.status = new AccountPayableRecurringInvoiceStatusTypeDTO('Z', 'Invalid DTO')
      accountPayableRecurringInvoiceDTO.expenseMonthCreationIndicator = new ExpenseMonthCreationTypeDTO('Z', 'Invalid DTO')

      when:
      put("$path/${accountPayableRecurringInvoiceEntity.id}", accountPayableRecurringInvoiceDTO)


      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson().collect().sort { a,b -> a.path <=> b.path }
      response.size() == 4
      response[0].path == 'expenseMonthCreationIndicator.value'
      response[0].message == 'Z was unable to be found'
      response[0].code == 'system.not.found'
      response[1].path == 'payTo.id'
      response[1].message == "$nonExistentPayToId.id was unable to be found"
      response[1].code == 'system.not.found'
      response[2].path == 'status.value'
      response[2].message == 'Z was unable to be found'
      response[2].code == 'system.not.found'
      response[3].path == 'vendor.id'
      response[3].message == "$nonExistentVendorId.id was unable to be found"
      response[3].code == 'system.not.found'
   }
}
