package com.cynergisuite.middleware.department.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.DatasetRepository
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.department.DepartmentEntity
import io.micronaut.cache.annotation.Cacheable
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

   fun findOne(id: Long, company: Company): DepartmentEntity? {
      logger.debug("Search for department by id {}", id)

      val found = jdbc.findFirstOrNull("""
         SELECT
            dept.id AS d_id,
            dept.code AS d_code,
            dept.description AS d_description,
            dept.security_profile AS d_security_profile,
            dept.dataset AS d_dataset,
            dept.default_menu AS d_default_menu
         FROM fastinfo_prod_import.department_vw dept
         JOIN company comp ON comp.dataset_code = dept.dataset
         WHERE dept.id = :id
               AND comp.id = :comp_id
         """.trimIndent(),
         mapOf("id" to id, "comp_id" to company.myId()),
         RowMapper { rs, _ -> mapRow(rs, company) }
      )

      logger.trace("Searching for department by id {} resulted in {}", id, found)

      return found
   }

   @Cacheable("department-cache")
   fun findOneByCodeAndDataset(code: String, company: Company): DepartmentEntity? {
      logger.debug("Searching for department by code {}", code)

      val found = jdbc.findFirstOrNull("""
         SELECT
            dept.id AS d_id,
            dept.code AS d_code,
            dept.description AS d_description,
            dept.security_profile AS d_security_profile,
            dept.dataset AS d_dataset,
            dept.default_menu AS d_default_menu
         FROM fastinfo_prod_import.department_vw dept
         JOIN company comp ON comp.dataset_code = dept.dataset
         WHERE dept.code = :code
               AND comp.id = :comp_id
         """.trimIndent(),
         mapOf(
            "code" to code,
            "comp_id" to company.myId()
         ),
         RowMapper { rs, _ -> mapRow(rs, company) }
      )

      logger.trace("Searching for department by code {} resulted in {}", code, found)

      return found
   }

   fun findAll(pageRequest: PageRequest, company: Company): RepositoryPage<DepartmentEntity, PageRequest> {
      var totalElements: Long? = null
      val elements = mutableListOf<DepartmentEntity>()

      jdbc.query("""
         SELECT
            dept.id AS d_id,
            dept.code AS d_code,
            dept.description AS d_description,
            dept.security_profile AS d_security_profile,
            dept.dataset AS d_dataset,
            dept.default_menu AS d_default_menu,
            (SELECT count(*) FROM fastinfo_prod_import.department_vw WHERE dataset = :dataset) AS total_elements
         FROM fastinfo_prod_import.department_vw dept
         JOIN company comp ON comp.dataset_code = dept.dataset
         WHERE comp.id = :comp_id
         ORDER BY d_${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "dataset" to company.myDataset(),
            "comp_id" to company.myId(),
            "limit" to pageRequest.size(),
            "offset" to pageRequest.offset()
         )
      ) { rs ->
         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }

         elements.add(mapRow(rs, company))
      }

      return RepositoryPage(
         requested = pageRequest,
         elements = elements,
         totalElements = totalElements ?: 0
      )
   }

   override fun exists(id: Long, company: Company): Boolean {
      val exists = jdbc.queryForObject("""
         SELECT count(dept.id) > 0
         FROM fastinfo_prod_import.department_vw dept
              JOIN company comp ON dept.dataset = comp.dataset_code
         WHERE dept.id = :dept_id AND comp.id = :comp_id
         """.trimIndent(),
         mapOf("dept_id" to id, "comp_id" to company.myId()),
         Boolean::class.java
      )!!

      logger.trace("Checking if department: {} exists resulted in {}", id, exists)

      return exists
   }

   fun doesNotExist(id: Long, company: Company): Boolean = !exists(id, company)

   fun mapRowOrNull(rs: ResultSet, company: Company, columnPrefix: String = "d_"): DepartmentEntity? =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, company, columnPrefix)
      } else {
         null
      }

   fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = "d_"): DepartmentEntity =
      DepartmentEntity(
         id = rs.getLong("${columnPrefix}id"),
         code = rs.getString("${columnPrefix}code"),
         description = rs.getString("${columnPrefix}description"),
         securityProfile = rs.getInt("${columnPrefix}security_profile"),
         defaultMenu = rs.getString("${columnPrefix}default_menu"),
         company = company
      )
}
