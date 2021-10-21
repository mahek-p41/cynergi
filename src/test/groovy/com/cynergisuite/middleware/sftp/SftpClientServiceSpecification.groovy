package com.cynergisuite.middleware.sftp

import com.cynergisuite.middleware.ssh.SftpClientService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import spock.lang.TempDir

import javax.inject.Inject
import java.nio.file.Path

@MicronautTest
class SftpClientServiceSpecification extends Specification {

   @TempDir File tempDir
   @Inject SftpClientService sftpClientService

   void "Single file upload" () {
      given:
      final uuid = UUID.randomUUID()
      final tempFile = new File(tempDir, "test.file")
      tempFile.write("Test file $uuid")

      when:
      sftpClientService.transferFile(Path.of("test-${uuid}.txt"), "sftpuser", "password", "0.0.0.0", 2223) { fileChannel ->
         try(final inputChannel = new FileInputStream(tempFile).channel) {
            println "File size = ${inputChannel.size()}"

            inputChannel.transferTo(0, inputChannel.size(), fileChannel)
         }
      }

      then:
      notThrown(Exception)
      def uploadedFile = new File("/tmp/sftpuser/test-${uuid}.txt")
      uploadedFile.exists()
      uploadedFile.text == "Test file $uuid"
      uploadedFile.delete()
   }
}
