package com.cynergisuite.middleware.schedule.infrastructure
//
//import com.cynergisuite.domain.infrastructure.Repository
//import com.cynergisuite.extensions.findFirstOrNull
//import com.cynergisuite.extensions.getOffsetDateTime
//import com.cynergisuite.extensions.getUuid
//import com.cynergisuite.middleware.schedule.Schedule
//import com.cynergisuite.middleware.schedule.ScheduleArg
//import com.cynergisuite.middleware.schedule.ScheduleType
//import org.slf4j.Logger
//import org.slf4j.LoggerFactory
//import org.springframework.jdbc.core.RowMapper
//import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
//import java.sql.ResultSet
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class ScheduleArgRepository @Inject constructor(
//   private val jdbc: NamedParameterJdbcTemplate
//) : Repository<ScheduleArg> {
//   private val logger: Logger = LoggerFactory.getLogger(ScheduleRepository::class.java)
//
//   override fun findOne(id: Long): ScheduleArg? {
//      logger.trace("Searching for ScheduleArg with id {}", id)
//
//      val schedleArg = jdbc.findFirstOrNull("""
//         SELECT
//             schedArg.id           AS sa_id,
//             schedArg.uu_row_id    AS sa_uu_row_id,
//             schedArg.time_created AS sa_time_created,
//             schedArg.time_updated AS sa_time_updated,
//             schedArg.value        AS sa_value,
//             schedArg.description  AS sa_description,
//             s.id                  AS s_id
//         FROM scheduleArg schedArg
//             JOIN schedule_domain s ON schedArg.schedule_id = s.id
//         WHERE schedArg.id = :id
//      """.trimIndent(),
//      mapOf("id" to id),
//      RowMapper { rs: ResultSet, _: Int ->
//         ScheduleArg(
//            id          = rs.getLong("sa_id"),
//            uuRowId     = rs.getUuid("sa_uu_row_id"),
//            timeCreated = rs.getOffsetDateTime("sa_time_created"),
//            timeUpdated = rs.getOffsetDateTime("sa_time_updated"),
//            value       = rs.getString("sa_value"),
//            description = rs.getString("sa_description"),
//            schedule    = Schedule(
//               id = rs.getLong("s_id"),
//               title = rs.getString("s_title"),
//               description = rs.getString("s_description"),
//               schedule = rs.getString("s_schedule"),
//               command = rs.getString("s_command"),
//               type = ScheduleType(
//
//               )
//            )
//         )
//      }
//}
