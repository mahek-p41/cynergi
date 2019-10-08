package com.cynergisuite.middleware.schedule.infrastructure

import com.cynergisuite.domain.infrastructure.Repository
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.*
import com.cynergisuite.middleware.schedule.Schedule
import com.cynergisuite.middleware.schedule.ScheduleType
import io.micronaut.spring.tx.annotation.Transactional
import org.intellij.lang.annotations.Language
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
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
   private val scheduleRepository: ScheduleRepository
) : Repository<Schedule> {
   private val logger: Logger = LoggerFactory.getLogger(ScheduleRepository::class.java)


   override fun findOne(id: Long): Schedule? {
      logger.trace("Searching for Schedule with id {}", id)

      val schedule = jdbc.findFirstOrNull("""
         SELECT
            sched.id AS sched_id,
            sched.uu_row_id AS sched_uu_row_id,
            sched.time_created AS sched_time_created,
            sched.time_updated AS sched_time_updated,
            sched.title AS sched_title,
            sched.description AS sched_description,
            sched.schedule AS sched_schedule,
            sched.command AS sched_command,
            schedType.id AS schedType_id,
            schedType.value AS schedType_value,
            schedType.description AS schedType_description,
            schedType.localization_code AS schedType_localization_code
         FROM schedule sched
              JOIN schedule_type_domain schedType ON sched.type_id = schedType.id
         WHERE sched.id = :id
         """.trimIndent(),
         mapOf("id" to id),
         RowMapper { rs: ResultSet, _: Int ->
            Schedule(
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
         })

      logger.trace("Searched for Schedule {} resulted in {}", id, schedule)

      return schedule
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM schedule WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Schedule: {} exists resulted in {}", id, exists)

      return exists
   }

   @Transactional
   override fun insert(entity: Schedule): Schedule {
      logger.debug("Inserting Schedule {}", entity)

      return jdbc.insertReturning(
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
   }

   @Transactional
   override fun update(entity: Schedule): Schedule {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
   }

   fun fetchAll(pageRequest: SchedulePageRequest): RepositoryPage<Schedule> {
      logger.trace("Fetching All")

      var totalElements: Long? = null
      var currentId: Long = -1
      //var currentParentEntity: Schedule? = null
      val resultList: MutableList<Schedule> = mutableListOf()
      val params = mutableMapOf<String, Any>()
      val id = pageRequest.id
      //val storeNumber = pageRequest.storeNumber
      //val whereBuilder = StringBuilder()
      //var and = EMPTY
      //var where = "WHERE"

      @Language("PostgreSQL")
      val sql = """
        SELECT
        s.id           AS s_id,
        s.uu_row_id    AS s_uu_row_id,
        s.time_created AS s_time_created,
        s.time_updated AS s_time_updated,
        s.title        AS s_title,
        s.description  AS s_description,
        s.schedule     AS s_schedule,
        s.command      AS s_command,
        stype.id                AS stype_id,
        stype.value             AS stype_value,
        stype.description       AS stype_description,
        stype.localization_code AS stype_localization_code
        FROM     schedule s
        JOIN     schedule_type_domain stype
        ON     s.type_id = stype.id
           ORDER by ${pageRequest.camelizeSortBy()} ${pageRequest.sortDirection}
           LIMIT ${pageRequest.size}
             OFFSET ${pageRequest.offset()}
             """.trimIndent()

      jdbc.query(sql, params) { rs ->
         val tempId = rs.getLong("s_id")
         val tempParentEntity: Schedule = if (tempId != currentId) {
            currentId = tempId
            currentParentEntity = mapRow(rs)
            resultList.add(currentParentEntity!!)
            currentParentEntity!!
         } else {
            currentParentEntity!!
         }

         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }

         tempParentEntity.actions.add(schedleActionRepository.mapRow(rs))
      }

      return RepositoryPage(
         elements = resultList,
         totalElements = totalElements ?: 0
      )

   }

   fun fetchAllOLD(): List<Schedule> {
      logger.trace("Fetching All")

      val scheduleList = jdbc.query(
         """
         SELECT
            s.id           AS s_id,
            s.uu_row_id    AS s_uu_row_id,
            s.time_created AS s_time_created,
            s.time_updated AS s_time_updated,
            s.title        AS s_title,
            s.description  AS s_description,
            s.schedule     AS s_schedule,
            s.command      AS s_command,
            stype.id                AS stype_id,
            stype.value             AS stype_value,
            stype.description       AS stype_description,
            stype.localization_code AS stype_localization_code
         FROM     schedule s
         JOIN     schedule_type_domain stype
           ON     s.type_id = stype.id
         ORDER by s.id asc
         """.trimIndent(),
         emptyMap<String, Any>()
      ) { rs: ResultSet, _: Int ->
            Schedule(
               id =          rs.getLong("s_id"),
               uuRowId =     rs.getUuid("s_uu_row_id"),
               timeCreated = rs.getOffsetDateTime("s_time_created"),
               timeUpdated = rs.getOffsetDateTime("s_time_updated"),
               title =       rs.getString("s_title"),
               description = rs.getString("s_description"),
               schedule =    rs.getString("s_schedule"),
               command =     rs.getString("s_command"),
               type = ScheduleType(
                  id =               rs.getLong("stype_id"),
                  value =            rs.getString("stype_value"),
                  description =      rs.getString("stype_description"),
                  localizationCode = rs.getString("stype_localization_code")
               )
            )
         }

      logger.trace("fetchAll resulted in size {}, {}", scheduleList.size, scheduleList)

      return scheduleList
   }

   private fun mapRow(rs: ResultSet): Schedule =
      Schedule(
         id = rs.getLong("s_id"),
         uuRowId = rs.getUuid("s_uu_row_id"),
         timeCreated = rs.getOffsetDateTime("s_time_created"),
         timeUpdated = rs.getOffsetDateTime("s_time_updated"),
         //title = scheduleRepository.mapRow(rs, "s_title").toString(),
         title = scheduleRepository.mapRow(rs, "s_title"),
         description = scheduleRepository.mapRow(rs, "s_"),
         schedule = scheduleRepository.mapRow(rs, "s_"),
         command = scheduleRepository.mapRow(rs, "s_"),
         type = scheduleRepository.mapRow(rs, "s_")
      )
}
