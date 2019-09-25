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
      response.size() == 5
      response.collect { new AuditStatusValueObject(it) }.sort { o1, o2 -> o1.value <=> o2.value } == [
          new AuditStatusValueObject("OPENED", "Opened", "FF0000"),
          new AuditStatusValueObject("IN-PROGRESS", "In Progress", "FF6600"),
          new AuditStatusValueObject("COMPLETED", "Completed", "FFCC00"),
          new AuditStatusValueObject("CANCELED", "Canceled", "CCFF00"),
          new AuditStatusValueObject("SIGNED-OFF", "Signed Off", "66FF00"),
      ].sort { o1, o2 -> o1.value <=> o2.value }
   }
}
