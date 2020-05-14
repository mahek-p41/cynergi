package com.cynergisuite.middleware.purchase.order.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.middleware.purchase.order.UpdatePurchaseOrderCostType
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdatePurchaseOrderCostTypeRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(UpdatePurchaseOrderCostTypeRepository::class.java)
   private val rowMapper = UpdatePurchaseOrderCostTypeRowMapper()

   fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM update_purchase_order_cost_type_domain WHERE UPPER(value) = :value)", mapOf("value" to value.toUpperCase()), Boolean::class.java)!!

      logger.trace("Checking if UpdatePurchaseOrderCostCode: {} exists resulted in {}", value, exists)

      return exists
   }

   fun findOne(id: Long): UpdatePurchaseOrderCostType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM update_purchase_order_cost_type_domain WHERE id = :id", mapOf("id" to id), rowMapper)

      logger.trace("Searching for UpdatePurchaseOrderCostTypeDomain: {} resulted in {}", id, found)

      return found
   }

   fun findOne(value: String): UpdatePurchaseOrderCostType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM update_purchase_order_cost_type_domain WHERE UPPER(value) = :value", mapOf("value" to value.toUpperCase()), rowMapper)

      logger.trace("Searching for UpdatePurchaseOrderCostTypeDomain: {} resulted in {}", value, found)

      return found
   }

   fun findAll(): List<UpdatePurchaseOrderCostType> =
      jdbc.query("SELECT * FROM update_purchase_order_cost_type_domain ORDER BY id", rowMapper)

}

private class UpdatePurchaseOrderCostTypeRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<UpdatePurchaseOrderCostType> {
   override fun mapRow(rs: ResultSet, rowNum: Int): UpdatePurchaseOrderCostType =
      mapRow(rs, columnPrefix)

   fun mapRow(rs: ResultSet, columnPrefix: String): UpdatePurchaseOrderCostType =
      UpdatePurchaseOrderCostType(
         id = rs.getLong("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
