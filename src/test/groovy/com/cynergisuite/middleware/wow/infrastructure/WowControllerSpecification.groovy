package com.cynergisuite.middleware.wow.infrastructure

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.domain.infrastructure.SimpleTransactionalSql
import com.cynergisuite.middleware.manager.SftpClientCredentialsDto
import com.cynergisuite.middleware.wow.WowTestDataLoaderService
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
class WowControllerSpecification extends ServiceSpecificationBase {
   @Inject @Client("/manage") HttpClient client
   @Inject SimpleTransactionalSql sql
   @Inject WowTestDataLoaderService WowTestDataLoaderService

   void "enable wow for a company" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "tstds1"}

      when:
      def result = client.toBlocking().exchange(POST("/wow", new SftpClientCredentialsDto(tstds1.id, "username", "password", "host", 12345)))

      then:
      notThrown(Exception)
      result.status == OK
      sql.firstRow("SELECT count(*) AS schedules FROM schedule WHERE title LIKE 'Wow%'").schedules == 13
   }

   void "enable with a null company" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "tstds1"}

      when:
      client.toBlocking().exchange(POST("/wow", new SftpClientCredentialsDto(null, "username", "password", "host", 12345)), Argument.of(String), Argument.of(String))

      then:
      def ex = thrown(HttpClientResponseException)
      ex.status == HttpStatus.BAD_REQUEST
      def body = ex.response.bodyAsJson()
      body.size() == 1
      body[0].message == "Is required"
      body[0].code == "javax.validation.constraints.NotNull.message"
      body[0].path == "enableWow.wowManagement.companyId"
   }

   void "disable wow for a company" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "tstds1"}
      final schedules = wowTestDataLoaderService.enableWow(tstds1)

      when:
      def result = client.toBlocking().exchange(HttpRequest.DELETE("/wow/${tstds1.id}"))

      then:
      notThrown(Exception)
      result.status == OK
      sql.firstRow("SELECT count(*) AS schedules FROM schedule WHERE title LIKE 'Wow%'").schedules == 7
   }
}
