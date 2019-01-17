package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.ChecklistReference
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.insertReturning
import com.hightouchinc.cynergi.middleware.extensions.ofPairs
import com.hightouchinc.cynergi.middleware.extensions.updateReturning
import org.eclipse.collections.impl.factory.Maps
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
class ChecklistReferenceRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<ChecklistReference> {
   private val logger: Logger = LoggerFactory.getLogger(ChecklistReferenceRepository::class.java)
   private val simpleChecklistReferenceRowMapper = ChecklistReferenceRowMapper()

   override fun findOne(id: Long): ChecklistReference? {
      val found = jdbc.findFirstOrNull("SELECT * FROM checklist_reference ca WHERE ca.id = :id", Maps.mutable.ofPairs("id" to id), simpleChecklistReferenceRowMapper)

      logger.trace("searching for {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM checklist_reference WHERE id = :id)", Maps.mutable.ofPairs("id" to id), Boolean::class.java)!!

      logger.trace("Checking if ID: {} exists resulted in {}", id, exists)

      return exists
   }

   override fun insert(entity: ChecklistReference): ChecklistReference {
      logger.trace("Inserting {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO checklist_reference(address, has_home_phone, known, leave_message, rating, relationship, reliable, time_frame, verify_phone, checklist_id)
         VALUES (:address, :has_home_phone, :known, :leave_message, :rating, :relationship, :reliable, :time_frame, :verify_phone, :checklist_id)
         RETURNING
            *
         """.trimIndent(),
         Maps.mutable.ofPairs(
            "address" to entity.address,
            "has_home_phone" to entity.hasHomePhone,
            "known" to entity.known,
            "leave_message" to entity.leaveMessage,
            "rating" to entity.rating,
            "relationship" to entity.relationship,
            "reliable" to entity.reliable,
            "time_frame" to entity.timeFrame,
            "verify_phone" to entity.verifyPhone,
            "checklist_id" to entity.checklistId
         ),
         simpleChecklistReferenceRowMapper
      )
   }

   override fun update(entity: ChecklistReference): ChecklistReference {
      logger.trace("Updating {}", entity)

      return jdbc.updateReturning("""
         UPDATE checklist_reference
         SET
            address = :address,
            has_home_phone = :has_home_phone,
            known = :known,
            leave_message = :leave_message,
            rating = :rating,
            relationship = :relationship,
            reliable = :reliable,
            time_frame = :time_frame,
            verify_phone = :verify_phone,
            checklist_id = :checklist_id
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         Maps.mutable.ofPairs(
            "id" to entity.id,
            "address" to entity.address,
            "has_home_phone" to entity.hasHomePhone,
            "known" to entity.known,
            "leave_message" to entity.leaveMessage,
            "rating" to entity.rating,
            "relationship" to entity.relationship,
            "reliable" to entity.reliable,
            "time_frame" to entity.timeFrame,
            "verify_phone" to entity.verifyPhone,
            "checklist_id" to entity.checklistId
         ),
         simpleChecklistReferenceRowMapper
      )
   }
}

private class ChecklistReferenceRowMapper : RowMapper<ChecklistReference> {
   override fun mapRow(rs: ResultSet, rowNum: Int): ChecklistReference =
      ChecklistReference(
         id = rs.getLong("id"),
         uuRowId = rs.getObject("uu_row_id", UUID::class.java),
         timeCreated = rs.getObject("time_created", OffsetDateTime::class.java),
         timeUpdated = rs.getObject("time_updated", OffsetDateTime::class.java),
         address = rs.getBoolean("address"),
         hasHomePhone = rs.getBoolean("has_home_address"),
         known = rs.getInt("known"),
         leaveMessage = rs.getBoolean("leave_message"),
         rating = rs.getString("rating"),
         relationship = rs.getBoolean("relationship"),
         reliable = rs.getBoolean("reliable"),
         timeFrame = rs.getInt("time_frame"),
         verifyPhone = rs.getBoolean("verify_phone"),
         checklistId = rs.getLong("checklist_id")
      )
}
