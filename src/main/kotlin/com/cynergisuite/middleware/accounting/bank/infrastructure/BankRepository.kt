package com.cynergisuite.middleware.accounting.bank.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.softDelete
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.bank.BankEntity
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class BankRepository @Inject constructor(
   private val jdbc: Jdbi,
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
            bank.id                                                        AS bank_id,
            bank.name                                                      AS bank_name,
            bank.number                                                    AS bank_number,
            bank.company_id                                                AS bank_comp_id,
            bank.deleted                                                   AS bank_deleted,
            account.account_id                                             AS bank_account_id,
            account.account_number                                         AS bank_account_number,
            account.account_name                                           AS bank_account_name,
            account.account_form_1099_field                                AS bank_account_form_1099_field,
            account.account_corporate_account_indicator                    AS bank_account_corporate_account_indicator,
            account.account_comp_id                                        AS bank_account_comp_id,
            account.account_deleted                                        AS bank_account_deleted,
            account.account_type_id                                        AS bank_account_type_id,
            account.account_type_value                                     AS bank_account_type_value,
            account.account_type_description                               AS bank_account_type_description,
            account.account_type_localization_code                         AS bank_account_type_localization_code,
            account.account_balance_type_id                                AS bank_account_balance_type_id,
            account.account_balance_type_value                             AS bank_account_balance_type_value,
            account.account_balance_type_description                       AS bank_account_balance_type_description,
            account.account_balance_type_localization_code                 AS bank_account_balance_type_localization_code,
            account.account_status_id                                      AS bank_account_status_id,
            account.account_status_value                                   AS bank_account_status_value,
            account.account_status_description                             AS bank_account_status_description,
            account.account_status_localization_code                       AS bank_account_status_localization_code,
            glProfitCenter.id                                              AS bank_glProfitCenter_id,
            glProfitCenter.number                                          AS bank_glProfitCenter_number,
            glProfitCenter.name                                            AS bank_glProfitCenter_name,
            glProfitCenter.dataset                                         AS bank_glProfitCenter_dataset
         FROM bank
               JOIN company comp ON bank.company_id = comp.id AND comp.deleted = FALSE
               JOIN fastinfo_prod_import.store_vw glProfitCenter
                    ON glProfitCenter.dataset = comp.dataset_code
                       AND glProfitCenter.number = bank.general_ledger_profit_center_sfk
               JOIN account ON account.account_id = bank.general_ledger_account_id AND account.account_deleted = FALSE
      """
   }

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): BankEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query = "${selectBaseQuery()} WHERE bank.id = :id AND bank.company_id = :comp_id AND bank.deleted = FALSE AND comp.deleted = FALSE"
      val found = jdbc.findFirstOrNull(
         query,
         params
      ) { rs, _ ->
         mapRow(rs, company, "bank_")
      }

      logger.trace("Searching for Bank id {}: \nQuery {} \nResulted in {}", id, query, found)

      return found
   }

   @ReadOnly
   fun findByNumber(number: Long, company: CompanyEntity): BankEntity? {
      val params = mutableMapOf<String, Any?>("number" to number, "comp_id" to company.id)
      val query = "${selectBaseQuery()} WHERE bank.number = :number AND comp.id = :comp_id AND bank.deleted = FALSE AND comp.deleted = FALSE"
      val found = jdbc.findFirstOrNull(
         query,
         params
      ) { rs, _ ->
         mapRow(rs, company, "bank_")
      }

      logger.trace("Searching for Bank id {}: \nQuery {} \nResulted in {}", number, query, found)

      return found
   }

   @ReadOnly
   fun findAll(company: CompanyEntity, page: PageRequest): RepositoryPage<BankEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val query =
         """
         WITH paged AS (
            ${selectBaseQuery()}
            WHERE bank.company_id = :comp_id AND bank.deleted = FALSE AND comp.deleted = FALSE
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

      jdbc.query(query, params) { rs, _ ->
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

   @ReadOnly
   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject(
         "SELECT EXISTS(SELECT id FROM bank WHERE id = :id AND bank.deleted = FALSE AND comp.deleted = FALSE)",
         mapOf("id" to id),
         Boolean::class.java
      )

      logger.trace("Checking if Bank: {} exists resulted in {}", id, exists)

      return exists
   }

   @Transactional
   fun insert(bank: BankEntity): BankEntity {
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
            "company_id" to bank.generalLedgerProfitCenter.myCompany().id,
            "name" to bank.name,
            "general_ledger_profit_center_sfk" to bank.generalLedgerProfitCenter.myNumber(),
            "general_ledger_account_id" to bank.generalLedgerAccount.id
         )
      ) { rs, _ ->
         mapRow(rs, bank)
      }
   }

   @Transactional
   fun update(bank: BankEntity): BankEntity {
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
            "company_id" to bank.generalLedgerProfitCenter.myCompany().id,
            "name" to bank.name,
            "general_ledger_profit_center_sfk" to bank.generalLedgerProfitCenter.myNumber(),
            "general_ledger_account_id" to bank.generalLedgerAccount.id
         )
      ) { rs, _ ->
         mapRow(rs, bank)
      }
   }

   @Transactional
   fun delete(id: UUID, company: CompanyEntity) {
      logger.debug("Deleting bank with id={}", id)

      val affectedRows = jdbc.softDelete(
         """
         UPDATE bank
         SET deleted = TRUE
         WHERE id = :id AND company_id = :company_id AND deleted = FALSE
         """,
         mapOf("id" to id, "company_id" to company.id),
         "bank"
      )

      logger.info("Affected rows: {}", affectedRows)

      if (affectedRows == 0) throw NotFoundException(id)
   }

   fun mapRow(rs: ResultSet, company: CompanyEntity, columnPrefix: String = EMPTY): BankEntity {
      return BankEntity(
         id = rs.getUuid("${columnPrefix}id"),
         number = rs.getLong("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         generalLedgerProfitCenter = storeRepository.mapRow(rs, company, "${columnPrefix}glProfitCenter_"),
         generalLedgerAccount = accountRepository.mapRow(rs, company, "${columnPrefix}account_")
      )
   }

   private fun mapRow(rs: ResultSet, bank: BankEntity, columnPrefix: String = EMPTY): BankEntity {
      return BankEntity(
         id = rs.getUuid("${columnPrefix}id"),
         number = rs.getLong("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         generalLedgerProfitCenter = bank.generalLedgerProfitCenter,
         generalLedgerAccount = bank.generalLedgerAccount
      )
   }
}
