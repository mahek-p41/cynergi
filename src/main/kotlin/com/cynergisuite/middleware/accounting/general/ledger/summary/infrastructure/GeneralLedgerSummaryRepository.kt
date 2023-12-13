package com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure

import com.cynergisuite.domain.GeneralLedgerProfitCenterTrialBalanceReportFilterRequest
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.update
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.financial.calendar.type.infrastructure.OverallPeriodTypeRepository
import com.cynergisuite.middleware.accounting.general.ledger.summary.GeneralLedgerSummaryEntity
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.sql.ResultSet
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class GeneralLedgerSummaryRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val accountRepository: AccountRepository,
   private val storeRepository: StoreRepository,
   private val overallPeriodTypeRepository: OverallPeriodTypeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerSummaryRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         WITH account AS (
            ${accountRepository.selectBaseQuery()}
         )
         SELECT
            glSummary.id                                              AS glSummary_id,
            glSummary.company_id                                      AS glSummary_company_id,
            glSummary.profit_center_id_sfk                            AS glSummary_profit_center_id_sfk,
            glSummary.net_activity_period_1                           AS glSummary_net_activity_period_1,
            glSummary.net_activity_period_2                           AS glSummary_net_activity_period_2,
            glSummary.net_activity_period_3                           AS glSummary_net_activity_period_3,
            glSummary.net_activity_period_4                           AS glSummary_net_activity_period_4,
            glSummary.net_activity_period_5                           AS glSummary_net_activity_period_5,
            glSummary.net_activity_period_6                           AS glSummary_net_activity_period_6,
            glSummary.net_activity_period_7                           AS glSummary_net_activity_period_7,
            glSummary.net_activity_period_8                           AS glSummary_net_activity_period_8,
            glSummary.net_activity_period_9                           AS glSummary_net_activity_period_9,
            glSummary.net_activity_period_10                          AS glSummary_net_activity_period_10,
            glSummary.net_activity_period_11                          AS glSummary_net_activity_period_11,
            glSummary.net_activity_period_12                          AS glSummary_net_activity_period_12,
            glSummary.beginning_balance                               AS glSummary_beginning_balance,
            glSummary.closing_balance                                 AS glSummary_closing_balance,
            acct.account_id                                           AS glSummary_acct_id,
            acct.account_number                                       AS glSummary_acct_number,
            acct.account_name                                         AS glSummary_acct_name,
            acct.account_form_1099_field                              AS glSummary_acct_form_1099_field,
            acct.account_corporate_account_indicator                  AS glSummary_acct_corporate_account_indicator,
            acct.account_comp_id                                      AS glSummary_acct_comp_id,
            acct.account_deleted                                      AS glSummary_acct_deleted,
            acct.account_type_id                                      AS glSummary_acct_type_id,
            acct.account_type_value                                   AS glSummary_acct_type_value,
            acct.account_type_description                             AS glSummary_acct_type_description,
            acct.account_type_localization_code                       AS glSummary_acct_type_localization_code,
            acct.account_balance_type_id                              AS glSummary_acct_balance_type_id,
            acct.account_balance_type_value                           AS glSummary_acct_balance_type_value,
            acct.account_balance_type_description                     AS glSummary_acct_balance_type_description,
            acct.account_balance_type_localization_code               AS glSummary_acct_balance_type_localization_code,
            acct.account_status_id                                    AS glSummary_acct_status_id,
            acct.account_status_value                                 AS glSummary_acct_status_value,
            acct.account_status_description                           AS glSummary_acct_status_description,
            acct.account_status_localization_code                     AS glSummary_acct_status_localization_code,
            acct.account_vendor_1099_type_id                          AS glSummary_acct_vendor_1099_type_id,
            acct.account_vendor_1099_type_value                       AS glSummary_acct_vendor_1099_type_value,
            acct.account_vendor_1099_type_description                 AS glSummary_acct_vendor_1099_type_description,
            acct.account_vendor_1099_type_localization_code           AS glSummary_acct_vendor_1099_type_localization_code,
            bank.id                                                   AS glSummary_acct_bank_id,
            profitCenter.id                                           AS glSummary_profitCenter_id,
            profitCenter.number                                       AS glSummary_profitCenter_number,
            profitCenter.name                                         AS glSummary_profitCenter_name,
            profitCenter.dataset                                      AS glSummary_profitCenter_dataset,
            overallPeriod.id                                          AS glSummary_overallPeriod_id,
            overallPeriod.value                                       AS glSummary_overallPeriod_value,
            overallPeriod.abbreviation                                AS glSummary_overallPeriod_abbreviation,
            overallPeriod.description                                 AS glSummary_overallPeriod_description,
            overallPeriod.localization_code                           AS glSummary_overallPeriod_localization_code
         FROM general_ledger_summary glSummary
            JOIN company comp ON glSummary.company_id = comp.id AND comp.deleted = FALSE
            JOIN fastinfo_prod_import.store_vw profitCenter
                    ON profitCenter.dataset = comp.dataset_code
                       AND profitCenter.number = glSummary.profit_center_id_sfk
            JOIN account acct ON glSummary.account_id = acct.account_id AND acct.account_deleted = FALSE
            JOIN overall_period_type_domain overallPeriod ON glSummary.overall_period_id = overallPeriod.id
            LEFT OUTER JOIN bank ON bank.general_ledger_account_id = acct.account_id AND bank.deleted = FALSE
      """
   }

   @ReadOnly
   fun exists(company: CompanyEntity, accountId: UUID, profitCenterNumber: Int, overallPeriodId: Int): Boolean {
      val params = mutableMapOf("comp_id" to company.id, "accountId" to accountId, "profitCenterNumber" to profitCenterNumber, "overallPeriodId" to overallPeriodId)
      val exists = jdbc.queryForObject(
         """
         SELECT EXISTS (SELECT id
                        FROM general_ledger_summary
                        WHERE company_id = :comp_id AND account_id = :accountId AND profit_center_id_sfk = :profitCenterNumber AND overall_period_id = :overallPeriodId)
         """.trimIndent(),
         params,
         Boolean::class.java
      )

      logger.trace("Checking if general_ledger_summary: {} exists resulted in {}", params, exists)

      return exists
   }

   @ReadOnly
   fun existsByCompanyOnly(company: CompanyEntity): Boolean {
      val params = mutableMapOf("comp_id" to company.id)
      val exists = jdbc.queryForObject(
         """
         SELECT EXISTS (SELECT id
                        FROM general_ledger_summary
                        WHERE company_id = :comp_id)
         """.trimIndent(),
         params,
         Boolean::class.java
      )

      logger.trace("Checking if general_ledger_summary by company only: {} exists resulted in {}", params, exists)

      return exists
   }

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): GeneralLedgerSummaryEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query = "${selectBaseQuery()}\nWHERE glSummary.id = :id AND glSummary.company_id = :comp_id"

      logger.debug("Searching for GeneralLedgerSummary using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ ->
         val generalLedgerSummary = mapRow(rs, company, "glSummary_")

         generalLedgerSummary
      }

      logger.trace("Searching for GeneralLedgerSummary: {} resulted in {}", id, found)

      return found
   }

   @ReadOnly
   fun findOneByBusinessKey(company: CompanyEntity, accountId: UUID, storeNumber: Int, overallPeriodValue: String): GeneralLedgerSummaryEntity? {
      val params = mutableMapOf("comp_id" to company.id, "accountId" to accountId, "storeNumber" to storeNumber, "overallPeriodValue" to overallPeriodValue)
      val query = "${selectBaseQuery()}\nWHERE glSummary.company_id = :comp_id AND glSummary.account_id = :accountId AND glSummary.profit_center_id_sfk = :storeNumber AND overallPeriod.value = :overallPeriodValue"

      logger.debug("Searching for GeneralLedgerSummary using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ ->
         val generalLedgerSummary = mapRow(rs, company, "glSummary_")

         generalLedgerSummary
      }

      logger.trace("Searching for GeneralLedgerSummary: {} resulted in {}", params, found)

      return found
   }

   @ReadOnly
   fun findAll(company: CompanyEntity, page: PageRequest): RepositoryPage<GeneralLedgerSummaryEntity, PageRequest> {
      var totalElements: Long? = null
      val resultList: MutableList<GeneralLedgerSummaryEntity> = mutableListOf()

      jdbc.query(
         """
         WITH paged AS (
            ${selectBaseQuery()}
            WHERE comp.id = :comp_id
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER by glSummary_${page.snakeSortBy()} ${page.sortDirection()}
         LIMIT :limit OFFSET :offset
         """,
         mapOf(
            "comp_id" to company.id,
            "limit" to page.size(),
            "offset" to page.offset()
         )
      ) { rs, _ ->
         resultList.add(mapRow(rs, company, "glSummary_"))
         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }
      }

      return RepositoryPage(
         requested = page,
         elements = resultList,
         totalElements = totalElements ?: 0
      )
   }

   @Transactional
   fun insert(entity: GeneralLedgerSummaryEntity, company: CompanyEntity): GeneralLedgerSummaryEntity {
      logger.debug("Inserting GeneralLedgerSummary {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO general_ledger_summary(
            company_id,
            account_id,
            profit_center_id_sfk,
            overall_period_id,
            net_activity_period_1,
            net_activity_period_2,
            net_activity_period_3,
            net_activity_period_4,
            net_activity_period_5,
            net_activity_period_6,
            net_activity_period_7,
            net_activity_period_8,
            net_activity_period_9,
            net_activity_period_10,
            net_activity_period_11,
            net_activity_period_12,
            beginning_balance,
            closing_balance
         )
         VALUES (
            :company_id,
            :account_id,
            :profit_center_id_sfk,
            :overall_period_id,
            :net_activity_period_1,
            :net_activity_period_2,
            :net_activity_period_3,
            :net_activity_period_4,
            :net_activity_period_5,
            :net_activity_period_6,
            :net_activity_period_7,
            :net_activity_period_8,
            :net_activity_period_9,
            :net_activity_period_10,
            :net_activity_period_11,
            :net_activity_period_12,
            :beginning_balance,
            :closing_balance
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.id,
            "account_id" to entity.account.myId(),
            "profit_center_id_sfk" to entity.profitCenter.myNumber(),
            "overall_period_id" to entity.overallPeriod.id,
            "net_activity_period_1" to entity.netActivityPeriod1,
            "net_activity_period_2" to entity.netActivityPeriod2,
            "net_activity_period_3" to entity.netActivityPeriod3,
            "net_activity_period_4" to entity.netActivityPeriod4,
            "net_activity_period_5" to entity.netActivityPeriod5,
            "net_activity_period_6" to entity.netActivityPeriod6,
            "net_activity_period_7" to entity.netActivityPeriod7,
            "net_activity_period_8" to entity.netActivityPeriod8,
            "net_activity_period_9" to entity.netActivityPeriod9,
            "net_activity_period_10" to entity.netActivityPeriod10,
            "net_activity_period_11" to entity.netActivityPeriod11,
            "net_activity_period_12" to entity.netActivityPeriod12,
            "beginning_balance" to entity.beginningBalance,
            "closing_balance" to entity.closingBalance
         )
      ) { rs, _ -> mapRow(rs, entity) }
   }

   @Transactional
   fun update(entity: GeneralLedgerSummaryEntity, company: CompanyEntity): GeneralLedgerSummaryEntity {
      logger.debug("Updating GeneralLedgerSummary {}", entity)

      return jdbc.updateReturning(
         """
            UPDATE general_ledger_summary
            SET
               company_id = :company_id,
               account_id = :account_id,
               profit_center_id_sfk = :profit_center_id_sfk,
               overall_period_id = :overall_period_id,
               net_activity_period_1 = :net_activity_period_1,
               net_activity_period_2 = :net_activity_period_2,
               net_activity_period_3 = :net_activity_period_3,
               net_activity_period_4 = :net_activity_period_4,
               net_activity_period_5 = :net_activity_period_5,
               net_activity_period_6 = :net_activity_period_6,
               net_activity_period_7 = :net_activity_period_7,
               net_activity_period_8 = :net_activity_period_8,
               net_activity_period_9 = :net_activity_period_9,
               net_activity_period_10 = :net_activity_period_10,
               net_activity_period_11 = :net_activity_period_11,
               net_activity_period_12 = :net_activity_period_12,
               beginning_balance = :beginning_balance,
               closing_balance = :closing_balance
            WHERE id = :id
            RETURNING
               *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to company.id,
            "account_id" to entity.account.myId(),
            "profit_center_id_sfk" to entity.profitCenter.myNumber(),
            "overall_period_id" to entity.overallPeriod.id,
            "net_activity_period_1" to entity.netActivityPeriod1,
            "net_activity_period_2" to entity.netActivityPeriod2,
            "net_activity_period_3" to entity.netActivityPeriod3,
            "net_activity_period_4" to entity.netActivityPeriod4,
            "net_activity_period_5" to entity.netActivityPeriod5,
            "net_activity_period_6" to entity.netActivityPeriod6,
            "net_activity_period_7" to entity.netActivityPeriod7,
            "net_activity_period_8" to entity.netActivityPeriod8,
            "net_activity_period_9" to entity.netActivityPeriod9,
            "net_activity_period_10" to entity.netActivityPeriod10,
            "net_activity_period_11" to entity.netActivityPeriod11,
            "net_activity_period_12" to entity.netActivityPeriod12,
            "beginning_balance" to entity.beginningBalance,
            "closing_balance" to entity.closingBalance
         )
      ) { rs, _ -> mapRow(rs, entity) }
   }

   @ReadOnly
   fun fetchProfitCenterTrialBalanceReportRecords(company: CompanyEntity, filterRequest: GeneralLedgerProfitCenterTrialBalanceReportFilterRequest, pair: Pair<Int, Int>): List<GeneralLedgerSummaryEntity> {
      val glSummaries = mutableListOf<GeneralLedgerSummaryEntity>()
      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "overall_period_id" to pair.first)
      val whereClause = StringBuilder(
         """
            WHERE glSummary.company_id = :comp_id
               AND glSummary.overall_period_id = :overall_period_id
               AND (acct.account_status_id = 1 OR glSummary.beginning_balance <> 0.00 OR glSummary.net_activity_period_${pair.second} <> 0.00)
         """.trimIndent()
      )

      if (filterRequest.startingAccount != null || filterRequest.endingAccount != null) {
         params["startingAccount"] = filterRequest.startingAccount
         params["endingAccount"] = filterRequest.endingAccount
         whereClause.append(" AND acct.account_number ")
            .append(buildFilterString( filterRequest.startingAccount != null, filterRequest.endingAccount != null, "startingAccount", "endingAccount"))
      }

      // select locations based on criteria (1 selects all locations)
      when (filterRequest.selectLocsBy) {
         2 ->
         {
            params["any10LocsOrGroups"] = filterRequest.any10LocsOrGroups
            whereClause.append(" AND glSummary.profit_center_id_sfk IN (<any10LocsOrGroups>)")
         }
         3 ->
         {
            params["startingLocOrGroup"] = filterRequest.startingLocOrGroup
            params["endingLocOrGroup"] = filterRequest.endingLocOrGroup
            whereClause.append(" AND glSummary.profit_center_id_sfk BETWEEN :startingLocOrGroup AND :endingLocOrGroup")
         }
         // todo: 4 & 5 use location groups
      }

      // assign sort by
      val sortBy = StringBuilder("ORDER BY ")
      when (filterRequest.sortBy) {
         "location" ->
            sortBy.append("glSummary.profit_center_id_sfk ASC, glSummary.account_id ASC")
         "account" ->
            sortBy.append("glSummary.account_id ASC, glSummary.profit_center_id_sfk ASC")
         null ->
            sortBy.append("glSummary.profit_center_id_sfk ASC, glSummary.account_id ASC")
      }

      val query = """
         ${selectBaseQuery()}
         $whereClause
         $sortBy
      """.trimIndent()

      logger.debug("GeneralLedgerSummary query {} with params {}", query, params)

      jdbc.query(
         query,
         params
      ) { rs, _ ->
         do {
            glSummaries.add(mapRow(rs, company, "glSummary_"))
         } while (rs.next())
      }

      return glSummaries
   }

   @ReadOnly
   fun calculateGLBalance(params: MutableMap<String, Any>): BigDecimal {
      return jdbc.queryForObject("""SELECT
             COALESCE(SUM(
                 beginning_balance +
                 CASE WHEN period >= 1 THEN net_activity_period_1 ELSE 0 END +
                 CASE WHEN period >= 2 THEN net_activity_period_2 ELSE 0 END +
                 CASE WHEN period >= 3 THEN net_activity_period_3 ELSE 0 END +
                 CASE WHEN period >= 4 THEN net_activity_period_4 ELSE 0 END +
                 CASE WHEN period >= 5 THEN net_activity_period_5 ELSE 0 END +
                 CASE WHEN period >= 6 THEN net_activity_period_6 ELSE 0 END +
                 CASE WHEN period >= 7 THEN net_activity_period_7 ELSE 0 END +
                 CASE WHEN period >= 8 THEN net_activity_period_8 ELSE 0 END +
                 CASE WHEN period >= 9 THEN net_activity_period_9 ELSE 0 END +
                 CASE WHEN period >= 10 THEN net_activity_period_10 ELSE 0 END +
                 CASE WHEN period >= 11 THEN net_activity_period_11 ELSE 0 END +
                 CASE WHEN period >= 12 THEN net_activity_period_12 ELSE 0 END
             ), 0) AS net_activity_period_sum
         FROM
             general_ledger_summary summary
               JOIN financial_calendar fc ON fc.company_id = summary.company_id
                     AND fc.overall_period_id = summary.overall_period_id
                     AND :date BETWEEN fc.period_from AND fc.period_to
               JOIN bank ON summary.profit_center_id_sfk = bank.general_ledger_profit_center_sfk
                     AND summary.account_id = bank.general_ledger_account_id
                     AND bank.number = :bank
         WHERE summary.company_id = :comp_id
         """, params, BigDecimal::class.java)
   }

   @ReadOnly
   fun updateClosingBalanceForCurrentFiscalYear(company: CompanyEntity): Int {
      return jdbc.update("""
         UPDATE general_ledger_summary
         SET closing_balance =
                 COALESCE(beginning_balance, 0) +
                 COALESCE(net_activity_period_1, 0) +
                 COALESCE(net_activity_period_2, 0) +
                 COALESCE(net_activity_period_3, 0) +
                 COALESCE(net_activity_period_4, 0) +
                 COALESCE(net_activity_period_5, 0) +
                 COALESCE(net_activity_period_6, 0) +
                 COALESCE(net_activity_period_7, 0) +
                 COALESCE(net_activity_period_8, 0) +
                 COALESCE(net_activity_period_9, 0) +
                 COALESCE(net_activity_period_10, 0) +
                 COALESCE(net_activity_period_11, 0) +
                 COALESCE(net_activity_period_12, 0)
         WHERE company_id = :comp_id AND overall_period_id = 3
         """, mapOf("comp_id" to company.id))
   }

   fun calculateNetIncomeForCurrentFiscalYear(company: CompanyEntity): BigDecimal {
      return jdbc.queryForObject("""SELECT
          COALESCE(SUM(closing_balance), 0)
      FROM general_ledger_summary summary
         JOIN account ON summary.account_id = account.id AND account.deleted = FALSE
         JOIN account_type_domain type ON account.type_id = type.id
      WHERE summary.company_id = :comp_id
         AND type.value IN ('R', 'E')
         AND summary.overall_period_id = 3
      """, mapOf("comp_id" to company.id), BigDecimal::class.java)
   }

   @Transactional
   fun rollOneFinancialYear(company: CompanyEntity) {
      logger.debug("Roll one financial year for general_ledger_summary and update beginning_balance {}", company)

      jdbc.update("""
         DELETE FROM general_ledger_summary
         WHERE company_id = :comp_id
               AND overall_period_id = 1;

         UPDATE general_ledger_summary
         SET overall_period_id = 1
         WHERE company_id = :comp_id
               AND overall_period_id in (2);

         UPDATE general_ledger_summary
         SET overall_period_id = 2
         WHERE company_id = :comp_id
               AND overall_period_id in (3);

         UPDATE general_ledger_summary
         SET overall_period_id = 3
         WHERE company_id = :comp_id
               AND overall_period_id in (4);

         INSERT INTO general_ledger_summary(company_id, account_id, profit_center_id_sfk, overall_period_id)
         SELECT company_id,
                account_id,
                profit_center_id_sfk,
                4
         FROM general_ledger_summary
         WHERE company_id = :comp_id
               AND overall_period_id = 3;
         """.trimIndent(),
         mapOf("comp_id" to company.id)
      )
   }


   fun updateBeginningBalanceForNextYear(company: CompanyEntity, jeNumber: Int): Int {
      logger.debug("Update beginning balance for the next year {}", company)

      val affectedRows = jdbc.update("""
         UPDATE general_ledger_summary AS summary
         SET beginning_balance = detail.amount
         FROM general_ledger_detail AS detail
         WHERE summary.company_id = :comp_id
            AND summary.overall_period_id = 4
            AND detail.journal_entry_number = :je_number
            AND summary.company_id = detail.company_id
            AND summary.account_id = detail.account_id
            AND summary.profit_center_id_sfk = detail.profit_center_id_sfk
         """.trimIndent(),
         mapOf("comp_id" to company.id, "je_number" to jeNumber)
      )
      logger.info("Updated {} general_ledger_summary rows.", affectedRows)
      return affectedRows
   }

   @ReadOnly
   fun findMissingSummaries(company: CompanyEntity): List<Pair<AccountEntity, Store>>? {
      logger.debug("Check detail summary {}", company)
      val toCreate = mutableListOf<Pair<AccountEntity, Store>>()

      jdbc.query(
         """
         SELECT DISTINCT detail.account_id, detail.profit_center_id_sfk
         FROM general_ledger_detail AS detail
         LEFT JOIN general_ledger_summary summary
         ON detail.account_id = summary.account_id AND detail.profit_center_id_sfk = summary.profit_center_id_sfk
         WHERE detail.company_id = :comp_id
            AND summary.account_id IS NULL AND summary.profit_center_id_sfk IS NULL
         """.trimIndent(),
         mapOf("comp_id" to company.id)
      ) { rs, _ ->
         do {
            toCreate.add(mapMissingPair(rs, company))
         } while (rs.next())
      }
      return toCreate
   }

   @Transactional
   fun resetGLBalance(company: CompanyEntity) {

      jdbc.update(
         """
         UPDATE general_ledger_summary summary
         SET beginning_balance = 0.00,
         closing_balance = 0.00,
         net_activity_period_1 = 0.00,
         net_activity_period_2 = 0.00,
         net_activity_period_3 = 0.00,
         net_activity_period_4 = 0.00,
         net_activity_period_5 = 0.00,
         net_activity_period_6 = 0.00,
         net_activity_period_7 = 0.00,
         net_activity_period_8 = 0.00,
         net_activity_period_9 = 0.00,
         net_activity_period_10 = 0.00,
         net_activity_period_11 = 0.00,
         net_activity_period_12 = 0.00
         WHERE company_id = :company_id
         AND overall_period_id = 3 OR overall_period_id = 4
         """,
         mapOf("company_id" to company.id)
      )
   }

   @Transactional
   fun recalculateGLBalance(company: CompanyEntity) {
      val query = """
         WITH glSum as (
         SELECT
            glSummary.id                                              AS glSummary_id,
            glSummary.company_id                                      AS glSummary_company_id,
            glSummary.profit_center_id_sfk                            AS glSummary_profit_center_id_sfk,
            glSummary.closing_balance                                 AS glSummary_closing_balance,
            acct.id                                                   AS glSummary_account_id,
            acct.number                                               AS glSummary_acct_number,
            acct.company_id                                           AS glSummary_acct_comp_id,
            acct.deleted                                              AS glSummary_acct_deleted,
            acct_type.id                                              AS glSummary_acct_type_id,
            acct_type.value                                           AS glSummary_acct_type_value,
            bank.id                                                   AS glSummary_acct_bank_id,
            overallPeriod.id                                          AS glSummary_overallPeriod_id,
            overallPeriod.value                                       AS glSummary_overallPeriod_value,
            overallPeriod.abbreviation                                AS glSummary_overallPeriod_abbreviation,
            overallPeriod.description                                 AS glSummary_overallPeriod_description,
            overallPeriod.localization_code                           AS glSummary_overallPeriod_localization_code
         FROM general_ledger_summary glSummary
            JOIN company comp ON glSummary.company_id = comp.id AND comp.deleted = FALSE
            JOIN account acct ON glSummary.account_id = acct.id AND acct.deleted = FALSE
            JOIN account_type_domain acct_type on acct.type_id = acct_type.id
            JOIN overall_period_type_domain overallPeriod ON glSummary.overall_period_id = overallPeriod.id
            LEFT OUTER JOIN bank ON bank.general_ledger_account_id = acct.id AND bank.deleted = FALSE
            WHERE glSummary.company_id = :comp_id AND acct_type.value IN ('A', 'L') AND overallPeriod.id = 2
         )
         update general_ledger_summary summary
         set beginning_balance = glSum.glSummary_closing_balance
         from glSum
         WHERE summary.account_id = glSum.glSummary_account_id AND summary.profit_center_id_sfk = glSum.glSummary_profit_center_id_sfk and summary.overall_period_id = 3
      """

      jdbc.update(query, mapOf("comp_id" to company.id))
   }

   @Transactional
   fun recalculateCapitalAccounts(company: CompanyEntity) {
      val query = """
         with details as (
         SELECT glDetail.*, finCal.period_from, finCal.period_to, finCal.overall_period_id, finCal.period, srcCodes.value from general_ledger_detail glDetail
         JOIN general_ledger_source_codes srcCodes on glDetail.source_id = srcCodes.id
         JOIN financial_calendar finCal on glDetail.company_id = finCal.company_id and glDetail.date >= finCal.period_from and glDetail.date <= finCal.period_to
         JOIN account acct ON glDetail.account_id = acct.id
		   JOIN account_type_domain acct_type ON acct.type_id = acct_type.id
         WHERE glDetail.company_id = :comp_id and glDetail.deleted = FALSE AND srcCodes.value = 'BAL' AND acct_type.value = 'C'
         ),
         detailSums as (
            SELECT account_id, profit_center_id_sfk, overall_period_id, SUM(amount) AS total_amount
            FROM details
            GROUP BY account_id, profit_center_id_sfk, overall_period_id
         )
         UPDATE general_ledger_summary
         SET beginning_balance = detailSums.total_amount
         FROM detailSums
         WHERE general_ledger_summary.account_id = detailSums.account_id AND general_ledger_summary.profit_center_id_sfk = detailSums.profit_center_id_sfk  AND general_ledger_summary.overall_period_id = detailSums.overall_period_id
      """

      jdbc.update(query, mapOf("comp_id" to company.id))
   }

   @Transactional
   fun setNetActivityPeriods(company: CompanyEntity) {
      val query = """
         with glDetail as (
         	SELECT glDetail.*, finCal.period_from, finCal.period_to, finCal.overall_period_id, finCal.period, srcCodes.value
            FROM general_ledger_detail glDetail
         	JOIN general_ledger_source_codes srcCodes on glDetail.source_id = srcCodes.id
         	JOIN financial_calendar finCal on glDetail.company_id = finCal.company_id and glDetail.date >= finCal.period_from and glDetail.date <= finCal.period_to
         	WHERE glDetail.company_id = :comp_id and glDetail.deleted = FALSE AND overall_period_id IN (3, 4) and srcCodes.value != 'BAL'
         ), detailSums3 as (
               SELECT account_id, profit_center_id_sfk, overall_period_id, period,
                  SUM(case when glDetail.period = 1 then amount end) as total1,
                  SUM(case when glDetail.period = 2 then amount end) as total2,
                  SUM(case when glDetail.period = 3 then amount end) as total3,
                  SUM(case when glDetail.period = 4 then amount end) as total4,
                  SUM(case when glDetail.period = 5 then amount end) as total5,
                  SUM(case when glDetail.period = 6 then amount end) as total6,
                  SUM(case when glDetail.period = 7 then amount end) as total7,
                  SUM(case when glDetail.period = 8 then amount end) as total8,
                  SUM(case when glDetail.period = 9 then amount end) as total9,
                  SUM(case when glDetail.period = 10 then amount end) as total10,
                  SUM(case when glDetail.period = 11 then amount end) as total11,
                  SUM(case when glDetail.period = 12 then amount end) as total12
               FROM glDetail
               WHERE overall_period_id = 3
               GROUP BY account_id, profit_center_id_sfk, overall_period_id, period
         ), gltotals3 as (
               SELECT account_id, profit_center_id_sfk, overall_period_id,
                  min(total1) as total1, min(total2) as total2, min(total3) as total3, min(total4) as total4, min(total5) as total5, min(total6) as total6,
                  min(total7) as total7, min(total8) as total8, min(total9) as total9, min(total10) as total10, min(total11) as total11, min(total12) as total12
               FROM detailSums3
               GROUP BY account_id, profit_center_id_sfk, overall_period_id
         )
         UPDATE general_ledger_summary AS summary
         SET
            net_activity_period_1 = gltotals3.total1,
            net_activity_period_2 = gltotals3.total2,
            net_activity_period_3 = gltotals3.total3,
            net_activity_period_4 = gltotals3.total4,
            net_activity_period_5 = gltotals3.total5,
            net_activity_period_6 = gltotals3.total6,
            net_activity_period_7 = gltotals3.total7,
            net_activity_period_8 = gltotals3.total8,
            net_activity_period_9 = gltotals3.total9,
            net_activity_period_10 = gltotals3.total10,
            net_activity_period_11 = gltotals3.total11,
            net_activity_period_12 = gltotals3.total12
         from gltotals3
           WHERE summary.account_id = gltotals3.account_id
             AND summary.profit_center_id_sfk = gltotals3.profit_center_id_sfk
             AND summary.overall_period_id = 3;

         WITH glDetail as (
         	SELECT glDetail.*, finCal.period_from, finCal.period_to, finCal.overall_period_id, finCal.period, srcCodes.value
            FROM general_ledger_detail glDetail
         	JOIN general_ledger_source_codes srcCodes on glDetail.source_id = srcCodes.id
         	JOIN financial_calendar finCal on glDetail.company_id = finCal.company_id and glDetail.date >= finCal.period_from and glDetail.date <= finCal.period_to
         	WHERE glDetail.company_id = :comp_id and glDetail.deleted = FALSE AND overall_period_id IN (3, 4) and srcCodes.value != 'BAL'
         ), detailSums4 as (
            SELECT account_id, profit_center_id_sfk, overall_period_id, period,
            SUM(case when glDetail.period = 1 then amount end) as total1,
            SUM(case when glDetail.period = 2 then amount end) as total2,
            SUM(case when glDetail.period = 3 then amount end) as total3,
            SUM(case when glDetail.period = 4 then amount end) as total4,
            SUM(case when glDetail.period = 5 then amount end) as total5,
            SUM(case when glDetail.period = 6 then amount end) as total6,
            SUM(case when glDetail.period = 7 then amount end) as total7,
            SUM(case when glDetail.period = 8 then amount end) as total8,
            SUM(case when glDetail.period = 9 then amount end) as total9,
            SUM(case when glDetail.period = 10 then amount end) as total10,
            SUM(case when glDetail.period = 11 then amount end) as total11,
            SUM(case when glDetail.period = 12 then amount end) as total12
         FROM glDetail
            WHERE overall_period_id = 4
            GROUP BY account_id, profit_center_id_sfk, overall_period_id, period
         ), gltotals4 as (
            SELECT account_id, profit_center_id_sfk, overall_period_id,
               min(total1) as total1, min(total2) as total2, min(total3) as total3, min(total4) as total4, min(total5) as total5, min(total6) as total6,
               min(total7) as total7, min(total8) as total8, min(total9) as total9, min(total10) as total10, min(total11) as total11, min(total12) as total12
            FROM detailSums4
            GROUP BY account_id, profit_center_id_sfk, overall_period_id
         )
         UPDATE general_ledger_summary AS summary
         SET
           net_activity_period_1 = gltotals4.total1,
           net_activity_period_2 = gltotals4.total2,
           net_activity_period_3 = gltotals4.total3,
           net_activity_period_4 = gltotals4.total4,
           net_activity_period_5 = gltotals4.total5,
           net_activity_period_6 = gltotals4.total6,
           net_activity_period_7 = gltotals4.total7,
           net_activity_period_8 = gltotals4.total8,
           net_activity_period_9 = gltotals4.total9,
           net_activity_period_10 = gltotals4.total10,
           net_activity_period_11 = gltotals4.total11,
           net_activity_period_12 = gltotals4.total12
         FROM gltotals4
         WHERE summary.account_id = gltotals4.account_id
           AND summary.profit_center_id_sfk = gltotals4.profit_center_id_sfk
           AND summary.overall_period_id = 4;
       """.trimIndent()

      jdbc.update(query, mapOf("comp_id" to company.id))
   }

   fun isGLBalanceForCurrentYear(companyId: UUID): Boolean {
      logger.info("Checking if GL is balance for current year:")
      return jdbc.queryForObject(
         """SELECT
                   CASE
                       WHEN SUM(COALESCE(net_activity_period_1, 0) + COALESCE(net_activity_period_2, 0) +
                               COALESCE(net_activity_period_3, 0) + COALESCE(net_activity_period_4, 0) +
                               COALESCE(net_activity_period_5, 0) + COALESCE(net_activity_period_6, 0) +
                               COALESCE(net_activity_period_7, 0) + COALESCE(net_activity_period_8, 0) +
                               COALESCE(net_activity_period_9, 0) + COALESCE(net_activity_period_10, 0) +
                               COALESCE(net_activity_period_11, 0) + COALESCE(net_activity_period_12, 0)) = 0
                           THEN true
                       ELSE false
                   END AS result
               FROM
                   general_ledger_summary
               WHERE
                   company_id = :comp_id AND
                   overall_period_id = 3
         """,
         mapOf("comp_id" to companyId),
         Boolean::class.java
      )
   }


   fun mapRow(rs: ResultSet, company: CompanyEntity, columnPrefix: String = EMPTY): GeneralLedgerSummaryEntity {
      return GeneralLedgerSummaryEntity(
         id = rs.getUuid("${columnPrefix}id"),
         account = accountRepository.mapRow(rs, company, "${columnPrefix}acct_"),
         profitCenter = storeRepository.mapRow(rs, company, "${columnPrefix}profitCenter_"),
         overallPeriod = overallPeriodTypeRepository.mapRow(rs, "${columnPrefix}overallPeriod_"),
         netActivityPeriod1 = rs.getBigDecimal("${columnPrefix}net_activity_period_1"),
         netActivityPeriod2 = rs.getBigDecimal("${columnPrefix}net_activity_period_2"),
         netActivityPeriod3 = rs.getBigDecimal("${columnPrefix}net_activity_period_3"),
         netActivityPeriod4 = rs.getBigDecimal("${columnPrefix}net_activity_period_4"),
         netActivityPeriod5 = rs.getBigDecimal("${columnPrefix}net_activity_period_5"),
         netActivityPeriod6 = rs.getBigDecimal("${columnPrefix}net_activity_period_6"),
         netActivityPeriod7 = rs.getBigDecimal("${columnPrefix}net_activity_period_7"),
         netActivityPeriod8 = rs.getBigDecimal("${columnPrefix}net_activity_period_8"),
         netActivityPeriod9 = rs.getBigDecimal("${columnPrefix}net_activity_period_9"),
         netActivityPeriod10 = rs.getBigDecimal("${columnPrefix}net_activity_period_10"),
         netActivityPeriod11 = rs.getBigDecimal("${columnPrefix}net_activity_period_11"),
         netActivityPeriod12 = rs.getBigDecimal("${columnPrefix}net_activity_period_12"),
         beginningBalance = rs.getBigDecimal("${columnPrefix}beginning_balance"),
         closingBalance = rs.getBigDecimal("${columnPrefix}closing_balance")
      )
   }

   private fun mapRow(rs: ResultSet, entity: GeneralLedgerSummaryEntity, columnPrefix: String = EMPTY): GeneralLedgerSummaryEntity {
      return GeneralLedgerSummaryEntity(
         id = rs.getUuid("${columnPrefix}id"),
         account = entity.account,
         profitCenter = entity.profitCenter,
         overallPeriod = entity.overallPeriod,
         netActivityPeriod1 = rs.getBigDecimal("${columnPrefix}net_activity_period_1"),
         netActivityPeriod2 = rs.getBigDecimal("${columnPrefix}net_activity_period_2"),
         netActivityPeriod3 = rs.getBigDecimal("${columnPrefix}net_activity_period_3"),
         netActivityPeriod4 = rs.getBigDecimal("${columnPrefix}net_activity_period_4"),
         netActivityPeriod5 = rs.getBigDecimal("${columnPrefix}net_activity_period_5"),
         netActivityPeriod6 = rs.getBigDecimal("${columnPrefix}net_activity_period_6"),
         netActivityPeriod7 = rs.getBigDecimal("${columnPrefix}net_activity_period_7"),
         netActivityPeriod8 = rs.getBigDecimal("${columnPrefix}net_activity_period_8"),
         netActivityPeriod9 = rs.getBigDecimal("${columnPrefix}net_activity_period_9"),
         netActivityPeriod10 = rs.getBigDecimal("${columnPrefix}net_activity_period_10"),
         netActivityPeriod11 = rs.getBigDecimal("${columnPrefix}net_activity_period_11"),
         netActivityPeriod12 = rs.getBigDecimal("${columnPrefix}net_activity_period_12"),
         beginningBalance = rs.getBigDecimal("${columnPrefix}beginning_balance"),
         closingBalance = rs.getBigDecimal("${columnPrefix}closing_balance")
      )
   }

   private fun mapMissingPair(rs: ResultSet, company: CompanyEntity): Pair<AccountEntity, Store> {
      return Pair(
         accountRepository.findOne(rs.getUuid("account_id"), company)!!,
         storeRepository.findOne(rs.getInt("profit_center_id_sfk"), company)!!
      )
   }
   private fun buildFilterString(begin: Boolean, end: Boolean, beginningParam: String, endingParam: String): String {
      return if (begin && end) " BETWEEN :$beginningParam AND :$endingParam "
      else if (begin) " >= :$beginningParam "
      else " <= :$endingParam "
   }


}
