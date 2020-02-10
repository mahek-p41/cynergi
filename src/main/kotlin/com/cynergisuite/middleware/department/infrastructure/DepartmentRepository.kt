package com.cynergisuite.middleware.department.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.DatasetRepository
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.middleware.department.DepartmentEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.SingleColumnRowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepartmentRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : DatasetRepository {
   private val logger: Logger = LoggerFactory.getLogger(DepartmentRepository::class.java)

   fun findOne(id: Long, dataset: String): DepartmentEntity? {
      logger.debug("Search for department by id {}", id)

      val found = jdbc.findFirstOrNull("""
         SELECT
            id AS d_id,
            code AS d_code,
            description AS d_description,
            security_profile AS d_security_profile,
            default_menu AS d_default_menu,
            dataset AS d_dataset
         FROM fastinfo_prod_import.department_vw
         WHERE id = :id
               AND dataset = :dataset
         """.trimIndent(),
         mapOf("id" to id, "dataset" to dataset),
         RowMapper { rs, _ -> mapRow(rs) }
      )

      logger.trace("Searching for department by id {} resulted in {}", id, found)

      return found
   }

   override fun findDataset(id: Long): String? {
      logger.debug("Search for dataset of department by id {}", id)

      val found = jdbc.findFirstOrNull("SELECT dataset FROM fastinfo_prod_import.department_vw WHERE id = :id", mapOf("id" to id), SingleColumnRowMapper(String::class.java))

      logger.trace("Search for dataset of department by id {} resulted in {}", id, found)

      return found
   }

   fun findOneByCodeAndDataset(code: String, dataset: String): DepartmentEntity? {
      logger.debug("Searching for department by code {}", code)

      val found = jdbc.findFirstOrNull("""
         SELECT
            id AS d_id,
            code AS d_code,
            description AS d_description,
            security_profile AS d_security_profile,
            default_menu AS d_default_menu,
            dataset AS d_dataset
         FROM fastinfo_prod_import.department_vw
         WHERE code = :code
               AND dataset = :dataset
         """.trimIndent(),
         mapOf(
            "code" to code,
            "dataset" to dataset
         ),
         RowMapper { rs, _ -> mapRow(rs) }
      )

      logger.trace("Searching for department by code {} resulted in {}", code, found)

      return found
   }

   fun findAll(pageRequest: PageRequest, dataset: String): RepositoryPage<DepartmentEntity, PageRequest> {
      var totalElements: Long? = null
      val elements = mutableListOf<DepartmentEntity>()

      jdbc.query("""
         SELECT
            id AS d_id,
            code AS d_code,
            description AS d_description,
            security_profile AS d_security_profile,
            default_menu AS d_default_menu,
            dataset AS d_dataset,
            (SELECT count(*) FROM fastinfo_prod_import.department_vw WHERE dataset = :dataset) AS total_elements
         FROM fastinfo_prod_import.department_vw
         WHERE dataset = :dataset
         ORDER BY ${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT ${pageRequest.size()}
            OFFSET ${pageRequest.offset()}
         """.trimIndent(),
         mapOf("dataset" to dataset)
      ) { rs ->
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

   fun exists(id: Long, dataset: String): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM fastinfo_prod_import.department_vw WHERE id = :id AND dataset = :dataset)", mapOf("id" to id, "dataset" to dataset), Boolean::class.java)!!

      logger.trace("Checking if department: {} exists resulted in {}", id, exists)

      return exists
   }

   fun doesNotExist(id: Long, dataset: String): Boolean = !exists(id, dataset)

   fun mapRow(rs: ResultSet, columnPrefix: String = "d_"): DepartmentEntity =
      DepartmentEntity(
         id = rs.getLong("${columnPrefix}id"),
         code = rs.getString("${columnPrefix}code"),
         description = rs.getString("${columnPrefix}description"),
         securityProfile = rs.getInt("${columnPrefix}security_profile"),
         defaultMenu = rs.getString("${columnPrefix}default_menu"),
         dataset = rs.getString("${columnPrefix}dataset")
      )
}
