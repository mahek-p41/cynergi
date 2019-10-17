package com.cynergisuite.middleware.schedule.infrastructure

import com.cynergisuite.domain.infrastructure.Repository
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.middleware.schedule.ScheduleArg
import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleArgRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<ScheduleArg> {
   private val logger: Logger = LoggerFactory.getLogger(ScheduleRepository::class.java)

   override fun findOne(id: Long): ScheduleArg? {
      logger.trace("Searching for ScheduleArg with id {}", id)

      val schedleArg: ScheduleArg? = null

      //jdbc.findFirstOrNull("""
      jdbc.query("""
         SELECT
             sarg.id               AS sa_id,
             sarg.uu_row_id        AS sa_uu_row_id,
             sarg.time_created     AS sa_time_created,
             sarg.time_updated     AS sa_time_updated,
             sarg.value            AS sa_value,
             sarg.description      AS sa_description
         FROM scheduleArg sarg
      """.trimIndent(),
         mapOf("id" to id),
         RowMapper { rs: ResultSet, _: Int ->
            ScheduleArg(
               id = rs.getLong("sa_id"),
               uuRowId = rs.getUuid("sa_uu_row_id"),
               timeCreated = rs.getOffsetDateTime("sa_time_created"),
               timeUpdated = rs.getOffsetDateTime("sa_time_updated"),
               value = rs.getString("sa_value"),
               description = rs.getString("sa_description")
            )
         }
      )
      return schedleArg
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM scheduleArg WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Schedule: {} exists resulted in {}", id, exists)

      return exists
   }

   @Transactional
   override fun insert(entity: ScheduleArg): ScheduleArg {
      //  TODO not ready yet
      return entity
   }

   @Transactional
   override fun update(entity: ScheduleArg): ScheduleArg {
      //  TODO not ready yet
      return entity
   }
}
