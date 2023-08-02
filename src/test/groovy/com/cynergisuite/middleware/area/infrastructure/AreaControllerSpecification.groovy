package com.cynergisuite.middleware.area.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.area.AccountPayable
import com.cynergisuite.middleware.area.AreaService
import com.cynergisuite.middleware.area.GeneralLedger
import com.cynergisuite.middleware.area.SignatureCapture
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

@MicronautTest(transactional = false)
class AreaControllerSpecification extends ControllerSpecificationBase {
   @Inject AreaService areaService

   void "AP enabled" () {
      setup:
      final company = companyFactoryService.forDatasetCode('coravt')
      final signatureCapture = areaService.enableFor(company, AccountPayable.INSTANCE)

      when:
      def response = getForResponse("/area/available/${AccountPayable.INSTANCE.value}")

      then:
      notThrown(HttpClientResponseException)
      response.status == HttpStatus.OK
   }

   void "GL not enabled" () {
      setup:
      final company = companyFactoryService.forDatasetCode('coravt')
      final signatureCapture = areaService.enableFor(company, SignatureCapture.INSTANCE) // enable something

      when:
      def response = getForResponse("/area/available/${GeneralLedger.INSTANCE.value}")

      then:
      final ex = thrown(HttpClientResponseException)
      ex.status == HttpStatus.NOT_FOUND
   }

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
}
