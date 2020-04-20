package com.cynergisuite.middleware.accounting.bank.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.middleware.accounting.bank.BankCurrencyType
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BankCurrencyTypeRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(BankCurrencyTypeRepository::class.java)
   private val simpleBankCurrencyCodeTypeRowMapper = BankCurrencyCodeTypeRowMapper()

   fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM bank_currency_code_type_domain WHERE UPPER(value) = :value)", mapOf("value" to value.toUpperCase()), Boolean::class.java)!!

      logger.trace("Checking if BankCurrencyCode: {} exists resulted in {}", value, exists)

      return exists
   }

   fun doesNotExist(bankCurrencyCode: String): Boolean = !exists(bankCurrencyCode)

   fun findOne(id: Long): BankCurrencyType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM bank_currency_code_type_domain WHERE id = :id", mapOf("id" to id), simpleBankCurrencyCodeTypeRowMapper)

      logger.trace("Searching for BankCurrencyCodeTypeTypeDomain: {} resulted in {}", id, found)

      return found
   }

   fun findOne(value: String): BankCurrencyType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM bank_currency_code_type_domain WHERE UPPER(value) = :value", mapOf("value" to value.toUpperCase()), simpleBankCurrencyCodeTypeRowMapper)

      logger.trace("Searching for BankCurrencyCodeTypeDomain: {} resulted in {}", value, found)

      return found
   }

   fun findAll(): List<BankCurrencyType> =
      jdbc.query("SELECT * FROM bank_currency_code_type_domain ORDER BY id", simpleBankCurrencyCodeTypeRowMapper)

}

private class BankCurrencyCodeTypeRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<BankCurrencyType> {
   override fun mapRow(rs: ResultSet, rowNum: Int): BankCurrencyType =
      mapRow(rs, columnPrefix)

   fun mapRow(rs: ResultSet, columnPrefix: String): BankCurrencyType =
      BankCurrencyType(
         id = rs.getLong("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
