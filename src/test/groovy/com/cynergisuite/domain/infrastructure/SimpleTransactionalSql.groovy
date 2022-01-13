package com.cynergisuite.domain.infrastructure

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
@CompileStatic
@Requires(env = ["test", "load"])
class SimpleTransactionalSql {
   private final Sql sql

   SimpleTransactionalSql(Sql sql) {
      this.sql = sql
   }

   @Transactional
   int executeUpdate(Map params, String sql) {
      this.sql.executeUpdate(params, sql)
   }

   @Transactional
   GroovyRowResult firstRow(String sql, Map params = [:]) {
      this.sql.firstRow(params, sql)
   }
}
