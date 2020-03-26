package com.cynergisuite.middleware.company.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import io.micronaut.core.type.Argument
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class CompanyControllerSpecification extends ServiceSpecificationBase {
   @Client("/api/company") @Inject RxHttpClient httpClient // since the company controller only has a single endpoint of fetchAll and it doesn't require authentication no need to use the ControllerSpecificationBase class.  Also there is nothing to cleanup

   void "fetch all companies predefined companies" () {
      given:
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")

      when:
      def pageOneResult = httpClient.toBlocking().exchange(GET(pageOne.toString()), // login without authorization
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      notThrown(Exception)
      new StandardPageRequest(pageOneResult.requested) == pageOne
      pageOneResult.elements != null
      pageOneResult.elements.size() == 2
      pageOneResult.totalElements == 2
      pageOneResult.elements[0].id == companies[0].id
      pageOneResult.elements[0].name == companies[0].name
      pageOneResult.elements[0].doingBusinessAs == companies[0].doingBusinessAs
      pageOneResult.elements[0].clientCode == companies[0].clientCode
      pageOneResult.elements[0].clientId == companies[0].clientId
      pageOneResult.elements[0].datasetCode == companies[0].datasetCode
      pageOneResult.elements[0].federalTaxNumber == companies[0].federalIdNumber
      pageOneResult.elements[1].id == companies[1].id
      pageOneResult.elements[1].name == companies[1].name
      pageOneResult.elements[1].doingBusinessAs == companies[1].doingBusinessAs
      pageOneResult.elements[1].clientCode == companies[1].clientCode
      pageOneResult.elements[1].clientId == companies[1].clientId
      pageOneResult.elements[1].datasetCode == companies[1].datasetCode
      pageOneResult.elements[1].federalTaxNumber == companies[1].federalIdNumber

      when:
      httpClient.toBlocking().exchange(GET(pageTwo.toString()), // login without authorization
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == NO_CONTENT
   }
}
