package com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure

import com.cynergisuite.domain.GeneralLedgerProfitCenterTrialBalanceReportFilterRequest
import com.cynergisuite.domain.GeneralLedgerSearchReportFilterRequest
import com.cynergisuite.domain.GeneralLedgerSourceReportFilterRequest
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.TrialBalanceWorksheetFilterRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.sumByBigDecimal
import com.cynergisuite.extensions.update
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerDetailPurgeCountDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceReportSourceDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailEntity
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailFilterRequest
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailPageRequest
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailPostPurgeDTO
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.accounting.general.ledger.inquiry.GeneralLedgerNetChangeDTO
import com.cynergisuite.middleware.accounting.general.ledger.summary.GeneralLedgerSummaryEntity
import com.cynergisuite.middleware.accounting.general.ledger.trial.balance.GeneralLedgerProfitCenterTrialBalanceReportDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.trial.balance.TrialBalanceEndOfReportDTO
import com.cynergisuite.middleware.accounting.general.ledger.trial.balance.TrialBalanceWorksheetDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.trial.balance.TrialBalanceWorksheetReportTemplate
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
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
import java.time.LocalDate
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class GeneralLedgerDetailRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val accountRepository: AccountRepository,
   private val storeRepository: StoreRepository,
   private val sourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val financialCalendarRepository: FinancialCalendarRepository,
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerDetailRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         WITH account AS (
            ${accountRepository.selectBaseQuery()}
         )
         SELECT DISTINCT
            glDetail.id                                               AS glDetail_id,
            glDetail.company_id                                       AS glDetail_company_id,
            glDetail.profit_center_id_sfk                             AS glDetail_profit_center_id_sfk,
            glDetail.date                                             AS glDetail_date,
            glDetail.amount                                           AS glDetail_amount,
            glDetail.message                                          AS glDetail_message,
            glDetail.employee_number_id_sfk                           AS glDetail_employee_number_id_sfk,
            glDetail.journal_entry_number                             AS glDetail_journal_entry_number,
            acct.account_id                                           AS glDetail_account_id,
            acct.account_number                                       AS glDetail_account_number,
            acct.account_name                                         AS glDetail_account_name,
            acct.account_form_1099_field                              AS glDetail_account_form_1099_field,
            acct.account_corporate_account_indicator                  AS glDetail_account_corporate_account_indicator,
            acct.account_comp_id                                      AS glDetail_account_comp_id,
            acct.account_deleted                                      AS glDetail_account_deleted,
            acct.account_type_id                                      AS glDetail_account_type_id,
            acct.account_type_value                                   AS glDetail_account_type_value,
            acct.account_type_description                             AS glDetail_account_type_description,
            acct.account_type_localization_code                       AS glDetail_account_type_localization_code,
            acct.account_balance_type_id                              AS glDetail_account_balance_type_id,
            acct.account_balance_type_value                           AS glDetail_account_balance_type_value,
            acct.account_balance_type_description                     AS glDetail_account_balance_type_description,
            acct.account_balance_type_localization_code               AS glDetail_account_balance_type_localization_code,
            acct.account_status_id                                    AS glDetail_account_status_id,
            acct.account_status_value                                 AS glDetail_account_status_value,
            acct.account_status_description                           AS glDetail_account_status_description,
            acct.account_status_localization_code                     AS glDetail_account_status_localization_code,
            acct.account_vendor_1099_type_id                          AS glDetail_account_vendor_1099_type_id,
            acct.account_vendor_1099_type_value                       AS glDetail_account_vendor_1099_type_value,
            acct.account_vendor_1099_type_description                 AS glDetail_account_vendor_1099_type_description,
            acct.account_vendor_1099_type_localization_code           AS glDetail_account_vendor_1099_type_localization_code,
            bank.id                                                   AS glDetail_account_bank_id,
            profitCenter.id                                           AS glDetail_profitCenter_id,
            profitCenter.number                                       AS glDetail_profitCenter_number,
            profitCenter.name                                         AS glDetail_profitCenter_name,
            profitCenter.dataset                                      AS glDetail_profitCenter_dataset,
            source.id                                                 AS glDetail_source_id,
            source.company_id                                         AS glDetail_source_company_id,
            source.value                                              AS glDetail_source_value,
            source.description                                        AS glDetail_source_description,
            source.deleted                                            AS glDetail_source_deleted,
            count(*) OVER() AS total_elements
         FROM general_ledger_detail glDetail
            JOIN company comp ON glDetail.company_id = comp.id AND comp.deleted = FALSE
            JOIN fastinfo_prod_import.store_vw profitCenter
                    ON profitCenter.dataset = comp.dataset_code
                       AND profitCenter.number = glDetail.profit_center_id_sfk
            JOIN account acct ON glDetail.account_id = acct.account_id AND acct.account_deleted = FALSE
            JOIN general_ledger_source_codes source ON glDetail.source_id = source.id AND source.deleted = FALSE
            LEFT OUTER JOIN bank ON bank.general_ledger_account_id = acct.account_id AND bank.deleted = FALSE
            JOIN general_ledger_summary glSummary ON glSummary.company_id = glDetail.company_id
                 AND glSummary.account_id = glDetail.account_id
                 AND glSummary.profit_center_id_sfk = glDetail.profit_center_id_sfk
      """
   }

   fun selectNetChangeQuery(where: String): String {
      return """
         SELECT
             glSummary.company_id                                                            AS company_id,
             acct.number                                                                     AS account_number,
             glSummary.profit_center_id_sfk                                                  AS profit_center_number,
             SUM(CASE WHEN glDetail.date BETWEEN :from AND :thru
                  AND glDetail.amount >= 0 THEN glDetail.amount ELSE 0 END)                  AS debit,
             SUM(CASE WHEN glDetail.date BETWEEN :from AND :thru
                  AND glDetail.amount < 0 THEN glDetail.amount ELSE 0 END)                   AS credit,
             SUM(CASE WHEN glDetail.date BETWEEN :from AND :thru
                  THEN glDetail.amount ELSE 0 END)                                           AS net_change,
             COALESCE(glSummary.net_activity_period_1, 0)                                    AS net_activity_period_1,
             COALESCE(glSummary.net_activity_period_2, 0)                                    AS net_activity_period_2,
             COALESCE(glSummary.net_activity_period_3, 0)                                    AS net_activity_period_3,
             COALESCE(glSummary.net_activity_period_4, 0)                                    AS net_activity_period_4,
             COALESCE(glSummary.net_activity_period_5, 0)                                    AS net_activity_period_5,
             COALESCE(glSummary.net_activity_period_6, 0)                                    AS net_activity_period_6,
             COALESCE(glSummary.net_activity_period_7, 0)                                    AS net_activity_period_7,
             COALESCE(glSummary.net_activity_period_8, 0)                                    AS net_activity_period_8,
             COALESCE(glSummary.net_activity_period_9, 0)                                    AS net_activity_period_9,
             COALESCE(glSummary.net_activity_period_10, 0)                                   AS net_activity_period_10,
             COALESCE(glSummary.net_activity_period_11, 0)                                   AS net_activity_period_11,
             COALESCE(glSummary.net_activity_period_12, 0)                                   AS net_activity_period_12,
             COALESCE(glSummary.beginning_balance, 0)
                  + SUM(CASE WHEN glDetail.date < :from THEN glDetail.amount ELSE 0 END)     AS begin_balance,
             COALESCE(glSummary.beginning_balance, 0) + SUM(COALESCE(glDetail.amount, 0))    AS end_balance
         FROM general_ledger_summary glSummary
                JOIN account acct ON glSummary.account_id = acct.id AND acct.deleted = FALSE
                JOIN company comp ON glSummary.company_id = comp.id AND comp.deleted = FALSE
                LEFT JOIN bank ON bank.general_ledger_account_id = acct.id AND bank.deleted = FALSE
                LEFT JOIN (
                            SELECT glDetail.*
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

   fun selectReportQuery(): String {
      return """
         WITH account AS (
            ${accountRepository.selectBaseQuery()}
         )
         SELECT
            glDetail.id                                               AS glDetail_id,
            glDetail.company_id                                       AS glDetail_company_id,
            glDetail.profit_center_id_sfk                             AS glDetail_profit_center_id_sfk,
            glDetail.date                                             AS glDetail_date,
            glDetail.amount                                           AS glDetail_amount,
            glDetail.message                                          AS glDetail_message,
            glDetail.employee_number_id_sfk                           AS glDetail_employee_number_id_sfk,
            glDetail.journal_entry_number                             AS glDetail_journal_entry_number,
            acct.account_id                                           AS glDetail_account_id,
            acct.account_number                                       AS glDetail_account_number,
            acct.account_name                                         AS glDetail_account_name,
            acct.account_form_1099_field                              AS glDetail_account_form_1099_field,
            acct.account_corporate_account_indicator                  AS glDetail_account_corporate_account_indicator,
            acct.account_comp_id                                      AS glDetail_account_comp_id,
            acct.account_deleted                                      AS glDetail_account_deleted,
            acct.account_type_id                                      AS glDetail_account_type_id,
            acct.account_type_value                                   AS glDetail_account_type_value,
            acct.account_type_description                             AS glDetail_account_type_description,
            acct.account_type_localization_code                       AS glDetail_account_type_localization_code,
            acct.account_balance_type_id                              AS glDetail_account_balance_type_id,
            acct.account_balance_type_value                           AS glDetail_account_balance_type_value,
            acct.account_balance_type_description                     AS glDetail_account_balance_type_description,
            acct.account_balance_type_localization_code               AS glDetail_account_balance_type_localization_code,
            acct.account_status_id                                    AS glDetail_account_status_id,
            acct.account_status_value                                 AS glDetail_account_status_value,
            acct.account_status_description                           AS glDetail_account_status_description,
            acct.account_status_localization_code                     AS glDetail_account_status_localization_code,
            acct.account_vendor_1099_type_id                          AS glDetail_account_vendor_1099_type_id,
            acct.account_vendor_1099_type_value                       AS glDetail_account_vendor_1099_type_value,
            acct.account_vendor_1099_type_description                 AS glDetail_account_vendor_1099_type_description,
            acct.account_vendor_1099_type_localization_code           AS glDetail_account_vendor_1099_type_localization_code,
            bank.id                                                   AS glDetail_account_bank_id,
            profitCenter.id                                           AS glDetail_profitCenter_id,
            profitCenter.number                                       AS glDetail_profitCenter_number,
            profitCenter.name                                         AS glDetail_profitCenter_name,
            profitCenter.dataset                                      AS glDetail_profitCenter_dataset,
            source.id                                                 AS glDetail_source_id,
            source.company_id                                         AS glDetail_source_company_id,
            source.value                                              AS glDetail_source_value,
            source.description                                        AS glDetail_source_description,
            source.deleted                                            AS glDetail_source_deleted,
            count(*) OVER() AS total_elements
         FROM general_ledger_detail glDetail
            JOIN company comp ON glDetail.company_id = comp.id AND comp.deleted = FALSE
            JOIN fastinfo_prod_import.store_vw profitCenter
                    ON profitCenter.dataset = comp.dataset_code
                       AND profitCenter.number = glDetail.profit_center_id_sfk
            JOIN account acct ON glDetail.account_id = acct.account_id AND acct.account_deleted = FALSE
            JOIN general_ledger_source_codes source ON glDetail.source_id = source.id AND source.deleted = FALSE
            LEFT OUTER JOIN bank ON bank.general_ledger_account_id = acct.account_id AND bank.deleted = FALSE
      """
   }

   @ReadOnly
   fun exists(company: CompanyEntity): Boolean {
      val exists = jdbc.queryForObject(
         "SELECT EXISTS (SELECT company_id FROM general_ledger_detail WHERE company_id = :company_id AND deleted = FALSE)",
         mapOf("company_id" to company.id),
         Boolean::class.java
      )

      logger.trace("Checking if GeneralLedgerDetail: {} exists resulted in {}", company, exists)

      return exists
   }

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): GeneralLedgerDetailEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query = "${selectBaseQuery()} WHERE glDetail.id = :id AND glDetail.company_id = :comp_id AND glDetail.deleted = FALSE"
      val found = jdbc.findFirstOrNull(
         query,
         params
      ) { rs, _ ->
         val account = accountRepository.mapRow(rs, company, "glDetail_account_")
         val profitCenter = storeRepository.mapRow(rs, company, "glDetail_profitCenter_")
         val sourceCode = sourceCodeRepository.mapRow(rs, "glDetail_source_")

         mapRow(
            rs,
            account,
            profitCenter,
            sourceCode,
            "glDetail_"
         )
      }

      logger.trace("Searching for GeneralLedgerDetail id: {} resulted in {}\nQuery {}", id, found, query)

      return found
   }

   @ReadOnly
   fun findNetChange(company: CompanyEntity, filterRequest: GeneralLedgerDetailFilterRequest): GeneralLedgerNetChangeDTO? {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val innerWhere = StringBuilder(" WHERE glSummary.company_id = :comp_id ")
      val subQueryWhere = StringBuilder(" WHERE glDetail.company_id = :comp_id AND glDetail.deleted = FALSE ")
      val outerWhere = StringBuilder()
      if (filterRequest.from != null && filterRequest.thru != null) {
         params["from"] = filterRequest.from
         params["thru"] = filterRequest.thru
         subQueryWhere.append(""" AND glDetail.date BETWEEN
               (SELECT DISTINCT period_from
                  FROM financial_calendar
                  WHERE fiscal_year = :fiscalYear AND company_id = :comp_id AND period = 1)
               AND :thru """)
      }
      if (filterRequest.profitCenter != null) {
         params["profitCenter"] = filterRequest.profitCenter
         subQueryWhere.append(" AND glDetail.profit_center_id_sfk = :profitCenter ")
         outerWhere.append(" WHERE profit_center_number = :profitCenter ")
      }
      if (filterRequest.account != null) {
         params["account"] = filterRequest.account
         innerWhere.append(" AND acct.number = :account ")
         subQueryWhere.append("""
               AND glDetail.account_id = (
                  SELECT DISTINCT id
                  FROM account
                  WHERE company_id = :comp_id AND number = :account AND deleted = false) """.trimMargin())
      }
      if (filterRequest.fiscalYear != null) {
         params["fiscalYear"] = filterRequest.fiscalYear
         innerWhere.append(""" AND glSummary.overall_period_id =
            (SELECT DISTINCT overall_period_id
            FROM financial_calendar
            WHERE fiscal_year = :fiscalYear AND company_id = :comp_id) """.trimIndent())
      }
      val innerQuery = """
         ${selectNetChangeQuery(subQueryWhere.toString())}
         $innerWhere
         GROUP BY glSummary.company_id, glSummary.account_id, acct.number, glSummary.id, glSummary.profit_center_id_sfk
      """.trimIndent()
      val mainQuery = """
         SELECT
            company_id                          AS company_id,
            account_number                      AS account_number,
            sum(debit)                          AS debit,
            sum(credit)                         AS credit,
            sum(net_change)                     AS net_change,
            sum(net_activity_period_1)          AS net_activity_period_1,
            sum(net_activity_period_2)          AS net_activity_period_2,
            sum(net_activity_period_3)          AS net_activity_period_3,
            sum(net_activity_period_4)          AS net_activity_period_4,
            sum(net_activity_period_5)          AS net_activity_period_5,
            sum(net_activity_period_6)          AS net_activity_period_6,
            sum(net_activity_period_7)          AS net_activity_period_7,
            sum(net_activity_period_8)          AS net_activity_period_8,
            sum(net_activity_period_9)          AS net_activity_period_9,
            sum(net_activity_period_10)         AS net_activity_period_10,
            sum(net_activity_period_11)         AS net_activity_period_11,
            sum(net_activity_period_12)         AS net_activity_period_12,
            sum(begin_balance)                  AS begin_balance,
            sum(end_balance)                    AS end_balance
         FROM  ($innerQuery) tmp
         $outerWhere
         GROUP BY company_id, account_number
      """.trimIndent()

      logger.info("Querying for General Ledger Inquiry Net Change using {} {}", mainQuery, params)

      val netChangeDTO = jdbc.findFirstOrNull(mainQuery, params) { rs, _  -> mapNetChange(rs) }

      logger.info("Querying for General Ledger Inquiry Net Change resulted in {}", netChangeDTO)

      return netChangeDTO
   }

   @ReadOnly
   fun findNetChangeProfitCenterTrialBalanceReport(
      company: CompanyEntity,
      fromDate: LocalDate? = null,
      thruDate: LocalDate? = null,
      profitCenterNumber: Int? = null,
      accountNumber: Long? = null,
      overallPeriodId: Int? = null
   ): GeneralLedgerNetChangeDTO? {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val innerWhere = StringBuilder(" WHERE glSummary.company_id = :comp_id ")
      val subQueryWhere = StringBuilder(" WHERE glDetail.company_id = :comp_id AND glDetail.deleted = FALSE ")
      val outerWhere = StringBuilder()
      if (fromDate != null || thruDate != null) {
         params["from"] = fromDate
         params["thru"] = thruDate
         params["overallPeriodId"] = overallPeriodId
         subQueryWhere.append(""" AND glDetail.date BETWEEN
               (SELECT DISTINCT period_from
                  FROM financial_calendar
                  WHERE overall_period_id = :overallPeriodId AND company_id = :comp_id AND period = 1)
               AND :thru """)
      }
      if (profitCenterNumber != null) {
         params["profitCenter"] = profitCenterNumber
         subQueryWhere.append(" AND glDetail.profit_center_id_sfk = :profitCenter ")
         outerWhere.append(" WHERE profit_center_number = :profitCenter ")
      }
      if (accountNumber != null) {
         params["account"] = accountNumber
         innerWhere.append(" AND acct.number = :account ")
         subQueryWhere.append("""
               AND glDetail.account_id = (
                  SELECT DISTINCT id
                  FROM account
                  WHERE company_id = :comp_id AND number = :account AND deleted = false) """.trimMargin())
      }
      if (overallPeriodId != null) {
         params["overallPeriodId"] = overallPeriodId
         innerWhere.append(""" AND glSummary.overall_period_id = :overallPeriodId """.trimIndent())
      }
      val innerQuery = """
         ${selectNetChangeQuery(subQueryWhere.toString())}
         $innerWhere
         GROUP BY glSummary.company_id, glSummary.account_id, acct.number, glSummary.id, glSummary.profit_center_id_sfk
      """.trimIndent()
      val mainQuery = """
         SELECT
            company_id                          AS company_id,
            account_number                      AS account_number,
            sum(debit)                          AS debit,
            sum(credit)                         AS credit,
            sum(net_change)                     AS net_change,
            sum(net_activity_period_1)          AS net_activity_period_1,
            sum(net_activity_period_2)          AS net_activity_period_2,
            sum(net_activity_period_3)          AS net_activity_period_3,
            sum(net_activity_period_4)          AS net_activity_period_4,
            sum(net_activity_period_5)          AS net_activity_period_5,
            sum(net_activity_period_6)          AS net_activity_period_6,
            sum(net_activity_period_7)          AS net_activity_period_7,
            sum(net_activity_period_8)          AS net_activity_period_8,
            sum(net_activity_period_9)          AS net_activity_period_9,
            sum(net_activity_period_10)         AS net_activity_period_10,
            sum(net_activity_period_11)         AS net_activity_period_11,
            sum(net_activity_period_12)         AS net_activity_period_12,
            sum(begin_balance)                  AS begin_balance,
            sum(end_balance)                    AS end_balance
         FROM  ($innerQuery) tmp
         $outerWhere
         GROUP BY company_id, account_number
      """.trimIndent()

      logger.info("Querying for General Ledger Profit Center Trial Balance Report Net Change using {} {}", mainQuery, params)

      val netChangeDTO = jdbc.findFirstOrNull(mainQuery, params) { rs, _  -> mapNetChange(rs) }

      logger.info("Querying for General Ledger Profit Center Trial Balance Report Net Change resulted in {}", netChangeDTO)

      return netChangeDTO
   }

   @ReadOnly
   fun findAll(company: CompanyEntity, page: GeneralLedgerDetailPageRequest): RepositoryPage<GeneralLedgerDetailEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "limit" to page.size(), "offset" to page.offset())
      val whereClause = StringBuilder(" WHERE glDetail.company_id = :comp_id AND glDetail.deleted = FALSE ")
      if (page.from != null || page.thru != null) {
         params["from"] = page.from
         params["thru"] = page.thru
         whereClause.append(" AND glDetail.date ")
            .append(buildFilterString(page.from != null, page.thru != null, "from", "thru"))
      }
      if (page.profitCenter != null) {
         params["profitCenter"] = page.profitCenter
         whereClause.append(" AND profitCenter.number = :profitCenter")
      }
      if (page.account != null) {
         params["account"] = page.account
         whereClause.append(" AND acct.account_number = :account")
      }
      if (page.fiscalYear != null) {
         params["fiscalYear"] = page.fiscalYear
         whereClause.append(" AND glSummary.overall_period_id = (SELECT DISTINCT overall_period_id FROM financial_calendar WHERE fiscal_year = :fiscalYear AND company_id = :comp_id) ")
      }

      val query = """
            ${selectBaseQuery()}
            $whereClause
            ORDER BY glDetail_profit_center_id_sfk, glDetail_date, glDetail_source_value
            LIMIT :limit OFFSET :offset
         """.trimIndent()

      logger.info("Querying for General Ledger Inquiry Detail using {} {}", query, params)

      return jdbc.queryPaged(
         query,
         params,
         page
      ) { rs, elements ->
         do {
            val account = accountRepository.mapRow(rs, company, "glDetail_account_")
            val profitCenter = storeRepository.mapRow(rs, company, "glDetail_profitCenter_")
            val sourceCode = sourceCodeRepository.mapRow(rs, "glDetail_source_")
            elements.add(
               mapRow(
                  rs,
                  account,
                  profitCenter,
                  sourceCode,
                  "glDetail_"
               )
            )
         } while (rs.next())
      }
   }

   @ReadOnly
   fun findNextJENumber(company: CompanyEntity): Int {
      val lastJENumber = jdbc.queryForObject(
         """
            SELECT MAX(journal_entry_number)
            FROM general_ledger_detail
            WHERE company_id = :comp_id AND deleted = FALSE
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id
         ),
         Int::class.java
      )

      return if (lastJENumber > 0) {
         lastJENumber + 1
      } else {
         1
      }
   }

   @Transactional
   fun insert(entity: GeneralLedgerDetailEntity, company: CompanyEntity): GeneralLedgerDetailEntity {
      logger.debug("Inserting general_ledger_detail {}", company)

      return jdbc.insertReturning(
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
         VALUES (
            :company_id,
            :account_id,
            :profit_center_id_sfk,
            :date,
            :source_id,
            :amount,
            :message,
            :employee_number_id_sfk,
            :journal_entry_number
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.id,
            "account_id" to entity.account.myId(),
            "profit_center_id_sfk" to entity.profitCenter.myNumber(),
            "date" to entity.date,
            "source_id" to entity.source.myId(),
            "amount" to entity.amount,
            "message" to entity.message,
            "employee_number_id_sfk" to entity.employeeNumberId,
            "journal_entry_number" to entity.journalEntryNumber
         )
      ) { rs, _ ->
         mapRow(
            rs,
            entity.account,
            entity.profitCenter,
            entity.source
         )
      }
   }

   @Transactional
   fun update(entity: GeneralLedgerDetailEntity, company: CompanyEntity): GeneralLedgerDetailEntity {
      logger.debug("Updating general_ledger_detail {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE general_ledger_detail
         SET
            company_id = :company_id,
            account_id = :account_id,
            profit_center_id_sfk = :profit_center_id_sfk,
            date = :date,
            source_id = :source_id,
            amount = :amount,
            message = :message,
            employee_number_id_sfk = :employee_number_id_sfk,
            journal_entry_number = :journal_entry_number
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to company.id,
            "account_id" to entity.account.myId(),
            "profit_center_id_sfk" to entity.profitCenter.myNumber(),
            "date" to entity.date,
            "source_id" to entity.source.myId(),
            "amount" to entity.amount,
            "message" to entity.message,
            "employee_number_id_sfk" to entity.employeeNumberId,
            "journal_entry_number" to entity.journalEntryNumber
         )
      ) { rs, _ ->
         mapRow(
            rs,
            entity.account,
            entity.profitCenter,
            entity.source
         )
      }
   }

   @ReadOnly
   fun fetchReports(company: CompanyEntity, filterRequest: GeneralLedgerSearchReportFilterRequest): List<GeneralLedgerDetailEntity> {
      val reports = mutableListOf<GeneralLedgerDetailEntity>()
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val whereClause = StringBuilder("WHERE glDetail.company_id = :comp_id AND glDetail.deleted = FALSE ")

      if (filterRequest.startingAccount != null && filterRequest.endingAccount != null) {
         params["startingAccount"] = filterRequest.startingAccount
         params["endingAccount"] = filterRequest.endingAccount
         whereClause.append(" AND acct.account_number ")
            .append(buildFilterString( filterRequest.startingAccount != null, filterRequest.endingAccount != null, "startingAccount", "endingAccount"))
      }

      if (filterRequest.profitCenter != null) {
         params["profitCenter"] = filterRequest.profitCenter
         whereClause.append(" AND profitCenter.number = :profitCenter")
      }

      if (filterRequest.sourceCode != null) {
         params["sourceCode"] = filterRequest.sourceCode
         whereClause.append(" AND source.value = :sourceCode")
      }

      when (filterRequest.typeEntry) {
         "C" -> whereClause.append(" AND glDetail.amount < 0 ")
         "D" -> whereClause.append(" AND glDetail.amount >= 0 ")
      }
      if(filterRequest.lowAmount != null || filterRequest.highAmount != null) {
         params["lowAmount"] = filterRequest.lowAmount
         params["highAmount"] = filterRequest.highAmount
         whereClause.append(" AND glDetail.amount ")
            .append(buildFilterString(filterRequest.lowAmount != null, filterRequest.highAmount != null, "lowAmount", "highAmount"))
      }

      if (filterRequest.description != null ) {
         params["description"] = "%" + filterRequest.description + "%"
         whereClause.append(" AND glDetail.message ILIKE :description")

      }

      if (filterRequest.jeNumber != null) {
         params["jeNumber"] = filterRequest.jeNumber
         whereClause.append(" AND glDetail.journal_entry_number = :jeNumber")
      }

      if (filterRequest.frmPmtDt != null || filterRequest.thruPmtDt != null) {
         params["frmPmtDt"] = filterRequest.frmPmtDt
         params["thruPmtDt"] = filterRequest.thruPmtDt
         whereClause.append(" AND glDetail.date ")
            .append(buildFilterString(filterRequest.frmPmtDt != null, filterRequest.thruPmtDt != null, "frmPmtDt", "thruPmtDt"))
      }

      jdbc.query(
         """
            ${selectReportQuery()}
            $whereClause
            ORDER BY acct.account_number, profitCenter.number, glDetail.date, glDetail.journal_entry_number
         """.trimIndent(),
         params
      ) { rs, _ ->
         do {
            val account = accountRepository.mapRow(rs, company, "glDetail_account_")
            val profitCenter = storeRepository.mapRow(rs, company, "glDetail_profitCenter_")
            val sourceCode = sourceCodeRepository.mapRow(rs, "glDetail_source_")
            val currentEntity = mapRow(rs, account, profitCenter, sourceCode, "glDetail_")
            reports.add(currentEntity)
         } while (rs.next())
      }

      return reports
   }

   @ReadOnly
   fun fetchSourceReportSourceDetails(company: CompanyEntity, filterRequest: GeneralLedgerSourceReportFilterRequest): List<GeneralLedgerSourceReportSourceDetailDTO> {
      // fetch source codes
      val sourceCodes = mutableListOf<GeneralLedgerSourceCodeEntity>()
      val whereClause = StringBuilder("WHERE glSrcCodes.company_id = :comp_id AND glSrcCodes.deleted = FALSE")
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)

      if (filterRequest.startSource != null && filterRequest.endSource != null) {
         whereClause.append(" AND glSrcCodes.value BETWEEN '${filterRequest.startSource}' AND '${filterRequest.endSource}'")
      } else if (filterRequest.startSource != null) {
         whereClause.append(" AND glSrcCodes.value = '${filterRequest.startSource}'")
      } else if (filterRequest.endSource != null) {
         whereClause.append(" AND glSrcCodes.value = '${filterRequest.endSource}'")
      }

      jdbc.query(
         """
            ${sourceCodeRepository.selectBaseQuery()}
            $whereClause
            ORDER BY glSrcCodes.value ASC
         """.trimIndent(),
         params
      ) { rs, _ ->
         do {
            sourceCodes.add(sourceCodeRepository.mapRow(rs, "glSrcCodes_"))
         } while (rs.next())
      }

      // fetch GL details for each source code
      val sourceDetailDTOs = mutableListOf<GeneralLedgerSourceReportSourceDetailDTO>()
      sourceCodes.forEach {
         val glDetailList = fetchSourceReportDetails(company, filterRequest, it)
         if (glDetailList.isNotEmpty()) {
            sourceDetailDTOs.add(GeneralLedgerSourceReportSourceDetailDTO(glDetailList))
         }
      }

      // calculate description totals
      if (filterRequest.sortBy == "message") {
         sourceDetailDTOs.forEach { sourceDetail ->
            var runningDescTotalDebit = BigDecimal.ZERO
            var runningDescTotalCredit = BigDecimal.ZERO
            var desc = sourceDetail.details?.get(0)?.message

            sourceDetail.details?.forEach {
               if (desc == it.message) {
                  if (it.debitAmount != null) {
                     runningDescTotalDebit += it.debitAmount!!
                     it.runningDescTotalDebit = runningDescTotalDebit
                     it.runningDescTotalCredit = runningDescTotalCredit
                  } else {
                     runningDescTotalCredit += it.creditAmount!!.abs()
                     it.runningDescTotalDebit = runningDescTotalDebit
                     it.runningDescTotalCredit = runningDescTotalCredit
                  }
               } else {
                  desc = it.message
                  runningDescTotalDebit = BigDecimal.ZERO
                  runningDescTotalCredit = BigDecimal.ZERO

                  if (it.debitAmount != null) {
                     runningDescTotalDebit += it.debitAmount!!
                     it.runningDescTotalDebit = runningDescTotalDebit
                     it.runningDescTotalCredit = runningDescTotalCredit
                  } else {
                     runningDescTotalCredit += it.creditAmount!!.abs()
                     it.runningDescTotalDebit = runningDescTotalDebit
                     it.runningDescTotalCredit = runningDescTotalCredit
                  }
               }
            }
         }
      }

      return sourceDetailDTOs
   }

   @ReadOnly
   fun fetchSourceReportDetails(company: CompanyEntity, filterRequest: GeneralLedgerSourceReportFilterRequest, sourceCodeEntity: GeneralLedgerSourceCodeEntity): List<GeneralLedgerDetailEntity> {
      val glDetails = mutableListOf<GeneralLedgerDetailEntity>()
      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "source_code_value" to sourceCodeEntity.value)
      val whereClause = StringBuilder("WHERE glDetail.company_id = :comp_id AND source.value = :source_code_value AND glDetail.deleted = FALSE")

      if (filterRequest.profitCenter != null) {
         params["profitCenter"] = filterRequest.profitCenter
         whereClause.append(" AND profitCenter.number = :profitCenter")
      }

      if (filterRequest.startDate != null || filterRequest.endDate != null) {
         params["startDate"] = filterRequest.startDate
         params["endDate"] = filterRequest.endDate
         whereClause.append(" AND glDetail.date ")
            .append(buildFilterString(filterRequest.startDate != null, filterRequest.endDate != null, "startDate", "endDate"))
      }

      if (filterRequest.jeNumber != null) {
         params["jeNumber"] = filterRequest.jeNumber
         whereClause.append(" AND glDetail.journal_entry_number = :jeNumber")
      }

      jdbc.query(
         """
            ${selectReportQuery()}
            $whereClause
            ORDER BY glDetail_${filterRequest.snakeSortBy()}, profitCenter.number, glDetail.date ${filterRequest.sortDirection()}
         """.trimIndent(),
         params
      ) { rs, _ ->
         do {
            val account = accountRepository.mapRow(rs, company, "glDetail_account_")
            val profitCenter = storeRepository.mapRow(rs, company, "glDetail_profitCenter_")
            val sourceCode = sourceCodeRepository.mapRow(rs, "glDetail_source_")
            val currentEntity = mapRow(rs, account, profitCenter, sourceCode, "glDetail_")
            glDetails.add(currentEntity)
         } while (rs.next())
      }

      return glDetails
   }

   @ReadOnly
   fun fetchProfitCenterTrialBalanceReportDetails(company: CompanyEntity, filterRequest: GeneralLedgerProfitCenterTrialBalanceReportFilterRequest, glSummary: GeneralLedgerSummaryEntity): List<GeneralLedgerProfitCenterTrialBalanceReportDetailDTO> {
      val glDetails = mutableListOf<GeneralLedgerDetailEntity>()
      val reportDetails = mutableListOf<GeneralLedgerProfitCenterTrialBalanceReportDetailDTO>()

      jdbc.query(
         """
         ${selectBaseQuery()}
         WHERE glDetail.company_id = :company_id
            AND glDetail.date BETWEEN :starting_date AND :ending_date
            AND glDetail.account_id = :account_id
            AND glDetail.profit_center_id_sfk = :profit_center_id
            AND source.value NOT LIKE 'BAL'
            AND glDetail.deleted = FALSE
         ORDER BY glDetail.date
      """.trimIndent(),
         mapOf(
            "company_id" to company.id,
            "starting_date" to filterRequest.fromDate,
            "ending_date" to filterRequest.thruDate,
            "account_id" to glSummary.account.id,
            "profit_center_id" to glSummary.profitCenter.myNumber()
         )
      ) { rs, _ ->
         do {
            val account = accountRepository.mapRow(rs, company, "glDetail_account_")
            val profitCenter = storeRepository.mapRow(rs, company, "glDetail_profitCenter_")
            val sourceCode = sourceCodeRepository.mapRow(rs, "glDetail_source_")
            glDetails.add(mapRow(rs, account, profitCenter, sourceCode, "glDetail_"))
         } while (rs.next())
      }

      glDetails.forEach {
         reportDetails.add(GeneralLedgerProfitCenterTrialBalanceReportDetailDTO(it))
      }

      return reportDetails
   }

   @ReadOnly
   fun fetchTrialBalanceEndOfReportTotals(company: CompanyEntity, filterRequest: GeneralLedgerProfitCenterTrialBalanceReportFilterRequest, overallPeriodId: Int): TrialBalanceEndOfReportDTO {
      var endOfReportDTO = TrialBalanceEndOfReportDTO()
      val emptyDTO = TrialBalanceEndOfReportDTO()
      val ytdBegin = financialCalendarRepository.findFirstDateOfFiscalYear(company, overallPeriodId)  // find YTD begin date (first date of fiscal year)
      val params = mutableMapOf<String, Any?>(
         "company_id" to company.id,
         "mtdBegin" to filterRequest.fromDate,
         "mtdEnd" to filterRequest.thruDate,
         "ytdBegin" to ytdBegin,
         "ytdEnd" to filterRequest.thruDate
      )
      val whereClause = StringBuilder(
         """
            WHERE glDetail.company_id = :company_id
               AND glDetail.date BETWEEN :ytdBegin AND :ytdEnd
               AND glDetail.deleted = FALSE
         """.trimIndent()
      )

      if (filterRequest.startingAccount != null || filterRequest.endingAccount != null) {
         params["startingAccount"] = filterRequest.startingAccount
         params["endingAccount"] = filterRequest.endingAccount
         whereClause.append(" AND acct.number ")
            .append(buildFilterString( filterRequest.startingAccount != null, filterRequest.endingAccount != null, "startingAccount", "endingAccount"))
      }

      // select locations based on criteria (1 selects all locations)
      when (filterRequest.selectLocsBy) {
         2 ->
         {
            params["any10LocsOrGroups"] = filterRequest.any10LocsOrGroups
            whereClause.append(" AND glDetail.profit_center_id_sfk IN (<any10LocsOrGroups>)")
         }
         3 ->
         {
            params["startingLocOrGroup"] = filterRequest.startingLocOrGroup
            params["endingLocOrGroup"] = filterRequest.endingLocOrGroup
            whereClause.append(" AND glDetail.profit_center_id_sfk BETWEEN :startingLocOrGroup AND :endingLocOrGroup")
         }
         // todo: 4 & 5 use location groups
      }

      jdbc.query(
         """
            SELECT
               SUM(CASE WHEN (acct.type_id = 3 OR acct.type_id = 5) AND glDetail.amount >= 0 AND (glDetail.date BETWEEN :mtdBegin AND :mtdEnd) THEN glDetail.amount ELSE 0 END) mtdDebitIE,
               SUM(CASE WHEN (acct.type_id = 3 OR acct.type_id = 5) AND glDetail.amount < 0 AND (glDetail.date BETWEEN :mtdBegin AND :mtdEnd) THEN glDetail.amount ELSE 0 END) mtdCreditIE,
               SUM(CASE WHEN (acct.type_id = 1 OR acct.type_id = 2 OR acct.type_id = 4) AND glDetail.amount >= 0 AND (glDetail.date BETWEEN :mtdBegin AND :mtdEnd) THEN glDetail.amount ELSE 0 END) mtdDebitAL,
               SUM(CASE WHEN (acct.type_id = 1 OR acct.type_id = 2 OR acct.type_id = 4) AND glDetail.amount < 0 AND (glDetail.date BETWEEN :mtdBegin AND :mtdEnd) THEN glDetail.amount ELSE 0 END) mtdCreditAL,
               SUM(CASE WHEN (acct.type_id = 3 OR acct.type_id = 5) AND glDetail.amount >= 0 THEN glDetail.amount ELSE 0 END) ytdDebitIE,
               SUM(CASE WHEN (acct.type_id = 3 OR acct.type_id = 5) AND glDetail.amount < 0 THEN glDetail.amount ELSE 0 END) ytdCreditIE,
               SUM(CASE WHEN (acct.type_id = 1 OR acct.type_id = 2 OR acct.type_id = 4) AND glDetail.amount >= 0 THEN glDetail.amount ELSE 0 END) ytdDebitAL,
               SUM(CASE WHEN (acct.type_id = 1 OR acct.type_id = 2 OR acct.type_id = 4) AND glDetail.amount < 0 THEN glDetail.amount ELSE 0 END) ytdCreditAL
            FROM general_ledger_detail glDetail
               JOIN account acct ON acct.id = glDetail.account_id
            $whereClause
         """.trimIndent(),
         params
      ) { rs, _ ->
         do {
            endOfReportDTO = mapTrialBalanceEndOfReport(rs)
         } while (rs.next())
      }

      if (endOfReportDTO != emptyDTO) {
         endOfReportDTO.mtdDifferenceIE = endOfReportDTO.mtdDebitIE!! + endOfReportDTO.mtdCreditIE!!
         endOfReportDTO.mtdDifferenceAL = endOfReportDTO.mtdDebitAL!! + endOfReportDTO.mtdCreditAL!!
         endOfReportDTO.ytdDifferenceIE = endOfReportDTO.ytdDebitIE!! + endOfReportDTO.ytdCreditIE!!
         endOfReportDTO.ytdDifferenceAL = endOfReportDTO.ytdDebitAL!! + endOfReportDTO.ytdCreditAL!!
      }

      return endOfReportDTO
   }

   @ReadOnly
   fun findPurgeCounts(company: CompanyEntity, filterRequest: GeneralLedgerDetailPostPurgeDTO) : GeneralLedgerDetailPurgeCountDTO {
      val glDetails = mutableListOf<GeneralLedgerDetailEntity>()
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val whereClause = StringBuilder("WHERE glDetail.company_id = :comp_id AND glDetail.deleted = FALSE")

      logger.trace("Repository - findPurgeCounts using: from {} thru {} source {}", filterRequest.fromDate, filterRequest.thruDate, filterRequest.sourceCode)

      params["source"] = filterRequest.sourceCode
      whereClause.append(" AND source.value = :source")

      params["fromDate"] = filterRequest.fromDate
      params["thruDate"] = filterRequest.thruDate
      whereClause.append(" AND glDetail.date")
         .append(
            buildFilterString(
               filterRequest.fromDate != null,
               filterRequest.thruDate != null,
               "fromDate",
               "thruDate"
            )
         )

      jdbc.query(
         """
         ${selectBaseQuery()}
         $whereClause
      """.trimIndent(),
         params
      ) { rs, _ ->
         do {
            val account = accountRepository.mapRow(rs, company, "glDetail_account_")
            val profitCenter = storeRepository.mapRow(rs, company, "glDetail_profitCenter_")
            val sourceCode = sourceCodeRepository.mapRow(rs, "glDetail_source_")
            glDetails.add(mapRow(rs, account, profitCenter, sourceCode, "glDetail_"))
         } while (rs.next())
      }

      val balance = glDetails.sumByBigDecimal { it.amount }

      return GeneralLedgerDetailPurgeCountDTO(glDetails, balance)

   }

   @ReadOnly
   fun findAllPurgePost(company: CompanyEntity, filterRequest: GeneralLedgerDetailPostPurgeDTO) : List<GeneralLedgerDetailEntity> {
      val glDetails = mutableListOf<GeneralLedgerDetailEntity>()
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val whereClause = StringBuilder("WHERE glDetail.company_id = :comp_id AND glDetail.deleted = FALSE")

      logger.trace("Repository - findAllPurgePost using: from {} thru {} source {}", filterRequest.fromDate, filterRequest.thruDate, filterRequest.sourceCode)

      params["source"] = filterRequest.sourceCode
      whereClause.append(" AND source.value = :source")

      params["fromDate"] = filterRequest.fromDate
      params["thruDate"] = filterRequest.thruDate
      whereClause.append(" AND glDetail.date")
         .append(
            buildFilterString(
               filterRequest.fromDate != null,
               filterRequest.thruDate != null,
               "fromDate",
               "thruDate"
            )
         )

      jdbc.query(
         """
         ${selectBaseQuery()}
         $whereClause
      """.trimIndent(),
         params
      ) { rs, _ ->
         do {
            val account = accountRepository.mapRow(rs, company, "glDetail_account_")
            val profitCenter = storeRepository.mapRow(rs, company, "glDetail_profitCenter_")
            val sourceCode = sourceCodeRepository.mapRow(rs, "glDetail_source_")
            glDetails.add(mapRow(rs, account, profitCenter, sourceCode, "glDetail_"))
         } while (rs.next())
      }

      return glDetails

   }

   @Transactional
   fun bulkDelete(dtoList: List<GeneralLedgerDetailEntity>, company: CompanyEntity): Int {
      val idList = dtoList.map { it.id }

      logger.trace("Deleting GeneralLedgerDetail with id={}", idList)

      val rowsAffected = jdbc.update(
         """
         UPDATE general_ledger_detail
         SET deleted = TRUE
         WHERE general_ledger_detail.id = any(array[<idList>]::uuid[]) AND company_id = :company_id AND deleted = FALSE
         """,
         mapOf("idList" to idList, "company_id" to company.id),
      )

      logger.info("Rows affected {}", rowsAffected)

      if (rowsAffected == 0) throw NotFoundException(idList) else return rowsAffected
   }

   @ReadOnly
   fun fetchTrialBalanceWorksheetDetails(company: CompanyEntity, filterRequest: TrialBalanceWorksheetFilterRequest): TrialBalanceWorksheetReportTemplate {
      val glDetails = mutableListOf<GeneralLedgerDetailEntity>()
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val whereClause = StringBuilder("WHERE glDetail.company_id = :comp_id AND glDetail.deleted = FALSE AND source.value NOT LIKE 'BAL'")

      if (filterRequest.beginAccount != null || filterRequest.endAccount != null) {
         params["beginAccount"] = filterRequest.beginAccount
         params["endAccount"] = filterRequest.endAccount
         whereClause.append(" AND acct.account_number")
            .append(
               buildFilterString(
                  filterRequest.beginAccount != null,
                  filterRequest.endAccount != null,
                  "beginAccount",
                  "endAccount"
               )
            )
      }
      if (filterRequest.fromDate != null || filterRequest.thruDate != null) {
         params["fromDate"] = filterRequest.fromDate
         params["thruDate"] = filterRequest.thruDate
         whereClause.append(" AND glDetail.date")
            .append(
               buildFilterString(filterRequest.fromDate != null, filterRequest.thruDate != null, "fromDate", "thruDate")
            )
      }
      if (filterRequest.profitCenter != null) {
         params["profitCenter"] = filterRequest.profitCenter
         whereClause.append(" AND glDetail.profit_center_id_sfk = :profitCenter")
      }
      jdbc.query(
         """
         ${selectBaseQuery()}
         $whereClause
         ORDER BY glDetail_account_number ${filterRequest.sortDirection()}
      """.trimIndent(),
         params
      ) { rs, elements ->
         do {
            val account = accountRepository.mapRow(rs, company, "glDetail_account_")
            val profitCenter = storeRepository.mapRow(rs, company, "glDetail_profitCenter_")
            val sourceCode = sourceCodeRepository.mapRow(rs, "glDetail_source_")
            glDetails.add(mapRow(rs, account, profitCenter, sourceCode, "glDetail_"))
         } while (rs.next())
      }

      var details = glDetails.groupBy {it.account}
         .map{ TrialBalanceWorksheetDetailDTO(
            it.key,
            it.value.sumByBigDecimal { if(it.amount >= BigDecimal.ZERO) it.amount else BigDecimal.ZERO},
            it.value.sumByBigDecimal { if(it.amount < BigDecimal.ZERO) it.amount else BigDecimal.ZERO})}
      return TrialBalanceWorksheetReportTemplate(details, details.sumOf{it.credits!!}, details.sumOf { it.debits!! })
   }

   fun mapRow(
      rs: ResultSet,
      account: AccountEntity,
      profitCenter: Store,
      source: GeneralLedgerSourceCodeEntity,
      columnPrefix: String = EMPTY
   ): GeneralLedgerDetailEntity {
      return GeneralLedgerDetailEntity(
         id = rs.getUuid("${columnPrefix}id"),
         account = account,
         date = rs.getLocalDate("${columnPrefix}date"),
         profitCenter = profitCenter,
         source = source,
         amount = rs.getBigDecimal("${columnPrefix}amount"),
         message = rs.getString("${columnPrefix}message"),
         employeeNumberId = rs.getIntOrNull("${columnPrefix}employee_number_id_sfk"),
         journalEntryNumber = rs.getIntOrNull("${columnPrefix}journal_entry_number")
      )
   }

   fun mapRow(
      rs: ResultSet,
      company: CompanyEntity,
      columnPrefix: String = EMPTY
   ): GeneralLedgerDetailEntity {
      return GeneralLedgerDetailEntity(
         id = rs.getUuid("${columnPrefix}id"),
         account = accountRepository.mapRow(rs, company, "acct_"),
         date = rs.getLocalDate("${columnPrefix}date"),
         profitCenter = storeRepository.mapRow(rs, company, "profitCenter_"),
         source = sourceCodeRepository.mapRow(rs, "source_"),
         amount = rs.getBigDecimal("${columnPrefix}amount"),
         message = rs.getString("${columnPrefix}message"),
         employeeNumberId = rs.getIntOrNull("${columnPrefix}employee_number_id_sfk"),
         journalEntryNumber = rs.getIntOrNull("${columnPrefix}journal_entry_number")
      )
   }

   fun mapNetChange(
      rs: ResultSet,
      columnPrefix: String = EMPTY
   ): GeneralLedgerNetChangeDTO {
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
      return GeneralLedgerNetChangeDTO(
         debit = rs.getBigDecimal("${columnPrefix}debit"),
         credit = rs.getBigDecimal("${columnPrefix}credit"),
         netActivityPeriod = netActivityPeriods,
         beginBalance = rs.getBigDecimal("${columnPrefix}begin_balance"),
         endBalance = rs.getBigDecimal("${columnPrefix}end_balance"),
         netChange = rs.getBigDecimal("${columnPrefix}net_change"),
      )
   }

   fun mapTrialBalanceEndOfReport(rs: ResultSet): TrialBalanceEndOfReportDTO {
      return TrialBalanceEndOfReportDTO(
         mtdDebitIE = rs.getBigDecimal("mtdDebitIE") ?: BigDecimal.ZERO,
         mtdCreditIE = rs.getBigDecimal("mtdCreditIE") ?: BigDecimal.ZERO,
         mtdDebitAL = rs.getBigDecimal("mtdDebitAL") ?: BigDecimal.ZERO,
         mtdCreditAL = rs.getBigDecimal("mtdCreditAL") ?: BigDecimal.ZERO,
         ytdDebitIE = rs.getBigDecimal("ytdDebitIE") ?: BigDecimal.ZERO,
         ytdCreditIE = rs.getBigDecimal("ytdCreditIE") ?: BigDecimal.ZERO,
         ytdDebitAL = rs.getBigDecimal("ytdDebitAL") ?: BigDecimal.ZERO,
         ytdCreditAL = rs.getBigDecimal("ytdCreditAL") ?: BigDecimal.ZERO
      )
   }

   private fun buildFilterString(begin: Boolean, end: Boolean, beginningParam: String, endingParam: String): String {
      return if (begin && end) " BETWEEN :$beginningParam AND :$endingParam "
      else if (begin) " >= :$beginningParam "
      else " <= :$endingParam "
   }


}