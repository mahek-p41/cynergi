package com.cynergisuite.middleware.accounting.account.payable.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceTypeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class AccountPayableInvoiceTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject AccountPayableInvoiceTypeDataLoaderService dataLoaderService

   void "fetch all account payable invoice types" () {
      given:
      def predefinedAccountPayableInvoiceType = dataLoaderService.predefined().collect { new AccountPayableInvoiceTypeDTO(it) }

      when:
      def response = get( "/accounting/account-payable/type/invoice")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new AccountPayableInvoiceTypeDTO(it.value, it.description) } == predefinedAccountPayableInvoiceType
   }
}
