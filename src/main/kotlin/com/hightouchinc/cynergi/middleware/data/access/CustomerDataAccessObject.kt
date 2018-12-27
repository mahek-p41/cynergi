package com.hightouchinc.cynergi.middleware.data.access

import com.hightouchinc.cynergi.middleware.data.domain.Page
import com.hightouchinc.cynergi.middleware.data.transfer.Company
import com.hightouchinc.cynergi.middleware.data.transfer.Customer
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import org.intellij.lang.annotations.Language
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerDataAccessObject @Inject constructor(
   jdbc: NamedParameterJdbcTemplate
): DataAccessObjectBase (
   tableName = "customer",
   jdbc = jdbc
), IdentityDataAccess<Customer> {

   private companion object {

      @Language("PostgreSQL")
      val FETCH_CUSTOMER_BY_ID = """
         SELECT
            c.first_name
         FROM Customer
         WHERE id = :id
      """.trimIndent()
   }

   override fun fetchOne(id: Long): Customer? {
      return jdbc.findFirstOrNull(FETCH_CUSTOMER_BY_ID, mapOf("id" to id)) { rs, _ ->
         Customer(
            firstName = rs.getString("firstName")
         )
      }
   }

   fun searchForCustomers(customerSearchString: String): Page<Customer> {
      /*val content = jdbcTemplate.query("") { rs: ResultSet ->

      }*/

      return Page(listOf(), 0, 1, false, false)
   }
}
