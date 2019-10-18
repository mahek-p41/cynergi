package com.cynergisuite.middleware.schedule.argument.infrastructure

import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.middleware.schedule.Schedule
import com.cynergisuite.middleware.schedule.argument.ScheduleArgument
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleArgumentRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {

   @Transactional
   fun insert(parent: Schedule, entity: ScheduleArgument): ScheduleArgument {
      return jdbc.insertReturning("""
         INSERT INTO schedule_arg(value, description, schedule_id)
         VALUES (:value, :description, :schedule_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf("value" to entity.value, "description" to entity.description, "schedule_id" to parent.id),
         RowMapper { rs, _ ->
            ScheduleArgument(
               id = rs.getLong("sa_id"),
               uuRowId = rs.getUuid("sa_uu_row_id"),
               timeCreated = rs.getOffsetDateTime("sa_time_created"),
               timeUpdated = rs.getOffsetDateTime("sa_time_updated"),
               value = rs.getString("sa_value"),
               description = rs.getString("sa_description")
            )
         }
      )
   }

   @Transactional
   fun update(entity: ScheduleArgument): ScheduleArgument {
      TODO("Implement Me")
   }
}
