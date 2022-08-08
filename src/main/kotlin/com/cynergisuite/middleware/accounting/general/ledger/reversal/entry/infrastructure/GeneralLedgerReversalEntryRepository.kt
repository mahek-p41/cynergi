package com.cynergisuite.middleware.accounting.general.ledger.reversal.entry.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.GeneralLedgerReversalDistributionEntity
import com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.infrastructure.GeneralLedgerReversalDistributionRepository
import com.cynergisuite.middleware.accounting.general.ledger.reversal.entry.GeneralLedgerReversalEntryEntity
import com.cynergisuite.middleware.accounting.general.ledger.reversal.infrastructure.GeneralLedgerReversalRepository
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.sql.ResultSet
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class GeneralLedgerReversalEntryRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val accountRepository: AccountRepository,
   private val generalLedgerReversalDistributionRepository: GeneralLedgerReversalDistributionRepository,
   private val generalLedgerReversalRepository: GeneralLedgerReversalRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerReversalEntryRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         WITH general_ledger_reversal AS (
            ${generalLedgerReversalRepository.selectBaseQuery()}
         ),
         account AS (
            ${accountRepository.selectBaseQuery()}
         )
         SELECT
            glReversal.glReversal_id                                                  AS glReversal_id,
            glReversal.glReversal_company_id                                          AS glReversal_company_id,
            glReversal.glReversal_date                                                AS glReversal_date,
            glReversal.glReversal_reversal_date                                       AS glReversal_reversal_date,
            glReversal.glReversal_comment                                             AS glReversal_comment,
            glReversal.glReversal_entry_month                                         AS glReversal_entry_month,
            glReversal.glReversal_entry_number                                        AS glReversal_entry_number,
            glReversal.glReversal_deleted                                             AS glReversal_deleted,
            glReversal.glReversal_source_id                                           AS glReversal_source_id,
            glReversal.glReversal_source_company_id                                   AS glReversal_source_company_id,
            glReversal.glReversal_source_value                                        AS glReversal_source_value,
            glReversal.glReversal_source_description                                  AS glReversal_source_description,
            glReversalDist.id                                                         AS glReversalDist_id,
            glReversalDist.general_ledger_reversal_distribution_profit_center_id_sfk  AS glReversalDist_profit_center_id_sfk,
            glReversalDist.general_ledger_reversal_distribution_amount                AS glReversalDist_general_ledger_reversal_distribution_amount,
            account.account_id                                                        AS glReversalDist_account_id,
            account.account_number                                                    AS glReversalDist_account_number,
            account.account_name                                                      AS glReversalDist_account_name,
            account.account_form_1099_field                                           AS glReversalDist_account_form_1099_field,
            account.account_corporate_account_indicator                               AS glReversalDist_account_corporate_account_indicator,
            account.account_comp_id                                                   AS glReversalDist_account_comp_id,
            account.account_deleted                                                   AS glReversalDist_account_deleted,
            account.account_type_id                                                   AS glReversalDist_account_type_id,
            account.account_type_value                                                AS glReversalDist_account_type_value,
            account.account_type_description                                          AS glReversalDist_account_type_description,
            account.account_type_localization_code                                    AS glReversalDist_account_type_localization_code,
            account.account_balance_type_id                                           AS glReversalDist_account_balance_type_id,
            account.account_balance_type_value                                        AS glReversalDist_account_balance_type_value,
            account.account_balance_type_description                                  AS glReversalDist_account_balance_type_description,
            account.account_balance_type_localization_code                            AS glReversalDist_account_balance_type_localization_code,
            account.account_status_id                                                 AS glReversalDist_account_status_id,
            account.account_status_value                                              AS glReversalDist_account_status_value,
            account.account_status_description                                        AS glReversalDist_account_status_description,
            account.account_status_localization_code                                  AS glReversalDist_account_status_localization_code,
            account.account_vendor_1099_type_id                                       AS glReversalDist_account_vendor_1099_type_id,
            account.account_vendor_1099_type_value                                    AS glReversalDist_account_vendor_1099_type_value,
            account.account_vendor_1099_type_description                              AS glReversalDist_account_vendor_1099_type_description,
            account.account_vendor_1099_type_localization_code                        AS glReversalDist_account_vendor_1099_type_localization_code,
            bank.id                                                                   AS glReversalDist_account_bank_id,
            count(*) OVER() AS total_elements
         FROM general_ledger_reversal_distribution glReversalDist
            JOIN general_ledger_reversal glReversal ON glReversalDist.general_ledger_reversal_id = glReversal.glReversal_id AND glReversal.glReversal_deleted = FALSE
            JOIN account ON glReversalDist.general_ledger_reversal_distribution_account_id = account.account_id AND account.account_deleted = FALSE
            LEFT OUTER JOIN bank ON bank.general_ledger_account_id = account.account_id AND bank.deleted = FALSE
      """
   }

   @ReadOnly
   fun findOne(generalLedgerReversalId: UUID, company: CompanyEntity): GeneralLedgerReversalEntryEntity? {
      val generalLedgerReversal = generalLedgerReversalRepository.findOne(generalLedgerReversalId, company)
      val generalLedgerReversalDistributions = generalLedgerReversalDistributionRepository.findAllByReversalId(generalLedgerReversalId, company)

      var generalLedgerReversalEntryEntity: GeneralLedgerReversalEntryEntity? = null
      if (generalLedgerReversal != null) {
         generalLedgerReversalEntryEntity = GeneralLedgerReversalEntryEntity(
            generalLedgerReversal,
            generalLedgerReversalDistributions as MutableList<GeneralLedgerReversalDistributionEntity>
         )
      }

      logger.trace("Searching for GeneralLedgerReversalEntry: resulted in {}", generalLedgerReversalEntryEntity)

      return generalLedgerReversalEntryEntity
   }

   @ReadOnly
   fun findAll(company: CompanyEntity, page: PageRequest): RepositoryPage<GeneralLedgerReversalEntryEntity, PageRequest> {
      val repoPage: RepositoryPage<GeneralLedgerReversalEntryEntity, PageRequest> = jdbc.queryPaged(
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
            elements.add(mapRow(rs, company))
         } while (rs.next())
      }

      return RepositoryPage(repoPage.elements.distinct(), repoPage.elements.distinct().size.toLong(), repoPage.requested)
   }

   @Transactional
   fun insert(company: CompanyEntity, entity: GeneralLedgerReversalEntryEntity): GeneralLedgerReversalEntryEntity {
      logger.debug("Inserting general_ledger_reversal and general_ledger_reversal_distribution")

      val glReversalEntity = generalLedgerReversalRepository.insert(entity.generalLedgerReversal, company)

      val list = entity.generalLedgerReversalDistributions.map {
         it.generalLedgerReversal = glReversalEntity
         generalLedgerReversalDistributionRepository.insert(it)
      }.toList()

      entity.generalLedgerReversal = glReversalEntity
      entity.generalLedgerReversalDistributions = list

      return entity
   }

   @Transactional
   fun update(company: CompanyEntity, entity: GeneralLedgerReversalEntryEntity): GeneralLedgerReversalEntryEntity {
      logger.debug("Updating GeneralLedgerReversalEntry {}", entity)

      val existing = findOne(entity.generalLedgerReversal.id!!, company)

      generalLedgerReversalDistributionRepository.deleteNotIn(entity.generalLedgerReversal.id!!, entity.generalLedgerReversalDistributions)

      entity.generalLedgerReversal = generalLedgerReversalRepository.update(entity.generalLedgerReversal, company)

      // loop through each distribution to update or delete it in general_ledger_reversal_distribution
      entity.generalLedgerReversalDistributions.map { generalLedgerReversalDistributionRepository.upsert(it) }

      logger.debug("Updated GeneralLedgerReversalEntry {}", entity)

      return entity
   }

   @Transactional
   fun delete(generalLedgerReversalId: UUID, company: CompanyEntity) {
      logger.debug("Deleting GeneralLedgerReversalEntry with GL Reversal id={}", generalLedgerReversalId)

      val glReversalDistributions = generalLedgerReversalDistributionRepository.findAllByReversalId(generalLedgerReversalId, company)
      glReversalDistributions.forEach {
         it.id?.let { glReversalDistId -> generalLedgerReversalDistributionRepository.delete(glReversalDistId) }
      }

      generalLedgerReversalRepository.delete(generalLedgerReversalId, company)
   }

   private fun mapRow(rs: ResultSet, company: CompanyEntity): GeneralLedgerReversalEntryEntity {
      val glReversal = generalLedgerReversalRepository.mapRow(rs, company, "glReversal_")
      val glReversalDistributions = glReversal.id?.let { generalLedgerReversalDistributionRepository.findAllByReversalId(it, company) }

      return GeneralLedgerReversalEntryEntity(
         generalLedgerReversal = glReversal,
         generalLedgerReversalDistributions = glReversalDistributions!!,
         balance = BigDecimal.ZERO
      )
   }
}
