package com.cynergisuite.middleware.purchase.order.type.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderType
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class PurchaseOrderTypeRepository @Inject constructor(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(PurchaseOrderTypeRepository::class.java)

   @ReadOnly fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject(
         "SELECT EXISTS(SELECT id FROM purchase_order_type_domain WHERE UPPER(value) = :value)",
         mapOf(
            "value" to value.uppercase()
         ),
         Boolean::class.java
      )

      logger.trace("Checking if PurchaseOrderCode: {} exists resulted in {}", value, exists)

      return exists
   }

   @ReadOnly fun findOne(id: Long): PurchaseOrderType? {
      val params = mutableMapOf<String, Any?>("id" to id)
      val query = "SELECT * FROM purchase_order_type_domain WHERE id = :id"
      logger.trace("Searching for PurchaseOrderCodeTypeDomain {}: \nQuery {}", params, query)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for PurchaseOrderCodeTypeDomain {}: \nQuery {} \nResulted in {}", params, query, found)

      return found
   }

   @ReadOnly fun findOne(value: String): PurchaseOrderType? {
      val params = mutableMapOf<String, Any?>("value" to value.uppercase())
      val query = "SELECT * FROM purchase_order_type_domain WHERE UPPER(value) = :value"
      logger.trace("Searching for PurchaseOrderTypeDomain {}: \nQuery {}", params, query)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for PurchaseOrderTypeDomain {}: \nQuery {} \nResulted in {}", params, query, found)

      return found
   }

   @ReadOnly
   fun findAll(): List<PurchaseOrderType> =
      jdbc.query("SELECT * FROM purchase_order_type_domain ORDER BY id") { rs, _ -> mapRow(rs) }

   fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY): PurchaseOrderType =
      PurchaseOrderType(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
