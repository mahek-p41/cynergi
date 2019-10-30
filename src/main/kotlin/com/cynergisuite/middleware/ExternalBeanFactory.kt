package com.cynergisuite.middleware

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Value
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Singleton
import javax.sql.DataSource

@Factory
class ExternalBeanFactory {

   @Bean
   @Singleton
   fun jdbcTemplate(dataSource: DataSource) = JdbcTemplate(dataSource)

   @Bean
   @Singleton
   fun namedJdbcTemplate(jdbcTemplate: JdbcTemplate) = NamedParameterJdbcTemplate(jdbcTemplate)

   @Bean
   @Singleton
   fun messageSource(): MessageSource {
      val resourceBundleMessageSource = ResourceBundleMessageSource()

      resourceBundleMessageSource.setBasename("i18n/messages")

      return resourceBundleMessageSource
   }

   @Bean
   @Singleton
   fun threadPool(@Value("\${cynergi.worker.pool.size}") workerPoolSize: Int): ExecutorService {
      val logger: Logger = LoggerFactory.getLogger("CynergiWorkerThreadUncaughtExceptionLogger")

      return Executors
         .newFixedThreadPool(
            workerPoolSize,
            BasicThreadFactory.Builder()
               .daemon(true)
               .namingPattern("cynergi-worker-thread")
               .uncaughtExceptionHandler { thread, throwable -> logger.error("Unhandled exception occurred in cynergi-worker-thread", throwable) }
               .build()
         )
   }
}
