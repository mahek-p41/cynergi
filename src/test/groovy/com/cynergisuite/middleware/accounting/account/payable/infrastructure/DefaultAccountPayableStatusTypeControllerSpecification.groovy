package com.cynergisuite.middleware.accounting.account.payable.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.payable.DefaultAccountPayableStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.DefaultAccountPayableStatusTypeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class DefaultAccountPayableStatusTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject DefaultAccountPayableStatusTypeDataLoaderService dataLoaderService

   void "fetch all default account payable status types" () {
      given:
      def predefinedDefaultAccountPayableStatusType = dataLoaderService.predefined().collect { new DefaultAccountPayableStatusTypeDTO(it) }

      when:
      def response = get( "/accounting/account/payable/type/default-account-payable-status")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new DefaultAccountPayableStatusTypeDTO(it.value, it.description) } == predefinedDefaultAccountPayableStatusType
   }
}
