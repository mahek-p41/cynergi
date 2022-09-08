package com.cynergisuite.middleware.accounting.general.ledger.inquiry.infrastructure

import com.cynergisuite.domain.GeneralLedgerInquiryFilterRequest
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getBigDecimalOrNull
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
            SUM(glSummary.profit_center_id_sfk)                            AS glSummary_profit_center_id_sfk,
            SUM(glSummary.net_activity_period_1)                           AS glSummary_net_activity_period_1,
            SUM(glSummary.net_activity_period_2)                           AS glSummary_net_activity_period_2,
            SUM(glSummary.net_activity_period_3)                           AS glSummary_net_activity_period_3,
            SUM(glSummary.net_activity_period_4)                           AS glSummary_net_activity_period_4,
            SUM(glSummary.net_activity_period_5)                           AS glSummary_net_activity_period_5,
            SUM(glSummary.net_activity_period_6)                           AS glSummary_net_activity_period_6,
            SUM(glSummary.net_activity_period_7)                           AS glSummary_net_activity_period_7,
            SUM(glSummary.net_activity_period_8)                           AS glSummary_net_activity_period_8,
            SUM(glSummary.net_activity_period_9)                           AS glSummary_net_activity_period_9,
            SUM(glSummary.net_activity_period_10)                          AS glSummary_net_activity_period_10,
            SUM(glSummary.net_activity_period_11)                          AS glSummary_net_activity_period_11,
            SUM(glSummary.net_activity_period_12)                          AS glSummary_net_activity_period_12,
            SUM(glSummary.beginning_balance)                               AS glSummary_beginning_balance,
            SUM(glSummary.closing_balance)                                 AS glSummary_closing_balance,
            overallPeriod.id                                               AS glSummary_overallPeriod_id,
            overallPeriod.value                                            AS glSummary_overallPeriod_value,
            overallPeriod.abbreviation                                     AS glSummary_overallPeriod_abbreviation,
            overallPeriod.description                                      AS glSummary_overallPeriod_description,
            overallPeriod.localization_code                                AS glSummary_overallPeriod_localization_code,
            SUM(glSummaryPrior.profit_center_id_sfk)                       AS glSummaryPrior_profit_center_id_sfk,
            SUM(glSummaryPrior.net_activity_period_1)                      AS glSummaryPrior_net_activity_period_1,
            SUM(glSummaryPrior.net_activity_period_2)                      AS glSummaryPrior_net_activity_period_2,
            SUM(glSummaryPrior.net_activity_period_3)                      AS glSummaryPrior_net_activity_period_3,
            SUM(glSummaryPrior.net_activity_period_4)                      AS glSummaryPrior_net_activity_period_4,
            SUM(glSummaryPrior.net_activity_period_5)                      AS glSummaryPrior_net_activity_period_5,
            SUM(glSummaryPrior.net_activity_period_6)                      AS glSummaryPrior_net_activity_period_6,
            SUM(glSummaryPrior.net_activity_period_7)                      AS glSummaryPrior_net_activity_period_7,
            SUM(glSummaryPrior.net_activity_period_8)                      AS glSummaryPrior_net_activity_period_8,
            SUM(glSummaryPrior.net_activity_period_9)                      AS glSummaryPrior_net_activity_period_9,
            SUM(glSummaryPrior.net_activity_period_10)                     AS glSummaryPrior_net_activity_period_10,
            SUM(glSummaryPrior.net_activity_period_11)                     AS glSummaryPrior_net_activity_period_11,
            SUM(glSummaryPrior.net_activity_period_12)                     AS glSummaryPrior_net_activity_period_12,
            SUM(glSummaryPrior.beginning_balance)                          AS glSummaryPrior_beginning_balance,
            SUM(glSummaryPrior.closing_balance)                            AS glSummaryPrior_closing_balance,
            priorOverallPeriod.id                                          AS glSummaryPrior_overallPeriod_id,
            priorOverallPeriod.value                                       AS glSummaryPrior_overallPeriod_value,
            priorOverallPeriod.abbreviation                                AS glSummaryPrior_overallPeriod_abbreviation,
            priorOverallPeriod.description                                 AS glSummaryPrior_overallPeriod_description,
            priorOverallPeriod.localization_code                           AS glSummaryPrior_overallPeriod_localization_code
         FROM general_ledger_summary glSummary
            JOIN company comp ON glSummary.company_id = comp.id AND comp.deleted = FALSE
            JOIN fastinfo_prod_import.store_vw profitCenter
                    ON profitCenter.dataset = comp.dataset_code
                       AND profitCenter.id = glSummary.profit_center_id_sfk
            JOIN account acct ON glSummary.account_id = acct.account_id AND acct.account_deleted = FALSE
            JOIN overall_period_type_domain overallPeriod ON glSummary.overall_period_id = overallPeriod.id
            JOIN general_ledger_summary glSummaryPrior ON glSummary.company_id = glSummaryPrior.company_id
                  AND glSummary.profit_center_id_sfk = glSummaryPrior.profit_center_id_sfk
                  AND glSummary.account_id = glSummaryPrior.account_id
            JOIN overall_period_type_domain priorOverallPeriod ON glSummaryPrior.overall_period_id = priorOverallPeriod.id

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
      val whereClause = StringBuilder("WHERE glSummary.company_id = :comp_id AND overallPeriod.value IN ('C') AND priorOverallPeriod.value IN ('P') ")
      val havingClause = " GROUP BY overallPeriod.id, priorOverallPeriod.id "

      if (filterRequest.profitCenter != null) {
         params["profitCenter"] = filterRequest.profitCenter
         whereClause.append(" AND profitCenter.id = :profitCenter")
      }
      if (filterRequest.account != null) {
         params["account"] = filterRequest.account
         whereClause.append(" AND acct.account_number = :account")
      }
      if (filterRequest.fiscalYear != null) {
         params["fiscalYear"] = filterRequest.fiscalYear
         whereClause.append(" AND glSummary.overall_period_id = (SELECT DISTINCT overall_period_id FROM financial_calendar WHERE fiscal_year = :fiscalYear) ")
      }
      val query =
         "${selectBaseQuery()}\n$whereClause\n$havingClause"

      logger.debug("Searching for GeneralLedgerInquiry using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ ->
         val generalLedgerInquiry = mapRow(rs, "glSummary_", "glSummaryPrior_")

         generalLedgerInquiry
      }

      logger.trace("Searching for GeneralLedgerInquiry resulted in {}", found)

      return found
   }

   private fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY, priorColumnPrefix: String = EMPTY): GeneralLedgerInquiryEntity {
      val netActivityPeriods: MutableList<BigDecimal> = mutableListOf()
      netActivityPeriods.add(rs.getBigDecimalOrNull("${columnPrefix}net_activity_period_1") ?: BigDecimal.ZERO)
      netActivityPeriods.add(rs.getBigDecimalOrNull("${columnPrefix}net_activity_period_2") ?: BigDecimal.ZERO)
      netActivityPeriods.add(rs.getBigDecimalOrNull("${columnPrefix}net_activity_period_3") ?: BigDecimal.ZERO)
      netActivityPeriods.add(rs.getBigDecimalOrNull("${columnPrefix}net_activity_period_4") ?: BigDecimal.ZERO)
      netActivityPeriods.add(rs.getBigDecimalOrNull("${columnPrefix}net_activity_period_5") ?: BigDecimal.ZERO)
      netActivityPeriods.add(rs.getBigDecimalOrNull("${columnPrefix}net_activity_period_6") ?: BigDecimal.ZERO)
      netActivityPeriods.add(rs.getBigDecimalOrNull("${columnPrefix}net_activity_period_7") ?: BigDecimal.ZERO)
      netActivityPeriods.add(rs.getBigDecimalOrNull("${columnPrefix}net_activity_period_8") ?: BigDecimal.ZERO)
      netActivityPeriods.add(rs.getBigDecimalOrNull("${columnPrefix}net_activity_period_9") ?: BigDecimal.ZERO)
      netActivityPeriods.add(rs.getBigDecimalOrNull("${columnPrefix}net_activity_period_10") ?: BigDecimal.ZERO)
      netActivityPeriods.add(rs.getBigDecimalOrNull("${columnPrefix}net_activity_period_11") ?: BigDecimal.ZERO)
      netActivityPeriods.add(rs.getBigDecimalOrNull("${columnPrefix}net_activity_period_12") ?: BigDecimal.ZERO)

      val priorNetActivityPeriods: MutableList<BigDecimal> = mutableListOf()
      priorNetActivityPeriods.add(rs.getBigDecimalOrNull("${priorColumnPrefix}net_activity_period_1") ?: BigDecimal.ZERO)
      priorNetActivityPeriods.add(rs.getBigDecimalOrNull("${priorColumnPrefix}net_activity_period_2") ?: BigDecimal.ZERO)
      priorNetActivityPeriods.add(rs.getBigDecimalOrNull("${priorColumnPrefix}net_activity_period_3") ?: BigDecimal.ZERO)
      priorNetActivityPeriods.add(rs.getBigDecimalOrNull("${priorColumnPrefix}net_activity_period_4") ?: BigDecimal.ZERO)
      priorNetActivityPeriods.add(rs.getBigDecimalOrNull("${priorColumnPrefix}net_activity_period_5") ?: BigDecimal.ZERO)
      priorNetActivityPeriods.add(rs.getBigDecimalOrNull("${priorColumnPrefix}net_activity_period_6") ?: BigDecimal.ZERO)
      priorNetActivityPeriods.add(rs.getBigDecimalOrNull("${priorColumnPrefix}net_activity_period_7") ?: BigDecimal.ZERO)
      priorNetActivityPeriods.add(rs.getBigDecimalOrNull("${priorColumnPrefix}net_activity_period_8") ?: BigDecimal.ZERO)
      priorNetActivityPeriods.add(rs.getBigDecimalOrNull("${priorColumnPrefix}net_activity_period_9") ?: BigDecimal.ZERO)
      priorNetActivityPeriods.add(rs.getBigDecimalOrNull("${priorColumnPrefix}net_activity_period_10") ?: BigDecimal.ZERO)
      priorNetActivityPeriods.add(rs.getBigDecimalOrNull("${priorColumnPrefix}net_activity_period_11") ?: BigDecimal.ZERO)
      priorNetActivityPeriods.add(rs.getBigDecimalOrNull("${priorColumnPrefix}net_activity_period_12") ?: BigDecimal.ZERO)

      return GeneralLedgerInquiryEntity(
         overallPeriod = overallPeriodTypeRepository.mapRow(rs, "${columnPrefix}overallPeriod_"),
         netActivityPeriod = netActivityPeriods,
         beginningBalance = rs.getBigDecimalOrNull("${columnPrefix}beginning_balance") ?: BigDecimal.ZERO,
         closingBalance = rs.getBigDecimalOrNull("${columnPrefix}closing_balance") ?: BigDecimal.ZERO,
         priorNetActivityPeriod = priorNetActivityPeriods,
         priorOverallPeriod = overallPeriodTypeRepository.mapRow(rs, "${priorColumnPrefix}overallPeriod_"),
         priorBeginningBalance = rs.getBigDecimalOrNull("${priorColumnPrefix}beginning_balance") ?: BigDecimal.ZERO,
         priorClosingBalance = rs.getBigDecimalOrNull("${priorColumnPrefix}closing_balance") ?: BigDecimal.ZERO,
      )
   }
}
