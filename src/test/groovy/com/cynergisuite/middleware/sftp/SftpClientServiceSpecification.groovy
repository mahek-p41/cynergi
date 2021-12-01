package com.cynergisuite.middleware.sftp

import com.cynergisuite.middleware.ssh.SftpClientService
import io.micronaut.context.annotation.Value
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import spock.lang.TempDir

import jakarta.inject.Inject
import java.nio.file.Path

@MicronautTest
class SftpClientServiceSpecification extends Specification {

   @TempDir File tempDir
   @Inject SftpClientService sftpClientService
   @Inject @Value("\${test.sftp.hostname}") String sftpHostname
   @Inject @Value("\${test.sftp.port}") int sftpPort

   void "Single file upload" () {
      given:
      final uuid = UUID.randomUUID()
      final tempFile = new File(tempDir, "test.file")
      tempFile.write("Test file $uuid")

      when:
      sftpClientService.transferFile(Path.of("test-${uuid}.txt"), "sftpuser", "password", sftpHostname, sftpPort) { fileChannel ->
         try(final inputChannel = new FileInputStream(tempFile).channel) {
            println "File size = ${inputChannel.size()}"

            inputChannel.transferTo(0, inputChannel.size(), fileChannel)
         }
      }

      then:
      notThrown(Exception)
   }
}
