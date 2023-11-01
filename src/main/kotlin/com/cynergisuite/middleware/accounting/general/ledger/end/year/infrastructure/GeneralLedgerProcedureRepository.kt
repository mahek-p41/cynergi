package com.cynergisuite.middleware.accounting.general.ledger.end.year.infrastructure

import com.cynergisuite.extensions.queryFullList
import com.cynergisuite.extensions.update
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.transaction.annotation.ReadOnly
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

   @ReadOnly
   fun findProfitCentersWithNetIncome(company: CompanyEntity): List<Int> {
      return jdbc.queryFullList(
         """
            SELECT profit_center_id_sfk AS profit_center
            FROM general_ledger_summary summary
               JOIN account ON summary.account_id = account.id AND account.deleted = FALSE
               JOIN account_type_domain type ON account.type_id = type.id
            WHERE summary.company_id = :comp_id
               AND type.value IN ('R', 'E')
               AND summary.overall_period_id = 3
            GROUP BY profit_center_id_sfk
            HAVING COALESCE(SUM(closing_balance), 0) <> 0
            ORDER BY profit_center
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id
         )
      ) { rs, _, elements ->
         do {
            elements.add(rs.getInt("profit_center"))
         } while (rs.next())
      }
   }

   @Transactional
   fun createBalEntriesForAssetLiabilityCapitalAccounts(params: Map<String, Any>): Int {
      logger.debug("Creating BAL entries for Asset, Liability, and Capital Accounts {}", params)
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
      logger.info("Inserted {} general_ledger_detail rows.", affectedRows)
      return affectedRows
   }

   fun createBalEntryForRetainedEarningsAccountForCorporateProfitCenter(params: Map<String, Any>): Int {
      logger.debug("Creating BAL entries for Retained Earnings Account for Corporate Profit Center {}", params)
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
                    glSummary.closing_balance + :corporate_net_income,
                    'Balance forward from the previous year',
                    :emp_number,
                    :je_number
         FROM general_ledger_summary glSummary
               JOIN company comp ON glSummary.company_id = comp.id AND comp.deleted = FALSE
               JOIN account acct ON glSummary.account_id = acct.id AND acct.deleted = FALSE
               JOIN account_type_domain accType ON accType.id = acct.type_id
         WHERE glSummary.overall_period_id = 3
             AND glSummary.company_id = :comp_id
             AND glSummary.account_id = :retained_earnings_account
             AND glSummary.profit_center_id_sfk = :profit_center
         ORDER BY glSummary.id
         """.trimIndent(),
         params
      )
      logger.info("Inserted {} general_ledger_detail rows.", affectedRows)
      return affectedRows
   }

   fun createBalEntriesForRetainedEarningsAccountForOtherProfitCenters(params: Map<String, Any>): Int {
      logger.debug("Creating BAL entries for Retained Earnings Account for other Profit Centers {}", params)
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
      logger.info("Inserted {} general_ledger_detail rows.", affectedRows)
      return affectedRows
   }

   fun createBalEntriesForRetainedEarningsAccountForEachProfitCenter(params: Map<String, Any>): Int {
      logger.debug("Creating BAL entries for Retained Earnings Account for each Profit Center {}", params)
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
                    glSummary.closing_balance + netIncomePerProfitCenter.net_income,
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
                     GROUP BY profit_center_id_sfk
                     ORDER BY profit_center
               ) AS netIncomePerProfitCenter ON glSummary.profit_center_id_sfk = netIncomePerProfitCenter.profit_center
         WHERE glSummary.overall_period_id = 3
             AND glSummary.company_id = :comp_id
             AND glSummary.account_id = :retained_earnings_account
         ORDER BY glSummary.id
         """.trimIndent(),
         params
      )
      logger.info("Inserted {} general_ledger_detail rows.", affectedRows)
      return affectedRows
   }

}
