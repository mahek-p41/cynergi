package com.cynergisuite.middleware.accounting.account.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.middleware.accounting.account.AccountType
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountTypeRepository @Inject constructor(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountTypeRepository::class.java)
   private val simpleAccountCodeTypeRowMapper = AccountCodeTypeRowMapper()

   @ReadOnly
   fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject(
         "SELECT EXISTS(SELECT id FROM account_type_domain WHERE UPPER(value) = :value)",
         mapOf(
            "value" to value.uppercase()
         ),
         Boolean::class.java
      )

      logger.trace("Checking if AccountCode: {} exists resulted in {}", value, exists)

      return exists
   }

   @ReadOnly
   fun findOne(id: Long): AccountType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM account_type_domain WHERE id = :id", mapOf("id" to id), simpleAccountCodeTypeRowMapper)

      logger.trace("Searching for AccountCodeTypeTypeDomain: {} resulted in {}", id, found)

      return found
   }

   @ReadOnly
   fun findOne(value: String): AccountType? {
      val found = jdbc.findFirstOrNull(
         "SELECT * FROM account_type_domain WHERE UPPER(value) = :value",
         mapOf(
            "value" to value.uppercase()
         ),
         simpleAccountCodeTypeRowMapper
      )

      logger.trace("Searching for AccountCodeTypeDomain: {} resulted in {}", value, found)

      return found
   }

   @ReadOnly
   fun findAll(): List<AccountType> =
      jdbc.query("SELECT * FROM account_type_domain ORDER BY id", rowMapper = simpleAccountCodeTypeRowMapper)
}

private class AccountCodeTypeRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<AccountType> {
   override fun map(rs: ResultSet, ctx: StatementContext): AccountType =
      mapRow(rs, columnPrefix)

   fun mapRow(rs: ResultSet, columnPrefix: String): AccountType =
      AccountType(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
