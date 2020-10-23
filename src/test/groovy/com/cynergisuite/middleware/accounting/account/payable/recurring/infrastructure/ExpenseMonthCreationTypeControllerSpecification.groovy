package com.cynergisuite.middleware.accounting.account.payable.recurring.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.payable.recurring.ExpenseMonthCreationTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.recurring.ExpenseMonthCreationTypeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class ExpenseMonthCreationTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject ExpenseMonthCreationTypeDataLoaderService dataLoaderService

   void "fetch all expense month creation types" () {
      given:
      def predefinedExpenseMonthCreationType = dataLoaderService.predefined().collect { new ExpenseMonthCreationTypeDTO(it) }

      when:
      def response = get( "/accounting/account-payable/type/expense-month-creation")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new ExpenseMonthCreationTypeDTO(it.value, it.description) } == predefinedExpenseMonthCreationType
   }
}
