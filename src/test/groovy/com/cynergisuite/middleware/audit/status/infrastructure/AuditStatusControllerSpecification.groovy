package com.cynergisuite.middleware.audit.status.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.audit.status.AuditStatusValueObject
import com.cynergisuite.middleware.audit.status.Canceled
import com.cynergisuite.middleware.audit.status.Completed
import com.cynergisuite.middleware.audit.status.Created
import com.cynergisuite.middleware.audit.status.InProgress
import com.cynergisuite.middleware.audit.status.SignedOff
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

@MicronautTest(transactional = false)
class AuditStatusControllerSpecification extends ControllerSpecificationBase {

   void "fetch all audit statuses" () {
      when:
      def response = get "/audit/status"

      then:
      notThrown(HttpClientResponseException)
      response.size() == 5
      response.collect { new AuditStatusValueObject(it) }.sort { o1, o2 -> o1.value <=> o2.value } == [
         new AuditStatusValueObject(Created.INSTANCE),
         new AuditStatusValueObject(InProgress.INSTANCE),
         new AuditStatusValueObject(Completed.INSTANCE),
         new AuditStatusValueObject(Canceled.INSTANCE),
         new AuditStatusValueObject(SignedOff.INSTANCE),
      ].sort { o1, o2 -> o1.value <=> o2.value }
   }
}
