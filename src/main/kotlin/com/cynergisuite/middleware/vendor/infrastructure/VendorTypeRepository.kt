package com.cynergisuite.middleware.vendor.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.middleware.vendor.VendorType
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet

@Singleton
class VendorTypeRepository @Inject constructor(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(VendorTypeRepository::class.java)

   @ReadOnly
   fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject(
         "SELECT EXISTS(SELECT id FROM vendor_1099_type_domain WHERE UPPER(value) = :value)",
         mapOf(
            "value" to value.uppercase()
         ),
         Boolean::class.java
      )

      logger.trace("Checking if VendorStatusCode: {} exists resulted in {}", value, exists)

      return exists
   }

   fun doesNotExist(vendorStatusCode: String): Boolean = !exists(vendorStatusCode)

   @ReadOnly
   fun findOne(id: Long): VendorType? {
      val params = mutableMapOf<String, Any?>("id" to id)
      val query = "SELECT * FROM vendor_1099_type_domain WHERE id = :id"
      logger.trace("Searching for VendorStatusTypeDomain {}: \nQuery {}", params, query)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for VendorStatusTypeDomain {}: \nQuery {} \nResulted in {}", params, query, found)

      return found
   }

   @ReadOnly
   fun findOne(value: Int): VendorType? {
      val params = mutableMapOf<String, Any?>("value" to value)
      val query = "SELECT * FROM vendor_1099_type_domain WHERE value = :value"
      logger.trace("Searching for VendorStatusTypeDomain {}: \nQuery {}", params, query)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for VendorStatusTypeDomain {}: \nQuery {} \nResulted in {}", params, query, found)

      return found
   }

   @ReadOnly
   fun findAll(): List<VendorType> =
      jdbc.query("SELECT * FROM vendor_1099_type_domain ORDER BY id") { rs, _ -> mapRow(rs) }

   fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY): VendorType =
      VendorType(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getInt("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
