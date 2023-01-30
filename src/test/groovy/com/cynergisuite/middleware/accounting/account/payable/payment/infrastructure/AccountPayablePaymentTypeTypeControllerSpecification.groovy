package com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentTypeTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentTypeTypeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

@MicronautTest(transactional = false)
class AccountPayablePaymentTypeTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject AccountPayablePaymentTypeTypeDataLoaderService dataLoaderService

   void "fetch all account payable payment type types" () {
      given:
      def predefinedAccountPayablePaymentTypeType = dataLoaderService.predefined().collect { new AccountPayablePaymentTypeTypeDTO(it) }

      when:
      def response = get("/accounting/account-payable/payment/type/type")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new AccountPayablePaymentTypeTypeDTO(it.value, it.description) } == predefinedAccountPayablePaymentTypeType
   }
}
