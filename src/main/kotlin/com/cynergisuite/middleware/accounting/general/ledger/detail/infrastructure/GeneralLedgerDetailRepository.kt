package com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailEntity
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class GeneralLedgerDetailRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val accountRepository: AccountRepository,
   private val storeRepository: StoreRepository,
   private val sourceCodeRepository: GeneralLedgerSourceCodeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerDetailRepository::class.java)

   private fun selectBaseQuery(): String {
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
            acct.comp_id                                              AS acct_comp_id,
            acct.comp_uu_row_id                                       AS acct_comp_uu_row_id,
            acct.comp_time_created                                    AS acct_comp_time_created,
            acct.comp_time_updated                                    AS acct_comp_time_updated,
            acct.comp_name                                            AS acct_comp_name,
            acct.comp_doing_business_as                               AS acct_comp_doing_business_as,
            acct.comp_client_code                                     AS acct_comp_client_code,
            acct.comp_client_id                                       AS acct_comp_client_id,
            acct.comp_dataset_code                                    AS acct_comp_dataset_code,
            acct.comp_federal_id_number                               AS acct_comp_federal_id_number,
            acct.type_id                                              AS acct_type_id,
            acct.type_value                                           AS acct_type_value,
            acct.type_description                                     AS acct_type_description,
            acct.type_localization_code                               AS acct_type_localization_code,
            acct.balance_type_id                                      AS acct_balance_type_id,
            acct.balance_type_value                                   AS acct_balance_type_value,
            acct.balance_type_description                             AS acct_balance_type_description,
            acct.balance_type_localization_code                       AS acct_balance_type_localization_code,
            acct.status_id                                            AS acct_status_id,
            acct.status_value                                         AS acct_status_value,
            acct.status_description                                   AS acct_status_description,
            acct.status_localization_code                             AS acct_status_localization_code,
            profitCenter.id                                           AS profitCenter_id,
            profitCenter.number                                       AS profitCenter_number,
            profitCenter.name                                         AS profitCenter_name,
            profitCenter.dataset                                      AS profitCenter_dataset,
            source.id                                                 AS source_id,
            source.company_id                                         AS source_company_id,
            source.value                                              AS source_value,
            source.description                                        AS source_description
         FROM general_ledger_detail glDetail
            JOIN company comp ON glDetail.company_id = comp.id
            JOIN fastinfo_prod_import.store_vw profitCenter
                    ON profitCenter.dataset = comp.dataset_code
                       AND profitCenter.number = glDetail.profit_center_id_sfk
            JOIN account acct ON glDetail.account_id = acct.account_id
            JOIN general_ledger_source_codes source on glDetail.source_id = source.id
      """
   }

   fun exists(company: Company): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS (SELECT company_id FROM general_ledger_detail WHERE company_id = :company_id)", mapOf("company_id" to company.myId()), Boolean::class.java)!!

      logger.trace("Checking if GeneralLedgerDetail: {} exists resulted in {}", company, exists)

      return exists
   }

   fun findOne(id: Long, company: Company): GeneralLedgerDetailEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${selectBaseQuery()} WHERE glDetail.id = :id AND glDetail.company_id = :comp_id"
      val found = jdbc.findFirstOrNull(
         query, params,
         RowMapper { rs, _ ->
            val account = accountRepository.mapRow(rs, company, "acct_", "acct_")
            val profitCenter = storeRepository.mapRow(rs, company, "profitCenter_")
            val sourceCode = sourceCodeRepository.mapDdlRow(rs, company, "source_")

            mapRow(
               rs,
               account,
               profitCenter,
               sourceCode,
               "glDetail_"
            )
         }
      )

      logger.trace("Searching for GeneralLedgerDetail id: {} resulted in {}\nQuery {}", id, found, query)

      return found
   }

   @Transactional
   fun insert(entity: GeneralLedgerDetailEntity, company: Company): GeneralLedgerDetailEntity {
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
            "company_id" to company.myId(),
            "account_id" to entity.account.myId(),
            "profit_center_id_sfk" to entity.profitCenter.myNumber(),
            "date" to entity.date,
            "source_id" to entity.source.myId(),
            "amount" to entity.amount,
            "message" to entity.message,
            "employee_number_id_sfk" to entity.employeeNumberId,
            "journal_entry_number" to entity.journalEntryNumber
         ),
         RowMapper { rs, _ ->
            mapRow(
               rs,
               entity.account,
               entity.profitCenter,
               entity.source
            )
         }
      )
   }

   @Transactional
   fun update(entity: GeneralLedgerDetailEntity, company: Company): GeneralLedgerDetailEntity {
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
            "company_id" to company.myId(),
            "account_id" to entity.account.myId(),
            "profit_center_id_sfk" to entity.profitCenter.myNumber(),
            "date" to entity.date,
            "source_id" to entity.source.myId(),
            "amount" to entity.amount,
            "message" to entity.message,
            "employee_number_id_sfk" to entity.employeeNumberId,
            "journal_entry_number" to entity.journalEntryNumber
         ),
         RowMapper { rs, _ ->
            mapRow(
               rs,
               entity.account,
               entity.profitCenter,
               entity.source
            )
         }
      )
   }

   private fun mapRow(
      rs: ResultSet,
      account: AccountEntity,
      profitCenter: Store,
      source: GeneralLedgerSourceCodeEntity,
      columnPrefix: String = EMPTY
   ): GeneralLedgerDetailEntity {
      return GeneralLedgerDetailEntity(
         id = rs.getLong("${columnPrefix}id"),
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
}
