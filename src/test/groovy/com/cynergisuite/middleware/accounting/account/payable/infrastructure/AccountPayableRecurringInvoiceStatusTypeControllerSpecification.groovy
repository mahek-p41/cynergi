package com.cynergisuite.middleware.accounting.account.payable.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableRecurringInvoiceStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableRecurringInvoiceStatusTypeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

@MicronautTest(transactional = false)
class AccountPayableRecurringInvoiceStatusTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject AccountPayableRecurringInvoiceStatusTypeDataLoaderService dataLoaderService

   void "fetch all account payable recurring invoice status types" () {
      given:
      def predefinedAccountPayableRecurringInvoiceStatusType = dataLoaderService.predefined().collect { new AccountPayableRecurringInvoiceStatusTypeDTO(it) }

      when:
      def response = get("/accounting/account-payable/type/recurring-invoice-status")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new AccountPayableRecurringInvoiceStatusTypeDTO(it.value, it.description) } == predefinedAccountPayableRecurringInvoiceStatusType
   }
}
