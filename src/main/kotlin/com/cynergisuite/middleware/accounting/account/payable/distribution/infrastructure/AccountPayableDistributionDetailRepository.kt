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
import com.cynergisuite.extensions.update
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionDetailDTO
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionDetailEntity
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
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
class AccountPayableDistributionDetailRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val accountRepository: AccountRepository,
   private val storeRepository: StoreRepository,
   private val accountPayableDistributionTemplateRepository: AccountPayableDistributionTemplateRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableDistributionDetailRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         WITH account_payable_distribution_template AS (
              ${accountPayableDistributionTemplateRepository.selectBaseQuery()}
        ),
         account AS (
            ${accountRepository.selectBaseQuery()}
         )
         SELECT
            apDistDetail.id                                                      AS apDistDetail_id,
            apDistDetail.time_created                                            AS apDistDetail_time_created,
            apDistDetail.time_updated                                            AS apDistDetail_time_updated,
            apDistDetail.percent                                                 AS apDistDetail_percent,
            apDistDetail.profit_center_sfk                                       AS apDistDetail_profit_center_sfk,
            apDistDetail.account_id                                              AS apDistDetail_account_id,
            apDistDetail.template_id                                             AS apDistDetail_template_id,
            apDistDetail.deleted                                                 AS apDistDetail_deleted,
            apDist.apDist_id                                                     AS apDistDetail_apDist_id,
            apDist.apDist_name                                                   AS apDistDetail_apDist_name,
            apDist.apDist_company_id                                             AS apDistDetail_apDist_company_id,
            profitCenter.id                                                AS apDistDetail_profitCenter_id,
            profitCenter.number                                            AS apDistDetail_profitCenter_number,
            profitCenter.name                                              AS apDistDetail_profitCenter_name,
            profitCenter.dataset                                           AS apDistDetail_profitCenter_dataset,
            account.account_id                                             AS apDistDetail_account_id,
            account.account_number                                         AS apDistDetail_account_number,
            account.account_name                                           AS apDistDetail_account_name,
            account.account_form_1099_field                                AS apDistDetail_account_form_1099_field,
            account.account_corporate_account_indicator                    AS apDistDetail_account_corporate_account_indicator,
            account.account_comp_id                                        AS apDistDetail_account_comp_id,
            account.account_deleted                                        AS apDistDetail_account_deleted,
            account.account_type_id                                        AS apDistDetail_account_type_id,
            account.account_type_value                                     AS apDistDetail_account_type_value,
            account.account_type_description                               AS apDistDetail_account_type_description,
            account.account_type_localization_code                         AS apDistDetail_account_type_localization_code,
            account.account_balance_type_id                                AS apDistDetail_account_balance_type_id,
            account.account_balance_type_value                             AS apDistDetail_account_balance_type_value,
            account.account_balance_type_description                       AS apDistDetail_account_balance_type_description,
            account.account_balance_type_localization_code                 AS apDistDetail_account_balance_type_localization_code,
            account.account_status_id                                      AS apDistDetail_account_status_id,
            account.account_status_value                                   AS apDistDetail_account_status_value,
            account.account_status_description                             AS apDistDetail_account_status_description,
            account.account_status_localization_code                       AS apDistDetail_account_status_localization_code,
            account.account_vendor_1099_type_id                            AS apDistDetail_account_vendor_1099_type_id,
            account.account_vendor_1099_type_value                         AS apDistDetail_account_vendor_1099_type_value,
            account.account_vendor_1099_type_description                   AS apDistDetail_account_vendor_1099_type_description,
            account.account_vendor_1099_type_localization_code             AS apDistDetail_account_vendor_1099_type_localization_code,
            (
               SELECT
                  CASE
                     WHEN COUNT(*) > 0 then bank.id
                     WHEN COUNT(*) = 0 then NULL
                  END
               FROM bank
               WHERE bank.general_ledger_account_id = account.account_id
               GROUP BY bank.id LIMIT 1
            ) AS apDistDetail_account_bank_id,
            count(*) OVER() as total_elements
         FROM account_payable_distribution_template_detail apDistDetail
            JOIN account_payable_distribution_template apDist ON apDistDetail.template_id = apDist.apDist_id
            JOIN company comp ON apDist.apDist_company_id = comp.id AND comp.deleted = FALSE
            JOIN system_stores_fimvw profitCenter
               ON profitCenter.dataset = comp.dataset_code
                  AND profitCenter.number = apDistDetail.profit_center_sfk
            JOIN account ON apDistDetail.account_id = account.account_id AND account.account_deleted = FALSE
      """
   }

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): AccountPayableDistributionDetailEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query = "${selectBaseQuery()} WHERE apDistDetail.id = :id AND comp.id = :comp_id AND apDistDetail.deleted = FALSE AND comp.deleted = FALSE"
      val found = jdbc.findFirstOrNull(
         query,
         params
      ) { rs, _ ->
         mapRow(rs, company, "apDistDetail_")
      }

      logger.trace("Searching for AccountPayableDistribution id {}: \nQuery {} \nResulted in {}", id, query, found)

      return found
   }

   @ReadOnly
   fun findAll(
      company: CompanyEntity,
      page: PageRequest
   ): RepositoryPage<AccountPayableDistributionDetailEntity, PageRequest> {
      var totalElements: Long? = null
      val resultList: MutableList<AccountPayableDistributionDetailEntity> = mutableListOf()

      jdbc.query(
         """
         WITH paged AS (
            ${selectBaseQuery()}
            WHERE comp.id = :comp_id AND apDistDetail.deleted = FALSE AND comp.deleted = FALSE
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER by apDistDetail_${page.snakeSortBy()} ${page.sortDirection()}
         LIMIT :limit OFFSET :offset
         """,
         mapOf(
            "comp_id" to company.id,
            "limit" to page.size(),
            "offset" to page.offset()
         )
      ) { rs, _ ->
         resultList.add(mapRow(rs, company, "apDistDetail_"))
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
   fun findAllRecordsByGroup(
      company: CompanyEntity,
      id: UUID,
      page: PageRequest
   ): RepositoryPage<AccountPayableDistributionDetailEntity, PageRequest> {
      return jdbc.queryPaged(
         """
            ${selectBaseQuery()}
            WHERE comp.id = :comp_id AND apDistDetail.template_id = :template_id AND apDistDetail.deleted = FALSE
            ORDER by apDistDetail_apDist_${page.snakeSortBy()} ${page.sortDirection()}
            LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "template_id" to id,
            "limit" to page.size(),
            "offset" to page.offset()
         ),
         page
      ) { rs, elements ->
         do {
            elements.add(mapRow(rs, company, "apDistDetail_"))
         } while (rs.next())
      }
   }

   @Transactional
   fun insert(entity: AccountPayableDistributionDetailEntity, company: CompanyEntity): AccountPayableDistributionDetailEntity {
      logger.debug("Inserting AccountPayableDistribution {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO account_payable_distribution_template_detail(profit_center_sfk, account_id, company_id, percent, template_id)
	      VALUES (:profit_center_sfk, :account_id, :company_id, :percent, :template_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "profit_center_sfk" to entity.profitCenter.myNumber(),
            "account_id" to entity.account.id,
            "company_id" to company.id,
            "percent" to entity.percent,
            "template_id" to entity.distributionTemplate.myId()
         )
      ) { rs, _ ->
         mapRow(rs, entity)
      }
   }

   @Transactional
   fun update(entity: AccountPayableDistributionDetailEntity, company: CompanyEntity): AccountPayableDistributionDetailEntity {
      logger.debug("Updating AccountPayableDistribution {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE account_payable_distribution_template_detail
         SET
            profit_center_sfk = :profit_center_sfk,
            account_id = :account_id,
            company_id = :company_id,
            percent = :percent,
            template_id = :template_id
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "profit_center_sfk" to entity.profitCenter.myNumber(),
            "account_id" to entity.account.id,
            "company_id" to company.id,
            "percent" to entity.percent,
            "template_id" to entity.distributionTemplate.myId()
         )
      ) { rs, _ ->
         mapRow(rs, entity)
      }
   }

   @Transactional
   fun bulkUpdate(entities: List<AccountPayableDistributionDetailEntity>, company: CompanyEntity): List<AccountPayableDistributionDetailEntity> {
      logger.debug("Updating AccountPayableDistributionTemplateEntity {}", entities)

      if(entities.any{ it.id != null }) {
         deleteNotIn(entities[0].distributionTemplate.myId()!!, entities)
      }
      val updated = mutableListOf<AccountPayableDistributionDetailEntity>()

      entities.map { upsert(it, company) }
         .forEach { updated.add(it) }

      return updated
   }

   @Transactional
   fun delete(id: UUID, company: CompanyEntity) {
      logger.debug("Deleting AccountPayableDistribution with id={}", id)

      val rowsAffected = jdbc.softDelete(
         """
         UPDATE account_payable_distribution_template_detail
         SET deleted = TRUE
         WHERE id = :id AND company_id = :company_id AND deleted = FALSE
         """,
         mapOf("id" to id, "company_id" to company.id),
         "account_payable_distribution_template_detail"
      )
      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) throw NotFoundException(id)
   }

   @Transactional
   fun deleteByTemplateId(id: UUID, company: CompanyEntity) {
      logger.debug("Deleting all AccountPayableDistributionDetails with template id={}", id)

      val rowsAffected = jdbc.update(
         """
            UPDATE account_payable_distribution_template_detail
            SET deleted = TRUE
            WHERE template_id = :id AND deleted = FALSE
         """,
         mapOf("id" to id)
      )
      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) throw NotFoundException(id)

   }

   @ReadOnly
   fun percentTotalForGroup(dto: AccountPayableDistributionDetailDTO, company: CompanyEntity): BigDecimal {
      logger.debug("Percent total for account payable distribution group with name={}", dto.distributionTemplate!!.myId())

      return jdbc.queryForObject(
         """SELECT COALESCE(SUM(percent), 0)
            FROM account_payable_distribution_template_detail
            WHERE template_id = :template_id
         """.trimIndent(),
         mapOf(
            "template_id" to dto.distributionTemplate!!.myId()
         ),
         BigDecimal::class.java
      )
   }

   fun mapRow(rs: ResultSet, company: CompanyEntity, columnPrefix: String = EMPTY): AccountPayableDistributionDetailEntity {
      return AccountPayableDistributionDetailEntity(
         id = rs.getUuid("${columnPrefix}id"),
         profitCenter = storeRepository.mapRow(rs, company, "${columnPrefix}profitCenter_"),
         account = accountRepository.mapRow(rs, company, "${columnPrefix}account_"),
         percent = rs.getBigDecimal("${columnPrefix}percent"),
         distributionTemplate = accountPayableDistributionTemplateRepository.mapRow(rs, company,"${columnPrefix}apDist_")
      )
   }

   private fun mapRow(
      rs: ResultSet,
      entity: AccountPayableDistributionDetailEntity,
      columnPrefix: String = EMPTY
   ): AccountPayableDistributionDetailEntity {
      return AccountPayableDistributionDetailEntity(
         id = rs.getUuid("${columnPrefix}id"),
         profitCenter = entity.profitCenter,
         account = entity.account,
         percent = rs.getBigDecimal("${columnPrefix}percent"),
         distributionTemplate = entity.distributionTemplate,
      )
   }

   private fun mapRowName(rs: ResultSet): String {
      return rs.getString("name")
   }

   @Transactional
   fun deleteNotIn(id: UUID, apdList: List<AccountPayableDistributionDetailEntity>): Int {

      return jdbc.update(
         """
        UPDATE account_payable_distribution_template_detail
        SET deleted = TRUE
        WHERE template_id = :template_id
            AND id NOT IN(<ids>)
            AND deleted = FALSE
        RETURNING
           *
         """.trimIndent(),
         mapOf(
            "template_id" to id,
            "ids" to apdList.asSequence().map { it.id }.toList().filterNotNull(),
         )
      )
   }

   fun upsert(apd: AccountPayableDistributionDetailEntity, company: CompanyEntity): AccountPayableDistributionDetailEntity =
      if (apd.id == null) {
         insert(apd, company)
      } else {
         update(apd, company)
      }
}
