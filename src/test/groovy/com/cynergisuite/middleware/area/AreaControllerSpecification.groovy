package com.cynergisuite.middleware.area


import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

@MicronautTest(transactional = false)
class AreaControllerSpecification extends ControllerSpecificationBase {

   void "fetch all areas" () {
      when:
      def response = get("/area")

      then:
      notThrown(HttpClientResponseException)
      response.size() == 6
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

   void "disable then re-enable an area" () {
      given:
      def areas = get("/area")
      when:

      put("/area/", areas[1])
      def disabledResult = get("/area")

      then:
      notThrown(Exception)
      disabledResult != null
      def disabledArea = disabledResult.find { it.value == "BR" }
      disabledArea != null
      !disabledArea.enabled


      when:
      put("/area/", areas[1])
      def enabledResult = get("/area")

      then:
      notThrown(Exception)
      def enabledArea = enabledResult.find { it.value == "BR" }
      enabledResult != null
      enabledArea != null
      enabledArea.enabled
   }
}
