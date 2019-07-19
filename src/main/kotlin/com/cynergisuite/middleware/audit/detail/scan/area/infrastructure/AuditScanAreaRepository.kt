package com.cynergisuite.middleware.audit.detail.scan.area.infrastructure

import com.cynergisuite.domain.infrastructure.TypeDomainRepository
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanArea
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditScanAreaRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : TypeDomainRepository<AuditScanArea> {
   private val logger: Logger = LoggerFactory.getLogger(AuditScanAreaRepository::class.java)
   private val simpleAuditScanAreaRowMapper = AuditScanAreaRowMapper()

   override fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM audit_scan_area_type_domain WHERE value = :value)", mapOf("value" to value), Boolean::class.java)!!

      logger.trace("Checking if Audit: {} exists resulted in {}", value, exists)

      return exists
   }

   override fun findOne(id: Long): AuditScanArea? {
      val found = jdbc.findFirstOrNull("SELECT * FROM audit_scan_area_type_domain WHERE id = :id", mapOf("id" to id), simpleAuditScanAreaRowMapper)

      logger.trace("Searching for AuditScanAreaTypeDomain: {} resulted in {}", id, found)

      return found
   }

   override fun findOne(value: String): AuditScanArea? {
      val found = jdbc.findFirstOrNull("SELECT * FROM audit_scan_area_type_domain WHERE UPPER(value) = :value", mapOf("value" to value.toUpperCase()), simpleAuditScanAreaRowMapper)

      logger.trace("Searching for AuditStatusTypeDomain: {} resulted in {}", value, found)

      return found
   }

   override fun findAll(): List<AuditScanArea> =
      jdbc.query("SELECT * FROM audit_scan_area_type_domain ORDER BY value", simpleAuditScanAreaRowMapper)

   fun mapPrefixedRow(rs: ResultSet, prefix: String = "ad_"): AuditScanArea =
      simpleAuditScanAreaRowMapper.mapRow(rs, prefix)
}

private class AuditScanAreaRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<AuditScanArea> {
   override fun mapRow(rs: ResultSet, rowNum: Int): AuditScanArea =
      mapRow(rs, columnPrefix)

   fun mapRow(rs: ResultSet, columnPrefix: String): AuditScanArea =
      AuditScanArea(
         id = rs.getLong("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
