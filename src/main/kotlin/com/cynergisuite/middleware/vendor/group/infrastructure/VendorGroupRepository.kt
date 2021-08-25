package com.cynergisuite.middleware.vendor.group.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.softDelete
import com.cynergisuite.extensions.update
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.vendor.group.VendorGroupEntity
import io.micronaut.transaction.annotation.ReadOnly
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class VendorGroupRepository @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(VendorGroupRepository::class.java)
   private fun baseSelectQuery() =
      """
      WITH company AS (
         ${companyRepository.companyBaseQuery()}
      )
      SELECT
         vgrp.id                     AS vgrp_id,
         vgrp.time_created           AS vgrp_time_created,
         vgrp.time_updated           AS vgrp_time_updated,
         vgrp.company_id             AS vgrp_company_id,
         vgrp.value                  AS vgrp_value,
         vgrp.description            AS vgrp_description,
         vgrp.deleted                AS vgrp_deleted,
         comp.id                     AS comp_id,
         comp.time_created           AS comp_time_created,
         comp.time_updated           AS comp_time_updated,
         comp.name                   AS comp_name,
         comp.doing_business_as      AS comp_doing_business_as,
         comp.client_code            AS comp_client_code,
         comp.client_id              AS comp_client_id,
         comp.dataset_code           AS comp_dataset_code,
         comp.federal_id_number      AS comp_federal_id_number,
         comp.address_id             AS address_id,
         comp.address_name           AS address_name,
         comp.address_address1       AS address_address1,
         comp.address_address2       AS address_address2,
         comp.address_city           AS address_city,
         comp.address_state          AS address_state,
         comp.address_postal_code    AS address_postal_code,
         comp.address_latitude       AS address_latitude,
         comp.address_longitude      AS address_longitude,
         comp.address_country        AS address_country,
         comp.address_county         AS address_county,
         comp.address_phone          AS address_phone,
         comp.address_fax            AS address_fax,
         count(*) OVER()             AS total_elements
      FROM vendor_group vgrp
           JOIN company comp ON vgrp.company_id = comp.id
   """

   @ReadOnly fun exists(value: String, company: CompanyEntity): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS (SELECT * FROM vendor_group WHERE value = :value AND company_id = :company_id AND deleted = FALSE)", mapOf("value" to value, "company_id" to company.id), Boolean::class.java)

      logger.trace("Checking if VendorGroup: {} exists resulted in {}", value, exists)

      return exists
   }

   @ReadOnly fun findOne(id: UUID, company: CompanyEntity): VendorGroupEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query = "${baseSelectQuery()}\nWHERE vgrp.id = :id AND comp.id = :comp_id AND vgrp.deleted = FALSE"

      logger.debug("Searching for VendorGroup using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ ->
         val vendorGroup = mapRow(rs)

         vendorGroup
      }

      logger.trace("Searching for VendorGroup: id={} resulted in {}", id, found)

      return found
   }

   @ReadOnly fun findOne(value: String, company: CompanyEntity): VendorGroupEntity? {
      val params = mutableMapOf("value" to value, "comp_id" to company.id)
      val query = "${baseSelectQuery()}\nWHERE vgrp.value = :value AND comp.id = :comp_id AND vgrp.deleted = FALSE"

      logger.debug("Searching for VendorGroup using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ ->
         val vendorGroup = mapRow(rs)

         vendorGroup
      }

      logger.trace("Searching for VendorGroup: value={} resulted in {}", value, found)

      return found
   }

   @ReadOnly
   fun findAll(pageRequest: PageRequest, company: CompanyEntity): RepositoryPage<VendorGroupEntity, PageRequest> {
      return jdbc.queryPaged(
         """
         ${baseSelectQuery()}
         WHERE comp.id = :comp_id AND vgrp.deleted = FALSE
         ORDER BY vgrp.${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "limit" to pageRequest.size(),
            "offset" to pageRequest.offset()
         ),
         pageRequest
      ) { rs, elements ->
         do {
            elements.add(mapRow(rs))
         } while (rs.next())
      }
   }

   @Transactional
   fun insert(entity: VendorGroupEntity, company: CompanyEntity): VendorGroupEntity {
      logger.debug("Inserting VendorGroup {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO vendor_group(company_id, value, description)
         VALUES (
            :company_id,
            :value,
            :description
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.id,
            "value" to entity.value,
            "description" to entity.description
         )
      ) { rs, _ -> mapDdlRow(rs, company) }
   }

   @Transactional
   fun update(entity: VendorGroupEntity): VendorGroupEntity {
      logger.debug("Updating VendorPaymentTerm {}", entity)

      val updated = jdbc.updateReturning(
         """
         UPDATE vendor_group
         SET
            company_id = :companyId,
            value = :value,
            description = :description
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "companyId" to entity.company.id,
            "value" to entity.value,
            "description" to entity.description
         )
      ) { rs, _ -> mapDdlRow(rs, entity.company) }

      logger.debug("Updated VendorGroup {}", updated)

      return updated
   }

   @Transactional
   fun delete(id: UUID, company: CompanyEntity) {
      logger.debug("Deleting vendor group with id={}", id)

      val affectedRows = jdbc.softDelete(
         """
         UPDATE vendor_group
         SET deleted = TRUE
         WHERE id = :id AND company_id = :company_id
         """,
         mapOf("id" to id, "company_id" to company.id),
         "vendor_group"
      )

      logger.info("Affected rows: {}", affectedRows)

      if (affectedRows == 0) throw NotFoundException(id)
   }

   fun mapRowOrNull(rs: ResultSet, company: CompanyEntity, columnPrefix: String): VendorGroupEntity? {
      return if (rs.getString("${columnPrefix}id") != null) {
         VendorGroupEntity(
            id = rs.getUuid("${columnPrefix}id"),
            company = company,
            value = rs.getString("${columnPrefix}value"),
            description = rs.getString("${columnPrefix}description")
         )
      } else {
         null
      }
   }

   private fun mapRow(rs: ResultSet): VendorGroupEntity {
      return VendorGroupEntity(
         id = rs.getUuid("vgrp_id"),
         company = companyRepository.mapRow(rs, "comp_"),
         value = rs.getString("vgrp_value"),
         description = rs.getString("vgrp_description")
      )
   }

   private fun mapDdlRow(rs: ResultSet, company: CompanyEntity): VendorGroupEntity {
      return VendorGroupEntity(
         id = rs.getUuid("id"),
         company = company,
         value = rs.getString("value"),
         description = rs.getString("description")
      )
   }
}
