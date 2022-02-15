package com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringType
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class GeneralLedgerRecurringTypeRepository @Inject constructor(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerRecurringTypeRepository::class.java)

   @ReadOnly
   fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject(
         "SELECT EXISTS(SELECT id FROM general_ledger_recurring_type_domain WHERE UPPER(value) = :value)",
         mapOf(
            "value" to value.uppercase()
         ),
         Boolean::class.java
      )

      logger.trace("Checking if GeneralLedgerRecurringType: {} exists resulted in {}", value, exists)

      return exists
   }

   @ReadOnly
   fun findOne(id: Long): GeneralLedgerRecurringType? {
      val params = mutableMapOf<String, Any?>("id" to id)
      val query = "SELECT * FROM general_ledger_recurring_type_domain WHERE id = :id"
      logger.trace("Searching for GeneralLedgerRecurringTypeDomain {}: \nQuery {}", params, query)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for GeneralLedgerRecurringTypeDomain {}: \nQuery {} \nResulted in {}", params, query, found)

      return found
   }

   @ReadOnly
   fun findOne(value: String): GeneralLedgerRecurringType? {
      val params = mutableMapOf<String, Any?>("value" to value.uppercase())
      val query = "SELECT * FROM general_ledger_recurring_type_domain WHERE UPPER(value) = :value"
      logger.trace("Searching for GeneralLedgerRecurringTypeDomain {}: \nQuery {}", params, query)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for GeneralLedgerRecurringTypeDomain {}: \nQuery {} \nResulted in {}", params, query, found)

      return found
   }

   @ReadOnly
   fun findAll(): List<GeneralLedgerRecurringType> =
      jdbc.query("SELECT * FROM general_ledger_recurring_type_domain ORDER BY id") { rs, _ -> mapRow(rs) }

   fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY): GeneralLedgerRecurringType =
      GeneralLedgerRecurringType(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
