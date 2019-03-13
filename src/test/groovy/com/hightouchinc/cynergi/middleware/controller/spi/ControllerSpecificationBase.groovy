package com.hightouchinc.cynergi.middleware.controller.spi

import com.hightouchinc.cynergi.test.helper.TruncateDatabaseService
import io.micronaut.context.ApplicationContext
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.DefaultHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import java.time.Duration

abstract class ControllerSpecificationBase extends Specification {
   @Shared @AutoCleanup EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
   @Shared @AutoCleanup HttpClient reactiveClient = HttpClient.create(embeddedServer.URL)
   @Shared applicationContext = embeddedServer.applicationContext
   private @Shared truncateDatabaseService = applicationContext.getBean(TruncateDatabaseService)
   @Shared BlockingHttpClient client

   void setupSpec() {
      ((DefaultHttpClient)reactiveClient).configuration.readTimeout = Duration.ofHours(1) // because sometimes it takes longer that 10 seconds to debug something
      client = reactiveClient.toBlocking()
   }

   void cleanup() {
      truncateDatabaseService.truncate()
   }
}
