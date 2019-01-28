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
import org.intellij.lang.annotations.Language
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
   private val notificationTypeDomainRepository: NotificationTypeDomainRepository
) : Repository<Notification> {
   private val logger: Logger = LoggerFactory.getLogger(NotificationRepository::class.java)
   private val fullNotificationsRowMapper = NotificationsRowMapper("n_", RowMapper { rs, rowNum -> notificationTypeDomainRepository.mapPrefixedRow(rs = rs, rowNum = rowNum)!! })
   @Language("PostgreSQL") private val baseFindQuery = """
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
            ntd.id AS ntd_id,
            ntd.uu_row_id AS ntd_uu_row_id,
            ntd.time_created AS ntd_time_created,
            ntd.time_updated AS ntd_time_updated,
            ntd.value AS ntd_value,
            ntd.description AS ntd_description
       FROM notification n
            JOIN notification_type_domain ntd
                 ON n.notification_type_id = ntd.id
   """.trimIndent()

   override fun findOne(id: Long): Notification? {
      val found = jdbc.findFirstOrNull("""
         $baseFindQuery
         WHERE n.id = :id
         """.trimIndent(),
         mapOf("id" to id),
         fullNotificationsRowMapper
      )

      logger.trace("searching for {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM notification WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if ID: {} exists resulted in {}", id, exists)

      return exists
   }

   override fun insert(entity: Notification): Notification {
      logger.debug("Inserting notification {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO notification(company_id, expiration_date, message, sending_employee, start_date, notification_type_id)
         VALUES (:company_id, :expiration_date, :message, :sending_employee, :start_date, :notification_type_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to entity.company,
            "start_date" to entity.startDate,
            "expiration_date" to entity.expirationDate,
            "message" to entity.message,
            "sending_employee" to entity.sendingEmployee,
            "expiration_date" to entity.expirationDate,
            "notification_type_id" to entity.notificationDomainType.entityId()!!
         ),
         NotificationsRowMapper(notificationDomainTypeRowMapper = RowMapper { _, _ -> entity.notificationDomainType.copy() })
      )
   }

   override fun update(entity: Notification): Notification {
      logger.debug("Updating notification {}", entity)

      return jdbc.updateReturning("""
         UPDATE notification
         SET
            company_id = :company_id,
            start_date = :start_date,
            expiration_date = :expiration_date,
            message = :message,
            sending_employee = :sending_employee,
            notification_type_id = :notification_type_id
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to entity.company,
            "start_date" to entity.startDate,
            "expiration_date" to entity.expirationDate,
            "message" to entity.message,
            "sending_employee" to entity.sendingEmployee,
            "notification_type_id" to entity.notificationDomainType.entityId()!!
         ),
         NotificationsRowMapper(notificationDomainTypeRowMapper = RowMapper { _, _ -> entity.notificationDomainType.copy() })
      )
   }

   fun findAllByCompany(companyId: String, type: String): List<Notification> =
      jdbc.query("""
         $baseFindQuery
         WHERE n.company_id = :company_id
               AND ntd.value = :notification_type
               AND n.start_date <= current_date
               AND n.expiration_date >= current_date
         """.trimIndent(),
         mapOf(
            "company_id" to companyId,
            "notification_type" to type
         ),
         fullNotificationsRowMapper
      )
}

private class NotificationsRowMapper(
   private val rowPrefix: String = EMPTY,
   private val notificationDomainTypeRowMapper: RowMapper<NotificationTypeDomain>
) : RowMapper<Notification> {
   override fun mapRow(rs: ResultSet, rowNum: Int): Notification =
      Notification(
         id = rs.getLong("${rowPrefix}id"),
         uuRowId = rs.getUUID("${rowPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${rowPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${rowPrefix}time_updated"),
         company = rs.getString("${rowPrefix}company_id"),
         expirationDate = rs.getLocalDate("${rowPrefix}expiration_date"),
         message = rs.getString("${rowPrefix}message"),
         sendingEmployee = rs.getString("${rowPrefix}sending_employee"),
         startDate = rs.getLocalDate("${rowPrefix}start_date"),
         notificationDomainType = notificationDomainTypeRowMapper.mapRow(rs, rowNum)!!
      )
}
