package com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure

import com.cynergisuite.domain.GeneralLedgerSearchReportFilterRequest
import com.cynergisuite.domain.GeneralLedgerSourceReportFilterRequest
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getBigDecimalOrNull
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceReportSourceDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailEntity
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailFilterRequest
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailPageRequest
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.accounting.general.ledger.inquiry.GeneralLedgerNetChangeDTO
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
class GeneralLedgerDetailRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val accountRepository: AccountRepository,
   private val storeRepository: StoreRepository,
   private val sourceCodeRepository: GeneralLedgerSourceCodeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerDetailRepository::class.java)

   fun selectBaseQuery(): String {
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
            JOIN general_ledger_summary glSummary ON glSummary.company_id = glDetail.company_id
                 AND glSummary.account_id = glDetail.account_id
                 AND glSummary.profit_center_id_sfk = glDetail.profit_center_id_sfk
      """
   }

   fun selectNetChangeQuery(): String {
      return """
         WITH account AS (
            ${accountRepository.selectBaseQuery()}
         )
         SELECT
             glDetail.company_id                                                    AS glDetail_company_id,
             acct.account_number                                                    AS glDetail_account_number,
             SUM (case when glDetail.amount >= 0 then glDetail.amount else 0 end)   AS debit,
             SUM (case when glDetail.amount < 0 then glDetail.amount else 0 end)    AS credit,
             SUM (glDetail.amount)                                                  AS net_change,
             glSummary.beginning_balance                                            AS begin_balance,
             COALESCE(glSummary.beginning_balance, 0) + SUM (glDetail.amount)          AS end_balance
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

   @ReadOnly
   fun exists(company: CompanyEntity): Boolean {
      val exists = jdbc.queryForObject(
         "SELECT EXISTS (SELECT company_id FROM general_ledger_detail WHERE company_id = :company_id)",
         mapOf("company_id" to company.id),
         Boolean::class.java
      )

      logger.trace("Checking if GeneralLedgerDetail: {} exists resulted in {}", company, exists)

      return exists
   }

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): GeneralLedgerDetailEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query = "${selectBaseQuery()} WHERE glDetail.id = :id AND glDetail.company_id = :comp_id"
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
      val whereClause = StringBuilder(" WHERE glDetail.company_id = :comp_id ")
      val groupBy = StringBuilder(" GROUP BY glDetail.company_id, acct.account_number, glSummary.beginning_balance")
      if (filterRequest.from != null || filterRequest.thru != null) {
         params["from"] = filterRequest.from
         params["thru"] = filterRequest.thru
         whereClause.append(" AND glDetail.date ")
            .append(buildFilterString(filterRequest.from != null, filterRequest.thru != null, "from", "thru"))
      }
      if (filterRequest.profitCenter != null) {
         params["profitCenter"] = filterRequest.profitCenter
         whereClause.append(" AND profitCenter.number = :profitCenter")
         groupBy.append(", glDetail.profit_center_id_sfk")
      }
      if (filterRequest.account != null) {
         params["account"] = filterRequest.account
         whereClause.append(" AND acct.account_number = :account")
      }
      if (filterRequest.fiscalYear != null) {
         params["fiscalYear"] = filterRequest.fiscalYear
         whereClause.append(" AND glSummary.overall_period_id = (SELECT DISTINCT overall_period_id FROM financial_calendar WHERE fiscal_year = :fiscalYear AND company_id = :comp_id) ")
      }
      val query =
         "${selectNetChangeQuery()}\n$whereClause\n$groupBy"

      logger.info("Querying for General Ledger Inquiry Net Change using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs, _  ->
         val generalLedgerInquiry = mapNetChange(rs)

         generalLedgerInquiry
      }

      logger.info("Querying for General Ledger Inquiry Net Change resulted in {}", found)

      return found
   }

   @ReadOnly
   fun findAll(company: CompanyEntity, page: GeneralLedgerDetailPageRequest): RepositoryPage<GeneralLedgerDetailEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "limit" to page.size(), "offset" to page.offset())
      val whereClause = StringBuilder(" WHERE glDetail.company_id = :comp_id ")
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
            ORDER BY glDetail_${page.snakeSortBy()} ${page.sortDirection()}
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
            WHERE company_id = :comp_id
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
      val whereClause = StringBuilder("WHERE glDetail.company_id = :comp_id ")

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
         params["description"] = filterRequest.description
         whereClause.append(" AND glDetail.message = :description")
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
            ${selectBaseQuery()}
            $whereClause
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
      val whereClause = StringBuilder("WHERE glDetail.company_id = :comp_id AND source.value = :source_code_value")

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

      if (filterRequest.fiscalYear != null) {
         params["fiscalYear"] = filterRequest.fiscalYear
         whereClause.append(" AND glSummary.overall_period_id = (SELECT DISTINCT overall_period_id FROM financial_calendar WHERE fiscal_year = :fiscalYear AND company_id = :comp_id) ")
      }

      jdbc.query(
         """
            ${selectBaseQuery()}
            $whereClause
            ORDER BY glDetail_${filterRequest.snakeSortBy()} ${filterRequest.sortDirection()}
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

   fun mapNetChange(
      rs: ResultSet,
      columnPrefix: String = EMPTY
   ): GeneralLedgerNetChangeDTO {
      return GeneralLedgerNetChangeDTO(
         debit = rs.getBigDecimalOrNull("${columnPrefix}debit") ?: BigDecimal.ZERO,
         credit = rs.getBigDecimalOrNull("${columnPrefix}credit") ?: BigDecimal.ZERO,
         beginBalance = rs.getBigDecimalOrNull("${columnPrefix}begin_balance") ?: BigDecimal.ZERO,
         endBalance = rs.getBigDecimalOrNull("${columnPrefix}end_balance") ?: BigDecimal.ZERO,
         netChange = rs.getBigDecimalOrNull("${columnPrefix}net_change") ?: BigDecimal.ZERO,
      )
   }

   private fun buildFilterString(begin: Boolean, end: Boolean, beginningParam: String, endingParam: String): String {
      return if (begin && end) " BETWEEN :$beginningParam AND :$endingParam "
      else if (begin) " > :$beginningParam "
      else " < :$endingParam "
   }


}
