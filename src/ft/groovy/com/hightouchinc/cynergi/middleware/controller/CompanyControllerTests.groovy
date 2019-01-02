package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.controller.spi.ControllerTestsBase
import com.hightouchinc.cynergi.middleware.entity.CompanyDto
import com.hightouchinc.cynergi.test.data.loader.CompanyTestDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.hateos.JsonError

import static io.micronaut.http.HttpRequest.*
import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

class CompanyControllerTests extends ControllerTestsBase {
   final def url = "/api/v1/companies"
   def companyTestDataLoaderService = applicationContext.getBean(CompanyTestDataLoaderService)

   void "fetch one company"() {
      given:
      final def savedCompany = companyTestDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Company")}

      expect:
      client.retrieve(GET("$url/${savedCompany.id}"), CompanyDto) == new CompanyDto(savedCompany)
   }

   void "fetch one company not found"() {
      when:
      client.exchange(GET("$url/0"))

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      exception.response.getBody(JsonError).orElse(null)?.message == "Resource 0 was unable to be found"
   }

   void "save company successfully"() {
      given:
      final CompanyDto savedCompany = client.retrieve(POST(url, new CompanyDto("Test Company")), CompanyDto)

      expect:
      savedCompany.id != null
      savedCompany.id > 0
      savedCompany.name == "Test Company"
   }

   void "save company failure due to missing name" () {
      when:
      client.exchange(POST(url, new CompanyDto(null,null)))

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST

      final error = exception.response.getBody(JsonError[]).orElse(null)
      error.length == 1
      error[0].message == "name is required"
      error[0].getPath().isPresent()
      error[0].getPath().get() == "name"
   }

   void "update company name successfully"() {
      given:
      final def company = companyTestDataLoaderService.stream(1).findFirst().orElseThrow{ new Exception("Unable to create Company") }

      when:
      final def update = new CompanyDto(company.id, "Test Company Updated")
      final CompanyDto updatedCompany = client.retrieve(PUT(url, update), CompanyDto)

      then:
      updatedCompany == update
   }
}
