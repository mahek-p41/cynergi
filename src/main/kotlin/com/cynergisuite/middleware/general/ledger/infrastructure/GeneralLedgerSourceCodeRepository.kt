package com.cynergisuite.middleware.general.ledger.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.general.ledger.GeneralLedgerSourceCodeEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class GeneralLedgerSourceCodeRepository @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerSourceCodeRepository::class.java)
   private fun selectBaseQuery() =
   """
      WITH company AS (
         ${companyRepository.companyBaseQuery()}
      )
      SELECT
         glSrcCodes.id               AS glSrcCodes_id,
         glSrcCodes.uu_row_id        AS glSrcCodes_uu_row_id,
         glSrcCodes.time_created     AS glSrcCodes_time_created,
         glSrcCodes.time_updated     AS glSrcCodes_time_updated,
         glSrcCodes.company_id       AS glSrcCodes_company_id,
         glSrcCodes.value            AS glSrcCodes_value,
         glSrcCodes.description      AS glSrcCodes_description,
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
      FROM general_ledger_source_codes glSrcCodes
         JOIN company comp ON glSrcCodes.company_id = comp.id
   """

   fun exists(value: String, company: Company): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS (SELECT * FROM general_ledger_source_codes WHERE value = :value AND company_id = :company_id)", mapOf("value" to value, "company_id" to company.myId()), Boolean::class.java)!!

      logger.trace("Checking if GeneralLedgerSourceCode: {} exists resulted in {}", value, exists)

      return exists
   }

   fun findOne(id: Long, company: Company): GeneralLedgerSourceCodeEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${selectBaseQuery()}\nWHERE glSrcCodes.id = :id AND comp.id = :comp_id"

      logger.debug("Searching for GeneralLedgerSourceCode using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs ->
         val GeneralLedgerSourceCode = mapRow(rs)

         GeneralLedgerSourceCode
      }

      logger.trace("Searching for GeneralLedgerSourceCode: {} resulted in {}", id, found)

      return found
   }

   fun findAll(pageRequest: PageRequest, company: Company): RepositoryPage<GeneralLedgerSourceCodeEntity, PageRequest> {
      return jdbc.queryPaged(
         """
         ${selectBaseQuery()}
         WHERE comp.id = :comp_id
         ORDER BY glSrcCodes.${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
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
         } while (rs.next())
      }
   }

   @Transactional
   fun insert(entity: GeneralLedgerSourceCodeEntity, company: Company): GeneralLedgerSourceCodeEntity {
      logger.debug("Inserting GeneralLedgerSourceCode {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO general_ledger_source_codes(company_id, value, description)
         VALUES (
            :company_id,
            :value,
            :description
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.myId(),
            "value" to entity.value,
            "description" to entity.description
         ),
         RowMapper { rs, _ -> mapDdlRow(rs, company) }
      )
   }

   @Transactional
   fun update(entity: GeneralLedgerSourceCodeEntity): GeneralLedgerSourceCodeEntity {
      logger.debug("Updating GeneralLedgerSourceCode {}", entity)

      val updated = jdbc.updateReturning(
         """
         UPDATE general_ledger_source_codes
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

      logger.debug("Updated GeneralLedgerSourceCode {}", updated)

      return updated
   }

   private fun mapRow(rs: ResultSet): GeneralLedgerSourceCodeEntity {
      return GeneralLedgerSourceCodeEntity(
         id = rs.getLong("glSrcCodes_id"),
         company = companyRepository.mapRow(rs, "comp_"),
         value = rs.getString("glSrcCodes_value"),
         description = rs.getString("glSrcCodes_description")
      )
   }

   private fun mapDdlRow(rs: ResultSet, company: Company): GeneralLedgerSourceCodeEntity {
      return GeneralLedgerSourceCodeEntity(
         id = rs.getLong("id"),
         company = company,
         value = rs.getString("value"),
         description = rs.getString("description")
      )
   }
}
