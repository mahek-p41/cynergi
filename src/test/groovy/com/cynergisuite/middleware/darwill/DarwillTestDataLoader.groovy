package com.cynergisuite.middleware.darwill

import com.cynergisuite.middleware.area.infrastructure.AreaRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.ssh.SftpClientCredentials
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import java.util.stream.IntStream
import java.util.stream.Stream
import jakarta.inject.Inject
import jakarta.inject.Singleton

@CompileStatic
class DarwillTestDataLoader {
   static Stream<SftpClientCredentials> stream(int numberIn = 1, String host = "localhost", int port = 2223) {
      final number = numberIn > 0 ? numberIn : 1

      return IntStream.range(0, number).mapToObj {
         new SftpClientCredentials(
            "sftpuser",
            "password",
            host,
            port,
         )
      }
   }

   static SftpClientCredentials single(String host = "localhost", int port = 2223) {
      return stream(1, host, port).findFirst().orElseThrow()
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class DarwillTestDataLoaderService {
   @Inject AreaRepository areaRepository
   @Inject DarwillService darwillService
   @Inject @Value("\${test.sftp.hostname}") String host
   @Inject @Value("\${test.sftp.port}") int port

   List<ScheduleEntity> enableDarwill(CompanyEntity company) {
      final darwillSftp = DarwillTestDataLoader.single(host, port)
      final enabledSchedules = darwillService.enableFor(company, darwillSftp)

      return enabledSchedules.collect { schedule ->
         new ScheduleEntity(schedule, schedule.arguments.collect { arg ->
            def toReturn

            if (arg.description == "sftpPassword" && arg.encrypted) {
               toReturn = arg.copy(arg.id, darwillSftp.password, arg.description, arg.encrypted)
            } else {
               toReturn = arg
            }

            return toReturn
         }.toSet())
      }
   }
}
