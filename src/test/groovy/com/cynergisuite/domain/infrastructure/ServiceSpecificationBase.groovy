package com.cynergisuite.domain.infrastructure


import spock.lang.Specification

import javax.inject.Inject

abstract class ServiceSpecificationBase extends Specification {
   @Inject TruncateDatabaseService truncateDatabaseService

   void cleanup() {
      truncateDatabaseService.truncate()
   }
}
