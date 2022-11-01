package com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure

import com.cynergisuite.domain.GeneralLedgerProfitCenterTrialBalanceReportFilterRequest
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.accounting.financial.calendar.type.infrastructure.OverallPeriodTypeRepository
import com.cynergisuite.middleware.accounting.general.ledger.summary.GeneralLedgerSummaryEntity
import com.cynergisuite.middleware.company.CompanyEntity
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
class GeneralLedgerSummaryRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val accountRepository: AccountRepository,
   private val financialCalendarRepository: FinancialCalendarRepository,
   private val storeRepository: StoreRepository,
   private val overallPeriodTypeRepository: OverallPeriodTypeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerSummaryRepository::class.java)

   private fun selectBaseQuery(): String {
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
   fun exists(company: CompanyEntity, accountId: Long, profitCenterId: Long, overallPeriodId: Long): Boolean {
      val params = mutableMapOf("comp_id" to company.id, "accountId" to accountId, "profitCenterId" to profitCenterId, "overallPeriodId" to overallPeriodId)
      val exists = jdbc.queryForObject(
         """
         SELECT EXISTS (SELECT id
                        FROM general_ledger_summary
                        WHERE company_id = :comp_id AND account_id = :accountId AND profit_center_id_sfk = :profitCenterId AND overall_period_id = :overallPeriodId)
         """.trimIndent(),
         params,
         Boolean::class.java
      )

      logger.trace("Checking if general_ledger_summary: {} exists resulted in {}", params, exists)

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
   fun fetchProfitCenterTrialBalanceReportRecords(company: CompanyEntity, filterRequest: GeneralLedgerProfitCenterTrialBalanceReportFilterRequest): List<GeneralLedgerSummaryEntity> {
      val pair = financialCalendarRepository.findOverallPeriodIdAndPeriod(company, filterRequest.startingDate!!)
      val overallPeriodId = pair.first
      val period = pair.second
      val glSummaries = mutableListOf<GeneralLedgerSummaryEntity>()
      val whereClause = StringBuilder(
         "WHERE glSummary.company_id = :company_id " +
         "AND glSummary.overall_period_id = :overall_period_id " +
         "AND (glSummary.account_id BETWEEN :starting_account AND :ending_account) " +
         "AND (" +
            "glSummary_acct_status_id = 1 OR " +
            "glSummary.beginning_balance <> 0.00 OR " +
            "glSummary.net_activity_period_$period <> 0.00" +
         ") " +
         "AND glSummary.deleted = FALSE"
      )

      // select locations based on criteria (1 selects all locations)
      when (filterRequest.selectLocsBy) {
         2 ->
            whereClause.append(" AND glSummary.profit_center_id_sfk IN :any_10_locs_or_groups")
         3 ->
            whereClause.append(" AND glSummary.profit_center_id_sfk BETWEEN :starting_loc_or_group AND :ending_loc_or_group")
         // todo: 4 & 5 use location groups
      }

      // assign sort by
      val sortBy = StringBuilder("ORDER BY ")
      when (filterRequest.sortBy) {
         "location" ->
            sortBy.append("glSummary.profit_center_id_sfk ASC, glSummary.account_id ASC")
         "account" ->
            sortBy.append("glSummary.account_id ASC, glSummary.profit_center_id_sfk ASC")
      }

      jdbc.query(
         """
            ${selectBaseQuery()}
            $whereClause
            $sortBy
         """.trimIndent(),
         mapOf(
            "company_id" to company.id,
            "overall_period_id" to overallPeriodId,
            "starting_account" to filterRequest.startingAccount,
            "ending_account" to filterRequest.endingAccount,
            "any_10_locs_or_groups" to filterRequest.any10LocsOrGroups,
            "starting_loc_or_group" to filterRequest.startingLocOrGroup,
            "ending_loc_or_group" to filterRequest.endingLocOrGroup
         )
      ) { rs, _ ->
         do {
            glSummaries.add(mapRow(rs, company, "glSummary_"))
         } while (rs.next())
      }

      return glSummaries
   }

   private fun mapRow(rs: ResultSet, company: CompanyEntity, columnPrefix: String = EMPTY): GeneralLedgerSummaryEntity {
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
}
