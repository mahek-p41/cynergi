package com.hightouchinc.cynergi.middleware.data.access

import com.hightouchinc.cynergi.middleware.data.domain.Page
import com.hightouchinc.cynergi.middleware.data.transfer.Customer
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import io.micronaut.spring.tx.annotation.Transactional
import org.intellij.lang.annotations.Language
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerDataAccessObject @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
): DataAccessObject<Customer> {

   private companion object {
      val CUSTOMER_COLUMNS = """
         id AS id,
         uuid AS uuid,
         date_created AS dateCreated,
         last_updated AS lastUpdated,
         account AS account,
         first_name AS firstName,
         last_name AS lastName,
         contact_name AS contactName,
         date_of_birth AS dateOfBirth
      """.trimIndent()

      @Language("PostgreSQL")
      val FETCH_CUSTOMER_BASE = """
         SELECT
            $CUSTOMER_COLUMNS
         FROM Customer c
      """.trimIndent()

      val CUSTOMER_ROW_MAPPER = { rs: ResultSet, _: Int ->
         Customer(
            id = rs.getLong("id"),
            account = rs.getString("account"),
            firstName = rs.getString("firstName"),
            lastName = rs.getString("lastName"),
            contactName = rs.getString("contactName"),
            dateOfBirth = rs.getObject("dateOfBirth", LocalDate::class.java)
         )
      }

      @Language("PostgreSQL")
      val FETCH_CUSTOMER_BY_ID = """
         $FETCH_CUSTOMER_BASE
         WHERE c.id = :id
      """.trimIndent()

      @Language("PostgreSQL")
      val CREATE_NEW_CUSTOMER = """
         INSERT INTO Customer (account, last_name, first_name, contact_name, date_of_birth)
         VALUES (:account, :lastName, :firstName, :contactName, :dateOfBirth)
         RETURNING $CUSTOMER_COLUMNS
      """.trimIndent()

      @Language("PostgreSQL")
      val SEARCH_CUSTOMERS = """
         $FETCH_CUSTOMER_BASE
         WHERE customer_vectors @@ to_tsquery(:customerSearchString)
      """.trimIndent()
   }

   override fun fetchOne(id: Long): Customer? {
      return jdbc.findFirstOrNull(FETCH_CUSTOMER_BY_ID, mapOf("id" to id), CUSTOMER_ROW_MAPPER)
   }

   @Transactional
   override fun save(t: Customer): Customer {
      return jdbc.queryForObject(CREATE_NEW_CUSTOMER, mapOf(
         "account"     to t.account,
         "firstName"   to t.firstName,
         "lastName"    to t.lastName,
         "contactName" to t.contactName,
         "dateOfBirth" to t.dateOfBirth
      ), CUSTOMER_ROW_MAPPER)!!
   }

   @Transactional // convince method mostly for testing
   fun save(customers: Collection<Customer>): Collection<Customer> {
      return customers.map {
         save(t = it)
      }
   }

   fun searchForCustomers(customerSearchTokens: List<String>): Page<Customer> {
      val searchString = customerSearchTokens.asSequence().map { "$it:*" }.joinToString(separator = " & ")
      val content: List<Customer> = jdbc.query(SEARCH_CUSTOMERS, mapOf("customerSearchString" to searchString), CUSTOMER_ROW_MAPPER)

      return Page(content, content.size, 1, true, false) //TODO make paging actually work
   }
}
