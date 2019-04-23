package com.cynergisuite.middleware.repository

import com.cynergisuite.middleware.entity.Notification
import com.cynergisuite.middleware.entity.NotificationRecipient
import com.cynergisuite.middleware.entity.NotificationTypeDomain
import com.cynergisuite.middleware.extensions.findAllWithCrossJoin
import com.cynergisuite.middleware.extensions.findFirstOrNullWithCrossJoin
import com.cynergisuite.middleware.extensions.getLocalDate
import com.cynergisuite.middleware.extensions.getOffsetDateTime
import com.cynergisuite.middleware.extensions.getUuid
import com.cynergisuite.middleware.extensions.insertReturning
import com.cynergisuite.middleware.extensions.updateReturning
import io.micronaut.spring.tx.annotation.Transactional
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
   private val notificationTypeDomainRepository: NotificationTypeDomainRepository,
   private val notificationRecipientRepository: NotificationRecipientRepository
) : Repository<Notification> {
   private val logger: Logger = LoggerFactory.getLogger(NotificationRepository::class.java)
   private val fullNotificationRowMapper = NotificationRowMapper("n_", RowMapper { rs, rowNum -> notificationTypeDomainRepository.mapPrefixedRow(rs = rs, rowNum = rowNum)!! })

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
         ntd.description AS ntd_description,
         nr.id AS nr_id,
         nr.uu_row_id AS nr_uu_row_id,
         nr.time_created AS nr_time_created,
         nr.time_updated AS nr_time_updated,
         nr.description AS nr_description,
         nr.recipient AS nr_recipient,
         nr.notification_id AS nr_notification_id
      FROM notification n
         JOIN notification_type_domain ntd
           ON n.notification_type_id = ntd.id
         LEFT OUTER JOIN notification_recipient nr
           ON n.id = nr.notification_id
   """.trimIndent()

   override fun findOne(id: Long): Notification? {
      val found: Notification? = jdbc.findFirstOrNullWithCrossJoin("$baseFindQuery\nWHERE n.id = :id", mapOf("id" to id), fullNotificationRowMapper) { notification, rs ->
         notificationRecipientRepository.mapRowPrefixedRow(rs = rs)?.also { notification.recipients.add(it) }
      }

      logger.trace("Searching for Notification: {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM notification WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Notification: {} exists resulted in {}", id, exists)

      return exists
   }

   fun findAllByCompany(companyId: String, type: String): List<Notification> =
      jdbc.findAllWithCrossJoin("""
         $baseFindQuery
         WHERE n.company_id = :company_id
               AND ntd.value = :notification_type
               AND current_date BETWEEN n.start_date AND n.expiration_date
         ORDER BY n_id ASC
         """.trimIndent(),
         mapOf(
            "company_id" to companyId,
            "notification_type" to type
         ),
         "n_id",
         fullNotificationRowMapper
      ) { notification, rs ->
         notificationRecipientRepository.mapRowPrefixedRow(rs = rs)?.also {
            notification.recipients.add(it)
         }
      }

   fun findAllByRecipient(companyId: String, recipientId: String, type: String): List<Notification> =
      jdbc.findAllWithCrossJoin("""
         $baseFindQuery
         WHERE n.company_id = :company_id
               AND ntd.value = :notification_type
               AND nr.recipient = :recipient_id
         """.trimIndent(),
         mapOf(
            "company_id" to companyId,
            "notification_type" to type,
            "recipient_id" to recipientId
         ),
         "n_id",
         fullNotificationRowMapper
      ) { notification, rs ->
         notificationRecipientRepository.mapRowPrefixedRow(rs = rs)?.also {
            notification.recipients.add(it)
         }
      }

   fun findAllTypes(): List<NotificationTypeDomain> =
      notificationTypeDomainRepository.findAll()

   fun findAllBySendingEmployee(companyId: String, sendingEmployee: String): List<Notification> =
      jdbc.findAllWithCrossJoin("""
         $baseFindQuery
         WHERE n.company_id = :company_id
               AND n.sending_employee = :sending_employee
         """.trimIndent(),
         mapOf(
            "company_id" to companyId,
            "sending_employee" to sendingEmployee
         ),
         "n_id",
         fullNotificationRowMapper
      ) { notification, rs ->
         notificationRecipientRepository.mapRowPrefixedRow(rs = rs)?.also {
            notification.recipients.add(it)
         }
      }

   @Transactional
   override fun insert(entity: Notification): Notification {
      logger.debug("Inserting notification {}", entity)

      val inserted = jdbc.insertReturning("""
         INSERT INTO notification(company_id, start_date, expiration_date, message, sending_employee, notification_type_id)
         VALUES (:company_id, :start_date, :expiration_date, :message, :sending_employee, :notification_type_id)
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
         NotificationRowMapper(notificationDomainTypeRowMapper = RowMapper { _, _ -> entity.notificationDomainType.copy() }) // making a copy here to guard against the possibility of the instance of notificationDomainType changing outside of this code
      )

      entity.recipients.asSequence()
         .map { notificationRecipientRepository.insert(it.copy(notification = inserted)) }
         .forEach { inserted.recipients.add(it) }

      return inserted
   }

   @Transactional
   override fun update(entity: Notification): Notification {
      logger.debug("Updating notification {}", entity)

      val existing = findOne(id = entity.id!!)!!

      val updated = jdbc.updateReturning("""
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
         NotificationRowMapper(notificationDomainTypeRowMapper = RowMapper { _, _ -> entity.notificationDomainType.copy() }) // making a copy here to guard against the possibility of the instance of notificationDomainType changing outside of this code
      )

      val recipients = doRecipientUpdates(entity, existing)

      doRecipientDeletes(existing, recipients)

      if (recipients.isNotEmpty()) {
         updated.recipients.addAll(recipients)
      }

      return updated
   }

   @Transactional
   fun delete(id: Long): Int {
      logger.trace("notification deletion requested for notification with id {}", id)

      notificationRecipientRepository.deleteForParent(parentId = id)

      return jdbc.update("DELETE FROM notification WHERE id = :id", mapOf("id" to id))
   }

   private fun doRecipientDeletes(existing: Notification, recipients: MutableSet<NotificationRecipient>) {
      val recipientsToDelete = existing.recipients.asSequence().filter { !recipients.contains(it) }.toList()

      if (recipientsToDelete.isNotEmpty()) {
         notificationRecipientRepository.deleteAll(recipientsToDelete)
      }
   }

   private fun doRecipientUpdates(entity: Notification, existing: Notification) =
      entity.recipients.asSequence()
         .map { n -> existing.recipients.firstOrNull { e -> e.recipient == n.recipient && e.notification.entityId() == n.notification.entityId() } ?: n } // find existing recipient based on the recipient and the parent notification ID otherwise return the new recipient
         .map { notificationRecipientRepository.upsert(entity = it) }
         .toMutableSet()
}

private class NotificationRowMapper(
   private val columnPrefix: String = EMPTY,
   private val notificationDomainTypeRowMapper: RowMapper<NotificationTypeDomain>
) : RowMapper<Notification> {
   override fun mapRow(rs: ResultSet, rowNum: Int): Notification =
      Notification(
         id = rs.getLong("${columnPrefix}id"),
         uuRowId = rs.getUuid("${columnPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         company = rs.getString("${columnPrefix}company_id"),
         expirationDate = rs.getLocalDate("${columnPrefix}expiration_date"),
         message = rs.getString("${columnPrefix}message"),
         sendingEmployee = rs.getString("${columnPrefix}sending_employee"),
         startDate = rs.getLocalDate("${columnPrefix}start_date"),
         notificationDomainType = notificationDomainTypeRowMapper.mapRow(rs, rowNum)!!
      )
}
