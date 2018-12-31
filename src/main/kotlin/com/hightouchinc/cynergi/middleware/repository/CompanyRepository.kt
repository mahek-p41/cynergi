package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Company
import com.hightouchinc.cynergi.middleware.extensions.ofPairs
import com.hightouchinc.cynergi.middleware.repository.spi.RepositoryBase
import org.eclipse.collections.api.map.MutableMap
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
   tableName = TABLE_NAME,
   jdbc = jdbc,
   entityRowMapper = COMPANY_ROW_MAPPER,
   fetchOneQuery = FETCH_COMPANY_BY_ID,
   saveOneQuery = CREATE_COMPANY,
   updateOneQuery = UPDATE_COMPANY
) {
   private companion object {
      val COMPANY_ROW_MAPPER: RowMapper<Company> = RowMapper { rs: ResultSet, _: Int ->
         Company(
            id = rs.getLong("id"),
            name = rs.getString("name")
         )
      }

      const val TABLE_NAME = "Company"

      val COMPANY_COLUMNS = """
         id AS id,
         name AS name
      """.trimIndent()

      @Language("PostgreSQL")
      val FETCH_COMPANY_BY_ID = """
          SELECT
             $COMPANY_COLUMNS
           FROM $TABLE_NAME c
           WHERE c.id = :id
      """.trimIndent()

      @Language("PostgreSQL")
      val CREATE_COMPANY = """
          INSERT INTO $TABLE_NAME(name)
          VALUES (:name)
          RETURNING $COMPANY_COLUMNS
      """.trimIndent()

      @Language("PostgreSQL")
      val UPDATE_COMPANY = """
          UPDATE COMPANY
          SET name = :name
          WHERE id = :id
          RETURNING $COMPANY_COLUMNS
      """.trimIndent()
   }

   override fun mapOfSaveParameters(entity: Company): MutableMap<String, Any?> =
      Maps.mutable.ofPairs("name" to entity.name)
}
