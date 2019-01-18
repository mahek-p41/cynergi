package com.hightouchinc.cynergi.middleware.config

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import org.springframework.context.MessageSource
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
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
}
