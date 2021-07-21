package com.cynergisuite.middleware.notification.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.middleware.notification.NotificationType
import com.cynergisuite.middleware.notification.NotificationTypeEntity
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
) {

   private val logger: Logger = LoggerFactory.getLogger(NotificationTypeDomainRepository::class.java)
   private val simpleNotificationDomainTypeRowMapper = NotificationTypeDomainRowMapper()
   private val prefixedNotificationDomainTypeRowMapper = NotificationTypeDomainRowMapper("ntd_")

   fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM notification_type_domain WHERE value = :value)", mapOf("value" to value), Boolean::class.java)!!

      logger.trace("Checking if Audit: {} exists resulted in {}", value, exists)

      return exists
   }

   fun findOne(id: Int): NotificationType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM notification_type_domain WHERE id = :id", mapOf("id" to id), simpleNotificationDomainTypeRowMapper)

      logger.trace("Searching for NotificationTypeDomain: {} resulted in {}", id, found)

      return found
   }

   fun findOne(value: String): NotificationType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM notification_type_domain WHERE value = :value", mapOf("value" to value), simpleNotificationDomainTypeRowMapper)

      logger.trace("searching for NotificationTypeDomain: {} resulted in {}", value, found)

      return found
   }

   fun findAll(): List<NotificationType> =
      jdbc.query("SELECT * FROM notification_type_domain ORDER BY value", simpleNotificationDomainTypeRowMapper)

   fun mapPrefixedRow(rs: ResultSet, rowNum: Int): NotificationType? =
      rs.getString("ntd_id")?.let { prefixedNotificationDomainTypeRowMapper.mapRow(rs = rs, rowNum = rowNum) }
}

private class NotificationTypeDomainRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<NotificationType> {
   override fun mapRow(rs: ResultSet, rowNum: Int): NotificationType =
      NotificationTypeEntity(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
