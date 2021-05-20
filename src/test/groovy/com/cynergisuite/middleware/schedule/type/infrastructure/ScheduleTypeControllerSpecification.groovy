package com.cynergisuite.middleware.schedule.type.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.schedule.type.ScheduleTypeTestDataLoader
import com.cynergisuite.middleware.schedule.type.ScheduleTypeValueObject
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

@MicronautTest(transactional = false)
class ScheduleTypeControllerSpecification extends ControllerSpecificationBase {

   void "fetch all" () {
      given:
      def pageOne = new StandardPageRequest(1, 5, "id", "ASC")

      when:
      def result = get("/schedule/type${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      result.requested.with { new StandardPageRequest(it) } == pageOne
      result.totalElements == 1
      result.totalPages == 1
      result.first == true
      result.last == true
      result.elements != null
      result.elements.size() == 1
      result.elements[0].with { new ScheduleTypeValueObject(it) } == new ScheduleTypeValueObject(ScheduleTypeTestDataLoader.weekly(), ScheduleTypeTestDataLoader.weekly().description)
   }
}
