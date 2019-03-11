package com.hightouchinc.cynergi.middleware.service.spi

import com.hightouchinc.cynergi.test.service.TruncateDatabaseService
import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

abstract class ServiceSpecificationBase extends Specification {
   @Shared @AutoCleanup EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
   @Shared applicationContext = embeddedServer.applicationContext
   private @Shared truncateDatabaseService = applicationContext.getBean(TruncateDatabaseService)

   void cleanup() {
      truncateDatabaseService.truncate()
   }
}
