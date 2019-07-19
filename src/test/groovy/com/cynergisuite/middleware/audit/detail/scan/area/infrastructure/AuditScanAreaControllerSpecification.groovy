package com.cynergisuite.middleware.audit.detail.scan.area.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaValueObject
import io.micronaut.test.annotation.MicronautTest

@MicronautTest(transactional = false)
class AuditScanAreaControllerSpecification extends ControllerSpecificationBase {

   void "fetch all audit detail scan areas" () {
      when:
      final response = get "/audit/detail/scan-area"

      then:
      response.size() == 3
      response.collect { new AuditScanAreaValueObject(it) }.containsAll(
         [
             new AuditScanAreaValueObject("SHOWROOM", "Showroom"),
             new AuditScanAreaValueObject("STOREROOM", "Storeroom"),
             new AuditScanAreaValueObject("WAREHOUSE", "Warehouse")
         ]
      )
   }
}
