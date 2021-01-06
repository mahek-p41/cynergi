package com.cynergisuite.middleware.accounting.general.ledger.reversal.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure.GeneralLedgerDetailRepository
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalEntity
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
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
class GeneralLedgerReversalRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val accountRepository: AccountRepository,
   private val generalLedgerDetailRepository: GeneralLedgerDetailRepository,
   private val generalLedgerSourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val storeRepository: StoreRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerReversalRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         WITH general_ledger_detail AS (
            ${generalLedgerDetailRepository.selectBaseQuery()}
         )
         SELECT
            glReversal.id                                                   AS glReversal_id,
            glReversal.uu_row_id                                            AS glReversal_uu_row_id,
            glReversal.time_created                                         AS glReversal_time_created,
            glReversal.time_updated                                         AS glReversal_time_updated,
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
            count(*) OVER()                                                 AS total_elements,
            glDetail.glDetail_id                                            AS glReversal_glDetail_id,
            glDetail.glDetail_company_id                                    AS glReversal_glDetail_company_id,
            glDetail.glDetail_profit_center_id_sfk                          AS glReversal_glDetail_profit_center_id_sfk,
            glDetail.glDetail_date                                          AS glReversal_glDetail_date,
            glDetail.glDetail_amount                                        AS glReversal_glDetail_amount,
            glDetail.glDetail_message                                       AS glReversal_glDetail_message,
            glDetail.glDetail_employee_number_id_sfk                        AS glReversal_glDetail_employee_number_id_sfk,
            glDetail.glDetail_journal_entry_number                          AS glReversal_glDetail_journal_entry_number,
            glDetail.acct_id                                                AS glReversal_glDetail_acct_id,
            glDetail.acct_number                                            AS glReversal_glDetail_acct_number,
            glDetail.acct_name                                              AS glReversal_glDetail_acct_name,
            glDetail.acct_form_1099_field                                   AS glReversal_glDetail_acct_form_1099_field,
            glDetail.acct_corporate_account_indicator                       AS glReversal_glDetail_acct_corporate_account_indicator,
            glDetail.acct_comp_id                                           AS glReversal_glDetail_acct_comp_id,
            glDetail.acct_type_id                                           AS glReversal_glDetail_acct_type_id,
            glDetail.acct_type_value                                        AS glReversal_glDetail_acct_type_value,
            glDetail.acct_type_description                                  AS glReversal_glDetail_acct_type_description,
            glDetail.acct_type_localization_code                            AS glReversal_glDetail_acct_type_localization_code,
            glDetail.acct_balance_type_id                                   AS glReversal_glDetail_acct_balance_type_id,
            glDetail.acct_balance_type_value                                AS glReversal_glDetail_acct_balance_type_value,
            glDetail.acct_balance_type_description                          AS glReversal_glDetail_acct_balance_type_description,
            glDetail.acct_balance_type_localization_code                    AS glReversal_glDetail_acct_balance_type_localization_code,
            glDetail.acct_status_id                                         AS glReversal_glDetail_acct_status_id,
            glDetail.acct_status_value                                      AS glReversal_glDetail_acct_status_value,
            glDetail.acct_status_description                                AS glReversal_glDetail_acct_status_description,
            glDetail.acct_status_localization_code                          AS glReversal_glDetail_acct_status_localization_code,
            glDetail.profitCenter_id                                        AS glReversal_glDetail_profitCenter_id,
            glDetail.profitCenter_number                                    AS glReversal_glDetail_profitCenter_number,
            glDetail.profitCenter_name                                      AS glReversal_glDetail_profitCenter_name,
            glDetail.profitCenter_dataset                                   AS glReversal_glDetail_profitCenter_dataset,
            glDetail.source_id                                              AS glReversal_glDetail_source_id,
            glDetail.source_company_id                                      AS glReversal_glDetail_source_company_id,
            glDetail.source_value                                           AS glReversal_glDetail_source_value,
            glDetail.source_description                                     AS glReversal_glDetail_source_description
         FROM general_ledger_reversal glReversal
            JOIN general_ledger_source_codes source ON glReversal.source_id = source.id
            JOIN general_ledger_detail glDetail ON glReversal.general_ledger_detail_id = glDetail.glDetail_id
      """
   }

   fun findOne(id: Long, company: Company): GeneralLedgerReversalEntity? {
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
      var totalElements: Long? = null
      val elements = mutableListOf<GeneralLedgerReversalEntity>()

      jdbc.query(
         """
         WITH paged AS (
            ${selectBaseQuery()}
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER BY ${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT ${pageRequest.size()}
            OFFSET ${pageRequest.offset()}
         """,
         emptyMap<String, Any>()
      ) { rs ->
         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }

         elements.add(mapRow(rs, company, "glReversal_"))
      }

      return RepositoryPage(
         requested = pageRequest,
         elements = elements,
         totalElements = totalElements ?: 0
      )
   }

   @Transactional
   fun insert(entity: GeneralLedgerReversalEntity, company: Company): GeneralLedgerReversalEntity {
      logger.debug("Inserting GeneralLedgerReversal {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO general_ledger_reversal(
            source_id,
            date,
            reversal_date,
            general_ledger_detail_id,
            comment,
            entry_month,
            entry_number
         )
         VALUES (
            :source_id,
            :date,
            :reversal_date,
            :general_ledger_detail_id,
            :comment,
            :entry_month,
            :entry_number
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "source_id" to entity.source.id,
            "date" to entity.date,
            "reversal_date" to entity.reversalDate,
            "general_ledger_detail_id" to entity.generalLedgerDetail.id,
            "comment" to entity.comment,
            "entry_month" to entity.entryMonth,
            "entry_number" to entity.entryNumber
         ),
         RowMapper { rs, _ -> mapRow(rs, entity) }
      )
   }

   @Transactional
   fun update(entity: GeneralLedgerReversalEntity, company: Company): GeneralLedgerReversalEntity {
      logger.debug("Updating GeneralLedgerReversal {}", entity)

      val updated = jdbc.updateReturning(
         """
         UPDATE general_ledger_reversal
         SET
            source_id = :source_id,
            date = :date,
            reversal_date = :reversal_date,
            general_ledger_detail_id = :general_ledger_detail_id,
            comment = :comment,
            entry_month = :entry_month,
            entry_number = :entry_number
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "source_id" to entity.source.id,
            "date" to entity.date,
            "reversal_date" to entity.reversalDate,
            "general_ledger_detail_id" to entity.generalLedgerDetail.id,
            "comment" to entity.comment,
            "entry_month" to entity.entryMonth,
            "entry_number" to entity.entryNumber
         ),
         RowMapper { rs, _ -> mapRow(rs, entity) }
      )

      logger.debug("Updated GeneralLedgerReversal {}", updated)

      return updated
   }

   fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): GeneralLedgerReversalEntity {
      val account = accountRepository.mapRow(rs, company, "${columnPrefix}glDetail_acct_")
      val profitCenter = storeRepository.mapRow(rs, company, "${columnPrefix}glDetail_profitCenter_")
      val sourceCode = generalLedgerSourceCodeRepository.mapRow(rs, "${columnPrefix}glDetail_source_")

      return GeneralLedgerReversalEntity(
         id = rs.getLong("${columnPrefix}id"),
         source = generalLedgerSourceCodeRepository.mapRow(rs, "${columnPrefix}source_"),
         date = rs.getLocalDate("${columnPrefix}date"),
         reversalDate = rs.getLocalDate("${columnPrefix}reversal_date"),
         generalLedgerDetail = generalLedgerDetailRepository.mapRow(rs, account, profitCenter, sourceCode, "${columnPrefix}glDetail_"),
         comment = rs.getString("${columnPrefix}comment"),
         entryMonth = rs.getInt("${columnPrefix}entry_month"),
         entryNumber = rs.getInt("${columnPrefix}entry_number")
      )
   }

   private fun mapRow(rs: ResultSet, entity: GeneralLedgerReversalEntity, columnPrefix: String = EMPTY): GeneralLedgerReversalEntity {
      return GeneralLedgerReversalEntity(
         id = rs.getLong("${columnPrefix}id"),
         source = entity.source,
         date = rs.getLocalDate("${columnPrefix}date"),
         reversalDate = rs.getLocalDate("${columnPrefix}reversal_date"),
         generalLedgerDetail = entity.generalLedgerDetail,
         comment = rs.getString("${columnPrefix}comment"),
         entryMonth = rs.getInt("${columnPrefix}entry_month"),
         entryNumber = rs.getInt("${columnPrefix}entry_number")
      )
   }
}
