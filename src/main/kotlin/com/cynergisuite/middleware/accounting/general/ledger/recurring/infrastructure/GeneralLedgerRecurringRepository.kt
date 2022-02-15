package com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.softDelete
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringEntity
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringType
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class GeneralLedgerRecurringRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val sourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val recurringTypeRepository: GeneralLedgerRecurringTypeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerRecurringRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         SELECT
            glRecurring.id                                        AS glRecurring_id,
            glRecurring.company_id                                AS glRecurring_company_id,
            glRecurring.reverse_indicator                         AS glRecurring_reverse_indicator,
            glRecurring.message                                   AS glRecurring_message,
            glRecurring.begin_date                                AS glRecurring_begin_date,
            glRecurring.end_date                                  AS glRecurring_end_date,
            glRecurring.last_transfer_date                        AS glRecurring_last_transfer_date,
            glRecurring.deleted                                   AS glRecurring_deleted,
            source.id                                             AS glRecurring_source_id,
            source.value                                          AS glRecurring_source_value,
            source.description                                    AS glRecurring_source_description,
            type.id                                               AS glRecurring_type_id,
            type.value                                            AS glRecurring_type_value,
            type.description                                      AS glRecurring_type_description,
            type.localization_code                                AS glRecurring_type_localization_code,
            count(*) OVER() AS total_elements
         FROM general_ledger_recurring glRecurring
            JOIN general_ledger_source_codes source ON glRecurring.source_id = source.id AND source.deleted = FALSE
            JOIN general_ledger_recurring_type_domain type ON glRecurring.type_id = type.id
      """
   }

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): GeneralLedgerRecurringEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query = "${selectBaseQuery()} WHERE glRecurring.id = :id AND glRecurring.company_id = :comp_id AND glRecurring.deleted = FALSE"
      val found = jdbc.findFirstOrNull(
         query,
         params
      ) { rs, _ ->
         mapRow(
            rs,
            "glRecurring_"
         )
      }

      logger.trace("Searching for GeneralLedgerRecurring: {} resulted in {}", company, found)

      return found
   }

   @ReadOnly
   fun findAll(company: CompanyEntity, page: PageRequest): RepositoryPage<GeneralLedgerRecurringEntity, PageRequest> {
      return jdbc.queryPaged(
         """
            ${selectBaseQuery()}
            WHERE glRecurring.company_id = :comp_id AND glRecurring.deleted = FALSE
            ORDER BY glRecurring_${page.snakeSortBy()} ${page.sortDirection()}
            LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "limit" to page.size(),
            "offset" to page.offset()
         ),
         page
      ) { rs, elements ->
         do {
            elements.add(mapRow(rs, "glRecurring_"))
         } while (rs.next())
      }
   }

   @Transactional
   fun insert(entity: GeneralLedgerRecurringEntity, company: CompanyEntity): GeneralLedgerRecurringEntity {
      logger.debug("Inserting general_ledger_recurring {}", company)

      return jdbc.insertReturning(
         """
         INSERT INTO general_ledger_recurring (
            company_id,
            reverse_indicator,
            message,
            type_id,
            source_id,
            begin_date,
            end_date
         )
         VALUES (
            :company_id,
            :reverse_indicator,
            :message,
            :type_id,
            :source_id,
            :begin_date,
            :end_date
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.id,
            "reverse_indicator" to entity.reverseIndicator,
            "message" to entity.message,
            "type_id" to entity.type.id,
            "source_id" to entity.source.id,
            "begin_date" to entity.beginDate,
            "end_date" to entity.endDate
         )
      ) { rs, _ ->
         mapRowUpsert(rs, entity.type, entity.source)
      }
   }

   @Transactional
   fun update(entity: GeneralLedgerRecurringEntity, company: CompanyEntity): GeneralLedgerRecurringEntity {
      logger.debug("Updating general_ledger_recurring {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE general_ledger_recurring
         SET
            company_id = :company_id,
            reverse_indicator = :reverse_indicator,
            message = :message,
            type_id = :type_id,
            source_id = :source_id,
            begin_date = :begin_date,
            end_date = :end_date
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to company.id,
            "reverse_indicator" to entity.reverseIndicator,
            "message" to entity.message,
            "type_id" to entity.type.id,
            "source_id" to entity.source.id,
            "begin_date" to entity.beginDate,
            "end_date" to entity.endDate
         )
      ) { rs, _ ->
         mapRowUpsert(rs, entity.type, entity.source)
      }
   }

   @Transactional
   fun delete(id: UUID, company: CompanyEntity) {
      logger.debug("Deleting GeneralLedgerRecurring with id={}", id)

      val rowsAffected = jdbc.softDelete(
         """
         UPDATE general_ledger_recurring
         SET deleted = TRUE
         WHERE id = :id AND company_id = :company_id AND deleted = FALSE
         """,
         mapOf("id" to id, "company_id" to company.id),
         "general_ledger_recurring"
      )
      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) throw NotFoundException(id)
   }

   fun mapRow(
      rs: ResultSet,
      columnPrefix: String = EMPTY
   ): GeneralLedgerRecurringEntity {
      return GeneralLedgerRecurringEntity(
         id = rs.getUuid("${columnPrefix}id"),
         reverseIndicator = rs.getBoolean("${columnPrefix}reverse_indicator"),
         message = rs.getString("${columnPrefix}message"),
         type = recurringTypeRepository.mapRow(rs, "${columnPrefix}type_"),
         source = sourceCodeRepository.mapRow(rs, "${columnPrefix}source_"),
         beginDate = rs.getLocalDate("${columnPrefix}begin_date"),
         endDate = rs.getLocalDateOrNull("${columnPrefix}end_date"),
         lastTransferDate = rs.getLocalDateOrNull("${columnPrefix}last_transfer_date")
      )
   }

   private fun mapRowUpsert(
      rs: ResultSet,
      type: GeneralLedgerRecurringType,
      source: GeneralLedgerSourceCodeEntity,
      columnPrefix: String = EMPTY
   ): GeneralLedgerRecurringEntity {
      return GeneralLedgerRecurringEntity(
         id = rs.getUuid("${columnPrefix}id"),
         reverseIndicator = rs.getBoolean("${columnPrefix}reverse_indicator"),
         message = rs.getString("${columnPrefix}message"),
         type = type,
         source = source,
         beginDate = rs.getLocalDate("${columnPrefix}begin_date"),
         endDate = rs.getLocalDateOrNull("${columnPrefix}end_date"),
         lastTransferDate = rs.getLocalDateOrNull("${columnPrefix}last_transfer_date")
      )
   }
}
