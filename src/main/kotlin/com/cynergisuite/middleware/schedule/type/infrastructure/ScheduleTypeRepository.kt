package com.cynergisuite.middleware.schedule.type.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirst
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.middleware.schedule.type.ScheduleTypeEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet

@Singleton
class ScheduleTypeRepository(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(ScheduleTypeRepository::class.java)

   @ReadOnly fun findOne(id: Long): ScheduleTypeEntity {
      logger.debug("Searching for schedule type by id {}", id)

      val found = jdbc.findFirst(
         """
         SELECT
            id AS std_id,
            value AS std_value,
            description AS std_description,
            localization_code AS localization_code
         FROM schedule_type_domain std
         WHERE id = :id
         """.trimIndent(),
         mapOf("id" to id)
      ) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for schedule type by id {} resulted in {}", id, found)

      return found
   }

   @ReadOnly
   fun findAll(pageRequest: PageRequest): RepositoryPage<ScheduleTypeEntity, PageRequest> {
      var totalElements: Long? = null
      val elements = mutableListOf<ScheduleTypeEntity>()

      jdbc.query(
         """
         SELECT
            id AS std_id,
            value AS std_value,
            description AS std_description,
            localization_code AS std_localization_code,
            (SELECT count(id) FROM schedule_type_domain) AS total_elements
         FROM schedule_type_domain std
         ORDER BY std_${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
               LIMIT ${pageRequest.size()}
               OFFSET ${pageRequest.offset()}
         """.trimIndent()
      ) { rs, _ ->
         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }

         elements.add(mapRow(rs))
      }

      return RepositoryPage(
         requested = pageRequest,
         elements = elements,
         totalElements = totalElements ?: 0
      )
   }

   @ReadOnly
   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM schedule_type_domain WHERE id = :id)", mapOf("id" to id), Boolean::class.java)

      logger.trace("checking if Schedule type: {} exists resulted in {}", id, exists)

      return exists
   }

   fun mapRow(rs: ResultSet, columnPrefix: String = "std_"): ScheduleTypeEntity =
      ScheduleTypeEntity(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
