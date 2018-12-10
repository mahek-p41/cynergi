package com.hightouchinc.cynergi.middleware.config

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import org.springframework.jdbc.core.JdbcTemplate
import javax.inject.Inject
import javax.inject.Singleton
import javax.sql.DataSource

@Factory
class JdbcFactory @Inject constructor(
   private val dataSource: DataSource
) {

   @Bean
   @Singleton
   fun jdbcTemplate(): JdbcTemplate {
      return JdbcTemplate(dataSource)
   }
}
