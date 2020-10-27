package com.cynergisuite.middleware.accounting.bank.reconciliation.type.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationType
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BankReconciliationTypeRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(BankReconciliationTypeRepository::class.java)

   fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS (SELECT id FROM bank_reconciliation_type_domain WHERE UPPER(value) = :value", mapOf("value" to value.toUpperCase()), Boolean::class.java)!!

      logger.trace("Checking if BankReconciliationType: {} exists resulting in {}", value, exists)

      return exists
   }

   fun findOne(id: Long): BankReconciliationType? {
      val params = mutableMapOf<String, Any?>("id" to id)
      val query = "SELECT * FROM bank_reconciliation_type_domain WHERE id = :id"
      logger.trace("Searching for BankReconciliationTypeDomain {}: \nQuery {}", params, query)

      val found = jdbc.findFirstOrNull(query, params, RowMapper { rs, _ -> mapRow(rs) })

      logger.trace("Searching for BankReconciliationTypeDomain {}: \nQuery {} \nResulted in {}", params, query, found)

      return found
   }

   fun findOne(value: String): BankReconciliationType? {
      val params = mutableMapOf<String, Any?>("value" to value.toUpperCase())
      val query = "SELECT * FROM bank_reconciliation_type_domain WHERE UPPER(value) = :value"
      logger.trace("Searching for BankReconciliationTypeDomain {}: \nQuery {}", params, query)

      val found = jdbc.findFirstOrNull(query, params, RowMapper { rs, _ -> mapRow(rs) })

      logger.trace("Searching for BankReconciliationTypeDomain {}: \nQuery {} \nResulted in {}", params, query, found)

      return found
   }

   fun findAll(): List<BankReconciliationType> =
      jdbc.query("SELECT * FROM bank_reconciliation_type_domain ORDER BY id") { rs, _ -> mapRow(rs) }

   fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY): BankReconciliationType =
      BankReconciliationType(
         id = rs.getLong("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
