package com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.softDelete
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionEntity
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.sql.ResultSet
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class AccountPayableDistributionRepository @Inject constructor(
   private val jdbc: Jdbi,
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
            apDist.time_created                                            AS apDist_time_created,
            apDist.time_updated                                            AS apDist_time_updated,
            apDist.name                                                    AS apDist_name,
            apDist.percent                                                 AS apDist_percent,
            apDist.profit_center_sfk                                       AS apDist_profit_center_sfk,
            apDist.deleted                                                 AS apDist_deleted,
            profitCenter.id                                                AS apDist_profitCenter_id,
            profitCenter.number                                            AS apDist_profitCenter_number,
            profitCenter.name                                              AS apDist_profitCenter_name,
            profitCenter.dataset                                           AS apDist_profitCenter_dataset,
            account.account_id                                             AS apDist_account_id,
            account.account_number                                         AS apDist_account_number,
            account.account_name                                           AS apDist_account_name,
            account.account_form_1099_field                                AS apDist_account_form_1099_field,
            account.account_corporate_account_indicator                    AS apDist_account_corporate_account_indicator,
            account.account_comp_id                                        AS apDist_account_comp_id,
            account.account_deleted                                        AS apDist_account_deleted,
            account.account_type_id                                        AS apDist_account_type_id,
            account.account_type_value                                     AS apDist_account_type_value,
            account.account_type_description                               AS apDist_account_type_description,
            account.account_type_localization_code                         AS apDist_account_type_localization_code,
            account.account_balance_type_id                                AS apDist_account_balance_type_id,
            account.account_balance_type_value                             AS apDist_account_balance_type_value,
            account.account_balance_type_description                       AS apDist_account_balance_type_description,
            account.account_balance_type_localization_code                 AS apDist_account_balance_type_localization_code,
            account.account_status_id                                      AS apDist_account_status_id,
            account.account_status_value                                   AS apDist_account_status_value,
            account.account_status_description                             AS apDist_account_status_description,
            account.account_status_localization_code                       AS apDist_account_status_localization_code
         FROM account_payable_distribution_template apDist
            JOIN company comp ON apDist.company_id = comp.id AND comp.deleted = FALSE
            JOIN fastinfo_prod_import.store_vw profitCenter
               ON profitCenter.dataset = comp.dataset_code
                  AND profitCenter.number = apDist.profit_center_sfk
            JOIN account ON account.account_id = apDist.account_id AND account.account_deleted = FALSE
      """
   }

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): AccountPayableDistributionEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query = "${selectBaseQuery()} WHERE apDist.id = :id AND comp.id = :comp_id AND apDist.deleted = FALSE AND comp.deleted = FALSE"
      val found = jdbc.findFirstOrNull(
         query,
         params
      ) { rs, _ ->
         mapRow(rs, company, "apDist_")
      }

      logger.trace("Searching for AccountPayableDistribution id {}: \nQuery {} \nResulted in {}", id, query, found)

      return found
   }

   @ReadOnly
   fun findAll(
      company: CompanyEntity,
      page: PageRequest
   ): RepositoryPage<AccountPayableDistributionEntity, PageRequest> {
      var totalElements: Long? = null
      val resultList: MutableList<AccountPayableDistributionEntity> = mutableListOf()

      jdbc.query(
         """
         WITH paged AS (
            ${selectBaseQuery()}
            WHERE comp.id = :comp_id AND apDist.deleted = FALSE AND comp.deleted = FALSE
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER by apDist_${page.snakeSortBy()} ${page.sortDirection()}
         LIMIT :limit OFFSET :offset
         """,
         mapOf(
            "comp_id" to company.id,
            "limit" to page.size(),
            "offset" to page.offset()
         )
      ) { rs, _ ->
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

   @ReadOnly
   fun findAllGroups(company: CompanyEntity, page: PageRequest): RepositoryPage<String, PageRequest> {
      return jdbc.queryPaged(
         """
            WITH paged AS (
            SELECT DISTINCT name
            FROM account_payable_distribution_template
            WHERE company_id = :comp_id AND deleted = FALSE
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
	      ORDER BY name
         LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "limit" to page.size(),
            "offset" to page.offset()
         ),
         page
      ) { rs, elements ->
         do {
            elements.add(mapRowName(rs))
         } while (rs.next())
      }
   }

   @ReadOnly
   fun findAllRecordsByGroup(
      company: CompanyEntity,
      name: String,
      page: PageRequest
   ): RepositoryPage<AccountPayableDistributionEntity, PageRequest> {
      var totalElements: Long? = null
      val resultList: MutableList<AccountPayableDistributionEntity> = mutableListOf()

      jdbc.query(
         """
         WITH paged AS (
            ${selectBaseQuery()}
            WHERE comp.id = :comp_id AND apDist.name = :name AND apDist.deleted = FALSE
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER by apDist_${page.snakeSortBy()} ${page.sortDirection()}
         LIMIT :limit OFFSET :offset
         """,
         mapOf(
            "comp_id" to company.id,
            "name" to name,
            "limit" to page.size(),
            "offset" to page.offset()
         )
      ) { rs, _ ->
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
   fun insert(entity: AccountPayableDistributionEntity, company: CompanyEntity): AccountPayableDistributionEntity {
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
            "company_id" to company.id,
            "percent" to entity.percent
         )
      ) { rs, _ ->
         mapRow(rs, entity)
      }
   }

   @Transactional
   fun update(entity: AccountPayableDistributionEntity, company: CompanyEntity): AccountPayableDistributionEntity {
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
            "profit_center_sfk" to entity.profitCenter.myNumber(),
            "account_id" to entity.account.id,
            "company_id" to company.id,
            "percent" to entity.percent
         )
      ) { rs, _ ->
         mapRow(rs, entity)
      }
   }

   @Transactional
   fun delete(id: UUID, company: CompanyEntity) {
      logger.debug("Deleting AccountPayableDistribution with id={}", id)

      val rowsAffected = jdbc.softDelete(
         """
         UPDATE account_payable_distribution_template
         SET deleted = TRUE
         WHERE id = :id AND company_id = :company_id
         """,
         mapOf("id" to id, "company_id" to company.id),
         "account_payable_distribution_template"
      )
      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) throw NotFoundException(id)
   }

   @ReadOnly
   fun percentTotalForGroup(company: CompanyEntity, name: String): BigDecimal {
      logger.debug("Percent total for account payable distribution group with name={}", name)

      return jdbc.queryForObject(
         """SELECT COALESCE(SUM(percent), 0)
            FROM account_payable_distribution_template
            WHERE company_id = :company_id AND name = :name
         """.trimIndent(),
         mapOf(
            "company_id" to company.id,
            "name" to name
         ),
         BigDecimal::class.java
      )
   }

   fun mapRow(rs: ResultSet, company: CompanyEntity, columnPrefix: String = EMPTY): AccountPayableDistributionEntity {
      return AccountPayableDistributionEntity(
         id = rs.getUuid("${columnPrefix}id"),
         name = rs.getString("${columnPrefix}name"),
         profitCenter = storeRepository.mapRow(rs, company, "${columnPrefix}profitCenter_"),
         account = accountRepository.mapRow(rs, company, "${columnPrefix}account_"),
         percent = rs.getBigDecimal("${columnPrefix}percent")
      )
   }

   private fun mapRow(
      rs: ResultSet,
      entity: AccountPayableDistributionEntity,
      columnPrefix: String = EMPTY
   ): AccountPayableDistributionEntity {
      return AccountPayableDistributionEntity(
         id = rs.getUuid("${columnPrefix}id"),
         name = rs.getString("${columnPrefix}name"),
         profitCenter = entity.profitCenter,
         account = entity.account,
         percent = rs.getBigDecimal("${columnPrefix}percent")
      )
   }

   private fun mapRowName(rs: ResultSet): String {
      return rs.getString("name")
   }
}
