package com.cynergisuite.middleware.accounting.general.ledger.recurring.entries.infrastructure

import com.cynergisuite.domain.GeneralLedgerRecurringEntriesFilterRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.GeneralLedgerRecurringDistributionEntity
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.infrastructure.GeneralLedgerRecurringDistributionRepository
import com.cynergisuite.middleware.accounting.general.ledger.recurring.entries.GeneralLedgerRecurringEntriesEntity
import com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure.GeneralLedgerRecurringRepository
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class GeneralLedgerRecurringEntriesRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val accountRepository: AccountRepository,
   private val generalLedgerRecurringDistributionRepository: GeneralLedgerRecurringDistributionRepository,
   private val generalLedgerRecurringRepository: GeneralLedgerRecurringRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerRecurringEntriesRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         WITH general_ledger_recurring AS (
            ${generalLedgerRecurringRepository.selectBaseQuery()}
         ),
         account AS (
            ${accountRepository.selectBaseQuery()}
         )
         SELECT
            glRecurring.glRecurring_id                                        AS glRecurring_id,
            glRecurring.glRecurring_company_id                                AS glRecurring_company_id,
            glRecurring.glRecurring_reverse_indicator                         AS glRecurring_reverse_indicator,
            glRecurring.glRecurring_message                                   AS glRecurring_message,
            glRecurring.glRecurring_begin_date                                AS glRecurring_begin_date,
            glRecurring.glRecurring_end_date                                  AS glRecurring_end_date,
            glRecurring.glRecurring_last_transfer_date                        AS glRecurring_last_transfer_date,
            glRecurring.glRecurring_source_id                                 AS glRecurring_source_id,
            glRecurring.glRecurring_source_value                              AS glRecurring_source_value,
            glRecurring.glRecurring_source_description                        AS glRecurring_source_description,
            glRecurring.glRecurring_type_id                                   AS glRecurring_type_id,
            glRecurring.glRecurring_type_value                                AS glRecurring_type_value,
            glRecurring.glRecurring_type_description                          AS glRecurring_type_description,
            glRecurring.glRecurring_type_localization_code                    AS glRecurring_type_localization_code,
            glRecurringDist.id                                                AS glRecurringDist_id,
            glRecurringDist.general_ledger_distribution_profit_center_id_sfk  AS glRecurringDist_profit_center_id_sfk,
            glRecurringDist.general_ledger_distribution_amount                AS glRecurringDist_general_ledger_distribution_amount,
            account.account_id                                                AS glRecurringDist_account_id,
            account.account_number                                            AS glRecurringDist_account_number,
            account.account_name                                              AS glRecurringDist_account_name,
            account.account_form_1099_field                                   AS glRecurringDist_account_form_1099_field,
            account.account_corporate_account_indicator                       AS glRecurringDist_account_corporate_account_indicator,
            account.account_comp_id                                           AS glRecurringDist_account_comp_id,
            account.account_deleted                                           AS glRecurringDist_account_deleted,
            account.account_type_id                                           AS glRecurringDist_account_type_id,
            account.account_type_value                                        AS glRecurringDist_account_type_value,
            account.account_type_description                                  AS glRecurringDist_account_type_description,
            account.account_type_localization_code                            AS glRecurringDist_account_type_localization_code,
            account.account_balance_type_id                                   AS glRecurringDist_account_balance_type_id,
            account.account_balance_type_value                                AS glRecurringDist_account_balance_type_value,
            account.account_balance_type_description                          AS glRecurringDist_account_balance_type_description,
            account.account_balance_type_localization_code                    AS glRecurringDist_account_balance_type_localization_code,
            account.account_status_id                                         AS glRecurringDist_account_status_id,
            account.account_status_value                                      AS glRecurringDist_account_status_value,
            account.account_status_description                                AS glRecurringDist_account_status_description,
            account.account_status_localization_code                          AS glRecurringDist_account_status_localization_code,
            bank.id                                                           AS glRecurringDist_account_bank_id,
            count(*) OVER() AS total_elements
         FROM general_ledger_recurring_distribution glRecurringDist
            JOIN general_ledger_recurring glRecurring ON glRecurringDist.general_ledger_recurring_id = glRecurring.glRecurring_id AND glRecurring.glRecurring_deleted = FALSE
            JOIN account ON glRecurringDist.general_ledger_distribution_account_id = account.account_id AND account.account_deleted = FALSE
            LEFT OUTER JOIN bank ON bank.general_ledger_account_id = account.account_id AND bank.deleted = FALSE
      """
   }

   @ReadOnly
   fun findOne(generalLedgerRecurringId: UUID, company: CompanyEntity): GeneralLedgerRecurringEntriesEntity? {
      val generalLedgerRecurring = generalLedgerRecurringRepository.findOne(generalLedgerRecurringId, company)
      val generalLedgerRecurringDistributions = generalLedgerRecurringDistributionRepository.findAllByRecurringId(generalLedgerRecurringId, company)

      var generalLedgerRecurringEntriesEntity: GeneralLedgerRecurringEntriesEntity? = null
      if (generalLedgerRecurring != null) {
         generalLedgerRecurringEntriesEntity = GeneralLedgerRecurringEntriesEntity(
            generalLedgerRecurring,
            generalLedgerRecurringDistributions as MutableList<GeneralLedgerRecurringDistributionEntity>
         )
      }

      logger.trace("Searching for GeneralLedgerRecurringEntries: resulted in {}", generalLedgerRecurringEntriesEntity)

      return generalLedgerRecurringEntriesEntity
   }

   @ReadOnly
   fun findAll(company: CompanyEntity, filterRequest: GeneralLedgerRecurringEntriesFilterRequest): RepositoryPage<GeneralLedgerRecurringEntriesEntity, GeneralLedgerRecurringEntriesFilterRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "limit" to filterRequest.size(), "offset" to filterRequest.offset())
      val whereClause = StringBuilder(" WHERE glRecurring.glRecurring_company_id = :comp_id AND glRecurringDist.deleted = FALSE ")

      filterRequest.entryType?.let {
         params["entryType"] = filterRequest.entryType
         whereClause.append(" AND glRecurring.glRecurring_type_value = :entryType ")
      }

      filterRequest.sourceCode?.let {
         params["sourceCode"] = filterRequest.sourceCode
         whereClause.append(" AND glRecurring.glRecurring_source_value = :sourceCode ")
      }

      filterRequest.entryDate?.let {
         params["entryDate"] = filterRequest.entryDate
         whereClause.append("""
            AND (
               (:entryDate BETWEEN glRecurring.glRecurring_begin_date AND glRecurring.glRecurring_end_date)
               OR (:entryDate > glRecurring.glRecurring_begin_date AND glRecurring.glRecurring_end_date IS NULL)
            )
         """.trimMargin())
      }

      val repoPage: RepositoryPage<GeneralLedgerRecurringEntriesEntity, GeneralLedgerRecurringEntriesFilterRequest> = jdbc.queryPaged(
         """
            ${selectBaseQuery()}
            $whereClause
            ORDER BY glRecurringDist_id
            LIMIT :limit OFFSET :offset
         """.trimIndent(),
         params,
         filterRequest
      ) { rs, elements ->
         do {
            elements.add(mapRow(rs, company))
         } while (rs.next())
      }

      return RepositoryPage(repoPage.elements.distinct(), repoPage.elements.distinct().size.toLong(), repoPage.requested)
   }

   @Transactional
   fun insert(company: CompanyEntity, entity: GeneralLedgerRecurringEntriesEntity): GeneralLedgerRecurringEntriesEntity {
      logger.debug("Inserting general_ledger_recurring and general_ledger_recurring_distribution")

      val glRecurringEntity = generalLedgerRecurringRepository.insert(entity.generalLedgerRecurring, company)

      val list = entity.generalLedgerRecurringDistributions.map {
         it.generalLedgerRecurringId = glRecurringEntity.id
         generalLedgerRecurringDistributionRepository.insert(it)
      }.toList()

      entity.generalLedgerRecurring = glRecurringEntity
      entity.generalLedgerRecurringDistributions = list

      return entity
   }

   @Transactional
   fun update(company: CompanyEntity, entity: GeneralLedgerRecurringEntriesEntity): GeneralLedgerRecurringEntriesEntity {
      logger.debug("Updating GeneralLedgerRecurringEntries {}", entity)

      generalLedgerRecurringDistributionRepository.deleteNotIn(entity.generalLedgerRecurring.id!!, entity.generalLedgerRecurringDistributions)

      entity.generalLedgerRecurring = generalLedgerRecurringRepository.update(entity.generalLedgerRecurring, company)

      // loop through each distribution to update or delete it in general_ledger_recurring_distribution
      entity.generalLedgerRecurringDistributions.map { generalLedgerRecurringDistributionRepository.upsert(it) }

      logger.debug("Updated GeneralLedgerRecurringEntries {}", entity)

      return entity
   }

   @Transactional
   fun delete(generalLedgerRecurringId: UUID, company: CompanyEntity) {
      logger.debug("Deleting GeneralLedgerRecurringEntries with GL Recurring id={}", generalLedgerRecurringId)

      generalLedgerRecurringDistributionRepository.deleteByRecurringId(generalLedgerRecurringId)

      generalLedgerRecurringRepository.delete(generalLedgerRecurringId, company)
   }

   private fun mapRow(rs: ResultSet, company: CompanyEntity): GeneralLedgerRecurringEntriesEntity {
      val glRecurring = generalLedgerRecurringRepository.mapRow(rs, "glRecurring_")
      val glRecurringDistributions = glRecurring.id?.let { generalLedgerRecurringDistributionRepository.findAllByRecurringId(it, company) }

      return GeneralLedgerRecurringEntriesEntity(
         generalLedgerRecurring = glRecurring,
         generalLedgerRecurringDistributions = glRecurringDistributions!!,
      )
   }
}
