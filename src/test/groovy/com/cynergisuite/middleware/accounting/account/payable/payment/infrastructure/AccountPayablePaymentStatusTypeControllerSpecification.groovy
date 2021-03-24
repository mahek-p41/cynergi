package com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentStatusTypeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class AccountPayablePaymentStatusTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject AccountPayablePaymentStatusTypeDataLoaderService dataLoaderService

   void "fetch all account payable payment status types" () {
      given:
      def predefinedAccountPayablePaymentStatusType = dataLoaderService.predefined().collect { new AccountPayablePaymentStatusTypeDTO(it) }

      when:
      def response = get("/accounting/account-payable/payment/type/status")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new AccountPayablePaymentStatusTypeDTO(it.value, it.description) } == predefinedAccountPayablePaymentStatusType
   }
}
