package com.cynergisuite.middleware.accounting.account.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.middleware.accounting.account.AccountType
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountTypeRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountTypeRepository::class.java)
   private val simpleAccountCodeTypeRowMapper = AccountCodeTypeRowMapper()

   fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM account_type_domain WHERE UPPER(value) = :value)", mapOf("value" to value.toUpperCase()), Boolean::class.java)!!

      logger.trace("Checking if AccountCode: {} exists resulted in {}", value, exists)

      return exists
   }

   fun findOne(id: Long): AccountType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM account_type_domain WHERE id = :id", mapOf("id" to id), simpleAccountCodeTypeRowMapper)

      logger.trace("Searching for AccountCodeTypeTypeDomain: {} resulted in {}", id, found)

      return found
   }

   fun findOne(value: String): AccountType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM account_type_domain WHERE UPPER(value) = :value", mapOf("value" to value.toUpperCase()), simpleAccountCodeTypeRowMapper)

      logger.trace("Searching for AccountCodeTypeDomain: {} resulted in {}", value, found)

      return found
   }

   fun findAll(): List<AccountType> =
      jdbc.query("SELECT * FROM account_type_domain ORDER BY id", simpleAccountCodeTypeRowMapper)
}

private class AccountCodeTypeRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<AccountType> {
   override fun mapRow(rs: ResultSet, rowNum: Int): AccountType =
      mapRow(rs, columnPrefix)

   fun mapRow(rs: ResultSet, columnPrefix: String): AccountType =
      AccountType(
         id = rs.getLong("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
