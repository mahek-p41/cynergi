package com.cynergisuite.middleware.audit.status.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.audit.status.AuditStatusValueObject
import io.micronaut.test.annotation.MicronautTest

@MicronautTest(transactional = false)
class AuditStatusControllerSpecification extends ControllerSpecificationBase {

   void "fetch all audit statuses" () {
      when:
      final response = get "/audit/status"

      then:
      response.size() == 6
      response.collect { new AuditStatusValueObject(it) }.containsAll(
         [
             new AuditStatusValueObject("OPENED", "Opened"),
             new AuditStatusValueObject("IN-PROGRESS", "In Progress"),
             new AuditStatusValueObject("COMPLETED", "Completed"),
             new AuditStatusValueObject("CANCELED", "Canceled"),
             new AuditStatusValueObject("SIGNED-OFF", "Signed Off"),
             new AuditStatusValueObject("CLOSED", "Closed"),
         ]
      )
   }
}
