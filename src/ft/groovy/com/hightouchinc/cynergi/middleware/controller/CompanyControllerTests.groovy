package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.controller.spi.ControllerTestsBase
import com.hightouchinc.cynergi.middleware.domain.NotFound
import com.hightouchinc.cynergi.middleware.entity.Company
import com.hightouchinc.cynergi.test.data.loader.CompanyTestDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException

import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpStatus.NOT_FOUND

class CompanyControllerTests extends ControllerTestsBase {
   final def url = "/api/v1/companies"
   def companyTestDataLoaderService = applicationContext.getBean(CompanyTestDataLoaderService)

   void "fetch one company"() {
      when:
      final def savedCompany = companyTestDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Company")}

      then:
      client.toBlocking().retrieve(GET("$url/${savedCompany.id}"), Company) == savedCompany
   }

   void "fetch one company not found"() {
      when:
      client.toBlocking().exchange(GET("$url/0"))

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      exception.response.getBody(NotFound.class).orElse(null) == new NotFound("0")
   }
}
