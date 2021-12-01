package com.cynergisuite.middleware.darwill.infrastructure

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.domain.infrastructure.SimpleTransactionalSql
import com.cynergisuite.middleware.darwill.DarwillManagementDto
import com.cynergisuite.middleware.darwill.DarwillTestDataLoaderService
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject


import static io.micronaut.http.HttpRequest.POST
import static io.micronaut.http.HttpStatus.OK

@MicronautTest(transactional = false)
class DarwillControllerSpecification extends ServiceSpecificationBase {
   @Inject @Client("/manage") HttpClient client
   @Inject SimpleTransactionalSql sql
   @Inject DarwillTestDataLoaderService darwillTestDataLoaderService

   void "enable darwill for a company" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "tstds1"}

      when:
      def result = client.toBlocking().exchange(POST("/darwill", new DarwillManagementDto(tstds1.datasetCode, "username", "password", "host", 12345)))

      then:
      notThrown(Exception)
      result.status == OK
      sql.firstRow("SELECT count(*) AS schedules FROM schedule WHERE title LIKE 'Darwill%'").schedules == 6
   }

   void "disable darwill for a company" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "tstds1"}
      final schedules = darwillTestDataLoaderService.enableDarwill(tstds1)

      when:
      def result = client.toBlocking().exchange(HttpRequest.DELETE("/darwill/${tstds1.datasetCode}"))

      then:
      notThrown(Exception)
      result.status == OK
      sql.firstRow("SELECT count(*) AS schedules FROM schedule WHERE title LIKE 'Darwill%'").schedules == 0
   }
}
