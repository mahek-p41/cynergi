package com.cynergisuite.middleware.inload

import io.micronaut.context.annotation.Value
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.scheduling.io.watch.event.FileChangedEvent
import io.micronaut.scheduling.io.watch.event.WatchEventType.CREATE
import io.micronaut.scheduling.io.watch.event.WatchEventType.MODIFY
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.FileSystems
import java.nio.file.Path

@Singleton
class InloadEventListener @Inject constructor(
   private val inloadService: InloadService,
   @Value("\${micronaut.io.watch.paths}")
   private val watchDirectory: String,
) : ApplicationEventListener<FileChangedEvent> {
   private val logger: Logger = LoggerFactory.getLogger(InloadEventListener::class.java)
   private val watchPath = Path.of(watchDirectory)
   private val csvPathGlob = "glob:*"
   private val csvPathMatcher = FileSystems.getDefault().getPathMatcher(csvPathGlob)
   private val processedPathMatcher = FileSystems.getDefault().getPathMatcher("glob:*.PROCESSED")

   override fun onApplicationEvent(event: FileChangedEvent) {
      try {
         val changedPath = event.path
         val changedAbsolutePath = watchPath.resolve(changedPath).toAbsolutePath()

         if (!processedPathMatcher.matches(changedPath)) {
            logger.info("File change detected {} of type {}", changedAbsolutePath, event.eventType.name)

            if (csvPathMatcher.matches(changedPath.fileName)) {
               when (event.eventType) {
                  CREATE, MODIFY -> {
                     logger.debug("File {} was {}, attempting to process", changedAbsolutePath, event.eventType.name)
                     inloadService.processPath(changedAbsolutePath)
                  }
                  else -> {
                     logger.warn("File {} change was not of type CREATE or MODIFY and will not be processed", changedAbsolutePath)
                  }
               }
            } else {
               logger.warn("File change for {} did not match {}", changedPath, csvPathGlob)
            }
         }
      } catch (e: Throwable) {
         logger.error("Error occurred processing ${event.path}", e)
      }
   }
}
