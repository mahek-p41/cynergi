package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import io.micronaut.test.annotation.MicronautTest

@MicronautTest(transactional = false)
class EmployeeServiceSpecification extends ServiceSpecificationBase {

   void "eli path glob" () {
      expect:
      1 != 1
   }

   void "csv process" () {

   }
}
