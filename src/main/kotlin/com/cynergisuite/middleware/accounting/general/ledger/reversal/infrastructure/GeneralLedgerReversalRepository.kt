package com.cynergisuite.middleware.accounting.general.ledger.reversal.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalEntity
import com.cynergisuite.middleware.company.Company
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
class GeneralLedgerReversalRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val generalLedgerSourceCodeRepository: GeneralLedgerSourceCodeRepository,
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerReversalRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         SELECT
            glReversal.id                                                   AS glReversal_id,
            glReversal.time_created                                         AS glReversal_time_created,
            glReversal.time_updated                                         AS glReversal_time_updated,
            glReversal.company_id                                           AS glReversal_company_id,
            glReversal.date                                                 AS glReversal_date,
            glReversal.reversal_date                                        AS glReversal_reversal_date,
            glReversal.comment                                              AS glReversal_comment,
            glReversal.entry_month                                          AS glReversal_entry_month,
            glReversal.entry_number                                         AS glReversal_entry_number,
            source.id                                                       AS glReversal_source_id,
            source.company_id                                               AS glReversal_source_company_id,
            source.value                                                    AS glReversal_source_value,
            source.description                                              AS glReversal_source_description,
            source.company_id                                               AS glReversal_source_comp_id,
            count(*) OVER()                                                 AS total_elements
         FROM general_ledger_reversal glReversal
            JOIN general_ledger_source_codes source ON glReversal.source_id = source.id
      """
   }

   fun findOne(id: UUID, company: Company): GeneralLedgerReversalEntity? {
      val params = mutableMapOf<String, Any?>("id" to id)
      val query = "${selectBaseQuery()}\nWHERE glReversal.id = :id"

      logger.debug("Searching for GeneralLedgerReversal using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs ->
         val generalLedgerReversal = mapRow(rs, company, "glReversal_")

         generalLedgerReversal
      }

      logger.trace("Searching for GeneralLedgerReversal: {} resulted in {}", id, found)

      return found
   }

   fun findAll(pageRequest: PageRequest, company: Company): RepositoryPage<GeneralLedgerReversalEntity, PageRequest> {
      return jdbc.queryPaged(
         """
         ${selectBaseQuery()}
         WHERE glReversal.company_id = :comp_id
         ORDER BY glReversal.${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
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
            elements.add(mapRow(rs, company, "glReversal_"))
         } while (rs.next())
      }
   }

   @Transactional
   fun insert(entity: GeneralLedgerReversalEntity, company: Company): GeneralLedgerReversalEntity {
      logger.debug("Inserting GeneralLedgerReversal {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO general_ledger_reversal(
            company_id,
            source_id,
            date,
            reversal_date,
            comment,
            entry_month,
            entry_number
         )
         VALUES (
            :company_id,
            :source_id,
            :date,
            :reversal_date,
            :comment,
            :entry_month,
            :entry_number
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.myId(),
            "source_id" to entity.source.id,
            "date" to entity.date,
            "reversal_date" to entity.reversalDate,
            "comment" to entity.comment,
            "entry_month" to entity.entryMonth,
            "entry_number" to entity.entryNumber
         )
      ) { rs, _ -> mapRow(rs, entity) }
   }

   @Transactional
   fun update(entity: GeneralLedgerReversalEntity, company: Company): GeneralLedgerReversalEntity {
      logger.debug("Updating GeneralLedgerReversal {}", entity)

      val updated = jdbc.updateReturning(
         """
         UPDATE general_ledger_reversal
         SET
            company_id = :company_id,
            source_id = :source_id,
            date = :date,
            reversal_date = :reversal_date,
            comment = :comment,
            entry_month = :entry_month,
            entry_number = :entry_number
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to company.myId(),
            "source_id" to entity.source.id,
            "date" to entity.date,
            "reversal_date" to entity.reversalDate,
            "comment" to entity.comment,
            "entry_month" to entity.entryMonth,
            "entry_number" to entity.entryNumber
         )
      ) { rs, _ -> mapRow(rs, entity) }

      logger.debug("Updated GeneralLedgerReversal {}", updated)

      return updated
   }

   fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): GeneralLedgerReversalEntity {
      return GeneralLedgerReversalEntity(
         id = rs.getUuid("${columnPrefix}id"),
         source = generalLedgerSourceCodeRepository.mapRow(rs, "${columnPrefix}source_"),
         date = rs.getLocalDate("${columnPrefix}date"),
         reversalDate = rs.getLocalDate("${columnPrefix}reversal_date"),
         comment = rs.getString("${columnPrefix}comment"),
         entryMonth = rs.getInt("${columnPrefix}entry_month"),
         entryNumber = rs.getInt("${columnPrefix}entry_number")
      )
   }

   private fun mapRow(rs: ResultSet, entity: GeneralLedgerReversalEntity, columnPrefix: String = EMPTY): GeneralLedgerReversalEntity {
      return GeneralLedgerReversalEntity(
         id = rs.getUuid("${columnPrefix}id"),
         source = entity.source,
         date = rs.getLocalDate("${columnPrefix}date"),
         reversalDate = rs.getLocalDate("${columnPrefix}reversal_date"),
         comment = rs.getString("${columnPrefix}comment"),
         entryMonth = rs.getInt("${columnPrefix}entry_month"),
         entryNumber = rs.getInt("${columnPrefix}entry_number")
      )
   }
}
