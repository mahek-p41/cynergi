package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.NotificationTypeDomain
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.getOffsetDateTime
import com.hightouchinc.cynergi.middleware.extensions.getUuid
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationTypeDomainRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : TypeDomainRepository<NotificationTypeDomain> {
   private val logger: Logger = LoggerFactory.getLogger(NotificationTypeDomainRepository::class.java)
   private val simpleNotificationDomainTypeRowMapper = NotificationTypeDomainRowMapper()
   private val prefixedNotificationDomainTypeRowMapper = NotificationTypeDomainRowMapper("ntd_")

   override fun findOne(id: Long): NotificationTypeDomain? {
      val found = jdbc.findFirstOrNull("SELECT * FROM notification_type_domain WHERE id = :id", mapOf("id" to id), simpleNotificationDomainTypeRowMapper)

      logger.trace("Searching for NotificationTypeDomain: {} resulted in {}", id, found)

      return found
   }

   override fun findOne(value: String): NotificationTypeDomain? {
      val found = jdbc.findFirstOrNull("SELECT * FROM notification_type_domain WHERE value = :value", mapOf("value" to value), simpleNotificationDomainTypeRowMapper)

      logger.trace("searching for NotificationTypeDomain: {} resulted in {}", value, found)

      return found
   }

   override fun findAll(): List<NotificationTypeDomain> =
      jdbc.query("SELECT * FROM notification_type_domain ORDER BY value", simpleNotificationDomainTypeRowMapper)

   fun mapPrefixedRow(rs: ResultSet, rowNum: Int): NotificationTypeDomain? =
      rs.getString("ntd_id")?.let { prefixedNotificationDomainTypeRowMapper.mapRow(rs = rs, rowNum = rowNum) }
}

private class NotificationTypeDomainRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<NotificationTypeDomain> {
   override fun mapRow(rs: ResultSet, rowNum: Int): NotificationTypeDomain =
      NotificationTypeDomain(
         id = rs.getLong("${columnPrefix}id"),
         uuRowId = rs.getUuid("${columnPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description")
      )
}
