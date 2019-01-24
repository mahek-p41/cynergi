package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Notification
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.insertReturning
import com.hightouchinc.cynergi.middleware.extensions.updateReturning
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<Notification> {
   private val logger: Logger = LoggerFactory.getLogger(NotificationsRepository::class.java)
   private val simpleNotificationsRowMapper = NotificationsRowMapper()

   override fun findOne(id: Long): Notification? {
      val found = jdbc.findFirstOrNull("SELECT * FROM notifications WHERE id = :id", mapOf("id" to id), simpleNotificationsRowMapper)

      logger.trace("searching for {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM notifications WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if ID: {} exists resulted in {}", id, exists)

      return exists
   }

   override fun insert(entity: Notification): Notification {
      logger.trace("Inserting {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO notifications()
         VALUES ()
         RETURNING
            *
         """.trimIndent(),
         mapOf<String, Any>(),
         simpleNotificationsRowMapper
      )
   }

   override fun update(entity: Notification): Notification {
      logger.trace("Updating {}", entity)

      return jdbc.updateReturning("""
         UPDATE notifications
         SET

         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id
         ),
         simpleNotificationsRowMapper
      )
   }
}

private class NotificationsRowMapper : RowMapper<Notification> {
   override fun mapRow(rs: ResultSet, rowNum: Int): Notification =
      Notification(
         id = rs.getLong("id"),
         uuRowId = rs.getObject("uu_row_id", UUID::class.java),
         timeCreated = rs.getObject("time_created", OffsetDateTime::class.java),
         timeUpdated = rs.getObject("time_updated", OffsetDateTime::class.java)
      )
}
