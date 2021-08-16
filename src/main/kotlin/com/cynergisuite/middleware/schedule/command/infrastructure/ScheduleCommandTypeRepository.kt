package com.cynergisuite.middleware.schedule.command.infrastructure

import com.cynergisuite.extensions.findFirst
import com.cynergisuite.middleware.schedule.command.ScheduleCommandTypeEntity
import io.micronaut.transaction.annotation.ReadOnly
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import javax.inject.Singleton

@Singleton
class ScheduleCommandTypeRepository(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(ScheduleCommandTypeRepository::class.java)

   @ReadOnly
   fun findByValue(value: String): ScheduleCommandTypeEntity {
      logger.debug("Searching for schedule command by {}", value)

      val found = jdbc.findFirst(
         """
         SELECT
            id AS sctd_id,
            value AS sctd_value,
            description AS sctd_description,
            localization_code AS sctd_localization_code
         FROM schedule_command_type_domain
         WHERE UPPER(value) = UPPER(:value)
         """.trimIndent(),
         mapOf("value" to value)
      ) { rs, _ -> mapRow(rs) }

      logger.debug("Searching for schedule command by {} resulted in {}", value, found)

      return found
   }

   fun mapRow(rs: ResultSet, columnPrefix: String = "sctd_"): ScheduleCommandTypeEntity =
      ScheduleCommandTypeEntity(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
