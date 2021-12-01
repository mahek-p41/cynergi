package com.cynergisuite.middleware.notification.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.middleware.notification.NotificationType
import com.cynergisuite.middleware.notification.NotificationTypeEntity
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class NotificationTypeDomainRepository @Inject constructor(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(NotificationTypeDomainRepository::class.java)
   private val simpleNotificationDomainTypeRowMapper = NotificationTypeDomainRowMapper()
   private val prefixedNotificationDomainTypeRowMapper = NotificationTypeDomainRowMapper("ntd_")

   @ReadOnly fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM notification_type_domain WHERE value = :value)", mapOf("value" to value), Boolean::class.java)

      logger.trace("Checking if Audit: {} exists resulted in {}", value, exists)

      return exists
   }

   @ReadOnly fun findOne(id: Int): NotificationType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM notification_type_domain WHERE id = :id", mapOf("id" to id), simpleNotificationDomainTypeRowMapper)

      logger.trace("Searching for NotificationTypeDomain: {} resulted in {}", id, found)

      return found
   }

   @ReadOnly fun findOne(value: String): NotificationType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM notification_type_domain WHERE value = :value", mapOf("value" to value), simpleNotificationDomainTypeRowMapper)

      logger.trace("searching for NotificationTypeDomain: {} resulted in {}", value, found)

      return found
   }

   @ReadOnly
   fun findAll(): List<NotificationType> =
      jdbc.query("SELECT * FROM notification_type_domain ORDER BY value", rowMapper = simpleNotificationDomainTypeRowMapper)

   fun mapPrefixedRow(rs: ResultSet, ctx: StatementContext): NotificationType? =
      rs.getString("ntd_id")?.let { prefixedNotificationDomainTypeRowMapper.map(rs = rs, ctx = ctx) }
}

private class NotificationTypeDomainRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<NotificationType> {
   override fun map(rs: ResultSet, ctx: StatementContext): NotificationType =
      NotificationTypeEntity(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
