package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.service.TruncateDatabaseService
import io.micronaut.context.ApplicationContext
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import static io.micronaut.http.HttpRequest.GET

class BusinessControllerTests extends Specification {
    @Shared @AutoCleanup EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
    @Shared @AutoCleanup HttpClient client = HttpClient.create(embeddedServer.URL)
    @Shared truncateDatabaseService = embeddedServer.applicationContext.getBean(TruncateDatabaseService)

    def cleanupSpec() {
        truncateDatabaseService.truncate()
    }

    void "test loading of a business" () {
        expect:
            client.toBlocking().retrieve(GET("/api/v1/businesses/1")) == ""
    }
}
