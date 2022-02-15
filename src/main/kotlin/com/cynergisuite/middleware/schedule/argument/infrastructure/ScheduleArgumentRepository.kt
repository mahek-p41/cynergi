package com.cynergisuite.middleware.schedule.argument.infrastructure

import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import io.micronaut.context.annotation.Value
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import java.sql.ResultSet
import javax.transaction.Transactional

@Singleton
class ScheduleArgumentRepository @Inject constructor(
   private val jdbc: Jdbi,
   @Value("\${cynergi.schedule.arg.key}") private val scheduleArgKey: String,
) {

   @Transactional
   fun insert(parent: ScheduleEntity, entity: ScheduleArgumentEntity): ScheduleArgumentEntity {
      val (valueSql, argMap) = this.encryptValueColumnSql(entity)

      argMap.putAll(
         mapOf<String, Any>(
            "value" to entity.value,
            "description" to entity.description,
            "schedule_id" to parent.id!!,
            "encrypted" to entity.encrypted,
         )
      )

      return jdbc.insertReturning(
         """
         INSERT INTO schedule_arg(value, description, schedule_id, encrypted)
         VALUES ($valueSql, :description, :schedule_id, :encrypted)
         RETURNING
            *
         """.trimIndent(),
         argMap
      ) { rs, _ ->
         ScheduleArgumentEntity(
            id = rs.getUuid("id"),
            value = rs.getString("value"),
            description = rs.getString("description"),
            encrypted = rs.getBoolean("encrypted"),
         )
      }
   }

   @Transactional
   fun update(entity: ScheduleArgumentEntity): ScheduleArgumentEntity {
      val (valueSql, argMap) = this.encryptValueColumnSql(entity)

      argMap.putAll(
         mapOf<String, Any>(
            "id" to entity.id!!,
            "value" to entity.value,
            "description" to entity.description,
            "encrypted" to entity.encrypted,
         )
      )

      return jdbc.updateReturning(
         """
         UPDATE schedule_arg
         SET value = $valueSql,
             description = :description,
             encrypted = :encrypted
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         argMap
      ) { rs, _ ->
         ScheduleArgumentEntity(
            id = rs.getUuid("id"),
            value = rs.getString("value"),
            description = rs.getString("description"),
            encrypted = rs.getBoolean("encrypted"),
         )
      }
   }

   @Transactional
   fun deleteNotIn(schedule: ScheduleEntity, arguments: Set<ScheduleArgumentEntity>): Set<ScheduleArgumentEntity> {
      val result = mutableSetOf<ScheduleArgumentEntity>()

      jdbc.query(
         """
         DELETE FROM schedule_arg
         WHERE schedule_id = :schedule_id
               AND id NOT IN(<ids>)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "schedule_id" to schedule.id,
            "ids" to arguments.asSequence().map { it.id }.toList()
         )
      ) { rs, _ ->
         result.add(
            ScheduleArgumentEntity(
               id = rs.getUuid("id"),
               value = rs.getString("value"),
               description = rs.getString("description"),
               encrypted = rs.getBoolean("encrypted"),
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
            id = rs.getUuid("${columnPrefix}id"),
            value = rs.getString("${columnPrefix}value"),
            description = rs.getString("${columnPrefix}description"),
            encrypted = rs.getBoolean("${columnPrefix}encrypted")
         )
      } else {
         null
      }
   }

   private fun encryptValueColumnSql(scheduleArgument: ScheduleArgumentEntity): Pair<String, MutableMap<String, Any>> =
      if (scheduleArgument.encrypted) {
         "encode(pgp_sym_encrypt(:value, :scheduleArgKey), 'hex')" to mutableMapOf("scheduleArgKey" to scheduleArgKey)
      } else {
         ":value" to mutableMapOf()
      }
}
