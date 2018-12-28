package com.hightouchinc.cynergi.middleware.data.access

import com.hightouchinc.cynergi.middleware.data.domain.Page
import com.hightouchinc.cynergi.middleware.data.transfer.Customer
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import io.micronaut.spring.tx.annotation.Transactional
import org.intellij.lang.annotations.Language
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerDataAccessObject @Inject constructor(
   jdbc: NamedParameterJdbcTemplate
): DataAccessObjectBase (
   tableName = "customer",
   jdbc = jdbc
), DataAccessObject<Customer> {
   
   private companion object {

      @Language("PostgreSQL")
      val FETCH_CUSTOMER_BY_ID = """
         SELECT
            c.id AS id,
            c.account AS account,
            c.first_name AS firstName,
            c.last_name AS lastName,
            c.contact_name AS contactName,
            c.date_of_birth AS dateOfBirth
         FROM Customer c
         WHERE c.id = :id
      """.trimIndent()

      @Language("PostgreSQL")
      val CREATE_NEW_CUSTOMER = """
         INSERT INTO Customer(id, account, last_name, first_name, contact_name, date_of_birth)
         VALUES (:id, :account, :lastName, :firstName, :contactName, :dateOfBirth)
      """.trimIndent()
   }

   override fun fetchOne(id: Long): Customer? {
      return jdbc.findFirstOrNull(FETCH_CUSTOMER_BY_ID, mapOf("id" to id)) { rs, _ ->
         Customer(
            id = rs.getLong("id"),
            account = rs.getString("account"),
            firstName = rs.getString("firstName"),
            lastName = rs.getString("lastName"),
            contactName = rs.getString("contactName"),
            dateOfBirth = rs.getObject("dateOfBirth", LocalDate::class.java)
         )
      }
   }

   @Transactional
   override fun save(t: Customer): Customer {
      val id = save(mapOf(
         "account"     to t.account,
         "firstName"   to t.firstName,
         "lastName"    to t.lastName,
         "contactName" to t.contactName,
         "dateOfBirth" to t.dateOfBirth
      ), CREATE_NEW_CUSTOMER)

      return t.copy(id = id)
   }

   fun searchForCustomers(customerSearchString: String): Page<Customer> {
      /*val content = jdbcTemplate.query("") { rs: ResultSet ->

      }*/

      return Page(listOf(), 0, 1, false, false)
   }
}
