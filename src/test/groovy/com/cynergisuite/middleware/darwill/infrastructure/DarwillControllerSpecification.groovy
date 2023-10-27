package com.cynergisuite.middleware.darwill.infrastructure

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.domain.infrastructure.SimpleTransactionalSql
import com.cynergisuite.middleware.manager.SftpClientCredentialsDto
import com.cynergisuite.middleware.darwill.DarwillTestDataLoaderService
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
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
      final tstds1 = companies.find { it.datasetCode == "coravt"}

      when:
      def result = client.toBlocking().exchange(POST("/darwill", new SftpClientCredentialsDto(tstds1.id, "username", "password", "host", 12345)))

      then:
      notThrown(Exception)
      result.status == OK
      sql.firstRow("SELECT count(*) AS schedules FROM schedule WHERE title LIKE 'Darwill%'").schedules == 6
   }

   void "enable with a null company" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "coravt"}

      when:
      client.toBlocking().exchange(POST("/darwill", new SftpClientCredentialsDto(null, "username", "password", "host", 12345)), Argument.of(String), Argument.of(String))

      then:
      def ex = thrown(HttpClientResponseException)
      ex.status == HttpStatus.BAD_REQUEST
      def body = ex.response.bodyAsJson()
      body.size() == 1
      body[0].message == "Is required"
      body[0].code == "javax.validation.constraints.NotNull.message"
      body[0].path == "enableDarwill.darwillManagement.companyId"
   }

   void "disable darwill for a company" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "coravt"}
      final schedules = darwillTestDataLoaderService.enableDarwill(tstds1)

      when:
      def result = client.toBlocking().exchange(HttpRequest.DELETE("/darwill/${tstds1.id}"))

      then:
      notThrown(Exception)
      result.status == OK
      sql.firstRow("SELECT count(*) AS schedules FROM schedule WHERE title LIKE 'Darwill%'").schedules == 0
   }
}
