package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Company
import com.hightouchinc.cynergi.middleware.extensions.ofPairs
import com.hightouchinc.cynergi.middleware.repository.spi.RepositoryBase
import io.micronaut.spring.tx.annotation.Transactional
import org.eclipse.collections.impl.factory.Maps
import org.intellij.lang.annotations.Language
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompanyRepository @Inject constructor(
   jdbc: NamedParameterJdbcTemplate
): RepositoryBase<Company>(
   tableName = "Company",
   jdbc = jdbc,
   entityRowMapper = COMPANY_ROW_MAPPER
) {
   private companion object {
      val COMPANY_ROW_MAPPER: RowMapper<Company> = RowMapper { rs: ResultSet, _: Int ->
         Company(
            id = rs.getLong("id"),
            name = rs.getString("name")
         )
      }

      @Language("PostgreSQL")
      val CREATE_COMPANY = """
         INSERT INTO Company (name)
         VALUES (:name)
         RETURNING
           id AS id,
           name AS name
      """.trimIndent()

      @Language("PostgreSQL")
      val UPDATE_COMPANY = """
         UPDATE Company c
         SET name = :name
         WHERE id = :id
         RETURNING
           c.id AS id,
           c.name AS name
      """.trimIndent()
   }

   fun exists(name: String): Boolean =
      jdbc.queryForObject("SELECT EXISTS(SELECT id FROM $tableName WHERE name = :name)", mapOf("name" to name), Boolean::class.java)!!

   @Transactional
   override fun insert(entity: Company): Company {
      return jdbc.queryForObject(
         CREATE_COMPANY,
         Maps.mutable.ofPairs("name" to entity.name),
         entityRowMapper
      )!!
   }

   @Transactional
   override fun update(entity: Company): Company {
      return jdbc.queryForObject(
         UPDATE_COMPANY,
         Maps.mutable.ofPairs(
            "id" to entity.id,
            "name" to entity.name
         ),
         entityRowMapper
      )!!
   }
}
