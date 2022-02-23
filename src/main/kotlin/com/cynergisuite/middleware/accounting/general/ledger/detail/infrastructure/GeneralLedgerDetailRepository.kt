package com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure

import com.cynergisuite.domain.GeneralLedgerSearchReportFilterRequest
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
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
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSearchReportTemplate
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailEntity
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
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
            acct.account_id                                           AS acct_id,
            acct.account_number                                       AS acct_number,
            acct.account_name                                         AS acct_name,
            acct.account_form_1099_field                              AS acct_form_1099_field,
            acct.account_corporate_account_indicator                  AS acct_corporate_account_indicator,
            acct.account_comp_id                                      AS acct_comp_id,
            acct.account_deleted                                      AS acct_deleted,
            acct.account_type_id                                      AS acct_type_id,
            acct.account_type_value                                   AS acct_type_value,
            acct.account_type_description                             AS acct_type_description,
            acct.account_type_localization_code                       AS acct_type_localization_code,
            acct.account_balance_type_id                              AS acct_balance_type_id,
            acct.account_balance_type_value                           AS acct_balance_type_value,
            acct.account_balance_type_description                     AS acct_balance_type_description,
            acct.account_balance_type_localization_code               AS acct_balance_type_localization_code,
            acct.account_status_id                                    AS acct_status_id,
            acct.account_status_value                                 AS acct_status_value,
            acct.account_status_description                           AS acct_status_description,
            acct.account_status_localization_code                     AS acct_status_localization_code,
            acct.account_vendor_1099_type_id                          AS acct_vendor_1099_type_id,
            acct.account_vendor_1099_type_value                       AS acct_vendor_1099_type_value,
            acct.account_vendor_1099_type_description                 AS acct_vendor_1099_type_description,
            acct.account_vendor_1099_type_localization_code           AS acct_vendor_1099_type_localization_code,
            profitCenter.id                                           AS profitCenter_id,
            profitCenter.number                                       AS profitCenter_number,
            profitCenter.name                                         AS profitCenter_name,
            profitCenter.dataset                                      AS profitCenter_dataset,
            source.id                                                 AS source_id,
            source.company_id                                         AS source_company_id,
            source.value                                              AS source_value,
            source.description                                        AS source_description,
            source.deleted                                            AS source_deleted,
            count(*) OVER() AS total_elements
         FROM general_ledger_detail glDetail
            JOIN company comp ON glDetail.company_id = comp.id AND comp.deleted = FALSE
            JOIN fastinfo_prod_import.store_vw profitCenter
                    ON profitCenter.dataset = comp.dataset_code
                       AND profitCenter.number = glDetail.profit_center_id_sfk
            JOIN account acct ON glDetail.account_id = acct.account_id AND acct.account_deleted = FALSE
            JOIN general_ledger_source_codes source ON glDetail.source_id = source.id AND source.deleted = FALSE
      """
   }

   @ReadOnly
   fun exists(company: CompanyEntity): Boolean {
      val exists = jdbc.queryForObject(
         "SELECT EXISTS (SELECT company_id FROM general_ledger_detail WHERE company_id = :company_id)",
         mapOf("company_id" to company.id),
         Boolean::class.java
      )!!

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
         val account = accountRepository.mapRow(rs, company, "acct_")
         val profitCenter = storeRepository.mapRow(rs, company, "profitCenter_")
         val sourceCode = sourceCodeRepository.mapRow(rs, "source_")

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
   fun findAll(company: CompanyEntity, page: PageRequest): RepositoryPage<GeneralLedgerDetailEntity, PageRequest> {
      return jdbc.queryPaged(
         """
            ${selectBaseQuery()}
            WHERE glDetail.company_id = :comp_id
            ORDER BY glDetail_${page.snakeSortBy()} ${page.sortDirection()}
            LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "limit" to page.size(),
            "offset" to page.offset()
         ),
         page
      ) { rs, elements ->
         val account = accountRepository.mapRow(rs, company, "acct_")
         val profitCenter = storeRepository.mapRow(rs, company, "profitCenter_")
         val sourceCode = sourceCodeRepository.mapRow(rs, "source_")

         do {
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
      var currentEntity: GeneralLedgerDetailEntity? = null
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val whereClause = StringBuilder("WHERE glDetail.company_id = :comp_id ")

      if (filterRequest.startingAccount != null && filterRequest.endingAccount != null) {
         params["startingAccount"] = filterRequest.startingAccount
         params["endingAccount"] = filterRequest.endingAccount
         whereClause.append(" AND acct.account_number ")
            .append(buildNumberFilterString("startingAccount", "endingAccount"))
      }

      if (filterRequest.profitCenter != null) {
         params["profitCenter"] = filterRequest.profitCenter
         whereClause.append(" AND profitCenter.id = :profitCenter")
      }

      if (filterRequest.sourceCode != null) {
         params["sourceCode"] = filterRequest.sourceCode
         whereClause.append(" AND source.value = :sourceCode")
      }

      when (filterRequest.typeEntry) {
         "C" -> whereClause.append(" AND glDetail.amount <= 0 ")
         "D" -> whereClause.append(" AND glDetail.amount >= 0 ")
      }
      if(filterRequest.lowAmount != null && filterRequest.highAmount != null) {
         params["lowAmount"] = filterRequest.lowAmount
         params["highAmount"] = filterRequest.highAmount
         whereClause.append(" AND glDetail.amount ")
            .append(buildNumberFilterString("lowAmount", "highAmount"))
      }

      if (filterRequest.description != null ) {
         params["description"] = filterRequest.description
         whereClause.append(" AND glDetail.message = :description")
      }

      if (filterRequest.jeNumber != null) {
         params["jeNumber"] = filterRequest.jeNumber
         whereClause.append(" AND glDetail.journal_entry_number = :jeNumber")
      }

      if (filterRequest.frmPmtDt != null && filterRequest.thruPmtDt != null) {
         params["frmPmtDt"] = filterRequest.frmPmtDt
         params["thruPmtDt"] = filterRequest.thruPmtDt
         whereClause.append(" AND glDetail.date ")
            .append(buildNumberFilterString("frmPmtDt", "thruPmtDt"))
      }

      jdbc.query(
         """
            ${selectBaseQuery()}
            $whereClause
         """.trimIndent(),
         params
      ) { rs, _ ->
         do {
            val account = accountRepository.mapRow(rs, company, "acct_")
            val profitCenter = storeRepository.mapRow(rs, company, "profitCenter_")
            val sourceCode = sourceCodeRepository.mapRow(rs, "source_")
            val currentEntity = mapRow(rs, account, profitCenter, sourceCode, "glDetail_")
            reports.add(currentEntity)
         } while (rs.next())
      }

      return reports
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

   private fun buildNumberFilterString(beginningParam: String, endingParam: String): String {
      return " BETWEEN :$beginningParam AND :$endingParam "
   }
}
