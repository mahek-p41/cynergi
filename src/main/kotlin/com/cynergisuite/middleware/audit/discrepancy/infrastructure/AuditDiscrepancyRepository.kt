package com.cynergisuite.middleware.audit.discrepancy.infrastructure

import com.cynergisuite.domain.IdentifiableEntity
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.domain.infrastructure.Repository
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.audit.Audit
import com.cynergisuite.middleware.audit.discrepancy.AuditDiscrepancy
import com.cynergisuite.middleware.employee.Employee
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditDiscrepancyRepository @Inject constructor(
   private val employeeRepository: EmployeeRepository,
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<AuditDiscrepancy> {
   private val logger: Logger = LoggerFactory.getLogger(AuditDiscrepancyRepository::class.java)
   private val selectBase = """
      WITH ad_employees AS (
         ${employeeRepository.selectBase}
      )
      SELECT
         ad.id AS ad_id,
         ad.uu_row_id AS ad_uu_row_id,
         ad.time_created AS ad_time_created,
         ad.time_updated AS ad_time_updated,
         ad.bar_code AS ad_bar_code,
         ad.inventory_id AS ad_inventory_id,
         ad.inventory_brand AS ad_inventory_brand,
         ad.inventory_model AS ad_inventory_model,
         ad.notes AS ad_notes,
         ad.audit_id AS ad_audit_id,
         e.e_id AS e_id,
         e.e_time_created AS e_time_created,
         e.e_time_updated AS e_time_updated,
         e.e_number AS e_number,
         e.e_last_name AS e_last_name,
         e.e_first_name_mi AS e_first_name_mi,
         e.e_pass_code AS  e_pass_code,
         e.e_active AS e_active,
         e.e_loc AS e_loc,
         e.s_id AS s_id,
         e.s_time_created AS s_time_created,
         e.s_time_updated AS s_time_updated,
         e.s_number AS s_number,
         e.s_name AS s_name,
         e.s_dataset AS s_dataset
      FROM audit_discrepancy ad
           JOIN ad_employees e
             ON ad.scanned_by = e.e_number
   """.trimIndent()

   override fun findOne(id: Long): AuditDiscrepancy? {
      val found = jdbc.findFirstOrNull(
         "$selectBase\nWHERE ad.id = :id",
         mapOf("id" to id),
         RowMapper { rs, _ ->
            val scannedBy = employeeRepository.mapRow(rs, "e_")

            mapRow(rs, scannedBy, SimpleIdentifiableEntity(rs.getLong("ad_audit_id")), "ad_")
         }
      )

      logger.trace("Searching for AuditException: {} resulted in {}", id, found)

      return found
   }

   fun findAll(audit: Audit, page: PageRequest): RepositoryPage<AuditDiscrepancy> {
      var totalElements: Long? = null
      val resultList: MutableList<AuditDiscrepancy> = mutableListOf()

      jdbc.query("""
         WITH paged AS (
            $selectBase
         )
         SELECT 
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         WHERE p.ad_audit_id = :audit_id
         ORDER by ad_${page.camelizeSortBy()} ${page.sortDirection}
         LIMIT ${page.size} OFFSET ${page.offset()}
      """.trimIndent(),
         mutableMapOf("audit_id" to audit.id)
      ) { rs ->
         val scannedBy = employeeRepository.mapRow(rs, "e_")

         resultList.add(mapRow(rs, scannedBy, SimpleIdentifiableEntity(rs.getLong("ad_audit_id")), "ad_"))

         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }
      }

      return RepositoryPage(
         elements = resultList,
         totalElements = totalElements ?: 0
      )
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM audit_discrepancy WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if AuditException: {} exists resulted in {}", id, exists)

      return exists
   }

   @Transactional
   override fun insert(entity: AuditDiscrepancy): AuditDiscrepancy {
      logger.debug("Inserting audit_discrepancy {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO audit_discrepancy(bar_code, inventory_id, inventory_brand, inventory_model, scanned_by, notes, audit_id)
         VALUES (:bar_code, :inventory_id, :inventory_brand, :inventory_model, :scanned_by, :notes, :audit_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "bar_code" to entity.barCode,
            "inventory_id" to entity.inventoryId,
            "inventory_brand" to entity.inventoryBrand,
            "inventory_model" to entity.inventoryModel,
            "scanned_by" to entity.scannedBy.number,
            "notes" to entity.notes,
            "audit_id" to entity.audit.entityId()
         ),
         RowMapper { rs, rowNum ->
            mapRow(rs, entity.scannedBy, entity.audit)
         }
      )
   }

   @Transactional
   override fun update(entity: AuditDiscrepancy): AuditDiscrepancy {
      logger.debug("Updating audit_discrepancy {}", entity)

      return jdbc.updateReturning("""
         UPDATE audit_discrepancy
         SET
            bar_code = :bar_code,
            inventory_id = :inventory_id,
            inventory_brand = :inventory_brand,
            inventory_model = :inventory_model,
            scanned_by = :scanned_by,
            notes = :notes
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "bar_code" to entity.barCode,
            "inventory_id" to entity.inventoryId,
            "inventory_brand" to entity.inventoryBrand,
            "inventory_model" to entity.inventoryModel,
            "scanned_by" to entity.scannedBy.number,
            "notes" to entity.notes
         ),
         RowMapper { rs, rowNum ->
            mapRow(rs, entity.scannedBy, entity.audit)
         }
      )
   }

   private fun mapRow(rs: ResultSet, scannedBy: Employee, audit: IdentifiableEntity, columnPrefix: String = EMPTY): AuditDiscrepancy =
      AuditDiscrepancy(
         id = rs.getLong("${columnPrefix}id"),
         uuRowId = rs.getUuid("${columnPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         barCode = rs.getString("${columnPrefix}bar_code"),
         inventoryId = rs.getString("${columnPrefix}inventory_id"),
         inventoryBrand = rs.getString("${columnPrefix}inventory_brand"),
         inventoryModel = rs.getString("${columnPrefix}inventory_model"),
         scannedBy = scannedBy,
         notes = rs.getString("${columnPrefix}notes"),
         audit = audit
      )
}
