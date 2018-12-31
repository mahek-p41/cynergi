package com.hightouchinc.cynergi.middleware.config

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import org.springframework.context.MessageSource
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.inject.Inject
import javax.inject.Singleton
import javax.sql.DataSource

@Factory
class ExternalBeanFactory @Inject constructor(
   private val dataSource: DataSource
) {

   @Bean
   @Singleton
   fun jdbcTemplate(): JdbcTemplate {
      return JdbcTemplate(dataSource)
   }

   @Bean
   @Singleton
   fun namedJdbcTemplate(jdbcTemplate: JdbcTemplate): NamedParameterJdbcTemplate {
      return NamedParameterJdbcTemplate(jdbcTemplate)
   }

   @Bean
   @Singleton
   fun messageSource(): MessageSource {
      val resourceBundleMessageSource = ResourceBundleMessageSource()

      resourceBundleMessageSource.setBasename("i18n/messages")

      return resourceBundleMessageSource
   }
}
