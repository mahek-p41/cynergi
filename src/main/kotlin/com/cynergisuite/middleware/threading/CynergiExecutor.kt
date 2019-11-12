package com.cynergisuite.middleware.threading

import io.micronaut.context.annotation.Value
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CynergiExecutor @Inject constructor(
   @Value("\${cynergi.worker.pool.size}") private val workerPoolSize: Int
) {
   private val logger: Logger = LoggerFactory.getLogger(CynergiExecutor::class.java)

   private val executor = Executors
      .newFixedThreadPool(
         workerPoolSize,
         BasicThreadFactory.Builder()
            .daemon(true)
            .namingPattern("cynergi-worker-thread")
            .uncaughtExceptionHandler { _, throwable -> logger.error("Unhandled exception occurred in cynergi-worker-thread", throwable) }
            .build()
      )

   fun execute(job: () -> Unit) {
      executor.execute(job)
   }
}
