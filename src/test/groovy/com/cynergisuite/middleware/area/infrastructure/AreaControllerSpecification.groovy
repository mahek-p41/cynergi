package com.cynergisuite.middleware.area.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.area.AccountPayable
import com.cynergisuite.middleware.area.AreaService
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
      final company = companyFactoryService.forDatasetCode('tstds1')
      final signatureCapture = areaService.enableFor(company, AccountPayable.INSTANCE)

      when:
      def response = getForResponse("/area/available/${AccountPayable.INSTANCE.value}")

      then:
      notThrown(HttpClientResponseException)
      response.status == HttpStatus.OK
   }

   void "AP not enabled" () {
      setup:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final signatureCapture = areaService.enableFor(company, SignatureCapture.INSTANCE) // enable something

      when:
      def response = getForResponse("/area/available/${AccountPayable.INSTANCE.value}")

      then:
      final ex = thrown(HttpClientResponseException)
      ex.status == HttpStatus.NOT_FOUND
   }
}
