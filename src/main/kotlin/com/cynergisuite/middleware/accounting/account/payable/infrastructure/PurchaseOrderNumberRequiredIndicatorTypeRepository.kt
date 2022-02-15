package com.cynergisuite.middleware.accounting.account.payable.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.middleware.accounting.account.payable.PurchaseOrderNumberRequiredIndicatorType
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet

@Singleton
class PurchaseOrderNumberRequiredIndicatorTypeRepository @Inject constructor(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(PurchaseOrderNumberRequiredIndicatorTypeRepository::class.java)

   @ReadOnly
   fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject(
         "SELECT EXISTS (SELECT id FROM purchase_order_number_required_indicator_type_domain WHERE UPPER(value) = :value)",
         mapOf(
            "value" to value.uppercase()
         ),
         Boolean::class.java
      )

      logger.trace("Checking if PurchaseOrderNumberRequiredIndicatorType: {} exists resulting in {}", value, exists)

      return exists
   }

   @ReadOnly
   fun findOne(id: Long): PurchaseOrderNumberRequiredIndicatorType? {
      val params = mutableMapOf<String, Any?>("id" to id)
      val query = "SELECT * FROM purchase_order_number_required_indicator_type_domain WHERE id = :id"
      logger.trace("Searching for PurchaseOrderNumberRequiredIndicatorTypeDomain {}: \nQuery {}", params, query)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ -> mapRow(rs) }

      logger.trace(
         "Searching for PurchaseOrderNumberRequiredIndicatorTypeDomain {}: \nQuery {} \nResulted in {}",
         params,
         query,
         found
      )

      return found
   }

   @ReadOnly
   fun findOne(value: String): PurchaseOrderNumberRequiredIndicatorType? {
      val params = mutableMapOf<String, Any?>("value" to value.uppercase())
      val query = "SELECT * FROM purchase_order_number_required_indicator_type_domain WHERE UPPER(value) = :value"
      logger.trace("Searching for PurchaseOrderNumberRequiredIndicatorTypeDomain {}: \nQuery {}", params, query)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ -> mapRow(rs) }

      logger.trace(
         "Searching for PurchaseOrderNumberRequiredIndicatorTypeDomain {}: \nQuery {} \nResulted in {}",
         params,
         query,
         found
      )

      return found
   }

   @ReadOnly
   fun findAll(): List<PurchaseOrderNumberRequiredIndicatorType> =
      jdbc.query("SELECT * FROM purchase_order_number_required_indicator_type_domain ORDER BY id") { rs, _ -> mapRow(rs) }

   fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY): PurchaseOrderNumberRequiredIndicatorType =
      PurchaseOrderNumberRequiredIndicatorType(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
