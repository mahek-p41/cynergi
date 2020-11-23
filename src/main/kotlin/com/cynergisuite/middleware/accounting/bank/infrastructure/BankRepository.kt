package com.cynergisuite.middleware.accounting.bank.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.bank.BankEntity
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
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
class BankRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val accountRepository: AccountRepository,
   private val storeRepository: StoreRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(BankRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         WITH account AS (
            ${accountRepository.selectBaseQuery()}
         )
         SELECT
            bank.id                                              AS bank_id,
            bank.name                                            AS bank_name,
            bank.number                                          AS bank_number,
            bank.company_id                                      AS comp_id,
            account.account_id                                   AS account_id,
            account.account_number                               AS account_number,
            account.account_name                                 AS account_name,
            account.account_form_1099_field                      AS account_form_1099_field,
            account.account_corporate_account_indicator          AS account_corporate_account_indicator,
            account.comp_id                                      AS account_comp_id,
            account.type_id                                      AS account_type_id,
            account.type_value                                   AS account_type_value,
            account.type_description                             AS account_type_description,
            account.type_localization_code                       AS account_type_localization_code,
            account.balance_type_id                              AS account_balance_type_id,
            account.balance_type_value                           AS account_balance_type_value,
            account.balance_type_description                     AS account_balance_type_description,
            account.balance_type_localization_code               AS account_balance_type_localization_code,
            account.status_id                                    AS account_status_id,
            account.status_value                                 AS account_status_value,
            account.status_description                           AS account_status_description,
            account.status_localization_code                     AS account_status_localization_code,
            glProfitCenter.id                                    AS glProfitCenter_id,
            glProfitCenter.number                                AS glProfitCenter_number,
            glProfitCenter.name                                  AS glProfitCenter_name,
            glProfitCenter.dataset                               AS glProfitCenter_dataset
         FROM bank
               JOIN company comp ON bank.company_id = comp.id
               JOIN fastinfo_prod_import.store_vw glProfitCenter
                    ON glProfitCenter.dataset = comp.dataset_code
                       AND glProfitCenter.number = bank.general_ledger_profit_center_sfk
               JOIN account ON account.account_id = bank.general_ledger_account_id
      """
   }

   fun findOne(id: Long, company: Company): BankEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${selectBaseQuery()} WHERE bank.id = :id AND bank.company_id = :comp_id"
      val found = jdbc.findFirstOrNull(
         query, params,
         RowMapper { rs, _ ->
            mapRow(rs, company, "bank_")
         }
      )

      logger.trace("Searching for Bank id {}: \nQuery {} \nResulted in {}", id, query, found)

      return found
   }

   fun findByNumber(number: Long, company: Company): BankEntity? {
      val params = mutableMapOf<String, Any?>("number" to number, "comp_id" to company.myId())
      val query = "${selectBaseQuery()} WHERE bank.number = :number AND comp.id = :comp_id"
      val found = jdbc.findFirstOrNull(
         query, params,
         RowMapper { rs, _ ->
            mapRow(rs, company, "bank_")
         }
      )

      logger.trace("Searching for Bank id {}: \nQuery {} \nResulted in {}", number, query, found)

      return found
   }

   fun findAll(company: Company, page: PageRequest): RepositoryPage<BankEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.myId())
      val query =
         """
         WITH paged AS (
            ${selectBaseQuery()}
            WHERE bank.company_id = :comp_id
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER by bank_${page.snakeSortBy()} ${page.sortDirection()}
         LIMIT ${page.size()} OFFSET ${page.offset()}
      """
      var totalElements: Long? = null
      val resultList: MutableList<BankEntity> = mutableListOf()

      jdbc.query(query, params) { rs ->
         resultList.add(mapRow(rs, company, "bank_"))
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

   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM bank WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Bank: {} exists resulted in {}", id, exists)

      return exists
   }

   @Transactional
   fun insert(bank: BankEntity, company: Company): BankEntity {
      logger.debug("Inserting bank {}", bank)

      return jdbc.insertReturning(
         """
         INSERT INTO bank(number, company_id, name, general_ledger_profit_center_sfk, general_ledger_account_id)
	      VALUES (:number, :company_id, :name, :general_ledger_profit_center_sfk, :general_ledger_account_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "number" to bank.number,
            "company_id" to company.myId(),
            "name" to bank.name,
            "general_ledger_profit_center_sfk" to bank.generalLedgerProfitCenter.myNumber(),
            "general_ledger_account_id" to bank.generalLedgerAccount.id
         ),
         RowMapper { rs, _ ->
            mapRow(rs, bank)
         }
      )
   }

   @Transactional
   fun update(bank: BankEntity, company: Company): BankEntity {
      logger.debug("Updating bank {}", bank)

      return jdbc.updateReturning(
         """
         UPDATE bank
         SET
            number = :number,
            company_id = :company_id,
            name = :name,
            general_ledger_profit_center_sfk = :general_ledger_profit_center_sfk,
            general_ledger_account_id = :general_ledger_account_id
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to bank.id,
            "number" to bank.number,
            "company_id" to company.myId(),
            "name" to bank.name,
            "general_ledger_profit_center_sfk" to bank.generalLedgerProfitCenter.myNumber(),
            "general_ledger_account_id" to bank.generalLedgerAccount.id
         ),
         RowMapper { rs, _ ->
            mapRow(rs, bank)
         }
      )
   }

   fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = EMPTY, bankPrefix: String = EMPTY): BankEntity {
      return BankEntity(
         id = rs.getLong("${columnPrefix}id"),
         number = rs.getLong("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         generalLedgerProfitCenter = storeRepository.mapRow(rs, company, "${bankPrefix}glProfitCenter_"),
         generalLedgerAccount = accountRepository.mapRow(rs, company, "${bankPrefix}account_", "${bankPrefix}account_")
      )
   }

   private fun mapRow(rs: ResultSet, bank: BankEntity, columnPrefix: String = EMPTY): BankEntity {
      return BankEntity(
         id = rs.getLong("${columnPrefix}id"),
         number = rs.getLong("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         generalLedgerProfitCenter = bank.generalLedgerProfitCenter,
         generalLedgerAccount = bank.generalLedgerAccount
      )
   }
}
