package com.cynergisuite.middleware

import groovy.sql.Sql
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory

import javax.sql.DataSource

@Factory
@CompileStatic
class TestingBeanFactory {

   @Bean(preDestroy = "close")
   Sql groovySql(DataSource dataSource) {
      return new Sql(dataSource)
   }
}

