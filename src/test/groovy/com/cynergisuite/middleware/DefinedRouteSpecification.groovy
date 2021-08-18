package com.cynergisuite.middleware

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class DefinedRouteSpecification extends ControllerSpecificationBase {
   void "Check that non-existent router returns 404" () {
      when:
      get("/api/some/route/that/does/not/exist")

      then:
      final ex = thrown(HttpClientResponseException)
      ex.status == NOT_FOUND
   }
}
