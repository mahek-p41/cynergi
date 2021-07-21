package com.cynergisuite.middleware.accounting.general.ledger.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class GeneralLedgerSourceCodeRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerSourceCodeRepository::class.java)
   private fun selectBaseQuery() =
      """
      SELECT
         glSrcCodes.id               AS glSrcCodes_id,
         glSrcCodes.company_id       AS glSrcCodes_company_id,
         glSrcCodes.value            AS glSrcCodes_value,
         glSrcCodes.description      AS glSrcCodes_description,
         count(*) OVER()             AS total_elements
      FROM general_ledger_source_codes glSrcCodes
   """

   fun exists(value: String, company: Company): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS (SELECT * FROM general_ledger_source_codes WHERE value = :value AND company_id = :company_id)", mapOf("value" to value, "company_id" to company.myId()), Boolean::class.java)!!

      logger.trace("Checking if GeneralLedgerSourceCode: {} exists resulted in {}", value, exists)

      return exists
   }

   fun findOne(id: UUID, company: Company): GeneralLedgerSourceCodeEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${selectBaseQuery()}\nWHERE glSrcCodes.id = :id AND glSrcCodes.company_id = :comp_id"

      logger.debug("Searching for GeneralLedgerSourceCode using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs -> mapRow(rs, "glSrcCodes_") }

      logger.trace("Searching for GeneralLedgerSourceCode: {} resulted in {}", id, found)

      return found
   }

   fun findAll(pageRequest: PageRequest, company: Company): RepositoryPage<GeneralLedgerSourceCodeEntity, PageRequest> {
      return jdbc.queryPaged(
         """
         ${selectBaseQuery()}
         WHERE glSrcCodes.company_id = :comp_id
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
            elements.add(mapRow(rs, "glSrcCodes_"))
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
         )
      ) { rs, _ -> mapRow(rs) }
   }

   @Transactional
   fun update(entity: GeneralLedgerSourceCodeEntity, company: Company): GeneralLedgerSourceCodeEntity {
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
            "companyId" to company.myId(),
            "value" to entity.value,
            "description" to entity.description
         )
      ) { rs, _ -> mapRow(rs) }

      logger.debug("Updated GeneralLedgerSourceCode {}", updated)

      return updated
   }

   @Transactional
   fun delete(id: UUID, company: Company) {
      logger.debug("Deleting GeneralLedgerSourceCode with id={}", id)

      val rowsAffected = jdbc.update(
         """
         DELETE FROM general_ledger_source_codes
         WHERE id = :id AND company_id = :company_id
         """,
         mapOf("id" to id, "company_id" to company.myId())
      )
      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) throw NotFoundException(id)
   }

   fun mapRow(rs: ResultSet, columnPrefix: String? = EMPTY): GeneralLedgerSourceCodeEntity {
      return GeneralLedgerSourceCodeEntity(
         id = rs.getUuid("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description")
      )
   }
}
