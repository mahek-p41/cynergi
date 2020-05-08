package com.cynergisuite.middleware.vendor.group.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.vendor.group.VendorGroupEntity
import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorGroupRepository @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(VendorGroupRepository::class.java)
   private fun baseSelectQuery() = """
      SELECT
         vgrp.id                     AS vgrp_id,
         vgrp.uu_row_id              AS vgrp_uu_row_id,
         vgrp.time_created           AS vgrp_time_created,
         vgrp.time_updated           AS vgrp_time_updated,
         vgrp.company_id             AS vgrp_company_id,
         vgrp.value                  AS vgrp_value,
         vgrp.description            AS vgrp_description,
         comp.id                     AS comp_id,
         comp.uu_row_id              AS comp_uu_row_id,
         comp.time_created           AS comp_time_created,
         comp.time_updated           AS comp_time_updated,
         comp.name                   AS comp_name,
         comp.doing_business_as      AS comp_doing_business_as,
         comp.client_code            AS comp_client_code,
         comp.client_id              AS comp_client_id,
         comp.dataset_code           AS comp_dataset_code,
         comp.federal_id_number      AS comp_federal_id_number,
         count(*) OVER()             AS total_elements
      FROM vendor_group vgrp
           JOIN company comp ON vgrp.company_id = comp.id
   """

   fun findOne(id: Long, company: Company): VendorGroupEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${baseSelectQuery()}\nWHERE vgrp.id = :id AND comp.id = :comp_id"

      logger.debug("Searching for VendorGroup using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs ->
         val vendorGroup = mapRow(rs)

         vendorGroup
      }

      logger.trace("Searching for VendorGroup: {} resulted in {}", id, found)

      return found
   }

   fun findAll(pageRequest: PageRequest, company: Company): RepositoryPage<VendorGroupEntity, PageRequest> {
      return jdbc.queryPaged("""
         ${baseSelectQuery()}
         WHERE comp.id = :comp_id
         ORDER BY vgrp.${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "comp_id" to company.myId(),
            "limit" to pageRequest.size(),
            "offset" to pageRequest.offset()
         ),
         pageRequest
      ) { rs, elements ->
         do {
            elements.add(mapRow(rs))
         } while(rs.next())
      }
   }

   @Transactional
   fun insert(entity: VendorGroupEntity): VendorGroupEntity {
      logger.debug("Inserting VendorGroup {}", entity)

      val inserted = jdbc.insertReturning(
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
            "company_id" to entity.company.myId(),
            "value" to entity.value,
            "description" to entity.description
         ),
         RowMapper { rs, _ -> mapDdlRow(rs, entity.company) }
      )

      return inserted
   }

   @Transactional
   fun update(entity: VendorGroupEntity): VendorGroupEntity {
      logger.debug("Updating VendorPaymentTerm {}", entity)

      val updated = jdbc.updateReturning("""
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
            "companyId" to entity.company.myId(),
            "value" to entity.value,
            "description" to entity.description
         ),
         RowMapper { rs, _ -> mapDdlRow(rs, entity.company) }
      )

      logger.debug("Updated VendorGroup {}", updated)

      return updated
   }

   private fun mapRow(rs: ResultSet): VendorGroupEntity {
      return VendorGroupEntity(
         id = rs.getLong("vgrp_id"),
         company = companyRepository.mapRow(rs, "comp_"),
         value = rs.getString("vgrp_value"),
         description = rs.getString("vgrp_description")
      )
   }

   private fun mapDdlRow(rs: ResultSet, company: Company): VendorGroupEntity {
      return VendorGroupEntity(
         id = rs.getLong("id"),
         company = company,
         value = rs.getString("value"),
         description = rs.getString("description")
      )
   }

}
