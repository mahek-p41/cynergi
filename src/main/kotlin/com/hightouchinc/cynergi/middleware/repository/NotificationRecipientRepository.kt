package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.NotificationRecipient
import com.hightouchinc.cynergi.middleware.entity.helper.SimpleIdentifiableEntity
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.getOffsetDateTime
import com.hightouchinc.cynergi.middleware.extensions.getUUID
import com.hightouchinc.cynergi.middleware.extensions.insertReturning
import com.hightouchinc.cynergi.middleware.extensions.updateReturning
import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRecipientRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<NotificationRecipient> {
   private val logger: Logger = LoggerFactory.getLogger(NotificationRecipientRepository::class.java)
   private val simpleNotificationRecipientRowMapper = NotificationRecipientRowMapper()

   override fun findOne(id: Long): NotificationRecipient? {
      val found = jdbc.findFirstOrNull("SELECT * FROM notification_recipient WHERE id = :id", mapOf("id" to id), simpleNotificationRecipientRowMapper)

      logger.trace("Searching for NotificationRecipient: {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM notification_recipient WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if NotificationRecipient: {} exists resulted in {}", id, exists)

      return exists
   }

   @Transactional
   override fun insert(entity: NotificationRecipient): NotificationRecipient {
      logger.debug("Inserting notification_recipient {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO notification_recipient(description, recipient, notification_id)
         VALUES (:description, :recipient, :notification_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "description" to entity.description,
            "recipient" to entity.recipient,
            "notification_id" to entity.notification.entityId()
         ),
         simpleNotificationRecipientRowMapper
      )
   }

   @Transactional
   override fun update(entity: NotificationRecipient): NotificationRecipient {
      logger.debug("Updating notification_recipient {}", entity)

      return jdbc.updateReturning("""
         UPDATE notification_recipient
         SET
            description = :description,
            recipient = :recipient,
            notification_id = :notification_id
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "description" to entity.description,
            "recipient" to entity.recipient,
            "notification_id" to entity.notification.entityId()
         ),
         simpleNotificationRecipientRowMapper
      )
   }

   @Transactional
   fun upsert(entity: NotificationRecipient): NotificationRecipient {
      logger.debug("Upserting NotificationRecipient {}", entity)

      return if (entity.id == null) {
         insert(entity = entity)
      } else {
         update(entity = entity)
      }
   }

   @Transactional
   fun deleteAll(recipientsToDelete: Collection<NotificationRecipient>): Int =
      jdbc.update(
         "DELETE from notification_recipient WHERE id IN (:ids)",
         mapOf("ids" to recipientsToDelete.asSequence().filter { it.id != null }.map { it.id }.toSet())
      )
}

private class NotificationRecipientRowMapper : RowMapper<NotificationRecipient> {
   override fun mapRow(rs: ResultSet, rowNum: Int): NotificationRecipient =
      NotificationRecipient(
         id = rs.getLong("id"),
         uuRowId = rs.getUUID("uu_row_id"),
         timeCreated = rs.getOffsetDateTime("time_created"),
         timeUpdated = rs.getOffsetDateTime("time_updated"),
         description = rs.getString("description"),
         recipient = rs.getString("recipient"),
         notification = SimpleIdentifiableEntity(id = rs.getLong("notification_id"))
      )
}
