package com.cynergisuite.middleware.area


import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

@MicronautTest(transactional = false)
class AreaControllerSpecification extends ControllerSpecificationBase {

   void "fetch all areas" () {
      when:
      def response = get("/area")

      then:
      notThrown(HttpClientResponseException)
      response.size() == 4
      with(response[0]) {
         id == 1
         value == "AP"
         description == "Account Payable"
         enabled == true
      }
      with(response[1]) {
         id == 2
         value == "BR"
         description == "Bank Reconciliation"
         enabled == true
      }
      with(response[2]) {
         id == 3
         value == "GL"
         description == "General Ledger"
         enabled == false
      }
      with(response[3]) {
         id == 4
         value == "PO"
         description == "Purchase Order"
         enabled == true
      }
   }
}
