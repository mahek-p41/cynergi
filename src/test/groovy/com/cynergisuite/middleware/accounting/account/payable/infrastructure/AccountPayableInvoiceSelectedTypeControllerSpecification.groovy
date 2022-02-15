package com.cynergisuite.middleware.accounting.account.payable.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceSelectedTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceSelectedTypeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import jakarta.inject.Inject

@MicronautTest(transactional = false)
class AccountPayableInvoiceSelectedTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject AccountPayableInvoiceSelectedTypeDataLoaderService dataLoaderService

   void "fetch all account payable invoice selected types" () {
      given:
      def predefinedAccountPayableInvoiceSelectedType = dataLoaderService.predefined().collect { new AccountPayableInvoiceSelectedTypeDTO(it) }

      when:
      def response = get("/accounting/account-payable/type/invoice-selected")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new AccountPayableInvoiceSelectedTypeDTO(it.value, it.description) } == predefinedAccountPayableInvoiceSelectedType
   }
}
