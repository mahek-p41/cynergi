package com.hightouchinc.cynergi.middleware.controller.spi

import com.hightouchinc.cynergi.test.data.loader.TruncateDatabaseService
import io.micronaut.context.ApplicationContext
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

abstract class ControllerTestsBase extends Specification {
   @Shared @AutoCleanup EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
   @Shared @AutoCleanup HttpClient client = HttpClient.create(embeddedServer.URL)
   @Shared truncateDatabaseService = embeddedServer.applicationContext.getBean(TruncateDatabaseService)
   @Shared applicationContext = embeddedServer.applicationContext

   void cleanupSpec() {
      truncateDatabaseService.truncate()
   }
}
