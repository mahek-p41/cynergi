package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.controller.spi.ControllerTestsBase
import com.hightouchinc.cynergi.middleware.data.transfer.Company
import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.test.data.loader.CompanyTestDataLoaderService

import static io.micronaut.http.HttpRequest.GET

class CompanyControllerTests extends ControllerTestsBase {
    def companyTestDataLoaderService = applicationContext.getBean(CompanyTestDataLoaderService)

    void "fetch one company" () {
        when:
           def savedCompany = companyTestDataLoaderService.stream(1).findFirst().orElseThrow { new NotFoundException("Unable to create Company") }
        then:
            client.toBlocking().retrieve(GET("/api/v1/companies/${savedCompany.id}"), Company) == savedCompany
    }
}
