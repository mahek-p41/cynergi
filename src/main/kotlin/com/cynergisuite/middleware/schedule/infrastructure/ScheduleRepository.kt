package com.cynergisuite.middleware.schedule.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.Repository
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.schedule.Schedule
import com.cynergisuite.middleware.schedule.ScheduleType
import com.cynergisuite.middleware.schedule.argument.ScheduleArgument
import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowCallbackHandler
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ScheduleRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<Schedule> {
   private val logger: Logger = LoggerFactory.getLogger(ScheduleRepository::class.java)

   override fun findOne(id: Long): Schedule? {
      logger.trace("Searching for Schedule with id {}", id)
      val schedule: Schedule? = null

      jdbc.query("""
         SELECT
            sched.id           AS sched_id,
            sched.uu_row_id    AS sched_uu_row_id,
            sched.time_created AS sched_time_created,
            sched.time_updated AS sched_time_updated,
            sched.title        AS sched_title,
            sched.description  AS sched_description,
            sched.schedule     AS sched_schedule,
            sched.command      AS sched_command,
            schedType.id                AS schedType_id,
            schedType.value             AS schedType_value,
            schedType.description       AS schedType_description,
            schedType.localization_code AS schedType_localization_code,
            sa.id                       AS sa_id,
            sa.uu_row_id                AS sa_uu_row_id,
            sa.time_created             AS sa_time_created,
            sa.time_updated             AS sa_time_updated,
            sa.value                    AS sa_value,
            sa.description              AS sa_description
         FROM schedule sched
              JOIN schedule_type_domain schedType ON sched.type_id = schedType.id
              LEFT OUTER JOIN schedule_arg sa ON sched.id = sa.schedule_id
         WHERE sched.id = :id
         """.trimIndent(),
         mapOf("id" to id)
      ) { rs: ResultSet ->
         val localSchedule: Schedule = schedule ?: Schedule(
            id = rs.getLong("sched_id"),
            uuRowId = rs.getUuid("sched_uu_row_id"),
            timeCreated = rs.getOffsetDateTime("sched_time_created"),
            timeUpdated = rs.getOffsetDateTime("sched_time_updated"),
            title = rs.getString("sched_title"),
            description = rs.getString("sched_description"),
            schedule = rs.getString("sched_schedule"),
            command = rs.getString("sched_command"),
            type = ScheduleType(
               id = rs.getLong("schedType_id"),
               value = rs.getString("schedType_value"),
               description = rs.getString("schedType_description"),
               localizationCode = rs.getString("schedType_localization_code")
            )
         )
         if (rs.getString("sa_id") != null) {
            val argument = ScheduleArgument(
               id = rs.getLong("sa_id"),
               uuRowId = rs.getUuid("sa_uu_row_id"),
               timeCreated = rs.getOffsetDateTime("sa_time_created"),
               timeUpdated = rs.getOffsetDateTime("sa_time_updated"),
               value = rs.getString("sa_value"),
               description = rs.getString("sa_description")
            )

            localSchedule.arguments.add(argument)
         }
      }

      logger.trace("Searched for Schedule {} resulted in {}", id, schedule)

      return schedule
   }

   fun fetchAll(pageRequest: PageRequest): RepositoryPage<Schedule> {
      logger.trace("Fetching All")
      var totalElement: Long? = null
      val elements = mutableListOf<Schedule>()
      val currentSchedule: Schedule? = null

      jdbc.query("""
         SELECT
            sched.id           AS sched_id,
            sched.uu_row_id    AS sched_uu_row_id,
            sched.time_created AS sched_time_created,
            sched.time_updated AS sched_time_updated,
            sched.title        AS sched_title,
            sched.description  AS sched_description,
            sched.schedule     AS sched_schedule,
            sched.command      AS sched_command,
            schedType.id       AS schedType_id,
            schedType.value    AS schedType_value,
            schedType.description AS schedType_description,
            schedType.localization_code AS schedType_localization_code,
            (SELECT count(*) FROM schedule) AS total_elements
         FROM schedule sched
              JOIN schedule_type_domain schedType ON sched.type_id = schedType.id
         ORDER BY sched_${pageRequest.camelizeSortBy()} ${pageRequest.sortDirection}
               LIMIT ${pageRequest.size}
               OFFSET ${pageRequest.offset()}
      """.trimIndent()) { rs ->
         val currentScheduleId = rs.getLong("sched_id")

         val localSchedule = if (currentSchedule?.id != currentScheduleId) {
            val created = Schedule(
               id = rs.getLong("sched_id"),
               uuRowId = rs.getUuid("sched_uu_row_id"),
               timeCreated = rs.getOffsetDateTime("sched_time_created"),
               timeUpdated = rs.getOffsetDateTime("sched_time_updated"),
               title = rs.getString("sched_title"),
               description = rs.getString("sched_description"),
               schedule = rs.getString("sched_schedule"),
               command = rs.getString("sched_command"),
               type = ScheduleType(
                  id = rs.getLong("schedType_id"),
                  value = rs.getString("schedType_value"),
                  description = rs.getString("schedType_description"),
                  localizationCode = rs.getString("schedType_localization_code")
               )
            )

            elements.add(created)
            created
         } else {
            currentSchedule
         }

         if (rs.getString("sa_id") != null) {
            val argument = ScheduleArgument(
               id = rs.getLong("sa_id"),
               uuRowId = rs.getUuid("sa_uu_row_id"),
               timeCreated = rs.getOffsetDateTime("sa_time_created"),
               timeUpdated = rs.getOffsetDateTime("sa_time_updated"),
               value = rs.getString("sa_value"),
               description = rs.getString("sa_description")
            )

            localSchedule.arguments.add(argument)
         }

         if (totalElement == null) {
            totalElement = rs.getLong("total_elements")
         }
      }

      return RepositoryPage(
         elements = elements,
         totalElements = totalElement ?: 0
      )

   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM schedule WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Schedule: {} exists resulted in {}", id, exists)

      return exists
   }

   @Transactional
   override fun insert(entity: Schedule): Schedule {
      logger.debug("Inserting Schedule {}", entity)

      val inserted = jdbc.insertReturning(
         """
         INSERT INTO schedule(title, description, schedule, command, type_id)
         VALUES(:title, :description, :schedule, :command, :type_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "title" to entity.title,
            "description" to entity.description,
            "schedule" to entity.schedule,
            "command" to entity.command,
            "type_id" to entity.type.id
         ),
         RowMapper { rs, _ ->
            Schedule(
               id = rs.getLong("id"),
               uuRowId = rs.getUuid("uu_row_id"),
               timeCreated = rs.getOffsetDateTime("time_created"),
               timeUpdated = rs.getOffsetDateTime("time_updated"),
               title = rs.getString("title"),
               description = rs.getString("description"),
               schedule = rs.getString("schedule"),
               command = rs.getString("command"),
               type = entity.type
            )
         }
      )

      return inserted
   }

   @Transactional
   override fun update(entity: Schedule): Schedule {
      logger.debug("Updating Schedule {}", entity)
      return jdbc.updateReturning("""
         UPDATE schedule
         SET
            title = :title,
            description = :description,
            schedule = :schedule,
            command = :command,
            type_id = :type_id
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "title" to entity.title,
            "description" to entity.description,
            "schedule" to entity.schedule,
            "command" to entity.command,
            "type_id" to entity.type.id
         ),
         RowMapper { rs, _ ->
            Schedule(
               id = rs.getLong("id"),
               uuRowId = rs.getUuid("uu_row_id"),
               timeCreated = rs.getOffsetDateTime("time_created"),
               timeUpdated = rs.getOffsetDateTime("time_updated"),
               title = rs.getString("title"),
               description = rs.getString("description"),
               schedule = rs.getString("schedule"),
               command = rs.getString("command"),
               type = entity.type
            )
         }
      )
   }
}
