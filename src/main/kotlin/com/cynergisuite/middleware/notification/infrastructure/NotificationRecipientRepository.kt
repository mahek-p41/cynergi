package com.cynergisuite.middleware.notification.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.notification.NotificationRecipient
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils
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
) {
   private val logger: Logger = LoggerFactory.getLogger(NotificationRecipientRepository::class.java)
   private val simpleNotificationRecipientRowMapper = NotificationRecipientRowMapper()
   private val prefixedNotificationRecipientRowMapper = NotificationRecipientRowMapper(columnPrefix = "nr_")

   fun findOne(id: Long): NotificationRecipient? {
      val found = jdbc.findFirstOrNull("SELECT * FROM notification_recipient WHERE id = :id", mapOf("id" to id), simpleNotificationRecipientRowMapper)

      logger.trace("Searching for NotificationRecipient: {} resulted in {}", id, found)

      return found
   }

   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM notification_recipient WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if NotificationRecipient: {} exists resulted in {}", id, exists)

      return exists
   }

   @Transactional
   fun insert(entity: NotificationRecipient): NotificationRecipient {
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
            "notification_id" to entity.notification.myId()
         ),
         simpleNotificationRecipientRowMapper
      )
   }

   @Transactional
   fun update(entity: NotificationRecipient): NotificationRecipient {
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
            "notification_id" to entity.notification.myId()
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
         "DELETE FROM notification_recipient WHERE id IN (:ids)",
         mapOf("ids" to recipientsToDelete.asSequence().filter { it.id != null }.map { it.id }.toSet())
      )

   @Transactional
   fun deleteForParent(parentId: Long): Int =
      jdbc.update("DELETE FROM notification_recipient WHERE notification_id = :parentId", mapOf("parentId" to parentId))

   fun mapRowPrefixedRow(rs: ResultSet, row: Int = 0): NotificationRecipient? =
      rs.getString("nr_id")?.let { prefixedNotificationRecipientRowMapper.mapRow(rs, row) }
}

private class NotificationRecipientRowMapper(
   private val columnPrefix: String = StringUtils.EMPTY
) : RowMapper<NotificationRecipient> {
   override fun mapRow(rs: ResultSet, rowNum: Int): NotificationRecipient =
      NotificationRecipient(
         id = rs.getLong("${columnPrefix}id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         description = rs.getString("${columnPrefix}description"),
         recipient = rs.getString("${columnPrefix}recipient"),
         notification = SimpleIdentifiableEntity(id = rs.getLong("${columnPrefix}notification_id"))
      )
}
