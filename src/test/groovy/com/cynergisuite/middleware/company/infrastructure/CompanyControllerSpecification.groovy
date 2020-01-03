package com.cynergisuite.middleware.company.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.company.CompanyFactoryService
import com.cynergisuite.middleware.company.CompanyValueObject
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import javax.inject.Inject


import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class CompanyControllerSpecification extends ControllerSpecificationBase {
   @Inject CompanyFactoryService companyFactoryService

   void "fetch all companies" () {
      given:
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      final allCompanies = CompanyFactory.all().sort { o1, o2 -> o1.id <=> o2.id }

      when:
      def pageOneResult = get("/company${pageOne}")

      then:
      notThrown(Exception)
      new StandardPageRequest(pageOneResult.requested) == pageOne
      pageOneResult.elements != null
      pageOneResult.elements.size() == 2
      pageOneResult.totalElements == 2
      pageOneResult.elements[0].id == allCompanies[0].id
      pageOneResult.elements[0].companyNumber == allCompanies[0].number
      pageOneResult.elements[0].name == allCompanies[0].name
      pageOneResult.elements[0].dataset == allCompanies[0].dataset
      pageOneResult.elements[1].id == allCompanies[1].id
      pageOneResult.elements[1].companyNumber == allCompanies[1].number
      pageOneResult.elements[1].name == allCompanies[1].name
      pageOneResult.elements[1].dataset == allCompanies[1].dataset

      when:
      get("/company${pageTwo}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == NO_CONTENT
   }
}
