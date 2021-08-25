package com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleLegacyIdentifiableEntity
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.update
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.GeneralLedgerRecurringDistributionEntity
import com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure.GeneralLedgerRecurringRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class GeneralLedgerRecurringDistributionRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val accountRepository: AccountRepository,
   private val generalLedgerRecurringRepository: GeneralLedgerRecurringRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerRecurringDistributionRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         WITH general_ledger_recurring AS (
            ${generalLedgerRecurringRepository.selectBaseQuery()}
         ),
         account AS (
            ${accountRepository.selectBaseQuery()}
         )
         SELECT
            glRecurringDist.id                                                AS glRecurringDist_id,
            glRecurringDist.general_ledger_distribution_account_id            AS glRecurringDist_account_id,
            glRecurringDist.general_ledger_distribution_profit_center_id_sfk  AS glRecurringDist_profit_center_id_sfk,
            glRecurringDist.general_ledger_distribution_amount                AS glRecurringDist_general_ledger_distribution_amount,
            glRecurringDist.deleted                                           AS glRecurringDist_deleted,
            glRecurring.glRecurring_id                                        AS glRecurringDist_glRecurring_id,
            glRecurring.glRecurring_company_id                                AS glRecurringDist_glRecurring_company_id,
            glRecurring.glRecurring_reverse_indicator                         AS glRecurringDist_glRecurring_reverse_indicator,
            glRecurring.glRecurring_message                                   AS glRecurringDist_glRecurring_message,
            glRecurring.glRecurring_begin_date                                AS glRecurringDist_glRecurring_begin_date,
            glRecurring.glRecurring_end_date                                  AS glRecurringDist_glRecurring_end_date,
            glRecurring.glRecurring_last_transfer_date                        AS glRecurringDist_glRecurring_last_transfer_date,
            glRecurring.glRecurring_source_id                                 AS glRecurringDist_glRecurring_source_id,
            glRecurring.glRecurring_source_value                              AS glRecurringDist_glRecurring_source_value,
            glRecurring.glRecurring_source_description                        AS glRecurringDist_glRecurring_source_description,
            glRecurring.glRecurring_type_id                                   AS glRecurringDist_glRecurring_type_id,
            glRecurring.glRecurring_type_value                                AS glRecurringDist_glRecurring_type_value,
            glRecurring.glRecurring_type_description                          AS glRecurringDist_glRecurring_type_description,
            glRecurring.glRecurring_type_localization_code                    AS glRecurringDist_glRecurring_type_localization_code,
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
            count(*) OVER() AS total_elements
         FROM general_ledger_recurring_distribution glRecurringDist
            JOIN general_ledger_recurring glRecurring ON glRecurringDist.general_ledger_recurring_id = glRecurring.glRecurring_id AND glRecurring.glRecurring_deleted = FALSE
            JOIN account ON glRecurringDist.general_ledger_distribution_account_id = account.account_id AND account.account_deleted = FALSE
      """
   }

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): GeneralLedgerRecurringDistributionEntity? {
      val params = mutableMapOf<String, Any?>("id" to id)
      val query = "${selectBaseQuery()} WHERE glRecurringDist.id = :id AND glRecurringDist.deleted = FALSE"
      val found = jdbc.findFirstOrNull(
         query, params
      ) { rs, _ ->
         mapRow(rs, company, "glRecurringDist_")
      }

      logger.trace("Searching for GeneralLedgerRecurringDistribution: resulted in {}", found)

      return found
   }

   @ReadOnly
   fun findAll(
      company: CompanyEntity,
      page: PageRequest
   ): RepositoryPage<GeneralLedgerRecurringDistributionEntity, PageRequest> {
      return jdbc.queryPaged(
         """
            ${selectBaseQuery()}
            WHERE glRecurringDist.deleted = FALSE
            ORDER BY glRecurringDist_${page.snakeSortBy()} ${page.sortDirection()}
            LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "limit" to page.size(),
            "offset" to page.offset()
         ),
         page
      ) { rs, elements ->
         do {
            elements.add(mapRow(rs, company, "glRecurringDist_"))
         } while (rs.next())
      }
   }

   @ReadOnly
   fun findAllByRecurringId(
      glRecurringId: UUID,
      company: CompanyEntity,
      page: PageRequest
   ): RepositoryPage<GeneralLedgerRecurringDistributionEntity, PageRequest> {
      return jdbc.queryPaged(
         """
            ${selectBaseQuery()}
            WHERE glRecurringDist.general_ledger_recurring_id = :glRecurringId AND glRecurringDist.deleted = FALSE
            ORDER BY glRecurringDist_${page.snakeSortBy()} ${page.sortDirection()}
            LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "glRecurringId" to glRecurringId,
            "limit" to page.size(),
            "offset" to page.offset()
         ),
         page
      ) { rs, elements ->
         do {
            elements.add(mapRow(rs, company, "glRecurringDist_"))
         } while (rs.next())
      }
   }

   @Transactional
   fun insert(entity: GeneralLedgerRecurringDistributionEntity): GeneralLedgerRecurringDistributionEntity {
      logger.debug("Inserting general_ledger_recurring_distribution")

      return jdbc.insertReturning(
         """
         INSERT INTO general_ledger_recurring_distribution (
            general_ledger_recurring_id,
            general_ledger_distribution_account_id,
            general_ledger_distribution_profit_center_id_sfk,
            general_ledger_distribution_amount
         )
         VALUES (
            :general_ledger_recurring_id,
            :general_ledger_distribution_account_id,
            :general_ledger_distribution_profit_center_id_sfk,
            :general_ledger_distribution_amount
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "general_ledger_recurring_id" to entity.generalLedgerRecurring.id,
            "general_ledger_distribution_account_id" to entity.generalLedgerDistributionAccount.myId(),
            "general_ledger_distribution_profit_center_id_sfk" to entity.generalLedgerDistributionProfitCenter.myId(),
            "general_ledger_distribution_amount" to entity.generalLedgerDistributionAmount
         )
      ) { rs, _ ->
         mapRow(rs, entity)
      }
   }

   @Transactional
   fun update(entity: GeneralLedgerRecurringDistributionEntity): GeneralLedgerRecurringDistributionEntity {
      logger.debug("Updating general_ledger_recurring_distribution {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE general_ledger_recurring_distribution
         SET
            general_ledger_recurring_id = :general_ledger_recurring_id,
            general_ledger_distribution_account_id = :general_ledger_distribution_account_id,
            general_ledger_distribution_profit_center_id_sfk = :general_ledger_distribution_profit_center_id_sfk,
            general_ledger_distribution_amount = :general_ledger_distribution_amount
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "general_ledger_recurring_id" to entity.generalLedgerRecurring.id,
            "general_ledger_distribution_account_id" to entity.generalLedgerDistributionAccount.myId(),
            "general_ledger_distribution_profit_center_id_sfk" to entity.generalLedgerDistributionProfitCenter.myId(),
            "general_ledger_distribution_amount" to entity.generalLedgerDistributionAmount
         ),
      ) { rs, _ ->
         mapRow(rs, entity)
      }
   }

   @Transactional
   fun delete(id: UUID) {
      logger.debug("Deleting GeneralLedgerRecurringDistribution with id={}", id)
//@TODO check what is wrong!
      val rowsAffected = jdbc.update(
         """
         UPDATE general_ledger_recurring_distribution
         SET deleted = TRUE
         WHERE id = :id
         """,
         mapOf("id" to id)
      )
      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) throw NotFoundException(id)
   }

   private fun mapRow(
      rs: ResultSet,
      company: CompanyEntity,
      columnPrefix: String = EMPTY
   ): GeneralLedgerRecurringDistributionEntity {
      return GeneralLedgerRecurringDistributionEntity(
         id = rs.getUuid("${columnPrefix}id"),
         generalLedgerRecurring = generalLedgerRecurringRepository.mapRow(rs, "${columnPrefix}glRecurring_"),
         generalLedgerDistributionAccount = accountRepository.mapRow(rs, company, "${columnPrefix}account_"),
         generalLedgerDistributionProfitCenter = SimpleLegacyIdentifiableEntity(rs.getLong("${columnPrefix}profit_center_id_sfk")),
         generalLedgerDistributionAmount = rs.getBigDecimal("${columnPrefix}general_ledger_distribution_amount")
      )
   }

   private fun mapRow(
      rs: ResultSet,
      entity: GeneralLedgerRecurringDistributionEntity,
      columnPrefix: String = EMPTY
   ): GeneralLedgerRecurringDistributionEntity {
      return GeneralLedgerRecurringDistributionEntity(
         id = rs.getUuid("${columnPrefix}id"),
         generalLedgerRecurring = entity.generalLedgerRecurring,
         generalLedgerDistributionAccount = entity.generalLedgerDistributionAccount,
         generalLedgerDistributionProfitCenter = entity.generalLedgerDistributionProfitCenter,
         generalLedgerDistributionAmount = rs.getBigDecimal("${columnPrefix}general_ledger_distribution_amount")
      )
   }
}
