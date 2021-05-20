package com.cynergisuite.middleware.accounting.account.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.middleware.accounting.account.NormalAccountBalanceType
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NormalAccountBalanceTypeRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(NormalAccountBalanceTypeRepository::class.java)
   private val simpleNormalAccountBalanceCodeTypeRowMapper = NormalAccountBalanceCodeTypeRowMapper()

   fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM normal_account_balance_type_domain WHERE UPPER(value) = :value)", mapOf("value" to value.toUpperCase()), Boolean::class.java)!!

      logger.trace("Checking if NormalAccountBalanceCode: {} exists resulted in {}", value, exists)

      return exists
   }

   fun doesNotExist(normalAccountBalanceCode: String): Boolean = !exists(normalAccountBalanceCode)

   fun findOne(id: Long): NormalAccountBalanceType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM normal_account_balance_type_domain WHERE id = :id", mapOf("id" to id), simpleNormalAccountBalanceCodeTypeRowMapper)

      logger.trace("Searching for NormalAccountBalanceCodeTypeTypeDomain: {} resulted in {}", id, found)

      return found
   }

   fun findOne(value: String): NormalAccountBalanceType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM normal_account_balance_type_domain WHERE UPPER(value) = :value", mapOf("value" to value.toUpperCase()), simpleNormalAccountBalanceCodeTypeRowMapper)

      logger.trace("Searching for NormalAccountBalanceCodeTypeDomain: {} resulted in {}", value, found)

      return found
   }

   fun findAll(): List<NormalAccountBalanceType> =
      jdbc.query("SELECT * FROM normal_account_balance_type_domain ORDER BY id", simpleNormalAccountBalanceCodeTypeRowMapper)
}

private class NormalAccountBalanceCodeTypeRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<NormalAccountBalanceType> {
   override fun mapRow(rs: ResultSet, rowNum: Int): NormalAccountBalanceType =
      mapRow(rs, columnPrefix)

   fun mapRow(rs: ResultSet, columnPrefix: String): NormalAccountBalanceType =
      NormalAccountBalanceType(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
