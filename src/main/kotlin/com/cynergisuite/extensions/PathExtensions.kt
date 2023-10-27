package com.cynergisuite.extensions

import org.apache.commons.io.input.CloseShieldInputStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.RandomAccessFile
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Path

private val logger: Logger = LoggerFactory.getLogger("com.hightouchtechnologies.commerce.extensions.PathExtension")

fun Path.fileExists(): Boolean =
   Files.exists(this) && Files.isRegularFile(this)

fun Path.tryLockForReader(process: (reader: BufferedReader) -> Unit): Boolean {
   val pathFile = this.toFile()
   var locked = false

   logger.debug("Attempting lock on {}", this)

   RandomAccessFile(pathFile, "rw").use { raf ->
      val fileChannel = raf.channel
      val lock = fileChannel.tryLock()

      if (lock != null) {
         logger.debug("Successfully locked {}", this)
         val inputStream = CloseShieldInputStream(Channels.newInputStream(fileChannel))
         val reader = BufferedReader(InputStreamReader(inputStream))

         process(reader)

         lock.release()

         logger.debug("Released lock on {}", this)

         locked = true
      } else {
         logger.debug("Unable to lock {}", this)
      }
   }

   return locked
}
