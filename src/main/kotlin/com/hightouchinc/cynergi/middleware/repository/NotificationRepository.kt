package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Notification
import com.hightouchinc.cynergi.middleware.entity.NotificationTypeDomain
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.getLocalDate
import com.hightouchinc.cynergi.middleware.extensions.getOffsetDateTime
import com.hightouchinc.cynergi.middleware.extensions.getUUID
import com.hightouchinc.cynergi.middleware.extensions.insertReturning
import com.hightouchinc.cynergi.middleware.extensions.updateReturning
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val notificationDomainTypeRepository: NotificationDomainTypeRepository
) : Repository<Notification> {
   private val logger: Logger = LoggerFactory.getLogger(NotificationRepository::class.java)
   private val fullNotificationsRowMapper = NotificationsRowMapper("n_", RowMapper { rs, rowNum -> notificationDomainTypeRepository.mapPrefixedRow(rs = rs, rowNum = rowNum)!! })

   override fun findOne(id: Long): Notification? {
      val found = jdbc.findFirstOrNull("""
         SELECT
            n.id AS n_id,
            n.uu_row_id AS n_uu_row_id,
            n.time_created AS n_time_created,
            n.time_updated AS n_time_updated,
            n.company_id AS n_company_id,
            n.expiration_date AS n_expiration_date,
            n.message AS n_message,
            n.sending_employee AS n_sending_employee,
            n.start_date AS n_start_date,
            ndt.id AS ndt_id,
            ndt.uu_row_id AS n_uu_row_id,
            ndt.time_created AS n_time_created,
            ndt.time_updated AS n_time_updated,
            ndt.value AS ndt_value,
            ndt.description AS ndt_description
         FROM notifications n
              JOIN notification_domain_type ndt
                   ON n.notification_domain_type_id = ndt.id
         WHERE id = :id
         """.trimIndent(),
         mapOf("id" to id),
         fullNotificationsRowMapper
      )

      logger.trace("searching for {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM notifications WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if ID: {} exists resulted in {}", id, exists)

      return exists
   }

   override fun insert(entity: Notification): Notification {
      logger.debug("Inserting notification {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO notifications(company_id, expiration_date, message, sending_employee, start_date, notification_domain_type_id)
         VALUES (:company_id, :expiration_date, :message, :sending_employee, :start_date, :notification_domain_type_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to entity.company,
            "expiration_date" to entity.expirationDate,
            "message" to entity.message,
            "sending_employee" to entity.sendingEmployee,
            "start_date" to entity.startDate,
            "notification_domain_type_id" to entity.notificationDomainType.entityId()!!
         ),
         NotificationsRowMapper(notificationDomainTypeRowMapper = RowMapper { _, _ -> entity.notificationDomainType.copy() })
      )
   }

   override fun update(entity: Notification): Notification {
      logger.debug("Updating notification {}", entity)

      return jdbc.updateReturning("""
         UPDATE notifications
         SET
            company_id = :company_id,
            expiration_date = :expiration_date,
            message = :message,
            sending_employee = :sending_employee,
            start_date = :start_date,
            notification_domain_type_id = :notification_domain_type_id
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to entity.company,
            "expiration_date" to entity.expirationDate,
            "message" to entity.message,
            "sending_employee" to entity.sendingEmployee,
            "start_date" to entity.startDate,
            "notification_domain_type_id" to entity.notificationDomainType.entityId()!!
         ),
         NotificationsRowMapper(notificationDomainTypeRowMapper = RowMapper { _, _ -> entity.notificationDomainType.copy() })
      )
   }
}

private class NotificationsRowMapper(
   private val rowPrefix: String = EMPTY,
   private val notificationDomainTypeRowMapper: RowMapper<NotificationTypeDomain>
) : RowMapper<Notification> {
   override fun mapRow(rs: ResultSet, rowNum: Int): Notification =
      Notification(
         id = rs.getLong("${rowPrefix}id"),
         uuRowId = rs.getUUID("uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${rowPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${rowPrefix}time_updated"),
         company = rs.getString("${rowPrefix}company"),
         expirationDate = rs.getLocalDate("${rowPrefix}expiration_date"),
         message = rs.getString("${rowPrefix}message"),
         sendingEmployee = rs.getString("${rowPrefix}sending_employee"),
         startDate = rs.getLocalDate("${rowPrefix}start_date"),
         notificationDomainType = notificationDomainTypeRowMapper.mapRow(rs, rowNum)!!
      )
}
