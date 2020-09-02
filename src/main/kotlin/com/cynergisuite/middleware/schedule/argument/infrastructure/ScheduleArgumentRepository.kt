package com.cynergisuite.middleware.schedule.argument.infrastructure

import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class ScheduleArgumentRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {

   @Transactional
   fun insert(parent: ScheduleEntity, entity: ScheduleArgumentEntity): ScheduleArgumentEntity {
      return jdbc.insertReturning(
         """
         INSERT INTO schedule_arg(value, description, schedule_id)
         VALUES (:value, :description, :schedule_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf("value" to entity.value, "description" to entity.description, "schedule_id" to parent.id),
         RowMapper { rs, _ ->
            ScheduleArgumentEntity(
               id = rs.getLong("id"),
               value = rs.getString("value"),
               description = rs.getString("description")
            )
         }
      )
   }

   @Transactional
   fun update(entity: ScheduleArgumentEntity): ScheduleArgumentEntity {
      return jdbc.updateReturning(
         """
         UPDATE schedule_arg
         SET value = :value,
             description = :description
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "value" to entity.value,
            "description" to entity.description
         ),
         RowMapper { rs, _ ->
            ScheduleArgumentEntity(
               id = rs.getLong("id"),
               value = rs.getString("value"),
               description = rs.getString("description")
            )
         }
      )
   }

   @Transactional
   fun deleteNotIn(schedule: ScheduleEntity, arguments: Set<ScheduleArgumentEntity>): Set<ScheduleArgumentEntity> {
      val result = mutableSetOf<ScheduleArgumentEntity>()

      jdbc.query(
         """
         DELETE FROM schedule_arg
         WHERE schedule_id = :schedule_id
               AND id NOT IN(:ids)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "schedule_id" to schedule.id,
            "ids" to arguments.asSequence().map { it.id }.toList()
         )
      ) { rs ->
         result.add(
            ScheduleArgumentEntity(
               id = rs.getLong("id"),
               value = rs.getString("value"),
               description = rs.getString("description")
            )
         )
      }

      return result
   }

   @Transactional
   fun upsert(parent: ScheduleEntity, scheduleArgument: ScheduleArgumentEntity): ScheduleArgumentEntity {
      return if (scheduleArgument.id == null) {
         insert(parent, scheduleArgument)
      } else {
         update(scheduleArgument)
      }
   }

   fun mapRowOrNull(rs: ResultSet, columnPrefix: String = "sa_"): ScheduleArgumentEntity? {
      return if (rs.getString("${columnPrefix}id") != null) {
         ScheduleArgumentEntity(
            id = rs.getLong("${columnPrefix}id"),
            value = rs.getString("${columnPrefix}value"),
            description = rs.getString("${columnPrefix}description")
         )
      } else {
         null
      }
   }
}
