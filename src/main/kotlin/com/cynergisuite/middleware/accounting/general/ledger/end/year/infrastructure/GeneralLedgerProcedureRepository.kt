package com.cynergisuite.middleware.accounting.general.ledger.end.year.infrastructure

import com.cynergisuite.extensions.update
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.transaction.Transactional

@Singleton
class GeneralLedgerProcedureRepository @Inject constructor(
   private val jdbc: Jdbi,
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerProcedureRepository::class.java)

   @Transactional
   fun createBalanceForwardGLDetailsForAssetLiabilityCapitalAccounts(params: Map<String, Any>): Int {
      logger.debug("Creating Balance Forward GL Details for Asset, Liability, and Capital Accounts {}", params)
      val affectedRows = jdbc.update(
         """
         INSERT INTO general_ledger_detail(
                     company_id,
                     account_id,
                     profit_center_id_sfk,
                     date,
                     source_id,
                     amount,
                     message,
                     employee_number_id_sfk,
                     journal_entry_number
         )
         SELECT     glSummary.company_id,
                    glSummary.account_id,
                    glSummary.profit_center_id_sfk,
                    :gl_date,
                    (SELECT id FROM general_ledger_source_codes WHERE company_id = :comp_id AND deleted = false AND value = 'BAL'),
                    glSummary.closing_balance,
                    'Balance forward from the previous year',
                    :emp_number,
                    :je_number
         FROM general_ledger_summary glSummary
               JOIN company comp ON glSummary.company_id = comp.id AND comp.deleted = FALSE
               JOIN account acct ON glSummary.account_id = acct.id AND acct.deleted = FALSE
               JOIN account_type_domain accType ON accType.id = acct.type_id
         WHERE accType.value in ('A', 'L', 'C')
             AND glSummary.overall_period_id = 3
             AND glSummary.company_id = :comp_id
             AND glSummary.closing_balance <> 0
             AND glSummary.account_id <> :retained_earnings_account
         ORDER BY glSummary.id
         """.trimIndent(),
         params
      )
      logger.info("Inserted general_ledger_detail rows {}", affectedRows)
      return affectedRows
   }

   fun createBalanceForwardGLDetailsForRetainedEarningsAccountForCorporateProfitCenter(params: Map<String, Any>): Int {
      logger.debug("Creating Balance Forward GL Detail for Retained Earnings Account for Corporate Profit Center {}", params)
      val affectedRows = jdbc.update(
         """
         INSERT INTO general_ledger_detail(
                     company_id,
                     account_id,
                     profit_center_id_sfk,
                     date,
                     source_id,
                     amount,
                     message,
                     employee_number_id_sfk,
                     journal_entry_number
         )
         SELECT     glSummary.company_id,
                    glSummary.account_id,
                    glSummary.profit_center_id_sfk,
                    :gl_date,
                    (SELECT id FROM general_ledger_source_codes WHERE company_id = :comp_id AND deleted = false AND value = 'BAL'),
                    :corporate_net_income,
                    'Balance forward from the previous year',
                    :emp_number,
                    :je_number
         FROM general_ledger_summary glSummary
               JOIN company comp ON glSummary.company_id = comp.id AND comp.deleted = FALSE
               JOIN account acct ON glSummary.account_id = acct.id AND acct.deleted = FALSE
               JOIN account_type_domain accType ON accType.id = acct.type_id
         WHERE glSummary.overall_period_id = 3
             AND glSummary.company_id = :comp_id
             AND glSummary.closing_balance <> 0
             AND glSummary.account_id = :retained_earnings_account
             AND glSummary.profit_center_id_sfk = :profit_center
         ORDER BY glSummary.id
         """.trimIndent(),
         params
      )
      logger.info("Inserted general_ledger_detail rows {}", affectedRows)
      return affectedRows
   }

   fun createBalanceForwardGLDetailsForRetainedEarningsAccountForOtherProfitCenters(params: Map<String, Any>): Int {
      logger.debug("Creating Balance Forward GL Details for Retained Earnings Account for other Profit Centers {}", params)
      val affectedRows = jdbc.update(
         """
         INSERT INTO general_ledger_detail(
                     company_id,
                     account_id,
                     profit_center_id_sfk,
                     date,
                     source_id,
                     amount,
                     message,
                     employee_number_id_sfk,
                     journal_entry_number
         )
         SELECT     glSummary.company_id,
                    glSummary.account_id,
                    glSummary.profit_center_id_sfk,
                    :gl_date,
                    (SELECT id FROM general_ledger_source_codes WHERE company_id = :comp_id AND deleted = false AND value = 'BAL'),
                    glSummary.closing_balance,
                    'Balance forward from the previous year',
                    :emp_number,
                    :je_number
         FROM general_ledger_summary glSummary
               JOIN company comp ON glSummary.company_id = comp.id AND comp.deleted = FALSE
               JOIN account acct ON glSummary.account_id = acct.id AND acct.deleted = FALSE
               JOIN account_type_domain accType ON accType.id = acct.type_id
         WHERE glSummary.overall_period_id = 3
             AND glSummary.company_id = :comp_id
             AND glSummary.closing_balance <> 0
             AND glSummary.account_id = :retained_earnings_account
             AND glSummary.profit_center_id_sfk <> :profit_center
         ORDER BY glSummary.id
         """.trimIndent(),
         params
      )
      logger.info("Inserted general_ledger_detail rows {}", affectedRows)
      return affectedRows
   }

   fun createBalanceForwardGLDetailsForRetainedEarningsAccountForEachProfitCenter(params: Map<String, Any>): Int {
      logger.debug("Creating Balance Forward GL Details for Retained Earnings Account for other Profit Centers {}", params)
      val affectedRows = jdbc.update(
         """
         INSERT INTO general_ledger_detail(
                     company_id,
                     account_id,
                     profit_center_id_sfk,
                     date,
                     source_id,
                     amount,
                     message,
                     employee_number_id_sfk,
                     journal_entry_number
         )
         SELECT     glSummary.company_id,
                    glSummary.account_id,
                    glSummary.profit_center_id_sfk,
                    :gl_date,
                    (SELECT id FROM general_ledger_source_codes WHERE company_id = :comp_id AND deleted = false AND value = 'BAL'),
                    netIncomePerProfitCenter.net_income,
                    'Balance forward from the previous year',
                    :emp_number,
                    :je_number
         FROM general_ledger_summary glSummary
               JOIN company comp ON glSummary.company_id = comp.id AND comp.deleted = FALSE
               JOIN account acct ON glSummary.account_id = acct.id AND acct.deleted = FALSE
               JOIN account_type_domain accType ON accType.id = acct.type_id
               JOIN (SELECT   profit_center_id_sfk              AS profit_center,
                              COALESCE(SUM(closing_balance), 0) AS net_income
                     FROM general_ledger_summary summary
                        JOIN account ON summary.account_id = account.id AND account.deleted = FALSE
                        JOIN account_type_domain type ON account.type_id = type.id
                     WHERE summary.company_id = :comp_id
                        AND type.value IN ('R', 'E')
                        AND summary.overall_period_id = 3
                        AND summary.account_id = :retained_earnings_account
                     GROUP BY profit_center_id_sfk
                     ORDER BY profit_center
               ) AS netIncomePerProfitCenter ON glSummary.profit_center_id_sfk = netIncomePerProfitCenter.profit_center
         WHERE glSummary.overall_period_id = 3
             AND glSummary.company_id = :comp_id
             AND glSummary.closing_balance <> 0
             AND glSummary.account_id = :retained_earnings_account
         ORDER BY glSummary.id
         """.trimIndent(),
         params
      )
      logger.info("Inserted general_ledger_detail rows {}", affectedRows)
      return affectedRows
   }

}
