package com.cynergisuite.middleware.accounting.general.ledger.inquiry.infrastructure

import com.cynergisuite.domain.GeneralLedgerInquiryFilterRequest
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.financial.calendar.type.infrastructure.OverallPeriodTypeRepository
import com.cynergisuite.middleware.accounting.general.ledger.inquiry.GeneralLedgerInquiryEntity
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.sql.ResultSet

@Singleton
class GeneralLedgerInquiryRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val accountRepository: AccountRepository,
   private val overallPeriodTypeRepository: OverallPeriodTypeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerInquiryRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         WITH account AS (
            ${accountRepository.selectBaseQuery()}
         )
         SELECT
            SUM (COALESCE(glSummary.net_activity_period_1, 0))                       AS glSummary_net_activity_period_1,
            SUM (COALESCE(glSummary.net_activity_period_2, 0))                       AS glSummary_net_activity_period_2,
            SUM (COALESCE(glSummary.net_activity_period_3, 0))                       AS glSummary_net_activity_period_3,
            SUM (COALESCE(glSummary.net_activity_period_4, 0))                       AS glSummary_net_activity_period_4,
            SUM (COALESCE(glSummary.net_activity_period_5, 0))                       AS glSummary_net_activity_period_5,
            SUM (COALESCE(glSummary.net_activity_period_6, 0))                       AS glSummary_net_activity_period_6,
            SUM (COALESCE(glSummary.net_activity_period_7, 0))                       AS glSummary_net_activity_period_7,
            SUM (COALESCE(glSummary.net_activity_period_8, 0))                       AS glSummary_net_activity_period_8,
            SUM (COALESCE(glSummary.net_activity_period_9, 0))                       AS glSummary_net_activity_period_9,
            SUM (COALESCE(glSummary.net_activity_period_10, 0))                      AS glSummary_net_activity_period_10,
            SUM (COALESCE(glSummary.net_activity_period_11, 0))                      AS glSummary_net_activity_period_11,
            SUM (COALESCE(glSummary.net_activity_period_12, 0))                      AS glSummary_net_activity_period_12,
            SUM (COALESCE(glSummary.beginning_balance, 0))                           AS glSummary_beginning_balance,
            SUM (COALESCE(glSummary.closing_balance, 0))                             AS glSummary_closing_balance,
            overallPeriod.id                                                         AS glSummary_overallPeriod_id,
            overallPeriod.value                                                      AS glSummary_overallPeriod_value,
            overallPeriod.abbreviation                                               AS glSummary_overallPeriod_abbreviation,
            overallPeriod.description                                                AS glSummary_overallPeriod_description,
            overallPeriod.localization_code                                          AS glSummary_overallPeriod_localization_code,
            SUM (COALESCE(glSummaryPrior.net_activity_period_1, 0))                  AS glSummaryPrior_net_activity_period_1,
            SUM (COALESCE(glSummaryPrior.net_activity_period_2, 0))                  AS glSummaryPrior_net_activity_period_2,
            SUM (COALESCE(glSummaryPrior.net_activity_period_3, 0))                  AS glSummaryPrior_net_activity_period_3,
            SUM (COALESCE(glSummaryPrior.net_activity_period_4, 0))                  AS glSummaryPrior_net_activity_period_4,
            SUM (COALESCE(glSummaryPrior.net_activity_period_5, 0))                  AS glSummaryPrior_net_activity_period_5,
            SUM (COALESCE(glSummaryPrior.net_activity_period_6, 0))                  AS glSummaryPrior_net_activity_period_6,
            SUM (COALESCE(glSummaryPrior.net_activity_period_7, 0))                  AS glSummaryPrior_net_activity_period_7,
            SUM (COALESCE(glSummaryPrior.net_activity_period_8, 0))                  AS glSummaryPrior_net_activity_period_8,
            SUM (COALESCE(glSummaryPrior.net_activity_period_9, 0))                  AS glSummaryPrior_net_activity_period_9,
            SUM (COALESCE(glSummaryPrior.net_activity_period_10, 0))                 AS glSummaryPrior_net_activity_period_10,
            SUM (COALESCE(glSummaryPrior.net_activity_period_11, 0))                 AS glSummaryPrior_net_activity_period_11,
            SUM (COALESCE(glSummaryPrior.net_activity_period_12, 0))                 AS glSummaryPrior_net_activity_period_12,
            SUM (COALESCE(glSummaryPrior.beginning_balance, 0))                      AS glSummaryPrior_beginning_balance,
            SUM (COALESCE(glSummaryPrior.closing_balance, 0))                        AS glSummaryPrior_closing_balance,
            priorOverallPeriod.id                                                    AS glSummaryPrior_overallPeriod_id,
            priorOverallPeriod.value                                                 AS glSummaryPrior_overallPeriod_value,
            priorOverallPeriod.abbreviation                                          AS glSummaryPrior_overallPeriod_abbreviation,
            priorOverallPeriod.description                                           AS glSummaryPrior_overallPeriod_description,
            priorOverallPeriod.localization_code                                     AS glSummaryPrior_overallPeriod_localization_code
         FROM general_ledger_summary glSummary
            JOIN company comp ON glSummary.company_id = comp.id AND comp.deleted = FALSE
            JOIN fastinfo_prod_import.store_vw profitCenter
                    ON profitCenter.dataset = comp.dataset_code
                       AND profitCenter.number = glSummary.profit_center_id_sfk
            JOIN account acct ON glSummary.account_id = acct.account_id AND acct.account_deleted = FALSE
            JOIN overall_period_type_domain overallPeriod ON glSummary.overall_period_id = overallPeriod.id
            LEFT JOIN general_ledger_summary glSummaryPrior ON glSummary.company_id = glSummaryPrior.company_id
                  AND glSummary.profit_center_id_sfk = glSummaryPrior.profit_center_id_sfk
                  AND glSummary.account_id = glSummaryPrior.account_id
                  AND glSummary.overall_period_id = glSummaryPrior.overall_period_id + 1
            LEFT JOIN overall_period_type_domain priorOverallPeriod ON glSummaryPrior.overall_period_id = priorOverallPeriod.id
      """
   }

   @ReadOnly
   fun exists(company: CompanyEntity, accountId: Long, profitCenterId: Long, overallPeriodId: Long): Boolean {
      val params = mutableMapOf(
         "comp_id" to company.id,
         "accountId" to accountId,
         "profitCenterId" to profitCenterId,
         "overallPeriodId" to overallPeriodId
      )
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
   fun findOne(company: CompanyEntity, filterRequest: GeneralLedgerInquiryFilterRequest): GeneralLedgerInquiryEntity? {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val whereClause = StringBuilder("WHERE glSummary.company_id = :comp_id ")
      val groupBy = " GROUP BY overallPeriod.id, priorOverallPeriod.id "

      if (filterRequest.profitCenter != null) {
         params["profitCenter"] = filterRequest.profitCenter
         whereClause.append(" AND profitCenter.number = :profitCenter")
      }
      if (filterRequest.account != null) {
         params["account"] = filterRequest.account
         whereClause.append(" AND acct.account_number = :account")
      }
      if (filterRequest.fiscalYear != null) {
         params["fiscalYear"] = filterRequest.fiscalYear
         whereClause.append(" AND glSummary.overall_period_id = (SELECT DISTINCT overall_period_id FROM financial_calendar WHERE fiscal_year = :fiscalYear AND company_id = :comp_id) ")
      }
      val query = "${selectBaseQuery()}\n$whereClause\n$groupBy"

      logger.info("Searching for GeneralLedgerInquiry using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ ->
         val generalLedgerInquiry = mapRow(rs, "glSummary_", "glSummaryPrior_")

         generalLedgerInquiry
      }

      logger.info("Searching for GeneralLedgerInquiry resulted in {}", found)

      return found
   }

   private fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY, priorColumnPrefix: String = EMPTY): GeneralLedgerInquiryEntity {
      val netActivityPeriods: MutableList<BigDecimal> = mutableListOf()
      netActivityPeriods.add(rs.getBigDecimal("${columnPrefix}net_activity_period_1"))
      netActivityPeriods.add(rs.getBigDecimal("${columnPrefix}net_activity_period_2"))
      netActivityPeriods.add(rs.getBigDecimal("${columnPrefix}net_activity_period_3"))
      netActivityPeriods.add(rs.getBigDecimal("${columnPrefix}net_activity_period_4"))
      netActivityPeriods.add(rs.getBigDecimal("${columnPrefix}net_activity_period_5"))
      netActivityPeriods.add(rs.getBigDecimal("${columnPrefix}net_activity_period_6"))
      netActivityPeriods.add(rs.getBigDecimal("${columnPrefix}net_activity_period_7"))
      netActivityPeriods.add(rs.getBigDecimal("${columnPrefix}net_activity_period_8"))
      netActivityPeriods.add(rs.getBigDecimal("${columnPrefix}net_activity_period_9"))
      netActivityPeriods.add(rs.getBigDecimal("${columnPrefix}net_activity_period_10"))
      netActivityPeriods.add(rs.getBigDecimal("${columnPrefix}net_activity_period_11"))
      netActivityPeriods.add(rs.getBigDecimal("${columnPrefix}net_activity_period_12"))

      val priorNetActivityPeriods: MutableList<BigDecimal> = mutableListOf()
      priorNetActivityPeriods.add(rs.getBigDecimal("${priorColumnPrefix}net_activity_period_1"))
      priorNetActivityPeriods.add(rs.getBigDecimal("${priorColumnPrefix}net_activity_period_2"))
      priorNetActivityPeriods.add(rs.getBigDecimal("${priorColumnPrefix}net_activity_period_3"))
      priorNetActivityPeriods.add(rs.getBigDecimal("${priorColumnPrefix}net_activity_period_4"))
      priorNetActivityPeriods.add(rs.getBigDecimal("${priorColumnPrefix}net_activity_period_5"))
      priorNetActivityPeriods.add(rs.getBigDecimal("${priorColumnPrefix}net_activity_period_6"))
      priorNetActivityPeriods.add(rs.getBigDecimal("${priorColumnPrefix}net_activity_period_7"))
      priorNetActivityPeriods.add(rs.getBigDecimal("${priorColumnPrefix}net_activity_period_8"))
      priorNetActivityPeriods.add(rs.getBigDecimal("${priorColumnPrefix}net_activity_period_9"))
      priorNetActivityPeriods.add(rs.getBigDecimal("${priorColumnPrefix}net_activity_period_10"))
      priorNetActivityPeriods.add(rs.getBigDecimal("${priorColumnPrefix}net_activity_period_11"))
      priorNetActivityPeriods.add(rs.getBigDecimal("${priorColumnPrefix}net_activity_period_12"))

      return GeneralLedgerInquiryEntity(
         overallPeriod = overallPeriodTypeRepository.mapRow(rs, "${columnPrefix}overallPeriod_"),
         netActivityPeriod = netActivityPeriods,
         beginningBalance = rs.getBigDecimal("${columnPrefix}beginning_balance"),
         closingBalance = rs.getBigDecimal("${columnPrefix}closing_balance"),
         priorNetActivityPeriod = priorNetActivityPeriods,
         priorOverallPeriod = overallPeriodTypeRepository.mapRowOrNull(rs, "${priorColumnPrefix}overallPeriod_"),
         priorBeginningBalance = rs.getBigDecimal("${priorColumnPrefix}beginning_balance"),
         priorClosingBalance = rs.getBigDecimal("${priorColumnPrefix}closing_balance"),
      )
   }
}
