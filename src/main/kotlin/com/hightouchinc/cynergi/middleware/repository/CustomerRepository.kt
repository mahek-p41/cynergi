package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.domain.Page
import com.hightouchinc.cynergi.middleware.entity.Customer
import com.hightouchinc.cynergi.middleware.extensions.ofPairs
import com.hightouchinc.cynergi.middleware.repository.spi.RepositoryBase
import io.micronaut.spring.tx.annotation.Transactional
import org.eclipse.collections.api.map.MutableMap
import org.eclipse.collections.impl.factory.Maps
import org.intellij.lang.annotations.Language
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
   jdbc: NamedParameterJdbcTemplate
): RepositoryBase<Customer>(
   tableName = TABLE_NAME,
   jdbc = jdbc,
   entityRowMapper = CUSTOMER_ROW_MAPPER,
   fetchOneQuery = FETCH_CUSTOMER_BY_ID,
   saveOneQuery = CREATE_CUSTOMER,
   updateOneQuery = UPDATE_CUSTOMER
) {
   private companion object {
      val CUSTOMER_COLUMNS = """
         id            AS id,
         uuid          AS uuid,
         date_created  AS dateCreated,
         last_updated  AS lastUpdated,
         account       AS account,
         first_name    AS firstName,
         last_name     AS lastName,
         contact_name  AS contactName,
         date_of_birth AS dateOfBirth
      """.trimIndent()

      const val TABLE_NAME = "Customer"

      @Language("PostgreSQL")
      val FETCH_CUSTOMER_BASE = """
         SELECT
            $CUSTOMER_COLUMNS
         FROM $TABLE_NAME c
      """.trimIndent()

      val CUSTOMER_ROW_MAPPER = RowMapper { rs: ResultSet, _: Int ->
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
      val CREATE_CUSTOMER = """
         INSERT INTO $TABLE_NAME (account, first_name, last_name, contact_name, date_of_birth)
         VALUES (:account, :firstName, :lastName, :contactName, :dateOfBirth)
         RETURNING $CUSTOMER_COLUMNS
      """.trimIndent()

      @Language("PostgreSQL")
      val UPDATE_CUSTOMER = """
         UPDATE $TABLE_NAME
         SET account = :account,
             first_name = :firstName,
             last_name = :lastName,
             contact_name = :contactName,
             date_of_birth = :dateOfBirth
          WHERE id = :id
          RETURNING $CUSTOMER_COLUMNS
      """.trimIndent()

      @Language("PostgreSQL")
      val SEARCH_CUSTOMERS = """
         $FETCH_CUSTOMER_BASE
         WHERE customer_vectors @@ to_tsquery(:customerSearchString)
      """.trimIndent()
   }

   override fun mapOfSaveParameters(entity: Customer): MutableMap<String, Any?> {
      return Maps.mutable.ofPairs(
         "account"     to entity.account,
         "firstName"   to entity.firstName,
         "lastName"    to entity.lastName,
         "contactName" to entity.contactName,
         "dateOfBirth" to entity.dateOfBirth
      )
   }

   @Transactional
   fun save(customers: Collection<Customer>): Collection<Customer> {
      return customers.map {
         save(entity = it)
      }
   }

   fun searchForCustomers(customerSearchTokens: List<String>): Page<Customer> {
      val searchString = customerSearchTokens.asSequence().map { "$it:*" }.joinToString(separator = " & ")
      val content: List<Customer> = jdbc.query(SEARCH_CUSTOMERS, mapOf("customerSearchString" to searchString), CUSTOMER_ROW_MAPPER)

      return Page(content, content.size, 1, true, false) //TODO make paging actually work
   }
}
