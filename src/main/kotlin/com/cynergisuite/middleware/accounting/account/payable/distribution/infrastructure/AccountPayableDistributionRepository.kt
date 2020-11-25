package com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionEntity
import com.cynergisuite.middleware.company.Company
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
class AccountPayableDistributionRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val accountRepository: AccountRepository,
   private val storeRepository: StoreRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableDistributionRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         WITH account AS (
            ${accountRepository.selectBaseQuery()}
         )
         SELECT
            apDist.id                                                      AS apDist_id,
            apDist.uu_row_id                                               AS apDist_uu_row_id,
            apDist.time_created                                            AS apDist_time_created,
            apDist.time_updated                                            AS apDist_time_updated,
            apDist.name                                                    AS apDist_name,
            apDist.percent                                                 AS apDist_percent,
            apDist.profit_center_sfk                                       AS apDist_profit_center_sfk,
            profitCenter.id                                                AS profitCenter_id,
            profitCenter.number                                            AS profitCenter_number,
            profitCenter.name                                              AS profitCenter_name,
            profitCenter.dataset                                           AS profitCenter_dataset,
            account.account_id                                             AS account_id,
            account.account_number                                         AS account_number,
            account.account_name                                           AS account_name,
            account.account_form_1099_field                                AS account_form_1099_field,
            account.account_corporate_account_indicator                    AS account_corporate_account_indicator,
            account.comp_id                                                AS account_comp_id,
            account.comp_uu_row_id                                         AS account_comp_uu_row_id,
            account.comp_time_created                                      AS account_comp_time_created,
            account.comp_time_updated                                      AS account_comp_time_updated,
            account.comp_name                                              AS account_comp_name,
            account.comp_doing_business_as                                 AS account_comp_doing_business_as,
            account.comp_client_code                                       AS account_comp_client_code,
            account.comp_client_id                                         AS account_comp_client_id,
            account.comp_dataset_code                                      AS account_comp_dataset_code,
            account.comp_federal_id_number                                 AS account_comp_federal_id_number,
            account.type_id                                                AS account_type_id,
            account.type_value                                             AS account_type_value,
            account.type_description                                       AS account_type_description,
            account.type_localization_code                                 AS account_type_localization_code,
            account.balance_type_id                                        AS account_balance_type_id,
            account.balance_type_value                                     AS account_balance_type_value,
            account.balance_type_description                               AS account_balance_type_description,
            account.balance_type_localization_code                         AS account_balance_type_localization_code,
            account.status_id                                              AS account_status_id,
            account.status_value                                           AS account_status_value,
            account.status_description                                     AS account_status_description,
            account.status_localization_code                               AS account_status_localization_code
         FROM account_payable_distribution_template apDist
            JOIN company comp ON apDist.company_id = comp.id
            JOIN fastinfo_prod_import.store_vw profitCenter
               ON profitCenter.dataset = comp.dataset_code
                  AND profitCenter.number = apDist.profit_center_sfk
            JOIN account ON account.account_id = apDist.account_id
      """
   }

   fun findOne(id: Long, company: Company): AccountPayableDistributionEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${selectBaseQuery()} WHERE apDist.id = :id AND comp.id = :comp_id"
      val found = jdbc.findFirstOrNull(
         query, params,
         RowMapper { rs, _ ->
            mapRow(rs, company, "apDist_")
         }
      )

      logger.trace("Searching for AccountPayableDistribution id {}: \nQuery {} \nResulted in {}", id, query, found)

      return found
   }

   fun findAll(company: Company, page: PageRequest): RepositoryPage<AccountPayableDistributionEntity, PageRequest> {
      var totalElements: Long? = null
      val resultList: MutableList<AccountPayableDistributionEntity> = mutableListOf()

      jdbc.query(
         """
         WITH paged AS (
            ${selectBaseQuery()}
            WHERE comp.id = :comp_id
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER by apDist_${page.snakeSortBy()} ${page.sortDirection()}
         LIMIT :limit OFFSET :offset
         """,
         mapOf(
            "comp_id" to company.myId(),
            "limit" to page.size(),
            "offset" to page.offset()
         )
      ) { rs ->
         resultList.add(mapRow(rs, company, "apDist_"))
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

   @Transactional
   fun insert(entity: AccountPayableDistributionEntity, company: Company): AccountPayableDistributionEntity {
      logger.debug("Inserting AccountPayableDistribution {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO account_payable_distribution_template(name, profit_center_sfk, account_id, company_id, percent)
	      VALUES (:name, :profit_center_sfk, :account_id, :company_id, :percent)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "name" to entity.name,
            "profit_center_sfk" to entity.profitCenter.myNumber(),
            "account_id" to entity.account.id,
            "company_id" to company.myId(),
            "percent" to entity.percent
         ),
         RowMapper { rs, _ ->
            mapRow(rs, entity)
         }
      )
   }

   @Transactional
   fun update(entity: AccountPayableDistributionEntity, company: Company): AccountPayableDistributionEntity {
      logger.debug("Updating AccountPayableDistribution {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE account_payable_distribution_template
         SET
            name = :name,
            profit_center_sfk = :profit_center_sfk,
            account_id = :account_id,
            company_id = :company_id,
            percent = :percent
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "name" to entity.name,
            "profit_center_sfK" to entity.profitCenter.myNumber(),
            "account_id" to entity.account.id,
            "company_id" to company.myId(),
            "percent" to entity.percent
         ),
         RowMapper { rs, _ ->
            mapRow(rs, entity)
         }
      )
   }

   fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): AccountPayableDistributionEntity {
      return AccountPayableDistributionEntity(
         id = rs.getLong("${columnPrefix}id"),
         name = rs.getString("${columnPrefix}name"),
         profitCenter = storeRepository.mapRow(rs, company, "profitCenter_"),
         account = accountRepository.mapRow(rs, company, "account_"),
         percent = rs.getBigDecimal("${columnPrefix}percent")
      )
   }

   private fun mapRow(rs: ResultSet, entity: AccountPayableDistributionEntity, columnPrefix: String = EMPTY): AccountPayableDistributionEntity {
      return AccountPayableDistributionEntity(
         id = rs.getLong("${columnPrefix}id"),
         name = rs.getString("${columnPrefix}name"),
         profitCenter = entity.profitCenter,
         account = entity.account,
         percent = rs.getBigDecimal("${columnPrefix}percent")
      )
   }
}
