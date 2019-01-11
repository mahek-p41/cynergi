package com.hightouchinc.cynergi.middleware.config

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import org.springframework.context.MessageSource
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.format.DateTimeFormatter.BASIC_ISO_DATE
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import javax.inject.Inject
import javax.inject.Singleton
import javax.sql.DataSource

@Factory
class ExternalBeanFactory @Inject constructor(
   private val dataSource: DataSource
) {

   @Bean
   @Singleton
   fun jdbcTemplate() = JdbcTemplate(dataSource)

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
   fun localDateDeserializer() = LocalDateDeserializer(BASIC_ISO_DATE)

   @Bean
   @Singleton
   fun localDateSerializer() = LocalDateSerializer(BASIC_ISO_DATE)

   @Bean
   @Singleton
   fun offsetDateTimeDeserializer() = OffsetDateTimeDeserializer(ISO_OFFSET_DATE_TIME)

   @Bean
   @Singleton
   fun offsetDateTimeSerializer() = OffsetDateTimeSerializer.INSTANCE
}
