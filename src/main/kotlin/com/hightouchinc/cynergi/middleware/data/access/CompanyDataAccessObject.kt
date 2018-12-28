package com.hightouchinc.cynergi.middleware.data.access

import com.hightouchinc.cynergi.middleware.data.transfer.Company
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import io.micronaut.spring.tx.annotation.Transactional
import org.intellij.lang.annotations.Language
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompanyDataAccessObject @Inject constructor(
   jdbc: NamedParameterJdbcTemplate
): DataAccessObjectBase(
   tableName = "company",
   jdbc = jdbc
), DataAccessObject<Company> {

   private companion object {

      @Language("PostgreSQL")
      val FETCH_COMPANY_BY_ID = """
           SELECT
              c.id AS id,
              c.name AS name
            FROM Company c
            WHERE id = :id
       """.trimIndent()

      @Language("PostgreSQL")
      val CREATE_NEW_COMPANY = """
         INSERT INTO Company(id, name)
         VALUES (:id, :name)
      """.trimIndent()
   }

   override fun fetchOne(id: Long): Company? {
      return jdbc.findFirstOrNull(FETCH_COMPANY_BY_ID, mapOf("id" to id)) { rs, _ ->
         Company(
            id = rs.getLong("id"),
            name = rs.getString("name")
         )
      }
   }

   @Transactional
   override fun save(company: Company): Company {
      val id: Long = save(parameters = mapOf("name" to company), query = CREATE_NEW_COMPANY)

      return company.copy(id = id)
   }
}
