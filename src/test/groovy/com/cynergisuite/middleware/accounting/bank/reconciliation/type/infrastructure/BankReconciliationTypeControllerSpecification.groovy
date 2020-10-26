package com.cynergisuite.middleware.accounting.bank.reconciliation.type.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class BankReconciliationTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject BankReconciliationTypeDataLoaderService dataLoaderService

   void "fetch all bank reconciliation types" () {
      given:
      def predefinedBankReconciliationType = dataLoaderService.predefined().collect { new BankReconciliationTypeDTO(it) }

      when:
      def response = get("/accounting/bank-recon/type")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new BankReconciliationTypeDTO(it.value, it.description) } == predefinedBankReconciliationType
   }
}
