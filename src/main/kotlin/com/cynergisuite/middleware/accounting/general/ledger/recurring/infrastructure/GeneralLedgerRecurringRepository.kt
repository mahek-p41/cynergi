package com.cynergisuite.middleware.accounting.account.payable.recurring.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringEntity
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringType
import com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure.GeneralLedgerRecurringTypeRepository
import com.cynergisuite.middleware.company.Company
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class GeneralLedgerRecurringRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val sourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val recurringTypeRepository: GeneralLedgerRecurringTypeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerRecurringRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         SELECT
            glRecurring.id                                        AS glRecurring_id,
            glRecurring.company_id                                AS glRecurring_company_id,
            glRecurring.reverse_indicator                         AS glRecurring_reverse_indicator,
            glRecurring.message                                   AS glRecurring_message,
            glRecurring.begin_date                                AS glRecurring_begin_date,
            glRecurring.end_date                                  AS glRecurring_end_date,
            source.id                                             AS glRecurring_source_id,
            source.value                                          AS glRecurring_source_value,
            source.description                                    AS glRecurring_source_description,
            type.id                                               AS glRecurring_type_id,
            type.value                                            AS glRecurring_type_value,
            type.description                                      AS glRecurring_type_description,
            type.localization_code                                AS glRecurring_type_localization_code,
            count(*) OVER() AS total_elements
         FROM general_ledger_recurring glRecurring
            JOIN general_ledger_source_codes source ON glRecurring.source_id = source.id
            JOIN general_ledger_recurring_type_domain type ON glRecurring.type_id = type.id
      """
   }

   fun findOne(id: Long, company: Company): GeneralLedgerRecurringEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${selectBaseQuery()} WHERE glRecurring.id = :id AND glRecurring.company_id = :comp_id"
      val found = jdbc.findFirstOrNull(
         query, params,
         RowMapper { rs, _ ->
            mapRow(
               rs,
               "glRecurring_"
            )
         }
      )

      logger.trace("Searching for GeneralLedgerRecurring: {} resulted in {}", company, found)

      return found
   }

   fun findAll(company: Company, page: PageRequest): RepositoryPage<GeneralLedgerRecurringEntity, PageRequest> {
      return jdbc.queryPaged(
         """
            ${selectBaseQuery()}
            WHERE glRecurring.company_id = :comp_id
            ORDER BY glRecurring_${page.snakeSortBy()} ${page.sortDirection()}
            LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "comp_id" to company.myId(),
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
   fun insert(entity: GeneralLedgerRecurringEntity, company: Company): GeneralLedgerRecurringEntity {
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
            "company_id" to company.myId(),
            "reverse_indicator" to entity.reverseIndicator,
            "message" to entity.message,
            "type_id" to entity.type.id,
            "source_id" to entity.source.id,
            "begin_date" to entity.beginDate,
            "end_date" to entity.endDate
         ),
         RowMapper { rs, _ ->
            mapRowUpsert(rs, entity.type, entity.source)
         }
      )
   }

   @Transactional
   fun update(entity: GeneralLedgerRecurringEntity, company: Company): GeneralLedgerRecurringEntity {
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
            "company_id" to company.myId(),
            "reverse_indicator" to entity.reverseIndicator,
            "message" to entity.message,
            "type_id" to entity.type.id,
            "source_id" to entity.source.id,
            "begin_date" to entity.beginDate,
            "end_date" to entity.endDate
         ),
         RowMapper { rs, _ ->
            mapRowUpsert(rs, entity.type, entity.source)
         }
      )
   }

   private fun mapRow(
      rs: ResultSet,
      columnPrefix: String = EMPTY
   ): GeneralLedgerRecurringEntity {
      return GeneralLedgerRecurringEntity(
         id = rs.getLong("${columnPrefix}id"),
         reverseIndicator = rs.getBoolean("${columnPrefix}reverse_indicator"),
         message = rs.getString("${columnPrefix}message"),
         type = recurringTypeRepository.mapRow(rs, "${columnPrefix}type_"),
         source = sourceCodeRepository.mapRow(rs, "${columnPrefix}source_"),
         beginDate = rs.getLocalDateOrNull("${columnPrefix}begin_date"),
         endDate = rs.getLocalDateOrNull("${columnPrefix}end_date")
      )
   }

   private fun mapRowUpsert(
      rs: ResultSet,
      type: GeneralLedgerRecurringType,
      source: GeneralLedgerSourceCodeEntity,
      columnPrefix: String = EMPTY
   ): GeneralLedgerRecurringEntity {
      return GeneralLedgerRecurringEntity(
         id = rs.getLong("${columnPrefix}id"),
         reverseIndicator = rs.getBoolean("${columnPrefix}reverse_indicator"),
         message = rs.getString("${columnPrefix}message"),
         type = type,
         source = source,
         beginDate = rs.getLocalDateOrNull("${columnPrefix}begin_date"),
         endDate = rs.getLocalDateOrNull("${columnPrefix}end_date")
      )
   }
}
