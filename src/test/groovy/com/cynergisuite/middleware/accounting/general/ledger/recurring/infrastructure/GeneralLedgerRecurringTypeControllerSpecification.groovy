package com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringTypeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringTypeDTO
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class GeneralLedgerRecurringTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject GeneralLedgerRecurringTypeDataLoaderService dataLoaderService

   void "fetch all general ledger recurring types" () {
      given:
      def predefinedGeneralLedgerRecurringType = dataLoaderService.predefined().collect { new GeneralLedgerRecurringTypeDTO(it) }

      when:
      def response = get("/accounting/general-ledger/type/recurring-type")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new GeneralLedgerRecurringTypeDTO(it.value, it.description) } == predefinedGeneralLedgerRecurringType
   }
}
