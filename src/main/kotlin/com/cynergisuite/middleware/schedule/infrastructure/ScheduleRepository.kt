package com.cynergisuite.middleware.schedule.infrastructure

import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.argument.infrastructure.ScheduleArgumentRepository
import com.cynergisuite.middleware.schedule.command.ScheduleCommandType
import com.cynergisuite.middleware.schedule.command.infrastructure.ScheduleCommandTypeRepository
import com.cynergisuite.middleware.schedule.type.ScheduleType
import com.cynergisuite.middleware.schedule.type.infrastructure.ScheduleTypeRepository
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ScheduleRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val scheduleArgumentRepository: ScheduleArgumentRepository,
   private val scheduleCommandTypeRepository: ScheduleCommandTypeRepository,
   private val scheduleTypeRepository: ScheduleTypeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(ScheduleRepository::class.java)

   fun findOne(id: Long): ScheduleEntity? {
      logger.trace("Searching for Schedule with id {}", id)

      var found: ScheduleEntity? = null

      jdbc.query("""
         SELECT
            sched.id                    AS sched_id,
            sched.uu_row_id             AS sched_uu_row_id,
            sched.time_created          AS sched_time_created,
            sched.time_updated          AS sched_time_updated,
            sched.title                 AS sched_title,
            sched.description           AS sched_description,
            sched.schedule              AS sched_schedule,
            sched.enabled               AS sched_enabled,
            schedType.id                AS schedType_id,
            schedType.value             AS schedType_value,
            schedType.description       AS schedType_description,
            schedType.localization_code AS schedType_localization_code,
            sctd.id                     AS sctd_id,
            sctd.value                  AS sctd_value,
            sctd.description            AS sctd_description,
            sctd.localization_code      AS sctd_localization_code,
            sa.id                       AS sa_id,
            sa.uu_row_id                AS sa_uu_row_id,
            sa.time_created             AS sa_time_created,
            sa.time_updated             AS sa_time_updated,
            sa.value                    AS sa_value,
            sa.description              AS sa_description
         FROM schedule sched
              JOIN schedule_type_domain schedType ON sched.type_id = schedType.id
              JOIN schedule_command_type_domain sctd ON sched.command_id = sctd.id
              LEFT OUTER JOIN schedule_arg sa ON sched.id = sa.schedule_id
         WHERE sched.id = :id
         """.trimIndent(),
         mapOf("id" to id)
      ) { rs: ResultSet ->
         val localSchedule = found ?: mapRow(
            rs = rs,
            scheduleTypeProvider = { scheduleTypeRepository.mapRow(rs, "schedType_") },
            scheduleCommandProvider = { scheduleCommandTypeRepository.mapRow(rs, "sctd_") }
         )

         scheduleArgumentRepository.mapRowOrNull(rs, "sa_")?.also { localSchedule.arguments.add(it) }

         found = localSchedule
      }

      logger.trace("Searched for Schedule {} resulted in {}", id, found)

      return found
   }

   fun findAll(pageRequest: SchedulePageRequest, type: ScheduleType? = null): RepositoryPage<ScheduleEntity, SchedulePageRequest> {
      logger.trace("Fetching All schedules {}", pageRequest)

      val command = pageRequest.command
      var totalElement: Long? = null
      val elements = mutableListOf<ScheduleEntity>()
      var currentSchedule: ScheduleEntity? = null
      var where = "WHERE"
      var and = EMPTY
      var whereClause = StringBuilder()
      val params = mutableMapOf<String, Any>()

      if (command != null) {
         whereClause.append(" $where sctd.value = :sctd_value")
         params["sctd_value"] = command
         where = EMPTY
         and = " AND "
      }

      if (type != null) {
         whereClause.append("$where $and schedType.value = :schedType_value")
         params["schedType_value"] = type.value
      }

      val sql = """
         WITH schedules AS (
            SELECT
               sched.id                                                        AS sched_id,
               sched.uu_row_id                                                 AS sched_uu_row_id,
               sched.time_created                                              AS sched_time_created,
               sched.time_updated                                              AS sched_time_updated,
               sched.title                                                     AS sched_title,
               sched.description                                               AS sched_description,
               sched.schedule                                                  AS sched_schedule,
               sched.enabled                                                   AS sched_enabled,
               schedType.id                                                    AS schedType_id,
               schedType.value                                                 AS schedType_value,
               schedType.description                                           AS schedType_description,
               schedType.localization_code                                     AS schedType_localization_code,
               sctd.id                                                         AS sctd_id,
               sctd.value                                                      AS sctd_value,
               sctd.description                                                AS sctd_description,
               sctd.localization_code                                          AS sctd_localization_code,
               (SELECT count(id) FROM schedule $whereClause) AS total_elements
            FROM schedule sched
               JOIN schedule_type_domain schedType ON sched.type_id = schedType.id
               JOIN schedule_command_type_domain sctd ON sched.command_id = sctd.id
            $whereClause
            ORDER BY sched_${pageRequest.snakeSortBy()} ${pageRequest.sortDirection}
               LIMIT ${pageRequest.size}
               OFFSET ${pageRequest.offset()}
         )
         SELECT
            sched.*,
            sa.id                                                              AS sa_id,
            sa.uu_row_id                                                       AS sa_uu_row_id,
            sa.time_created                                                    AS sa_time_created,
            sa.time_updated                                                    AS sa_time_updated,
            sa.value                                                           AS sa_value,
            sa.description                                                     AS sa_description
         FROM schedules sched
              LEFT OUTER JOIN schedule_arg sa ON sched_id = sa.schedule_id
      """
      jdbc.query(sql.trimIndent(), params) { rs ->
         val dbScheduleId = rs.getLong("sched_id")

         val localSchedule: ScheduleEntity = if (currentSchedule?.id != dbScheduleId) {
            val created = mapRow(
               rs = rs,
               scheduleTypeProvider = { scheduleTypeRepository.mapRow(rs, "schedType_") },
               scheduleCommandProvider = { scheduleCommandTypeRepository.mapRow(rs, "sctd_") }
            )

            elements.add(created)
            currentSchedule = created

            created
         } else {
            currentSchedule!!
         }

         scheduleArgumentRepository.mapRowOrNull(rs, "sa_")?.also { localSchedule.arguments.add(it) }

         if (totalElement == null) {
            totalElement = rs.getLong("total_elements")
         }
      }

      logger.trace("Query for Schedule with params {} \n{}", params, sql)

      return RepositoryPage(
         requested = pageRequest,
         elements = elements,
         totalElements = totalElement ?: 0
      )
   }

   fun forEach(type: ScheduleType, callback: (ScheduleEntity) -> Unit) {
      var result = findAll(SchedulePageRequest(page = 1, size = 100, sortBy = "id", sortDirection = "ASC"), type)

      while(result.elements.isNotEmpty()) {
         for (schedule in result.elements) {
            callback(schedule)
         }

         result = findAll(result.requested.nextPage(), type)
      }
   }

   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM schedule WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Schedule: {} exists resulted in {}", id, exists)

      return exists
   }

   fun doesNotExist(id: Long): Boolean = !exists(id)

   @Transactional
   fun insert(entity: ScheduleEntity): ScheduleEntity {
      logger.debug("Inserting Schedule {}", entity)

      val inserted = jdbc.insertReturning("""
         INSERT INTO schedule(title, description, schedule, command_id, enabled, type_id)
         VALUES(:title, :description, :schedule, :command_id, :enabled, :type_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "title" to entity.title,
            "description" to entity.description,
            "schedule" to entity.schedule,
            "command_id" to entity.command.id,
            "enabled" to entity.enabled,
            "type_id" to entity.type.id
         ),
         RowMapper { rs, _ ->
            mapRow(rs, entity)
         }
      )

      entity.arguments
         .map { scheduleArgumentRepository.insert(inserted, it) }
         .forEach { inserted.arguments.add(it) }

      return inserted
   }

   @Transactional
   fun update(entity: ScheduleEntity): ScheduleEntity {
      logger.debug("Updating Schedule {}", entity)

      val updated = jdbc.updateReturning("""
         UPDATE schedule
         SET
            title = :title,
            description = :description,
            schedule = :schedule,
            command_id = :command_id,
            enabled = :enabled,
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
            "command_id" to entity.command.id,
            "enabled" to entity.enabled,
            "type_id" to entity.type.id
         ),
         RowMapper { rs, _ ->
            mapRow(rs, entity)
         }
      )

      entity.arguments.asSequence()
         .map { scheduleArgumentRepository.upsert(updated, it) }
         .forEach { updated.arguments.add(it) }

      scheduleArgumentRepository.deleteNotIn(updated, updated.arguments)

      return updated
   }

   private fun mapRow(rs: ResultSet, entity: ScheduleEntity): ScheduleEntity =
      mapRow(rs, "", { entity.type }, { entity.command })

   private fun mapRow(rs: ResultSet, scheduleColumnPrefix: String = "sched_", scheduleTypeProvider: (rs: ResultSet) -> ScheduleType, scheduleCommandProvider: (rs: ResultSet) -> ScheduleCommandType): ScheduleEntity =
      ScheduleEntity(
         id = rs.getLong("${scheduleColumnPrefix}id"),
         uuRowId = rs.getUuid("${scheduleColumnPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${scheduleColumnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${scheduleColumnPrefix}time_updated"),
         title = rs.getString("${scheduleColumnPrefix}title"),
         description = rs.getString("${scheduleColumnPrefix}description"),
         schedule = rs.getString("${scheduleColumnPrefix}schedule"),
         enabled = rs.getBoolean("${scheduleColumnPrefix}enabled"),
         command = scheduleCommandProvider(rs),
         type = scheduleTypeProvider(rs)
      )
}
