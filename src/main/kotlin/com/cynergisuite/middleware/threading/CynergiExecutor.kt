package com.cynergisuite.middleware.threading

import io.micronaut.http.MediaType
import io.micronaut.http.server.types.files.StreamedFile
import jakarta.inject.Singleton
import org.apache.commons.io.output.CloseShieldOutputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.ForkJoinPool

@Singleton
class CynergiExecutor {
   private val executor = ForkJoinPool.commonPool()

   fun execute(job: () -> Unit) = executor.execute(job)

   fun pipeBlockingOutputToStreamedFile(mediaType: String, pipeTo: (outputStream: OutputStream) -> Unit): StreamedFile {
      val os = PipedOutputStream()
      val input = PipedInputStream(os)

      execute {
         try {
            pipeTo(CloseShieldOutputStream.wrap(os))
         } finally {
            os.flush()
            os.close()
         }
      }

      return StreamedFile(input, MediaType.of(mediaType))
   }
}
