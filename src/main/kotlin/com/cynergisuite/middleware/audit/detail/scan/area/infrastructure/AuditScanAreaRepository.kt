package com.cynergisuite.middleware.audit.detail.scan.area.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaEntity
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import org.apache.commons.lang3.StringUtils.EMPTY
import org.intellij.lang.annotations.Language
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class AuditScanAreaRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val storeRepository: StoreRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditScanAreaRepository::class.java)

   @Language("PostgreSQL")
   private fun selectBaseQuery(): String {
      return """
         SELECT
            area.id,
            area.name,
            area.store_number_sfk,
            area.company_id,
            store.id AS store_id,
            store.number AS store_number,
            store.name AS store_name
         FROM audit_scan_area area
            JOIN company comp ON area.company_id = comp.id
            JOIN fastinfo_prod_import.store_vw store ON comp.dataset_code = store.dataset AND area.store_number_sfk = store.number
      """
   }

   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS (SELECT id FROM audit_scan_area WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Scan Area: {} exists resulted in {}", id, exists)

      return exists
   }

   fun exists(name: String, store: StoreEntity): Boolean {
      val exists = jdbc.queryForObject(
         """
               SELECT EXISTS (
                  SELECT id
                  FROM audit_scan_area
                  WHERE name = :name
                     AND store_number_sfk = :store_number
                     AND company_id = :comp_id
               )
            """,
         mapOf("name" to name, "comp_id" to store.myCompany().myId(), "store_number" to store.myNumber()),
         Boolean::class.java
      )!!

      logger.trace("Checking if Scan Area with the same name, company, store exists resulted in {}", exists)

      return exists
   }

   fun findOne(id: Long, company: Company): AuditScanAreaEntity? =
      jdbc.findFirstOrNull(
         "${selectBaseQuery()} WHERE area.id = :id", mapOf("id" to id),
         RowMapper { rs, _ -> mapRow(rs, company) }
      )

   fun findAll(user: User): List<AuditScanAreaEntity> =
      jdbc.query(
         """${selectBaseQuery()}
                    WHERE company_id = :comp_id AND store_number_sfk = :store_number
                    ORDER BY name""",
         mapOf("comp_id" to user.myCompany().myId(), "store_number" to user.myLocation().myNumber())
      ) { rs, _ -> mapRow(rs, user.myCompany()) }

   fun findAll(company: Company, storeId: Long, pageRequest: PageRequest): RepositoryPage<AuditScanAreaEntity, PageRequest> {
      var totalElements: Long? = null
      val elements = mutableListOf<AuditScanAreaEntity>()
      jdbc.query(
         """
         WITH paged AS (
            ${selectBaseQuery()}
            WHERE store.id = :store_id
            ORDER BY name
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER BY ${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT :limit OFFSET :offset
         """,
         mapOf("store_id" to storeId, "limit" to pageRequest.size(), "offset" to pageRequest.offset())
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

   @Transactional
   fun insert(entity: AuditScanAreaEntity): AuditScanAreaEntity {
      logger.debug("Inserting audit scan area {}", entity)
      return jdbc.insertReturning(
         """
            INSERT INTO audit_scan_area(name, store_number_sfk, company_id)
            VALUES (:name, :store_number, :company_id)
            RETURNING *
         """.trimIndent(),
         mapOf(
            "name" to entity.name,
            "store_number" to entity.store!!.number,
            "company_id" to entity.company.myId()
         ),
         RowMapper { rs, _ -> mapRow(rs, entity.company, entity.store) }
      )
   }

   @Transactional
   fun update(entity: AuditScanAreaEntity): AuditScanAreaEntity {
      logger.debug("Updating audit scan area {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE audit_scan_area
         SET
            name = :name,
            store_number_sfk = :store_number,
            company_id = :comp_id
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "name" to entity.name,
            "store_number" to entity.store!!.number,
            "comp_id" to entity.company.myId()
         ),
         RowMapper { rs, _ ->
            mapRow(rs, entity.company, entity.store)
         }
      )
   }

   fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = EMPTY, storePrefix: String = "store_"): AuditScanAreaEntity =
      AuditScanAreaEntity(
         id = rs.getLong("${columnPrefix}id"),
         name = rs.getString("${columnPrefix}name"),
         store = storeRepository.mapRowOrNull(rs, company, storePrefix) as? StoreEntity,
         company = company as CompanyEntity
      )

   private fun mapRow(rs: ResultSet, company: Company, store: StoreEntity, columnPrefix: String? = EMPTY): AuditScanAreaEntity =
      AuditScanAreaEntity(
         id = rs.getLong("${columnPrefix}id"),
         name = rs.getString("${columnPrefix}name"),
         store = store,
         company = company as CompanyEntity
      )
}
