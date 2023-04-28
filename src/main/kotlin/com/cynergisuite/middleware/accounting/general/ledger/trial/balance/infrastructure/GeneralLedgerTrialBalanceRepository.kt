package com.cynergisuite.middleware.accounting.general.ledger.trial.balance.infrastructure

import com.cynergisuite.domain.GeneralLedgerTrialBalanceReportFilterRequest
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.getUuidOrNull
import com.cynergisuite.extensions.query
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.inquiry.GeneralLedgerNetChangeDTO
import com.cynergisuite.middleware.accounting.general.ledger.trial.balance.GeneralLedgerTrialBalanceReportAccountDTO
import com.cynergisuite.middleware.accounting.general.ledger.trial.balance.GeneralLedgerTrialBalanceReportDetailDTO
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.sql.ResultSet

@Singleton
class GeneralLedgerTrialBalanceRepository @Inject constructor(
   private val jdbc: Jdbi,
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerTrialBalanceRepository::class.java)

   fun selectTrialBalanceQuery(where: String): String {
      return """
         SELECT
             glSummary.company_id                                                            AS company_id,
             acct.id                                                                         AS account_id,
             acct.number                                                                     AS account_number,
             acct.name                                                                       AS account_name,
             acctType.value                                                                  AS account_type,
             glSummary.profit_center_id_sfk                                                  AS profit_center,
             CASE WHEN glDetail.date BETWEEN :from AND :thru
               AND glDetail.amount >= 0 THEN glDetail.amount ELSE 0 END                      AS debit,
             CASE WHEN glDetail.date BETWEEN :from AND :thru
               AND glDetail.amount < 0 THEN glDetail.amount ELSE 0 END                       AS credit,
             CASE WHEN glDetail.amount >= 0 THEN glDetail.amount ELSE 0 END                  AS ytd_debit,
             CASE WHEN glDetail.amount < 0 THEN glDetail.amount ELSE 0 END                   AS ytd_credit,
             CASE WHEN glDetail.date BETWEEN :from AND :thru
               THEN glDetail.amount ELSE 0 END                                               AS net_change,
             COALESCE(glSummary.beginning_balance, 0)                                        AS begin_balance1,
             CASE WHEN glDetail.date < :from THEN glDetail.amount ELSE 0 END                 AS begin_balance2,
             COALESCE(glSummary.beginning_balance, 0) + SUM(COALESCE(glDetail.amount, 0))    AS end_balance,
             glDetail.date                                                                   AS detail_date,
             glDetail.id                                                                     AS detail_id,
             glDetail.journal_entry_number                                                   AS detail_je,
             glDetail.amount                                                                 AS detail_amount,
             glDetail.message                                                                AS detail_message,
             glDetail.source_id                                                              AS detail_source_id,
             glDetail.source_value                                                           AS detail_source_value,
             glDetail.source_description                                                     AS detail_source_description
         FROM general_ledger_summary glSummary
                JOIN account acct ON glSummary.account_id = acct.id AND acct.deleted = FALSE
                JOIN account_type_domain acctType ON acct.type_id = acctType.id
                JOIN company comp ON glSummary.company_id = comp.id AND comp.deleted = FALSE
                LEFT JOIN bank ON bank.general_ledger_account_id = acct.id AND bank.deleted = FALSE
                LEFT JOIN (
                            SELECT
                                 glDetail.*,
                                 glSource.value             AS source_value,
                                 glSource.description       AS source_description
                            FROM general_ledger_detail glDetail
                                 JOIN general_ledger_source_codes glSource
                                       ON glDetail.source_id = glSource.id AND glSource.value <> 'BAL'
                            $where
               ) glDetail
                        ON glDetail.company_id = glSummary.company_id
                            AND glDetail.account_id = glSummary.account_id
                            AND glDetail.profit_center_id_sfk  = glSummary.profit_center_id_sfk
      """
   }

   @ReadOnly
   fun fetchReport(company: CompanyEntity, filterRequest: GeneralLedgerTrialBalanceReportFilterRequest): List<GeneralLedgerTrialBalanceReportAccountDTO> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val innerWhere = StringBuilder(" WHERE glSummary.company_id = :comp_id ")
      val subQueryWhere = StringBuilder(" WHERE glDetail.company_id = :comp_id AND glDetail.deleted = FALSE ")
      if (filterRequest.from != null && filterRequest.thru != null) {
         params["from"] = filterRequest.from
         params["thru"] = filterRequest.thru
         subQueryWhere.append(""" AND glDetail.date BETWEEN
               (SELECT MAX(period_from)
                  FROM financial_calendar
                  WHERE period_from <= :from AND company_id = :comp_id AND period = 1)
               AND :thru """)

         innerWhere.append(""" AND glSummary.overall_period_id =
               (SELECT DISTINCT overall_period_id
               FROM financial_calendar
               WHERE :from BETWEEN period_from AND period_to AND company_id = :comp_id)
            """.trimIndent())
      }
      if (filterRequest.profitCenter != null) {
         params["profitCenter"] = filterRequest.profitCenter
         subQueryWhere.append(" AND glDetail.profit_center_id_sfk = :profitCenter ")
         innerWhere.append(" AND glSummary.profit_center_id_sfk = :profitCenter ")
      }
      if (filterRequest.beginAccount != null && filterRequest.endAccount != null) {
         params["beginAccount"] = filterRequest.beginAccount
         params["endAccount"] = filterRequest.endAccount
         innerWhere.append(" AND (acct.number >= :beginAccount AND acct.number <= :endAccount) ")
         subQueryWhere.append("""
               AND glDetail.account_id IN (
                  SELECT DISTINCT id
                  FROM account
                  WHERE company_id = :comp_id
                        AND (account.number >= :beginAccount AND account.number <= :endAccount)
                        AND deleted = false
               ) """.trimMargin())
      }

      val mainQuery = """
         ${selectTrialBalanceQuery(subQueryWhere.toString())}
         $innerWhere
         GROUP BY glSummary.company_id, acct.id, acctType.value, glSummary.id, glSummary.profit_center_id_sfk,
            glDetail.id, glDetail.date, glDetail.journal_entry_number, glDetail.amount, glDetail.message,
            glDetail.source_id, glDetail.source_value, glDetail.source_description
      """.trimIndent()

      logger.info("Querying for General Ledger Trial Balance report:")
      val reportAccounts = mutableListOf<GeneralLedgerTrialBalanceReportAccountDTO>()
      var currentReportAccountDTO: GeneralLedgerTrialBalanceReportAccountDTO? = null
      // the map used to remove repeated begin balance values
      val beginBalancesMap: MutableMap<Pair<Int, Int>, BigDecimal> = mutableMapOf()
      jdbc.query(mainQuery, params) { rs, element  ->
         val tempReportAccount = if (currentReportAccountDTO?.accountID != rs.getUuid("account_id")) {
            val localReportAccount = mapRow(rs)
            reportAccounts.add(localReportAccount)
            currentReportAccountDTO = localReportAccount

            localReportAccount
         } else {
            currentReportAccountDTO!!
         }

         if (tempReportAccount.glTotals != null) {
            val otherGLTotals = mapGLTotals(rs)
            tempReportAccount.glTotals!!.debit += otherGLTotals.debit
            tempReportAccount.glTotals!!.credit += otherGLTotals.credit
            tempReportAccount.glTotals!!.ytdDebit += otherGLTotals.ytdDebit
            tempReportAccount.glTotals!!.ytdCredit += otherGLTotals.ytdCredit
            tempReportAccount.glTotals!!.beginBalance = otherGLTotals.beginBalance
            tempReportAccount.glTotals!!.beginBalance2 = tempReportAccount.glTotals!!.beginBalance2?.plus(otherGLTotals.beginBalance2!!)
            tempReportAccount.glTotals!!.netChange += otherGLTotals.netChange
         } else {
            tempReportAccount.glTotals = mapGLTotals(rs)
         }
         beginBalancesMap[Pair(tempReportAccount.accountNumber, rs.getInt("profit_center"))] = tempReportAccount.glTotals!!.beginBalance

         if (rs.getUuidOrNull("detail_id") != null) {
            val details = mapDetails(rs).takeIf { it.date >= filterRequest.from && it.date <= filterRequest.thru }
            if (details != null) tempReportAccount.glDetails.add(details)
         }
      }

      val beginBalancesSumMap:Map<Int, BigDecimal> = beginBalancesMap.toList().groupBy { it.first.first }.mapValues { it.value.sumOf { it.second } }

      reportAccounts.forEach {
         it.glTotals!!.beginBalance =
            (beginBalancesSumMap[it.accountNumber] ?: BigDecimal.ZERO) + it.glTotals!!.beginBalance2!!
         it.glTotals!!.endBalance =
            it.glTotals!!.beginBalance + it.glTotals!!.netChange
         it.glTotals!!.beginBalance2 = null
      }

      return reportAccounts
   }

   private fun mapRow(rs: ResultSet): GeneralLedgerTrialBalanceReportAccountDTO {
      return GeneralLedgerTrialBalanceReportAccountDTO(
         accountID = rs.getUuid("account_id"),
         accountNumber = rs.getInt("account_number"),
         accountName = rs.getString("account_name"),
         accountType = rs.getString("account_type"),
      )
   }

   private fun mapGLTotals(rs: ResultSet): GeneralLedgerNetChangeDTO {
      return GeneralLedgerNetChangeDTO(
         debit = rs.getBigDecimal("debit"),
         credit = rs.getBigDecimal("credit"),
         beginBalance = rs.getBigDecimal("begin_balance1"),
         endBalance = rs.getBigDecimal("end_balance"),
         netChange = rs.getBigDecimal("net_change"),
         ytdDebit = rs.getBigDecimal("ytd_debit"),
         ytdCredit = rs.getBigDecimal("ytd_credit"),
         beginBalance2 = rs.getBigDecimal("begin_balance2"),
      )
   }

   private fun mapDetails(
      rs: ResultSet
   ): GeneralLedgerTrialBalanceReportDetailDTO {
      return GeneralLedgerTrialBalanceReportDetailDTO(
         id = rs.getUuid("detail_id"),
         date = rs.getLocalDate("detail_date"),
         source = GeneralLedgerSourceCodeDTO(
            id = rs.getUuid("detail_source_id"),
            value = rs.getString("detail_source_value"),
            description = rs.getString("detail_source_description"),
         ),
         journalEntryNumber = rs.getIntOrNull("detail_je"),
         message = rs.getString("detail_message"),
         amount = rs.getBigDecimal("detail_amount"),
      )
   }
}
