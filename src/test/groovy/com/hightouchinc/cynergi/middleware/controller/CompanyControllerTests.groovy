package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.data.transfer.Company
import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.service.TruncateDatabaseService
import com.hightouchinc.cynergi.test.data.loader.CompanyTestDataLoaderService
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class CompanyControllerTests extends Specification {
    @Shared @AutoCleanup EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
    @Shared @AutoCleanup HttpClient client = HttpClient.create(embeddedServer.URL)
    @Shared truncateDatabaseService = embeddedServer.applicationContext.getBean(TruncateDatabaseService)
    @Shared ac = embeddedServer.applicationContext

    def companyTestDataLoaderService = ac.getBean(CompanyTestDataLoaderService)

    void cleanupSpec() {
        truncateDatabaseService.truncate()
    }

    void "test loading of a company" () {
        when:
           def savedCompany = companyTestDataLoaderService.stream(1).findFirst().orElseThrow { new NotFoundException("Unable to create Company") }
        then:
            client.toBlocking().retrieve(HttpRequest.GET("/api/v1/companies/${savedCompany.id}"), Company) == savedCompany
    }
}
