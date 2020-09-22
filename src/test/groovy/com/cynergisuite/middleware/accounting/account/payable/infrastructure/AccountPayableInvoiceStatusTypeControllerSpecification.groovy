package com.cynergisuite.middleware.accounting.account.payable.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusTypeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class AccountPayableInvoiceStatusTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject AccountPayableInvoiceStatusTypeDataLoaderService dataLoaderService

   void "fetch all account payable invoice status types" () {
      given:
      def predefinedAccountPayableInvoiceStatusType = dataLoaderService.predefined().collect { new AccountPayableInvoiceStatusTypeDTO(it) }

      when:
      def response = get( "/accounting/account/payable/type/invoice-status")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new AccountPayableInvoiceStatusTypeDTO(it.value, it.description) } == predefinedAccountPayableInvoiceStatusType
   }
}
