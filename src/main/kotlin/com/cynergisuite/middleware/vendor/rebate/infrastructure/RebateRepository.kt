package com.cynergisuite.middleware.vendor.rebate.infrastructure

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.update
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.AccountStatusType
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountStatusTypeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import com.cynergisuite.middleware.vendor.rebate.RebateEntity
import com.cynergisuite.middleware.vendor.rebate.RebateType
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class RebateRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val accountRepository: AccountRepository,
   private val accountStatusTypeRepository: AccountStatusTypeRepository,
   private val rebateTypeRepository: RebateTypeRepository,
   private val vendorRepository: VendorRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(RebateRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         WITH account AS (
            ${accountRepository.selectBaseQuery()}
         )
         SELECT
            r.id                                                AS r_id,
            r.time_created                                      AS r_time_created,
            r.time_updated                                      AS r_time_updated,
            r.company_id                                        AS r_comp_id,
            r.description                                       AS r_description,
            r.percent                                           AS r_percent,
            r.amount_per_unit                                   AS r_amount_per_unit,
            r.accrual_indicator                                 AS r_accrual_indicator,
            status.id                                           AS status_id,
            status.value                                        AS status_value,
            status.description                                  AS status_description,
            status.localization_code                            AS status_localization_code,
            rebate.id                                           AS rebate_id,
            rebate.value                                        AS rebate_value,
            rebate.description                                  AS rebate_description,
            rebate.localization_code                            AS rebate_localization_code,
            glDebitAcct.account_id                              AS glDebitAcct_id,
            glDebitAcct.account_number                          AS glDebitAcct_number,
            glDebitAcct.account_name                            AS glDebitAcct_name,
            glDebitAcct.account_form_1099_field                 AS glDebitAcct_form_1099_field,
            glDebitAcct.account_corporate_account_indicator     AS glDebitAcct_corporate_account_indicator,
            glDebitAcct.account_comp_id                         AS glDebitAcct_comp_id,
            glDebitAcct.account_type_id                         AS glDebitAcct_type_id,
            glDebitAcct.account_type_value                      AS glDebitAcct_type_value,
            glDebitAcct.account_type_description                AS glDebitAcct_type_description,
            glDebitAcct.account_type_localization_code          AS glDebitAcct_type_localization_code,
            glDebitAcct.account_balance_type_id                 AS glDebitAcct_balance_type_id,
            glDebitAcct.account_balance_type_value              AS glDebitAcct_balance_type_value,
            glDebitAcct.account_balance_type_description        AS glDebitAcct_balance_type_description,
            glDebitAcct.account_balance_type_localization_code  AS glDebitAcct_balance_type_localization_code,
            glDebitAcct.account_status_id                       AS glDebitAcct_status_id,
            glDebitAcct.account_status_value                    AS glDebitAcct_status_value,
            glDebitAcct.account_status_description              AS glDebitAcct_status_description,
            glDebitAcct.account_status_localization_code        AS glDebitAcct_status_localization_code,
            glCreditAcct.account_id                             AS glCreditAcct_id,
            glCreditAcct.account_number                         AS glCreditAcct_number,
            glCreditAcct.account_name                           AS glCreditAcct_name,
            glCreditAcct.account_form_1099_field                AS glCreditAcct_form_1099_field,
            glCreditAcct.account_corporate_account_indicator    AS glCreditAcct_corporate_account_indicator,
            glCreditAcct.account_comp_id                        AS glCreditAcct_comp_id,
            glCreditAcct.account_type_id                        AS glCreditAcct_type_id,
            glCreditAcct.account_type_value                     AS glCreditAcct_type_value,
            glCreditAcct.account_type_description               AS glCreditAcct_type_description,
            glCreditAcct.account_type_localization_code         AS glCreditAcct_type_localization_code,
            glCreditAcct.account_balance_type_id                AS glCreditAcct_balance_type_id,
            glCreditAcct.account_balance_type_value             AS glCreditAcct_balance_type_value,
            glCreditAcct.account_balance_type_description       AS glCreditAcct_balance_type_description,
            glCreditAcct.account_balance_type_localization_code AS glCreditAcct_balance_type_localization_code,
            glCreditAcct.account_status_id                      AS glCreditAcct_status_id,
            glCreditAcct.account_status_value                   AS glCreditAcct_status_value,
            glCreditAcct.account_status_description             AS glCreditAcct_status_description,
            glCreditAcct.account_status_localization_code       AS glCreditAcct_status_localization_code,
            count(*) OVER()                                     AS total_elements
         FROM rebate r
            JOIN account_status_type_domain status ON r.status_type_id = status.id
            JOIN rebate_type_domain rebate         ON r.rebate_type_id = rebate.id
            JOIN account glCreditAcct              ON r.general_ledger_credit_account_id = glCreditAcct.account_id
            LEFT JOIN account glDebitAcct          ON r.general_ledger_debit_account_id = glDebitAcct.account_id
      """
   }

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): RebateEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query = "${selectBaseQuery()}\nWHERE r.id = :id AND r.company_id = :comp_id"

      logger.debug("Searching for Rebate using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ ->
         val rebate = mapRow(rs, company, "r_")

         rebate
      }

      logger.trace("Searching for Rebate: {} resulted in {}", id, found)

      return found
   }

   @ReadOnly
   fun findAll(company: CompanyEntity, page: PageRequest): RepositoryPage<RebateEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "limit" to page.size(), "offset" to page.offset())
      var whereClause = StringBuilder(" WHERE r.company_id = :comp_id ")
      if (page is RebatePageRequest && !page.vendorIds.isNullOrEmpty()) {
         params["vendor_ids"] = page.vendorIds
         whereClause.append(" AND r.id IN (SELECT DISTINCT rebate_id FROM rebate_to_vendor WHERE vendor_id in (<vendor_ids>)) ")
      }
      return jdbc.queryPaged(
         """
            ${selectBaseQuery()}
            $whereClause
            ORDER BY r_${page.snakeSortBy()} ${page.sortDirection()}
            LIMIT :limit OFFSET :offset
         """.trimIndent(),
         params,
         page
      ) { rs, elements ->
         do {
            elements.add(mapRow(rs, company, "r_"))
         } while (rs.next())
      }
   }

   @Transactional
   fun insert(entity: RebateEntity, company: CompanyEntity): RebateEntity {
      logger.debug("Inserting rebate {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO rebate(
            company_id,
            status_type_id,
            description,
            rebate_type_id,
            percent,
            amount_per_unit,
            accrual_indicator,
            general_ledger_debit_account_id,
            general_ledger_credit_account_id
         )
         VALUES (
            :company_id,
            :status_type_id,
            :description,
            :rebate_type_id,
            :percent,
            :amount_per_unit,
            :accrual_indicator,
            :general_ledger_debit_account_id,
            :general_ledger_credit_account_id
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.id,
            "status_type_id" to entity.status.id,
            "description" to entity.description,
            "rebate_type_id" to entity.rebate.id,
            "percent" to entity.percent,
            "amount_per_unit" to entity.amountPerUnit,
            "accrual_indicator" to entity.accrualIndicator,
            "general_ledger_debit_account_id" to entity.generalLedgerDebitAccount?.id,
            "general_ledger_credit_account_id" to entity.generalLedgerCreditAccount.id
         )
      ) { rs, _ ->
         mapRowUpsert(
            rs,
            company,
            entity.status,
            entity.rebate,
            entity.generalLedgerDebitAccount,
            entity.generalLedgerCreditAccount
         )
      }
   }

   @Transactional
   fun update(entity: RebateEntity, company: CompanyEntity): RebateEntity {
      logger.debug("Updating rebate {}", entity)

      return jdbc.updateReturning(
         """
            UPDATE rebate
            SET
               company_id = :company_id,
               status_type_id = :status_type_id,
               description = :description,
               rebate_type_id = :rebate_type_id,
               percent = :percent,
               amount_per_unit = :amount_per_unit,
               accrual_indicator = :accrual_indicator,
               general_ledger_debit_account_id = :general_ledger_debit_account_id,
               general_ledger_credit_account_id = :general_ledger_credit_account_id
            WHERE id = :id
            RETURNING
               *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to company.id,
            "status_type_id" to entity.status.id,
            "description" to entity.description,
            "rebate_type_id" to entity.rebate.id,
            "percent" to entity.percent,
            "amount_per_unit" to entity.amountPerUnit,
            "accrual_indicator" to entity.accrualIndicator,
            "general_ledger_debit_account_id" to entity.generalLedgerDebitAccount?.id,
            "general_ledger_credit_account_id" to entity.generalLedgerCreditAccount.id
         )
      ) { rs, _ ->
         mapRowUpsert(
            rs,
            company,
            entity.status,
            entity.rebate,
            entity.generalLedgerDebitAccount,
            entity.generalLedgerCreditAccount
         )
      }
   }

   @Transactional
   fun disassociateVendorFromRebate(rebate: RebateEntity, vendor: Identifiable) {
      logger.debug("Deleting Rebate To Vendor rebate id {}, vendor id {}", rebate, vendor)

      jdbc.update(
         """
         DELETE FROM rebate_to_vendor
         WHERE rebate_id = :rebate_id AND vendor_id = :vendor_id
         """,
         mapOf(
            "rebate_id" to rebate.id,
            "vendor_id" to vendor.myId()
         )
      )
   }

   @Transactional
   fun assignVendorToRebate(rebate: RebateEntity, vendor: Identifiable) {
      logger.trace("Assigning Vendor {} to Rebate {}", vendor, rebate)

      jdbc.update(
         """
         INSERT INTO rebate_to_vendor (rebate_id, vendor_id)
         VALUES (:rebate_id, :vendor_id)
         """.trimIndent(),
         mapOf(
            "rebate_id" to rebate.id,
            "vendor_id" to vendor.myId()
         )
      )
   }

   private fun mapRow(rs: ResultSet, company: CompanyEntity, columnPrefix: String = EMPTY): RebateEntity {
      val id = rs.getUuid("${columnPrefix}id")
      val vendors = vendorRepository.findVendorIdsByRebate(id, company)

      return RebateEntity(
         id = id,
         vendors = vendors,
         status = accountStatusTypeRepository.mapRow(rs, "status_"),
         description = rs.getString("${columnPrefix}description"),
         rebate = rebateTypeRepository.mapRow(rs, "rebate_"),
         percent = rs.getBigDecimal("${columnPrefix}percent"),
         amountPerUnit = rs.getBigDecimal("${columnPrefix}amount_per_unit"),
         accrualIndicator = rs.getBoolean("${columnPrefix}accrual_indicator"),
         generalLedgerDebitAccount = accountRepository.mapRowOrNull(rs, company, "glDebitAcct_"),
         generalLedgerCreditAccount = accountRepository.mapRow(rs, company, "glCreditAcct_")
      )
   }

   private fun mapRowUpsert(rs: ResultSet, company: CompanyEntity, status: AccountStatusType, rebate: RebateType, generalLedgerDebitAccount: AccountEntity?, generalLedgerCreditAccount: AccountEntity, columnPrefix: String = EMPTY): RebateEntity {
      val id = rs.getUuid("${columnPrefix}id")
      val vendors = vendorRepository.findVendorIdsByRebate(id, company)

      return RebateEntity(
         id = id,
         vendors = vendors,
         status = status,
         description = rs.getString("${columnPrefix}description"),
         rebate = rebate,
         percent = rs.getBigDecimal("${columnPrefix}percent"),
         amountPerUnit = rs.getBigDecimal("${columnPrefix}amount_per_unit"),
         accrualIndicator = rs.getBoolean("${columnPrefix}accrual_indicator"),
         generalLedgerDebitAccount = generalLedgerDebitAccount,
         generalLedgerCreditAccount = generalLedgerCreditAccount
      )
   }
}
