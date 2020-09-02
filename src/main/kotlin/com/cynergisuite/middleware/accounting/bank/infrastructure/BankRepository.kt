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
import javax.transaction.Transactional
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BankRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val accountRepository: AccountRepository,
   private val companyRepository: CompanyRepository,
   private val storeRepository: StoreRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(BankRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         WITH account AS (
            ${accountRepository.selectBaseQuery()}
         ), company AS (
            ${companyRepository.companyBaseQuery()}
         )
         SELECT
            bank.id                                   AS bank_id,
            bank.name                                 AS bank_name,
            comp.id                                   AS comp_id,
            comp.uu_row_id                            AS comp_uu_row_id,
            comp.time_created                         AS comp_time_created,
            comp.time_updated                         AS comp_time_updated,
            comp.name                                 AS comp_name,
            comp.doing_business_as                    AS comp_doing_business_as,
            comp.client_code                          AS comp_client_code,
            comp.client_id                            AS comp_client_id,
            comp.dataset_code                         AS comp_dataset_code,
            comp.federal_id_number                    AS comp_federal_id_number,
            comp.address_id                           AS comp_address_id,
            comp.address_name                         AS address_name,
            comp.address_address1                     AS address_address1,
            comp.address_address2                     AS address_address2,
            comp.address_city                         AS address_city,
            comp.address_state                        AS address_state,
            comp.address_postal_code                  AS address_postal_code,
            comp.address_latitude                     AS address_latitude,
            comp.address_longitude                    AS address_longitude,
            comp.address_country                      AS address_country,
            comp.address_county                       AS address_county,
            comp.address_phone                        AS address_phone,
            comp.address_fax                          AS address_fax,
            account.*,
            glProfitCenter.id                         AS glProfitCenter_id,
            glProfitCenter.number                     AS glProfitCenter_number,
            glProfitCenter.name                       AS glProfitCenter_name,
            glProfitCenter.dataset                    AS glProfitCenter_dataset
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
      val query = "${selectBaseQuery()} WHERE bank.id = :id AND comp.id = :comp_id"
      val found = jdbc.findFirstOrNull(
         query, params,
         RowMapper { rs, _ ->
            mapRow(rs, company, "bank_")
         }
      )

      logger.trace("Searching for Bank id {}: \nQuery {} \nResulted in {}", id, query, found)

      return found
   }

   fun findAll(company: Company, page: PageRequest): RepositoryPage<BankEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.myId())
      val query =
         """
         WITH paged AS (
            ${selectBaseQuery()}
            WHERE comp.id = :comp_id
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
   fun insert(bank: BankEntity): BankEntity {
      logger.debug("Inserting bank {}", bank)

      return jdbc.insertReturning(
         """
         INSERT INTO bank(company_id, name, general_ledger_profit_center_sfk, general_ledger_account_id)
	      VALUES (:company_id, :name, :general_ledger_profit_center_sfk, :general_ledger_account_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to bank.company.myId(),
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
   fun update(bank: BankEntity): BankEntity {
      logger.debug("Updating bank {}", bank)

      return jdbc.updateReturning(
         """
         UPDATE bank
         SET
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
            "company_id" to bank.company.myId(),
            "name" to bank.name,
            "general_ledger_profit_center_sfk" to bank.generalLedgerProfitCenter.myNumber(),
            "general_ledger_account_id" to bank.generalLedgerAccount.id
         ),
         RowMapper { rs, _ ->
            mapRow(rs, bank)
         }
      )
   }

   private fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): BankEntity {
      return BankEntity(
         id = rs.getLong("${columnPrefix}id"),
         company = company,
         name = rs.getString("${columnPrefix}name"),
         generalLedgerProfitCenter = storeRepository.mapRow(rs, company, "glProfitCenter_"),
         generalLedgerAccount = accountRepository.mapRow(rs, company, "account_")
      )
   }

   private fun mapRow(rs: ResultSet, bank: BankEntity, columnPrefix: String = EMPTY): BankEntity {
      return BankEntity(
         id = rs.getLong("${columnPrefix}id"),
         company = bank.company,
         name = rs.getString("${columnPrefix}name"),
         generalLedgerProfitCenter = bank.generalLedgerProfitCenter,
         generalLedgerAccount = bank.generalLedgerAccount
      )
   }
}
