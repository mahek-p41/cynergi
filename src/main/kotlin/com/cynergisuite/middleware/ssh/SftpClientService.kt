package com.cynergisuite.middleware.ssh

import org.apache.sshd.client.SshClient
import org.apache.sshd.sftp.client.SftpClient.OpenMode.Create
import org.apache.sshd.sftp.client.SftpClient.OpenMode.Write
import org.apache.sshd.sftp.client.impl.DefaultSftpClientFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.time.Duration
import javax.inject.Singleton

@Singleton
class SftpClientService {

   val verifyDuration: Duration = Duration.ofSeconds(30L)
   val logger: Logger = LoggerFactory.getLogger(SftpClientService::class.java)

   fun transferFile(fileName: Path, username: String, password: String, host: String, port: Int, copyTo: (fileChannel: FileChannel) -> Unit) {
      logger.debug("Creating ssh client")

      SshClient.setUpDefaultClient().use { sshClient ->
         try {
            logger.debug("Starting ssh client")
            sshClient.start()

            logger.debug("Connecting ssh client to {}:{}", host, port)
            sshClient.connect(username, host, port).verify(verifyDuration).session.use { session ->
               session.addPasswordIdentity(password)
               session.auth().verify(verifyDuration)

               DefaultSftpClientFactory.INSTANCE.createSftpClient(session).singleSessionInstance().use { sftpClient ->
                  val fileChannel = sftpClient.openRemoteFileChannel(fileName.toString(), Create, Write)

                  copyTo(fileChannel)
               }
            }
         } finally {
            sshClient.stop()
         }
      }
   }
}
