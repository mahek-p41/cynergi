package com.hightouchinc.cynergi.middleware.data.access

import com.hightouchinc.cynergi.middleware.data.transfer.Business
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import org.intellij.lang.annotations.Language
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusinessDataAccessObject @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private companion object {

      @Language("GenericSQL")
      val FETCH_BUSINESS_BY_ID = """
           SELECT
               b.id AS id,
               b.name AS name
            FROM Business b
       """.trimIndent()
   }

   fun fetchOne(id: Long): Business? {
      return jdbc.findFirstOrNull(FETCH_BUSINESS_BY_ID, mapOf("id" to id)) { rs, _ ->
         Business(
            id = rs.getLong("id"),
            name = rs.getString("name")
         )
      }
   }
}
