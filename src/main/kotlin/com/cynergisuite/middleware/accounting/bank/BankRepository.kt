package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.address.AddressRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.SimpleStore

import io.micronaut.spring.tx.annotation.Transactional
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
   private val addressRepository: AddressRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(BankRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         SELECT
            bank.id                                   AS bank_id,
            bank.uu_row_id                            AS bank_uu_row_id,
            bank.time_created                         AS bank_time_created,
            bank.time_updated                         AS bank_time_updated,
            bank.number                               AS bank_number,
            bank.name                                 AS bank_name,
            bank.account_number                       AS bank_account_number,
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
            store.id                                  AS store_id,
            store.number                              AS store_number,
            store.name                                AS store_name,
            store.dataset                             AS store_dataset,
            address.id                                AS address_id,
            address.uu_row_id                         AS address_uu_row_id,
            address.time_created                      AS address_time_created,
            address.time_updated                      AS address_time_updated,
            address.number                            AS address_number,
            address.name                              AS address_name,
            address.address1                          AS address_address1,
            address.address2                          AS address_address2,
            address.city                              AS address_city,
            address.state                             AS address_state,
            address.postal_code                       AS address_postal_code,
            address.latitude                          AS address_latitude,
            address.longitude                         AS address_longitude,
            address.country                           AS address_country,
            address.county                            AS address_county,
            currency.id                               AS currency_id,
            currency.value                            AS currency_value,
            currency.description                      AS currency_description,
            currency.localization_code                AS currency_localization_code
         FROM bank
               JOIN company comp                               ON bank.company_id = comp.id
               JOIN fastinfo_prod_import.store_vw store        ON store.dataset = comp.dataset_code
                                                                  AND store.number = bank.general_ledger_profit_center_sfk
               JOIN address                                    ON address.id = bank.address_id
               JOIN bank_currency_code_type_domain currency    ON currency.id = bank.currency_code_id
      """
   }

   fun findOne(id: Long, company: Company): BankEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${selectBaseQuery()} WHERE bank.id = :id AND comp.id = :comp_id"
      val found = jdbc.findFirstOrNull(query, params, RowMapper { rs, _ ->
            mapRow(rs, company, "bank_")
         }
      )

      logger.trace("Searching for Bank id {}: \nQuery {} \nResulted in {}", id, query, found)

      return found
   }

   fun findAll(company: Company, page: PageRequest): RepositoryPage<BankEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.myId())
      val query = """
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

      val address = addressRepository.insert(bank.address)
      val bank = bank.copy(address = address)

      return jdbc.insertReturning("""
         INSERT INTO bank(company_id, address_id, name, general_ledger_profit_center_sfk, account_number, currency_code_id)
	      VALUES (:company_id, :address_id, :name, :general_ledger_profit_center_sfk, :account_number, :currency_code_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to bank.company.myId(),
            "address_id" to bank.address.id,
            "name" to bank.name,
            "general_ledger_profit_center_sfk" to bank.generalLedgerProfitCenter.myNumber(),
            "account_number" to bank.accountNumber,
            "currency_code_id" to bank.currency.id
         ),
         RowMapper { rs, _ ->
            mapRow(rs, bank)
         }
      )
   }

   @Transactional
   fun update(bank: BankEntity): BankEntity {
      logger.debug("Updating bank {}", bank)
      addressRepository.update(bank.address)

      return jdbc.updateReturning("""
         UPDATE bank
         SET
            company_id = :company_id,
            address_id = :address_id,
            name = :name,
            general_ledger_profit_center_sfk = :general_ledger_profit_center_sfk,
            account_number = :account_number,
            currency_code_id = :currency_code_id
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to bank.id,
            "company_id" to bank.company.myId(),
            "address_id" to bank.address.id,
            "name" to bank.name,
            "general_ledger_profit_center_sfk" to bank.generalLedgerProfitCenter.myNumber(),
            "account_number" to bank.accountNumber,
            "currency_code_id" to bank.currency.id
         ),
         RowMapper { rs, _ ->
            mapRow(rs, bank)
         }
      )
   }

   private fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): BankEntity {
      return BankEntity(
         id = rs.getLong("${columnPrefix}id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         company = CompanyEntity.create(company)!!,
         address = mapAddress(rs, "address_"),
         name = rs.getString("${columnPrefix}name"),
         number = rs.getInt("${columnPrefix}number"),
         generalLedgerProfitCenter = mapSimpleStore(rs, company,"store_"),
         accountNumber = rs.getInt("${columnPrefix}account_number"),
         currency = mapCurrency(rs, "currency_")
      )
   }

   private fun mapRow(rs: ResultSet, bank: BankEntity, columnPrefix: String = EMPTY): BankEntity {
      return BankEntity(
         id = rs.getLong("${columnPrefix}id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         company = bank.company,
         address = bank.address,
         name = rs.getString("${columnPrefix}name"),
         number = rs.getInt("${columnPrefix}number"),
         generalLedgerProfitCenter = bank.generalLedgerProfitCenter,
         accountNumber = rs.getInt("${columnPrefix}account_number"),
         currency = bank.currency
      )
   }

   private fun mapCurrency(rs: ResultSet, columnPrefix: String): BankCurrencyType =
      BankCurrencyType(
         id = rs.getLong("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )

   private fun mapCompany(rs: ResultSet, columnPrefix: String): CompanyEntity =
      CompanyEntity(
         id = rs.getLong("${columnPrefix}id"),
         name = rs.getString("${columnPrefix}name"),
         doingBusinessAs = rs.getString("${columnPrefix}doing_business_as"),
         clientCode = rs.getString("${columnPrefix}client_code"),
         clientId = rs.getInt("${columnPrefix}client_id"),
         datasetCode = rs.getString("${columnPrefix}dataset_code"),
         federalIdNumber = rs.getString("${columnPrefix}federal_id_number")
      )

   private fun mapSimpleStore(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): SimpleStore =
      SimpleStore(
         id = rs.getLong("${columnPrefix}id"),
         number = rs.getInt("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         company = company
      )

   private fun mapAddress(rs: ResultSet, columnPrefix: String = EMPTY): AddressEntity =
      AddressEntity(
         id = rs.getLong("${columnPrefix}id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         number = rs.getInt("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         address1 = rs.getString("${columnPrefix}address1"),
         address2 = rs.getString("${columnPrefix}address2"),
         city = rs.getString("${columnPrefix}city"),
         state = rs.getString("${columnPrefix}state"),
         postalCode = rs.getString("${columnPrefix}postal_code"),
         latitude = rs.getDouble("${columnPrefix}latitude"),
         longitude = rs.getDouble("${columnPrefix}longitude"),
         country = rs.getString("${columnPrefix}country"),
         county = rs.getString("${columnPrefix}county")
      )

}
