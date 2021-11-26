package com.cynergisuite.middleware.department.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.DatasetRequiringRepository
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.department.DepartmentEntity
import io.micronaut.transaction.annotation.ReadOnly
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepartmentRepository @Inject constructor(
   private val jdbc: Jdbi
) : DatasetRequiringRepository {
   private val logger: Logger = LoggerFactory.getLogger(DepartmentRepository::class.java)

   @ReadOnly
   fun findOne(id: Long, company: CompanyEntity): DepartmentEntity? {
      logger.debug("Search for department by id {}", id)

      val found = jdbc.findFirstOrNull(
         """
         SELECT
            dept.id AS d_id,
            dept.code AS d_code,
            dept.description AS d_description,
            dept.dataset AS d_dataset
         FROM fastinfo_prod_import.department_vw dept
         JOIN company comp ON comp.dataset_code = dept.dataset AND comp.deleted = FALSE
         WHERE dept.id = :id
               AND comp.id = :comp_id
         """.trimIndent(),
         mapOf("id" to id, "comp_id" to company.id)
      ) { rs, _ -> mapRow(rs, company) }

      logger.trace("Searching for department by id {} resulted in {}", id, found)

      return found
   }

   @ReadOnly
   fun findOneByCodeAndDataset(code: String, company: CompanyEntity): DepartmentEntity? {
      logger.debug("Searching for department by code {}", code)

      val found = jdbc.findFirstOrNull(
         """
         SELECT
            dept.id AS d_id,
            dept.code AS d_code,
            dept.description AS d_description,
            dept.dataset AS d_dataset
         FROM fastinfo_prod_import.department_vw dept
         JOIN company comp ON comp.dataset_code = dept.dataset AND comp.deleted = FALSE
         WHERE dept.code = :code
               AND comp.id = :comp_id
         """.trimIndent(),
         mapOf(
            "code" to code,
            "comp_id" to company.id
         )
      ) { rs, _ -> mapRow(rs, company) }

      logger.trace("Searching for department by code {} resulted in {}", code, found)

      return found
   }

   @ReadOnly
   fun findAll(pageRequest: PageRequest, company: CompanyEntity): RepositoryPage<DepartmentEntity, PageRequest> {
      var totalElements: Long? = null
      val elements = mutableListOf<DepartmentEntity>()

      jdbc.query(
         """
         SELECT
            dept.id AS d_id,
            dept.code AS d_code,
            dept.description AS d_description,
            dept.dataset AS d_dataset,
            (SELECT count(*) FROM fastinfo_prod_import.department_vw WHERE dataset = :dataset) AS total_elements
         FROM fastinfo_prod_import.department_vw dept
         JOIN company comp ON comp.dataset_code = dept.dataset AND comp.deleted = FALSE
         WHERE comp.id = :comp_id
         ORDER BY d_${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "dataset" to company.datasetCode,
            "comp_id" to company.id,
            "limit" to pageRequest.size(),
            "offset" to pageRequest.offset()
         )
      ) { rs, _ ->
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

   @ReadOnly
   override fun exists(id: Long, company: CompanyEntity): Boolean {
      val exists = jdbc.queryForObject(
         """
         SELECT count(dept.id) > 0
         FROM fastinfo_prod_import.department_vw dept
              JOIN company comp ON dept.dataset = comp.dataset_code AND comp.deleted = FALSE
         WHERE dept.id = :dept_id AND comp.id = :comp_id
         """.trimIndent(),
         mapOf("dept_id" to id, "comp_id" to company.id),
         Boolean::class.java
      )

      logger.trace("Checking if department: {} exists resulted in {}", id, exists)

      return exists
   }

   fun doesNotExist(id: Long, company: CompanyEntity): Boolean = !exists(id, company)

   fun mapRowOrNull(rs: ResultSet, company: CompanyEntity, columnPrefix: String = "d_"): DepartmentEntity? =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, company, columnPrefix)
      } else {
         null
      }

   fun mapRow(rs: ResultSet, company: CompanyEntity, columnPrefix: String = "d_"): DepartmentEntity =
      DepartmentEntity(
         id = rs.getLong("${columnPrefix}id"),
         code = rs.getString("${columnPrefix}code"),
         description = rs.getString("${columnPrefix}description"),
         company = company
      )
}
