package com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryFullList
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.update
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.GeneralLedgerReversalDistributionEntity
import com.cynergisuite.middleware.accounting.general.ledger.reversal.infrastructure.GeneralLedgerReversalRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class GeneralLedgerReversalDistributionRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val accountRepository: AccountRepository,
   private val generalLedgerReversalRepository: GeneralLedgerReversalRepository,
   private val storeRepository: StoreRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerReversalDistributionRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         WITH general_ledger_reversal AS (
            ${generalLedgerReversalRepository.selectBaseQuery()}
         ),
         account AS (
            ${accountRepository.selectBaseQuery()}
         )
         SELECT
            glReversalDist.id                                                AS glReversalDist_id,
            glReversalDist.general_ledger_reversal_distribution_account_id   AS glReversalDist_account_id,
            glReversalDist.general_ledger_reversal_distribution_amount       AS glReversalDist_general_ledger_reversal_distribution_amount,
            glReversalDist.deleted                                           AS glReversalDist_deleted,
            glReversal.glReversal_id                                         AS glReversalDist_glReversal_id,
            glReversal.glReversal_company_id                                 AS glReversalDist_glReversal_company_id,
            glReversal.glReversal_date                                       AS glReversalDist_glReversal_date,
            glReversal.glReversal_reversal_date                              AS glReversalDist_glReversal_reversal_date,
            glReversal.glReversal_comment                                    AS glReversalDist_glReversal_comment,
            glReversal.glReversal_entry_month                                AS glReversalDist_glReversal_entry_month,
            glReversal.glReversal_entry_number                               AS glReversalDist_glReversal_entry_number,
            glReversal.glReversal_source_id                                  AS glReversalDist_glReversal_source_id,
            glReversal.glReversal_source_value                               AS glReversalDist_glReversal_source_value,
            glReversal.glReversal_source_description                         AS glReversalDist_glReversal_source_description,
            account.account_id                                               AS glReversalDist_account_id,
            account.account_number                                           AS glReversalDist_account_number,
            account.account_name                                             AS glReversalDist_account_name,
            account.account_form_1099_field                                  AS glReversalDist_account_form_1099_field,
            account.account_corporate_account_indicator                      AS glReversalDist_account_corporate_account_indicator,
            account.account_comp_id                                          AS glReversalDist_account_comp_id,
            account.account_deleted                                          AS glReversalDist_account_deleted,
            account.account_type_id                                          AS glReversalDist_account_type_id,
            account.account_type_value                                       AS glReversalDist_account_type_value,
            account.account_type_description                                 AS glReversalDist_account_type_description,
            account.account_type_localization_code                           AS glReversalDist_account_type_localization_code,
            account.account_balance_type_id                                  AS glReversalDist_account_balance_type_id,
            account.account_balance_type_value                               AS glReversalDist_account_balance_type_value,
            account.account_balance_type_description                         AS glReversalDist_account_balance_type_description,
            account.account_balance_type_localization_code                   AS glReversalDist_account_balance_type_localization_code,
            account.account_status_id                                        AS glReversalDist_account_status_id,
            account.account_status_value                                     AS glReversalDist_account_status_value,
            account.account_status_description                               AS glReversalDist_account_status_description,
            account.account_status_localization_code                         AS glReversalDist_account_status_localization_code,
            account.account_vendor_1099_type_id                              AS glReversalDist_account_vendor_1099_type_id,
            account.account_vendor_1099_type_value                           AS glReversalDist_account_vendor_1099_type_value,
            account.account_vendor_1099_type_description                     AS glReversalDist_account_vendor_1099_type_description,
            account.account_vendor_1099_type_localization_code               AS glReversalDist_account_vendor_1099_type_localization_code,
            bank.id                                                          AS glReversalDist_account_bank_id,
            profitCenter.id                                                  AS glReversalDist_profitCenter_id,
            profitCenter.number                                              AS glReversalDist_profitCenter_number,
            profitCenter.name                                                AS glReversalDist_profitCenter_name,
            profitCenter.dataset                                             AS glReversalDist_profitCenter_dataset,
            count(*) OVER() AS total_elements
         FROM general_ledger_reversal_distribution glReversalDist
            JOIN general_ledger_reversal glReversal ON glReversalDist.general_ledger_reversal_id = glReversal.glReversal_id AND glReversal.glReversal_deleted = FALSE
            JOIN account ON glReversalDist.general_ledger_reversal_distribution_account_id = account.account_id AND account.account_deleted = FALSE
            JOIN company comp ON glReversal.glReversal_company_id = comp.id
            JOIN fastinfo_prod_import.store_vw profitCenter
               ON profitCenter.dataset = comp.dataset_code
                  AND profitCenter.number = glReversalDist.general_ledger_reversal_distribution_profit_center_id_sfk
            LEFT OUTER JOIN bank ON bank.general_ledger_account_id = account.account_id AND bank.deleted = FALSE
      """
   }

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): GeneralLedgerReversalDistributionEntity? {
      val params = mutableMapOf<String, Any?>("id" to id)
      val query = "${selectBaseQuery()} WHERE glReversalDist.id = :id AND glReversalDist.deleted = FALSE"
      val found = jdbc.findFirstOrNull(
         query, params
      ) { rs, _ ->
         mapRow(rs, company, "glReversalDist_")
      }

      logger.trace("Searching for GeneralLedgerReversalDistribution: resulted in {}", found)

      return found
   }

   @ReadOnly
   fun findAll(
      company: CompanyEntity,
      page: PageRequest
   ): RepositoryPage<GeneralLedgerReversalDistributionEntity, PageRequest> {
      return jdbc.queryPaged(
         """
            ${selectBaseQuery()}
            WHERE glReversalDist.deleted = FALSE
            ORDER BY glReversalDist_${page.snakeSortBy()} ${page.sortDirection()}
            LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "limit" to page.size(),
            "offset" to page.offset()
         ),
         page
      ) { rs, elements ->
         do {
            elements.add(mapRow(rs, company, "glReversalDist_"))
         } while (rs.next())
      }
   }

   @ReadOnly
   fun findAllByReversalId(
      glReversalId: UUID,
      company: CompanyEntity,
      page: PageRequest
   ): RepositoryPage<GeneralLedgerReversalDistributionEntity, PageRequest> {
      return jdbc.queryPaged(
         """
            ${selectBaseQuery()}
            WHERE glReversalDist.general_ledger_reversal_id = :glReversalId AND glReversalDist.deleted = FALSE
            ORDER BY glReversalDist_${page.snakeSortBy()} ${page.sortDirection()}
            LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "glReversalId" to glReversalId,
            "limit" to page.size(),
            "offset" to page.offset()
         ),
         page
      ) { rs, elements ->
         do {
            elements.add(mapRow(rs, company, "glReversalDist_"))
         } while (rs.next())
      }
   }

   @ReadOnly
   fun findAllByReversalId(glReversalId: UUID, company: CompanyEntity): List<GeneralLedgerReversalDistributionEntity> {
      return jdbc.queryFullList(
         """
            ${selectBaseQuery()}
            WHERE glReversalDist.general_ledger_reversal_id = :glReversalId AND glReversalDist.deleted = FALSE
         """.trimIndent(),
         mapOf(
            "glReversalId" to glReversalId
         )
      ) { rs, _, elements ->
         do {
            elements.add(mapRow(rs, company, "glReversalDist_"))
         } while (rs.next())
      }
   }

   fun upsert(generalLedgerReversalDistribution: GeneralLedgerReversalDistributionEntity): GeneralLedgerReversalDistributionEntity =
      if (generalLedgerReversalDistribution.id == null) {
         insert(generalLedgerReversalDistribution)
      } else {
         update(generalLedgerReversalDistribution)
      }

   @Transactional
   fun insert(entity: GeneralLedgerReversalDistributionEntity): GeneralLedgerReversalDistributionEntity {
      logger.debug("Inserting general_ledger_reversal_distribution")

      return jdbc.insertReturning(
         """
         INSERT INTO general_ledger_reversal_distribution (
            general_ledger_reversal_id,
            general_ledger_reversal_distribution_account_id,
            general_ledger_reversal_distribution_profit_center_id_sfk,
            general_ledger_reversal_distribution_amount
         )
         VALUES (
            :general_ledger_reversal_id,
            :general_ledger_reversal_distribution_account_id,
            :general_ledger_reversal_distribution_profit_center_id_sfk,
            :general_ledger_reversal_distribution_amount
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "general_ledger_reversal_id" to entity.generalLedgerReversal.id,
            "general_ledger_reversal_distribution_account_id" to entity.generalLedgerReversalDistributionAccount.myId(),
            "general_ledger_reversal_distribution_profit_center_id_sfk" to entity.generalLedgerReversalDistributionProfitCenter.myNumber(),
            "general_ledger_reversal_distribution_amount" to entity.generalLedgerReversalDistributionAmount
         )
      ) { rs, _ ->
         mapRow(rs, entity)
      }
   }

   @Transactional
   fun update(entity: GeneralLedgerReversalDistributionEntity): GeneralLedgerReversalDistributionEntity {
      logger.debug("Updating general_ledger_reversal_distribution {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE general_ledger_reversal_distribution
         SET
            general_ledger_reversal_id = :general_ledger_reversal_id,
            general_ledger_reversal_distribution_account_id = :general_ledger_reversal_distribution_account_id,
            general_ledger_reversal_distribution_profit_center_id_sfk = :general_ledger_reversal_distribution_profit_center_id_sfk,
            general_ledger_reversal_distribution_amount = :general_ledger_reversal_distribution_amount
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "general_ledger_reversal_id" to entity.generalLedgerReversal.id,
            "general_ledger_reversal_distribution_account_id" to entity.generalLedgerReversalDistributionAccount.myId(),
            "general_ledger_reversal_distribution_profit_center_id_sfk" to entity.generalLedgerReversalDistributionProfitCenter.myNumber(),
            "general_ledger_reversal_distribution_amount" to entity.generalLedgerReversalDistributionAmount
         ),
      ) { rs, _ ->
         mapRow(rs, entity)
      }
   }

   @Transactional
   fun delete(id: UUID) {
      logger.debug("Deleting GeneralLedgerReversalDistribution with id={}", id)
      val rowsAffected = jdbc.update(
         """
         UPDATE general_ledger_reversal_distribution
         SET deleted = TRUE
         WHERE id = :id AND deleted = FALSE
         """,
         mapOf("id" to id)
      )
      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) throw NotFoundException(id)
   }

   @Transactional
   fun deleteByReversalId(id: UUID) {
      logger.debug("Deleting all GeneralLedgerReversalDistributions with id={}", id)
      val rowsAffected = jdbc.update(
         """
            UPDATE general_ledger_reversal_distribution
            SET deleted = TRUE
            WHERE general_ledger_reversal_id = :id AND deleted = FALSE
         """,
         mapOf("id" to id)
      )
      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) throw NotFoundException(id)
   }

   @Transactional
   fun deleteNotIn(generalLedgerReversalId: UUID, distributions: List<GeneralLedgerReversalDistributionEntity>) {
      val result = mutableListOf<GeneralLedgerReversalDistributionEntity>()

      jdbc.update(
         """
         UPDATE general_ledger_reversal_distribution
         SET deleted = TRUE
         WHERE general_ledger_reversal_id = :general_ledger_reversal_id
               AND id NOT IN(<ids>)
               AND deleted = FALSE
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "general_ledger_reversal_id" to generalLedgerReversalId,
            "ids" to distributions.asSequence().map { it.id }.toList()
         )
      )
   }

   private fun mapRow(
      rs: ResultSet,
      company: CompanyEntity,
      columnPrefix: String = EMPTY
   ): GeneralLedgerReversalDistributionEntity {
      return GeneralLedgerReversalDistributionEntity(
         id = rs.getUuid("${columnPrefix}id"),
         generalLedgerReversal = generalLedgerReversalRepository.mapRow(rs, company, "${columnPrefix}glReversal_"),
         generalLedgerReversalDistributionAccount = accountRepository.mapRow(rs, company, "${columnPrefix}account_"),
         generalLedgerReversalDistributionProfitCenter = storeRepository.mapRow(rs, company, "${columnPrefix}profitCenter_"),
         generalLedgerReversalDistributionAmount = rs.getBigDecimal("${columnPrefix}general_ledger_reversal_distribution_amount")
      )
   }

   private fun mapRow(
      rs: ResultSet,
      entity: GeneralLedgerReversalDistributionEntity,
      columnPrefix: String = EMPTY
   ): GeneralLedgerReversalDistributionEntity {
      return GeneralLedgerReversalDistributionEntity(
         id = rs.getUuid("${columnPrefix}id"),
         generalLedgerReversal = entity.generalLedgerReversal,
         generalLedgerReversalDistributionAccount = entity.generalLedgerReversalDistributionAccount,
         generalLedgerReversalDistributionProfitCenter = entity.generalLedgerReversalDistributionProfitCenter,
         generalLedgerReversalDistributionAmount = rs.getBigDecimal("${columnPrefix}general_ledger_reversal_distribution_amount")
      )
   }
}
