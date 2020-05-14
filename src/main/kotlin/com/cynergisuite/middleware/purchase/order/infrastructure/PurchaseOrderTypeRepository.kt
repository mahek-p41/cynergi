package com.cynergisuite.middleware.purchase.order.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.middleware.purchase.order.PurchaseOrderType
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseOrderTypeRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(PurchaseOrderTypeRepository::class.java)
   private val rowMapper = PurchaseOrderCodeTypeRowMapper()

   fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM purchase_order_type_domain WHERE UPPER(value) = :value)", mapOf("value" to value.toUpperCase()), Boolean::class.java)!!

      logger.trace("Checking if PurchaseOrderCode: {} exists resulted in {}", value, exists)

      return exists
   }

   fun findOne(id: Long): PurchaseOrderType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM purchase_order_type_domain WHERE id = :id", mapOf("id" to id), rowMapper)

      logger.trace("Searching for PurchaseOrderCodeTypeDomain: {} resulted in {}", id, found)

      return found
   }

   fun findOne(value: String): PurchaseOrderType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM purchase_order_type_domain WHERE UPPER(value) = :value", mapOf("value" to value.toUpperCase()), rowMapper)

      logger.trace("Searching for PurchaseOrderTypeDomain: {} resulted in {}", value, found)

      return found
   }

   fun findAll(): List<PurchaseOrderType> =
      jdbc.query("SELECT * FROM purchase_order_type_domain ORDER BY id", rowMapper)

}

private class PurchaseOrderCodeTypeRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<PurchaseOrderType> {
   override fun mapRow(rs: ResultSet, rowNum: Int): PurchaseOrderType =
      mapRow(rs, columnPrefix)

   fun mapRow(rs: ResultSet, columnPrefix: String): PurchaseOrderType =
      PurchaseOrderType(
         id = rs.getLong("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
