package com.cynergisuite.middleware.wow

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
class WowTestDataLoader {
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
class WowTestDataLoaderService {
   @Inject AreaRepository areaRepository
   @Inject WowService wowService
   @Inject @Value("\${test.sftp.hostname}") String host
   @Inject @Value("\${test.sftp.port}") int port

   List<ScheduleEntity> enableWow(CompanyEntity company) {
      final wowSftp = WowTestDataLoader.single(host, port)
      final enabledSchedules = wowService.enableFor(company, wowSftp)

      return enabledSchedules.collect { schedule ->
         new ScheduleEntity(schedule, schedule.arguments.collect { arg ->
            def toReturn

            if (arg.description == "sftpPassword" && arg.encrypted) {
               toReturn = arg.copy(arg.id, wowSftp.password, arg.description, arg.encrypted)
            } else {
               toReturn = arg
            }

            return toReturn
         }.toSet())
      }
   }
}
