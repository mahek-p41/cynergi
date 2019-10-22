package com.cynergisuite.middleware.department.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.middleware.department.DepartmentEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepartmentRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(DepartmentRepository::class.java)

   fun findOne(id: Long): DepartmentEntity? {
      logger.debug("Search for department by id {}", id)

      val found = jdbc.findFirstOrNull("""
         SELECT 
            id AS d_id,
            time_created AS d_time_created,
            time_updated AS d_time_updated,
            code AS d_code,
            description AS d_description,
            security_profile AS d_security_profile,
            default_menu AS d_default_menu
         FROM fastinfo_prod_import.department_vw
         WHERE id = :id
         """.trimIndent(),
         mapOf("id" to id),
         RowMapper { rs, _ -> mapRow(rs) }
      )

      logger.debug("Searching for department by id {} resulted in {}", id, found)

      return found
   }

   fun findOneByCode(code: String): DepartmentEntity? {
      logger.debug("Searching for department by code {}", code)

      val found = jdbc.findFirstOrNull("""
         SELECT 
            id AS d_id,
            time_created AS d_time_created,
            time_updated AS d_time_updated,
            code AS d_code,
            description AS d_description,
            security_profile AS d_security_profile,
            default_menu AS d_default_menu
         FROM fastinfo_prod_import.department_vw
         WHERE code = :code
         """.trimIndent(),
         mapOf("code" to code),
         RowMapper { rs, _ -> mapRow(rs) }
      )

      logger.debug("Searching for department by code {} resulted in {}", code, found)

      return found
   }

   fun findAll(pageRequest: PageRequest): RepositoryPage<DepartmentEntity> {
      var totalElements: Long? = null
      val elements = mutableListOf<DepartmentEntity>()

      jdbc.query("""
         SELECT 
            id AS d_id,
            time_created AS d_time_created,
            time_updated AS d_time_updated,
            code AS d_code,
            description AS d_description,
            security_profile AS d_security_profile,
            default_menu AS d_default_menu
            (SELECT count(*) FROM fastinfo_prod_import.department_vw) AS total_elements
         FROM fastinfo_prod_import.department_vw
         ORDER BY ${pageRequest.snakeSortBy()} ${pageRequest.sortDirection}
         LIMIT ${pageRequest.size}
            OFFSET ${pageRequest.offset()}
         """.trimIndent()
      ) { rs ->
         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }

         elements.add(mapRow(rs))
      }

      return RepositoryPage(
         elements = elements,
         totalElements = totalElements ?: 0
      )
   }

   fun mapRowOrNull(rs: ResultSet, columnPrefix: String = "d_"): DepartmentEntity? =
      if (rs.getString("d_id") != null) {
         mapRow(rs, columnPrefix)
      } else {
         null
      }

   private fun mapRow(rs: ResultSet, columnPrefix: String = "d_"): DepartmentEntity =
      DepartmentEntity(
         id = rs.getLong("${columnPrefix}id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         code = rs.getString("${columnPrefix}code"),
         description = rs.getString("${columnPrefix}description"),
         securityProfile = rs.getInt("${columnPrefix}security_profile"),
         defaultMenu = rs.getString("${columnPrefix}default_menu")
      )
}
