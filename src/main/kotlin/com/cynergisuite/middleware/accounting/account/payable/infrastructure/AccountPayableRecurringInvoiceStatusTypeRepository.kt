package com.cynergisuite.middleware.accounting.account.payable.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableRecurringInvoiceStatusType
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet

@Singleton
class AccountPayableRecurringInvoiceStatusTypeRepository @Inject constructor(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableRecurringInvoiceStatusTypeRepository::class.java)

   @ReadOnly
   fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject(
         "SELECT EXISTS(SELECT id FROM account_payable_recurring_invoice_status_type_domain WHERE UPPER(value) = :value)",
         mapOf(
            "value" to value.uppercase()
         ),
         Boolean::class.java
      )

      logger.trace("Checking if AccountPayableRecurringInvoiceStatusType: {} exists resulted in {}", value, exists)

      return exists
   }

   @ReadOnly
   fun findOne(id: Long): AccountPayableRecurringInvoiceStatusType? {
      val params = mutableMapOf<String, Any?>("id" to id)
      val query = "SELECT * FROM account_payable_recurring_invoice_status_type_domain WHERE id = :id"
      logger.trace("Searching for AccountPayableRecurringInvoiceStatusTypeDomain {}: \nQuery {}", params, query)

      val found = jdbc.findFirstOrNull(query, params, RowMapper { rs, _ -> mapRow(rs) })

      logger.trace(
         "Searching for AccountPayableRecurringInvoiceStatusTypeDomain {}: \nQuery {} \nResulted in {}",
         params,
         query,
         found
      )

      return found
   }

   @ReadOnly
   fun findOne(value: String): AccountPayableRecurringInvoiceStatusType? {
      val params = mutableMapOf<String, Any?>("value" to value.uppercase())
      val query = "SELECT * FROM account_payable_recurring_invoice_status_type_domain WHERE UPPER(value) = :value"
      logger.trace("Searching for AccountPayableRecurringInvoiceStatusTypeDomain {}: \nQuery {}", params, query)

      val found = jdbc.findFirstOrNull(query, params, RowMapper { rs, _ -> mapRow(rs) })

      logger.trace(
         "Searching for AccountPayableRecurringInvoiceStatusTypeDomain {}: \nQuery {} \nResulted in {}",
         params,
         query,
         found
      )

      return found
   }

   @ReadOnly
   fun findAll(): List<AccountPayableRecurringInvoiceStatusType> =
      jdbc.query("SELECT * FROM account_payable_recurring_invoice_status_type_domain ORDER BY id") { rs, _ -> mapRow(rs) }

   fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY): AccountPayableRecurringInvoiceStatusType =
      AccountPayableRecurringInvoiceStatusType(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
