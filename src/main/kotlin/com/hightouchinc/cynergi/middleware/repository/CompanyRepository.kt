package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Company
import com.hightouchinc.cynergi.middleware.repository.spi.RepositoryBase
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
   jdbc = jdbc,
   entityRowMapper = COMPANY_ROW_MAPPER,
   fetchOneQuery = FETCH_COMPANY_BY_ID,
   saveOneQuery = CREATE_NEW_COMPANY
) {
   private companion object {
      val COMPANY_ROW_MAPPER: RowMapper<Company> = RowMapper { rs: ResultSet, _: Int ->
         Company(
            id = rs.getLong("id"),
            name = rs.getString("name")
         )
      }

      @Language("PostgreSQL")
      val FETCH_COMPANY_BY_ID = """
          SELECT
             c.id AS id,
             c.name AS name
           FROM Company c
           WHERE c.id = :id
      """.trimIndent()

      @Language("PostgreSQL")
      val CREATE_NEW_COMPANY = """
         INSERT INTO Company(name)
         VALUES (:name)
         RETURNING id, name
      """.trimIndent()
   }

   override fun mapOfSaveParameters(entity: Company): Map<String, Any?> =
      mapOf("name" to entity.name)
}
