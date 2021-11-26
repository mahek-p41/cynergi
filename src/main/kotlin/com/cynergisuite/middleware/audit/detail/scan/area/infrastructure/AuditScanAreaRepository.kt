package com.cynergisuite.middleware.audit.detail.scan.area.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaEntity
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.lang3.StringUtils.EMPTY
import org.intellij.lang.annotations.Language
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class AuditScanAreaRepository @Inject constructor(
   private val jdbc: Jdbi,
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
            JOIN company comp ON area.company_id = comp.id AND comp.deleted = FALSE
            JOIN fastinfo_prod_import.store_vw store ON comp.dataset_code = store.dataset AND area.store_number_sfk = store.number
      """
   }

   @ReadOnly
   fun exists(id: UUID): Boolean {
      val exists = jdbc.queryForObject(
         "SELECT EXISTS (SELECT id FROM audit_scan_area WHERE id = :id)",
         mapOf("id" to id),
         Boolean::class.java
      )

      logger.info("Checking if Scan Area: {} exists resulted in {}", id, exists)

      return exists
   }

   @ReadOnly
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
         mapOf("name" to name, "comp_id" to store.myCompany().id, "store_number" to store.myNumber()),
         Boolean::class.java
      )

      logger.info("Checking if Scan Area with the same name, company, store exists resulted in {}", exists)

      return exists
   }

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): AuditScanAreaEntity? =
      jdbc.findFirstOrNull(
         "${selectBaseQuery()} WHERE area.id = :id AND comp.deleted = FALSE",
         mapOf("id" to id)
      ) { rs, _ -> mapRow(rs, company) }

   @ReadOnly
   fun findAll(user: User): List<AuditScanAreaEntity> {
      val elements = mutableListOf<AuditScanAreaEntity>()
      val params = mapOf("comp_id" to user.myCompany().id, "store_number" to user.myLocation().myNumber())
      jdbc.query(
         """${selectBaseQuery()}
                    WHERE company_id = :comp_id AND store_number_sfk = :store_number
                    ORDER BY name""",
         params
      ) { rs, _ -> elements.add(mapRow(rs, user.myCompany())) }

      logger.info("Find all scan-areas with params {} \nResulted in {}", params, elements)

      return elements
   }

   @ReadOnly
   fun findAll(
      company: CompanyEntity,
      storeId: Long,
      pageRequest: PageRequest
   ): RepositoryPage<AuditScanAreaEntity, PageRequest> {
      var totalElements: Long? = null
      val elements = mutableListOf<AuditScanAreaEntity>()
      val params = mapOf(
         "comp_id" to company.id,
         "store_id" to storeId,
         "limit" to pageRequest.size(),
         "offset" to pageRequest.offset()
      )

      jdbc.query(
         """
         WITH paged AS (
            ${selectBaseQuery()}
            WHERE company_id = :comp_id AND store.id = :store_id
            ORDER BY name
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER BY ${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT :limit OFFSET :offset
         """,
         params
      ) { rs, _ ->
         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }
         elements.add(mapRow(rs, company))
      }

      logger.info("Find all scan-areas with params {} \nResulted in {}", params, elements)

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
            "company_id" to entity.company.id
         )
      ) { rs, _ -> mapRow(rs, entity.company, entity.store) }
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
            "comp_id" to entity.company.id
         )
      ) { rs, _ ->
         mapRow(rs, entity.company, entity.store)
      }
   }

   fun mapRow(
      rs: ResultSet,
      company: CompanyEntity,
      columnPrefix: String = EMPTY,
      storePrefix: String = "store_"
   ): AuditScanAreaEntity =
      AuditScanAreaEntity(
         id = rs.getUuid("${columnPrefix}id"),
         name = rs.getString("${columnPrefix}name"),
         store = storeRepository.mapRowOrNull(rs, company, storePrefix) as? StoreEntity,
         company = company
      )

   private fun mapRow(
      rs: ResultSet,
      company: CompanyEntity,
      store: StoreEntity,
      columnPrefix: String? = EMPTY
   ): AuditScanAreaEntity =
      AuditScanAreaEntity(
         id = rs.getUuid("${columnPrefix}id"),
         name = rs.getString("${columnPrefix}name"),
         store = store,
         company = company
      )
}
