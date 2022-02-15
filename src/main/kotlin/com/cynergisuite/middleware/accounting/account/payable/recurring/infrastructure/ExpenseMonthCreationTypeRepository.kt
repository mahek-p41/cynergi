package com.cynergisuite.middleware.accounting.account.payable.recurring.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.middleware.accounting.account.payable.recurring.ExpenseMonthCreationType
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet

@Singleton
class ExpenseMonthCreationTypeRepository @Inject constructor(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(ExpenseMonthCreationTypeRepository::class.java)

   @ReadOnly
   fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject(
         "SELECT EXISTS(SELECT id FROM expense_month_creation_type_domain WHERE UPPER(value) = :value)",
         mapOf(
            "value" to value.uppercase()
         ),
         Boolean::class.java
      )

      logger.trace("Checking if ExpenseMonthCreationType: {} exists resulted in {}", value, exists)

      return exists
   }

   @ReadOnly
   fun findOne(id: Long): ExpenseMonthCreationType? {
      val params = mutableMapOf<String, Any?>("id" to id)
      val query = "SELECT * FROM expense_month_creation_type_domain WHERE id = :id"
      logger.trace("Searching for ExpenseMonthCreationTypeDomain {}: \nQuery {}", params, query)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for ExpenseMonthCreationTypeDomain {}: \nQuery {} \nResulted in {}", params, query, found)

      return found
   }

   @ReadOnly
   fun findOne(value: String): ExpenseMonthCreationType? {
      val params = mutableMapOf<String, Any?>("value" to value.uppercase())
      val query = "SELECT * FROM expense_month_creation_type_domain WHERE UPPER(value) = :value"
      logger.trace("Searching for ExpenseMonthCreationTypeDomain {}: \nQuery {}", params, query)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for ExpenseMonthCreationTypeDomain {}: \nQuery {} \nResulted in {}", params, query, found)

      return found
   }

   @ReadOnly
   fun findAll(): List<ExpenseMonthCreationType> =
      jdbc.query("SELECT * FROM expense_month_creation_type_domain ORDER BY id") { rs, _ -> mapRow(rs) }

   fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY): ExpenseMonthCreationType =
      ExpenseMonthCreationType(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
