package com.cynergisuite.middleware.accounting.general.ledger.reversal.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.softDelete
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalEntity
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.time.LocalDate
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class GeneralLedgerReversalRepository @Inject constructor(
   private val jdbc: Jdbi,
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
            glReversal.deleted                                              AS glReversal_deleted,
            source.id                                                       AS glReversal_source_id,
            source.company_id                                               AS glReversal_source_company_id,
            source.value                                                    AS glReversal_source_value,
            source.description                                              AS glReversal_source_description,
            source.company_id                                               AS glReversal_source_comp_id,
            count(*) OVER()                                                 AS total_elements
         FROM general_ledger_reversal glReversal
            JOIN general_ledger_source_codes source ON glReversal.source_id = source.id AND source.deleted = FALSE
      """
   }

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): GeneralLedgerReversalEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query = "${selectBaseQuery()}\nWHERE glReversal.id = :id AND glReversal.company_id = :comp_id AND glReversal.deleted = FALSE"

      logger.debug("Searching for GeneralLedgerReversal using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ ->
         val generalLedgerReversal = mapRow(rs, company, "glReversal_")

         generalLedgerReversal
      }

      logger.trace("Searching for GeneralLedgerReversal: {} resulted in {}", id, found)

      return found
   }

   @ReadOnly
   fun findAll(
      pageRequest: PageRequest,
      company: CompanyEntity
   ): RepositoryPage<GeneralLedgerReversalEntity, PageRequest> {
      return jdbc.queryPaged(
         """
         ${selectBaseQuery()}
         WHERE glReversal.company_id = :comp_id AND glReversal.deleted = FALSE
         ORDER BY glReversal.${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
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
            elements.add(mapRow(rs, company, "glReversal_"))
         } while (rs.next())
      }
   }

   @Transactional
   fun insert(entity: GeneralLedgerReversalEntity, company: CompanyEntity): GeneralLedgerReversalEntity {
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
            "company_id" to company.id,
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
   fun update(entity: GeneralLedgerReversalEntity, company: CompanyEntity): GeneralLedgerReversalEntity {
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
            "company_id" to company.id,
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

   @Transactional
   fun delete(id: UUID, company: CompanyEntity) {
      logger.debug("Deleting GeneralLedgerReversal with id={}", id)

      val rowsAffected = jdbc.softDelete(
         """
         UPDATE general_ledger_reversal
         SET deleted = TRUE
         WHERE id = :id AND company_id = :company_id AND deleted = FALSE
         """,
         mapOf("id" to id, "company_id" to company.id),
         "general_ledger_reversal"
      )
      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) throw NotFoundException(id)
   }

   fun mapRow(rs: ResultSet, company: CompanyEntity, columnPrefix: String = EMPTY): GeneralLedgerReversalEntity {
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

   private fun mapRow(
      rs: ResultSet,
      entity: GeneralLedgerReversalEntity,
      columnPrefix: String = EMPTY
   ): GeneralLedgerReversalEntity {
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

   fun findPendingJournalReversalEntriesForCurrentFiscalYear(company: CompanyEntity, from: LocalDate, thru: LocalDate): Int {
      return jdbc.queryForObject(
         """
            SELECT COUNT(*)
            FROM general_ledger_reversal reversal
               JOIN company comp ON reversal.company_id = comp.id AND comp.deleted = FALSE
            WHERE reversal.company_id = :comp_id
                  AND reversal.reversal_date BETWEEN :from AND :thru
                  AND reversal.deleted = FALSE
         """,
         mapOf("comp_id" to company.id, "from" to from, "thru" to thru),
         Int::class.java
      )
   }
}
